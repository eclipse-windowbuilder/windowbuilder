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
package org.eclipse.wb.tests.designer.core.model.parser;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.ObjectInfoVisitor;
import org.eclipse.wb.internal.core.model.clipboard.IClipboardCreationSupport;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMementoTransfer;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ToolkitDescription;
import org.eclipse.wb.internal.core.model.description.helpers.EditorStateLoadingContext;
import org.eclipse.wb.internal.core.model.description.helpers.ILoadingContext;
import org.eclipse.wb.internal.core.model.generic.FlowContainer;
import org.eclipse.wb.internal.core.model.generic.FlowContainerFactory;
import org.eclipse.wb.internal.core.model.generic.SimpleContainer;
import org.eclipse.wb.internal.core.model.generic.SimpleContainerFactory;
import org.eclipse.wb.internal.core.model.util.GlobalStateJava;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.parser.JavaInfoParser;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.BodyDeclarationTarget;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.internal.swing.model.layout.absolute.AbsoluteLayoutInfo;
import org.eclipse.wb.tests.designer.core.PreferencesRepairer;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.description.Description;

import java.util.List;

public class AbstractJavaInfoTest extends AbstractJavaInfoRelatedTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    m_ignoreCompilationProblems = false;
    m_waitForAutoBuild = false;
    // use strict evaluation mode
    useStrictEvaluationMode(true);
    // configure toolkits
    configureToolkits();
    // use default Java project options
    if (m_javaProject != null) {
      m_javaProject.setOptions(null);
    }
  }

  protected void configureToolkits() {
    // none
  }

  @Override
  protected void tearDown() throws Exception {
    disposeLastModel();
    tearDown_afterLastModelDispose();
    super.tearDown();
  }

  /**
   * This method is executed during {@link #tearDown()}, directly after disposing last
   * {@link JavaInfo} model.
   */
  protected void tearDown_afterLastModelDispose() throws Exception {
  }

  /**
   * Disposes last parsed {@link JavaInfo}.
   */
  protected final void disposeLastModel() throws Exception {
    if (m_lastParseInfo != null) {
      m_lastParseInfo.refresh_dispose();
      m_lastParseInfo.getBroadcastObject().dispose();
      m_lastParseInfo = null;
    }
    if (m_lastState != null) {
      GlobalStateJava.deactivate();
      m_lastState = null;
      m_lastLoadingContext = null;
    }
    m_lastLoader = null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Project disposing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void test_tearDown() throws Exception {
    do_projectDispose();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Configuration
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Specifies if strict evaluation mode should be used or not.
   */
  protected static void useStrictEvaluationMode(boolean value) {
    DesignerPlugin.getDefault().getPreferenceStore().setValue(
        org.eclipse.wb.internal.core.preferences.IPreferenceConstants.P_CODE_STRICT_EVALUATE,
        value);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // get: some ASTNode relative to JavaInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link TypeDeclaration} that contains given {@link JavaInfo}.
   */
  protected static TypeDeclaration getTypeDeclaration(JavaInfo javaInfo) {
    ASTNode node = javaInfo.getCreationSupport().getNode();
    return AstNodeUtils.getEnclosingType(node);
  }

  /**
   * @return the {@link BodyDeclaration} with given index in {@link TypeDeclaration} that contains
   *         given {@link JavaInfo}.
   */
  protected static BodyDeclaration getBodyDeclaration(JavaInfo javaInfo, int index) {
    ASTNode node = javaInfo.getCreationSupport().getNode();
    TypeDeclaration typeDeclaration = AstNodeUtils.getEnclosingType(node);
    return DomGenerics.bodyDeclarations(typeDeclaration).get(index);
  }

  /**
   * @return the {@link Statement} with given index's in {@link Block} that contains given
   *         {@link JavaInfo}.
   */
  protected static Block getBlock(JavaInfo javaInfo, int... indexes) {
    return (Block) getStatement(javaInfo, indexes);
  }

  /**
   * @return the {@link Block} with given index's in body of {@link MethodDeclaration} with given
   *         signature.
   */
  protected static Block getBlock(JavaInfo javaInfo, String methodSignature, int... indexes) {
    return (Block) getStatement(javaInfo, methodSignature, indexes);
  }

  /**
   * @return the {@link Statement} with given index's in body of {@link MethodDeclaration} with
   *         given signature.
   */
  protected static Statement getStatement(JavaInfo javaInfo, String methodSignature, int... indexes) {
    TypeDeclaration typeDeclaration = JavaInfoUtils.getTypeDeclaration(javaInfo);
    MethodDeclaration methodDeclaration =
        AstNodeUtils.getMethodBySignature(typeDeclaration, methodSignature);
    Assert.isNotNull(methodDeclaration, "no %s in %s", methodSignature, typeDeclaration);
    return getStatement(methodDeclaration.getBody(), indexes);
  }

  /**
   * Asserts that given {@link StatementTarget} has same state as expected.
   */
  protected static void assertTarget(StatementTarget target,
      Block expectedBlock,
      Statement expectedStatement,
      boolean expectedBefore) {
    StatementTarget expectedTarget =
        new StatementTarget(expectedBlock, expectedStatement, expectedBefore);
    assertEquals(expectedTarget.toString(), target.toString());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Target utils
  //
  ////////////////////////////////////////////////////////////////////////////
  protected static NodeTarget getNodeStatementTarget(JavaInfo javaInfo,
      boolean before,
      int... indexes) {
    return new NodeTarget(getStatementTarget(javaInfo, before, indexes));
  }

  protected static NodeTarget getNodeStatementTarget(JavaInfo javaInfo,
      String methodSignature,
      boolean before,
      int... indexes) {
    StatementTarget statementTarget =
        getStatementTarget(javaInfo, methodSignature, before, indexes);
    return new NodeTarget(statementTarget);
  }

  protected static NodeTarget getNodeBlockTarget(JavaInfo javaInfo,
      String methodSignature,
      boolean before,
      int... indexes) {
    StatementTarget statementTarget = getBlockTarget(javaInfo, methodSignature, before, indexes);
    return new NodeTarget(statementTarget);
  }

  protected static NodeTarget getNodeBlockTarget(JavaInfo javaInfo, boolean before, int... indexes) {
    return new NodeTarget(getBlockTarget(javaInfo, before, indexes));
  }

  protected static NodeTarget getNodeBodyDeclarationTarget(JavaInfo javaInfo,
      boolean before,
      int index) {
    return new NodeTarget(getBodyDeclarationTarget(javaInfo, before, index));
  }

  protected static NodeTarget getNodeTypeDeclarationTarget(JavaInfo javaInfo, boolean before) {
    return new NodeTarget(getTypeDeclarationTarget(javaInfo, before));
  }

  protected static StatementTarget getStatementTarget(JavaInfo javaInfo,
      boolean before,
      int... indexes) {
    Statement targetStatement = getStatement(javaInfo, indexes);
    return new StatementTarget(targetStatement, before);
  }

  protected static StatementTarget getBlockTarget(JavaInfo javaInfo, boolean before, int... indexes) {
    Block targetBlock = (Block) getStatement(javaInfo, indexes);
    return new StatementTarget(targetBlock, before);
  }

  protected static StatementTarget getStatementTarget(JavaInfo javaInfo,
      String methodSignature,
      boolean before,
      int... indexes) {
    Statement targetStatement = getStatement(javaInfo, methodSignature, indexes);
    return new StatementTarget(targetStatement, before);
  }

  protected static StatementTarget getBlockTarget(JavaInfo javaInfo,
      String methodSignature,
      boolean before,
      int... indexes) {
    Block targetBlock = (Block) getStatement(javaInfo, methodSignature, indexes);
    return new StatementTarget(targetBlock, before);
  }

  /**
   * Asserts that {@link StatementTarget} for given {@link JavaInfo} has same state as expected.
   */
  protected static void assertStatementTarget(JavaInfo javaInfo,
      Block expectedBlock,
      Statement expectedStatement,
      boolean expectedBefore) throws Exception {
    VariableSupport variableSupport = javaInfo.getVariableSupport();
    StatementTarget target = variableSupport.getStatementTarget();
    assertTarget(target, expectedBlock, expectedStatement, expectedBefore);
  }

  protected static BodyDeclarationTarget getBodyDeclarationTarget(JavaInfo javaInfo,
      boolean before,
      int index) {
    BodyDeclaration bodyDeclaration = getBodyDeclaration(javaInfo, index);
    return new BodyDeclarationTarget(bodyDeclaration, before);
  }

  protected static BodyDeclarationTarget getTypeDeclarationTarget(JavaInfo javaInfo, boolean before) {
    TypeDeclaration typeDeclaration = getTypeDeclaration(javaInfo);
    return new BodyDeclarationTarget(typeDeclaration, before);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  protected static void assertNodes(AstEditor editor, List<ASTNode> nodes, String expectedNodes[])
      throws Exception {
    assertEquals(expectedNodes.length, nodes.size());
    for (int i = 0; i < expectedNodes.length; i++) {
      String expectedSource = expectedNodes[i];
      String actualSource = editor.getSource(nodes.get(i));
      assertEquals(expectedSource, actualSource);
    }
  }

  /**
   * Creates components tree for given {@link JavaInfo} root and check that all {@link JavaInfo}'s
   * have objects.
   */
  protected static void assert_creation(JavaInfo root) throws Exception {
    // initially objects are not created
    assert_isCleanHierarchy(root);
    // after "refresh" we should have object for each JavaInfo
    root.refresh();
    root.accept(new ObjectInfoVisitor() {
      @Override
      public void endVisit(ObjectInfo objectInfo) throws Exception {
        if (objectInfo instanceof AbsoluteLayoutInfo) {
          // in Swing absolute layout has object "null"
        } else if (objectInfo instanceof JavaInfo) {
          JavaInfo javaInfo = (JavaInfo) objectInfo;
          assertNotNull("Object expected.", javaInfo.getObject());
        }
      }
    });
    // after "refresh_beforeCreate" all objects should be removed
    root.refresh_dispose();
    assert_isCleanHierarchy(root);
  }

  /**
   * During parsing we create objects, but at the end we should clean up all.
   */
  protected static void assert_isCleanHierarchy(JavaInfo root) throws Exception {
    root.accept(new ObjectInfoVisitor() {
      @Override
      public void endVisit(ObjectInfo objectInfo) throws Exception {
        if (objectInfo instanceof JavaInfo) {
          JavaInfo javaInfo = (JavaInfo) objectInfo;
          assertNull(javaInfo.getObject());
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clipboard
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Asserts that {@link CreationSupport} of given {@link JavaInfo} implements
   * {@link IClipboardCreationSupport} and returns expected source.
   */
  public static void assertClipboardSource(JavaInfo javaInfo, String expectedSource)
      throws Exception {
    IClipboardCreationSupport clipboardCreationSupport =
        javaInfo.getCreationSupport().getClipboard();
    CreationSupport creationSupport = clipboardCreationSupport.create(javaInfo);
    String creationSource = creationSupport.add_getSource(null);
    assertEquals(expectedSource, creationSource);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clipboard
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the copy of given {@link JavaInfoMemento}, created by serializing and reading given
   *         one.
   */
  public static JavaInfoMemento recodeMemento(JavaInfoMemento memento) throws Exception {
    byte[] bytes = JavaInfoMementoTransfer.convertObjectToBytes(memento);
    return (JavaInfoMemento) JavaInfoMementoTransfer.convertBytesToObject(bytes);
  }

  /**
   * Performs copy/paste of given {@link JavaInfo}.
   */
  public static <T extends JavaInfo> void doCopyPaste(final T source,
      final PasteProcedure<T> pasteProcedure) throws Exception {
    final JavaInfoMemento memento = JavaInfoMemento.createMemento(source);
    ExecutionUtils.run(source, new RunnableEx() {
      @SuppressWarnings("unchecked")
      public void run() throws Exception {
        T copy = (T) memento.create(source);
        pasteProcedure.run(copy);
        memento.apply();
      }
    });
  }

  public interface PasteProcedure<T> {
    void run(T copy) throws Exception;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  protected boolean m_ignoreCompilationProblems;
  protected boolean m_waitForAutoBuild;
  protected JavaInfo m_lastParseInfo;
  protected EditorState m_lastState;
  protected ILoadingContext m_lastLoadingContext;
  protected ClassLoader m_lastLoader;

  /**
   * @return {@link JavaInfo} root for parsing given unit.
   */
  public final JavaInfo parseSource(String packageName, String unitName, String unitSource)
      throws Exception {
    //System.out.println(getName());
    //long start = System.currentTimeMillis();
    ICompilationUnit unit = createModelCompilationUnit(packageName, unitName, unitSource);
    return parseCompilationUnit(unit);
  }

  public final JavaInfo parseCompilationUnit(ICompilationUnit unit) throws Exception {
    // wait for build
    if (m_waitForAutoBuild) {
      waitForAutoBuild();
    }
    // check that there are no compilation problems
    if (!m_ignoreCompilationProblems) {
      AstEditor editor = new AstEditor(unit);
      IProblem[] problems = editor.getAstUnit().getProblems();
      for (IProblem problem : problems) {
        Assert.isTrue(
            !problem.isError(),
            problem.getMessage() + " at line " + problem.getSourceLineNumber());
      }
    }
    // parse for JavaInfo
    m_lastParseInfo = JavaInfoParser.parse(unit);
    m_lastEditor = m_lastParseInfo.getEditor();
    m_lastState = EditorState.get(m_lastEditor);
    m_lastLoadingContext = EditorStateLoadingContext.get(m_lastState);
    m_lastLoader = m_lastState.getEditorLoader();
    //System.out.println("\tparse time: " + (System.currentTimeMillis() - start));
    return m_lastParseInfo;
  }

  /**
   * Calls {@link JavaInfo#refresh()} for last paster hierarchy.
   */
  public void refresh() throws Exception {
    m_lastParseInfo.refresh();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets value for "text" property, with "auto rename" set as "always".
   */
  public static void setText_withAlwaysRename(JavaInfo component, String text) throws Exception {
    ToolkitDescription toolkit = component.getDescription().getToolkit();
    PreferencesRepairer preferencesRepairer = new PreferencesRepairer(toolkit.getPreferences());
    try {
      // configure
      preferencesRepairer.setValue(
          org.eclipse.wb.internal.core.preferences.IPreferenceConstants.P_VARIABLE_TEXT_MODE,
          org.eclipse.wb.internal.core.preferences.IPreferenceConstants.V_VARIABLE_TEXT_MODE_ALWAYS);
      preferencesRepairer.setValue(
          org.eclipse.wb.internal.core.preferences.IPreferenceConstants.P_VARIABLE_TEXT_TEMPLATE,
          "${text}${class_name}");
      // set text
      component.getPropertyByTitle("text").setValue(text);
    } finally {
      preferencesRepairer.restore();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation children
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Asserts that given {@link ObjectInfo} is visible in tree children of parent.
   */
  public static void assertVisibleInTree(final ObjectInfo child, boolean expected) throws Exception {
    List<ObjectInfo> children = child.getParent().getPresentation().getChildrenTree();
    if (expected) {
      assertThat(children).as(new Description() {
        public String value() {
          return "Should be in children: " + child;
        }
      }).contains(child);
    } else {
      assertThat(children).as(new Description() {
        public String value() {
          return "Should not be in children: " + child;
        }
      }).doesNotContain(child);
    }
  }

  /**
   * Asserts that given {@link ObjectInfo} is visible in graphical children of parent.
   */
  public static void assertVisibleInGraphical(final ObjectInfo child, boolean expected)
      throws Exception {
    List<ObjectInfo> children = child.getParent().getPresentation().getChildrenGraphical();
    if (expected) {
      assertThat(children).as(new Description() {
        public String value() {
          return "Should be in children: " + child;
        }
      }).contains(child);
    } else {
      assertThat(children).as(new Description() {
        public String value() {
          return "Should not be in children: " + child;
        }
      }).doesNotContain(child);
    }
  }

  /**
   * Asserts that given {@link ObjectInfo} is visible in both tree and graphical children of parent.
   */
  public static void assertVisible(ObjectInfo child, boolean expected) throws Exception {
    assertVisibleInGraphical(child, expected);
    assertVisibleInTree(child, expected);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // FlowContainer
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Uses first {@link FlowContainer} to perform CREATE.
   */
  public static void flowContainer_CREATE(final JavaInfo container,
      final Object object,
      final Object reference) throws Exception {
    ExecutionUtils.run(container, new RunnableEx() {
      public void run() throws Exception {
        FlowContainer flowContainer = new FlowContainerFactory(container, false).get().get(0);
        flowContainer.command_CREATE(object, reference);
      }
    });
  }

  /**
   * Uses first {@link FlowContainer} to perform MOVE.
   */
  public static void flowContainer_MOVE(final JavaInfo container,
      final Object object,
      final Object reference) throws Exception {
    ExecutionUtils.run(container, new RunnableEx() {
      public void run() throws Exception {
        FlowContainer flowContainer = new FlowContainerFactory(container, false).get().get(0);
        flowContainer.command_MOVE(object, reference);
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // SimpleContainer
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Uses first {@link SimpleContainer} to perform CREATE.
   */
  public static void simpleContainer_CREATE(JavaInfo container, Object object) throws Exception {
    SimpleContainer simpleContainer = new SimpleContainerFactory(container, false).get().get(0);
    simpleContainer.command_CREATE(object);
  }

  /**
   * Uses first {@link SimpleContainer} to perform ADD.
   */
  public static void simpleContainer_ADD(JavaInfo container, Object object) throws Exception {
    SimpleContainer simpleContainer = new SimpleContainerFactory(container, false).get().get(0);
    simpleContainer.command_ADD(object);
  }
}
