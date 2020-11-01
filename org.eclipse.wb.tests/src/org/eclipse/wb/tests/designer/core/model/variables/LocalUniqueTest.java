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

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ToolkitDescription;
import org.eclipse.wb.internal.core.model.generation.statement.block.BlockStatementGeneratorDescription;
import org.eclipse.wb.internal.core.model.order.MethodOrderBeforeAssociation;
import org.eclipse.wb.internal.core.model.variable.EmptyVariableSupport;
import org.eclipse.wb.internal.core.model.variable.FieldUniqueVariableSupport;
import org.eclipse.wb.internal.core.model.variable.LocalUniqueVariableSupport;
import org.eclipse.wb.internal.core.model.variable.NamesManager;
import org.eclipse.wb.internal.core.model.variable.NamesManager.ComponentNameDescription;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.model.variable.description.LocalUniqueVariableDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.BodyDeclarationTarget;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;
import org.eclipse.wb.internal.swing.ToolkitProvider;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.FlowLayoutInfo;
import org.eclipse.wb.tests.designer.swing.SwingTestUtils;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JTextField;

/**
 * Test for {@link LocalUniqueVariableSupport}.
 *
 * @author scheglov_ke
 */
public class LocalUniqueTest extends AbstractVariableTest {
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
   * Test for {@link LocalUniqueVariableSupport} as object.
   */
  public void test_object() throws Exception {
    ContainerInfo panel = parseCase_1();
    // check child: 0
    {
      JavaInfo button = panel.getChildrenComponents().get(0);
      VariableSupport variableSupport = button.getVariableSupport();
      //
      assertTrue(variableSupport instanceof LocalUniqueVariableSupport);
      // name
      assertTrue(variableSupport.hasName());
      assertSame(button, variableSupport.getJavaInfo());
      assertEquals("button", variableSupport.getName());
      assertEquals("button", variableSupport.getTitle());
      //
      assertEquals("local-unique: button", variableSupport.toString());
      assertTrue(variableSupport.canConvertLocalToField());
      assertFalse(variableSupport.canConvertFieldToLocal());
      try {
        variableSupport.convertFieldToLocal();
        fail();
      } catch (IllegalStateException e) {
      }
    }
    // check child: 1
    {
      JavaInfo button = panel.getChildrenComponents().get(1);
      VariableSupport variableSupport = button.getVariableSupport();
      //
      assertTrue(variableSupport instanceof LocalUniqueVariableSupport);
      assertTrue(variableSupport.hasName());
      assertEquals("button", variableSupport.getName());
    }
  }

  /**
   * Test for {@link VariableSupport#setName(String)}.
   */
  public void test_setName() throws Exception {
    ContainerInfo panel = parseContainer(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    JButton button = new JButton();",
        "    add(button);",
        "  }",
        "}");
    //
    JavaInfo button = panel.getChildrenComponents().get(0);
    VariableSupport variableSupport = button.getVariableSupport();
    variableSupport.setName("abc");
    assertAST(m_lastEditor);
    assertEquals(
        getTestSource(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JButton abc = new JButton();",
            "    add(abc);",
            "  }",
            "}"),
        m_lastEditor.getSource());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // toField
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_toField() throws Exception {
    ContainerInfo panel = parseContainer(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    JButton button = new JButton();",
        "    add(button);",
        "  }",
        "}");
    check_toField(
        panel,
        getTestSource(
            "public class Test extends JPanel {",
            "  private JButton button;",
            "  public Test() {",
            "    button = new JButton();",
            "    add(button);",
            "  }",
            "}"));
  }

  /**
   * Root method is static, and variable is in <em>static</em> method, so field should be declared
   * as <em>static</em>.
   */
  public void test_toFieldStatic() throws Exception {
    ComponentInfo button = parseContainer(
        "public class Test {",
        "  public static void main(String [] args) {",
        "    JButton button = new JButton();",
        "  }",
        "}");
    button.getVariableSupport().convertLocalToField();
    assertEditor(
        "public class Test {",
        "  private static JButton button;",
        "  public static void main(String [] args) {",
        "    button = new JButton();",
        "  }",
        "}");
  }

  /**
   * Root method is static, but variable is in <em>instance</em> method, so field should be declared
   * as <em>instance</em>.
   */
  public void test_toFieldStatic2() throws Exception {
    m_waitForAutoBuild = true;
    ComponentInfo button = parseContainer(
        "public class Test {",
        "  public static void main(String [] args) {",
        "    Test application = new Test();",
        "    application.open();",
        "  }",
        "  public void open() {",
        "    JButton button = new JButton();",
        "  }",
        "}");
    button.getVariableSupport().convertLocalToField();
    assertEditor(
        "public class Test {",
        "  private JButton button;",
        "  public static void main(String [] args) {",
        "    Test application = new Test();",
        "    application.open();",
        "  }",
        "  public void open() {",
        "    button = new JButton();",
        "  }",
        "}");
  }

  public void test_toField_withPrefixes() throws Exception {
    ContainerInfo panel = parseContainer(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    JButton button = new JButton();",
        "    add(button);",
        "  }",
        "}");
    IJavaProject javaProject = m_lastEditor.getJavaProject();
    //
    Map<String, String> options;
    {
      options = ProjectUtils.getOptions(javaProject);
      javaProject.setOption(JavaCore.CODEASSIST_FIELD_PREFIXES, "m_");
      javaProject.setOption(JavaCore.CODEASSIST_FIELD_SUFFIXES, "_Q");
    }
    //
    try {
      check_toField(
          panel,
          getTestSource(
              "public class Test extends JPanel {",
              "  private JButton m_button_Q;",
              "  public Test() {",
              "    m_button_Q = new JButton();",
              "    add(m_button_Q);",
              "  }",
              "}"));
    } finally {
      javaProject.setOptions(options);
    }
  }

  public void test_toField_assignment() throws Exception {
    ContainerInfo panel = parseContainer(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    JButton button;",
        "    button = new JButton();",
        "    add(button);",
        "  }",
        "}");
    check_toField(
        panel,
        getTestSource(
            "public class Test extends JPanel {",
            "  private JButton button;",
            "  public Test() {",
            "    button = new JButton();",
            "    add(button);",
            "  }",
            "}"));
  }

  public void test_toField_numberPrefix() throws Exception {
    ContainerInfo panel = parseContainer(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    JButton button_1 = new JButton();",
        "    add(button_1);",
        "  }",
        "}");
    check_toField(
        panel,
        getTestSource(
            "public class Test extends JPanel {",
            "  private JButton button;",
            "  public Test() {",
            "    button = new JButton();",
            "    add(button);",
            "  }",
            "}"));
  }

  public void test_toField_afterExistingField() throws Exception {
    ContainerInfo panel = parseContainer(
        "public class Test extends JPanel {",
        "  private int m_value;",
        "  public Test() {",
        "    JButton button_1 = new JButton();",
        "    add(button_1);",
        "  }",
        "}");
    check_toField(
        panel,
        getTestSource(
            "public class Test extends JPanel {",
            "  private int m_value;",
            "  private JButton button;",
            "  public Test() {",
            "    button = new JButton();",
            "    add(button);",
            "  }",
            "}"));
  }

  private static void check_toField(ContainerInfo panel, String expectedSource) throws Exception {
    AstEditor editor = panel.getEditor();
    //
    ComponentInfo button = panel.getChildrenComponents().get(0);
    button.getVariableSupport().convertLocalToField();
    assertTrue(button.getVariableSupport() instanceof FieldUniqueVariableSupport);
    //
    assertAST(editor);
    assertEquals(expectedSource, editor.getSource());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // isJavaInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  /*public void test_isJavaInfo_assignment() throws Exception {
  	ContainerInfo panel =
  			parseTestSource(new String[]{
  					"public class Test extends JPanel {",
  					"  Test() {",
  					"    JButton button;",
  					"    button = new JButton();",
  					"    add(button);",
  					"  }",
  					"}"});
  	ComponentInfo button = panel.getChildrenComponents().get(0);
  	VariableSupport variableSupport = button.getVariableSupport();
  	// test assignment of "button" variable
  	{
  		SimpleName node = (SimpleName) m_lastEditor.getEnclosingNode("button =");
  		assertEquals("button", node.getIdentifier());
  		variableSupport.isJavaInfo(node);
  	}
  	// invalid node
  	assertFalse(variableSupport.isJavaInfo(null));
  }
  public void test_isJavaInfo_declaration() throws Exception {
  	ContainerInfo panel =
  			parseTestSource(new String[]{
  					"public class Test extends JPanel {",
  					"  Test() {",
  					"    JButton button = new JButton();",
  					"    add(button);",
  					"  }",
  					"}"});
  	ComponentInfo button = panel.getChildrenComponents().get(0);
  	VariableSupport variableSupport = button.getVariableSupport();
  	// test declaration of "button" variable
  	{
  		SimpleName node = (SimpleName) m_lastEditor.getEnclosingNode("button =");
  		assertEquals("button", node.getIdentifier());
  		variableSupport.isJavaInfo(node);
  	}
  }*/
  ////////////////////////////////////////////////////////////////////////////
  //
  // hasExpression()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_hasExpression() throws Exception {
    ContainerInfo panel = parseContainer(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    JButton button = new JButton();",
        "    add(button);",
        "  }",
        "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    VariableSupport variableSupport = button.getVariableSupport();
    // no expression "before button Statement"
    {
      NodeTarget target = getNodeStatementTarget(panel, true, 0);
      assertFalse(variableSupport.hasExpression(target));
    }
    // has expression "after button Statement"
    {
      NodeTarget target = getNodeStatementTarget(panel, false, 0);
      assertTrue(variableSupport.hasExpression(target));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getReferenceExpression
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getReferenceExpression_local_declarationWithInitializer() throws Exception {
    ContainerInfo panel = parseContainer(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    JButton button = new JButton();",
        "    add(button);",
        "  }",
        "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    VariableSupport variableSupport = button.getVariableSupport();
    // local expression keeps local variable
    NodeTarget target = getNodeStatementTarget(panel, false, 0);
    assertEquals("button", variableSupport.getReferenceExpression(target));
    assertEquals("button.", variableSupport.getAccessExpression(target));
    assertTrue(button.getVariableSupport() instanceof LocalUniqueVariableSupport);
  }

  public void test_getReferenceExpression_local_assignAfterDeclaration() throws Exception {
    ContainerInfo panel = parseContainer(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    JButton button;",
        "    button = new JButton();",
        "    add(button);",
        "  }",
        "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    VariableSupport variableSupport = button.getVariableSupport();
    // local expression keeps local variable
    NodeTarget target = getNodeStatementTarget(panel, false, 2);
    assertEquals("button", variableSupport.getReferenceExpression(target));
    assertEquals("button.", variableSupport.getAccessExpression(target));
    assertTrue(button.getVariableSupport() instanceof LocalUniqueVariableSupport);
  }

  public void test_getReferenceExpression_local_beginOfBlock() throws Exception {
    ContainerInfo panel = parseContainer(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    JButton button = new JButton();",
        "    add(button);",
        "    {",
        "      // empty block",
        "    }",
        "  }",
        "}");
    String expectedSource = m_lastEditor.getSource();
    ComponentInfo button = panel.getChildrenComponents().get(0);
    VariableSupport variableSupport = button.getVariableSupport();
    // begin of "empty" block
    {
      NodeTarget target = getNodeBlockTarget(panel, true, 2);
      assertEquals("button", variableSupport.getReferenceExpression(target));
      assertEquals("button.", variableSupport.getAccessExpression(target));
      assertTrue(button.getVariableSupport() instanceof LocalUniqueVariableSupport);
      assertEditor(expectedSource, m_lastEditor);
    }
  }

  public void test_getReferenceExpression_local_endOfBlock() throws Exception {
    ContainerInfo panel = parseContainer(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    JButton button = new JButton();",
        "    add(button);",
        "    {",
        "      // empty block",
        "    }",
        "  }",
        "}");
    String expectedSource = m_lastEditor.getSource();
    ComponentInfo button = panel.getChildrenComponents().get(0);
    VariableSupport variableSupport = button.getVariableSupport();
    // end of "empty" block
    {
      NodeTarget target = getNodeBlockTarget(panel, false, 2);
      assertEquals("button", variableSupport.getReferenceExpression(target));
      assertEquals("button.", variableSupport.getAccessExpression(target));
      assertTrue(button.getVariableSupport() instanceof LocalUniqueVariableSupport);
      assertEditor(expectedSource, m_lastEditor);
    }
    // end of "Test" block
    {
      NodeTarget target = getNodeBlockTarget(panel, false);
      assertEquals("button", variableSupport.getReferenceExpression(target));
      assertEquals("button.", variableSupport.getAccessExpression(target));
      assertTrue(button.getVariableSupport() instanceof LocalUniqueVariableSupport);
      assertEditor(expectedSource, m_lastEditor);
    }
  }

  public void test_getReferenceExpression_remote_afterBlock() throws Exception {
    ContainerInfo panel = parseContainer(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    {",
        "      JButton button = new JButton();",
        "      add(button);",
        "    }",
        "  }",
        "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    VariableSupport variableSupport = button.getVariableSupport();
    // after "button" block (not at end of this block!)
    NodeTarget target = getNodeStatementTarget(panel, false, 0);
    assertEquals("button", variableSupport.getReferenceExpression(target));
    assertTrue(button.getVariableSupport() instanceof FieldUniqueVariableSupport);
    assertEditor(
        "public class Test extends JPanel {",
        "  private JButton button;",
        "  public Test() {",
        "    {",
        "      button = new JButton();",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  public void test_getReferenceExpression_remote_afterBlock_asBeforeStatement() throws Exception {
    ContainerInfo panel = parseContainer(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    {",
        "      JButton button = new JButton();",
        "      add(button);",
        "    }",
        "    int target;",
        "  }",
        "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    VariableSupport variableSupport = button.getVariableSupport();
    // after "button" block, but using "before" next statement
    NodeTarget target = getNodeStatementTarget(panel, true, 1);
    assertEquals("button", variableSupport.getReferenceExpression(target));
    assertTrue(button.getVariableSupport() instanceof FieldUniqueVariableSupport);
    assertEditor(
        "public class Test extends JPanel {",
        "  private JButton button;",
        "  public Test() {",
        "    {",
        "      button = new JButton();",
        "      add(button);",
        "    }",
        "    int target;",
        "  }",
        "}");
  }

  /**
   * Test for remote {@link VariableSupport#getReferenceExpression(NodeTarget)}.
   */
  public void test_getReferenceExpression_remote_otherMethodOfExecutionFlow() throws Exception {
    ContainerInfo panel = parseContainer(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    addButton();",
        "  }",
        "  private void addButton() {",
        "    JButton button = new JButton();",
        "    add(button);",
        "  }",
        "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // remote expression forces conversion
    NodeTarget target = getNodeStatementTarget(panel, false, 0);
    assertEquals("button", button.getVariableSupport().getReferenceExpression(target));
    assertEquals("button.", button.getVariableSupport().getAccessExpression(target));
    assertTrue(button.getVariableSupport() instanceof FieldUniqueVariableSupport);
    assertEditor(
        "public class Test extends JPanel {",
        "  private JButton button;",
        "  public Test() {",
        "    addButton();",
        "  }",
        "  private void addButton() {",
        "    button = new JButton();",
        "    add(button);",
        "  }",
        "}");
  }

  /**
   * Test for remote {@link VariableSupport#getReferenceExpression(NodeTarget)}.
   */
  public void test_getReferenceExpression_remote_invokedMethod() throws Exception {
    ContainerInfo panel = parseContainer(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    JButton button = new JButton();",
        "    add(button);",
        "    someInvokedMethod();",
        "  }",
        "  private void someInvokedMethod() {",
        "  }",
        "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // remote expression forces conversion
    NodeTarget target = getNodeStatementTarget(panel, "someInvokedMethod()", true);
    assertEquals("button", button.getVariableSupport().getReferenceExpression(target));
    assertEquals("button.", button.getVariableSupport().getAccessExpression(target));
    assertTrue(button.getVariableSupport() instanceof FieldUniqueVariableSupport);
    assertEditor(
        "public class Test extends JPanel {",
        "  private JButton button;",
        "  public Test() {",
        "    button = new JButton();",
        "    add(button);",
        "    someInvokedMethod();",
        "  }",
        "  private void someInvokedMethod() {",
        "  }",
        "}");
  }

  /**
   * Test for remote {@link VariableSupport#getReferenceExpression(NodeTarget)}.
   */
  public void test_getReferenceExpression_remote_methodBodyNotOfExecutionFlow() throws Exception {
    ContainerInfo panel = parseContainer(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    JButton button = new JButton();",
        "    add(button);",
        "  }",
        "  private void externalMethod() {",
        "  }",
        "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // remote expression forces conversion
    Block targetBlock = getBlock(panel, "externalMethod()");
    NodeTarget target = new NodeTarget(new StatementTarget(targetBlock, true));
    assertEquals("button", button.getVariableSupport().getReferenceExpression(target));
    assertEquals("button.", button.getVariableSupport().getAccessExpression(target));
    assertTrue(button.getVariableSupport() instanceof FieldUniqueVariableSupport);
    assertEditor(
        "public class Test extends JPanel {",
        "  private JButton button;",
        "  public Test() {",
        "    button = new JButton();",
        "    add(button);",
        "  }",
        "  private void externalMethod() {",
        "  }",
        "}");
  }

  /**
   * Test for remote {@link VariableSupport#getReferenceExpression(NodeTarget)}.
   */
  public void test_getReferenceExpression_remote_afterMethodDeclaration() throws Exception {
    ContainerInfo panel = parseContainer(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    JButton button = new JButton();",
        "    add(button);",
        "  }",
        "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // remote expression forces conversion
    MethodDeclaration targetMethod = (MethodDeclaration) getBodyDeclaration(panel, 0);
    NodeTarget target = new NodeTarget(new BodyDeclarationTarget(targetMethod, false));
    assertEquals("button", button.getVariableSupport().getReferenceExpression(target));
    assertEquals("button.", button.getVariableSupport().getAccessExpression(target));
    assertTrue(button.getVariableSupport() instanceof FieldUniqueVariableSupport);
    assertEditor(
        "public class Test extends JPanel {",
        "  private JButton button;",
        "  public Test() {",
        "    button = new JButton();",
        "    add(button);",
        "  }",
        "}");
  }

  /**
   * Test for remote {@link VariableSupport#getReferenceExpression(NodeTarget)}.
   */
  public void test_getReferenceExpression_remote_endOfTypeDeclaration() throws Exception {
    ContainerInfo panel = parseContainer(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    JButton button = new JButton();",
        "    add(button);",
        "  }",
        "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // remote expression forces conversion
    TypeDeclaration targetType = getTypeDeclaration(panel);
    NodeTarget target = new NodeTarget(new BodyDeclarationTarget(targetType, false));
    assertEquals("button", button.getVariableSupport().getReferenceExpression(target));
    assertEquals("button.", button.getVariableSupport().getAccessExpression(target));
    assertTrue(button.getVariableSupport() instanceof FieldUniqueVariableSupport);
    assertEditor(
        "public class Test extends JPanel {",
        "  private JButton button;",
        "  public Test() {",
        "    button = new JButton();",
        "    add(button);",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Target
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_target() throws Exception {
    ContainerInfo panel = parseContainer(
        "public class Test {",
        "  public static void main(String args[]){",
        "    JPanel panel = new JPanel();",
        "  }",
        "}");
    TypeDeclaration typeDeclaration = AstNodeUtils.getTypeByName(m_lastEditor.getAstUnit(), "Test");
    MethodDeclaration mainMethod = typeDeclaration.getMethods()[0];
    assertStatementTarget(panel, null, (Statement) mainMethod.getBody().statements().get(0), false);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // isVisibleAt
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_isVisibleAt() throws Exception {
    ContainerInfo panel = parseContainer(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    {",
        "      System.out.println();",
        "    }",
        "    {",
        "      JButton button = new JButton('button 1');",
        "      add(button);",
        "      {",
        "        System.out.println();",
        "      }",
        "    }",
        "    {",
        "      JButton button = new JButton('button 2');",
        "      add(button);",
        "    }",
        "  }",
        "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // this block - visible
    check_isVisibleAt(true, button, new int[]{1, 1});
    // inner block - visible
    check_isVisibleAt(true, button, new int[]{1, 2, 0});
    // block before - invisible
    check_isVisibleAt(false, button, new int[]{0, 0});
    // block after - invisible
    check_isVisibleAt(false, button, new int[]{2});
    check_isVisibleAt(false, button, new int[]{2, 1});
  }

  private static void check_isVisibleAt(boolean expected, JavaInfo button, int[] indexes)
      throws Exception {
    TypeDeclaration typeDeclaration =
        (TypeDeclaration) button.getEditor().getAstUnit().types().get(0);
    MethodDeclaration methodDeclaration = typeDeclaration.getMethods()[0];
    //
    Statement statement = getStatement(methodDeclaration.getBody(), indexes);
    assertEquals(expected, button.getVariableSupport().isValidStatementForChild(statement));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ADD
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test adding new component.
   */
  public void test_ADD_normal() throws Exception {
    ContainerInfo panel = parseContainer(
        "// filler filler filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "  }",
        "}");
    FlowLayoutInfo flowLayout = (FlowLayoutInfo) panel.getLayout();
    //
    ComponentInfo newComponent = createJButton();
    // add component
    SwingTestUtils.setGenerations(
        LocalUniqueVariableDescription.INSTANCE,
        BlockStatementGeneratorDescription.INSTANCE);
    try {
      flowLayout.add(newComponent, null);
    } finally {
      SwingTestUtils.setGenerationDefaults();
    }
    // check
    assertEditor(
        "// filler filler filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    {",
        "      JButton button = new JButton();",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test adding new component, with "final".
   */
  public void test_ADD_final() throws Exception {
    ContainerInfo panel = parseContainer(
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
        LocalUniqueVariableDescription.INSTANCE,
        BlockStatementGeneratorDescription.INSTANCE);
    panel.getDescription().getToolkit().getPreferences().setValue(
        LocalUniqueVariableSupport.P_DECLARE_FINAL,
        true);
    try {
      flowLayout.add(newComponent, null);
    } finally {
      SwingTestUtils.setGenerationDefaults();
    }
    // check
    assertEditor(
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    {",
        "      final JButton button = new JButton();",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test adding new component, {@link JTextField}, forced field.
   */
  public void test_ADD_forcedField() throws Exception {
    ToolkitDescription toolkit = ToolkitProvider.DESCRIPTION;
    // set type specific
    {
      List<ComponentNameDescription> descriptions = new ArrayList<>();
      descriptions.add(
          new ComponentNameDescription("javax.swing.JTextField", "textField", "txt", true));
      NamesManager.setNameDescriptions(toolkit, descriptions);
    }
    // check descriptions
    ContainerInfo panel = parseContainer(
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "  }",
        "}");
    FlowLayoutInfo flowLayout = (FlowLayoutInfo) panel.getLayout();
    // add component
    SwingTestUtils.setGenerations(
        LocalUniqueVariableDescription.INSTANCE,
        BlockStatementGeneratorDescription.INSTANCE);
    try {
      ComponentInfo newComponent = createComponent(JTextField.class);
      flowLayout.add(newComponent, null);
    } finally {
      SwingTestUtils.setGenerationDefaults();
    }
    // check
    assertEditor(
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  private JTextField textField;",
        "  public Test() {",
        "    {",
        "      textField = new JTextField();",
        "      add(textField);",
        "      textField.setColumns(10);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Special case when one of the methods has {@link MethodOrderBeforeAssociation}.
   */
  public void test_ADD_beforeAssociation() throws Exception {
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "// filler filler filler filler filler",
            "public class MyButton extends JButton {",
            "}"));
    setFileContentSrc(
        "test/MyButton.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <creation>",
            "    <source><![CDATA[new test.MyButton()]]></source>",
            "    <invocation signature='setText(java.lang.String)'><![CDATA['New Button']]></invocation>",
            "  </creation>",
            "  <method-order>",
            "    <default order='beforeAssociation'/>",
            "  </method-order>",
            "</component>"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel = parseContainer(
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "  }",
        "}");
    // add new MyButton
    ComponentInfo button = createJavaInfo("test.MyButton");
    ((FlowLayoutInfo) panel.getLayout()).add(button, null);
    assertEditor(
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    {",
        "      MyButton myButton = new MyButton();",
        "      myButton.setText('New Button');",
        "      add(myButton);",
        "    }",
        "  }",
        "}");
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
    ContainerInfo panel = parseContainer(
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "  }",
        "}");
    // add new MyButton
    ComponentInfo button = createJavaInfo("test.MyButton");
    ((FlowLayoutInfo) panel.getLayout()).add(button, null);
    assertEditor(
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    {",
        "      MyButton myButton = new MyButton('myButton');",
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
    ContainerInfo panel = parseContainer(
        "// filler filler filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "  }",
        "}");
    // add new MyButton
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
        "  public Test() {",
        "    {",
        "      MyButton<String, List<Double>> myButton = new MyButton<String, List<Double>>();",
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
   * Variable declaration deleted with {@link Statement}.
   */
  public void test_delete_1() throws Exception {
    ContainerInfo panel = parseContainer(
        "// filler filler filler",
        "// filler filler filler",
        "public class  Test extends JPanel {",
        "  Test() {",
        "    JButton button = new JButton();",
        "    add(button);",
        "}",
        "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    //
    assertTrue(button.canDelete());
    button.delete();
    assertEditor(
        "// filler filler filler",
        "// filler filler filler",
        "public class  Test extends JPanel {",
        "  Test() {",
        "}",
        "}");
  }

  /**
   * Variable declaration in separate {@link Statement}.
   */
  public void test_delete_2() throws Exception {
    ContainerInfo panel = parseContainer(
        "// filler filler filler",
        "public final class Test extends JPanel {",
        "  public Test() {",
        "    JButton button;",
        "    button = new JButton();",
        "    add(button);",
        "  }",
        "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    //
    assertTrue(button.canDelete());
    button.delete();
    assertEditor(
        "// filler filler filler",
        "public final class Test extends JPanel {",
        "  public Test() {",
        "  }",
        "}");
  }

  /**
   * Component is root, so its variable should not be removed.
   */
  public void test_delete_3() throws Exception {
    ContainerInfo panel = parseContainer(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public class Test {",
        "  public static void main(String[] args) {",
        "    JPanel rootPanel = new JPanel();",
        "  }",
        "}");
    //
    assertTrue(panel.canDelete());
    panel.delete();
    assertEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public class Test {",
        "  public static void main(String[] args) {",
        "    JPanel rootPanel = new JPanel();",
        "  }",
        "}");
  }

  /**
   * Variable declaration in same {@link Statement}.
   */
  public void test_delete_4() throws Exception {
    parseContainer(
        "// filler filler filler",
        "public final class Test extends JPanel {",
        "  public Test() {",
        "    JButton button_1 = new JButton(), button_2, button_3;",
        "    add(button_1);",
        "    button_2 = new JButton();",
        "    add(button_2);",
        "    button_3 = new JButton();",
        "    add(button_3);",
        "  }",
        "}");
    //
    {
      ComponentInfo button_1 = getJavaInfoByName("button_1");
      assertTrue(button_1.canDelete());
      button_1.delete();
      assertEditor(
          "// filler filler filler",
          "public final class Test extends JPanel {",
          "  public Test() {",
          "    JButton button_2, button_3;",
          "    button_2 = new JButton();",
          "    add(button_2);",
          "    button_3 = new JButton();",
          "    add(button_3);",
          "  }",
          "}");
    }
    {
      ComponentInfo button_3 = getJavaInfoByName("button_3");
      assertTrue(button_3.canDelete());
      button_3.delete();
      assertEditor(
          "// filler filler filler",
          "public final class Test extends JPanel {",
          "  public Test() {",
          "    JButton button_2;",
          "    button_2 = new JButton();",
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
          "public final class Test extends JPanel {",
          "  public Test() {",
          "  }",
          "}");
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // setType()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_setType() throws Exception {
    ContainerInfo panel = parseContainer(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    JButton button = new JButton();",
        "    add(button);",
        "  }",
        "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // check
    LocalUniqueVariableSupport variable = (LocalUniqueVariableSupport) button.getVariableSupport();
    variable.setType("javax.swing.JTextField");
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    JTextField button = new JButton();",
        "    add(button);",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Inline
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link LocalUniqueVariableSupport#canInline()}.
   */
  public void test_inline_1() throws Exception {
    ContainerInfo panel = parseContainer(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    JButton button = new JButton();",
        "    button.setEnabled(false);",
        "    add(button);",
        "  }",
        "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // can not inline
    LocalUniqueVariableSupport variableSupport =
        (LocalUniqueVariableSupport) button.getVariableSupport();
    assertFalse(variableSupport.canInline());
  }

  /**
   * Test for {@link LocalUniqueVariableSupport#inline()}.
   */
  public void test_inline_2() throws Exception {
    ContainerInfo panel = parseContainer(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    JButton button = new JButton();",
        "    add(button);",
        "  }",
        "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // prepare initial CreationSupport
    CreationSupport creationSupport = button.getCreationSupport();
    ASTNode creationNode = creationSupport.getNode();
    // do inline
    {
      LocalUniqueVariableSupport variableSupport =
          (LocalUniqueVariableSupport) button.getVariableSupport();
      assertTrue(variableSupport.canInline());
      variableSupport.inline();
    }
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    add(new JButton());",
        "  }",
        "}");
    // CreationSupport and its ASTNode should be same
    assertSame(creationSupport, button.getCreationSupport());
    assertSame(creationNode, creationSupport.getNode());
    // and now EmptyVariableSupport
    {
      EmptyVariableSupport variableSupport = (EmptyVariableSupport) button.getVariableSupport();
      assertSame(creationNode, variableSupport.getInitializer());
    }
  }

  /**
   * Test for {@link LocalUniqueVariableSupport#inline()}.<br>
   * Case when inlined {@link Expression} should be wrapped with {@link ParenthesizedExpression}
   * because of execution precedence.
   */
  public void test_inline_3() throws Exception {
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "public class MyButton extends JButton {",
            "  public MyButton(Container container) {",
            "    container.add(this);",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyButton.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <constructors>",
            "    <constructor>",
            "      <parameter type='java.awt.Container' parent='true'/>",
            "    </constructor>",
            "  </constructors>",
            "</component>"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel = parseContainer(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    MyButton button = (MyButton) new MyButton(this);",
        "    button.setEnabled(false);",
        "  }",
        "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // prepare initial CreationSupport
    CreationSupport creationSupport = button.getCreationSupport();
    ASTNode creationNode = creationSupport.getNode();
    // do inline
    {
      LocalUniqueVariableSupport variableSupport =
          (LocalUniqueVariableSupport) button.getVariableSupport();
      assertTrue(variableSupport.canInline());
      variableSupport.inline();
    }
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    ((MyButton) new MyButton(this)).setEnabled(false);",
        "  }",
        "}");
    // CreationSupport and its ASTNode should be same
    assertSame(creationSupport, button.getCreationSupport());
    assertSame(creationNode, creationSupport.getNode());
    // and now EmptyVariableSupport
    {
      EmptyVariableSupport variableSupport = (EmptyVariableSupport) button.getVariableSupport();
      CastExpression casted = (CastExpression) variableSupport.getInitializer();
      assertSame(creationNode, casted.getExpression());
    }
    // after inlining (with ParenthesizedExpression) invocations/fields still should be accessible
    {
      MethodInvocation invocation = button.getMethodInvocation("setEnabled(boolean)");
      assertNotNull(invocation);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Cases
  //
  ////////////////////////////////////////////////////////////////////////////
  private ContainerInfo parseCase_1() throws Exception {
    return parseContainer(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    {",
        "      final JButton button = new JButton('button 1');",
        "      add(button);",
        "      button.addActionListener(new ActionListener() {",
        "        public void actionPerformed(ActionEvent e) {",
        "          button.setVisible(false);",
        "        }",
        "      });",
        "    }",
        "    {",
        "      JButton button = new JButton('button 2');",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }
}
