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
package org.eclipse.wb.internal.core.nls.bundle.eclipse.old;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.converter.StringConverter;
import org.eclipse.wb.internal.core.model.util.WorkspaceUtils;
import org.eclipse.wb.internal.core.nls.bundle.AbstractBundleSource;
import org.eclipse.wb.internal.core.nls.edit.IEditableSource;
import org.eclipse.wb.internal.core.nls.model.AbstractSource;
import org.eclipse.wb.internal.core.nls.model.IKeyGeneratorStrategy;
import org.eclipse.wb.internal.core.nls.model.LocaleInfo;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.state.EditorState;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.StringLiteral;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.util.List;

/**
 * Source for standard (may be old) Eclipse accessor class.
 *
 * @author scheglov_ke
 * @coverage core.nls
 */
public final class EclipseSource extends AbstractBundleSource {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Possible sources
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String getBundleComment() {
    return "Eclipse messages class";
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
    IJavaElement[] packageElements = pkg.getChildren();
    for (int i = 0; i < packageElements.length; i++) {
      ICompilationUnit unit = (ICompilationUnit) packageElements[i];
      if (unit.getElementName().endsWith("Messages.java")) {
        IType type = unit.findPrimaryType();
        if (type != null) {
          // check for field BUNDLE_NAME
          {
            IField field_bundleName = type.getField("BUNDLE_NAME");
            if (!field_bundleName.exists()) {
              continue;
            }
          }
          // check for method getString(key[, default value])
          {
            IMethod method_getString_1 =
                CodeUtils.findMethodSingleType(type, "getString(java.lang.String)");
            IMethod method_getString_2 =
                CodeUtils.findMethodSingleType(type, "getString(java.lang.String,java.lang.String)");
            if (method_getString_1 == null && method_getString_2 == null) {
              continue;
            }
          }
          // OK, this is probably correct source
          try {
            String accessorClassName = pkg.getElementName() + "." + type.getElementName();
            AbstractSource source = new EclipseSource(root, accessorClassName, null);
            sources.add(source);
          } catch (Throwable e) {
            DesignerPlugin.log(e);
          }
        }
      }
    }
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
      String accessorClassName = expressionInfo.m_accessorClassName;
      EclipseSource source = getNewOrExistingSource(component, accessorClassName, sources);
      source.onKeyAdd(component, expressionInfo.m_key);
      return source;
    }
    return null;
  }

  /**
   * Find existing source with same accessor or create new one.
   */
  private static EclipseSource getNewOrExistingSource(JavaInfo component,
      String accessorClassName,
      List<AbstractSource> sources) throws Exception {
    for (AbstractSource abstractSource : sources) {
      if (abstractSource instanceof EclipseSource) {
        EclipseSource source = (EclipseSource) abstractSource;
        if (source.m_accessorClassName.equals(accessorClassName)) {
          return source;
        }
      }
    }
    // create new source
    return new EclipseSource(component.getRootJava(), accessorClassName, null);
  }

  /**
   * Parse given expression and if it is valid Eclipse style accessor class access, extract resource
   * bundle name, key and optional default value.
   */
  private static ExpressionInfo getExpressionInfo(JavaInfo component, Expression expression)
      throws Exception {
    if (expression instanceof MethodInvocation) {
      // check for getString(key)
      MethodInvocation getString_invocation = (MethodInvocation) expression;
      int argumentCount = getString_invocation.arguments().size();
      {
        boolean is_getString =
            getString_invocation.getName().getIdentifier().equals("getString")
                && (argumentCount == 1 || argumentCount == 2)
                && getString_invocation.arguments().get(0) instanceof StringLiteral;
        if (!is_getString) {
          return null;
        }
      }
      // check for Messages.getString(key)
      String accessorClassName;
      {
        Expression invocationExpression = getString_invocation.getExpression();
        if (invocationExpression == null) {
          return null;
        }
        accessorClassName = AstNodeUtils.getFullyQualifiedName(invocationExpression, false);
      }
      // check that accessor has BUNDLE_NAME field
      {
        String bundleName = accessor_getBundleName(component, accessorClassName);
        if (bundleName == null) {
          return null;
        }
      }
      // all was checked, we can create expression information
      {
        // prepare key
        StringLiteral keyLiteral = (StringLiteral) getString_invocation.arguments().get(0);
        String key = keyLiteral.getLiteralValue();
        // prepare default value
        Expression def_argument =
            argumentCount == 2 ? (Expression) getString_invocation.arguments().get(1) : null;
        StringLiteral defaultLiteral =
            def_argument instanceof StringLiteral ? (StringLiteral) def_argument : null;
        String defaultValue = defaultLiteral != null ? defaultLiteral.getLiteralValue() : null;
        // return information about expression
        ExpressionInfo expressionInfo =
            new ExpressionInfo(expression,
                accessorClassName,
                keyLiteral,
                key,
                defaultLiteral,
                defaultValue);
        expression.setProperty(NLS_EXPRESSION_INFO, expressionInfo);
        return expressionInfo;
      }
    }
    return null;
  }

  /**
   * Get bundle name (value of field BUNDLE_NAME) from given accessor class.
   */
  private static String accessor_getBundleName(JavaInfo component, String accessorClassName)
      throws Exception {
    Class<?> accessorClass = loadClass(component, accessorClassName);
    //Field bundleNameField = accessorClass.getDeclaredField("BUNDLE_NAME");
    Field bundleNameField = ReflectionUtils.getFieldByName(accessorClass, "BUNDLE_NAME");
    if (bundleNameField != null) {
      String bundleName = (String) bundleNameField.get(null);
      return bundleName.replace('/', '.');
    }
    return null;
  }

  /**
   * Check if given accessor class has "getString" with default value.
   */
  private static boolean accessor_withDefaultValue(JavaInfo component, String accessorClassName)
      throws Exception {
    Class<?> accessorClass = loadClass(component, accessorClassName);
    return ReflectionUtils.getMethodBySignature(
        accessorClass,
        "getString(java.lang.String,java.lang.String)") != null;
  }

  /**
   * @return the {@link Class} loaded from {@link EditorState} {@link ClassLoader}.
   */
  private static Class<?> loadClass(JavaInfo component, String className)
      throws ClassNotFoundException {
    EditorState editorState = EditorState.get(component.getEditor());
    return editorState.getEditorLoader().loadClass(className);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Expression information
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Information about expression. We store it in expression property to avoid parsing every time.
   */
  private static class ExpressionInfo extends BasicExpressionInfo {
    private final String m_accessorClassName;
    private StringLiteral m_defaultExpression;
    private String m_defaultValue;

    public ExpressionInfo(Expression expression,
        String accessorClassName,
        StringLiteral keyExpression,
        String key,
        StringLiteral defaultExpression,
        String defaultValue) {
      super(expression, keyExpression, key);
      m_accessorClassName = accessorClassName;
      m_defaultExpression = defaultExpression;
      m_defaultValue = defaultValue;
    }
  }

  /**
   * Get expression information.
   */
  private ExpressionInfo getExpressionInfo(Expression expression) {
    return (ExpressionInfo) getBasicExpressionInfo(expression);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance fields
  //
  ////////////////////////////////////////////////////////////////////////////
  private final String m_accessorClassName;
  /**
   * <code>true</code> if method "get(key)" has default value, i.e. "get(key, defaultValue)".
   */
  private final boolean m_withDefaultValue;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public EclipseSource(JavaInfo root, String accessorClassName, String bundleName) throws Exception {
    super(root, bundleName != null ? bundleName : accessor_getBundleName(root, accessorClassName));
    // initialize fields
    if (accessorClassName != null) {
      m_accessorClassName = accessorClassName;
      m_withDefaultValue = accessor_withDefaultValue(root, accessorClassName);
    } else {
      m_accessorClassName = null;
      m_withDefaultValue = false;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getTypeTitle() throws Exception {
    return "Eclipse messages class " + m_accessorClassName;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Value access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getValue(Expression expression) throws Exception {
    // prepare key and default value
    ExpressionInfo expressionInfo = getExpressionInfo(expression);
    String key = expressionInfo.m_key;
    String defaultValue = expressionInfo.m_defaultValue;
    // return value
    String value = getValue(key);
    return value != null ? value : defaultValue;
  }

  @Override
  public void setValue(Expression expression, String value) throws Exception {
    ExpressionInfo expressionInfo = getExpressionInfo(expression);
    String key = expressionInfo.m_key;
    LocaleInfo localeInfo = getLocaleInfo();
    // change value in bundle
    setValueInBundle(key, value);
    // if locale is default, change default value in source
    if (localeInfo.isDefault() && expressionInfo.m_defaultExpression != null) {
      String code = StringConverter.INSTANCE.toJavaSource(m_root, value);
      StringLiteral newDefaultLiteral =
          (StringLiteral) m_root.getEditor().replaceExpression(
              expressionInfo.m_defaultExpression,
              code);
      // change expression information
      expressionInfo.m_defaultExpression = newDefaultLiteral;
      expressionInfo.m_defaultValue = value;
    }
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
    String code;
    {
      code =
          m_accessorClassName + ".getString(" + StringConverter.INSTANCE.toJavaSource(m_root, key);
      if (m_withDefaultValue) {
        String value = (String) property.getValue();
        code += ", " + StringConverter.INSTANCE.toJavaSource(m_root, value);
      }
      code += ")";
    }
    // replace expression
    ExpressionInfo expressionInfo = (ExpressionInfo) replaceExpression_getInfo(expression, code);
    // add //$NON-NLS-xxx$ comments
    {
      addNonNLSComment(expressionInfo.m_keyExpression);
      if (m_withDefaultValue) {
        addNonNLSComment(expressionInfo.m_defaultExpression);
      }
    }
    // return expression information
    return expressionInfo;
  }

  @Override
  protected void apply_removeNonNLSComments(BasicExpressionInfo basicExpressionInfo)
      throws Exception {
    super.apply_removeNonNLSComments(basicExpressionInfo);
    ExpressionInfo expressionInfo = (ExpressionInfo) basicExpressionInfo;
    removeNonNLSComment((StringLiteral) expressionInfo.m_keyExpression);
    removeNonNLSComment(expressionInfo.m_defaultExpression);
  }

  /**
   * Create NLS source for given root and parameters.
   */
  public static EclipseSource apply_create(IEditableSource editable, JavaInfo root, Object o)
      throws Exception {
    // prepare parameters
    SourceParameters parameters = (SourceParameters) o;
    // check, may be accessor class file exists
    if (!parameters.m_accessorExists) {
      // prepare accessor class source
      String template;
      {
        String templateName =
            parameters.m_withDefaultValue ? "newAccessor_default.jvt" : "newAccessor.jvt";
        template = IOUtils.toString(EclipseSource.class.getResourceAsStream(templateName));
        template =
            StringUtils.replace(template, "%PACKAGE_NAME%", parameters.m_accessorPackageName);
        template = StringUtils.replace(template, "%CLASS_NAME%", parameters.m_accessorClassName);
        template = StringUtils.replace(template, "%BUNDLE_NAME%", parameters.m_propertyBundleName);
      }
      // create accessor class file
      {
        IFolder accessorFolder = (IFolder) parameters.m_accessorPackage.getUnderlyingResource();
        IFile accessorFile = accessorFolder.getFile(parameters.m_accessorClassName + ".java");
        accessorFile.create(
            new ByteArrayInputStream(template.getBytes()),
            true,
            new NullProgressMonitor());
      }
      // create property bundle
      createPropertyBundleFile(parameters.m_propertyPackage, parameters.m_propertyFileName, null);
      // wait until accessor class will be compiled
      WorkspaceUtils.waitForClass(root.getEditor(), parameters.m_accessorFullClassName);
    }
    // create source
    return new EclipseSource(root, parameters.m_accessorFullClassName, null);
  }
}
