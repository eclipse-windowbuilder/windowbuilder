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

import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.variable.ExposedPropertyVariableSupport;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.BorderLayoutInfo;

import org.eclipse.jdt.core.dom.Statement;

import java.awt.Component;

import javax.swing.JTextField;

/**
 * Test for {@link ExposedPropertyVariableSupport}.
 *
 * @author scheglov_ke
 */
public class ExposedPropertyTest extends AbstractVariableTest {
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
    ContainerInfo frame =
        parseContainer(
            "// filler filler filler",
            "public final class Test extends JFrame {",
            "  public Test() {",
            "  }",
            "}");
    ContainerInfo contentPane = (ContainerInfo) frame.getChildrenComponents().get(0);
    //
    ExposedPropertyVariableSupport variableSupport =
        (ExposedPropertyVariableSupport) contentPane.getVariableSupport();
    assertEquals("property", variableSupport.toString());
    assertEquals("getContentPane()", variableSupport.getTitle());
    // we can request expression
    assertTrue(variableSupport.hasExpression(null));
    // expressions
    {
      NodeTarget target = getNodeBlockTarget(frame, true);
      assertEquals("getContentPane()", variableSupport.getReferenceExpression(target));
      assertEquals("getContentPane().", variableSupport.getAccessExpression(target));
    }
    // component name
    assertEquals("thisContentPane", variableSupport.getComponentName());
    // name
    assertFalse(variableSupport.hasName());
    try {
      variableSupport.getName();
      fail();
    } catch (IllegalStateException e) {
    }
    try {
      variableSupport.setName("foo");
      fail();
    } catch (IllegalStateException e) {
    }
    // local -> field
    assertFalse(variableSupport.canConvertLocalToField());
    try {
      variableSupport.convertLocalToField();
      fail();
    } catch (IllegalStateException e) {
    }
    // field -> local
    assertFalse(variableSupport.canConvertFieldToLocal());
    try {
      variableSupport.convertFieldToLocal();
      fail();
    } catch (IllegalStateException e) {
    }
    // target
    {
      StatementTarget target = variableSupport.getStatementTarget();
      StatementTarget frameTarget = frame.getVariableSupport().getStatementTarget();
      assertSame(frameTarget.getBlock(), target.getBlock());
      assertSame(frameTarget.getStatement(), target.getStatement());
      assertEquals(frameTarget.isBefore(), target.isBefore());
    }
  }

  public void test_getChildTarget() throws Exception {
    ContainerInfo frame =
        parseContainer(
            "public final class Test extends JFrame {",
            "  public Test() {",
            "    setEnabled(false);",
            "  }",
            "}");
    ContainerInfo contentPane = (ContainerInfo) frame.getChildrenComponents().get(0);
    VariableSupport variableSupport = contentPane.getVariableSupport();
    // target
    {
      StatementTarget target = variableSupport.getChildTarget();
      assertEquals(JavaInfoUtils.getTarget(frame, null).toString(), target.toString());
    }
  }

  /**
   * Test that we add new component after last {@link Statement} of contentPane.
   */
  public void test_addButton() throws Exception {
    ContainerInfo frame =
        parseContainer(
            "public final class Test extends JFrame {",
            "  public Test() {",
            "    getContentPane().setEnabled(true);",
            "  }",
            "}");
    ContainerInfo contentPane = (ContainerInfo) frame.getChildrenComponents().get(0);
    BorderLayoutInfo layout = (BorderLayoutInfo) contentPane.getLayout();
    //
    ComponentInfo button = createJButton();
    layout.command_CREATE(button, java.awt.BorderLayout.NORTH);
    assertEditor(
        "public final class Test extends JFrame {",
        "  public Test() {",
        "    getContentPane().setEnabled(true);",
        "    {",
        "      JButton button = new JButton();",
        "      getContentPane().add(button, BorderLayout.NORTH);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getStatementTarget()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ExposedPropertyVariableSupport#getStatementTarget()}.
   * <p>
   * No related nodes, so target is determined by "this", i.e. in constructor.
   */
  public void test_getStatementTarget_noRelatedNodes() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  private JButton m_button;",
            "  public MyPanel() {",
            "    add(getButton());",
            "  }",
            "  public JButton getButton() {",
            "    if (m_button == null) {",
            "      m_button = new JButton();",
            "    }",
            "    return m_button;",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends MyPanel {",
            "  public Test() {",
            "    setEnabled(false);",
            "  }",
            "}");
    ComponentInfo button = getJavaInfoByName("getButton()");
    // target = begin of constructor
    {
      StatementTarget target = button.getVariableSupport().getStatementTarget();
      assertTarget(target, getBlock(panel), null, true);
    }
  }

  /**
   * Test for {@link ExposedPropertyVariableSupport#getStatementTarget()}.
   * <p>
   * Related statement in constructor, target = before it.
   */
  public void test_getStatementTarget_hasRelatedStatement() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  private JButton m_button;",
            "  public MyPanel() {",
            "    add(getButton());",
            "  }",
            "  public JButton getButton() {",
            "    if (m_button == null) {",
            "      m_button = new JButton();",
            "    }",
            "    return m_button;",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends MyPanel {",
            "  public Test() {",
            "    setEnabled(false);",
            "    getButton().setBorderPainted(true);",
            "    getButton().setAutoscrolls(true);",
            "  }",
            "  protected void configureButton() {",
            "  }",
            "}");
    ComponentInfo button = getJavaInfoByName("getButton()");
    // target = begin of constructor
    {
      StatementTarget target = button.getVariableSupport().getStatementTarget();
      assertTarget(target, getBlock(panel), null, true);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Special cases
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Getter returns <code>null</code>, so nothing to expose.
   */
  public void test_nullComponent() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  public JButton getButton() {",
            "    return null;",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    parseContainer(
        "// filler filler filler",
        "public class Test extends MyPanel {",
        "  public Test() {",
        "  }",
        "}");
    assertHierarchy(
        "{this: test.MyPanel} {this} {}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}");
  }

  /**
   * Superclass has method that returns some {@link Component}, however this component is not bound
   * to parent object. So, such method should be ignored.
   */
  public void test_ignoreDisconnectedComponent() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  private JButton m_button;",
            "  public JButton getButton() {",
            "    if (m_button == null) {",
            "      m_button = new JButton();",
            "    }",
            "    return m_button;",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    parseContainer(
        "// filler filler filler",
        "public class Test extends MyPanel {",
        "  public Test() {",
        "  }",
        "}");
    assertHierarchy(
        "{this: test.MyPanel} {this} {}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}");
  }

  /**
   * In contrast to {@link #test_ignoreDisconnectedComponent()}, here we want to force exposed
   * component association, even if it is not connected to host. For example we need this for
   * exposing {@link JTextField#getDocument()}.
   */
  public void test_forceDisconnectedComponent() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  private JButton m_button;",
            "  public JButton getButton() {",
            "    if (m_button == null) {",
            "      m_button = new JButton();",
            "    }",
            "    return m_button;",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <methods>",
            "    <method name='getButton'>",
            "      <tag name='exposeDisconnectedComponent' value='true'/>",
            "    </method>",
            "  </methods>",
            "</component>"));
    waitForAutoBuild();
    // parse
    parseContainer(
        "// filler filler filler",
        "public class Test extends MyPanel {",
        "  public Test() {",
        "  }",
        "}");
    assertHierarchy(
        "{this: test.MyPanel} {this} {}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {method: public javax.swing.JButton test.MyPanel.getButton()} {property} {}");
  }
}
