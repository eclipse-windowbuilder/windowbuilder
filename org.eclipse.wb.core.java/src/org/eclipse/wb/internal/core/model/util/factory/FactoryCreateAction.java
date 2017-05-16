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
package org.eclipse.wb.internal.core.model.util.factory;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.eclipse.wb.core.editor.IDesignPageSite;
import org.eclipse.wb.core.editor.palette.model.CategoryInfo;
import org.eclipse.wb.core.editor.palette.model.IPaletteSite;
import org.eclipse.wb.core.editor.palette.model.PaletteInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.editor.palette.command.factory.FactoryAddCommand;
import org.eclipse.wb.internal.core.model.ModelMessages;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.description.AbstractInvocationDescription;
import org.eclipse.wb.internal.core.model.description.ConstructorDescription;
import org.eclipse.wb.internal.core.model.description.MethodDescription;
import org.eclipse.wb.internal.core.model.description.ParameterDescription;
import org.eclipse.wb.internal.core.model.description.helpers.FactoryDescriptionHelper;
import org.eclipse.wb.internal.core.model.variable.NamesManager;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.BodyDeclarationTarget;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.dialogfields.AbstractValidationTitleAreaDialog;
import org.eclipse.wb.internal.core.utils.dialogfields.ComboDialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.DialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.DialogFieldUtils;
import org.eclipse.wb.internal.core.utils.dialogfields.IDialogFieldListener;
import org.eclipse.wb.internal.core.utils.dialogfields.IStringButtonAdapter;
import org.eclipse.wb.internal.core.utils.dialogfields.StringButtonDialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.StringDialogField;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;
import org.eclipse.wb.internal.core.utils.jdt.ui.JdtUiUtils;
import org.eclipse.wb.internal.core.utils.jdt.ui.PackageRootAndPackageSelectionDialogField;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.core.utils.ui.UiUtils;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Set;

/**
 * {@link Action} for creating new factory method for this component.
 *
 * @author scheglov_ke
 * @coverage core.model.util
 */
public final class FactoryCreateAction extends Action {
  private final JavaInfo m_component;
  private final AstEditor m_editor;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public FactoryCreateAction(JavaInfo component) {
    m_component = component;
    m_editor = m_component.getEditor();
    // configure action look
    setImageDescriptor(DesignerPlugin.getImageDescriptor("actions/factory/factory_new.png"));
    setText(ModelMessages.FactoryCreateAction_text);
    // prepare collections for creation/invocations
    ExecutionUtils.runLog(new RunnableEx() {
      public void run() throws Exception {
        fillCollections();
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Creation/configuration collections
  //
  ////////////////////////////////////////////////////////////////////////////
  private final CreationInfo m_creationInfo = new CreationInfo();
  private final List<InvocationInfo> m_invocations = Lists.newArrayList();

  /**
   * Information about single argument {@link Expression} in component creation or invocation.
   */
  private static final class ArgumentInfo {
    ParameterDescription m_description;
    Expression m_expression;
    boolean m_hasVariables = false;
    boolean m_parameter = false;
    String m_source;
    String m_parameterName;
  }
  /**
   * Information about component creation or single {@link MethodInvocation} existing for component.
   */
  private static abstract class AbstractInvocationInfo {
    Expression m_expression;
    List<ArgumentInfo> m_arguments = Lists.newArrayList();
  }
  /**
   * Information about component creation.
   */
  private static final class CreationInfo extends AbstractInvocationInfo {
  }
  /**
   * Information about single {@link MethodInvocation} existing for component.
   */
  private static final class InvocationInfo extends AbstractInvocationInfo {
    MethodInvocation m_invocation;
    boolean m_canExtract = true;
    boolean m_extract = false;
    String m_signature;
  }

  /**
   * Fills creation and configuration collections.
   */
  private void fillCollections() {
    // creation
    if (m_component.getCreationSupport() instanceof ConstructorCreationSupport) {
      ConstructorCreationSupport creationSupport =
          (ConstructorCreationSupport) m_component.getCreationSupport();
      ClassInstanceCreation creation = creationSupport.getCreation();
      m_creationInfo.m_expression = creation;
      // fill arguments
      List<Expression> arguments = DomGenerics.arguments(creation);
      ConstructorDescription description = creationSupport.getDescription();
      fillArguments(m_creationInfo.m_arguments, arguments, description);
    }
    // invocations
    for (ASTNode relatedNode : m_component.getRelatedNodes()) {
      if (relatedNode.getLocationInParent() == MethodInvocation.EXPRESSION_PROPERTY) {
        MethodInvocation invocation = (MethodInvocation) relatedNode.getParent();
        String signature = AstNodeUtils.getMethodSignature(invocation);
        // prepare description
        MethodDescription description = m_component.getDescription().getMethod(signature);
        if (description == null) {
          continue;
        }
        // check, may be this method is disabled
        if (description.hasTrueTag("noFactory")) {
          continue;
        }
        // add invocation information
        InvocationInfo invocationInfo = new InvocationInfo();
        invocationInfo.m_expression = invocation;
        invocationInfo.m_invocation = invocation;
        invocationInfo.m_signature = signature;
        m_invocations.add(invocationInfo);
        // fill arguments
        List<Expression> arguments = DomGenerics.arguments(invocation);
        fillArguments(invocationInfo.m_arguments, arguments, description);
        // check that no arguments with variables
        for (ArgumentInfo argument : invocationInfo.m_arguments) {
          if (argument.m_hasVariables) {
            invocationInfo.m_canExtract = false;
          }
        }
        // extract by default
        invocationInfo.m_extract = true;
      }
    }
  }

  /**
   * Fills collection of {@link ArgumentInfo} using collection of {@link Expression} arguments and
   * description on invocation.
   */
  private void fillArguments(List<ArgumentInfo> argumentInfos,
      List<Expression> arguments,
      AbstractInvocationDescription description) {
    for (int i = 0; i < arguments.size(); i++) {
      Expression argument = arguments.get(i);
      ParameterDescription parameter = description.getParameter(i);
      // add argument information
      ArgumentInfo argumentInfo = new ArgumentInfo();
      argumentInfo.m_description = parameter;
      argumentInfo.m_expression = argument;
      argumentInfo.m_hasVariables = hasVariables(argument);
      argumentInfo.m_parameter = argumentInfo.m_hasVariables;
      argumentInfo.m_source = m_editor.getExternalSource(argument, null);
      argumentInfo.m_parameterName = parameter.getName();
      argumentInfos.add(argumentInfo);
    }
  }

  /**
   * @return <code>true</code> if {@link Expression} has references on some variables (or
   *         {@link ThisExpression}), so can not be used as value.
   */
  private static boolean hasVariables(Expression expression) {
    final boolean[] hasVariables = new boolean[1];
    expression.accept(new ASTVisitor() {
      @Override
      public void endVisit(ThisExpression node) {
        hasVariables[0] |= true;
      }

      @Override
      public void endVisit(SimpleName node) {
        hasVariables[0] |= AstNodeUtils.isVariable(node);
      }
    });
    return hasVariables[0];
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Run
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void run() {
    FactoryCreateDialog dialog = new FactoryCreateDialog();
    if (dialog.open() == Window.OK) {
      ExecutionUtils.runLog(new RunnableEx() {
        public void run() throws Exception {
          generate(true);
          m_editor.commitChanges();
        }
      });
      // force reparse
      {
        IDesignPageSite pageSite = IDesignPageSite.Helper.getSite(m_component);
        if (pageSite != null) {
          pageSite.reparse();
        }
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implementation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Returns first {@link ICompilationUnit} that can contains factory.
   *
   * @return the first {@link ICompilationUnit} with factory, or <code>null</code> if not factories
   *         found in current package.
   */
  private ICompilationUnit findFactoryUnit() throws Exception {
    IPackageFragment currentPackage = (IPackageFragment) m_editor.getModelUnit().getParent();
    List<ICompilationUnit> factoryUnits =
        FactoryDescriptionHelper.getFactoryUnits(m_editor, currentPackage);
    return !factoryUnits.isEmpty() ? factoryUnits.get(0) : null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // State
  //
  ////////////////////////////////////////////////////////////////////////////
  // basic elements
  private IPackageFragmentRoot m_sourceFolder;
  private IPackageFragment m_package;
  private String m_className;
  private String m_qualifiedClassName;
  private String m_methodName;
  private CategoryInfo m_paletteCategory;
  // prepared for factory creation
  private boolean m_canPreview;
  private List<String> m_generate_methodComments;
  private String m_generate_methodHeader;
  private String m_generate_methodSignature;
  private List<String> m_generate_methodBody;
  private String m_generate_invocationArguments;

  /**
   * @return the preview source of factory method that will be generated.
   */
  private String getFactoryPreviewSource() throws Exception {
    ICompilationUnit factoryUnit =
        m_editor.getModelUnit().getWorkingCopy(new NullProgressMonitor());
    try {
      String className = "__wbp_tmpUnit";
      String source = "";
      // add package
      {
        PackageDeclaration packageDeclaration = m_editor.getAstUnit().getPackage();
        if (packageDeclaration != null) {
          source += "package " + packageDeclaration.getName().toString() + ";\n";
        }
      }
      // add empty class
      source +=
          StringUtils.join(new String[]{
              "class " + className + " {",
              "  private int ___filler___ = 0;",
              "}"}, "\n");
      factoryUnit.getBuffer().setContents(source);
      // parse and generate
      AstEditor factoryEditor = new AstEditor(factoryUnit);
      generateFactory(factoryEditor, className);
      // return only source of method
      TypeDeclaration typeDeclaration =
          AstNodeUtils.getTypeByName(factoryEditor.getAstUnit(), className);
      String indentation = factoryEditor.getGeneration().getIndentation(1);
      return indentation + factoryEditor.getSource(typeDeclaration.getMethods()[0]);
    } finally {
      factoryUnit.discardWorkingCopy();
    }
  }

  /**
   * Generates new factory method, replaces component creation and remove extracted invocations.
   *
   * @param replaceWithFactory
   *          is <code>true</code> if existing component creation should be replace with factory
   *          creation.
   */
  private void generate(boolean replaceWithFactory) throws Exception {
    // prepare factory unit
    ICompilationUnit factoryUnit;
    {
      String factoryUnitName = m_className + ".java";
      factoryUnit = m_package.getCompilationUnit(factoryUnitName);
      if (!factoryUnit.exists()) {
        String eol = m_editor.getGeneration().getEndOfLine();
        String factoryUnitSource =
            StringUtils.join(new String[]{
                "package " + m_package.getElementName() + ";",
                "",
                "public final class " + m_className + " {",
                "}"}, eol);
        m_package.createCompilationUnit(factoryUnitName, factoryUnitSource, false, null);
      }
    }
    boolean sameCompilationUnit = m_editor.getModelUnit().equals(factoryUnit);
    // prepare AST editor
    AstEditor factoryEditor;
    if (sameCompilationUnit) {
      factoryEditor = m_editor;
    } else {
      factoryEditor = new AstEditor(factoryUnit);
    }
    // generate factory
    generateFactory(factoryEditor, m_className);
    // save/compile factory
    {
      factoryUnit.save(null, true);
      factoryUnit.getBuffer().save(null, true);
      ProjectUtils.waitForAutoBuild();
    }
    // add factory on palette
    if (m_paletteCategory != null) {
      String id = "custom_" + System.currentTimeMillis();
      String name = m_generate_methodSignature;
      String description =
          "Class: " + m_qualifiedClassName + "\nMethod: " + m_generate_methodSignature;
      IPaletteSite.Helper.getSite(m_component).addCommand(
          new FactoryAddCommand(id,
              name,
              description,
              true,
              m_qualifiedClassName,
              m_generate_methodSignature,
              true,
              m_paletteCategory));
    }
    // replace creation, if enabled
    if (replaceWithFactory) {
      // replace creation with factory invocation
      {
        String creationSource = "";
        {
          creationSource += (sameCompilationUnit ? "" : m_qualifiedClassName + ".") + m_methodName;
          creationSource += "(";
          creationSource += m_generate_invocationArguments;
          creationSource += ")";
        }
        m_editor.replaceExpression(m_creationInfo.m_expression, creationSource);
      }
      // remove extracted invocations
      for (InvocationInfo invocation : m_invocations) {
        if (invocation.m_extract) {
          m_editor.removeEnclosingStatement(invocation.m_invocation);
        }
      }
    }
  }

  /**
   * Generates factory method in given {@link AstEditor} and returns arguments for using this method
   * for creation.
   *
   * @param factoryEditor
   *          the {@link AstEditor} to generate factory method.
   * @return the arguments for component creation using factory method.
   */
  private void generateFactory(AstEditor factoryEditor, String className) throws Exception {
    // add factory method
    {
      TypeDeclaration typeDeclaration =
          AstNodeUtils.getTypeByName(factoryEditor.getAstUnit(), className);
      BodyDeclarationTarget target = new BodyDeclarationTarget(typeDeclaration, false);
      MethodDeclaration methodDeclaration =
          factoryEditor.addMethodDeclaration(m_generate_methodHeader, m_generate_methodBody, target);
      factoryEditor.setJavadoc(
          methodDeclaration,
          Iterables.toArray(m_generate_methodComments, String.class));
    }
    // commit changes to factory unit
    factoryEditor.commitChanges();
  }

  /**
   * Prepares information for generating factory method and new creation, using existing
   * configuration of {@link #m_creationInfo} and {@link #m_invocations}.
   */
  private void prepareFactoryMethod() {
    // prepare identifiers
    final Set<String> usedIdentifiers = Sets.newTreeSet();
    String componentTypeName = m_component.getDescription().getComponentClass().getName();
    String componentName =
        generateUniqueIdentifier(usedIdentifiers, NamesManager.getName(m_component));
    // add method
    m_qualifiedClassName = m_package.getElementName() + "." + m_className;
    m_generate_methodSignature = m_methodName + "(";
    m_generate_invocationArguments = "";
    {
      // prepare list of JavaDoc comments
      m_generate_methodComments = Lists.newArrayList();
      m_generate_methodComments.add("@wbp.factory");
      // prepare method header
      {
        String header = "";
        header +=
            "public static " + m_component.getDescription().getComponentClass().getName() + " ";
        header += m_methodName + "(";
        boolean firstParameter = true;
        // prepare all parameters
        List<ArgumentInfo> parameters;
        {
          parameters = Lists.newArrayList();
          for (ArgumentInfo argument : m_creationInfo.m_arguments) {
            if (argument.m_parameter) {
              parameters.add(argument);
            }
          }
          for (InvocationInfo invocation : m_invocations) {
            if (invocation.m_extract) {
              for (ArgumentInfo argument : invocation.m_arguments) {
                if (argument.m_parameter) {
                  parameters.add(argument);
                }
              }
            }
          }
        }
        // generate parameters (for declaration) and arguments (for invocation)
        for (ArgumentInfo argument : parameters) {
          // prepare parameter name
          if (argument.m_description.isParent()) {
            argument.m_parameterName = "parent";
          } else {
            argument.m_parameterName =
                generateUniqueIdentifier(usedIdentifiers, argument.m_description.getName());
          }
          // add "comma"
          if (!firstParameter) {
            header += ", ";
            m_generate_methodSignature += ",";
            m_generate_invocationArguments += ", ";
          }
          firstParameter = false;
          // add parameter to header
          header += argument.m_description.getType().getName();
          header += " ";
          header += argument.m_parameterName;
          // add parameter description in JavaDoc
          if (!argument.m_description.isParent()) {
            String descriptionSource = argument.m_source;
            if (descriptionSource.equals("getClass()")) {
              descriptionSource = "{wbp_class}";
            }
            m_generate_methodComments.add("@wbp.factory.parameter.source "
                + argument.m_parameterName
                + " "
                + descriptionSource);
          }
          // add type to signature
          m_generate_methodSignature += argument.m_description.getType().getName();
          // add argument to invocation
          m_generate_invocationArguments += argument.m_source;
        }
        header += ")";
        m_generate_methodSignature += ")";
        m_generate_methodHeader = header;
      }
      // prepare creation source
      String creationSource = getFactorySource(m_creationInfo);
      // prepare body lines
      {
        List<String> bodyLines = Lists.newArrayList();
        // create component
        bodyLines.add(componentTypeName + " " + componentName + " = " + creationSource + ";");
        // invocations
        for (InvocationInfo invocation : m_invocations) {
          if (invocation.m_extract) {
            bodyLines.add(componentName + getFactorySource(invocation) + ";");
          }
        }
        // return component
        bodyLines.add("return " + componentName + ";");
        m_generate_methodBody = bodyLines;
      }
    }
  }

  /**
   * Generates new identifier based on given name, and not present in used identifiers. After
   * generation adds new identifier into {@link Set} of used identifiers.
   *
   * @return the new generated identifier.
   */
  private static String generateUniqueIdentifier(final Set<String> usedIdentifiers, String baseName) {
    String newIdentifier = CodeUtils.generateUniqueName(baseName, new Predicate<String>() {
      public boolean apply(String t) {
        return !usedIdentifiers.contains(t);
      }
    });
    usedIdentifiers.add(newIdentifier);
    return newIdentifier;
  }

  /**
   * @return the source ready to use in factory, with arguments replaced with parameters, where
   *         needed.
   */
  private String getFactorySource(final AbstractInvocationInfo invocation) {
    return m_editor.getExternalSource(invocation.m_expression, new Function<ASTNode, String>() {
      public String apply(ASTNode from) {
        // replace arguments with parameters
        for (ArgumentInfo argument : invocation.m_arguments) {
          if (argument.m_parameter && argument.m_expression == from) {
            return argument.m_parameterName;
          }
        }
        // replace getClass() with ClassLiteral source
        if (from instanceof MethodInvocation) {
          MethodInvocation invocationNode = (MethodInvocation) from;
          if (invocationNode.getExpression() == null
              && invocationNode.getName().getIdentifier().equals("getClass")
              && invocationNode.arguments().isEmpty()) {
            TypeDeclaration enclosingType = AstNodeUtils.getEnclosingType(invocationNode);
            return AstNodeUtils.getFullyQualifiedName(enclosingType, false) + ".class";
          }
        }
        // replace "expression" of method invocation with empty string
        if (invocation instanceof InvocationInfo) {
          InvocationInfo methodInvocation = (InvocationInfo) invocation;
          if (from == methodInvocation.m_invocation.getExpression()) {
            return "";
          }
        }
        // use usual expression source
        return null;
      }
    });
  }

  /**
   * Validates the currently entered source folder, package and factory class name.
   *
   * @return the error message, or <code>null</code> if values are valid.
   */
  private String validate() throws Exception {
    m_canPreview = false;
    // validate source folder
    {
      if (m_sourceFolder == null || !m_sourceFolder.exists()) {
        return ModelMessages.FactoryCreateAction_validateInvalidSourceFolder;
      }
    }
    // validate package
    {
      if (m_package == null || !m_package.exists()) {
        return ModelMessages.FactoryCreateAction_validateInvalidPackage;
      } else if (m_package.getElementName().length() == 0) {
        return ModelMessages.FactoryCreateAction_validateDefaultPackage;
      }
    }
    // now we know, that basic elements are valid, so we can prepare method
    m_canPreview = true;
    prepareFactoryMethod();
    // validate class name
    {
      IStatus status = JavaConventions.validateJavaTypeName(m_className);
      if (m_className.length() == 0) {
        return ModelMessages.FactoryCreateAction_validateEmptyClass;
      } else if (m_className.indexOf('.') != -1) {
        return ModelMessages.FactoryCreateAction_validateDotInClass;
      } else if (status.getSeverity() != IStatus.OK) {
        return status.getMessage();
      }
    }
    // validate method name
    {
      IStatus status = JavaConventions.validateMethodName(m_methodName);
      if (m_methodName.length() == 0) {
        return ModelMessages.FactoryCreateAction_validateEmptyMethod;
      } else if (status.getSeverity() != IStatus.OK) {
        return status.getMessage();
      }
    }
    // check that new factory method is unique
    {
      IType factoryType = m_editor.getJavaProject().findType(m_qualifiedClassName);
      if (factoryType != null
          && CodeUtils.findMethodSingleType(factoryType, m_generate_methodSignature) != null) {
        return "Method " + m_generate_methodSignature + " already exists.";
      }
    }
    // OK
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Dialog
  //
  ////////////////////////////////////////////////////////////////////////////
  private final class FactoryCreateDialog extends AbstractValidationTitleAreaDialog {
    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public FactoryCreateDialog() {
      super(DesignerPlugin.getShell(),
          DesignerPlugin.getDefault(),
          ModelMessages.FactoryCreateAction_dialogShellTitle,
          ModelMessages.FactoryCreateAction_dialogTitle,
          DesignerPlugin.getImage("actions/factory/factory_banner.png"),
          ModelMessages.FactoryCreateAction_dialogMessage);
      setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // GUI
    //
    ////////////////////////////////////////////////////////////////////////////
    private List<CategoryInfo> m_paletteCategories;
    private PackageRootAndPackageSelectionDialogField m_packageField;
    private StringButtonDialogField m_classField;
    private StringDialogField m_methodField;
    private ComboDialogField m_categoryField;
    private SourceViewer m_previewViewer;

    @Override
    protected void createControls(Composite container) {
      m_fieldsContainer = container;
      GridLayoutFactory.create(container).columns(3);
      // parameters
      createParametersComposite(container);
      // separator
      {
        Label separator = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
        GridDataFactory.create(separator).spanH(3).hintVC(1).grabH().fillH();
      }
      // arguments/invocations and preview
      {
        SashForm sashForm = new SashForm(container, SWT.VERTICAL);
        GridDataFactory.create(sashForm).spanH(3).grab().fill();
        // creation/invocations tree
        createArgumentsInvocationsComposite(sashForm);
        // preview
        createPreviewComposite(sashForm);
        // set weight, after creating children
        sashForm.setWeights(new int[]{1, 1});
      }
    }

    /**
     * Creates {@link Composite} for factory creation parameters - package, class, etc.
     */
    private void createParametersComposite(Composite parent) {
      // package
      {
        m_packageField =
            new PackageRootAndPackageSelectionDialogField(60,
                ModelMessages.FactoryCreateAction_dialogPackageSourceFolder,
                ModelMessages.FactoryCreateAction_dialogPackageSourceFolderBrowse,
                ModelMessages.FactoryCreateAction_dialogPackagePackage,
                ModelMessages.FactoryCreateAction_dialogPackagePackageBrowse);
        m_packageField.setDialogFieldListener(m_validateListener);
        m_packageField.doFillIntoGrid(m_fieldsContainer, 3);
        // use current package
        m_packageField.setPackage((IPackageFragment) m_editor.getModelUnit().getParent());
      }
      // class name
      {
        m_classField = new StringButtonDialogField(new IStringButtonAdapter() {
          public void changeControlPressed(DialogField field) {
            ExecutionUtils.runLog(new RunnableEx() {
              public void run() throws Exception {
                IType type = JdtUiUtils.selectClassType(getShell(), m_editor.getJavaProject());
                if (type != null) {
                  ICompilationUnit compilationUnit = type.getCompilationUnit();
                  m_classField.setTextWithoutUpdate(type.getElementName());
                  m_packageField.setPackage((IPackageFragment) compilationUnit.getParent());
                }
              }
            });
          }
        });
        m_classField.setButtonLabel(ModelMessages.FactoryCreateAction_classBrowse);
        doCreateField(m_classField, ModelMessages.FactoryCreateAction_classLabel);
        // try to use existing factory
        ExecutionUtils.runLog(new RunnableEx() {
          public void run() throws Exception {
            ICompilationUnit factoryUnit = findFactoryUnit();
            if (factoryUnit != null) {
              m_classField.setText(factoryUnit.findPrimaryType().getElementName());
            } else {
              m_classField.setFocus();
            }
          }
        });
      }
      // method name
      {
        m_methodField = new StringDialogField();
        doCreateField(m_methodField, ModelMessages.FactoryCreateAction_methodLabel);
        // use "createComponent" as initial name
        m_methodField.setText("create"
            + CodeUtils.getShortClass(m_component.getDescription().getComponentClass().getName()));
      }
      // palette category
      {
        // create Combo
        {
          m_categoryField = new ComboDialogField(SWT.READ_ONLY);
          m_categoryField.setLabelText(ModelMessages.FactoryCreateAction_paletteCategoryLabel);
          m_categoryField.setDialogFieldListener(new IDialogFieldListener() {
            public void dialogFieldChanged(DialogField field) {
              if (m_paletteCategories != null) {
                int selectionIndex = m_categoryField.getSelectionIndex();
                if (selectionIndex >= 1) {
                  m_paletteCategory = m_paletteCategories.get(selectionIndex - 1);
                } else {
                  m_paletteCategory = null;
                }
              }
            }
          });
          DialogFieldUtils.fillControls(m_fieldsContainer, m_categoryField, 2, 40);
          fillPaletteCategories();
          m_categoryField.selectItem(0);
        }
        // "Open manager..." button
        {
          Button managerButton = new Button(m_fieldsContainer, SWT.NONE);
          GridDataFactory.create(managerButton).fillH();
          managerButton.setText(ModelMessages.FactoryCreateAction_paletteManagerButton);
          managerButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
              IPaletteSite.Helper.getSite(m_component).editPalette();
              fillPaletteCategories();
            }
          });
        }
      }
    }

    /**
     * Creates {@link Composite} for selection of arguments/invocations.
     */
    private void createArgumentsInvocationsComposite(Composite parent) {
      Composite composite = new Composite(parent, SWT.NONE);
      GridLayoutFactory.create(composite).noMargins();
      new Label(composite, SWT.NONE).setText(ModelMessages.FactoryCreateAction_dialogArgumentsHint);
      {
        Tree tree = new Tree(composite, SWT.BORDER | SWT.CHECK);
        GridDataFactory.create(tree).hintC(150, 12).grab().fill();
        // listeners
        tree.addListener(SWT.Selection, new Listener() {
          public void handleEvent(Event event) {
            if (event.detail == SWT.CHECK) {
              TreeItem item = (TreeItem) event.item;
              // don't change check state for grayed (special) items
              if (item.getGrayed()) {
                item.setChecked(!item.getChecked());
                return;
              }
              // handle check state for model
              if (item.getData() instanceof InvocationInfo) {
                InvocationInfo invocation = (InvocationInfo) item.getData();
                invocation.m_extract = item.getChecked();
              } else if (item.getData() instanceof ArgumentInfo) {
                ArgumentInfo argument = (ArgumentInfo) item.getData();
                if (argument.m_hasVariables) {
                  // arguments with variables should be parameters
                  item.setChecked(true);
                  return;
                } else {
                  argument.m_parameter = item.getChecked();
                }
              }
              // validate, update preview
              validateAll();
            }
          }
        });
        // creation
        {
          TreeItem creationItem = new TreeItem(tree, SWT.NONE);
          creationItem.setImage(DesignerPlugin.getImage("actions/factory/folder.png"));
          creationItem.setText(ModelMessages.FactoryCreateAction_dialogArgumentsCreation);
          creationItem.setGrayed(true);
          creationItem.setChecked(true);
          // arguments
          for (ArgumentInfo argument : m_creationInfo.m_arguments) {
            TreeItem item = new TreeItem(creationItem, SWT.NONE);
            item.setData(argument);
            item.setImage(DesignerPlugin.getImage("actions/factory/argument_can_parameter.gif"));
            item.setText(argument.m_parameterName + " = " + argument.m_source);
            if (argument.m_hasVariables) {
              item.setImage(DesignerPlugin.getImage("actions/factory/argument_must_parameter.gif"));
              item.setChecked(true);
            }
          }
        }
        // invocations
        {
          TreeItem invocationsItem = new TreeItem(tree, SWT.NONE);
          invocationsItem.setText(ModelMessages.FactoryCreateAction_dialogArgumentsInvocations);
          invocationsItem.setImage(DesignerPlugin.getImage("actions/factory/folder.png"));
          invocationsItem.setGrayed(true);
          invocationsItem.setChecked(true);
          // for each invocation
          for (InvocationInfo invocation : m_invocations) {
            // skip, if can not be extracted
            if (!invocation.m_canExtract) {
              continue;
            }
            // item for invocation
            TreeItem invocationItem = new TreeItem(invocationsItem, SWT.NONE);
            invocationItem.setData(invocation);
            invocationItem.setImage(DesignerPlugin.getImage("actions/factory/invocation.gif"));
            invocationItem.setText(invocation.m_signature);
            invocationItem.setChecked(invocation.m_extract);
            // arguments
            for (ArgumentInfo argument : invocation.m_arguments) {
              TreeItem item = new TreeItem(invocationItem, SWT.NONE);
              item.setData(argument);
              item.setImage(DesignerPlugin.getImage("actions/factory/argument_can_parameter.gif"));
              item.setText(argument.m_parameterName + " = " + argument.m_source);
            }
          }
        }
        // expand
        UiUtils.expandAll(tree);
      }
    }

    /**
     * Creates {@link #m_previewViewer}.
     */
    private void createPreviewComposite(Composite parent) {
      Composite composite = new Composite(parent, SWT.NONE);
      GridLayoutFactory.create(composite).noMargins();
      new Label(composite, SWT.NONE).setText(ModelMessages.FactoryCreateAction_dialogPreview);
      m_previewViewer =
          JdtUiUtils.createJavaSourceViewer(composite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
      GridDataFactory.create(m_previewViewer.getControl()).hintVC(12).grab().fill();
    }

    /**
     * Fills palette categories in {@link #m_categoryField}.
     */
    private void fillPaletteCategories() {
      CategoryInfo selectedCategory = m_paletteCategory;
      // do fill with items
      {
        m_categoryField.setItems(new String[]{});
        m_categoryField.addItem(ModelMessages.FactoryCreateAction_dialogCategoryNo);
        // add categories
        {
          IPaletteSite paletteSite = IPaletteSite.Helper.getSite(m_component);
          if (paletteSite != null) {
            PaletteInfo palette = paletteSite.getPalette();
            m_paletteCategories = palette.getCategories();
            for (CategoryInfo category : m_paletteCategories) {
              m_categoryField.addItem(category.getName());
            }
          }
        }
        // show all items
        UiUtils.setVisibleItemCount(
            m_categoryField.getComboControl(null),
            m_categoryField.getItemCount());
      }
      // restore selection
      m_categoryField.selectItem(0);
      if (selectedCategory != null) {
        m_categoryField.selectItem(selectedCategory.getName());
      }
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Buttons
    //
    ////////////////////////////////////////////////////////////////////////////
    private static final int CREATE_ID = IDialogConstants.CLIENT_ID + 1;

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
      createButton(parent, CREATE_ID, ModelMessages.FactoryCreateAction_dialogCreateButton, false);
      super.createButtonsForButtonBar(parent);
    }

    @Override
    protected void buttonPressed(int buttonId) {
      super.buttonPressed(buttonId);
      if (buttonId == CREATE_ID) {
        ExecutionUtils.runLog(new RunnableEx() {
          public void run() throws Exception {
            generate(false);
            validateAll();
          }
        });
      }
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Validation
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    protected String validate() throws Exception {
      // validate
      String message;
      {
        m_sourceFolder = m_packageField.getRoot();
        m_package = m_packageField.getPackage();
        m_className = m_classField.getText();
        m_methodName = m_methodField.getText();
        message = FactoryCreateAction.this.validate();
      }
      // update preview
      if (m_canPreview) {
        ExecutionUtils.runLog(new RunnableEx() {
          public void run() throws Exception {
            JdtUiUtils.setJavaSourceForViewer(m_previewViewer, getFactoryPreviewSource());
          }
        });
      } else {
        JdtUiUtils.setJavaSourceForViewer(
            m_previewViewer,
            ModelMessages.FactoryCreateAction_dialogNoPreview);
      }
      // return validation message
      return message;
    }

    @Override
    protected void setValid(boolean enabled) {
      super.setValid(enabled);
      {
        Button button = getButton(CREATE_ID);
        button.setEnabled(enabled);
      }
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Utils
    //
    ////////////////////////////////////////////////////////////////////////////
    private Composite m_fieldsContainer;

    /**
     * Configures given {@link DialogField} for specific of this dialog.
     */
    protected final void doCreateField(DialogField dialogField, String labelText) {
      dialogField.setLabelText(labelText);
      dialogField.setDialogFieldListener(m_validateListener);
      DialogFieldUtils.fillControls(m_fieldsContainer, dialogField, 3, 40);
    }
  }
}