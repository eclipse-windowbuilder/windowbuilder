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
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.association.AssociationObjects;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.factory.InstanceFactoryInfo;
import org.eclipse.wb.internal.core.model.generation.statement.block.BlockStatementGenerator;
import org.eclipse.wb.internal.core.model.generation.statement.block.BlockStatementGeneratorDescription;
import org.eclipse.wb.internal.core.model.nonvisual.NonVisualBeanContainerInfo;
import org.eclipse.wb.internal.core.model.variable.FieldInitializerVariableSupport;
import org.eclipse.wb.internal.core.model.variable.FieldVariableSupport;
import org.eclipse.wb.internal.core.model.variable.description.FieldInitializerVariableDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.check.AssertionFailedException;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.FlowLayoutInfo;
import org.eclipse.wb.tests.designer.swing.SwingTestUtils;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.VariableDeclaration;

import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * Test for {@link FieldInitializerVariableSupport}.
 * 
 * @author scheglov_ke
 */
public class FieldInitializerTest extends AbstractVariableTest {
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
  public void test_object() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private JButton button = new JButton();",
            "  public Test() {",
            "    button.setText('text');",
            "    add(button);",
            "  }",
            "}");
    AstEditor editor = m_lastEditor;
    assertEquals(1, panel.getChildrenComponents().size());
    // check child: 0
    {
      ComponentInfo button = panel.getChildrenComponents().get(0);
      FieldInitializerVariableSupport variableSupport =
          (FieldInitializerVariableSupport) button.getVariableSupport();
      assertEquals("field-initializer: button", variableSupport.toString());
      assertEquals("button", variableSupport.getName());
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
      //
      {
        String expected = StringUtils.replace(editor.getSource(), "button", "abc");
        variableSupport.setName("abc");
        assertEquals(expected, editor.getSource());
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getReferenceExpression()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getReferenceExpression() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private JButton button = new JButton();",
            "  public Test() {",
            "    add(button);",
            "  }",
            "}");
    String expectedSource = m_lastEditor.getSource();
    ComponentInfo button = panel.getChildrenComponents().get(0);
    FieldInitializerVariableSupport variableSupport =
        (FieldInitializerVariableSupport) button.getVariableSupport();
    // in Test()
    {
      NodeTarget target = getNodeStatementTarget(panel, true);
      assertTrue(variableSupport.hasExpression(target));
      assertEquals("button", variableSupport.getReferenceExpression(target));
      assertEditor(expectedSource, m_lastEditor);
    }
    // after "button"
    {
      NodeTarget target = getNodeBodyDeclarationTarget(button, false, 0);
      assertTrue(variableSupport.hasExpression(target));
      assertEquals("button", variableSupport.getReferenceExpression(target));
      assertEditor(expectedSource, m_lastEditor);
    }
    // after "Test()"
    {
      NodeTarget target = getNodeBodyDeclarationTarget(button, false, 1);
      assertTrue(variableSupport.hasExpression(target));
      assertEquals("button", variableSupport.getReferenceExpression(target));
      assertEditor(expectedSource, m_lastEditor);
    }
    // before "button"
    {
      NodeTarget target = getNodeBodyDeclarationTarget(button, true, 0);
      assertFalse(variableSupport.hasExpression(target));
      try {
        variableSupport.getReferenceExpression(target);
        fail();
      } catch (AssertionFailedException e) {
      }
    }
    // before "Test()"
    {
      NodeTarget target = getNodeBodyDeclarationTarget(button, true, 1);
      assertTrue(variableSupport.hasExpression(target));
      assertEquals("button", variableSupport.getReferenceExpression(target));
      assertEditor(expectedSource, m_lastEditor);
    }
    // begin of "Test"
    {
      NodeTarget target = getNodeTypeDeclarationTarget(button, true);
      assertFalse(variableSupport.hasExpression(target));
    }
    // end of "Test"
    {
      NodeTarget target = getNodeTypeDeclarationTarget(button, false);
      assertTrue(variableSupport.hasExpression(target));
      assertEquals("button", variableSupport.getReferenceExpression(target));
      assertEditor(expectedSource, m_lastEditor);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Target
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * No related statements.<br>
   * Target: beginning of root method (main).
   */
  public void test_getTarget_1() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test {",
            "  static JPanel panel = new JPanel();",
            "  public static void main(String args[]) {",
            "    int value;",
            "  }",
            "}");
    // check target
    Block expectedBlock = getBlock(panel, "main(java.lang.String[])");
    assertStatementTarget(panel, expectedBlock, null, true);
  }

  /**
   * No related statements, has child.<br>
   * Target: before first related statement.
   */
  public void test_getTarget_3() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test {",
            "  static JPanel panel = new JPanel();",
            "  public static void main(String args[]) {",
            "    int value;",
            "    panel.add(new JButton());",
            "  }",
            "}");
    // check target
    Statement expectedStatement = getStatement(panel, "main(java.lang.String[])", 1);
    assertStatementTarget(panel, null, expectedStatement, true);
  }

  /**
   * No related statements, invocation-kind, but has association.<br>
   * Target: before association.
   */
  public void test_getTarget_4() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  static JButton button = new JButton();",
            "  public Test() {",
            "    super();",
            "    int value;",
            "    add(button);",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // check target
    Statement expectedStatement = getStatement(panel, 2);
    assertStatementTarget(button, null, expectedStatement, true);
  }

  /**
   * Has related statements with {@link MethodInvocation}.<br>
   * Target: before first related statement.
   */
  public void test_getTarget_5() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test {",
            "  static JPanel panel = new JPanel();",
            "  public static void main(String args[]) {",
            "    int value;",
            "    panel.setVisible(true);",
            "    panel.setEnabled(true);",
            "  }",
            "}");
    // check target
    Statement expectedStatement = getStatement(panel, "main(java.lang.String[])", 1);
    assertStatementTarget(panel, null, expectedStatement, true);
  }

  /**
   * Has related statement with {@link FieldAccess}.<br>
   * Target: before first related statement.
   */
  public void test_getTarget_6() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyPanel extends JPanel {",
            "  public int m_value;",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test {",
            "  static MyPanel panel = new MyPanel();",
            "  public static void main(String args[]) {",
            "    int value;",
            "    panel.m_value = 1;",
            "  }",
            "}");
    // check target
    Statement expectedStatement = getStatement(panel, "main(java.lang.String[])", 1);
    assertStatementTarget(panel, null, expectedStatement, true);
  }

  /**
   * Has related statements, but not invocation of <b>its</b> method.<br>
   * Target: beginning of root method (main).
   */
  public void test_getTarget_7() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test {",
            "  static JPanel panel = new JPanel();",
            "  public static void main(String args[]) {",
            "    int value;",
            "    foo(panel);",
            "  }",
            "  private static void foo(Component component) {",
            "  }",
            "}");
    // check target
    Block expectedBlock = getBlock(panel, "main(java.lang.String[])");
    assertStatementTarget(panel, expectedBlock, null, true);
  }

  /**
   * No related statements.<br>
   * Special case: "application" pattern, i.e. first root method is static "main".<br>
   * Target: beginning of first <em>non-static</em> method on execution flow.
   */
  public void test_getTarget_8() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test {",
            "  private JPanel panel = new JPanel();",
            "  public static void main(String args[]) {",
            "    Test application = new Test();",
            "  }",
            "  public Test() {",
            "    int constructorMarker;",
            "  }",
            "}");
    // only "main()" is root method
    {
      ExecutionFlowDescription flowDescription = m_lastState.getFlowDescription();
      List<MethodDeclaration> startMethods = flowDescription.getStartMethods();
      assertEquals(1, startMethods.size());
      assertEquals("main(java.lang.String[])", AstNodeUtils.getMethodSignature(startMethods.get(0)));
    }
    // but "target" should be "Test()"
    Block expectedBlock = getBlock(panel, "<init>()");
    assertStatementTarget(panel, expectedBlock, null, true);
  }

  /**
   * No related statements.<br>
   * Special case: "application" pattern, i.e. first root method is static "main".<br>
   * Note, that root {@link JavaInfo} is created in "createContents()", so target is also in this
   * method.<br>
   * Target: beginning of first <em>non-static</em> method on execution flow.
   */
  public void test_getTarget_9() throws Exception {
    setFileContentSrc(
        "test/InstanceFactory.java",
        getTestSource(
            "public class InstanceFactory {",
            "  public JButton createButton() {",
            "    return new JButton();",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test {",
            "  private final InstanceFactory m_factory = new InstanceFactory();",
            "  private JPanel panel;",
            "  public static void main(String args[]) {",
            "    Test application = new Test();",
            "  }",
            "  public Test() {",
            "    createContents();",
            "  }",
            "  public void createContents() {",
            "    panel = new JPanel();",
            "  }",
            "}");
    assertEquals("panel", panel.getVariableSupport().getName());
    InstanceFactoryInfo factory =
        InstanceFactoryInfo.getFactories(panel, m_lastLoader.loadClass("test.InstanceFactory")).get(
            0);
    // only "main()" is root method
    {
      ExecutionFlowDescription flowDescription = m_lastState.getFlowDescription();
      List<MethodDeclaration> startMethods = flowDescription.getStartMethods();
      assertEquals(1, startMethods.size());
      assertEquals("main(java.lang.String[])", AstNodeUtils.getMethodSignature(startMethods.get(0)));
    }
    // but "target" should be "createContents()"
    Block expectedBlock = getBlock(panel, "createContents()");
    assertStatementTarget(factory, expectedBlock, null, true);
  }

  /**
   * Non-visual object is special, because it has no {@link Statement} for associaion.<br>
   * Target: after {@link SuperConstructorInvocation}.
   */
  public void test_getTarget_forNonVisualBean_whenSuper() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  /**",
            "  * @wbp.nonvisual location=150,400",
            "  */",
            "  private final Object object = new Object();",
            "  public Test() {",
            "    super();",
            "  }",
            "}");
    NonVisualBeanContainerInfo nvoContainer = NonVisualBeanContainerInfo.find(panel);
    JavaInfo object = nvoContainer.getChildren(JavaInfo.class).get(0);
    // check target
    Statement expectedStatement = getStatement(panel, 0);
    assertStatementTarget(object, null, expectedStatement, false);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ADD
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test adding new component, with "private" method modifier.
   */
  public void test_ADD_private() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    FlowLayoutInfo flowLayout = (FlowLayoutInfo) panel.getLayout();
    //
    ComponentInfo newComponent = createJButton();
    // add component
    SwingTestUtils.setGenerations(
        FieldInitializerVariableDescription.INSTANCE,
        BlockStatementGeneratorDescription.INSTANCE);
    SwingTestUtils.setFieldInitializerModifier(FieldVariableSupport.V_FIELD_MODIFIER_PRIVATE);
    try {
      flowLayout.add(newComponent, null);
    } finally {
      SwingTestUtils.setGenerationDefaults();
    }
    // check
    assertEditor(
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  private final JButton button = new JButton();",
        "  public Test() {",
        "    {",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test adding new component, with "package private" method modifier.
   */
  public void test_ADD_package() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    FlowLayoutInfo flowLayout = (FlowLayoutInfo) panel.getLayout();
    //
    ComponentInfo newComponent = createJButton();
    // add component
    SwingTestUtils.setGenerations(
        FieldInitializerVariableDescription.INSTANCE,
        BlockStatementGeneratorDescription.INSTANCE);
    SwingTestUtils.setFieldInitializerModifier(FieldVariableSupport.V_FIELD_MODIFIER_PACKAGE);
    try {
      flowLayout.add(newComponent, null);
    } finally {
      SwingTestUtils.setGenerationDefaults();
    }
    // check
    assertEditor(
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  final JButton button = new JButton();",
        "  public Test() {",
        "    {",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test adding new component, static context.
   */
  public void test_ADD_static() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test {",
            "  public static void main(String args[]) {",
            "    JPanel panel = new JPanel();",
            "  }",
            "}");
    FlowLayoutInfo flowLayout = (FlowLayoutInfo) panel.getLayout();
    //
    ComponentInfo newComponent = createJButton();
    // add component
    SwingTestUtils.setGenerations(
        FieldInitializerVariableDescription.INSTANCE,
        BlockStatementGeneratorDescription.INSTANCE);
    try {
      flowLayout.add(newComponent, null);
    } finally {
      SwingTestUtils.setGenerationDefaults();
    }
    // check
    assertEditor(
        "public class Test {",
        "  private static final JButton button = new JButton();",
        "  public static void main(String args[]) {",
        "    JPanel panel = new JPanel();",
        "    {",
        "      panel.add(button);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Force <code>static</code> modifier.
   */
  public void test_ADD_forceStatic() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    ComponentInfo newComponent = createJButton();
    // add component
    FieldInitializerVariableSupport variableSupport =
        new FieldInitializerVariableSupport(newComponent);
    variableSupport.setForceStaticModifier(true);
    JavaInfoUtils.add(
        newComponent,
        variableSupport,
        BlockStatementGenerator.INSTANCE,
        AssociationObjects.invocationChild("%parent%.add(%child%)", false),
        panel,
        null);
    // check
    assertEditor(
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  private static final JButton button = new JButton();",
        "  public Test() {",
        "    {",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test adding new component, with "this." prefix.
   */
  public void test_ADD_thisPrefix() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    FlowLayoutInfo flowLayout = (FlowLayoutInfo) panel.getLayout();
    //
    ComponentInfo newComponent = createJButton();
    // add component
    SwingTestUtils.setGenerations(
        FieldInitializerVariableDescription.INSTANCE,
        BlockStatementGeneratorDescription.INSTANCE);
    panel.getDescription().getToolkit().getPreferences().setValue(
        FieldInitializerVariableSupport.P_PREFIX_THIS,
        true);
    try {
      flowLayout.add(newComponent, null);
      assertEditor(
          "// filler filler filler",
          "public class Test extends JPanel {",
          "  private final JButton button = new JButton();",
          "  public Test() {",
          "    {",
          "      add(this.button);",
          "    }",
          "  }",
          "}");
      // check variable
      {
        FieldInitializerVariableSupport variableSupport =
            (FieldInitializerVariableSupport) newComponent.getVariableSupport();
        NodeTarget target = getNodeStatementTarget(panel, false, 0, 0);
        assertTrue(variableSupport.hasExpression(target));
        assertEquals("this.button", variableSupport.getReferenceExpression(target));
        assertEquals("this.button.", variableSupport.getAccessExpression(target));
      }
    } finally {
      SwingTestUtils.setGenerationDefaults();
    }
  }

  /**
   * Support for "%variable-name%" in creation source.
   */
  public void test_ADD_variableName_inCreationSource() throws Exception {
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "// filler filler filler filler filler",
            "public class MyButton extends JButton {",
            "  public MyButton(String text) {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyButton.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <creation>",
            "    <source><![CDATA[new test.MyButton('%variable-name%')]]></source>",
            "  </creation>",
            "</component>"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    // add new MyButton
    ComponentInfo button = createJavaInfo("test.MyButton");
    SwingTestUtils.setGenerations(
        FieldInitializerVariableDescription.INSTANCE,
        BlockStatementGeneratorDescription.INSTANCE);
    ((FlowLayoutInfo) panel.getLayout()).add(button, null);
    assertEditor(
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  private final MyButton myButton = new MyButton('myButton');",
        "  public Test() {",
        "    {",
        "      add(myButton);",
        "    }",
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
        FieldInitializerVariableDescription.INSTANCE,
        BlockStatementGeneratorDescription.INSTANCE);
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
        "  private final MyButton<String, List<Double>> myButton = new MyButton<String, List<Double>>();",
        "  public Test() {",
        "    {",
        "      add(myButton);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Delete
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * {@link FieldDeclaration} should be removed.
   */
  public void test_delete_1() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class  Test extends JPanel {",
            "  private JButton button = new JButton();",
            "  Test() {",
            "    add(button);",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    //
    assertTrue(button.canDelete());
    button.delete();
    assertEditor(
        "// filler filler filler",
        "public class  Test extends JPanel {",
        "  Test() {",
        "  }",
        "}");
  }

  /**
   * {@link VariableDeclaration} should be removed by one.
   */
  public void test_delete_2() throws Exception {
    parseContainer(
        "// filler filler filler",
        "public class  Test extends JPanel {",
        "  private JButton button_1 = new JButton(), button_2 = new JButton();",
        "  Test() {",
        "    add(button_1);",
        "    add(button_2);",
        "  }",
        "}");
    //
    {
      ComponentInfo button_1 = getJavaInfoByName("button_1");
      assertTrue(button_1.canDelete());
      button_1.delete();
      assertEditor(
          "// filler filler filler",
          "public class  Test extends JPanel {",
          "  private JButton button_2 = new JButton();",
          "  Test() {",
          "    add(button_2);",
          "  }",
          "}");
    }
    {
      ComponentInfo button_2 = getJavaInfoByName("button_2");
      assertTrue(button_2.canDelete());
      button_2.delete();
      assertEditor(
          "// filler filler filler",
          "public class  Test extends JPanel {",
          "  Test() {",
          "  }",
          "}");
    }
  }

  /**
   * Component is root, so its variable should not be removed.
   */
  public void test_delete_3() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class Test {",
            "  private static JPanel rootPanel = new JPanel();",
            "  public static void main(String[] args) {",
            "  }",
            "}");
    assertTrue(panel.canDelete());
    panel.delete();
    assertEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public class Test {",
        "  private static JPanel rootPanel = new JPanel();",
        "  public static void main(String[] args) {",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Move
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for moving component with {@link FieldInitializerVariableSupport}.<br>
   * Component has related nodes.
   */
  public void test_move_withRelatedNodes() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private final JPanel panel2 = new JPanel();",
            "  public Test() {",
            "    {",
            "      JPanel panel = new JPanel();",
            "      add(panel);",
            "    }",
            "    {",
            "      add(panel2);",
            "      panel2.setEnabled(false);",
            "    }",
            "  }",
            "}");
    ContainerInfo panel_1 = (ContainerInfo) panel.getChildrenComponents().get(0);
    ContainerInfo panel_2 = (ContainerInfo) panel.getChildrenComponents().get(1);
    FlowLayoutInfo flowLayout_1 = (FlowLayoutInfo) panel_1.getLayout();
    // move and check
    flowLayout_1.move(panel_2, null);
    assertEditor(
        "public class Test extends JPanel {",
        "  private final JPanel panel2 = new JPanel();",
        "  public Test() {",
        "    {",
        "      JPanel panel = new JPanel();",
        "      add(panel);",
        "      {",
        "        panel.add(panel2);",
        "        panel2.setEnabled(false);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for moving component with {@link FieldInitializerVariableSupport}.<br>
   * Component has no related nodes.
   */
  public void test_move_noRelatedNodes() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private final JPanel panel2 = new JPanel();",
            "  public Test() {",
            "    {",
            "      JPanel panel = new JPanel();",
            "      add(panel);",
            "    }",
            "    {",
            "      add(panel2);",
            "    }",
            "  }",
            "}");
    ContainerInfo panel_1 = (ContainerInfo) panel.getChildrenComponents().get(0);
    ContainerInfo panel_2 = (ContainerInfo) panel.getChildrenComponents().get(1);
    FlowLayoutInfo flowLayout_1 = (FlowLayoutInfo) panel_1.getLayout();
    // move and check
    flowLayout_1.move(panel_2, null);
    assertEditor(
        "public class Test extends JPanel {",
        "  private final JPanel panel2 = new JPanel();",
        "  public Test() {",
        "    {",
        "      JPanel panel = new JPanel();",
        "      add(panel);",
        "      panel.add(panel2);",
        "    }",
        "  }",
        "}");
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
            "  private JButton button = new JButton();",
            "  public Test() {",
            "    add(button);",
            "  }",
            "}");
    JavaInfo button = panel.getChildrenComponents().get(0);
    // check
    FieldInitializerVariableSupport variable =
        (FieldInitializerVariableSupport) button.getVariableSupport();
    variable.setType("javax.swing.JTextField");
    assertEditor(
        "public class Test extends JPanel {",
        "  private JTextField button = new JButton();",
        "  public Test() {",
        "    add(button);",
        "  }",
        "}");
  }
}
