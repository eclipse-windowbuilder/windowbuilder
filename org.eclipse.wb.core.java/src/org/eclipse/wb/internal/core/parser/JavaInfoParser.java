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
package org.eclipse.wb.internal.core.parser;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.eclipse.wb.core.eval.ExecutionFlowDescription;
import org.eclipse.wb.core.eval.ExecutionFlowUtils;
import org.eclipse.wb.core.eval.ExecutionFlowUtils.ExecutionFlowFrameVisitor;
import org.eclipse.wb.core.eval.ExecutionFlowUtils.VisitingContext;
import org.eclipse.wb.core.eval.ExecutionFlowUtils2;
import org.eclipse.wb.core.eval.ExpressionValue;
import org.eclipse.wb.core.model.IRootProcessor;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.association.Association;
import org.eclipse.wb.core.model.association.CompoundAssociation;
import org.eclipse.wb.core.model.association.ConstructorChildAssociation;
import org.eclipse.wb.core.model.association.ConstructorParentAssociation;
import org.eclipse.wb.core.model.association.InvocationChildArrayAssociation;
import org.eclipse.wb.core.model.association.InvocationChildAssociation;
import org.eclipse.wb.core.model.association.InvocationChildEllipsisAssociation;
import org.eclipse.wb.core.model.association.InvocationSecondaryAssociation;
import org.eclipse.wb.core.model.association.RootAssociation;
import org.eclipse.wb.core.model.association.SuperConstructorArgumentAssociation;
import org.eclipse.wb.core.model.broadcast.EvaluationEventListener;
import org.eclipse.wb.core.model.broadcast.ExecutionFlowEnterFrame;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.core.model.broadcast.JavaInfoMethodAssociationOnParse;
import org.eclipse.wb.core.model.broadcast.JavaInfoTreeAlmostComplete;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.core.model.broadcast.ObjectInfoTreeComplete;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.model.JavaInfoEvaluationHelper;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.description.ConstructorDescription;
import org.eclipse.wb.internal.core.model.description.MethodDescription;
import org.eclipse.wb.internal.core.model.description.ParameterDescription;
import org.eclipse.wb.internal.core.model.description.helpers.FactoryDescriptionHelper;
import org.eclipse.wb.internal.core.model.generation.GenerationSettings;
import org.eclipse.wb.internal.core.model.nonvisual.AbstractArrayObjectInfo;
import org.eclipse.wb.internal.core.model.nonvisual.ArrayObjectInfo;
import org.eclipse.wb.internal.core.model.nonvisual.EllipsisObjectInfo;
import org.eclipse.wb.internal.core.model.nonvisual.NonVisualBeanContainerInfo;
import org.eclipse.wb.internal.core.model.nonvisual.NonVisualBeanInfo;
import org.eclipse.wb.internal.core.model.util.GlobalStateJava;
import org.eclipse.wb.internal.core.model.variable.EmptyVariableSupport;
import org.eclipse.wb.internal.core.model.variable.FieldInitializerVariableSupport;
import org.eclipse.wb.internal.core.model.variable.FieldReuseVariableSupport;
import org.eclipse.wb.internal.core.model.variable.FieldUniqueVariableSupport;
import org.eclipse.wb.internal.core.model.variable.LazyVariableSupportUtils;
import org.eclipse.wb.internal.core.model.variable.LocalReuseVariableSupport;
import org.eclipse.wb.internal.core.model.variable.LocalUniqueVariableSupport;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.nls.NlsSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.exception.DesignerExceptionUtils;
import org.eclipse.wb.internal.core.utils.exception.ICoreExceptionConstants;
import org.eclipse.wb.internal.core.utils.exception.NoEntryPointError;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.internal.core.utils.state.GlobalState;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Parser converts given model {@link ICompilationUnit} into tree of {@link JavaInfo} objects.
 *
 * @author scheglov_ke
 * @coverage core.model.parser
 */
public final class JavaInfoParser implements IJavaInfoParseResolver {
  /**
   * The key for accessing root {@link JavaInfo} using {@link AstEditor#getGlobalValue(String)}.
   */
  public static final String KEY_ROOT = "KEY_ROOT";
  /**
   * The key for accessing all {@link JavaInfo}'s using {@link AstEditor#getGlobalValue(String)}.
   */
  public static final String KEY_COMPONENTS = "KEY_COMPONENTS";

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Parses given compilation unit and returns single root {@link JavaInfo}.
   */
  public static JavaInfo parse(ICompilationUnit modelUnit) throws Exception {
    DesignerPlugin.installSecurityManager();
    checkJavaVersion(modelUnit);
    final JavaInfoParser parser = new JavaInfoParser(modelUnit);
    return ExecutionUtils.runDesignTime(new RunnableObjectEx<JavaInfo>() {
      public JavaInfo runObject() throws Exception {
        return parser.parse();
      }
    });
  }

  /**
   * Parses given {@link MethodDeclaration} in {@link AstEditor}.
   */
  public static JavaInfo parse(AstEditor editor, MethodDeclaration rootMethod) throws Exception {
    final JavaInfoParser parser = new JavaInfoParser(editor);
    parser.m_editorState.setFlowDescription(new ExecutionFlowDescription(rootMethod));
    return ExecutionUtils.runDesignTime(new RunnableObjectEx<JavaInfo>() {
      public JavaInfo runObject() throws Exception {
        return parser.parseRootMethods();
      }
    });
  }

  /**
   * Checks that Java version used to run Eclipse is compatible with Java version of
   * {@link IJavaProject}.
   */
  private static void checkJavaVersion(ICompilationUnit unit) {
    IJavaProject javaProject = unit.getJavaProject();
    float projectVersion = ProjectUtils.getJavaVersion(javaProject);
    float eclipseVersion = EnvironmentUtils.getJavaVersion();
    if (eclipseVersion < projectVersion) {
      NumberFormat format = new DecimalFormat("#.###", new DecimalFormatSymbols(Locale.ENGLISH));
      String projectVersionString = format.format(projectVersion);
      String eclipseVersionString = format.format(eclipseVersion);
      throw new DesignerException(ICoreExceptionConstants.PARSER_JAVA_VERSION,
          projectVersionString,
          eclipseVersionString);
    }
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance fields
  //
  ////////////////////////////////////////////////////////////////////////////
  private final AstEditor m_editor;
  private final EditorState m_editorState;
  private final JavaInfoResolver m_javaInfoResolver;
  private final EvaluationEventListener m_evaluationListener;
  private final List<IParseFactory> m_parseFactories;
  private TypeDeclaration m_typeDeclaration;
  private JavaInfo m_rootComponent;
  private final List<JavaInfo> m_components;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private JavaInfoParser(ICompilationUnit modelUnit) throws Exception {
    this(new AstEditor(modelUnit));
  }

  private JavaInfoParser(AstEditor editor) throws Exception {
    m_editor = editor;
    m_editorState = EditorState.get(m_editor);
    m_javaInfoResolver = new JavaInfoResolver(editor);
    m_components = m_javaInfoResolver.getComponents();
    m_evaluationListener = m_editorState.getBroadcast().getListener(EvaluationEventListener.class);
    m_parseFactories =
        ExternalFactoriesHelper.getElementsInstances(
            IParseFactory.class,
            "org.eclipse.wb.core.java.parseFactories",
            "factory");
    m_editor.putGlobalValue(KEY_COMPONENTS, m_components);
    GlobalState.setParsing(true);
    m_editorState.setExecuting(true);
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Parsing
  //
  ////////////////////////////////////////////////////////////////////////////
  private ExecutionFlowParseVisitor m_visitor;

  /**
   * Parses compilation unit.
   *
   * @return single root {@link JavaInfo}.
   */
  private JavaInfo parse() throws Exception {
    validateASTEditor();
    // prepare parse context
    {
      ParseRootContext rootContext = prepareParseContext();
      // prepare flow description
      ExecutionFlowDescription flowDescription = rootContext.getFlowDescription();
      addStartMethodsNVO(flowDescription);
      // remember root information
      m_rootComponent = rootContext.getRoot();
      m_editorState.setFlowDescription(flowDescription);
      m_editorState.setTmp_visitingContext(new VisitingContext(false));
      m_editorState.setTmp_Components(m_components);
      // add root
      if (rootContext.getRoot() != null) {
        addJavaInfo(rootContext.getRoot(), null);
      }
      // use IParseContextProcessor-s
      {
        List<IParseContextProcessor> processors =
            ExternalFactoriesHelper.getElementsInstances(
                IParseContextProcessor.class,
                "org.eclipse.wb.core.java.parseContextProcessors",
                "processor");
        for (IParseContextProcessor processor : processors) {
          processor.process(m_editor, flowDescription, m_components);
        }
      }
    }
    // parse execution flow
    return parseRootMethods();
  }

  /**
   * Uses {@link IParseValidator}s to validate this {@link AstEditor}.
   */
  private void validateASTEditor() throws Exception {
    List<IParseValidator> validators =
        ExternalFactoriesHelper.getElementsInstances(
            IParseValidator.class,
            "org.eclipse.wb.core.java.parseFactories",
            "validator");
    for (IParseValidator validator : validators) {
      validator.validate(m_editor);
    }
  }

  /**
   * We don't visit NVO {@link MethodDeclaration}-s, so we should add them forcedly.
   */
  private static void addStartMethodsNVO(ExecutionFlowDescription flowDescription) throws Exception {
    // prepare enclosing type
    TypeDeclaration typeDeclaration;
    {
      MethodDeclaration startMethod = flowDescription.getStartMethods().get(0);
      typeDeclaration = (TypeDeclaration) startMethod.getParent();
    }
    // check all methods
    for (MethodDeclaration method : typeDeclaration.getMethods()) {
      NonVisualBeanInfo nonVisualInfo = NonVisualBeanContainerInfo.getNonVisualInfo(method);
      if (nonVisualInfo != null) {
        flowDescription.addStartMethod(method);
      }
    }
  }

  /**
   * @return the single root {@link JavaInfo} for current root methods from {@link EditorState}.
   */
  private JavaInfo parseRootMethods() throws Exception {
    ExecutionFlowParseVisitor parseVisitor = getParseVisitor();
    parseVisitor.m_currentStatement = null;
    try {
      // visit execution flow
      ExecutionFlowUtils.visit(
          m_editorState.getTmp_visitingContext(),
          m_editorState.getFlowDescription(),
          parseVisitor);
      GlobalState.setParsing(false);
      m_editorState.setExecuting(false);
      // now we visited execution flow, so lock ExecutionFlowDescription
      m_editorState.getFlowDescription().lockBinaryFlow();
      // find root
      {
        JavaInfo root = getRoot();
        if (root != null) {
          m_javaInfoResolver.setRootJavaInfo(root);
          return root;
        }
      }
      // fail, no root found
      if (m_editor.hasCompilationErrors()) {
        throw new DesignerException(ICoreExceptionConstants.PARSER_NO_ROOT_WHEN_COMPILATION_ERRORS);
      }
      throw new NoEntryPointError(m_editor, m_typeDeclaration);
    } catch (Throwable e) {
      disposeComponentsHierarchy(m_components, true);
      // remember position
      if (parseVisitor.m_currentStatement != null) {
        int position = AstNodeUtils.getSourceBegin(parseVisitor.m_currentStatement);
        DesignerExceptionUtils.setSourcePosition(e, position);
      }
      // re-throw
      throw ReflectionUtils.propagate(e);
    }
  }

  /**
   * In case of exception during parsing we have to dispose all components, because after exception
   * we don't return any model object to caller.
   */
  private static void disposeComponentsHierarchy(List<JavaInfo> components, boolean sendBroadcast)
      throws Exception {
    for (JavaInfo component : components) {
      try {
        component.refresh_dispose();
        if (sendBroadcast) {
          component.getBroadcastObject().dispose();
        }
      } catch (Throwable e) {
        DesignerPlugin.log(e);
      }
    }
  }

  /**
   * If we created {@link JavaInfo} for some {@link Expression} however it was not connected to
   * hierarchy, then we should remove reference on model, to prevent re-using this value again and
   * again. We should evaluate this {@link Expression} as any other.
   */
  private void clearValuesOfDisconnectedModels(final JavaInfo root) {
    if (m_editorState.isLiveComponent()) {
      return;
    }
    m_editor.getAstUnit().accept(new ASTVisitor() {
      @Override
      public void postVisit(ASTNode node) {
        if (node instanceof Expression) {
          Expression expression = (Expression) node;
          ExpressionValue value = ExecutionFlowUtils2.getValue0(expression);
          if (value != null) {
            JavaInfo model = (JavaInfo) value.getModel();
            if (model != null && !root.isItOrParentOf(model)) {
              ExecutionFlowUtils2.clearPermanentValue(expression);
            }
          }
        }
      }
    });
  }

  /**
   * @return the {@link ExecutionFlowParseVisitor} that is used by this {@link JavaInfoParser} for
   *         parsing.
   */
  private ExecutionFlowParseVisitor getParseVisitor() {
    if (m_visitor == null) {
      m_visitor = new ExecutionFlowParseVisitor();
    }
    return m_visitor;
  }

  /**
   * @return the root {@link JavaInfo} for parsed components.
   */
  private JavaInfo getRoot() throws Throwable {
    m_editorState.getBroadcast().getListener(JavaEventListener.class).bindComponents(m_components);
    JavaInfoUtils.bindBinaryComponents(m_components);
    // select root JavaInfo
    List<JavaInfo> rootComponents = getRootComponents();
    if (!rootComponents.isEmpty()) {
      try {
        // use first component as root
        JavaInfo root = rootComponents.get(0);
        GlobalStateJava.activate(root);
        root.setAssociation(new RootAssociation());
        // process root
        callRootProcessors(root);
        root.getBroadcast(JavaInfoTreeAlmostComplete.class).invoke(root, m_components);
        m_editor.putGlobalValue(KEY_ROOT, root);
        root.getBroadcast(ObjectInfoTreeComplete.class).invoke();
        clearValuesOfDisconnectedModels(root);
        // prepare NLS
        {
          // we do this here because at this point we have objects for all components,
          // so can safely access all properties (NLSSupport does this at initialization)
          NlsSupport.get(root);
        }
        // do return
        GenerationSettings.deduce(root);
        return root;
      } finally {
        // We create objects during parsing, so dispose them now.
        disposeComponentsHierarchy(rootComponents, false);
      }
    }
    // no root found
    return null;
  }

  /**
   * Calls {@link IRootProcessor}-s.
   */
  private void callRootProcessors(JavaInfo root) throws Exception {
    List<JavaInfo> components = Lists.newArrayList();
    // fill components with no duplicates
    for (JavaInfo javaInfo : m_components) {
      if (!components.contains(javaInfo)) {
        components.add(javaInfo);
      }
    }
    // prepare processors
    List<IRootProcessor> processors =
        ExternalFactoriesHelper.getElementsInstances(
            IRootProcessor.class,
            "org.eclipse.wb.core.java.rootProcessors",
            "processor");
    //
    for (IRootProcessor processor : processors) {
      processor.process(root, components);
    }
  }

  /**
   * @return the potential root {@link JavaInfo}'s, sorted by the hierarchy size.
   */
  private List<JavaInfo> getRootComponents() throws Exception {
    List<JavaInfo> rootComponents = new LinkedList<JavaInfo>(m_components);
    // remove all components that: have parent, or can not be root
    for (Iterator<JavaInfo> I = rootComponents.iterator(); I.hasNext();) {
      JavaInfo javaInfo = I.next();
      // remove component with parent
      if (javaInfo.getParent() != null) {
        I.remove();
        continue;
      }
      // remove component that can not be root
      if (NonVisualBeanInfo.isNVO(javaInfo)) {
        I.remove();
        continue;
      }
      if (!javaInfo.canBeRoot()) {
        I.remove();
        continue;
      }
    }
    // handle "parser.preferredRoot"
    getRootComponents_preferredRoot(rootComponents);
    // in general case there are more than one root, select biggest hierarchy
    Collections.sort(rootComponents, new Comparator<JavaInfo>() {
      public int compare(JavaInfo o1, JavaInfo o2) {
        return getComponentsTreeSize(o2) - getComponentsTreeSize(o1);
      }
    });
    // OK, return root components
    return rootComponents;
  }

  /**
   * If one of the root {@link JavaInfo}'s marked as "parser.preferredRoot", keep only these roots.
   */
  private static void getRootComponents_preferredRoot(List<JavaInfo> rootComponents) {
    // check if there is JavaInfo marked with "parser.preferredRoot"
    boolean hasPreferredRoot = false;
    for (JavaInfo root : rootComponents) {
      hasPreferredRoot |= isPreferredRoot(root);
    }
    // remove JavaInfo without "parser.preferredRoot"
    if (hasPreferredRoot) {
      for (Iterator<JavaInfo> I = rootComponents.iterator(); I.hasNext();) {
        JavaInfo root = I.next();
        if (!isPreferredRoot(root)) {
          I.remove();
        }
      }
    }
  }

  /**
   * @return <code>true</code> if given {@link JavaInfo} is marked as "parser.preferredRoot".
   */
  private static boolean isPreferredRoot(JavaInfo root) {
    // check EOL comment
    {
      int position = root.getCreationSupport().getNode().getStartPosition();
      String endOfLineComment = root.getEditor().getEndOfLineComment(position);
      if (StringUtils.contains(endOfLineComment, "@wbp.parser.preferredRoot")) {
        return true;
      }
    }
    // check description
    return JavaInfoUtils.hasTrueParameter(root, "parser.preferredRoot");
  }
  /**
   * Visitor for parsing {@link ASTNode}'s on execution flow.
   *
   * @author scheglov_ke
   */
  private final class ExecutionFlowParseVisitor extends ExecutionFlowFrameVisitor {
    private final Set<MethodDeclaration> m_visitedLazyMethods = Sets.newHashSet();

    ////////////////////////////////////////////////////////////////////////////
    //
    // Frames
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public boolean enterFrame(final ASTNode node) {
      // send broadcast
      ExecutionUtils.runRethrow(new RunnableEx() {
        public void run() throws Exception {
          m_editorState.getBroadcast().getListener(ExecutionFlowEnterFrame.class).invoke(node);
        }
      });
      // enter in "lazy creation" methods, but only one time
      if (node instanceof MethodDeclaration) {
        MethodDeclaration method = (MethodDeclaration) node;
        if (m_visitedLazyMethods.contains(method)) {
          return false;
        }
        if (LazyVariableSupportUtils.getInformation(method) != null) {
          m_visitedLazyMethods.add(method);
          return true;
        }
      }
      // skip factories
      if (node instanceof MethodDeclaration) {
        MethodDeclaration method = (MethodDeclaration) node;
        if (FactoryDescriptionHelper.isFactoryMethod(method)) {
          return false;
        }
      }
      // continue
      return super.enterFrame(node);
    }

    @Override
    public void leaveFrame(final ASTNode node) {
      super.leaveFrame(node);
      // send broadcast
      ExecutionUtils.runRethrow(new RunnableEx() {
        public void run() throws Exception {
          m_editorState.getBroadcast().getListener(EvaluationEventListener.class).leaveFrame(node);
        }
      });
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Evaluation
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public void postVisit(final ASTNode node) {
      ExecutionUtils.runRethrow(new RunnableEx() {
        public void run() throws Exception {
          // try to create JavaInfo using generic Expression
          if (node instanceof Expression
              && !(node instanceof ClassInstanceCreation)
              && !(node instanceof MethodInvocation)
          /*&& !(node instanceof ArrayCreation)*/) {
            endVisit((Expression) node);
          }
          // process related JavaInfo's
          evaluateNode(node);
        }
      });
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // JavaInfo's creation
    //
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Attempts to create {@link JavaInfo} based on some abstract {@link Expression}.
     */
    private void endVisit(Expression expression) {
      try {
        if (createJavaInfo_noModel(expression)) {
          return;
        }
        // ask each factory, may be this creation is JavaInfo creation
        for (IParseFactory parseFactory : m_parseFactories) {
          JavaInfo javaInfo = parseFactory.create(m_editor, expression);
          if (javaInfo != null) {
            addJavaInfo(javaInfo, expression);
            break;
          }
        }
      } catch (Throwable e) {
        ReflectionUtils.propagate(e);
      }
    }

    /**
     * @return <code>true</code> if it is known that this {@link ASTNode} must not have
     *         {@link JavaInfo} model.
     */
    private boolean createJavaInfo_noModel(ASTNode node) {
      List<ParseFactoryNoModel> validators =
          ExternalFactoriesHelper.getElementsInstances(
              ParseFactoryNoModel.class,
              "org.eclipse.wb.core.java.parseFactories",
              "noModel");
      for (ParseFactoryNoModel validator : validators) {
        if (validator.noModel(node)) {
          return true;
        }
      }
      return false;
    }

    @Override
    public void endVisit(ClassInstanceCreation creation) {
      try {
        if (createJavaInfo_noModel(creation)) {
          return;
        }
        // prepare binding
        IMethodBinding methodBinding = AstNodeUtils.getCreationBinding(creation);
        ITypeBinding typeBinding = AstNodeUtils.getTypeBinding(creation);
        if (typeBinding == null) {
          return;
        }
        // may be we know, that there is not model
        boolean canBeOptimized = !maybeNVO(creation);
        if (canBeOptimized && createJavaInfo_noModel(creation, typeBinding)) {
          return;
        }
        // prepare JavaInfo arguments
        Expression arguments[] = getExpressionArray(DomGenerics.arguments(creation));
        JavaInfo[] argumentInfos = getJavaInfoArray(arguments);
        JavaInfo javaInfo = null;
        // ask each "simple" factory
        if (canBeOptimized) {
          List<IParseFactorySimpleModelCic> validators =
              ExternalFactoriesHelper.getElementsInstances(
                  IParseFactorySimpleModelCic.class,
                  "org.eclipse.wb.core.java.parseFactories",
                  "simpleModel_CIC");
          for (IParseFactorySimpleModelCic factory : validators) {
            if (factory.accept(m_editor, creation, typeBinding)) {
              javaInfo = factory.create(m_editor, creation, typeBinding);
              if (javaInfo == null) {
                return;
              }
              break;
            }
          }
        }
        // ask each "complex" factory
        if (javaInfo == null) {
          for (IParseFactory parseFactory : m_parseFactories) {
            javaInfo =
                parseFactory.create(
                    m_editor,
                    creation,
                    methodBinding,
                    typeBinding,
                    arguments,
                    argumentInfos);
            if (javaInfo != null) {
              break;
            }
          }
        }
        // process JavaInfo
        if (javaInfo != null) {
          addJavaInfo(javaInfo, creation);
          // establish parent/child link
          bindChild_ClassInstanceCreation(creation, methodBinding, javaInfo, argumentInfos);
          bindChild_SuperConstructorInvocation(creation, javaInfo);
        }
      } catch (Throwable e) {
        ReflectionUtils.propagate(e);
      }
    }

    /**
     * @return <code>true</code> if it is known that this {@link ClassInstanceCreation} does not
     *         have {@link JavaInfo} model.
     */
    private boolean createJavaInfo_noModel(ClassInstanceCreation creation, ITypeBinding typeBinding) {
      List<ParseFactoryNoModel> validators =
          ExternalFactoriesHelper.getElementsInstances(
              ParseFactoryNoModel.class,
              "org.eclipse.wb.core.java.parseFactories",
              "noModel");
      for (ParseFactoryNoModel validator : validators) {
        if (validator.noModel(creation, typeBinding)) {
          return true;
        }
      }
      return false;
    }

    @Override
    public void endVisit(MethodInvocation invocation) {
      try {
        if (createJavaInfo_noModel(invocation)) {
          return;
        }
        // prepare method information
        IMethodBinding methodBinding = AstNodeUtils.getMethodBinding(invocation);
        if (methodBinding == null) {
          return;
        }
        Expression arguments[] = getExpressionArray(DomGenerics.arguments(invocation));
        // prepare JavaInfo's
        JavaInfo expressionInfo = getJavaInfo(invocation.getExpression());
        JavaInfo[] argumentInfos = getJavaInfoArray(arguments);
        // ask each factory, may be this invocation is JavaInfo creation
        for (IParseFactory parseFactory : m_parseFactories) {
          JavaInfo javaInfo =
              parseFactory.create(
                  m_editor,
                  invocation,
                  methodBinding,
                  arguments,
                  expressionInfo,
                  argumentInfos,
                  JavaInfoParser.this);
          if (javaInfo != null) {
            addJavaInfo(javaInfo, invocation);
            break;
          }
        }
        // establish parent/child link
        bindChild_MethodInvocation(
            invocation,
            methodBinding,
            expressionInfo,
            arguments,
            argumentInfos);
        // support for chain of invocations
        if (expressionInfo != null) {
          MethodDescription methodDescription =
              expressionInfo.getDescription().getMethod(methodBinding);
          if (methodDescription != null) {
            if (methodDescription.hasTrueTag("returnThis")) {
              expressionInfo.bindToExpression(invocation);
            }
          }
        }
      } catch (Throwable e) {
        ReflectionUtils.propagate(e);
      }
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Association
    //
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Sets the {@link Association} between parent/child {@link JavaInfo}'s. If <code>child</code>
     * already has {@link Association}, then {@link CompoundAssociation} will be installed.
     */
    private void bindChild_setAssociation(JavaInfo parent,
        JavaInfo child,
        Association newAssociation) throws Exception {
      Association existingAssociation = child.getAssociation();
      if (existingAssociation == null) {
        parent.addChild(child);
        child.setAssociation(newAssociation);
      } else {
        if (child.getParent() != parent) {
          throwDoubleAssociationException(parent, child, existingAssociation, newAssociation);
        }
        child.setAssociation(new CompoundAssociation(existingAssociation, newAssociation));
      }
    }

    /**
     * Establish parent/child {@link Association} using given {@link MethodInvocation}.
     */
    private void bindChild_MethodInvocation(MethodInvocation invocation,
        IMethodBinding methodBinding,
        JavaInfo expressionInfo,
        Expression[] arguments,
        JavaInfo argumentInfos[]) throws Exception {
      if (expressionInfo != null) {
        MethodDescription methodDescription =
            expressionInfo.getDescription().getMethod(methodBinding);
        if (methodDescription != null) {
          ITypeBinding[] parameterTypes = methodBinding.getParameterTypes();
          List<ParameterDescription> parameterDescriptions = methodDescription.getParameters();
          JavaInfo parent2Info = null;
          JavaInfo child2Info = null;
          // add association
          for (ParameterDescription parameterDescription : parameterDescriptions) {
            int parameterIndex = parameterDescription.getIndex();
            if (parameterDescription.isChild()) {
              JavaInfo parameterJavaInfo = argumentInfos[parameterIndex];
              if (parameterTypes[parameterIndex].isArray()) {
                // array parameter
                Expression parameterExpression = arguments[parameterDescription.getIndex()];
                if (parameterJavaInfo == null && parameterExpression instanceof ArrayCreation) {
                  // allow only inline array creation for simple type
                  ArrayCreation creation = (ArrayCreation) parameterExpression;
                  if (creation.getType().getComponentType().isSimpleType()) {
                    // prepare ArrayObjectInfo
                    bindChild_MethodInvocationParameter_ArrayCreation(
                        invocation,
                        methodDescription,
                        expressionInfo,
                        parameterDescription,
                        creation);
                  }
                } else if (parameterIndex == parameterDescriptions.size() - 1) {
                  // ellipsis support
                  Expression[] arrayArguments =
                      (Expression[]) ArrayUtils.subarray(
                          arguments,
                          parameterIndex,
                          arguments.length);
                  JavaInfo[] arrayArgumentInfos =
                      (JavaInfo[]) ArrayUtils.subarray(
                          argumentInfos,
                          parameterIndex,
                          argumentInfos.length);
                  // prepare EllipsisObjectInfo
                  bindChild_MethodInvocationParameter_ArrayEllipsis(
                      invocation,
                      methodDescription,
                      expressionInfo,
                      parameterIndex,
                      parameterDescription,
                      arrayArguments,
                      arrayArgumentInfos);
                }
              } else if (parameterJavaInfo != null) {
                // simple parameter
                bindChild_MethodInvocationParameter(
                    invocation,
                    methodDescription,
                    expressionInfo,
                    parameterDescription,
                    parameterJavaInfo);
              }
            }
            // find "secondary" association
            if (parameterDescription.isParent2()) {
              parent2Info = argumentInfos[parameterIndex];
            }
            if (parameterDescription.isChild2()) {
              child2Info = argumentInfos[parameterIndex];
            }
          }
          // set "secondary" association
          if (parent2Info != null && child2Info != null) {
            bindChild_setAssociation(
                parent2Info,
                child2Info,
                new InvocationSecondaryAssociation(invocation));
          }
        }
      }
    }

    private void bindChild_MethodInvocationParameter(MethodInvocation invocation,
        MethodDescription methodDescription,
        JavaInfo expressionInfo,
        ParameterDescription parameterDescription,
        JavaInfo parameterJavaInfo) throws Exception {
      if (parameterDescription.hasTrueTag("associateOnlyFirstTime")
          && parameterJavaInfo.getParent() != null) {
        return;
      }
      expressionInfo.getBroadcast(JavaInfoMethodAssociationOnParse.class).invoke(
          expressionInfo,
          parameterJavaInfo,
          methodDescription);
      bindChild_setAssociation(
          expressionInfo,
          parameterJavaInfo,
          new InvocationChildAssociation(invocation));
    }

    @SuppressWarnings("unchecked")
    private void bindChild_MethodInvocationParameter_ArrayCreation(MethodInvocation invocation,
        MethodDescription methodDescription,
        JavaInfo expressionInfo,
        ParameterDescription parameterDescription,
        ArrayCreation creation) throws Exception {
      ArrayObjectInfo arrayInfo =
          new ArrayObjectInfo(m_editor, methodDescription.getName(), creation);
      expressionInfo.addChild(arrayInfo);
      arrayInfo.setRemoveOnEmpty(parameterDescription.hasTrueTag(AbstractArrayObjectInfo.REMOVE_ON_EMPTY_TAG));
      arrayInfo.setHideInTree(parameterDescription.hasTrueTag(AbstractArrayObjectInfo.HIDE_IN_TREE_TAG));
      //arrayInfo.setCreationId(parameter.getTag(ArrayObjectInfo.CREATION_ID_TAG));
      // prepare items
      Expression[] arrayItems = getExpressionArray(creation.getInitializer().expressions());
      JavaInfo[] arrayJavaInfos = getJavaInfoArray(arrayItems);
      // process array items
      for (int i = 0; i < arrayJavaInfos.length; i++) {
        JavaInfo arrayJavaInfo = arrayJavaInfos[i];
        if (arrayJavaInfo != null) {
          if (parameterDescription.hasTrueTag("associateOnlyFirstTime")
              && arrayJavaInfo.getParent() != null) {
            continue;
          }
          Assert.isNull(
              arrayJavaInfo.getParent(),
              String.format("Parent for array item %s already assigned.", arrayJavaInfo.toString()));
          arrayInfo.addItem(arrayJavaInfo);
          expressionInfo.getBroadcast(JavaInfoMethodAssociationOnParse.class).invoke(
              expressionInfo,
              arrayJavaInfo,
              methodDescription);
          bindChild_setAssociation(
              expressionInfo,
              arrayJavaInfo,
              new InvocationChildArrayAssociation(invocation, arrayInfo));
        }
      }
    }

    @SuppressWarnings("cast")
    private void bindChild_MethodInvocationParameter_ArrayEllipsis(MethodInvocation invocation,
        MethodDescription methodDescription,
        JavaInfo expressionInfo,
        int parameterIndex,
        ParameterDescription parameterDescription,
        Expression[] arrayArguments,
        JavaInfo arrayJavaInfos[]) throws Exception {
      Class<?> itemType = (Class<?>) parameterDescription.getType();
      Assert.isTrue(itemType.isArray(), "Ellipsis type not array.");
      EllipsisObjectInfo arrayInfo =
          new EllipsisObjectInfo(m_editor,
              methodDescription.getName(),
              itemType.getComponentType(),
              invocation,
              parameterIndex);
      expressionInfo.addChild(arrayInfo);
      arrayInfo.setRemoveOnEmpty(parameterDescription.hasTrueTag(AbstractArrayObjectInfo.REMOVE_ON_EMPTY_TAG));
      arrayInfo.setHideInTree(parameterDescription.hasTrueTag(AbstractArrayObjectInfo.HIDE_IN_TREE_TAG));
      arrayInfo.setOnEmptySource(parameterDescription.getTag(EllipsisObjectInfo.ON_EMPTY_SOURCE_TAG));
      // process array items
      for (int i = 0; i < arrayJavaInfos.length; i++) {
        JavaInfo arrayJavaInfo = arrayJavaInfos[i];
        if (arrayJavaInfo != null) {
          if (parameterDescription.hasTrueTag("associateOnlyFirstTime")
              && arrayJavaInfo.getParent() != null) {
            continue;
          }
          Assert.isNull(
              arrayJavaInfo.getParent(),
              String.format("Parent for array item %s already assigned.", arrayJavaInfo.toString()));
          arrayInfo.addItem(arrayJavaInfo);
          expressionInfo.getBroadcast(JavaInfoMethodAssociationOnParse.class).invoke(
              expressionInfo,
              arrayJavaInfo,
              methodDescription);
          bindChild_setAssociation(
              expressionInfo,
              arrayJavaInfo,
              new InvocationChildEllipsisAssociation(invocation, arrayInfo));
        }
      }
    }

    /**
     * Establish parent/child {@link Association} using given {@link ClassInstanceCreation}.
     */
    private void bindChild_ClassInstanceCreation(ClassInstanceCreation creation,
        IMethodBinding methodBinding,
        JavaInfo javaInfo,
        JavaInfo argumentInfos[]) throws Exception {
      if (javaInfo.getParent() == null) {
        ConstructorDescription constructor =
            javaInfo.getDescription().getConstructor(methodBinding);
        if (constructor != null) {
          for (ParameterDescription parameter : constructor.getParameters()) {
            // parent passed as constructor argument
            if (parameter.isParent()) {
              JavaInfo parentInfo = argumentInfos[parameter.getIndex()];
              if (parentInfo != null) {
                parentInfo.addChild(javaInfo);
                javaInfo.setAssociation(new ConstructorParentAssociation());
              }
              break;
            }
            // child passed as constructor argument
            if (parameter.isChild()) {
              JavaInfo childInfo = argumentInfos[parameter.getIndex()];
              if (childInfo != null) {
                javaInfo.addChild(childInfo);
                childInfo.setAssociation(new ConstructorChildAssociation());
              }
              break;
            }
          }
        }
      }
    }

    /**
     * New {@link JavaInfo} was just created using given {@link Expression}. This method performs
     * special check, if this {@link Expression} is argument of {@link SuperConstructorInvocation},
     * so sets object and parent/child link.
     * <p>
     * For example: <code><pre>
     *   public class Test extends JPanel {
     *     public Test() {
     *       this(new BorderLayout());
     *     }
     *   }
     * </pre><code>
     */
    private void bindChild_SuperConstructorInvocation(Expression expression, JavaInfo javaInfo)
        throws Exception {
      if (javaInfo.getParent() == null) {
        if (expression.getLocationInParent() == SuperConstructorInvocation.ARGUMENTS_PROPERTY) {
          SuperConstructorInvocation constructorInvocation =
              (SuperConstructorInvocation) expression.getParent();
          IMethodBinding constructorBinding = AstNodeUtils.getSuperBinding(constructorInvocation);
          // set Object
          {
            Object object = JavaInfoEvaluationHelper.getValue(expression);
            javaInfo.setObject(object);
          }
          // set parent/child link
          {
            JavaInfo thisJavaInfo = getJavaInfo(null);
            ConstructorDescription constructorDescription =
                thisJavaInfo.getDescription().getConstructor(constructorBinding);
            if (constructorDescription != null) {
              int argumentIndex = constructorInvocation.arguments().indexOf(expression);
              ParameterDescription parameter = constructorDescription.getParameter(argumentIndex);
              // child passed as SuperConstructorInvocation argument
              if (parameter.isChild()) {
                thisJavaInfo.addChild(javaInfo);
                Association association =
                    new SuperConstructorArgumentAssociation(constructorInvocation);
                javaInfo.setAssociation(association);
              }
            }
          }
        }
      }
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Variables
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public void endVisit(VariableDeclarationFragment declaration) {
      if (createJavaInfo_noModel(declaration)) {
        return;
      }
      SimpleName variable = declaration.getName();
      Expression initializer = declaration.getInitializer();
      createVariableSupport(variable, initializer);
    }

    @Override
    public void endVisit(Assignment assignment) {
      if (createJavaInfo_noModel(assignment)) {
        return;
      }
      Expression variable = assignment.getLeftHandSide();
      if (AstNodeUtils.isVariable(variable)) {
        Expression initializer = assignment.getRightHandSide();
        createVariableSupport(variable, initializer);
      }
    }

    private void createVariableSupport(final Expression variable, final Expression initializer) {
      ExecutionUtils.runRethrow(new RunnableEx() {
        public void run() throws Exception {
          JavaInfo javaInfo = getJavaInfo(initializer);
          if (javaInfo != null) {
            // check if there is already variable support
            boolean noVariableSupport;
            {
              VariableSupport support = javaInfo.getVariableSupport();
              noVariableSupport = support == null || support.isDefault();
            }
            // only first assignment is variable
            if (noVariableSupport) {
              VariableSupport support;
              // prepare assignments and binding
              List<Expression> assignments =
                  ExecutionFlowUtils.getAssignments(m_editorState.getFlowDescription(), variable);
              IVariableBinding variableBinding = AstNodeUtils.getVariableBinding(variable);
              // local or field
              if (variableBinding.isField()) {
                if (assignments.size() == 1) {
                  if (variable.getParent() instanceof VariableDeclarationFragment) {
                    support = new FieldInitializerVariableSupport(javaInfo, variable);
                  } else {
                    support = new FieldUniqueVariableSupport(javaInfo, variable);
                  }
                } else {
                  support = new FieldReuseVariableSupport(javaInfo, variable);
                }
              } else {
                SimpleName simpleVariable = (SimpleName) variable;
                if (assignments.size() == 1) {
                  support = new LocalUniqueVariableSupport(javaInfo, simpleVariable);
                } else {
                  support = new LocalReuseVariableSupport(javaInfo, simpleVariable);
                }
              }
              // set variable support
              javaInfo.setVariableSupport(support);
            }
          }
        }
      });
    }
  }

  /**
   * @return the {@link ParseRootContext} for beginning parsing
   */
  private ParseRootContext prepareParseContext() throws Exception {
    // prepare type and binding
    IType primaryType;
    ITypeBinding typeBinding;
    {
      ICompilationUnit modelUnit = m_editor.getModelUnit();
      String unitPath = modelUnit.getUnderlyingResource().getFullPath().toPortableString();
      primaryType = CodeUtils.findPrimaryType(modelUnit);
      Assert.isTrueException(
          primaryType != null,
          ICoreExceptionConstants.PARSER_NO_PRIMARY_TYPE,
          unitPath);
      String primaryTypeName = primaryType.getElementName();
      m_typeDeclaration = AstNodeUtils.getTypeByName(m_editor.getAstUnit(), primaryTypeName);
      Assert.isTrueException(
          m_typeDeclaration != null,
          ICoreExceptionConstants.PARSER_NO_TYPE_DECLARATION,
          unitPath);
      typeBinding = m_typeDeclaration.resolveBinding();
      Assert.isNotNull(typeBinding);
    }
    // ask each factory for root methods
    for (IParseFactory parseFactory : m_parseFactories) {
      ParseRootContext rootContext =
          parseFactory.getRootContext(m_editor, m_typeDeclaration, typeBinding);
      if (rootContext != null) {
        return rootContext;
      }
    }
    // check that one of the parse factories identified toolkit
    {
      EditorState editorState = EditorState.get(m_editor);
      if (editorState.getEditorLoader() == null) {
        if (!canBeClassWithGUI()) {
          throw new DesignerException(ICoreExceptionConstants.PARSER_NOT_GUI,
              primaryType.getFullyQualifiedName());
        }
        throw new DesignerException(ICoreExceptionConstants.PARSER_NO_TOOLKIT,
            primaryType.getFullyQualifiedName());
      }
    }
    // no root context found, try to find "main"
    {
      MethodDeclaration mainMethod =
          AstNodeUtils.getMethodBySignature(m_typeDeclaration, "main(java.lang.String[])");
      if (mainMethod != null) {
        return new ParseRootContext(null, new ExecutionFlowDescription(mainMethod));
      }
    }
    // still no entry point, try to use constructor
    {
      MethodDeclaration constructor =
          ExecutionFlowUtils.getExecutionFlowConstructor(m_typeDeclaration);
      if (constructor != null) {
        return new ParseRootContext(null, new ExecutionFlowDescription(constructor));
      }
    }
    // special handling for factories
    AbstractParseFactory.failIfFactory(m_editor, m_typeDeclaration, typeBinding);
    // can not find root methods, fail
    m_editorState.getBroadcast().getListener(ObjectEventListener.class).dispose();
    throw new NoEntryPointError(m_editor, m_typeDeclaration);
  }

  /**
   * Checks if {@link CompilationUnit} can have GUI, but we were not able no understand it because
   * of no corresponding GUI toolkit. Right now we just check that there are
   * {@link ClassInstanceCreation}'s, so for example identify empty classes or data beans.
   */
  private boolean canBeClassWithGUI() {
    final AtomicBoolean result = new AtomicBoolean();
    m_editor.getAstUnit().accept(new ASTVisitor() {
      @Override
      public void endVisit(ClassInstanceCreation node) {
        result.set(true);
      }
    });
    return result.get();
  }

  /**
   * Adds new given {@link JavaInfo} to the list of components.
   */
  private void addJavaInfo(JavaInfo javaInfo, Expression creation) throws Exception {
    // add component
    m_javaInfoResolver.addJavaInfo(javaInfo, creation);
    javaInfo.addRelatedNode(creation);
    // activate Java state
    GlobalStateJava.activate(javaInfo);
    // set default variable support
    {
      // check for "lazy creation" pattern
      if (javaInfo.getVariableSupport() == null) {
        LazyVariableSupportUtils.setLazyVariable(javaInfo);
      }
      // set "empty" variable support
      if (javaInfo.getVariableSupport() == null) {
        Assert.isNotNull(creation);
        javaInfo.setVariableSupport(new EmptyVariableSupport(javaInfo, creation));
      }
    }
    // evaluate "this"
    if (!javaInfo.getCreationSupport().canBeEvaluated()) {
      getEvaluationHelper().evaluateJavaInfoUsingCreationSupport(javaInfo);
    }
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Evaluation
  //
  ////////////////////////////////////////////////////////////////////////////
  private final Set<ASTNode> m_evaluatedNodes = Sets.newHashSet();
  private JavaInfoEvaluationHelper m_evaluationHelper;

  /**
   * @return the {@link JavaInfoEvaluationHelper} for this {@link AstEditor}.
   */
  private JavaInfoEvaluationHelper getEvaluationHelper() {
    if (m_evaluationHelper == null) {
      m_evaluationHelper = new JavaInfoEvaluationHelper(m_editor, getParseVisitor()) {
        @Override
        protected JavaInfo getRootJavaInfo() {
          return m_rootComponent;
        }

        @Override
        protected JavaInfo getJavaInfoRepresentedBy(Expression expression) {
          return getJavaInfo(expression);
        }

        @Override
        protected void thisJavaInfoNodeProcessed(JavaInfo javaInfo, ASTNode node) throws Exception {
          javaInfo.addRelatedNode(node);
        }
      };
    }
    return m_evaluationHelper;
  }

  /**
   * Evaluates given {@link ASTNode} using {@link JavaInfoEvaluationHelper}.
   */
  private void evaluateNode(ASTNode node) throws Exception {
    if (!m_evaluatedNodes.contains(node)) {
      m_evaluatedNodes.add(node);
      try {
        JavaInfoEvaluationHelper evaluationHelper = getEvaluationHelper();
        // send notifications and evaluate
        if (shouldNotifyAboutEvaluate(node)) {
          m_evaluationListener.evaluateBefore(null, node);
        }
        evaluationHelper.evaluate(node);
        if (shouldNotifyAboutEvaluate(node)) {
          m_evaluationListener.evaluateAfter(null, node);
        }
      } catch (Throwable e) {
        // fail, if fatal
        if (DesignerExceptionUtils.isFatal(e)) {
          ReflectionUtils.propagate(e);
        }
        // exception during JavaInfo evaluation is serious, so fail
        if (node instanceof Expression && getJavaInfo((Expression) node) != null) {
          ReflectionUtils.propagate(e);
        }
        // some other exception, log and continue
        m_editorState.getBadParserNodes().add(node, e);
      }
    }
  }

  private static boolean shouldNotifyAboutEvaluate(ASTNode node) {
    return node instanceof ClassInstanceCreation
        || node instanceof MethodInvocation
        || node instanceof Statement;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the array of {@link Expression}'s for given list of them.
   */
  private static Expression[] getExpressionArray(List<Expression> expressionList) {
    return expressionList.toArray(new Expression[expressionList.size()]);
  }

  /**
   * @return the size (count of elements) in components tree starting from given root.
   */
  private static int getComponentsTreeSize(ObjectInfo info) {
    int size = 1;
    // add sizes of children
    for (ObjectInfo child : info.getChildren()) {
      size += getComponentsTreeSize(child);
    }
    // return final size
    return size;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // JavaInfo access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link JavaInfo} for given {@link Expression} or <code>null</code> if given
   *         {@link Expression} does not represent {@link JavaInfo}.
   */
  public JavaInfo getJavaInfo(Expression expression) {
    return m_javaInfoResolver.getJavaInfo(expression);
  }

  /**
   * @return the array of {@link JavaInfo}'s represented by given array of {@link Expression}'s.
   */
  private JavaInfo[] getJavaInfoArray(Expression expressions[]) throws Exception {
    JavaInfo javaInfos[] = new JavaInfo[expressions.length];
    for (int i = 0; i < expressions.length; i++) {
      Expression expression = expressions[i];
      javaInfos[i] = getJavaInfo(expression);
    }
    return javaInfos;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Exceptions
  //
  ////////////////////////////////////////////////////////////////////////////
  private void throwDoubleAssociationException(JavaInfo parent,
      JavaInfo child,
      Association existingAssociation,
      Association newAssociation) throws DesignerException {
    String existingStatementSource = getAssociationStatementSource(existingAssociation);
    String newStatementSource = getAssociationStatementSource(newAssociation);
    String message =
        MessageFormat.format(
            "\nChild:                    \n  {0}"
                + "\nExisting: \n  {1} \n    {2}"
                + "\nNew: \n  {3} \n    {4}",
            child,
            child.getParent(),
            existingAssociation,
            parent,
            newAssociation);
    DesignerException designerWarning =
        new DesignerException(ICoreExceptionConstants.PARSER_DOUBLE_ASSOCIATION,
            getShortComponentName(child),
            existingStatementSource,
            newStatementSource,
            message);
    designerWarning.setSourcePosition(m_visitor.m_currentStatement.getStartPosition());
    throw designerWarning;
  }

  /**
   * @return the source of {@link Association} statement or <code>"null"</code>.
   */
  private String getAssociationStatementSource(Association association) {
    Statement statement = association.getStatement();
    return statement != null ? m_editor.getSource(statement) : "<null>";
  }

  private static String getShortComponentName(JavaInfo javaInfo) {
    if (javaInfo == null) {
      return "<null>";
    }
    return javaInfo.getVariableSupport().getComponentName();
  }

  /**
   * @return <code>true</code> if given {@link ClassInstanceCreation} may be NVO creation, so sohuld
   *         be handled without optimizations.
   */
  private static boolean maybeNVO(ClassInstanceCreation creation) {
    BodyDeclaration body = AstNodeUtils.getEnclosingNode(creation, BodyDeclaration.class);
    if (body != null) {
      Javadoc doc = body.getJavadoc();
      return doc != null && doc.toString().contains("@wbp.nonvisual");
    }
    return false;
  }
}
