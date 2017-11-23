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
package org.eclipse.wb.tests.designer.core.model;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.JavaInfoSetVariable;
import org.eclipse.wb.core.model.broadcast.ObjectInfoAllProperties;
import org.eclipse.wb.core.model.broadcast.ObjectInfoDelete;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.accessor.FieldAccessor;
import org.eclipse.wb.internal.core.model.property.accessor.SetterAccessor;
import org.eclipse.wb.internal.core.model.util.ObjectsLabelProvider;
import org.eclipse.wb.internal.core.model.util.TemplateUtils;
import org.eclipse.wb.internal.core.model.variable.FieldInitializerVariableSupport;
import org.eclipse.wb.internal.core.model.variable.FieldUniqueVariableSupport;
import org.eclipse.wb.internal.core.model.variable.LocalUniqueVariableSupport;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.Component;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * @author scheglov_ke
 */
public class JavaInfoTest extends SwingModelTest {
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
  // toString()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Simple {@link JPanel} without any related nodes.
   */
  public void test_toString_0() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    assertEquals("{this: javax.swing.JPanel} {this} {}", panel.toString());
  }

  /**
   * {@link JPanel} with related nodes.
   */
  public void test_toString_1() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test {",
            "  public static void main(String[] args) {",
            "    JPanel panel = new JPanel();",
            "    panel.setEnabled(true);",
            "  }",
            "}");
    assertEquals(
        "{new: javax.swing.JPanel} {local-unique: panel} {/new JPanel()/ /panel.setEnabled(true)/}",
        panel.toString());
  }

  /**
   * {@link JPanel} with child.
   */
  public void test_toString_2() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JButton button = new JButton();",
            "    add(button);",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    assertEquals("{this: javax.swing.JPanel} {this} {/add(button)/}", panel.toString());
    assertEquals(
        "{new: javax.swing.JButton} {local-unique: button} {/new JButton()/ /add(button)/}",
        button.toString());
  }

  /**
   * Component created as {@link AnonymousClassDeclaration}.
   */
  public void test_toString_3() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JButton button = new JButton() {};",
            "    add(button);",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    assertEquals(
        "{new: javax.swing.JButton} {local-unique: button} {/new JButton()/ /add(button)/}",
        button.toString());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // isRepresentedBy
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link JavaInfo#isRepresentedBy(ASTNode)}.<br>
   * Request that should be handled by {@link CreationSupport}.
   */
  public void test_isRepresentedBy_CreationSupport() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  Test() {",
            "    add(new JButton());",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    ExpressionStatement statement = (ExpressionStatement) getStatement(panel, 0);
    MethodInvocation invocation = (MethodInvocation) statement.getExpression();
    Expression creationExpression = DomGenerics.arguments(invocation).get(0);
    assertTrue(button.isRepresentedBy(creationExpression));
  }

  /**
   * Test for {@link JavaInfo#isRepresentedBy(ASTNode)}.<br>
   * Request using local unique variable, assigned in {@link VariableDeclarationFragment}.
   */
  public void test_isRepresentedBy_localUniqueVariableDeclaration() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  Test() {",
            "    JButton button = new JButton();",
            "    add(button);",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    VariableDeclarationStatement statement = (VariableDeclarationStatement) getStatement(panel, 0);
    VariableDeclarationFragment fragment = DomGenerics.fragments(statement).get(0);
    assertEquals("button", fragment.getName().getIdentifier());
    assertTrue(button.isRepresentedBy(fragment.getName()));
  }

  /**
   * Test for {@link JavaInfo#isRepresentedBy(ASTNode)}.<br>
   * Request using local unique variable, assigned using {@link Assignment}.
   */
  public void test_isRepresentedBy_localUniqueVariableAssignment() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  Test() {",
            "    JButton button;",
            "    button = new JButton();",
            "    add(button);",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // test left side of Assignment
    {
      ExpressionStatement statement = (ExpressionStatement) getStatement(panel, 1);
      Assignment assignment = (Assignment) statement.getExpression();
      assertEquals("button", ((SimpleName) assignment.getLeftHandSide()).getIdentifier());
      assertTrue(button.isRepresentedBy(assignment.getLeftHandSide()));
    }
  }

  /**
   * Test for {@link JavaInfo#isRepresentedBy(ASTNode)}.<br>
   * Request using {@link FieldInitializerVariableSupport}.
   */
  public void test_isRepresentedBy_fieldInitializer() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private JButton button = new JButton();",
            "  Test() {",
            "    add(button);",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    //
    FieldDeclaration fieldDeclaration = JavaInfoUtils.getTypeDeclaration(panel).getFields()[0];
    VariableDeclarationFragment fragment = DomGenerics.fragments(fieldDeclaration).get(0);
    assertEquals("button", fragment.getName().getIdentifier());
    assertTrue(button.isRepresentedBy(fragment.getName()));
  }

  /**
   * Test for {@link JavaInfo#isRepresentedBy(ASTNode)}.<br>
   * Request using {@link FieldUniqueVariableSupport}, assigned using {@link Assignment}.
   */
  public void test_isRepresentedBy_fieldUnique() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private JButton button;",
            "  Test() {",
            "    button = new JButton();",
            "    add(button);",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // test left side of Assignment
    {
      ExpressionStatement statement = (ExpressionStatement) getStatement(panel, 0);
      Assignment assignment = (Assignment) statement.getExpression();
      assertEquals("button", ((SimpleName) assignment.getLeftHandSide()).getIdentifier());
      assertTrue(button.isRepresentedBy(assignment.getLeftHandSide()));
    }
  }

  /**
   * Test for {@link TryStatement} and
   * {@link JavaInfo#isRepresentedBy(org.eclipse.jdt.core.dom.ASTNode)} with
   * {@link SingleVariableDeclaration} in {@link CatchClause}.
   */
  public void test_isRepresentedBy_referenceInTryStatement() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  JButton button = new JButton();",
            "  Test() {",
            "    init();",
            "    add(button);",
            "  }",
            "  void init() {",
            "    try {",
            "      button.setEnabled(false);",
            "    } catch (Throwable e) {",
            "      showError(e);",
            "    }",
            "  }",
            "  void showError(Throwable e) {",
            "    System.out.println(e);",
            "  }",
            "}");
    assertEquals(1, panel.getChildrenComponents().size());
    ComponentInfo button = panel.getChildrenComponents().get(0);
    assertTrue(button.getDescription().getComponentClass() == JButton.class);
  }

  /**
   * Test for {@link JavaInfo#isRepresentedBy(ASTNode)}.<br>
   * Use {@link ParenthesizedExpression}.
   */
  public void test_isRepresentedBy_ParenthesizedExpression() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  Test() {",
            "    add((new JButton()));",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    //
    int index = m_lastEditor.getSource().indexOf("(new");
    ASTNode node = m_lastEditor.getEnclosingNode(index);
    assertTrue(button.isRepresentedBy(node));
  }

  /**
   * Test for {@link JavaInfo#isRepresentedBy(ASTNode)}.<br>
   * Use {@link CastExpression}.
   */
  public void test_isRepresentedBy_CastExpression() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  Test() {",
            "    add((Component) new JButton());",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    //
    int index = m_lastEditor.getSource().indexOf("(Component");
    ASTNode node = m_lastEditor.getEnclosingNode(index);
    assertTrue(button.isRepresentedBy(node));
  }

  /**
   * Test for {@link JavaInfo#isRepresentedBy(ASTNode)}.
   * <p>
   * {@link FieldDeclaration} without assignment.
   */
  public void test_isRepresentedBy_emptyField() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private JButton button;",
            "  public Test() {",
            "    System.out.println(button);",
            "  }",
            "}");
    //
    ASTNode node = m_lastEditor.getEnclosingNode("button)");
    assertFalse(panel.isRepresentedBy(node));
  }

  /**
   * Test for {@link JavaInfo#isRepresentedBy(ASTNode)}.
   * <p>
   * {@link FieldDeclaration} with {@link NullLiteral} initializer.
   */
  public void test_isRepresentedBy_nullField() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private JButton button = null;",
            "  public Test() {",
            "    System.out.println(button);",
            "  }",
            "}");
    //
    ASTNode node = m_lastEditor.getEnclosingNode("button)");
    assertFalse(panel.isRepresentedBy(node));
  }

  /**
   * Test for {@link JavaInfo#isRepresentedBy(ASTNode)}.
   * <p>
   * {@link SingleVariableDeclaration} parameter and {@link ThisExpression}.
   */
  public void test_isRepresentedBy_SingleVariableDeclaration() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    createContents(this);",
            "  }",
            "  private void createContents(JPanel parent) {",
            "    System.out.println(parent);",
            "  }",
            "}");
    // usage of "parent"
    {
      SimpleName node = getNode("parent);");
      assertTrue(panel.isRepresentedBy(node));
    }
    // declaration of "parent"
    {
      SimpleName node = getNode("parent) {", SimpleName.class);
      assertTrue(panel.isRepresentedBy(node));
    }
  }

  /**
   * Test for {@link JavaInfo#isRepresentedBy(ASTNode)}.
   * <p>
   * {@link SingleVariableDeclaration} parameter.
   */
  public void test_isRepresentedBy_SingleVariableDeclaration_moreThanOneInvocation()
      throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    createContents(this, 0);",
            "    createContents(this, 1);",
            "  }",
            "  private void createContents(JPanel parent, int value) {",
            "    System.out.println(parent);",
            "  }",
            "}");
    // usage of "parent"
    {
      SimpleName node = getNode("parent);");
      assertTrue(panel.isRepresentedBy(node));
    }
  }

  /**
   * Test for {@link JavaInfo#isRepresentedBy(ASTNode)}.
   */
  public void test_isRepresentedBy_ConstructorInvocation_argument() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    this(new JButton());",
            "  }",
            "  public Test(Component child) {",
            "    add(child);",
            "  }",
            "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(child)/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: javax.swing.JButton} {empty} {/new JButton()/ /add(child)/}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // creation of JButton
    {
      ClassInstanceCreation node =
          (ClassInstanceCreation) m_lastEditor.getEnclosingNode("new JButton()");
      assertTrue(button.isRepresentedBy(node));
    }
    // usage of JButton
    {
      SimpleName node = (SimpleName) m_lastEditor.getEnclosingNode("child);");
      assertTrue(button.isRepresentedBy(node));
    }
  }

  /**
   * Test for {@link JavaInfo#isRepresentedBy(ASTNode)}.
   * <p>
   * Attempt to ask using {@link ASTNode} from recursive method.
   */
  public void test_isRepresentedBy_recursion() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    System.out.println(recursiveMethod(new Object()));",
            "  }",
            "  private Object recursiveMethod(Object o) {",
            "    o = recursiveMethod(o);",
            "    return o;",
            "  }",
            "}");
    //
    ASTNode node = m_lastEditor.getEnclosingNode("recursiveMethod(new");
    assertFalse(panel.isRepresentedBy(node));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Variable
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test that {@link JavaInfo#setVariableSupport(VariableSupport)} sends
   * {@link JavaInfoSetVariable} broadcast.
   */
  public void test_setVariableBroadcast() throws Exception {
    parseContainer(
        "public class Test extends JPanel {",
        "  Test() {",
        "    JButton button = new JButton();",
        "    add(button);",
        "  }",
        "}");
    final ComponentInfo button = getJavaInfoByName("button");
    final VariableSupport expected_oldVariable = button.getVariableSupport();
    final VariableSupport expected_newVariable;
    {
      SimpleName name = (SimpleName) m_lastEditor.getEnclosingNode("button =");
      expected_newVariable = new LocalUniqueVariableSupport(button, name);
    }
    //
    final AtomicInteger count = new AtomicInteger();
    button.addBroadcastListener(new JavaInfoSetVariable() {
      public void invoke(JavaInfo javaInfo, VariableSupport oldVariable, VariableSupport newVariable)
          throws Exception {
        assertSame(button, javaInfo);
        assertSame(expected_oldVariable, oldVariable);
        assertSame(expected_newVariable, newVariable);
        count.incrementAndGet();
      }
    });
    button.setVariableSupport(expected_newVariable);
    assertEquals(1, count.get());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // addExpressionStatement()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link JavaInfo#addExpressionStatement(String)}.
   */
  public void test_addExpressionStatement_1_local() throws Exception {
    JavaInfo panel =
        parseContainer(
            "class Test {",
            "  public static void main(String args[]) {",
            "    JPanel panel = new JPanel();",
            "  }",
            "}");
    // initial checks
    assertRelatedNodes(panel, new String[]{"new JPanel()"});
    // start edit and never finish it to avoid refresh()
    panel.startEdit();
    // add statement
    MethodInvocation invocation;
    {
      String source = TemplateUtils.format("{0}.setEnabled(false)", panel);
      invocation = (MethodInvocation) panel.addExpressionStatement(source);
      assertNotNull(invocation);
      assertEditor(
          "class Test {",
          "  public static void main(String args[]) {",
          "    JPanel panel = new JPanel();",
          "    panel.setEnabled(false);",
          "  }",
          "}");
      assertRelatedNodes(panel, new String[]{"new JPanel()", "panel.setEnabled(false)"});
    }
    // remove statement
    {
      m_lastEditor.removeEnclosingStatement(invocation);
      assertEditor(
          getTestSource(
              "class Test {",
              "  public static void main(String args[]) {",
              "    JPanel panel = new JPanel();",
              "  }",
              "}"),
          m_lastEditor);
      assertRelatedNodes(panel, new String[]{"new JPanel()"});
    }
  }

  /**
   * Test for {@link JavaInfo#addExpressionStatement(String, Map)}.
   */
  public void test_addExpressionStatement_template() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    // add statement
    String expressionSource = TemplateUtils.format("{0}.setEnabled(true)", panel);
    panel.addExpressionStatement(expressionSource);
    assertEditor(
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setEnabled(true);",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Replace expression
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link JavaInfo#replaceExpression(Expression, String)}.
   */
  public void test_replaceExpression() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JLabel label = new JLabel();",
            "    add(label);",
            "    //",
            "    JButton button_1 = new JButton();",
            "    add(button_1);",
            "    //",
            "    JButton button_2 = new JButton();",
            "    add(button_2);",
            "    //",
            "    label.setLabelFor(button_1);",
            "  }",
            "}");
    ComponentInfo label = panel.getChildrenComponents().get(0);
    ComponentInfo button_2 = panel.getChildrenComponents().get(2);
    //
    MethodInvocation invocation = label.getMethodInvocation("setLabelFor(java.awt.Component)");
    Expression expression = DomGenerics.arguments(invocation).get(0);
    label.replaceExpression(expression, TemplateUtils.getExpression(button_2));
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    JLabel label = new JLabel();",
        "    add(label);",
        "    //",
        "    JButton button_1 = new JButton();",
        "    add(button_1);",
        "    //",
        "    JButton button_2 = new JButton();",
        "    add(button_2);",
        "    //",
        "    label.setLabelFor(button_2);",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // MethodInvocation
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getMethodInvocations() throws Exception {
    JavaInfo panel =
        parseContainer(
            "class Test {",
            "  public static void main(String args[]) {",
            "    JPanel panel = new JPanel();",
            "    panel.setEnabled(false);",
            "    panel.setEnabled(true);",
            "  }",
            "}");
    // getMethodInvocations
    {
      assertEquals(0, panel.getMethodInvocations("setVisible(boolean)").size());
      //
      List<MethodInvocation> invocations = panel.getMethodInvocations("setEnabled(boolean)");
      assertEquals(2, invocations.size());
      assertEquals("panel.setEnabled(false)", m_lastEditor.getSource(invocations.get(0)));
      assertEquals("panel.setEnabled(true)", m_lastEditor.getSource(invocations.get(1)));
    }
    // getMethodInvocation
    {
      assertNull(panel.getMethodInvocation("setVisible(boolean)"));
      {
        MethodInvocation invocation = panel.getMethodInvocation("setEnabled(boolean)");
        assertNotNull(invocation);
        assertEquals("setEnabled", invocation.getName().getIdentifier());
      }
    }
  }

  /**
   * Test for {@link JavaInfo#getMethodInvocations()}. For code like
   * <code>new Component().setFoo()</code> invocation <code>setFoo()</code> also should be added.
   */
  public void test_getMethodInvocations_all_emptyInvocation() throws Exception {
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "public class MyButton extends JButton {",
            "  public MyButton(Container container) {",
            "    container.add(this);",
            "  }",
            "  public void setFoo(boolean value) {",
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
    //
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    new MyButton(this).setFoo(false);",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    List<MethodInvocation> invocations = button.getMethodInvocations();
    assertThat(invocations).isNotEmpty();
  }

  public void test_removeMethodInvocations() throws Exception {
    JavaInfo panel =
        parseContainer(
            "class Test {",
            "  public static void main(String args[]) {",
            "    JPanel panel = new JPanel();",
            "    panel.setEnabled(false);",
            "    panel.setEnabled(true);",
            "  }",
            "}");
    panel.removeMethodInvocations("setEnabled(boolean)");
    assertEditor(
        "class Test {",
        "  public static void main(String args[]) {",
        "    JPanel panel = new JPanel();",
        "  }",
        "}");
  }

  /**
   * Test for {@link JavaInfo#addMethodInvocation(String, String, String[])}.
   */
  public void test_addMethodInvocation() throws Exception {
    JavaInfo panel =
        parseContainer(
            "public class Test {",
            "  public static void main(String args[]) {",
            "    JPanel panel = new JPanel();",
            "  }",
            "}");
    assertRelatedNodes(panel, new String[]{"new JPanel()"});
    //
    panel.addMethodInvocation("setSize(int,int)", "200, 100");
    assertEditor(
        "public class Test {",
        "  public static void main(String args[]) {",
        "    JPanel panel = new JPanel();",
        "    panel.setSize(200, 100);",
        "  }",
        "}");
    assertRelatedNodes(panel, new String[]{"new JPanel()", "panel.setSize(200, 100)"});
  }

  /**
   * Test for {@link JavaInfo#addMethodInvocation(StatementTarget, String, String)}.
   */
  public void test_addMethodInvocation_withTarget() throws Exception {
    JavaInfo panel =
        parseContainer(
            "public class Test {",
            "  public static void main(String args[]) {",
            "    JPanel panel = new JPanel();",
            "    panel.setEnabled(false);",
            "  }",
            "}");
    assertRelatedNodes(panel, new String[]{"new JPanel()", "panel.setEnabled(false)"});
    // prepare target
    StatementTarget target;
    {
      MethodInvocation invocation = panel.getMethodInvocation("setEnabled(boolean)");
      target = new StatementTarget(AstNodeUtils.getEnclosingStatement(invocation), false);
    }
    //
    panel.addMethodInvocation(target, "setSize(int,int)", "200, 100");
    assertEditor(
        "public class Test {",
        "  public static void main(String args[]) {",
        "    JPanel panel = new JPanel();",
        "    panel.setEnabled(false);",
        "    panel.setSize(200, 100);",
        "  }",
        "}");
    assertRelatedNodes(panel, new String[]{
        "new JPanel()",
        "panel.setEnabled(false)",
        "panel.setSize(200, 100)"});
  }

  /**
   * Test for {@link JavaInfo#addMethodInvocation(String, String, String[])}.
   */
  public void test_addMethodInvocation_this() throws Exception {
    JavaInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    assertRelatedNodes(panel, new String[]{});
    //
    panel.addMethodInvocation("setSize(int,int)", "200, 100");
    assertEditor(
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setSize(200, 100);",
        "  }",
        "}");
    assertRelatedNodes(panel, new String[]{"setSize(200, 100)"});
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Field assignment
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link JavaInfo#getFieldAssignments()}.
   */
  public void test_getAllFieldAssignments() throws Exception {
    createTypeDeclaration(
        "test",
        "MyButton.java",
        getTestSource(
            "public class MyButton extends JButton {",
            "  public int m_value1;",
            "  public int m_value2;",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    MyButton button = new MyButton();",
            "    button.m_value1 = 1;",
            "    button.m_value2 = 2;",
            "    add(button);",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // all assignments
    List<Assignment> assignments = button.getFieldAssignments();
    assertThat(assignments).hasSize(2);
    assertEquals("button.m_value1 = 1", m_lastEditor.getSource(assignments.get(0)));
    assertEquals("button.m_value2 = 2", m_lastEditor.getSource(assignments.get(1)));
  }

  /**
   * Test for {@link JavaInfo#getFieldAssignments(String)}.
   */
  public void test_getFieldAssignments() throws Exception {
    createTypeDeclaration(
        "test",
        "MyButton.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyButton extends JButton {",
            "  public int m_value;",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    MyButton button = new MyButton();",
            "    button.m_value = 1;",
            "    button.m_value = 2;",
            "    add(button);",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // no such field
    assertThat(button.getFieldAssignments("noSuchField")).isEmpty();
    // existing assignments
    {
      List<Assignment> assignments = button.getFieldAssignments("m_value");
      assertThat(assignments).hasSize(2);
      assertEquals("button.m_value = 1", m_lastEditor.getSource(assignments.get(0)));
      assertEquals("button.m_value = 2", m_lastEditor.getSource(assignments.get(1)));
    }
  }

  /**
   * Test for {@link JavaInfo#getFieldAssignments(String)}, using {@link FieldAccess}.
   */
  public void test_getFieldAssignments_FieldAccess() throws Exception {
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "public class MyButton extends JButton {",
            "  public int m_value;",
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
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    new MyButton(this).m_value = 1;",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // no such field
    assertThat(button.getFieldAssignments("noSuchField")).isEmpty();
    // existing assignment
    {
      List<Assignment> assignments = button.getFieldAssignments("m_value");
      assertThat(assignments).hasSize(1);
      assertEquals("new MyButton(this).m_value = 1", m_lastEditor.getSource(assignments.get(0)));
    }
  }

  /**
   * Test for {@link JavaInfo#getFieldAssignment(String)}.
   */
  public void test_getFieldAssignment() throws Exception {
    createTypeDeclaration(
        "test",
        "MyButton.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyButton extends JButton {",
            "  public int m_value;",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    MyButton button = new MyButton();",
            "    button.m_value = 1;",
            "    add(button);",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // no such field
    assertNull(button.getFieldAssignment("noSuchField"));
    // existing assignment
    {
      Assignment assignment = button.getFieldAssignment("m_value");
      assertEquals("button.m_value = 1", m_lastEditor.getSource(assignment));
    }
  }

  /**
   * Test for {@link JavaInfo#getFieldAssignment(String)}.<br>
   * Use {@link ParenthesizedExpression} as left hand side.
   */
  public void test_getFieldAssignment_ParenthesizedExpression() throws Exception {
    createTypeDeclaration(
        "test",
        "MyButton.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyButton extends JButton {",
            "  public int m_value;",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    MyButton button = new MyButton();",
            "    (button).m_value = 1;",
            "    add(button);",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // existing assignment
    {
      Assignment assignment = button.getFieldAssignment("m_value");
      assertNotNull(assignment);
      assertEquals("(button).m_value = 1", m_lastEditor.getSource(assignment));
    }
  }

  /**
   * Test for {@link JavaInfo#addFieldAssignment(String, String)}.
   */
  public void test_addFieldAssignment() throws Exception {
    createTypeDeclaration(
        "test",
        "MyButton.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyButton extends JButton {",
            "  public int m_value;",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    MyButton button = new MyButton();",
            "    add(button);",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // add assignment
    Assignment assignment = button.addFieldAssignment("m_value", "1");
    assertEquals("button.m_value = 1", m_lastEditor.getSource(assignment));
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    MyButton button = new MyButton();",
        "    button.m_value = 1;",
        "    add(button);",
        "  }",
        "}");
  }

  /**
   * Test for {@link JavaInfo#removeFieldAssignments(String)}.
   */
  public void test_removeFieldAssignments_normal() throws Exception {
    createTypeDeclaration(
        "test",
        "MyButton.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyButton extends JButton {",
            "  public int m_value;",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    MyButton button = new MyButton();",
            "    button.m_value = 1;",
            "    button.m_value = 2;",
            "    add(button);",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // remove assignments
    button.removeFieldAssignments("m_value");
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    MyButton button = new MyButton();",
        "    add(button);",
        "  }",
        "}");
  }

  /**
   * Test for {@link JavaInfo#removeFieldAssignments(String)}.
   */
  public void test_removeFieldAssignments_withDangling() throws Exception {
    createTypeDeclaration(
        "test",
        "MyButton.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyButton extends JButton {",
            "  public int m_value;",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    MyButton button = new MyButton();",
            "    button.m_value = button.m_value = 1;",
            "    add(button);",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // remove assignments
    button.removeFieldAssignments("m_value");
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    MyButton button = new MyButton();",
        "    add(button);",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Delete
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_delete_1() throws Exception {
    String[] lines =
        {
            "public class Test extends JPanel {",
            "  // filler filler filler",
            "  public Test() {",
            "    add(new JButton());",
            "  }",
            "}"};
    final ContainerInfo panel = parseContainer(lines);
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(new JButton())/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: javax.swing.JButton} {empty} {/add(new JButton())/}");
    final ComponentInfo button = panel.getChildrenComponents().get(0);
    // wait for expected broadcasts
    final boolean[] wasBefore = new boolean[]{false};
    final boolean[] wasAfter = new boolean[]{false};
    panel.addBroadcastListener(new ObjectInfoDelete() {
      @Override
      public void before(ObjectInfo parent, ObjectInfo child) throws Exception {
        assertSame(panel, parent);
        assertSame(button, child);
        assertTrue(button.isDeleting());
        assertFalse(button.isDeleted());
        wasBefore[0] = true;
      }

      @Override
      public void after(ObjectInfo parent, ObjectInfo child) throws Exception {
        assertSame(panel, parent);
        assertSame(button, child);
        assertTrue(button.isDeleting());
        assertTrue(button.isDeleted());
        wasAfter[0] = true;
      }
    });
    // do delete
    button.delete();
    assertFalse(button.isDeleting());
    assertTrue(button.isDeleted());
    assertEquals(
        getTestSource(
            "public class Test extends JPanel {",
            "  // filler filler filler",
            "  public Test() {",
            "  }",
            "}"),
        m_lastEditor.getSource());
    assertTrue(wasBefore[0]);
    assertTrue(wasAfter[0]);
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}");
  }

  /**
   * When we delete child and parent is created directly in association {@link Statement}, we should
   * materialize parent to prevent its removing with association.
   */
  public void test_delete_whenNoParentVariable() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class Test {",
            "  public static void main(String[] args) {",
            "    JButton button = new JButton();",
            "    new JPanel().add(button);",
            "  }",
            "}");
    assertHierarchy(
        "{new: javax.swing.JPanel} {empty} {/new JPanel().add(button)/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: javax.swing.JButton} {local-unique: button} {/new JButton()/ /new JPanel().add(button)/}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // do delete
    button.delete();
    assertEditor(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public class Test {",
        "  public static void main(String[] args) {",
        "    JPanel panel = new JPanel();",
        "  }",
        "}");
    assertHierarchy(
        "{new: javax.swing.JPanel} {local-unique: panel} {/new JPanel()/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}");
  }

  /**
   * Test that "false" from "canDelete" script disables delete.
   */
  public void test_canDelete_script_false() throws Exception {
    check_canDelete_script("false", false);
  }

  /**
   * Test that "true" from "canDelete" script enables delete.
   */
  public void test_canDelete_script_true() throws Exception {
    check_canDelete_script("true", true);
  }

  /**
   * Test that not boolean value from "canDelete" script disables delete.
   */
  public void test_canDelete_script_notBoolean() throws Exception {
    check_canDelete_script("42", false);
  }

  /**
   * Test that we can use "canDelete" script to disable delete.
   */
  private void check_canDelete_script(String script, boolean expected) throws Exception {
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyButton extends JButton {",
            "}"));
    setFileContentSrc(
        "test/MyButton.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <parameters>",
            "    <parameter name='canDelete'>" + script + "</parameter>",
            "  </parameters>",
            "</component>"));
    waitForAutoBuild();
    //
    parseContainer(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public class Test {",
        "  public static void main(String[] args) {",
        "    MyButton button = new MyButton();",
        "    new JPanel().add(button);",
        "  }",
        "}");
    ComponentInfo button = getJavaInfoByName("button");
    // ask canDelete()
    assertEquals(expected, button.canDelete());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // canBeRoot
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_canBeRoot() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    add(new Button('AWT button'));",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    assertTrue(panel.canBeRoot());
    assertFalse(button.canBeRoot());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // this.fieldName
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_this_fieldName() throws Exception {
    parseContainer(
        "class Test extends JPanel {",
        "  private JButton button;",
        "  Test() {",
        "    this.button = new JButton();",
        "    add(this.button);",
        "  }",
        "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/this/ /add(this.button)/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: javax.swing.JButton} {field-unique: button} {/new JButton()/ /add(this.button)/}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * When we use component, we can not see its <code>protected</code> properties.
   */
  public void test_protectedProperty_use() throws Exception {
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "public class MyButton extends JButton {",
            "  public MyButton() {",
            "  }",
            "  protected void setFoo(int value) {",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    add(new MyButton());",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    assertThat(button.getPropertyByTitle("foo")).isNull();
  }

  /**
   * When we inherit from component, we can see its <code>protected</code> properties.
   */
  public void test_protectedProperty_this() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  public MyPanel() {",
            "  }",
            "  protected void setFoo(int value) {",
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
    assertThat(panel.getPropertyByTitle("foo")).isNotNull();
  }

  /**
   * Test for {@link JavaInfo#getPropertyByTitle(String)}.
   */
  public void test_getPropertyByTitle() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    add(new JButton());",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    assertNotNull(button.getPropertyByTitle("text"));
    assertNull(button.getPropertyByTitle("no such property"));
  }

  /**
   * {@link ObjectInfo#getProperties()} should use broadcast
   * {@link ObjectInfoAllProperties#invoke(ObjectInfo, List)}.
   */
  public void test_getProperties_allProperties() throws Exception {
    final ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    // initially "panel" has properties
    assertThat(panel.getProperties()).isNotEmpty();
    // add allProperties() listener
    panel.addBroadcastListener(new ObjectInfoAllProperties() {
      public void invoke(ObjectInfo object, List<Property> properties) throws Exception {
        if (object == panel) {
          properties.clear();
        }
      }
    });
    assertThat(panel.getProperties()).isEmpty();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // "Text" property as title decorator
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_textInComponentTitle() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    add(new JButton());",
            "    add(new JButton('theText'));",
            "  }",
            "}");
    panel.refresh();
    // prepare buttons
    ComponentInfo button_1 = panel.getChildrenComponents().get(0);
    ComponentInfo button_2 = panel.getChildrenComponents().get(1);
    // do checks
    assertEquals("(no variable)", ObjectsLabelProvider.INSTANCE.getText(button_1));
    assertEquals("(no variable) - \"theText\"", ObjectsLabelProvider.INSTANCE.getText(button_2));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // JavaInfo visibility in tree/GEF
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for using <code>"visible"</code>, <code>"visible.inTree"</code>,
   * <code>"visible.inGraphical"</code>.
   */
  public void test_visibility() throws Exception {
    // create fixture
    createModelType(
        "test",
        "MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  public MyPanel() {",
            "    add(getA());",
            "    add(getB());",
            "    add(getC());",
            "    add(getD());",
            "  }",
            "  private final JButton m_A = new JButton();",
            "  private final JButton m_B = new JButton();",
            "  private final JButton m_C = new JButton();",
            "  private final JButton m_D = new JButton();",
            "  public JButton getA() {",
            "    return m_A;",
            "  }",
            "  public JButton getB() {",
            "    return m_B;",
            "  }",
            "  public JButton getC() {",
            "    return m_C;",
            "  }",
            "  public JButton getD() {",
            "    return m_D;",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyPanel.getA.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <parameters>",
            "    <parameter name='visible.inTree'>true</parameter>",
            "    <parameter name='visible.inGraphical'>true</parameter>",
            "  </parameters>",
            "</component>"));
    setFileContentSrc(
        "test/MyPanel.getB.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <parameters>",
            "    <parameter name='visible.inTree'>false</parameter>",
            "  </parameters>",
            "</component>"));
    setFileContentSrc(
        "test/MyPanel.getC.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <parameters>",
            "    <parameter name='visible.inGraphical'>false</parameter>",
            "  </parameters>",
            "</component>"));
    setFileContentSrc(
        "test/MyPanel.getD.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <parameters>",
            "    <parameter name='visible'>false</parameter>",
            "  </parameters>",
            "</component>"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends MyPanel {",
            "  public Test() {",
            "  }",
            "}");
    assertHierarchy(
        "{this: test.MyPanel} {this} {}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {method: public javax.swing.JButton test.MyPanel.getA()} {property} {}",
        "  {method: public javax.swing.JButton test.MyPanel.getB()} {property} {}",
        "  {method: public javax.swing.JButton test.MyPanel.getC()} {property} {}",
        "  {method: public javax.swing.JButton test.MyPanel.getD()} {property} {}");
    List<ComponentInfo> childrenComponents = panel.getChildrenComponents();
    ComponentInfo buttonA = childrenComponents.get(0);
    ComponentInfo buttonB = childrenComponents.get(1);
    ComponentInfo buttonC = childrenComponents.get(2);
    ComponentInfo buttonD = childrenComponents.get(3);
    // check visibility
    assertThat(childrenComponents).contains(buttonA, buttonB, buttonC, buttonD);
    assertThat(panel.getPresentation().getChildrenTree()).containsOnly(buttonA, buttonC);
    assertThat(panel.getPresentation().getChildrenGraphical()).containsOnly(buttonA, buttonB);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // setObject()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test that {@link JavaInfo#setObject(Object)} sends notification that is used by
   * {@link FieldAccessor} to fetch default value, but only first time during refresh().
   */
  public void test_setObject_FieldAccessor_notification() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyPanel extends JPanel {",
            "  public int foo = 1;",
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
    panel.refresh();
    Property fooProperty = panel.getPropertyByTitle("foo");
    // default value is "1"
    assertEquals(1, fooProperty.getValue());
    // set new Object, with updated "foo"
    {
      Object panelObject = panel.getObject();
      ReflectionUtils.setField(panelObject, "foo", 2);
      panel.setObject(panelObject);
    }
    // default value is still "1", because we notify FieldAccessor only first time in refresh()
    assertEquals(1, fooProperty.getValue());
  }

  /**
   * Test that {@link JavaInfo#setObject(Object)} sends notification that is used by
   * {@link SetterAccessor} to fetch default value, but only first time during refresh().
   */
  public void test_setObject_SetterAccessor_notification() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  private int foo = 1;",
            "  public int getFoo() {",
            "    return foo;",
            "  }",
            "  public void setFoo(int foo) {",
            "    this.foo = foo;",
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
    panel.refresh();
    Property fooProperty = panel.getPropertyByTitle("foo");
    // default value is "1"
    assertEquals(1, fooProperty.getValue());
    // set new Object, with updated "foo"
    {
      Object panelObject = panel.getObject();
      ReflectionUtils.invokeMethod(panelObject, "setFoo(int)", 2);
      panel.setObject(panelObject);
    }
    // default value is still "1", because we notify SetterAccessor only first time in refresh()
    assertEquals(1, fooProperty.getValue());
  }

  /**
   * Test that "object ready" notification is sent when allowed by MVEL script.
   * <p>
   * In this case we allow when there is at least two components on our {@link JPanel}.
   */
  public void test_setObject_objectReadyValidator() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  public int getFoo() {",
            "    return getComponentCount();",
            "  }",
            "  public void setFoo(int foo) {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <parameters>",
            "    <parameter name='objectReadyValidator'>object.componentCount >= 2</parameter>",
            "  </parameters>",
            "</component>"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends MyPanel {",
            "  public Test() {",
            "    {",
            "      JButton button_1 = new JButton();",
            "      add(button_1);",
            "    }",
            "    {",
            "      JButton button_2 = new JButton();",
            "      add(button_2);",
            "    }",
            "    {",
            "      JButton button_3 = new JButton();",
            "      add(button_3);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    Property fooProperty = panel.getPropertyByTitle("foo");
    // default value is "2"
    assertEquals(2, fooProperty.getValue());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getChildByObject()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link JavaInfo#getChildByObject(Object)}.
   */
  public void test_getChildByObject() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    add(new JButton());",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    panel.refresh();
    // check
    assertSame(panel, panel.getChildByObject(panel.getObject()));
    assertSame(button, panel.getChildByObject(button.getObject()));
    assertNull(panel.getChildByObject(this));
    assertNull(panel.getChildByObject(null));
  }

  /**
   * Test for {@link JavaInfo#getChildByObject(Object)}.
   */
  public void test_getChildByObject_absoluteLayout() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    setLayout(null);",
            "  }",
            "}");
    panel.refresh();
    // check
    assertSame(panel, panel.getChildByObject(panel.getObject()));
    assertNull(panel.getChildByObject(null));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // "refresh_afterCreate" script
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test that during "refresh" the script "refresh_afterCreate" is executed.
   */
  public void test_refresh_afterCreate_script() throws Exception {
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "// filler filler filler",
            "public class MyButton extends JButton {",
            "}"));
    setFileContentSrc(
        "test/MyButton.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <parameters>",
            "    <parameter name='refresh_afterCreate'>object.setEnabled(false)</parameter>",
            "  </parameters>",
            "</component>"));
    waitForAutoBuild();
    //
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      MyButton button = new MyButton();",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    //
    ComponentInfo button = getJavaInfoByName("button");
    Component buttonObject = (Component) button.getObject();
    assertFalse(buttonObject.isEnabled());
  }

  /**
   * Test that for placeholder the script "refresh_afterCreate" is not executed.
   */
  public void test_refresh_afterCreate_script_placeholder() throws Exception {
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "// filler filler filler",
            "public class MyButton extends JButton {",
            "  public MyButton() {",
            "    throw new IllegalStateException('actual');",
            "  }",
            "  public void foo() {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyButton.wbp-component.xml",
        getSource(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <parameters>",
            "    <parameter name='refresh_afterCreate'>System.out.println(0); object.foo(); System.out.println(1);</parameter>",
            "  </parameters>",
            "</component>"));
    waitForAutoBuild();
    //
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      MyButton button = new MyButton();",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    //
    ComponentInfo button = getJavaInfoByName("button");
    assertTrue(button.isPlaceholder());
  }
}
