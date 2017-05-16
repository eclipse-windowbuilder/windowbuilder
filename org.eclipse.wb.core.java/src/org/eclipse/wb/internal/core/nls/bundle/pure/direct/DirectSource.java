/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.nls.bundle.pure.direct;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.converter.StringConverter;
import org.eclipse.wb.internal.core.nls.bundle.pure.AbstractPureBundleSource;
import org.eclipse.wb.internal.core.nls.edit.IEditableSource;
import org.eclipse.wb.internal.core.nls.model.AbstractSource;
import org.eclipse.wb.internal.core.nls.model.IKeyGeneratorStrategy;
import org.eclipse.wb.internal.core.utils.IOUtils2;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.StringLiteral;

import org.apache.commons.lang.StringUtils;

import java.io.InputStream;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Source for direct {@link ResourceBundle} usage, for example:
 *
 * ResourceBundle.getBundle("the.bundle.name").getString("key.for.property")
 *
 * @author scheglov_ke
 * @coverage core.nls
 */
public final class DirectSource extends AbstractPureBundleSource {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Possible sources
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final String BUNDLE_COMMENT = "Direct ResourceBundle";

  @Override
  protected String getBundleComment() {
    return BUNDLE_COMMENT;
  }

  /**
   * Return "possible" sources that exist in given package.
   *
   * "Possible" source is source that exists in current package, but is not used in current unit. We
   * show "possible" sources only if there are no "real" sources.
   */
  public static List<AbstractSource> getPossibleSources(JavaInfo root, IPackageFragment pkg)
      throws Exception {
    List<AbstractSource> sources = Lists.newArrayList();
    //
    Object[] nonJavaResources = pkg.getNonJavaResources();
    for (int i = 0; i < nonJavaResources.length; i++) {
      Object o = nonJavaResources[i];
      if (o instanceof IFile) {
        IFile file = (IFile) o;
        String fileName = file.getName();
        // we need .properties files
        if (!fileName.endsWith(".properties")) {
          continue;
        }
        // we need only main (default) bundles
        if (fileName.indexOf('_') != -1) {
          continue;
        }
        // check first line for required comment
        InputStream is = file.getContents(true);
        String firstLine = IOUtils2.readFirstLine(is);
        if (firstLine == null || !firstLine.startsWith("#" + BUNDLE_COMMENT)) {
          continue;
        }
        // OK, this is probably correct source
        try {
          String bundleName =
              pkg.getElementName()
                  + "."
                  + StringUtils.substring(fileName, 0, -".properties".length());
          AbstractSource source = new DirectSource(root, bundleName);
          sources.add(source);
        } catch (Throwable e) {
          DesignerPlugin.log(e);
        }
      }
    }
    //
    return sources;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Creation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Parse given expression and return NLSSource for it (new or existing from list).
   */
  public static AbstractSource get(JavaInfo component,
      GenericProperty property,
      Expression expression,
      List<AbstractSource> sources) throws Exception {
    ExpressionInfo expressionInfo = getExpressionInfo(component, expression);
    if (expressionInfo != null) {
      String bundleName = expressionInfo.m_bundleName;
      DirectSource source = getNewOrExistingSource(component, bundleName, sources);
      source.onKeyAdd(component, expressionInfo.m_key);
      return source;
    }
    return null;
  }

  /**
   * Find existing source with same bundle name or create new one.
   */
  private static DirectSource getNewOrExistingSource(JavaInfo component,
      String bundleName,
      List<AbstractSource> sources) throws Exception {
    for (AbstractSource abstractSource : sources) {
      if (abstractSource instanceof DirectSource) {
        DirectSource source = (DirectSource) abstractSource;
        if (source.m_bundleName.equals(bundleName)) {
          return source;
        }
      }
    }
    return new DirectSource(component.getRootJava(), bundleName);
  }

  /**
   * Parse given expression and if it is valid Eclipse style messages class access, extract resource
   * bundle name, key and optional default value.
   */
  private static ExpressionInfo getExpressionInfo(JavaInfo component, Expression expression) {
    if (expression instanceof MethodInvocation) {
      // check for getString(key)
      MethodInvocation getString_invocation = (MethodInvocation) expression;
      {
        boolean is_getString =
            getString_invocation.getName().getIdentifier().equals("getString")
                && getString_invocation.arguments().size() == 1
                && getString_invocation.arguments().get(0) instanceof StringLiteral;
        if (!is_getString) {
          return null;
        }
      }
      // check for getBundle(bundleName)
      MethodInvocation getBundle_invocation;
      {
        if (!(getString_invocation.getExpression() instanceof MethodInvocation)) {
          return null;
        }
        getBundle_invocation = (MethodInvocation) getString_invocation.getExpression();
        //
        boolean is_getBundle =
            getBundle_invocation.getName().getIdentifier().equals("getBundle")
                && getBundle_invocation.arguments().size() == 1
                && getBundle_invocation.arguments().get(0) instanceof StringLiteral;
        if (!is_getBundle) {
          return null;
        }
      }
      // check that getBundle_invocation.getExpression() is reference on type ResourceBundle
      {
        Expression possible_ResourceBundle = getBundle_invocation.getExpression();
        String typeName = AstNodeUtils.getFullyQualifiedName(possible_ResourceBundle, false);
        if (!typeName.equals("java.util.ResourceBundle")) {
          return null;
        }
      }
      // all was checked, we can create expression information
      {
        // prepare bundle name
        StringLiteral bundleNameLiteral = (StringLiteral) getBundle_invocation.arguments().get(0);
        String bundleName = bundleNameLiteral.getLiteralValue();
        // prepare key
        StringLiteral keyLiteral = (StringLiteral) getString_invocation.arguments().get(0);
        String key = keyLiteral.getLiteralValue();
        // return information about expression
        ExpressionInfo expressionInfo =
            new ExpressionInfo(expression, bundleNameLiteral, bundleName, keyLiteral, key);
        expression.setProperty(NLS_EXPRESSION_INFO, expressionInfo);
        return expressionInfo;
      }
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Expression information
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Information about expression. We store it in expression property to avoid parsing every time.
   */
  protected static class ExpressionInfo extends BasicExpressionInfo {
    private final StringLiteral m_bundleNameExpression;
    private final String m_bundleName;

    public ExpressionInfo(Expression expression,
        StringLiteral bundleNameExpression,
        String bundleName,
        StringLiteral keyExpression,
        String key) {
      super(expression, keyExpression, key);
      m_bundleNameExpression = bundleNameExpression;
      m_bundleName = bundleName;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DirectSource(JavaInfo root, String bundleName) throws Exception {
    super(root, bundleName);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getTypeTitle() throws Exception {
    return "Direct ResourceBundle usage";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Edit support
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected IKeyGeneratorStrategy getKeyGeneratorStrategy() {
    return KEY_GENERATOR;
  }

  @Override
  protected Expression apply_renameKey_replaceKeyExpression(AstEditor editor,
      Expression keyExpression,
      String newKey) throws Exception {
    String newSource = StringConverter.INSTANCE.toJavaSource(m_root, newKey);
    return editor.replaceExpression(keyExpression, newSource);
  }

  @Override
  protected BasicExpressionInfo apply_externalize_replaceExpression(GenericProperty property,
      String key) throws Exception {
    // prepare code
    String code =
        "java.util.ResourceBundle.getBundle("
            + StringConverter.INSTANCE.toJavaSource(m_root, m_bundleName)
            + ").getString("
            + StringConverter.INSTANCE.toJavaSource(m_root, key)
            + ")";
    // replace expression
    Expression expression = property.getExpression();
    ExpressionInfo expressionInfo = (ExpressionInfo) replaceExpression_getInfo(expression, code);
    // add //$NON-NLS-xxx$ comments
    {
      addNonNLSComment(expressionInfo.m_bundleNameExpression);
      addNonNLSComment(expressionInfo.m_keyExpression);
    }
    // return expression information
    return expressionInfo;
  }

  @Override
  protected void apply_removeNonNLSComments(BasicExpressionInfo basicExpressionInfo)
      throws Exception {
    ExpressionInfo expressionInfo = (ExpressionInfo) basicExpressionInfo;
    removeNonNLSComment((StringLiteral) expressionInfo.m_keyExpression);
    removeNonNLSComment(expressionInfo.m_bundleNameExpression);
  }

  public static DirectSource apply_create(IEditableSource editable, JavaInfo root, Object o)
      throws Exception {
    // prepare parameters
    SourceParameters parameters = (SourceParameters) o;
    // create property bundle
    createPropertyBundleFile(parameters.m_propertyPackage, parameters.m_propertyFileName, null);
    // create source
    return new DirectSource(root, parameters.m_propertyBundleName);
  }
}
