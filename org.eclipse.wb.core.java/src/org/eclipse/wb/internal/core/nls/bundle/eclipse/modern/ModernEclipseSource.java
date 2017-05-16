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
package org.eclipse.wb.internal.core.nls.bundle.eclipse.modern;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.util.WorkspaceUtils;
import org.eclipse.wb.internal.core.nls.bundle.pure.AbstractPureBundleSource;
import org.eclipse.wb.internal.core.nls.edit.IEditableSource;
import org.eclipse.wb.internal.core.nls.model.AbstractSource;
import org.eclipse.wb.internal.core.nls.model.IKeyGeneratorStrategy;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.AstVisitorEx;
import org.eclipse.wb.internal.core.utils.ast.BodyDeclarationTarget;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.utils.state.EditorState;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Source for modern Eclipse accessor class (subclass of org.eclipse.osgi.util.NLS).
 *
 * @author scheglov_ke
 * @coverage core.nls
 */
public final class ModernEclipseSource extends AbstractPureBundleSource {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Static fields
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final IKeyGeneratorStrategy MODERN_KEY_GENERATOR = new IKeyGeneratorStrategy() {
    public final String generateBaseKey(JavaInfo component, GenericProperty property) {
      String typeName = getTypeName(component).replace('.', '_');
      String componentName = component.getVariableSupport().getComponentName();
      String titleName = property.getTitle().replace(' ', '_');
      return typeName + "_" + componentName + "_" + titleName;
    }
  };

  ////////////////////////////////////////////////////////////////////////////
  //
  // Possible sources
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String getBundleComment() {
    return "Eclipse modern messages class";
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
    // check that there is "NLS" type
    IType typeNLS = root.getEditor().getJavaProject().findType("org.eclipse.osgi.util.NLS");
    if (typeNLS != null) {
      IJavaElement[] packageElements = pkg.getChildren();
      for (int i = 0; i < packageElements.length; i++) {
        ICompilationUnit unit = (ICompilationUnit) packageElements[i];
        IType type = unit.findPrimaryType();
        if (type != null) {
          // check that type is is successor of NLS
          if (CodeUtils.isSuccessorOf(type, typeNLS)) {
            try {
              String accessorClassName = type.getFullyQualifiedName();
              AbstractSource source = new ModernEclipseSource(root, accessorClassName, null);
              sources.add(source);
            } catch (Throwable e) {
              DesignerPlugin.log(e);
            }
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
    ExpressionInfo expressionInfo = getExpressionInfo(expression);
    if (expressionInfo != null) {
      String accessorClassName = expressionInfo.m_accessorClassName;
      ModernEclipseSource source = getNewOrExistingSource(component, accessorClassName, sources);
      source.onKeyAdd(component, expressionInfo.m_key);
      return source;
    }
    return null;
  }

  /**
   * Find existing source with same accessor or create new one.
   */
  private static ModernEclipseSource getNewOrExistingSource(JavaInfo component,
      String accessorClassName,
      List<AbstractSource> sources) throws Exception {
    for (AbstractSource abstractSource : sources) {
      if (abstractSource instanceof ModernEclipseSource) {
        ModernEclipseSource source = (ModernEclipseSource) abstractSource;
        if (source.m_accessorClassName.equals(accessorClassName)) {
          return source;
        }
      }
    }
    return new ModernEclipseSource(component.getRootJava(), accessorClassName, null);
  }

  /**
   * Parse given expression and if it is valid Eclipse style accessor class access, extract resource
   * bundle name and key.
   */
  private static ExpressionInfo getExpressionInfo(Expression expression) {
    if (expression instanceof QualifiedName) {
      QualifiedName qualifiedName = (QualifiedName) expression;
      Name qualifier = qualifiedName.getQualifier();
      // check that qualifier is successor of NLS
      if (!AstNodeUtils.isSuccessorOf(
          AstNodeUtils.getTypeBinding(qualifier),
          "org.eclipse.osgi.util.NLS")) {
        return null;
      }
      // prepare parameters
      String accessorClassName = AstNodeUtils.getFullyQualifiedName(qualifier, true);
      SimpleName keyExpression = qualifiedName.getName();
      String key = keyExpression.getIdentifier();
      //
      ExpressionInfo expressionInfo =
          new ExpressionInfo(expression, accessorClassName, keyExpression, key);
      expression.setProperty(NLS_EXPRESSION_INFO, expressionInfo);
      return expressionInfo;
    }
    return null;
  }

  /**
   * Get bundle name (value of field BUNDLE_NAME) from given accessor class.
   */
  private static String accessor_getBundleName(JavaInfo component, String accessorClassName)
      throws Exception {
    ClassLoader editorLoader = EditorState.get(component.getEditor()).getEditorLoader();
    Class<?> accessorClass = editorLoader.loadClass(accessorClassName);
    Field bundleNameField = accessorClass.getDeclaredField("BUNDLE_NAME");
    bundleNameField.setAccessible(true);
    return (String) bundleNameField.get(null);
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

    public ExpressionInfo(Expression expression,
        String accessorClassName,
        Expression keyExpression,
        String key) {
      super(expression, keyExpression, key);
      m_accessorClassName = accessorClassName;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance fields
  //
  ////////////////////////////////////////////////////////////////////////////
  private final String m_accessorClassName;
  private final AstEditor m_accessorEditor;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ModernEclipseSource(JavaInfo root, String accessorClassName, String bundleName)
      throws Exception {
    super(root, bundleName != null ? bundleName : accessor_getBundleName(root, accessorClassName));
    // initialize fields
    if (accessorClassName != null) {
      m_accessorClassName = accessorClassName;
      // prepare AST editor for accessor
      {
        IType accessor_type = m_root.getEditor().getJavaProject().findType(m_accessorClassName);
        ICompilationUnit accessor_unit = accessor_type.getCompilationUnit();
        m_accessorEditor = new AstEditor(accessor_unit);
      }
    } else {
      m_accessorClassName = null;
      m_accessorEditor = null;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getTypeTitle() throws Exception {
    return "Modern Eclipse messages class " + m_accessorClassName;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Edit support
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected IKeyGeneratorStrategy getKeyGeneratorStrategy() {
    return MODERN_KEY_GENERATOR;
  }

  @Override
  public void apply_addKey(String key) throws Exception {
    addKey(key);
    super.apply_addKey(key);
  }

  @Override
  protected BasicExpressionInfo apply_externalize_replaceExpression(GenericProperty property,
      String key) throws Exception {
    // replace expression
    Expression expression = property.getExpression();
    String source = m_accessorClassName + "." + key;
    Expression newExpression = m_root.getEditor().replaceExpression(expression, source);
    // side effect of this invocation is that ExpressionInfo placed in newExpression
    return getExpressionInfo(newExpression);
  }

  @Override
  protected Expression apply_renameKey_replaceKeyExpression(AstEditor editor,
      Expression keyExpression,
      String newKey) throws Exception {
    editor.setIdentifier((SimpleName) keyExpression, newKey);
    return keyExpression;
  }

  @Override
  protected void apply_renameKeys_pre(final Map<String, String> oldToNew) throws Exception {
    m_accessorEditor.getAstUnit().accept(new AstVisitorEx() {
      @Override
      public void postVisitEx(ASTNode node) throws Exception {
        if (node instanceof FieldDeclaration) {
          FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
          if (fieldDeclaration.fragments().size() == 1) {
            VariableDeclarationFragment vdf =
                (VariableDeclarationFragment) fieldDeclaration.fragments().get(0);
            String fieldName = vdf.getName().getIdentifier();
            String newFieldName = oldToNew.get(fieldName);
            if (newFieldName != null) {
              m_accessorEditor.setIdentifier(vdf.getName(), newFieldName);
            }
          }
        }
      }
    });
    commitAccessorChanges();
  }

  @Override
  protected void apply_internalizeKeys_post(final Set<String> keys) throws Exception {
    m_accessorEditor.getAstUnit().accept(new AstVisitorEx() {
      @Override
      public void postVisitEx(ASTNode node) throws Exception {
        if (node instanceof FieldDeclaration) {
          FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
          if (fieldDeclaration.fragments().size() == 1) {
            VariableDeclarationFragment vdf =
                (VariableDeclarationFragment) fieldDeclaration.fragments().get(0);
            String fieldName = vdf.getName().getIdentifier();
            if (keys.contains(fieldName)) {
              m_accessorEditor.removeBodyDeclaration(fieldDeclaration);
            }
          }
        }
      }
    });
    commitAccessorChanges();
  }

  /**
   * Create NLS source for given root and parameters.
   */
  public static ModernEclipseSource apply_create(IEditableSource editable, JavaInfo root, Object o)
      throws Exception {
    // prepare parameters
    SourceParameters parameters = (SourceParameters) o;
    // create accessor class file
    if (!parameters.m_accessorExists) {
      // prepare accessor class source
      String template;
      {
        template =
            IOUtils.toString(ModernEclipseSource.class.getResourceAsStream("newAccessor.jvt"));
        template =
            StringUtils.replace(
                template,
                "%PACKAGE_NAME%",
                parameters.m_accessorPackage.getElementName());
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
    return new ModernEclipseSource(root, parameters.m_accessorFullClassName, null);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private void addKey(String key) throws Exception {
    // prepare last field declaration
    FieldDeclaration lastFieldDeclaration;
    {
      TypeDeclaration typeDeclaration =
          (TypeDeclaration) m_accessorEditor.getAstUnit().types().get(0);
      FieldDeclaration[] fields = typeDeclaration.getFields();
      lastFieldDeclaration = fields[fields.length - 1];
    }
    // add new field declaration
    m_accessorEditor.addFieldDeclaration(
        "public static String " + key + ";",
        new BodyDeclarationTarget(lastFieldDeclaration, false));
    //
    commitAccessorChanges();
  }

  /**
   * Commits changes in accessor {@link AstEditor}, including saving {@link ICompilationUnit}
   * buffer.
   */
  private void commitAccessorChanges() throws Exception {
    m_accessorEditor.saveChanges(true);
  }
}
