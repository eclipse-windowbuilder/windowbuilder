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
import org.eclipse.wb.core.model.association.AssociationObjects;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.generation.statement.PureFlatStatementGenerator;
import org.eclipse.wb.internal.core.model.order.MethodOrderAfterAssociation;
import org.eclipse.wb.internal.core.model.order.MethodOrderAfterCreation;
import org.eclipse.wb.internal.core.model.variable.EmptyInvocationVariableSupport;
import org.eclipse.wb.internal.core.model.variable.EmptyVariableSupport;
import org.eclipse.wb.internal.core.model.variable.FieldInitializerVariableSupport;
import org.eclipse.wb.internal.core.model.variable.FieldUniqueVariableSupport;
import org.eclipse.wb.internal.core.model.variable.LocalUniqueVariableSupport;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.FlowLayoutInfo;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.Statement;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.Container;

/**
 * Test for {@link EmptyVariableSupport}.
 * 
 * @author scheglov_ke
 */
public class EmptyTest extends AbstractVariableTest {
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
            "public final class Test extends JPanel {",
            "  public Test() {",
            "    add(new JButton('button'));",
            "  }",
            "}");
    assertThat(panel.getChildrenComponents()).hasSize(1);
    // 
    JavaInfo button = panel.getChildrenComponents().get(0);
    EmptyVariableSupport variableSupport = (EmptyVariableSupport) button.getVariableSupport();
    // basic checks
    assertEquals("empty", variableSupport.toString());
    assertEquals("(no variable)", variableSupport.getTitle());
    assertTrue(variableSupport.isDefault());
    assertEquals(
        "new JButton(\"button\")",
        m_lastEditor.getSource(variableSupport.getInitializer()));
    // name
    assertTrue(variableSupport.hasName());
    assertNull(variableSupport.getName());
    // conversion
    assertTrue(variableSupport.canConvertLocalToField());
    assertTrue(variableSupport.canConvertFieldToLocal());
  }

  public void test_setName() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public final class Test extends JPanel {",
            "  public Test() {",
            "    add(new JButton('button'));",
            "  }",
            "}");
    JavaInfo button = panel.getChildrenComponents().get(0);
    VariableSupport variableSupport = button.getVariableSupport();
    // 
    variableSupport.setName("abc");
    assertTrue(button.getVariableSupport() instanceof LocalUniqueVariableSupport);
    assertEditor(
        "public final class Test extends JPanel {",
        "  public Test() {",
        "    JButton abc = new JButton('button');",
        "    add(abc);",
        "  }",
        "}");
    // two related nodes expected - for initializer and use place
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(abc)/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: javax.swing.JButton} {local-unique: abc} {/new JButton('button')/ /add(abc)/}");
  }

  public void test_getAccessExpression() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    add(new JButton());",
            "  }",
            "}");
    JavaInfo button = panel.getChildrenComponents().get(0);
    VariableSupport variableSupport = button.getVariableSupport();
    // we can request  expression
    assertTrue(variableSupport.hasExpression(null));
    // ask for expression - convert to local
    NodeTarget target = getNodeStatementTarget(panel, false, 0);
    assertEquals("button.", variableSupport.getAccessExpression(target));
    assertTrue(button.getVariableSupport() instanceof LocalUniqueVariableSupport);
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    JButton button = new JButton();",
        "    add(button);",
        "  }",
        "}");
  }

  public void test_toLocal() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public final class Test extends JPanel {",
            "  public Test() {",
            "    add(new JButton('button'));",
            "  }",
            "}");
    JavaInfo button = panel.getChildrenComponents().get(0);
    VariableSupport variableSupport = button.getVariableSupport();
    // 
    variableSupport.convertFieldToLocal();
    assertTrue(button.getVariableSupport() instanceof LocalUniqueVariableSupport);
    assertEditor(
        "public final class Test extends JPanel {",
        "  public Test() {",
        "    JButton button = new JButton('button');",
        "    add(button);",
        "  }",
        "}");
  }

  public void test_toField() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public final class Test extends JPanel {",
            "  public Test() {",
            "    add(new JButton('button'));",
            "  }",
            "}");
    JavaInfo button = panel.getChildrenComponents().get(0);
    VariableSupport variableSupport = button.getVariableSupport();
    // 
    variableSupport.convertLocalToField();
    assertTrue(button.getVariableSupport() instanceof FieldUniqueVariableSupport);
    assertEditor(
        "public final class Test extends JPanel {",
        "  private JButton button;",
        "  public Test() {",
        "    button = new JButton('button');",
        "    add(button);",
        "  }",
        "}");
  }

  /**
   * Test for {@link EmptyVariableSupport#materialize()}, in particular
   * {@link JavaEventListener#variable_emptyMaterializeBefore(EmptyVariableSupport)}.
   */
  public void test_materialize_wasInStatement() throws Exception {
    prepare_genericButton();
    ContainerInfo panel =
        parseContainer(
            "public final class Test extends JPanel {",
            "  public Test() {",
            "    add(new Button<String>());",
            "  }",
            "}");
    final ComponentInfo button = panel.getChildrenComponents().get(0);
    final EmptyVariableSupport variableSupport = (EmptyVariableSupport) button.getVariableSupport();
    // set "materialize" listener
    final boolean[] broadcastNotified = new boolean[]{false};
    panel.addBroadcastListener(new JavaEventListener() {
      @Override
      public void variable_emptyMaterializeBefore(EmptyVariableSupport _variableSupport)
          throws Exception {
        assertSame(variableSupport, _variableSupport);
        broadcastNotified[0] = true;
      }
    });
    // do materialize
    variableSupport.materialize();
    assertTrue(broadcastNotified[0]);
    assertEditor(
        "public final class Test extends JPanel {",
        "  public Test() {",
        "    Button<String> button = new Button<String>();",
        "    add(button);",
        "  }",
        "}");
    assertInstanceOf(LocalUniqueVariableSupport.class, button.getVariableSupport());
  }

  /**
   * Test for {@link EmptyVariableSupport#materialize()}, when enclosing {@link ASTNode} is
   * {@link FieldDeclaration}, not statement.
   */
  public void test_materialize_wasInField() throws Exception {
    prepare_genericButton();
    parseContainer(
        "public final class Test extends JPanel {",
        "  private JScrollPane panel = new JScrollPane(new Button<String>());",
        "  public Test() {",
        "    add(panel);",
        "  }",
        "}");
    ContainerInfo panel = getJavaInfoByName("panel");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    final EmptyVariableSupport variableSupport = (EmptyVariableSupport) button.getVariableSupport();
    // set "materialize" listener
    final boolean[] broadcastNotified = new boolean[]{false};
    panel.addBroadcastListener(new JavaEventListener() {
      @Override
      public void variable_emptyMaterializeBefore(EmptyVariableSupport _variableSupport)
          throws Exception {
        assertSame(variableSupport, _variableSupport);
        broadcastNotified[0] = true;
      }
    });
    // do materialize
    variableSupport.materialize();
    assertTrue(broadcastNotified[0]);
    assertEditor(
        "public final class Test extends JPanel {",
        "  private Button<String> button = new Button<String>();",
        "  private JScrollPane panel = new JScrollPane(button);",
        "  public Test() {",
        "    add(panel);",
        "  }",
        "}");
    assertInstanceOf(FieldInitializerVariableSupport.class, button.getVariableSupport());
  }

  /**
   * Test for {@link EmptyVariableSupport#materialize()}.<br>
   * Enclosing {@link ParenthesizedExpression} should be removed at the end.
   */
  public void test_materialize_removeParenthesizedExpression() throws Exception {
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
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    (new MyButton(this)).setEnabled(false);",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // do materialize
    EmptyVariableSupport variableSupport = (EmptyVariableSupport) button.getVariableSupport();
    variableSupport.materialize();
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    MyButton myButton = new MyButton(this);",
        "    myButton.setEnabled(false);",
        "  }",
        "}");
    assertInstanceOf(LocalUniqueVariableSupport.class, button.getVariableSupport());
  }

  public void test_target() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public final class Test extends JPanel {",
            "  public Test() {",
            "    add(new JButton('button'));",
            "  }",
            "}");
    JavaInfo button = panel.getChildrenComponents().get(0);
    VariableSupport variableSupport = button.getVariableSupport();
    // check target
    {
      StatementTarget target = variableSupport.getStatementTarget();
      assertTarget(target, null, getStatement(panel, 0), false);
    }
    // check state
    assertEditor(
        "public final class Test extends JPanel {",
        "  public Test() {",
        "    JButton button = new JButton('button');",
        "    add(button);",
        "  }",
        "}");
    assertInstanceOf(LocalUniqueVariableSupport.class, button.getVariableSupport());
  }

  /**
   * Test that {@link EmptyVariableSupport} implements method
   * {@link VariableSupport#ensureInstanceReadyAt(StatementTarget)}.
   */
  public void test_ensureInstanceReadyAt() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    add(new JButton('1'));",
            "    add(new JButton('2'));",
            "  }",
            "}");
    panel.refresh();
    FlowLayoutInfo flowLayout = (FlowLayoutInfo) panel.getLayout();
    ComponentInfo button_1 = panel.getChildrenComponents().get(0);
    ComponentInfo button_2 = panel.getChildrenComponents().get(1);
    // do move
    flowLayout.move(button_2, button_1);
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    JButton button = new JButton('2');",
        "    add(button);",
        "    add(new JButton('1'));",
        "  }",
        "}");
  }

  /**
   * Test adding new component with {@link EmptyVariableSupport}.
   */
  public void test_add() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    // add new JButton
    ComponentInfo button = createJButton();
    JavaInfoUtils.add(
        button,
        new EmptyInvocationVariableSupport(button, "%parent%.add(%child%)", 0),
        PureFlatStatementGenerator.INSTANCE,
        AssociationObjects.invocationChildNull(),
        panel,
        null);
    // check
    assertEditor(
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    add(new JButton());",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Special cases
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for SWT-like component, that has parent in constructor, so can be associated with parent
   * without any outer {@link Expression}, like {@link Container#add(java.awt.Component)}. So, we
   * should materialize such component "in place", without separate {@link Statement}.
   */
  public void test_materialize_noOuterExpression() throws Exception {
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "public class MyButton<T> extends JButton {",
            "  public MyButton(Container container) {",
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
            "    new MyButton<String>(this);",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // materialize, note that target is updated
    {
      NodeTarget target = getNodeStatementTarget(panel, false, 0);
      StatementTarget statementTarget = target.getStatementTarget();
      // old target statement
      Statement targetStatement_old;
      {
        assertFalse(statementTarget.isBefore());
        targetStatement_old = statementTarget.getStatement();
        assertEquals("new MyButton<String>(this);", m_lastEditor.getSource(targetStatement_old));
      }
      // reference expression
      assertEquals("myButton", button.getVariableSupport().getReferenceExpression(target));
      // new target statement
      {
        assertFalse(statementTarget.isBefore());
        Statement targetStatement_new = statementTarget.getStatement();
        assertNotSame(targetStatement_old, targetStatement_new);
        assertEquals(
            "MyButton<String> myButton = new MyButton<String>(this);",
            m_lastEditor.getSource(targetStatement_new));
      }
    }
    // now LocalUniqueVariableSupport
    assertInstanceOf(LocalUniqueVariableSupport.class, button.getVariableSupport());
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    MyButton<String> myButton = new MyButton<String>(this);",
        "  }",
        "}");
  }

  /**
   * Test for SWT-like component, that has parent in constructor, so can be associated with parent
   * without any outer {@link Expression}, like {@link Container#add(java.awt.Component)}.<br>
   * When we move such component, we don't need to materialize its variable.
   */
  public void test_move_noOuterExpression() throws Exception {
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "public class MyButton extends JButton {",
            "  public MyButton(Container container, int value) {",
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
            "      <parameter type='int'/>",
            "    </constructor>",
            "  </constructors>",
            "</component>"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    new MyButton(this, 0);",
            "    new MyButton(this, 1);",
            "  }",
            "}");
    FlowLayoutInfo flowLayout = (FlowLayoutInfo) panel.getLayout();
    ComponentInfo button_0 = panel.getChildrenComponents().get(0);
    ComponentInfo button_1 = panel.getChildrenComponents().get(1);
    // move
    flowLayout.move(button_1, button_0);
    assertInstanceOf(EmptyVariableSupport.class, button_1.getVariableSupport());
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    new MyButton(this, 1);",
        "    new MyButton(this, 0);",
        "  }",
        "}");
  }

  public void test_returnInitializer_setProperty() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    add(createButton());",
            "  }",
            "  private JButton createButton() {",
            "    return new JButton();",
            "  }",
            "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(createButton())/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: javax.swing.JButton} {empty} {/new JButton()/ /add(createButton())/}");
    // set property for "JButton"
    ComponentInfo button = panel.getChildrenComponents().get(0);
    button.getPropertyByTitle("text").setValue("text");
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    add(createButton());",
        "  }",
        "  private JButton createButton() {",
        "    JButton button = new JButton();",
        "    button.setText('text');",
        "    return button;",
        "  }",
        "}");
  }

  public void test_returnInitializer_addChild() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    add(createPanel());",
            "  }",
            "  private JPanel createPanel() {",
            "    return new JPanel();",
            "  }",
            "}");
    ContainerInfo subPanel = (ContainerInfo) panel.getChildrenComponents().get(0);
    FlowLayoutInfo flowLayout = (FlowLayoutInfo) subPanel.getLayout();
    // add new JButton
    ComponentInfo button = createJButton();
    flowLayout.add(button, null);
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    add(createPanel());",
        "  }",
        "  private JPanel createPanel() {",
        "    JPanel panel = new JPanel();",
        "    {",
        "      JButton button = new JButton();",
        "      panel.add(button);",
        "    }",
        "    return panel;",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Set property
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for SWT-like component, that has parent in constructor, so can be associated with parent
   * without any outer {@link Expression}, like {@link Container#add(java.awt.Component)}. So, we
   * should materialize such component "in place", without separate {@link Statement}.
   * <p>
   * Use {@link MethodOrderAfterCreation}.
   */
  public void test_noOuterExpression_setProperty_afterCreation() throws Exception {
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "public class MyButton extends JButton {",
            "  public MyButton(Container container) {",
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
            "  <!-- METHODS ORDER -->",
            "  <method-order>",
            "    <default order='afterCreation'/>",
            "  </method-order>",
            "</component>"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    new MyButton(this);",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // set "text" property
    button.getPropertyByTitle("text").setValue("Some text");
    assertInstanceOf(LocalUniqueVariableSupport.class, button.getVariableSupport());
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    MyButton myButton = new MyButton(this);",
        "    myButton.setText('Some text');",
        "  }",
        "}");
  }

  /**
   * Test for SWT-like component, that has parent in constructor, so can be associated with parent
   * without any outer {@link Expression}, like {@link Container#add(java.awt.Component)}. So, we
   * should materialize such component "in place", without separate {@link Statement}.
   * <p>
   * Use {@link MethodOrderAfterAssociation}.
   */
  public void test_noOuterExpression_setProperty_afterAssociation() throws Exception {
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "public class MyButton extends JButton {",
            "  public MyButton(Container container) {",
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
            "  <!-- METHODS ORDER -->",
            "  <method-order>",
            "    <default order='afterAssociation'/>",
            "  </method-order>",
            "</component>"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    new MyButton(this);",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // set "text" property
    button.getPropertyByTitle("text").setValue("Some text");
    assertInstanceOf(LocalUniqueVariableSupport.class, button.getVariableSupport());
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    MyButton myButton = new MyButton(this);",
        "    myButton.setText('Some text');",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Move
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for case when component created with {@link EmptyVariableSupport} has children. We to this
   * using trick with <code>"implicit factory"</code>. We test that children of such component are
   * also moved.
   */
  public void test_moveWithChildren() throws Exception {
    setFileContentSrc(
        "test/MyMenuItem.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyMenuItem extends JPanel {",
            "}"));
    setFileContentSrc(
        "test/MyMenuItem.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <parameters>",
            "    <parameter name='layout.has'>false</parameter>",
            "  </parameters>",
            "</component>"));
    setFileContentSrc(
        "test/MyMenuBar.java",
        getTestSource(
            "public class MyMenuBar extends JPanel {",
            "  public MyMenuItem addItem(String text, Component content) {",
            "    MyMenuItem item = new MyMenuItem();",
            "    add(item);",
            "    item.add(content);",
            "    return item;",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyMenuBar.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <!-- METHODS -->",
            "  <methods>",
            "    <method name='addItem'>",
            "      <parameter type='java.lang.String'/>",
            "      <parameter type='java.awt.Component'>",
            "        <tag name='implicitFactory.child' value='true'/>",
            "      </parameter>",
            "      <tag name='implicitFactory' value='true'/>",
            "    </method>",
            "  </methods>",
            "</component>"));
    waitForAutoBuild();
    // parse
    ContainerInfo menuBar =
        parseContainer(
            "public class Test extends MyMenuBar {",
            "  public Test() {",
            "    {",
            "      JButton button_1 = new JButton();",
            "      addItem('item_1', button_1);",
            "    }",
            "    {",
            "      JButton button_2 = new JButton();",
            "      addItem('item_2', button_2);",
            "    }",
            "  }",
            "}");
    assertHierarchy(
        "{this: test.MyMenuBar} {this} {/addItem('item_1', button_1)/ /addItem('item_2', button_2)/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {implicit-factory} {empty} {/addItem('item_1', button_1)/}",
        "    {new: javax.swing.JButton} {local-unique: button_1} {/new JButton()/ /addItem('item_1', button_1)/}",
        "  {implicit-factory} {empty} {/addItem('item_2', button_2)/}",
        "    {new: javax.swing.JButton} {local-unique: button_2} {/new JButton()/ /addItem('item_2', button_2)/}");
    ContainerInfo item_1 = (ContainerInfo) menuBar.getChildrenComponents().get(0);
    ContainerInfo item_2 = (ContainerInfo) menuBar.getChildrenComponents().get(1);
    FlowLayoutInfo menuBarLayout = (FlowLayoutInfo) menuBar.getLayout();
    // move "item_2" before "item_1", should be moved with children
    menuBarLayout.move(item_2, item_1);
    assertEditor(
        "public class Test extends MyMenuBar {",
        "  public Test() {",
        "    {",
        "      JButton button_2 = new JButton();",
        "      MyMenuItem myMenuItem = addItem('item_2', button_2);",
        "    }",
        "    {",
        "      JButton button_1 = new JButton();",
        "      addItem('item_1', button_1);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Prepare component <code>Button</code> with type parameters.
   */
  private void prepare_genericButton() throws Exception {
    setFileContentSrc(
        "test/Button.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class Button<T> extends JButton {",
            "}"));
    setFileContentSrc(
        "test/Button.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <creation>",
            "    <source><![CDATA[new test.Button<%keyType%>()]]></source>",
            "  </creation>",
            "</component>"));
    waitForAutoBuild();
  }
}
