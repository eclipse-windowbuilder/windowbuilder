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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import org.eclipse.wb.internal.core.model.variable.AbstractNamedVariableSupport;
import org.eclipse.wb.internal.core.model.variable.AbstractSimpleVariableSupport;
import org.eclipse.wb.internal.core.model.variable.LocalUniqueVariableSupport;
import org.eclipse.wb.internal.core.model.variable.VariableProperty;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.gef.UIRunnable;
import org.eclipse.wb.tests.gef.UiContext;

import org.eclipse.jdt.core.dom.ASTNode;

import org.easymock.EasyMock;

import java.util.Map;
import java.util.Set;

/**
 * Test for {@link AbstractNamedVariableSupport}.
 *
 * @author scheglov_ke
 */
public class AbstractNamedTest extends AbstractVariableTest {
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
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_accessName() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    JButton button = new JButton();",
            "    add(button);",
            "  }",
            "}");
    // prepare variable
    ComponentInfo button = panel.getChildrenComponents().get(0);
    VariableSupport variableSupport = button.getVariableSupport();
    // check variable
    assertTrue(variableSupport.hasName());
    assertEquals("button", variableSupport.getName());
    assertEquals("button", variableSupport.getTitle());
    assertEquals("button", variableSupport.getComponentName());
  }

  /**
   * It is possible, that {@link VariableSupport} in general has name, i.e.
   * {@link VariableSupport#hasName()}, but right now there are no {@link ASTNode} with variable
   * yet.
   */
  public void test_getName_whenNoVariable() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "class Test extends JPanel {",
            "  Test() {",
            "  }",
            "}");
    // new VariableSupport, without variable ASTNode
    VariableSupport variable = new LocalUniqueVariableSupport(panel);
    assertFalse(variable.hasName());
    assertEquals("no-variable-yet", variable.getName());
  }

  /**
   * Test that {@link AbstractNamedVariableSupport#getAccessExpression(NodeTarget)} just calls
   * {@link AbstractNamedVariableSupport#getReferenceExpression(NodeTarget)} and appends
   * <code>"."</code>.
   */
  public void test_getAccessExpression() throws Exception {
    AbstractNamedVariableSupport variable =
        EasyMock.createStrictMock(AbstractNamedVariableSupport.class);
    NodeTarget target = null;
    // configure variable
    org.easymock.EasyMock.expect(variable.getReferenceExpression(target)).andReturn("button");
    EasyMock.replay(variable);
    // do verify
    assertEquals("button.", variable.getAccessExpression(target));
    EasyMock.verify(variable);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // setNameBase()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * One variable, set same name as current.
   */
  public void test_setNameBase_sameName() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    JButton button = new JButton();",
            "    add(button);",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    AbstractNamedVariableSupport variable =
        (AbstractNamedVariableSupport) button.getVariableSupport();
    variable.setNameBase("button");
    assertEditor(
        "class Test extends JPanel {",
        "  Test() {",
        "    JButton button = new JButton();",
        "    add(button);",
        "  }",
        "}");
  }

  /**
   * Two variables, potential shadow conflict.<br>
   * No test for visible conflict, we use {@link AstEditor} that already supports both cases.
   */
  public void test_setNameBase_shadowConflict() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    JButton button = new JButton();",
            "    add(button);",
            "    //",
            "    int button2;",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    AbstractNamedVariableSupport variable =
        (AbstractNamedVariableSupport) button.getVariableSupport();
    variable.setNameBase("button2");
    assertEditor(
        "class Test extends JPanel {",
        "  Test() {",
        "    JButton button2_1 = new JButton();",
        "    add(button2_1);",
        "    //",
        "    int button2;",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Variable property
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_variableProperty() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    JButton button = new JButton();",
            "    add(button);",
            "    //",
            "    JButton button_2 = new JButton();",
            "    add(button_2);",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    //
    final VariableProperty variableProperty =
        (VariableProperty) button.getPropertyByTitle("Variable");
    assertTrue(variableProperty.isModified());
    assertTrue(variableProperty.getCategory().isSystem());
    assertEquals("button", variableProperty.getValue());
    // try to set something other than String, should be ignored
    variableProperty.setValue(this);
    // set different name
    variableProperty.setValue("abc");
    assertEditor(
        "class Test extends JPanel {",
        "  Test() {",
        "    JButton abc = new JButton();",
        "    add(abc);",
        "    //",
        "    JButton button_2 = new JButton();",
        "    add(button_2);",
        "  }",
        "}");
    // initial state
    {
      assertEquals("abc", getPropertyText(variableProperty));
      assertEquals("abc", getTextEditorText(variableProperty));
    }
    // duplicate name
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        setTextEditorText(variableProperty, "button_2");
      }
    }, new UIRunnable() {
      public void run(UiContext context) throws Exception {
        context.useShell("Variable");
        context.clickButton("OK");
      }
    });
    // no changes
    {
      assertEquals("abc", getPropertyText(variableProperty));
      assertEquals("abc", getTextEditorText(variableProperty));
    }
    // set different name
    {
      setTextEditorText(variableProperty, "button_1");
      assertEquals("button_1", button.getVariableSupport().getName());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // validateVariables()
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final Set<AbstractNamedVariableSupport> NO_VARIABLES = Sets.newHashSet();

  private static String validateVariables(Map<AbstractNamedVariableSupport, String> variablesNames) {
    return validateVariables(variablesNames, NO_VARIABLES, NO_VARIABLES);
  }

  private static void validateVariables(boolean valid,
      Map<AbstractNamedVariableSupport, String> variablesNames) {
    String message = validateVariables(variablesNames);
    if (valid) {
      assertNull(message, message);
    } else {
      assertNotNull(message);
    }
  }

  private static String validateVariables(Map<AbstractNamedVariableSupport, String> variablesNames,
      Set<AbstractNamedVariableSupport> toLocalVariables,
      Set<AbstractNamedVariableSupport> toFieldVariables) {
    return AbstractNamedVariableSupport.validateVariables(
        variablesNames,
        toLocalVariables,
        toFieldVariables);
  }

  /**
   * Test for single variable.
   */
  public void test_validateVariables_singleVariable() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private int m_field;",
            "  public Test() {",
            "    JButton button = new JButton();",
            "    add(button);",
            "  }",
            "}");
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // invalid identifier
    {
      Map<AbstractNamedVariableSupport, String> variablesNames =
          ImmutableMap.of((AbstractNamedVariableSupport) button.getVariableSupport(), "in-valid");
      assertTrue(validateVariables(variablesNames).contains("identifier"));
    }
    // valid identifier
    {
      Map<AbstractNamedVariableSupport, String> variablesNames =
          ImmutableMap.of((AbstractNamedVariableSupport) button.getVariableSupport(), "myButton");
      validateVariables(true, variablesNames);
    }
  }

  /**
   * Tests for renaming two variables with/without visible/shadow conflicts.
   */
  public void test_validateVariables_twoVariables_plain() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private int m_field;",
            "  public Test() {",
            "    JButton button1 = new JButton();",
            "    add(button1);",
            "    //",
            "    JButton button2 = new JButton();",
            "    add(button2);",
            "  }",
            "}");
    ComponentInfo button1 = panel.getChildrenComponents().get(0);
    ComponentInfo button2 = panel.getChildrenComponents().get(1);
    AbstractNamedVariableSupport variable1 =
        (AbstractNamedVariableSupport) button1.getVariableSupport();
    AbstractNamedVariableSupport variable2 =
        (AbstractNamedVariableSupport) button2.getVariableSupport();
    // no conflict: no modifications
    {
      Map<AbstractNamedVariableSupport, String> variablesNames = ImmutableMap.of();
      validateVariables(true, variablesNames);
    }
    // visible conflict: button2 -> button1
    {
      validateVariables(false, ImmutableMap.of(variable2, "button1"));
    }
    // no visible conflict: button2 -> button1, button1 -> button_1
    {
      Map<AbstractNamedVariableSupport, String> variablesNames =
          ImmutableMap.of(variable2, "button1", variable1, "button_1");
      validateVariables(true, variablesNames);
    }
    // no visible conflict: button2 -> button1, button1 -> button2
    {
      Map<AbstractNamedVariableSupport, String> variablesNames =
          ImmutableMap.of(variable2, "button1", variable1, "button2");
      validateVariables(true, variablesNames);
    }
    // shadow conflict: button1 -> button2
    {
      Map<AbstractNamedVariableSupport, String> variablesNames =
          ImmutableMap.of(variable1, "button2");
      validateVariables(false, variablesNames);
    }
    // no shadow conflict: button1 -> button2, button2 -> button_2
    {
      Map<AbstractNamedVariableSupport, String> variablesNames =
          ImmutableMap.of(variable1, "button2", variable2, "button_2");
      validateVariables(true, variablesNames);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // validateName()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_validateName_local() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  JButton m_field;",
            "  public Test() {",
            "    JButton button1 = new JButton();",
            "    add(button1);",
            "    //",
            "    {",
            "      JButton button2 = new JButton();",
            "    }",
            "    //",
            "    JButton button3 = new JButton();",
            "    add(button3);",
            "    //",
            "    {",
            "      JButton button4 = new JButton();",
            "    }",
            "  }",
            "}");
    ComponentInfo button_1 = panel.getChildrenComponents().get(0);
    ComponentInfo button_2 = panel.getChildrenComponents().get(1);
    AbstractSimpleVariableSupport variable_1 =
        (AbstractSimpleVariableSupport) button_1.getVariableSupport();
    AbstractSimpleVariableSupport variable_2 =
        (AbstractSimpleVariableSupport) button_2.getVariableSupport();
    // try invalid identifier
    {
      String errorMessage = variable_2.validateName("@#$%");
      assertNotNull(errorMessage);
    }
    // try reserved work
    {
      String errorMessage = variable_2.validateName("while");
      assertNotNull(errorMessage);
    }
    // visible variable
    {
      String errorMessage = variable_2.validateName("button1");
      assertNotNull(errorMessage);
    }
    // visible field
    {
      String errorMessage = variable_2.validateName("m_field");
      assertNotNull(errorMessage);
    }
    // new variable
    {
      String errorMessage = variable_2.validateName("buttonA");
      assertNull(errorMessage);
    }
    // variable that hidden in block
    {
      String errorMessage = variable_2.validateName("button2");
      assertNull(errorMessage);
    }
    // back visible
    {
      String errorMessage = variable_1.validateName("button3");
      assertNotNull(errorMessage);
    }
    // back visible in block
    {
      String errorMessage = variable_1.validateName("button4");
      assertNotNull(errorMessage);
    }
  }

  public void test_validateName_field() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  JButton button1 = new JButton();",
            "  JButton m_field;",
            "  public Test() {",
            "    add(button1);",
            "    //",
            "    JButton button2 = new JButton();",
            "    add(button2);",
            "  }",
            "}");
    ComponentInfo button_1 = panel.getChildrenComponents().get(0);
    AbstractSimpleVariableSupport variable_1 =
        (AbstractSimpleVariableSupport) button_1.getVariableSupport();
    // visible field
    {
      String errorMessage = variable_1.validateName("m_field");
      assertNotNull(errorMessage);
    }
    // field that hides variable
    {
      String errorMessage = variable_1.validateName("button2");
      assertNotNull(errorMessage);
    }
  }
}
