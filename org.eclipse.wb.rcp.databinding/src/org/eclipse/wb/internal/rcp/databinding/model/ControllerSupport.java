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
package org.eclipse.wb.internal.rcp.databinding.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.databinding.model.IBindingInfo;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.model.reference.StringReferenceProvider;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.PropertyAdapter;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.core.databinding.wizards.autobindings.AbstractDescriptor;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.variable.FieldUniqueVariableSupport;
import org.eclipse.wb.internal.core.utils.IOUtils2;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.BodyDeclarationTarget;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.internal.rcp.ToolkitProvider;
import org.eclipse.wb.internal.rcp.databinding.Activator;
import org.eclipse.wb.internal.rcp.databinding.DatabindingsProvider;
import org.eclipse.wb.internal.rcp.databinding.model.beans.bindables.FieldBeanBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.bindables.MethodBeanBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables.JavaInfoReferenceProvider;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables.WidgetBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.wizards.autobindings.AutomaticDatabindingFirstPage;
import org.eclipse.wb.internal.rcp.databinding.wizards.autobindings.IAutomaticWizardStub;
import org.eclipse.wb.internal.rcp.databinding.wizards.autobindings.JFaceBindingStrategyDescriptor;
import org.eclipse.wb.internal.rcp.databinding.wizards.autobindings.SwtDatabindingProvider;
import org.eclipse.wb.internal.rcp.databinding.wizards.autobindings.SwtWidgetDescriptor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.ui.wizards.NewTypeWizardPage.ImportsManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;

import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Helper class for separate binding code to {@code Controller} class.
 * 
 * @author lobas_av
 * @coverage bindings.rcp.model
 */
public class ControllerSupport {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Configure
  //
  ////////////////////////////////////////////////////////////////////////////
  public static void configure(final DatabindingsProvider provider) throws Exception {
    // detect controller
    final StringBuffer controllerClass = new StringBuffer();
    provider.getRootNode().accept(new ASTVisitor() {
      @Override
      public void endVisit(ClassInstanceCreation creation) {
        String className = AstNodeUtils.getFullyQualifiedName(creation, false);
        List<Expression> arguments = DomGenerics.arguments(creation);
        if (className.endsWith("Controller")
            && arguments.size() == 1
            && arguments.get(0) instanceof ThisExpression) {
          provider.setController(true);
          controllerClass.append(className);
        }
      }
    });
    //
    if (provider.isController()) {
      // create controller ASTEditor
      IType controllerType =
          provider.getAstEditor().getJavaProject().findType(controllerClass.toString());
      ICompilationUnit controllerUnit = controllerType.getCompilationUnit();
      AstEditor astEditor = new AstEditor(controllerUnit);
      // create controller EditorState
      EditorState thisEditorState = EditorState.get(provider.getAstEditor());
      EditorState controllerEditorState = EditorState.get(astEditor);
      controllerEditorState.initialize(
          thisEditorState.getToolkitId(),
          thisEditorState.getEditorLoader());
      controllerEditorState.setFlowDescription(thisEditorState.getFlowDescription());
      // prepare controller root AST node
      TypeDeclaration rootNode = DomGenerics.types(astEditor.getAstUnit()).get(0);
      // prepare reference to this into controller
      String rootClassName = AstNodeUtils.getFullyQualifiedName(provider.getRootNode(), false);
      for (FieldDeclaration field : rootNode.getFields()) {
        org.eclipse.jdt.core.dom.Type type = field.getType();
        if (type == null || AstNodeUtils.getTypeBinding(type) == null) {
          continue;
        }
        //
        String fieldClassName = AstNodeUtils.getFullyQualifiedName(type, false);
        if (rootClassName.equals(fieldClassName)) {
          VariableDeclarationFragment fragment = DomGenerics.fragments(field).get(0);
          provider.setControllerViewerField(fragment.getName().getIdentifier());
          break;
        }
      }
      // replace to controller data
      provider.setAstEditor(astEditor);
      provider.setRootNode(rootNode);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Reference
  //
  ////////////////////////////////////////////////////////////////////////////
  public static String getReference(DatabindingsProvider provider, JavaInfo javaInfo)
      throws Exception {
    String localReference = JavaInfoReferenceProvider.getReference(javaInfo);
    if (localReference != null) {
      if ("this".equals(localReference)) {
        return provider.getControllerViewerField();
      }
      MethodDeclaration[] methods = JavaInfoUtils.getTypeDeclaration(javaInfo).getMethods();
      for (MethodDeclaration method : methods) {
        String methodName = method.getName().getIdentifier();
        if (methodName.startsWith("get")
            && method.parameters().isEmpty()
            && Modifier.isPublic(method.getModifiers())) {
          final Expression[] expressions = new Expression[1];
          method.accept(new ASTVisitor() {
            @Override
            public void endVisit(ReturnStatement node) {
              expressions[0] = node.getExpression();
            }
          });
          if (expressions[0] != null && AstNodeUtils.isVariable(expressions[0])) {
            if (localReference.equals(CoreUtils.getNodeReference(expressions[0]))) {
              return provider.getControllerViewerField() + "." + methodName + "()";
            }
          }
        }
      }
    }
    return null;
  }

  /**
   * Create for given {@link JavaInfo} public getter method.
   */
  public static String ensureControllerReference(DatabindingsProvider provider,
      JavaInfo javaInfo,
      boolean commit) throws Exception {
    // prepare target
    TypeDeclaration typeDeclaration = JavaInfoUtils.getTypeDeclaration(javaInfo);
    BodyDeclarationTarget target = new BodyDeclarationTarget(typeDeclaration, null, false);
    // prepare method name
    String reference = JavaInfoReferenceProvider.getReference(javaInfo);
    String fieldPrefix = JavaCore.getOption(JavaCore.CODEASSIST_FIELD_PREFIXES);
    fieldPrefix = fieldPrefix == null ? "m_" : fieldPrefix;
    String methodName =
        "get" + StringUtils.capitalize(StringUtils.removeStart(reference, fieldPrefix)) + "()";
    // prepare method header
    String header =
        "public " + javaInfo.getDescription().getComponentClass().getName() + " " + methodName;
    // prepare method lines
    List<String> methodLines = ImmutableList.of("return " + reference + ";");
    // add method
    javaInfo.getEditor().addMethodDeclaration(header, methodLines, target);
    if (commit) {
      javaInfo.getEditor().commitChanges();
    }
    // controller reference
    return provider.getControllerViewerField() + "." + methodName;
  }

  /**
   * Convert controller widget reference to host widget reference.
   */
  public static Expression convertWidgetBindableExpression(DatabindingsProvider provider,
      List<WidgetBindableInfo> observables,
      Expression expression) throws Exception {
    if (provider.isController()) {
      if (expression instanceof MethodInvocation) {
        MethodInvocation invocation = (MethodInvocation) expression;
        String reference = CoreUtils.getNodeReference(invocation.getExpression());
        //
        if (provider.getControllerViewerField().equals(reference)) {
          TypeDeclaration rootNode =
              JavaInfoUtils.getTypeDeclaration(observables.get(0).getJavaInfo());
          MethodDeclaration method =
              AstNodeUtils.getMethodByName(rootNode, invocation.getName().getIdentifier());
          //
          final Expression[] expressions = new Expression[1];
          method.accept(new ASTVisitor() {
            @Override
            public void endVisit(ReturnStatement node) {
              expressions[0] = node.getExpression();
            }
          });
          expression = expressions[0];
        }
      } else if (provider.getControllerViewerField().equals(
          CoreUtils.getSafeNodeReference(expression))) {
        return null;
      }
    }
    return expression;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Save
  //
  ////////////////////////////////////////////////////////////////////////////
  @SuppressWarnings("unchecked")
  public static void doSave(AstEditor editor, JavaInfo rootJavaInfo) throws Exception {
    // save changes
    editor.commitChanges();
    editor.getModelUnit().getBuffer().save(null, true);
    // reset modification stamp for controller file
    IResource resource = editor.getModelUnit().getResource();
    Map<IResource, Long> dependencies =
        (Map<IResource, Long>) rootJavaInfo.getEditor().getGlobalValue("JavaInfo.dependencies");
    dependencies.put(resource, resource.getModificationStamp());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // AutoWizard
  //
  ////////////////////////////////////////////////////////////////////////////
  public static String automaticWizardPerformSubstitutions(AutomaticDatabindingFirstPage firstWizardPage,
      String code,
      ImportsManager imports,
      IJavaProject javaProject,
      ClassLoader classLoader,
      Class<?> beanClass,
      List<PropertyAdapter> properties,
      Map<PropertyAdapter, AbstractDescriptor[]> propertyToEditor) throws Exception {
    InputStream controllerStream = Activator.getFile("templates/Controller.jvt");
    String controllerCode = IOUtils.toString(controllerStream);
    IOUtils.closeQuietly(controllerStream);
    //
    String hostClassName = ClassUtils.getShortClassName(firstWizardPage.getTypeName());
    String hostVariable = StringUtils.uncapitalize(hostClassName);
    String hostField = "m_" + hostVariable;
    //
    controllerCode = StringUtils.replace(controllerCode, "%HostClass%", hostClassName);
    controllerCode = StringUtils.replace(controllerCode, "%HostVariable%", hostVariable);
    controllerCode = StringUtils.replace(controllerCode, "%HostField%", hostField);
    //
    String begin = "";
    String end = "\t\t";
    String widgetStart = "";
    boolean blockMode = SwtDatabindingProvider.useBlockMode();
    if (blockMode) {
      begin = "\t\t{\r\n";
      end = "\t\t}";
      widgetStart = "\t";
    }
    // prepare imports
    Collection<String> hostImportList = Sets.newHashSet();
    hostImportList.add(SWT.class.getName());
    hostImportList.add("org.eclipse.swt.widgets.Label");
    hostImportList.add("org.eclipse.swt.layout.GridLayout");
    hostImportList.add("org.eclipse.swt.layout.GridData");
    //
    Collection<String> controllerImportList = Sets.newHashSet();
    controllerImportList.add(SWT.class.getName());
    controllerImportList.add("org.eclipse.jface.databinding.swt.SWTObservables");
    controllerImportList.add("org.eclipse.core.databinding.observable.value.IObservableValue");
    controllerImportList.add("org.eclipse.core.databinding.UpdateValueStrategy");
    //
    DataBindingsCodeUtils.ensureDBLibraries(javaProject);
    //
    IAutomaticWizardStub automaticWizardStub =
        GlobalFactoryHelper.automaticWizardCreateStub(javaProject, classLoader, beanClass);
    //
    String observeMethod = null;
    if (automaticWizardStub == null) {
      if (ObservableInfo.isPojoBean(beanClass)) {
        String pojoClassName = DataBindingsCodeUtils.getPojoObservablesClass();
        observeMethod =
            "ObserveValue = " + ClassUtils.getShortClassName(pojoClassName) + ".observeValue(";
        controllerImportList.add(pojoClassName);
      } else {
        observeMethod = "ObserveValue = BeansObservables.observeValue(";
        controllerImportList.add("org.eclipse.core.databinding.beans.BeansObservables");
      }
    } else {
      automaticWizardStub.addImports(controllerImportList);
    }
    // prepare bean
    String beanClassName = CoreUtils.getClassName(beanClass);
    String beanClassShortName = ClassUtils.getShortClassName(beanClassName);
    String fieldPrefix = JavaCore.getOption(JavaCore.CODEASSIST_FIELD_PREFIXES);
    String fieldName = fieldPrefix + StringUtils.uncapitalize(beanClassShortName);
    controllerCode = StringUtils.replace(controllerCode, "%BeanClass%", beanClassName);
    //
    if (ReflectionUtils.getConstructorBySignature(beanClass, "<init>()") == null) {
      controllerCode = StringUtils.replace(controllerCode, "%BeanField%", fieldName);
    } else {
      controllerCode =
          StringUtils.replace(controllerCode, "%BeanField%", fieldName
              + " = new "
              + beanClassName
              + "()");
    }
    //
    IPreferenceStore preferences = ToolkitProvider.DESCRIPTION.getPreferences();
    String accessPrefix =
        preferences.getBoolean(FieldUniqueVariableSupport.P_PREFIX_THIS) ? "this." : "";
    controllerCode =
        StringUtils.replace(controllerCode, "%BeanFieldAccess%", accessPrefix + fieldName);
    //
    controllerCode =
        StringUtils.replace(
            controllerCode,
            "%BeanName%",
            StringUtils.capitalize(beanClassShortName));
    // prepare code
    StringBuffer widgetFields = new StringBuffer();
    StringBuffer widgets = new StringBuffer();
    String swtContainer = StringUtils.substringBetween(code, "%Widgets%", "%");
    String swtContainerWithDot = "this".equals(swtContainer) ? "" : swtContainer + ".";
    //
    StringBuffer observables = new StringBuffer();
    StringBuffer bindings = new StringBuffer();
    StringBuffer widgetGetters = new StringBuffer();
    //
    hostImportList.add(GridLayout.class.getName());
    widgets.append("\t\t" + swtContainerWithDot + "setLayout(new GridLayout(2, false));\r\n");
    if (!blockMode) {
      widgets.append("\t\t\r\n");
    }
    //
    for (Iterator<PropertyAdapter> I = properties.iterator(); I.hasNext();) {
      PropertyAdapter property = I.next();
      Object[] editorData = propertyToEditor.get(property);
      SwtWidgetDescriptor widgetDescriptor = (SwtWidgetDescriptor) editorData[0];
      JFaceBindingStrategyDescriptor strategyDescriptor =
          (JFaceBindingStrategyDescriptor) editorData[1];
      //
      String propertyName = property.getName();
      String widgetClassName = widgetDescriptor.getClassName();
      String widgetFieldName = fieldPrefix + propertyName + widgetClassName;
      String widgetFieldAccess = accessPrefix + widgetFieldName;
      String widgetAccessor = "get" + StringUtils.capitalize(propertyName) + widgetClassName + "()";
      String widgetControllerAccessor = hostField + "." + widgetAccessor;
      // getter
      widgetGetters.append("method\n\tpublic "
          + widgetClassName
          + " "
          + widgetAccessor
          + " {\n\t\treturn "
          + widgetFieldName
          + ";\n\t}\n\n");
      // field
      widgetFields.append("\r\nfield\r\n\tprivate " + widgetClassName + " " + widgetFieldName + ";");
      // widget
      widgets.append(begin);
      widgets.append(widgetStart
          + "\t\tnew Label("
          + swtContainer
          + ", SWT.NONE).setText(\""
          + StringUtils.capitalize(propertyName)
          + ":\");\r\n");
      widgets.append(end + "\r\n");
      //
      widgets.append(begin);
      widgets.append("\t\t"
          + widgetFieldAccess
          + " = "
          + widgetDescriptor.getCreateCode(swtContainer)
          + ";\r\n");
      widgets.append(widgetStart
          + "\t\t"
          + widgetFieldAccess
          + ".setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));\r\n");
      widgets.append(end);
      // observables
      observables.append("\t\tIObservableValue "
          + propertyName
          + "ObserveWidget = "
          + widgetDescriptor.getBindingCode(widgetControllerAccessor)
          + ";\r\n");
      if (automaticWizardStub == null) {
        observables.append("\t\tIObservableValue "
            + propertyName
            + observeMethod
            + fieldName
            + ", \""
            + propertyName
            + "\");");
      } else {
        observables.append(automaticWizardStub.createSourceCode(fieldName, propertyName));
      }
      // bindings
      bindings.append("\t\tbindingContext.bindValue("
          + propertyName
          + "ObserveWidget, "
          + propertyName
          + "ObserveValue, "
          + strategyDescriptor.getTargetStrategyCode()
          + ", "
          + strategyDescriptor.getModelStrategyCode()
          + ");");
      //
      if (I.hasNext()) {
        widgetFields.append("\r\n");
        widgets.append("\r\n");
        observables.append("\r\n");
        bindings.append("\r\n");
      }
      //
      hostImportList.add(widgetDescriptor.getFullClassName());
    }
    // replace template patterns
    String controllerClass = hostClassName + "Controller";
    code = StringUtils.replace(code, "%ControllerClass%", controllerClass);
    code = StringUtils.replace(code, "%WidgetFields%", widgetFields.toString());
    code = StringUtils.replace(code, "%Widgets%" + swtContainer + "%", widgets.toString());
    code = StringUtils.replace(code, "%WidgetGetters%", widgetGetters.toString());
    //
    controllerCode = StringUtils.replace(controllerCode, "%Observables%", observables.toString());
    controllerCode = StringUtils.replace(controllerCode, "%Bindings%", bindings.toString());
    // add imports
    for (String qualifiedTypeName : hostImportList) {
      imports.addImport(qualifiedTypeName);
    }
    StringBuffer controllerImportString = new StringBuffer();
    for (String controllerImport : controllerImportList) {
      controllerImportString.append("import " + controllerImport + ";\n");
    }
    controllerCode =
        StringUtils.replace(controllerCode, "%imports%", controllerImportString.toString());
    //
    IPackageFragment packageFragment = firstWizardPage.getPackageFragment();
    //
    String packageString = packageFragment.getElementName();
    if (packageString.length() > 0) {
      packageString = "package " + packageString + ";";
    }
    controllerCode = StringUtils.replace(controllerCode, "%package%", packageString);
    //
    IPath packagePath = packageFragment.getPath().removeFirstSegments(1);
    IFile controllerFile =
        javaProject.getProject().getFile(packagePath.append(controllerClass + ".java"));
    IOUtils2.setFileContents(controllerFile, controllerCode);
    //
    return code;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Convert
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Move method {@code initDataBindings()} code to separate controller class.
   */
  public static void convertToController(DatabindingsProvider provider,
      JavaInfo javaInfoRoot,
      AstEditor editor,
      TypeDeclaration rootNode) {
    try {
      convertJavaInfosToGetters(provider, editor, rootNode);
      createContollerClass(provider, editor, rootNode);
      moveBeans(provider);
      removeInitDatabindings(provider, editor, rootNode);
      addControllerInvocation(javaInfoRoot, editor, rootNode);
      //
      provider.saveEdit(true);
      //
      editor.commitChanges();
      editor.getModelUnit().getBuffer().save(null, true);
    } catch (Throwable e) {
      DesignerPlugin.log(e);
    }
  }

  private static void convertJavaInfosToGetters(DatabindingsProvider provider,
      AstEditor editor,
      TypeDeclaration rootNode) throws Exception {
    provider.setController(true);
    provider.setControllerViewerField("m_"
        + StringUtils.uncapitalize(rootNode.getName().getIdentifier()));
    //
    List<IBindingInfo> bindings = provider.getBindings();
    for (IBindingInfo binding : bindings) {
      convertJavaInfoToGetter(provider, binding.getModel());
      convertJavaInfoToGetter(provider, binding.getTarget());
    }
    editor.commitChanges();
  }

  private static void convertJavaInfoToGetter(DatabindingsProvider provider, IObserveInfo observe)
      throws Exception {
    if (observe instanceof WidgetBindableInfo) {
      WidgetBindableInfo widgetBindable = (WidgetBindableInfo) observe;
      JavaInfo javaInfo = widgetBindable.getJavaInfo();
      if (getReference(provider, javaInfo) == null) {
        ensureControllerReference(provider, javaInfo, false);
      }
      JavaInfoReferenceProvider referenceProvider =
          (JavaInfoReferenceProvider) widgetBindable.getReferenceProvider();
      referenceProvider.setJavaInfo(javaInfo);
    }
  }

  private static void createContollerClass(DatabindingsProvider provider,
      AstEditor editor,
      TypeDeclaration rootNode) throws Exception {
    String endOfLine = editor.getGeneration().getEndOfLine();
    //
    StringBuffer controllerCode = new StringBuffer();
    //
    IType hostType =
        editor.getJavaProject().findType(AstNodeUtils.getFullyQualifiedName(rootNode, false));
    IPackageFragment packageFragment = hostType.getPackageFragment();
    //
    String hostClass = rootNode.getName().getIdentifier();
    String controllerClass = hostClass + "Controller";
    String hostVariable = StringUtils.uncapitalize(hostClass);
    String hostField = "m_" + hostVariable;
    String fullControllerClass = controllerClass;
    //
    String packageString = packageFragment.getElementName();
    if (packageString.length() > 0) {
      controllerCode.append("package " + packageString + ";" + endOfLine + endOfLine);
      fullControllerClass = packageString + "." + controllerClass;
    }
    //
    controllerCode.append("public class " + controllerClass + " {" + endOfLine);
    controllerCode.append("\tprivate " + hostClass + " " + hostField + ";" + endOfLine + endOfLine);
    controllerCode.append("\tpublic "
        + controllerClass
        + "("
        + hostClass
        + " "
        + hostVariable
        + ") {"
        + endOfLine);
    controllerCode.append("\t\t" + hostField + " = " + hostVariable + ";" + endOfLine);
    controllerCode.append("\t}" + endOfLine);
    controllerCode.append("}");
    //
    IPath packagePath = packageFragment.getPath().removeFirstSegments(1);
    IFile controllerFile =
        editor.getJavaProject().getProject().getFile(packagePath.append(controllerClass + ".java"));
    IOUtils2.setFileContents(controllerFile, controllerCode.toString());
    //
    ProjectUtils.waitForAutoBuild();
    //
    IType controllerType = editor.getJavaProject().findType(fullControllerClass);
    ICompilationUnit controllerUnit = controllerType.getCompilationUnit();
    AstEditor controllerASTEditor = new AstEditor(controllerUnit);
    // create controller EditorState
    EditorState thisEditorState = EditorState.get(provider.getAstEditor());
    EditorState controllerEditorState = EditorState.get(controllerASTEditor);
    controllerEditorState.initialize(
        thisEditorState.getToolkitId(),
        thisEditorState.getEditorLoader());
    controllerEditorState.setFlowDescription(thisEditorState.getFlowDescription());
    // prepare controller root AST node
    TypeDeclaration controllerRootNode = DomGenerics.types(controllerASTEditor.getAstUnit()).get(0);
    //
    provider.setAstEditor(controllerASTEditor);
    provider.setRootNode(controllerRootNode);
  }

  private static void moveBeans(DatabindingsProvider provider) throws Exception {
    AstEditor controllerASTEditor = provider.getAstEditor();
    TypeDeclaration controllerRootNode = provider.getRootNode();
    List<IBindingInfo> bindings = provider.getBindings();
    int size = bindings.size();
    Set<IObserveInfo> observes = Sets.newHashSet();
    for (int i = size - 1; i >= 0; i--) {
      IBindingInfo binding = bindings.get(i);
      IObserveInfo model = binding.getModel();
      if (observes.add(model)) {
        moveBean(provider, model, controllerASTEditor, controllerRootNode);
      }
      IObserveInfo target = binding.getTarget();
      if (observes.add(target)) {
        moveBean(provider, target, controllerASTEditor, controllerRootNode);
      }
    }
    //
    controllerASTEditor.commitChanges();
    controllerASTEditor.getModelUnit().getBuffer().save(null, true);
  }

  private static void moveBean(DatabindingsProvider provider,
      IObserveInfo observe,
      AstEditor controllerEditor,
      TypeDeclaration controllerRootNode) throws Exception {
    if (observe instanceof FieldBeanBindableInfo) {
      //
      FieldBeanBindableInfo fieldBindable = (FieldBeanBindableInfo) observe;
      VariableDeclarationFragment fragment = fieldBindable.getFragment();
      //
      if (fragment != null) {
        FieldDeclaration fieldDeclaration = AstNodeUtils.getEnclosingFieldDeclaration(fragment);
        String modifier =
            Modifier.ModifierKeyword.fromFlagValue(fieldDeclaration.getModifiers()).toString();
        //
        controllerEditor.addFieldDeclaration(
            modifier
                + " "
                + CoreUtils.getClassName(fieldBindable.getObjectType())
                + " "
                + fragment.getName().getIdentifier()
                + ";",
            new BodyDeclarationTarget(controllerRootNode, true));
      }
    } else if (observe instanceof MethodBeanBindableInfo) {
      IObserveInfo parentObserve = observe.getParent();
      if (parentObserve == null) {
        MethodBeanBindableInfo methodBindable = (MethodBeanBindableInfo) observe;
        methodBindable.setReferenceProvider(new StringReferenceProvider(provider.getControllerViewerField()
            + "."
            + methodBindable.getReference()));
      } else {
        moveBean(provider, parentObserve, controllerEditor, controllerRootNode);
      }
    } else {
      GlobalFactoryHelper.moveBean(observe, controllerEditor, controllerRootNode);
    }
  }

  private static void removeInitDatabindings(DatabindingsProvider provider,
      AstEditor editor,
      TypeDeclaration rootNode) throws Exception {
    MethodDeclaration initDataBindings = provider.getRootInfo().getInitDataBindings();
    if (initDataBindings != null) {
      final String signature = AstNodeUtils.getMethodSignature(initDataBindings);
      final List<Statement> statements = Lists.newArrayList();
      rootNode.accept(new ASTVisitor() {
        @Override
        public void endVisit(MethodInvocation node) {
          if (signature.equals(AstNodeUtils.getMethodSignature(node))) {
            statements.add(AstNodeUtils.getEnclosingStatement(node));
          }
        }
      });
      for (Statement statement : statements) {
        editor.removeStatement(statement);
      }
      //
      editor.removeBodyDeclaration(initDataBindings);
      provider.getRootInfo().setInitDataBindings(null);
    }
  }

  private static void addControllerInvocation(JavaInfo javaInfoRoot,
      AstEditor editor,
      TypeDeclaration rootNode) throws Exception {
    MethodDeclaration lastInfoMethod =
        DataBindingsCodeUtils.getLastInfoDeclaration(null, javaInfoRoot);
    ShellVisitor visitor = new ShellVisitor();
    lastInfoMethod.accept(visitor);
    //
    Statement shellOpenStatement = visitor.getShellOpenStatement();
    List<Statement> statements = DomGenerics.statements(lastInfoMethod.getBody());
    StatementTarget methodTarget;
    //
    if (shellOpenStatement != null) {
      methodTarget = new StatementTarget(shellOpenStatement, true);
    } else if (statements.isEmpty()) {
      methodTarget = new StatementTarget(lastInfoMethod, true);
    } else {
      Statement lastStatement = statements.get(statements.size() - 1);
      methodTarget = new StatementTarget(lastStatement, lastStatement instanceof ReturnStatement);
    }
    //
    editor.addStatement(
        "new " + rootNode.getName().getIdentifier() + "Controller(this);",
        methodTarget);
    editor.commitChanges();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ShellVisitor
  //
  ////////////////////////////////////////////////////////////////////////////
  private static class ShellVisitor extends ASTVisitor {
    private Statement m_shellOpenStatement;

    ////////////////////////////////////////////////////////////////////////////
    //
    // ASTVisitor
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public boolean visit(MethodInvocation node) {
      String methodName = node.getName().getIdentifier();
      if (node.arguments().isEmpty()
          && "open".equals(methodName)
          && AstNodeUtils.isSuccessorOf(node.getExpression(), "org.eclipse.swt.widgets.Shell")) {
        Assert.isNull(m_shellOpenStatement, "Double invoke %shell%.open()");
        m_shellOpenStatement = AstNodeUtils.getEnclosingStatement(node);
      }
      return false;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Access
    //
    ////////////////////////////////////////////////////////////////////////////
    public Statement getShellOpenStatement() {
      return m_shellOpenStatement;
    }
  }
}