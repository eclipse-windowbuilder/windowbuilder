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
package org.eclipse.wb.tests.designer.swing.model.component;

import org.eclipse.wb.core.model.association.Association;
import org.eclipse.wb.core.model.association.InvocationChildAssociation;
import org.eclipse.wb.core.model.association.InvocationVoidAssociation;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.creation.VoidInvocationCreationSupport;
import org.eclipse.wb.internal.core.model.creation.factory.ImplicitFactoryCreationSupport;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.util.PropertyUtils;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.model.variable.VoidInvocationVariableSupport;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.swing.model.bean.ActionContainerInfo;
import org.eclipse.wb.internal.swing.model.bean.ActionInfo;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.component.JToolBarInfo;
import org.eclipse.wb.internal.swing.model.component.JToolBarSeparatorCreationSupport;
import org.eclipse.wb.internal.swing.model.component.JToolBarSeparatorInfo;
import org.eclipse.wb.internal.swing.model.layout.FlowLayoutInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Statement;

import static org.assertj.core.api.Assertions.assertThat;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

/**
 * Test for {@link JToolBar}.
 * 
 * @author scheglov_ke
 */
public class JToolBarTest extends SwingModelTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for association.
   */
  public void test_association_Component() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    JToolBar bar = new JToolBar();",
            "    add(bar);",
            "    {",
            "      JButton button = new JButton();",
            "      bar.add(button);",
            "    }",
            "  }",
            "}");
    assertNoErrors(panel);
    JToolBarInfo bar = (JToolBarInfo) panel.getChildrenComponents().get(0);
    panel.refresh();
    //
    assertEquals(1, bar.getChildrenComponents().size());
    ComponentInfo button = bar.getChildrenComponents().get(0);
    assertInstanceOf(InvocationChildAssociation.class, button.getAssociation());
  }

  /**
   * Test for horizontal orientation.
   */
  public void test_orientation_horizontal() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    JToolBar bar = new JToolBar();",
            "    add(bar);",
            "    {",
            "      JButton button = new JButton();",
            "      bar.add(button);",
            "    }",
            "  }",
            "}");
    JToolBarInfo bar = (JToolBarInfo) panel.getChildrenComponents().get(0);
    panel.refresh();
    assertTrue(bar.isHorizontal());
  }

  /**
   * Test for {@link SwingConstants#VERTICAL} orientation.
   */
  public void test_orientation_vertical() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    JToolBar bar = new JToolBar(SwingConstants.VERTICAL);",
            "    add(bar);",
            "  }",
            "}");
    JToolBarInfo bar = (JToolBarInfo) panel.getChildrenComponents().get(0);
    panel.refresh();
    assertFalse(bar.isHorizontal());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CREATE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_CREATE() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    JToolBar bar = new JToolBar();",
            "    add(bar);",
            "  }",
            "}");
    JToolBarInfo bar = (JToolBarInfo) panel.getChildrenComponents().get(0);
    // add component
    {
      ComponentInfo newComponent = createJButton();
      bar.command_CREATE(newComponent, null);
      assertInstanceOf(InvocationChildAssociation.class, newComponent.getAssociation());
    }
    assertEditor(
        "class Test extends JPanel {",
        "  Test() {",
        "    JToolBar bar = new JToolBar();",
        "    add(bar);",
        "    {",
        "      JButton button = new JButton();",
        "      bar.add(button);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Move
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_OUT() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    JToolBar bar = new JToolBar();",
            "    add(bar);",
            "    {",
            "      JButton button = new JButton();",
            "      bar.add(button);",
            "    }",
            "    {",
            "      JPanel innerPanel = new JPanel();",
            "      add(innerPanel);",
            "    }",
            "  }",
            "}");
    // prepare source
    JToolBarInfo bar = (JToolBarInfo) panel.getChildrenComponents().get(0);
    ComponentInfo button = bar.getChildrenComponents().get(0);
    // prepare target
    ContainerInfo innerPanel = (ContainerInfo) panel.getChildrenComponents().get(1);
    FlowLayoutInfo innerLayout = (FlowLayoutInfo) innerPanel.getLayout();
    // do move
    innerLayout.move(button, null);
    assertEditor(
        "class Test extends JPanel {",
        "  Test() {",
        "    JToolBar bar = new JToolBar();",
        "    add(bar);",
        "    {",
        "      JPanel innerPanel = new JPanel();",
        "      add(innerPanel);",
        "      {",
        "        JButton button = new JButton();",
        "        innerPanel.add(button);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  public void test_MOVE() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    JToolBar bar = new JToolBar();",
            "    add(bar);",
            "    {",
            "      JButton button = new JButton('000');",
            "      bar.add(button);",
            "    }",
            "    {",
            "      JButton button = new JButton('111');",
            "      bar.add(button);",
            "    }",
            "  }",
            "}");
    // prepare source
    JToolBarInfo bar = (JToolBarInfo) panel.getChildrenComponents().get(0);
    ComponentInfo button_0 = bar.getChildrenComponents().get(0);
    ComponentInfo button_1 = bar.getChildrenComponents().get(1);
    // do move
    bar.command_MOVE(button_1, button_0);
    assertEditor(
        "class Test extends JPanel {",
        "  Test() {",
        "    JToolBar bar = new JToolBar();",
        "    add(bar);",
        "    {",
        "      JButton button = new JButton('111');",
        "      bar.add(button);",
        "    }",
        "    {",
        "      JButton button = new JButton('000');",
        "      bar.add(button);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ADD
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_ADD() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    JToolBar bar = new JToolBar();",
            "    add(bar);",
            "    {",
            "      JPanel innerPanel = new JPanel();",
            "      add(innerPanel);",
            "      {",
            "        JButton button = new JButton();",
            "        innerPanel.add(button);",
            "      }",
            "    }",
            "  }",
            "}");
    JToolBarInfo bar = (JToolBarInfo) panel.getChildrenComponents().get(0);
    ContainerInfo innerPanel = (ContainerInfo) panel.getChildrenComponents().get(1);
    ComponentInfo button = innerPanel.getChildrenComponents().get(0);
    // 
    bar.command_MOVE(button, null);
    assertEditor(
        "class Test extends JPanel {",
        "  Test() {",
        "    JToolBar bar = new JToolBar();",
        "    add(bar);",
        "    {",
        "      JButton button = new JButton();",
        "      bar.add(button);",
        "    }",
        "    {",
        "      JPanel innerPanel = new JPanel();",
        "      add(innerPanel);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Separator
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link JToolBar#addSeparator()} parsing, {@link CreationSupport} and
   * {@link VariableSupport}.
   */
  public void test_separator_Supports() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    JToolBar bar = new JToolBar();",
            "    add(bar);",
            "    //",
            "    bar.addSeparator();",
            "  }",
            "}");
    JToolBarInfo bar = (JToolBarInfo) panel.getChildrenComponents().get(0);
    JToolBarSeparatorInfo separator = (JToolBarSeparatorInfo) bar.getChildrenComponents().get(0);
    // check association
    assertEquals("bar.addSeparator()", separator.getAssociation().getSource());
    Statement associationStatement = separator.getAssociation().getStatement();
    // check VoidInvocationVariableSupport
    {
      VariableSupport variable = separator.getVariableSupport();
      assertInstanceOf(VoidInvocationVariableSupport.class, variable);
      assertEquals("void", variable.toString());
      assertEquals("addSeparator()", variable.getTitle());
      // target
      try {
        assertTarget(variable.getStatementTarget(), null, associationStatement, false);
        fail();
      } catch (IllegalStateException e) {
      }
      // name
      {
        assertFalse(variable.hasName());
        try {
          variable.getName();
          fail();
        } catch (IllegalStateException e) {
        }
        try {
          variable.setName("can-not-set-name");
          fail();
        } catch (IllegalStateException e) {
        }
      }
      // expressions
      {
        try {
          variable.getReferenceExpression((NodeTarget) null);
          fail();
        } catch (IllegalStateException e) {
        }
        try {
          variable.getAccessExpression((NodeTarget) null);
          fail();
        } catch (IllegalStateException e) {
        }
      }
      // conversion
      {
        assertFalse(variable.canConvertLocalToField());
        try {
          variable.convertLocalToField();
          fail();
        } catch (IllegalStateException e) {
        }
        //
        assertFalse(variable.canConvertFieldToLocal());
        try {
          variable.convertFieldToLocal();
          fail();
        } catch (IllegalStateException e) {
        }
      }
    }
    // check JToolBar_Separator_CreationSupport
    {
      JToolBarSeparatorCreationSupport creation =
          (JToolBarSeparatorCreationSupport) separator.getCreationSupport();
      assertEquals("void", creation.toString());
      assertSame(
          ((InvocationVoidAssociation) separator.getAssociation()).getInvocation(),
          creation.getNode());
      // validation
      assertTrue(creation.canReorder());
      assertFalse(creation.canReparent());
      assertTrue(creation.canDelete());
    }
    // check association
    assertInstanceOf(InvocationVoidAssociation.class, separator.getAssociation());
  }

  /**
   * {@link VoidInvocationCreationSupport} does not return value, so has only factory properties, no
   * method/field based ones.
   */
  public void test_addSeparator_noBeanProperties() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    JToolBar bar = new JToolBar();",
            "    add(bar);",
            "    bar.addSeparator(new Dimension(100, 50));",
            "  }",
            "}");
    JToolBarInfo bar = (JToolBarInfo) panel.getChildrenComponents().get(0);
    ComponentInfo separator = bar.getChildrenComponents().get(0);
    //
    Property[] properties = separator.getProperties();
    assertThat(properties).hasSize(1);
    assertNotNull(PropertyUtils.getByPath(properties, "Factory"));
    assertNotNull(PropertyUtils.getByPath(properties, "Factory/size"));
  }

  /**
   * Test for adding {@link JToolBarSeparatorInfo}.
   */
  public void test_separator_create() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    JToolBar bar = new JToolBar();",
            "    add(bar);",
            "  }",
            "}");
    JToolBarInfo bar = (JToolBarInfo) panel.getChildrenComponents().get(0);
    // create separator
    JToolBarSeparatorCreationSupport creationSupport = new JToolBarSeparatorCreationSupport(bar);
    JToolBarSeparatorInfo separator =
        (JToolBarSeparatorInfo) JavaInfoUtils.createJavaInfo(
            m_lastEditor,
            JToolBar.Separator.class,
            creationSupport);
    // add separator
    bar.command_CREATE(separator, null);
    // check creation
    assertNotNull(creationSupport.getInvocation());
    // check variable
    assertInstanceOf(VoidInvocationVariableSupport.class, separator.getVariableSupport());
    // check association
    {
      Association association = separator.getAssociation();
      assertInstanceOf(InvocationVoidAssociation.class, association);
      assertEquals("bar.addSeparator()", association.getSource());
    }
    // check source
    assertEditor(
        "class Test extends JPanel {",
        "  Test() {",
        "    JToolBar bar = new JToolBar();",
        "    add(bar);",
        "    bar.addSeparator();",
        "  }",
        "}");
  }

  public void test_separator_move() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    JToolBar bar = new JToolBar();",
            "    add(bar);",
            "    {",
            "      JButton button = new JButton();",
            "      bar.add(button);",
            "    }",
            "    bar.addSeparator();",
            "  }",
            "}");
    JToolBarInfo bar = (JToolBarInfo) panel.getChildrenComponents().get(0);
    ComponentInfo button = bar.getChildrenComponents().get(0);
    JToolBarSeparatorInfo separator = (JToolBarSeparatorInfo) bar.getChildrenComponents().get(1);
    // move separator
    bar.command_MOVE(separator, button);
    assertEditor(
        "class Test extends JPanel {",
        "  Test() {",
        "    JToolBar bar = new JToolBar();",
        "    add(bar);",
        "    bar.addSeparator();",
        "    {",
        "      JButton button = new JButton();",
        "      bar.add(button);",
        "    }",
        "  }",
        "}");
  }

  public void test_separator_delete() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    JToolBar bar = new JToolBar();",
            "    add(bar);",
            "    bar.addSeparator();",
            "  }",
            "}");
    JToolBarInfo bar = (JToolBarInfo) panel.getChildrenComponents().get(0);
    JToolBarSeparatorInfo separator = (JToolBarSeparatorInfo) bar.getChildrenComponents().get(0);
    // delete separator
    assertTrue(separator.canDelete());
    separator.delete();
    assertEditor(
        "class Test extends JPanel {",
        "  Test() {",
        "    JToolBar bar = new JToolBar();",
        "    add(bar);",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Action
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Create {@link CompilationUnit} with external {@link Action}.
   */
  private void createExternalAction() throws Exception {
    setFileContentSrc(
        "test/ExternalAction.java",
        getTestSource(
            "public class ExternalAction extends AbstractAction {",
            "  public ExternalAction() {",
            "    putValue(NAME, 'My name');",
            "    putValue(SHORT_DESCRIPTION, 'My short description');",
            "  }",
            "  public void actionPerformed(ActionEvent e) {",
            "  }",
            "}"));
    waitForAutoBuild();
  }

  /**
   * Invocation of {@link JToolBar#add(Action)} creates {@link JButton}, so we also should create
   * {@link ComponentInfo} for such invocation.
   */
  public void test_addAction_parse() throws Exception {
    createExternalAction();
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  private ExternalAction action = new ExternalAction();",
            "  Test() {",
            "    JToolBar bar = new JToolBar();",
            "    add(bar);",
            "    //",
            "    JButton button = bar.add(action);",
            "    button.setEnabled(false);",
            "  }",
            "}");
    JToolBarInfo bar = (JToolBarInfo) panel.getChildrenComponents().get(0);
    // we should have ComponentInfo
    assertEquals(1, bar.getChildrenComponents().size());
    ComponentInfo button = bar.getChildrenComponents().get(0);
    // properties
    assertNotNull(PropertyUtils.getByPath(button, "Factory/action"));
    // ImplicitFactoryCreationSupport
    {
      ImplicitFactoryCreationSupport creationSupport =
          (ImplicitFactoryCreationSupport) button.getCreationSupport();
      assertEquals("bar.add(action)", m_lastEditor.getSource(creationSupport.getNode()));
      assertTrue(creationSupport.canReorder());
      assertFalse(creationSupport.canReparent());
    }
  }

  /**
   * Use {@link ImplicitFactoryCreationSupport} with {@link JToolBar#add(Action)} to create
   * {@link JButton}.
   */
  public void test_addAction_generate() throws Exception {
    createExternalAction();
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  private ExternalAction action = new ExternalAction();",
            "  Test() {",
            "    JToolBar bar = new JToolBar();",
            "    add(bar);",
            "  }",
            "}");
    JToolBarInfo bar = (JToolBarInfo) panel.getChildrenComponents().get(0);
    ActionInfo action = ActionContainerInfo.getActions(panel).get(0);
    // add new JButton using ActionInfo
    ComponentInfo newButton = bar.command_CREATE(action, null);
    assertEditor(
        "class Test extends JPanel {",
        "  private ExternalAction action = new ExternalAction();",
        "  Test() {",
        "    JToolBar bar = new JToolBar();",
        "    add(bar);",
        "    {",
        "      JButton button = bar.add(action);",
        "    }",
        "  }",
        "}");
    // ImplicitFactoryCreationSupport
    {
      ImplicitFactoryCreationSupport creationSupport =
          (ImplicitFactoryCreationSupport) newButton.getCreationSupport();
      assertEquals("bar.add(action)", m_lastEditor.getSource(creationSupport.getNode()));
      // check that MethodDescription was initialized
      newButton.getProperties();
    }
  }
}
