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
package org.eclipse.wb.tests.designer.core.model.variables;

import org.eclipse.wb.core.eval.ExecutionFlowDescription;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.factory.ImplicitFactoryCreationSupport;
import org.eclipse.wb.internal.core.model.generation.statement.lazy.LazyStatementGeneratorDescription;
import org.eclipse.wb.internal.core.model.variable.LazyVariableSupport;
import org.eclipse.wb.internal.core.model.variable.LazyVariableSupportUtils;
import org.eclipse.wb.internal.core.model.variable.description.LazyVariableDescription;
import org.eclipse.wb.internal.core.utils.ast.BodyDeclarationTarget;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.component.JPanelInfo;
import org.eclipse.wb.internal.swing.model.layout.BorderLayoutInfo;
import org.eclipse.wb.internal.swing.model.layout.FlowLayoutInfo;
import org.eclipse.wb.tests.designer.swing.SwingTestUtils;

import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.lang.StringUtils;

/**
 * Test for {@link LazyVariableSupport}.
 * 
 * @author scheglov_ke
 */
public class LazyTest extends AbstractVariableTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Exit zone :-) XXX
  //
  ////////////////////////////////////////////////////////////////////////////
  public void _test_exit() throws Exception {
    System.exit(0);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Basic tests for {@link LazyVariableSupport}.
   */
  public void test_basic() throws Exception {
    ContainerInfo panel = parseLazyCode();
    // prepare button
    assertEquals(1, panel.getChildrenComponents().size());
    ComponentInfo button = panel.getChildrenComponents().get(0);
    LazyVariableSupport variableSupport = (LazyVariableSupport) button.getVariableSupport();
    // toString()
    assertEquals("lazy: button getButton()", variableSupport.toString());
    // check conversion
    assertFalse(variableSupport.canConvertLocalToField());
    assertFalse(variableSupport.canConvertFieldToLocal());
    try {
      variableSupport.convertLocalToField();
      fail();
    } catch (IllegalStateException e) {
    }
    try {
      variableSupport.convertFieldToLocal();
      fail();
    } catch (IllegalStateException e) {
    }
  }

  public void test_getReferenceExpression() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    add(getButton());",
            "  }",
            "  private JButton button;",
            "  private JButton getButton() {",
            "    if (button == null) {",
            "      button = new JButton();",
            "    }",
            "    return button;",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    LazyVariableSupport variableSupport = (LazyVariableSupport) button.getVariableSupport();
    // in "getButton()" = "local" expression
    {
      NodeTarget target = getNodeStatementTarget(panel, "getButton()", false, 0, 0);
      assertTrue(variableSupport.hasExpression(target));
      assertEquals("button", variableSupport.getReferenceExpression(target));
      assertEquals("button.", variableSupport.getAccessExpression(target));
    }
    // in Test() constructor = "remote" expression
    {
      NodeTarget target = getNodeBlockTarget(panel, true);
      assertTrue(variableSupport.hasExpression(target));
      assertEquals("getButton()", variableSupport.getReferenceExpression(target));
      assertEquals("getButton().", variableSupport.getAccessExpression(target));
    }
    // begin of TypeDeclaration = "remote" expression
    {
      TypeDeclaration targetType = getTypeDeclaration(panel);
      NodeTarget target = new NodeTarget(new BodyDeclarationTarget(targetType, true));
      assertTrue(variableSupport.hasExpression(target));
      assertEquals("getButton()", variableSupport.getReferenceExpression(target));
      assertEquals("getButton().", variableSupport.getAccessExpression(target));
    }
  }

  /**
   * Test name in {@link LazyVariableSupport}.
   */
  public void test_setName() throws Exception {
    ContainerInfo panel = parseLazyCode();
    // prepare button
    assertEquals(1, panel.getChildrenComponents().size());
    ComponentInfo button = panel.getChildrenComponents().get(0);
    LazyVariableSupport variableSupport = (LazyVariableSupport) button.getVariableSupport();
    // check initial name
    assertEquals(true, variableSupport.hasName());
    assertEquals("button", variableSupport.getName());
    // change name
    {
      String expectedSource = m_lastEditor.getSource();
      expectedSource = StringUtils.replace(expectedSource, "button", "addButton");
      expectedSource = StringUtils.replace(expectedSource, "getButton()", "getAddButton()");
      variableSupport.setName("addButton");
      assertEditor(expectedSource, m_lastEditor);
    }
    // change name second time
    {
      String expectedSource = m_lastEditor.getSource();
      expectedSource = StringUtils.replace(expectedSource, "addButton", "renameButton");
      expectedSource = StringUtils.replace(expectedSource, "getAddButton()", "getRenameButton()");
      variableSupport.setName("renameButton");
      assertEditor(expectedSource, m_lastEditor);
    }
    //
    assert_creation(panel);
  }

  public void test_delete() throws Exception {
    ContainerInfo panel = parseLazyCode();
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // do delete
    assertTrue(button.canDelete());
    button.delete();
    assertEditor(
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "  }",
        "}");
  }

  /**
   * Parses code sample for above tests.
   */
  private ContainerInfo parseLazyCode() throws Exception {
    return parseContainer(
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    add(getButton());",
        "    getButton().setEnabled(false);",
        "  }",
        "  private JButton button;",
        "  private JButton getButton() {",
        "    if (button == null) {",
        "      button = new JButton();",
        "      button.setText('Lazy JButton');",
        "    }",
        "    return button;",
        "  }",
        "}");
  }

  public void test_parse_EmptyVariable_inLazy() throws Exception {
    parseContainer(
        "public class Test extends JPanel {",
        "  private JPanel inner;",
        "  public Test() {",
        "    add(getInner());",
        "  }",
        "  private JPanel getInner() {",
        "    if (inner == null) {",
        "      inner = new JPanel();",
        "      inner.setLayout(new BorderLayout());",
        "    }",
        "    return inner;",
        "  }",
        "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(getInner())/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: javax.swing.JPanel} {lazy: inner getInner()} {/new JPanel()/ /inner.setLayout(new BorderLayout())/ /inner/ /add(getInner())/}",
        "    {new: java.awt.BorderLayout} {empty} {/inner.setLayout(new BorderLayout())/}");
  }

  public void test_parse_withAbstractMethod() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public abstract class Test extends JPanel {",
            "  public Test() {",
            "    someAbstractMethod();",
            "  }",
            "  protected abstract void someAbstractMethod();",
            "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}");
    //
    panel.refresh();
    assertNoErrors(panel);
  }

  /**
   * Component creation in "accessor" method may be not first in "then" statement.
   * <p>
   * 40243: Parsing failure with VA Java created
   */
  public void test_parse_notFirstCreate() throws Exception {
    parseContainer(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    add(getButton());",
        "  }",
        "  private JButton button;",
        "  private JButton getButton() {",
        "    if (button == null) {",
        "      int otherStatement;",
        "      button = new JButton();",
        "    }",
        "    return button;",
        "  }",
        "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(getButton())/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: javax.swing.JButton} {lazy: button getButton()} {/new JButton()/ /button/ /add(getButton())/}");
  }

  public void test_parse_thisQualifier() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    add(getButton());",
            "  }",
            "  private JButton button;",
            "  private JButton getButton() {",
            "    if (this.button == null) {",
            "      this.button = new JButton();",
            "    }",
            "    return this.button;",
            "  }",
            "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/this/ /add(getButton())/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: javax.swing.JButton} {lazy: this.button getButton()} {/new JButton()/ /this.button/ /add(getButton())/}");
    //
    panel.refresh();
    assertNoErrors(panel);
  }

  /**
   * There was problem with "return;" statement, without expression.
   */
  public void test_parse_methodWithEmptyReturn() throws Exception {
    m_ignoreCompilationProblems = true;
    parseContainer(
        "import java.util.*;",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    foo();",
        "  }",
        "  private JButton button;",
        "  private void foo() {",
        "    if (button == null) {",
        "    }",
        "    return;",
        "  }",
        "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}");
  }

  /**
   * If "return field;" is bad, so no {@link IVariableBinding} this caused
   * {@link NullPointerException}.
   */
  public void test_parse_badReturnName() throws Exception {
    m_ignoreCompilationProblems = true;
    parseContainer(
        "import java.util.*;",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    add(getButton());",
        "  }",
        "  private JButton button;",
        "  private JButton getButton() {",
        "    if (button == null) {",
        "      button = new JButton();",
        "    }",
        "    return noSuchName;",
        "  }",
        "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(getButton())/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ADD
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for adding new component, with "private" method modifier.
   */
  public void test_ADD_private() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new BorderLayout());",
            "  }",
            "}");
    BorderLayoutInfo borderLayout = (BorderLayoutInfo) panel.getLayout();
    //
    ComponentInfo newComponent = createJButton();
    // add component
    SwingTestUtils.setGenerations(
        LazyVariableDescription.INSTANCE,
        LazyStatementGeneratorDescription.INSTANCE);
    SwingTestUtils.setLazyMethodModifier(LazyVariableSupport.V_METHOD_MODIFIER_PRIVATE);
    try {
      borderLayout.command_CREATE(newComponent, java.awt.BorderLayout.NORTH);
    } finally {
      SwingTestUtils.setGenerationDefaults();
    }
    // check
    assertEditor(
        "class Test extends JPanel {",
        "  private JButton button;",
        "  public Test() {",
        "    setLayout(new BorderLayout());",
        "    add(getButton(), BorderLayout.NORTH);",
        "  }",
        "  private JButton getButton() {",
        "    if (button == null) {",
        "      button = new JButton();",
        "    }",
        "    return button;",
        "  }",
        "}");
    // delete
    newComponent.delete();
    assertEditor(
        "class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new BorderLayout());",
        "  }",
        "}");
  }

  /**
   * Test for adding new component, with "public" method modifier.
   */
  public void test_ADD_public() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new BorderLayout());",
            "  }",
            "}");
    BorderLayoutInfo borderLayout = (BorderLayoutInfo) panel.getLayout();
    //
    ComponentInfo newComponent = createJButton();
    // add component
    SwingTestUtils.setGenerations(
        LazyVariableDescription.INSTANCE,
        LazyStatementGeneratorDescription.INSTANCE);
    SwingTestUtils.setLazyMethodModifier(LazyVariableSupport.V_METHOD_MODIFIER_PUBLIC);
    try {
      borderLayout.command_CREATE(newComponent, java.awt.BorderLayout.NORTH);
    } finally {
      SwingTestUtils.setGenerationDefaults();
    }
    // check
    assertEditor(
        "class Test extends JPanel {",
        "  private JButton button;",
        "  public Test() {",
        "    setLayout(new BorderLayout());",
        "    add(getButton(), BorderLayout.NORTH);",
        "  }",
        "  public JButton getButton() {",
        "    if (button == null) {",
        "      button = new JButton();",
        "    }",
        "    return button;",
        "  }",
        "}");
  }

  /**
   * Test for adding new component, with "private static" method modifier.
   */
  public void test_ADD_static() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test {",
            "  public static void main(String args[]) {",
            "    JPanel panel = new JPanel();",
            "    panel.setLayout(new BorderLayout());",
            "  }",
            "}");
    BorderLayoutInfo borderLayout = (BorderLayoutInfo) panel.getLayout();
    //
    ComponentInfo newComponent = createJButton();
    // add component
    SwingTestUtils.setGenerations(
        LazyVariableDescription.INSTANCE,
        LazyStatementGeneratorDescription.INSTANCE);
    try {
      borderLayout.command_CREATE(newComponent, java.awt.BorderLayout.NORTH);
    } finally {
      SwingTestUtils.setGenerationDefaults();
    }
    // check
    assertEditor(
        "class Test {",
        "  private static JButton button;",
        "  public static void main(String args[]) {",
        "    JPanel panel = new JPanel();",
        "    panel.setLayout(new BorderLayout());",
        "    panel.add(getButton(), BorderLayout.NORTH);",
        "  }",
        "  private static JButton getButton() {",
        "    if (button == null) {",
        "      button = new JButton();",
        "    }",
        "    return button;",
        "  }",
        "}");
  }

  /**
   * We should use canonical {@link Class} name.
   */
  public void test_ADD_innerClass() throws Exception {
    setFileContentSrc(
        "test/Foo.java",
        getTestSource(
            "// filler filler filler",
            "public class Foo {",
            "  public static class MyPanel extends JPanel {",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    FlowLayoutInfo layout = (FlowLayoutInfo) panel.getLayout();
    //
    ComponentInfo newComponent = createComponent("test.Foo$MyPanel");
    // add component
    SwingTestUtils.setGenerations(
        LazyVariableDescription.INSTANCE,
        LazyStatementGeneratorDescription.INSTANCE);
    try {
      layout.add(newComponent, null);
    } finally {
      SwingTestUtils.setGenerationDefaults();
    }
    assertEditor(
        "import test.Foo.MyPanel;",
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  private MyPanel myPanel;",
        "  public Test() {",
        "    add(getMyPanel());",
        "  }",
        "  private MyPanel getMyPanel() {",
        "    if (myPanel == null) {",
        "      myPanel = new MyPanel();",
        "    }",
        "    return myPanel;",
        "  }",
        "}");
  }

  /**
   * Test for generating unique method name, when such method exists in superclass.
   */
  public void test_ADD_conflictWithSuperClass() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  public int getButton() {",
            "    return 0;",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends MyPanel {",
            "  public Test() {",
            "  }",
            "}");
    FlowLayoutInfo layout = (FlowLayoutInfo) panel.getLayout();
    //
    ComponentInfo newComponent = createJButton();
    // add component
    SwingTestUtils.setGenerations(
        LazyVariableDescription.INSTANCE,
        LazyStatementGeneratorDescription.INSTANCE);
    try {
      layout.add(newComponent, null);
    } finally {
      SwingTestUtils.setGenerationDefaults();
    }
    // check
    assertEditor(
        "// filler filler filler",
        "public class Test extends MyPanel {",
        "  private JButton button;",
        "  public Test() {",
        "    add(getButton_1());",
        "  }",
        "  private JButton getButton_1() {",
        "    if (button == null) {",
        "      button = new JButton();",
        "    }",
        "    return button;",
        "  }",
        "}");
  }

  /**
   * Support for generic components and type arguments.
   */
  public void test_ADD_typeArguments() throws Exception {
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyButton<K, V> extends JButton {",
            "}"));
    setFileContentSrc(
        "test/MyButton.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <creation>",
            "    <source><![CDATA[new test.MyButton<%keyType%, %valueType%>()]]></source>",
            "  </creation>",
            "</component>"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    // add new MyButton
    SwingTestUtils.setGenerations(
        LazyVariableDescription.INSTANCE,
        LazyStatementGeneratorDescription.INSTANCE);
    {
      ComponentInfo newButton = createJavaInfo("test.MyButton");
      newButton.putTemplateArgument("keyType", "java.lang.String");
      newButton.putTemplateArgument("valueType", "java.util.List<java.lang.Double>");
      ((FlowLayoutInfo) panel.getLayout()).add(newButton, null);
    }
    assertEditor(
        "import java.util.List;",
        "// filler filler filler filler filler",
        "public class Test extends JPanel {",
        "  private MyButton<String, List<Double>> myButton;",
        "  public Test() {",
        "    add(getMyButton());",
        "  }",
        "  private MyButton<String, List<Double>> getMyButton() {",
        "    if (myButton == null) {",
        "      myButton = new MyButton<String, List<Double>>();",
        "    }",
        "    return myButton;",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Target
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link LazyVariableSupport#ensureInstanceReadyAt(StatementTarget)}.
   */
  public void test_moveTarget() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    add(getButton());",
            "    int a;",
            "  }",
            "  private JButton button;",
            "  private JButton getButton() {",
            "    if (button == null) {",
            "      button = new JButton();",
            "      button.setText('Lazy JButton');",
            "    }",
            "    return button;",
            "  }",
            "}");
    panel.refresh();
    ComponentInfo button = panel.getChildrenComponents().get(0);
    LazyVariableSupport buttonVariable = (LazyVariableSupport) button.getVariableSupport();
    // prepare "request" component target
    Statement requestStatement = getStatement(panel, 1);
    StatementTarget requestTarget = new StatementTarget(requestStatement, false);
    // ensureInstanceReadyAt(), no changes expected
    {
      String expectedSource = m_lastEditor.getSource();
      buttonVariable.ensureInstanceReadyAt(requestTarget);
      assertEditor(expectedSource, m_lastEditor);
    }
    // getAssociationTarget()
    {
      StatementTarget associationTarget = buttonVariable.getAssociationTarget(requestTarget);
      assertTarget(associationTarget, null, requestStatement, false);
    }
  }

  /**
   * Test for {@link LazyVariableSupport#isValidStatementForChild(Statement)}.
   */
  public void test_isValidStatementForChild() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    add(getButton());",
            "  }",
            "  private JButton button;",
            "  private JButton getButton() {",
            "    if (button == null) {",
            "      button = new JButton();",
            "      button.setText('Lazy JButton');",
            "    }",
            "    return button;",
            "  }",
            "}");
    panel.refresh();
    ComponentInfo button = panel.getChildrenComponents().get(0);
    LazyVariableSupport buttonVariable = (LazyVariableSupport) button.getVariableSupport();
    //
    Statement statement_1 = getStatement(panel, 0);
    Statement statement_2 = getStatement(panel, "getButton()", 0, 1);
    assertFalse(buttonVariable.isValidStatementForChild(statement_1));
    assertTrue(buttonVariable.isValidStatementForChild(statement_2));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // setType()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_setType() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    add(getButton());",
            "    getButton().setEnabled(false);",
            "  }",
            "  private JButton button;",
            "  private JButton getButton() {",
            "    if (button == null) {",
            "      button = new JButton();",
            "      button.setText('Lazy JButton');",
            "    }",
            "    return button;",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // check
    LazyVariableSupport variable = (LazyVariableSupport) button.getVariableSupport();
    variable.setType("javax.swing.JTextField");
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    add(getButton());",
        "    getButton().setEnabled(false);",
        "  }",
        "  private JTextField button;",
        "  private JTextField getButton() {",
        "    if (button == null) {",
        "      button = new JButton();",
        "      button.setText(\"Lazy JButton\");",
        "    }",
        "    return button;",
        "  }",
        "}");
    assertInstanceOf(LazyVariableSupport.class, button.getVariableSupport());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Special cases
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_tryInIfStatement() throws Exception {
    String[] lines =
        {
            "public class Test extends JPanel {",
            "  public Test() {",
            "    add(getButton());",
            "  }",
            "  private JButton button;",
            "  private JButton getButton() {",
            "    if (button == null) {",
            "      try {",
            "        button = new JButton();",
            "      } catch (Throwable e) {",
            "      }",
            "    }",
            "    return button;",
            "  }",
            "}"};
    parseContainer(lines);
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(getButton())/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: javax.swing.JButton} {lazy: button getButton()} {/new JButton()/ /button/ /add(getButton())/}");
  }

  /**
   * Sometimes we need to include "accessor" method into execution flow start methods. For example
   * to support not attached Swing actions. But when we delete "accessor", we should not return it
   * as start method.
   */
  public void test_deleteAccessorFromStartMethods() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    add(getButton());",
            "  }",
            "  private JButton button;",
            "  private JButton getButton() {",
            "    if (button == null) {",
            "      button = new JButton();",
            "    }",
            "    return button;",
            "  }",
            "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(getButton())/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: javax.swing.JButton} {lazy: button getButton()} {/new JButton()/ /button/ /add(getButton())/}");
    ExecutionFlowDescription flowDescription = m_lastState.getFlowDescription();
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // prepare accessor
    MethodDeclaration accessor;
    {
      LazyVariableSupport variable = (LazyVariableSupport) button.getVariableSupport();
      accessor = variable.m_accessor;
    }
    // initially no accessor
    assertThat(flowDescription.getStartMethods()).doesNotContain(accessor);
    // add "accessor" into execution flow
    flowDescription.addStartMethod(accessor);
    assertThat(flowDescription.getStartMethods()).contains(accessor);
    // delete "button"
    button.delete();
    assertThat(flowDescription.getStartMethods()).doesNotContain(accessor);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implicit factory
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_implicitFactory_0() throws Exception {
    prepare_implicitFactory();
    String[] lines =
        {
            "class Test extends JPanel {",
            "  private MyBar bar;",
            "  Test() {",
            "    add(getBar());",
            "  }",
            "  private MyBar getBar() {",
            "    if (bar == null) {",
            "      bar = new MyBar();",
            "    }",
            "    return bar;",
            "  }",
            "}"};
    ContainerInfo panel = parseContainer(lines);
    ContainerInfo bar = (ContainerInfo) panel.getChildrenComponents().get(0);
    // prepare CreationSupport
    ImplicitFactoryCreationSupport creationSupport;
    {
      String signature = "addButton()";
      String invocationSource = "addButton()";
      creationSupport = new ImplicitFactoryCreationSupport(signature, invocationSource);
    }
    // add "implicit" JButton
    ComponentInfo newButton =
        (ComponentInfo) JavaInfoUtils.createJavaInfo(
            m_lastEditor,
            "javax.swing.JButton",
            creationSupport);
    SwingTestUtils.setGenerations(
        LazyVariableDescription.INSTANCE,
        LazyStatementGeneratorDescription.INSTANCE);
    ((FlowLayoutInfo) bar.getLayout()).add(newButton, null);
    assertEditor(
        "class Test extends JPanel {",
        "  private MyBar bar;",
        "  private JButton button;",
        "  Test() {",
        "    add(getBar());",
        "  }",
        "  private MyBar getBar() {",
        "    if (bar == null) {",
        "      bar = new MyBar();",
        "      getButton();",
        "    }",
        "    return bar;",
        "  }",
        "  private JButton getButton() {",
        "    if (button == null) {",
        "      button = getBar().addButton();",
        "    }",
        "    return button;",
        "  }",
        "}");
  }

  private void prepare_implicitFactory() throws Exception {
    setFileContentSrc(
        "test/MyBar.java",
        getTestSource(
            "public class MyBar extends JPanel {",
            "  public JButton addButton() {",
            "    JButton button = new JButton();",
            "    add(button);",
            "    return button;",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyBar.wbp-component.xml",
        getSource(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <methods>",
            "    <method name='addButton'>",
            "      <tag name='implicitFactory' value='true'/>",
            "    </method>",
            "  </methods>",
            "</component>"));
    waitForAutoBuild();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Conversion tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_convert() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  public Test() {",
            "    BorderLayout borderLayout = new BorderLayout();",
            "    borderLayout.setVgap(5);",
            "    setLayout(borderLayout);",
            "  }",
            "}");
    BorderLayoutInfo borderLayout = (BorderLayoutInfo) panel.getLayout();
    //
    assertThat(LazyVariableSupportUtils.canConvert(borderLayout)).isTrue();
    LazyVariableSupportUtils.convert(borderLayout);
    // check
    assertEditor(
        "class Test extends JPanel {",
        "  private BorderLayout borderLayout;",
        "  public Test() {",
        "    setLayout(getBorderLayout());",
        "  }",
        "  private BorderLayout getBorderLayout() {",
        "    if (borderLayout == null) {",
        "      borderLayout = new BorderLayout();",
        "      borderLayout.setVgap(5);",
        "    }",
        "    return borderLayout;",
        "  }",
        "}");
    // delete
    borderLayout.delete();
    assertEditor( // filler
        "class Test extends JPanel {",
        "  public Test() {",
        "  }",
        "}");
  }

  public void test_convert_children() throws Exception {
    ContainerInfo testPanel =
        parseContainer(
            "class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(null);",
            "    JPanel panel = new JPanel();",
            "    panel.setBounds(42, 40, 270, 160);",
            "    add(panel);",
            "    JTextField textField = new JTextField();",
            "    panel.add(textField);",
            "    textField.setColumns(10);",
            "    JButton button = new JButton('New button');",
            "    panel.add(button);",
            "  }",
            "}");
    JPanelInfo panel = testPanel.getChildren(JPanelInfo.class).get(0);
    assertThat(panel.getChildren(ComponentInfo.class).size()).isEqualTo(2);
    //
    assertThat(LazyVariableSupportUtils.canConvert(panel)).isTrue();
    LazyVariableSupportUtils.convert(panel);
    // check
    assertEditor(
        "class Test extends JPanel {",
        "  private JPanel panel;",
        "  public Test() {",
        "    setLayout(null);",
        "    add(getPanel());",
        "  }",
        "  private JPanel getPanel() {",
        "    if (panel == null) {",
        "      panel = new JPanel();",
        "      panel.setBounds(42, 40, 270, 160);",
        "      JTextField textField = new JTextField();",
        "      panel.add(textField);",
        "      textField.setColumns(10);",
        "      JButton button = new JButton('New button');",
        "      panel.add(button);",
        "    }",
        "    return panel;",
        "  }",
        "}");
  }
}
