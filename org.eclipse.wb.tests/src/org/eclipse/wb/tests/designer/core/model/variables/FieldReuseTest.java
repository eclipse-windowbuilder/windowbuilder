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

import org.eclipse.wb.internal.core.model.variable.FieldReuseVariableSupport;
import org.eclipse.wb.internal.core.model.variable.FieldUniqueVariableSupport;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for {@link FieldReuseVariableSupport}.
 * 
 * @author scheglov_ke
 */
public class FieldReuseTest extends AbstractVariableTest {
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
  // getReferenceExpression()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getReferenceExpression_local() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  private JButton button;",
            "  Test() {",
            "    {",
            "      button = new JButton('1');",
            "      add(button);",
            "    }",
            "    {",
            "      button = new JButton('2');",
            "      add(button);",
            "    }",
            "  }",
            "}");
    assertThat(panel.getChildrenComponents()).hasSize(2);
    ComponentInfo button = panel.getChildrenComponents().get(0);
    //
    VariableSupport variableSupport = button.getVariableSupport();
    assertTrue(variableSupport instanceof FieldReuseVariableSupport);
    NodeTarget target = getNodeStatementTarget(panel, false, 0);
    assertEquals("button", variableSupport.getReferenceExpression(target));
  }

  public void test_getReferenceExpression_remote() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  private JButton button;",
            "  Test() {",
            "    {",
            "      button = new JButton('1');",
            "      add(button);",
            "    }",
            "    {",
            "      button = new JButton('2');",
            "      add(button);",
            "    }",
            "  }",
            "}");
    assertThat(panel.getChildrenComponents()).hasSize(2);
    ComponentInfo button = panel.getChildrenComponents().get(0);
    //
    VariableSupport variableSupport = button.getVariableSupport();
    assertTrue(variableSupport instanceof FieldReuseVariableSupport);
    NodeTarget target = getNodeStatementTarget(panel, false, 1);
    assertEquals("button_1", variableSupport.getReferenceExpression(target));
    assertTrue(button.getVariableSupport() instanceof FieldUniqueVariableSupport);
    assertEditor(
        "class Test extends JPanel {",
        "  private JButton button;",
        "  private JButton button_1;",
        "  Test() {",
        "    {",
        "      button_1 = new JButton('1');",
        "      add(button_1);",
        "    }",
        "    {",
        "      button = new JButton('2');",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link FieldReuseVariableSupport} when field is assigned for this component.
   */
  public void test_1() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  private JButton button = new JButton('1');",
            "  Test() {",
            "    button = new JButton('2');",
            "    add(button);",
            "  }",
            "}");
    assertThat(panel.getChildrenComponents()).hasSize(1);
    ComponentInfo button = panel.getChildrenComponents().get(0);
    //
    VariableSupport variableSupport = button.getVariableSupport();
    assertTrue(variableSupport instanceof FieldReuseVariableSupport);
    assertEquals("field-reused: button", variableSupport.toString());
    assertEquals("button", variableSupport.getName());
    // no "this" prefix support
    assertFalse((Boolean) ReflectionUtils.invokeMethod(variableSupport, "prefixThis()"));
    // conversion
    assertFalse(variableSupport.canConvertLocalToField());
    try {
      variableSupport.convertLocalToField();
      fail();
    } catch (IllegalStateException e) {
    }
    assertFalse(variableSupport.canConvertFieldToLocal());
    try {
      variableSupport.convertFieldToLocal();
      fail();
    } catch (IllegalStateException e) {
    }
  }

  /**
   * Test for {@link FieldReuseVariableSupport} when field is declared with this component.
   */
  public void test_2() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  private JButton button = new JButton('1');",
            "  Test() {",
            "    add(button);",
            "    button = new JButton('2');",
            "  }",
            "}");
    assertThat(panel.getChildrenComponents()).hasSize(1);
    ComponentInfo button = panel.getChildrenComponents().get(0);
    //
    {
      VariableSupport variableSupport = button.getVariableSupport();
      assertTrue(variableSupport instanceof FieldReuseVariableSupport);
      // split variable
      {
        NodeTarget target = getNodeStatementTarget(panel, false, 1);
        assertEquals("button_1", variableSupport.getReferenceExpression(target));
        assertTrue(button.getVariableSupport() instanceof FieldUniqueVariableSupport);
        assertAST(m_lastEditor);
        assertEquals(
            getTestSource(
                "class Test extends JPanel {",
                "  private JButton button;",
                "  private JButton button_1 = new JButton('1');",
                "  Test() {",
                "    add(button_1);",
                "    button = new JButton('2');",
                "  }",
                "}"),
            m_lastEditor.getSource());
      }
    }
    // change name, just to check that all references are valid
    {
      button.getVariableSupport().setName("firstButton");
      assertAST(m_lastEditor);
      assertEquals(
          getTestSource(
              "class Test extends JPanel {",
              "  private JButton button;",
              "  private JButton firstButton = new JButton('1');",
              "  Test() {",
              "    add(firstButton);",
              "    button = new JButton('2');",
              "  }",
              "}"),
          m_lastEditor.getSource());
    }
  }

  /**
   * Test for {@link FieldReuseVariableSupport#setName(String)} and static context.
   */
  public void test_3() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test {",
            "  private static JButton button = new JButton('111');",
            "  public static void main(String args[]) {",
            "    JPanel panel = new JPanel();",
            "    panel.add(button);",
            "    button = new JButton('222');",
            "  }",
            "}");
    assertEquals(1, panel.getChildrenComponents().size());
    //
    ComponentInfo button = panel.getChildrenComponents().get(0);
    VariableSupport variableSupport = button.getVariableSupport();
    assertTrue(variableSupport instanceof FieldReuseVariableSupport);
    // split variable
    variableSupport.setName("firstButton");
    assertTrue(button.getVariableSupport() instanceof FieldUniqueVariableSupport);
    assertAST(m_lastEditor);
    assertEquals(
        getTestSource(
            "class Test {",
            "  private static JButton button;",
            "  private static JButton firstButton = new JButton('111');",
            "  public static void main(String args[]) {",
            "    JPanel panel = new JPanel();",
            "    panel.add(firstButton);",
            "    button = new JButton('222');",
            "  }",
            "}"),
        m_lastEditor.getSource());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // setType()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link FieldReuseVariableSupport} when field is declared with this component.
   */
  public void test_setType() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private JButton button;",
            "  public Test() {",
            "    button = new JButton('button 1');",
            "    add(button);",
            "    //",
            "    button = new JButton('button 2');",
            "    add(button);",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // set type
    {
      FieldReuseVariableSupport variable = (FieldReuseVariableSupport) button.getVariableSupport();
      variable.setType("javax.swing.JTextField");
    }
    // checks
    assertEditor(
        "public class Test extends JPanel {",
        "  private JButton button;",
        "  private JTextField button_1;",
        "  public Test() {",
        "    button_1 = new JButton('button 1');",
        "    add(button_1);",
        "    //",
        "    button = new JButton('button 2');",
        "    add(button);",
        "  }",
        "}");
    {
      FieldUniqueVariableSupport variable =
          (FieldUniqueVariableSupport) button.getVariableSupport();
      assertEquals("button_1", variable.getName());
    }
  }
}
