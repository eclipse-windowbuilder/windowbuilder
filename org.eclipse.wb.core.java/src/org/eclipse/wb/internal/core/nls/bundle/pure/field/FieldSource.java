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
package org.eclipse.wb.internal.core.nls.bundle.pure.field;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.eval.ExecutionFlowUtils;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.converter.StringConverter;
import org.eclipse.wb.internal.core.nls.bundle.pure.AbstractPureBundleSource;
import org.eclipse.wb.internal.core.nls.edit.IEditableSource;
import org.eclipse.wb.internal.core.nls.model.AbstractSource;
import org.eclipse.wb.internal.core.nls.model.IKeyGeneratorStrategy;
import org.eclipse.wb.internal.core.utils.IOUtils2;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.BodyDeclarationTarget;
import org.eclipse.wb.internal.core.utils.state.EditorState;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import org.apache.commons.lang.StringUtils;

import java.io.InputStream;
import java.util.List;

/**
 * Source for ResourceBundle field usage, for example:
 *
 * private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("the.bundle.name");
 *
 * m_button.setText( BUNDLE.getString("key.for.property") );
 *
 * @author scheglov_ke
 * @coverage core.nls
 */
public final class FieldSource extends AbstractPureBundleSource {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Possible sources
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final String BUNDLE_COMMENT = "Field ResourceBundle: ";

  @Override
  protected String getBundleComment() {
    return BUNDLE_COMMENT + m_fieldName;
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
    for (Object o : nonJavaResources) {
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
        String fieldName;
        {
          InputStream is = file.getContents(true);
          String firstLine = IOUtils2.readFirstLine(is);
          if (firstLine == null || !firstLine.startsWith("#" + BUNDLE_COMMENT)) {
            continue;
          }
          fieldName = firstLine.substring(1 + BUNDLE_COMMENT.length());
        }
        // OK, this is probably correct source
        try {
          String bundleName =
              pkg.getElementName()
                  + "."
                  + StringUtils.substring(fileName, 0, -".properties".length());
          AbstractSource source = new FieldSource(root, bundleName, fieldName);
          sources.add(source);
        } catch (Throwable e) {
          DesignerPlugin.log(e);
        }
      }
    }
    //
    return sources;
  }

  @Override
  public void attachPossible() throws Exception {
    addField(m_root, m_bundleName, m_fieldName);
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
      String fieldName = expressionInfo.m_fieldName;
      FieldSource source = getNewOrExistingSource(component, bundleName, fieldName, sources);
      source.onKeyAdd(component, expressionInfo.m_key);
      return source;
    }
    return null;
  }

  /**
   * Find existing source with same bundle name or create new one.
   */
  private static FieldSource getNewOrExistingSource(JavaInfo component,
      String bundleName,
      String fieldName,
      List<AbstractSource> sources) throws Exception {
    for (AbstractSource abstractSource : sources) {
      if (abstractSource instanceof FieldSource) {
        FieldSource source = (FieldSource) abstractSource;
        if (source.m_bundleName.equals(bundleName)) {
          return source;
        }
      }
    }
    return new FieldSource(component.getRootJava(), bundleName, fieldName);
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
      // check that invocation expression is field
      if (getString_invocation.getExpression() instanceof SimpleName) {
        // check that field is used
        SimpleName fieldExpression = (SimpleName) getString_invocation.getExpression();
        if (AstNodeUtils.getFullyQualifiedName(fieldExpression, false).equals(
            "java.util.ResourceBundle")) {
          // prepare initializer for field
          Expression fieldInitializer;
          {
            EditorState editorState = EditorState.get(component.getEditor());
            ASTNode assignment =
                ExecutionFlowUtils.getLastAssignment(
                    editorState.getFlowDescription(),
                    fieldExpression);
            if (!(assignment instanceof VariableDeclarationFragment)) {
              return null;
            }
            VariableDeclarationFragment fieldAssignment = (VariableDeclarationFragment) assignment;
            fieldInitializer = fieldAssignment.getInitializer();
          }
          // prepare bundle name
          String bundleName = getBundleName_for_getBundle(fieldInitializer);
          if (bundleName == null) {
            return null;
          }
          // prepare key
          StringLiteral keyLiteral = (StringLiteral) getString_invocation.arguments().get(0);
          String key = keyLiteral.getLiteralValue();
          // return information about expression
          ExpressionInfo expressionInfo =
              new ExpressionInfo(expression,
                  bundleName,
                  fieldExpression.getIdentifier(),
                  keyLiteral,
                  key);
          expression.setProperty(NLS_EXPRESSION_INFO, expressionInfo);
          return expressionInfo;
        }
      }
    }
    return null;
  }

  /**
   * Check that given expression is ResourceBundle.getBundle(bundleName) and return bundle name
   * (return <code>null</code> in other case).
   */
  private static String getBundleName_for_getBundle(Expression expression) {
    // get Bundle name from @wbp.nls.resourceBundle tag
    {
      BodyDeclaration declaration =
          AstNodeUtils.getEnclosingNode(expression, BodyDeclaration.class);
      if (declaration != null) {
        TagElement tagElement = AstNodeUtils.getJavaDocTag(declaration, "@wbp.nls.resourceBundle");
        if (tagElement != null && tagElement.fragments().size() == 1) {
          return tagElement.fragments().get(0).toString().trim();
        }
      }
    }
    // get Bundle name from ResourceBundle.getBundle(String)
    if (expression instanceof MethodInvocation) {
      MethodInvocation invocation = (MethodInvocation) expression;
      // check for: getBundle(bundleName)
      {
        boolean is_getBundle =
            invocation.getName().getIdentifier().equals("getBundle")
                && invocation.arguments().size() == 1
                && invocation.arguments().get(0) instanceof StringLiteral;
        boolean is_getBundleWithLocale =
            invocation.getName().getIdentifier().equals("getBundle")
                && invocation.arguments().size() == 2
                && invocation.arguments().get(0) instanceof StringLiteral
                && AstNodeUtils.getFullyQualifiedName(
                    (Expression) invocation.arguments().get(1),
                    false).equals("java.util.Locale");
        if (!is_getBundle && !is_getBundleWithLocale) {
          return null;
        }
      }
      // check for: ResourceBundle
      {
        Expression bundleExpression = invocation.getExpression();
        String bundleTypeName = AstNodeUtils.getFullyQualifiedName(bundleExpression, true);
        if (!bundleTypeName.equals("java.util.ResourceBundle")) {
          return null;
        }
      }
      // prepare bundle name
      StringLiteral bundleNameLiteral = (StringLiteral) invocation.arguments().get(0);
      return bundleNameLiteral.getLiteralValue();
    }
    // no, this is not ResourceBundle.getBundle(bundleName) or ResourceBundle.getBundle(bundleName,Locale)
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
    private final String m_bundleName;
    private final String m_fieldName;

    public ExpressionInfo(Expression expression,
        String bundleName,
        String fieldName,
        StringLiteral keyExpression,
        String key) {
      super(expression, keyExpression, key);
      m_bundleName = bundleName;
      m_fieldName = fieldName;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance fields
  //
  ////////////////////////////////////////////////////////////////////////////
  private final String m_fieldName;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public FieldSource(JavaInfo root, String bundleName, String fieldName) throws Exception {
    super(root, bundleName);
    m_fieldName = fieldName;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getTypeTitle() throws Exception {
    return "ResourceBundle in field '" + m_fieldName + "'";
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
    Expression expression = property.getExpression();
    // prepare code
    String code =
        m_fieldName + ".getString(" + StringConverter.INSTANCE.toJavaSource(m_root, key) + ")";
    // replace expression
    ExpressionInfo expressionInfo = (ExpressionInfo) replaceExpression_getInfo(expression, code);
    // add //$NON-NLS-xxx$ comment for key
    addNonNLSComment(expressionInfo.m_keyExpression);
    // return expression information
    return expressionInfo;
  }

  @Override
  protected void apply_removeNonNLSComments(BasicExpressionInfo basicExpressionInfo)
      throws Exception {
    ExpressionInfo expressionInfo = (ExpressionInfo) basicExpressionInfo;
    removeNonNLSComment((StringLiteral) expressionInfo.m_keyExpression);
  }

  public static FieldSource apply_create(IEditableSource editable, JavaInfo root, Object o)
      throws Exception {
    // prepare parameters
    SourceParameters parameters = (SourceParameters) o;
    String bundleName = parameters.m_propertyBundleName;
    String fieldName = parameters.m_fieldName;
    // create property bundle
    createPropertyBundleFile(parameters.m_propertyPackage, parameters.m_propertyFileName, null);
    // add field
    addField(root, bundleName, fieldName);
    // create source
    return new FieldSource(root, bundleName, fieldName);
  }

  /**
   * Add field with ResourceBundle definition.
   */
  private static void addField(JavaInfo root, String bundleName, String fieldName) throws Exception {
    // prepare code
    String code =
        "private static final java.util.ResourceBundle "
            + fieldName
            + " = java.util.ResourceBundle.getBundle("
            + StringConverter.INSTANCE.toJavaSource(root, bundleName)
            + "); //$NON-NLS-1$";
    // add field
    TypeDeclaration typeDeclaration = JavaInfoUtils.getTypeDeclaration(root);
    root.getEditor().addFieldDeclaration(code, new BodyDeclarationTarget(typeDeclaration, true));
  }
}
