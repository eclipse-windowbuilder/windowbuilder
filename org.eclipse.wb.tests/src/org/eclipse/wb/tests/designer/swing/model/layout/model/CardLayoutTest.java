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
package org.eclipse.wb.tests.designer.swing.model.layout.model;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.association.InvocationAssociation;
import org.eclipse.wb.core.model.association.InvocationChildAssociation;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.component.menu.JPopupMenuInfo;
import org.eclipse.wb.internal.swing.model.layout.CardLayoutInfo;
import org.eclipse.wb.internal.swing.model.layout.FlowLayoutInfo;
import org.eclipse.wb.tests.designer.swing.model.layout.AbstractLayoutTest;

import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.StringLiteral;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.util.List;

import javax.swing.JPopupMenu;

/**
 * Test for {@link CardLayoutInfo}.
 * 
 * @author lobas_av
 * @author scheglov_ke
 */
public class CardLayoutTest extends AbstractLayoutTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // setLayout
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for installing.
   */
  public void test_setLayout() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    setLayout(panel, CardLayout.class);
    assertEditor(
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new CardLayout(0, 0));",
        "  }",
        "}");
    // no components
    CardLayoutInfo layout = (CardLayoutInfo) panel.getLayout();
    assertNull(layout.getCurrentComponent());
    assertNull(layout.getPrevComponent());
    assertNull(layout.getNextComponent());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Dangling {@link CardLayout} should not cause problems.
   * <p>
   * https://bugs.eclipse.org/bugs/show_bug.cgi?id=363376
   */
  public void test_dangling() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    // create CardLayoutInfo just to have "stack container" broadcast listeners
    createJavaInfo("java.awt.CardLayout");
    // add new JButton, during this CardLayoutInfo will be called
    ComponentInfo button = createJavaInfo("javax.swing.JButton");
    ((FlowLayoutInfo) panel.getLayout()).add(button, null);
  }

  /**
   * {@link JPopupMenu} is not managed.
   */
  public void test_managedComponents_excludeJPopupMenu() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new CardLayout());",
            "    {",
            "      JButton button = new JButton();",
            "      add(button, '0');",
            "    }",
            "    {",
            "      JPopupMenu popup = new JPopupMenu();",
            "      addPopup(this, popup);",
            "    }",
            "  }",
            "  private static void addPopup(Component component, JPopupMenu popup) {",
            "  }",
            "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/setLayout(new CardLayout())/ /add(button, '0')/ /addPopup(this, popup)/}",
        "  {new: java.awt.CardLayout} {empty} {/setLayout(new CardLayout())/}",
        "  {new: javax.swing.JButton} {local-unique: button} {/new JButton()/ /add(button, '0')/}",
        "  {new: javax.swing.JPopupMenu} {local-unique: popup} {/new JPopupMenu()/ /addPopup(this, popup)/}");
    CardLayoutInfo layout = (CardLayoutInfo) panel.getLayout();
    ComponentInfo button = getJavaInfoByName("button");
    // only JButton is managed component
    assertThat(layout.getComponents()).containsExactly(button);
  }

  /**
   * Exposed {@link ComponentInfo} is managed.
   */
  public void test_managedComponents_includeExposedComponents() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getTestSource(
            "public class MyPanel extends JPanel {",
            "  private JButton buttonA = new JButton();",
            "  private JButton buttonB = new JButton();",
            "  public MyPanel() {",
            "    setLayout(new CardLayout());",
            "    add(buttonA, 'A');",
            "    add(buttonB, 'B');",
            "  }",
            "  public JButton getButtonA() {",
            "    return buttonA;",
            "  }",
            "  public JButton getButtonB() {",
            "    return buttonB;",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class Test extends MyPanel {",
            "  public Test() {",
            "  }",
            "}");
    assertHierarchy(
        "{this: test.MyPanel} {this} {}",
        "  {implicit-layout: java.awt.CardLayout} {implicit-layout} {}",
        "  {method: public javax.swing.JButton test.MyPanel.getButtonA()} {property} {}",
        "  {method: public javax.swing.JButton test.MyPanel.getButtonB()} {property} {}");
    CardLayoutInfo layout = (CardLayoutInfo) panel.getLayout();
    ComponentInfo buttonA = getJavaInfoByName("getButtonA()");
    ComponentInfo buttonB = getJavaInfoByName("getButtonB()");
    // getButtonA() and getButtonB() are managed components
    assertThat(layout.getComponents()).containsExactly(buttonA, buttonB);
  }

  /**
   * Test for converting into {@link CardLayout}.
   * <p>
   * https://bugs.eclipse.org/bugs/show_bug.cgi?id=366817
   */
  public void test_convert() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      JButton button = new JButton();",
            "      add(button);",
            "    }",
            "  }",
            "}");
    refresh();
    ComponentInfo button = getJavaInfoByName("button");
    // set layout
    {
      CardLayoutInfo layout = createJavaInfo("java.awt.CardLayout");
      panel.setLayout(layout);
    }
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new CardLayout(0, 0));",
        "    {",
        "      JButton button = new JButton();",
        "      add(button, '" + getAssociationName(button) + "');",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CREATE
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link CardLayoutInfo#command_CREATE(ComponentInfo, ComponentInfo)}.
   */
  public void test_CREATE() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new CardLayout());",
            "    add(new JLabel(), '0');",
            "  }",
            "}");
    panel.refresh();
    final CardLayoutInfo layout = (CardLayoutInfo) panel.getLayout();
    // currently "label" is selected
    {
      ComponentInfo label = panel.getChildrenComponents().get(0);
      assertSame(label, layout.getCurrentComponent());
    }
    // add new component
    final ComponentInfo newComponent = createJButton();
    ExecutionUtils.run(panel, new RunnableEx() {
      public void run() throws Exception {
        layout.command_CREATE(newComponent, null);
      }
    });
    assertInstanceOf(InvocationChildAssociation.class, newComponent.getAssociation());
    // added component should be selected
    assertSame(newComponent, layout.getCurrentComponent());
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new CardLayout());",
        "    add(new JLabel(), '0');",
        "    {",
        "      JButton button = new JButton();",
        "      add(button, '" + getAssociationName(newComponent) + "');",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link CardLayoutInfo#command_CREATE(ComponentInfo, ComponentInfo)}.
   * <p>
   * Create {@link JPopupMenu}.
   */
  public void test_CREATE_JPopupMenu() throws Exception {
    final ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new CardLayout());",
            "    add(new JButton(), '0');",
            "    add(new JButton(), '1');",
            "  }",
            "}");
    panel.refresh();
    // prepare components
    final CardLayoutInfo layout = (CardLayoutInfo) panel.getLayout();
    assertEquals(2, panel.getChildrenComponents().size());
    ComponentInfo button_0 = panel.getChildrenComponents().get(0);
    ComponentInfo button_1 = panel.getChildrenComponents().get(1);
    // currently "button_0" is selected
    assertSame(button_0, layout.getCurrentComponent());
    assertSame(button_1, layout.getPrevComponent());
    assertSame(button_1, layout.getNextComponent());
    // add JPopupMenu
    final JPopupMenuInfo newPopup = (JPopupMenuInfo) createComponent(JPopupMenu.class);
    ExecutionUtils.run(panel, new RunnableEx() {
      public void run() throws Exception {
        newPopup.command_CREATE(panel);
      }
    });
    // "popup" is listed in "components", but ignored by CardLayout
    assertEquals(3, panel.getChildrenComponents().size());
    assertSame(newPopup, panel.getChildrenComponents().get(0));
    // "popup" can not be selected in CardLayout, so "label" is still selected
    assertSame(button_0, layout.getCurrentComponent());
    assertSame(button_1, layout.getPrevComponent());
    assertSame(button_1, layout.getNextComponent());
    // special case - "popup" is not selected, but still in "graphical children", because it is not attached
    assertTrue(panel.getPresentation().getChildrenGraphical().contains(newPopup));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // MOVE
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for reparenting, normal variable.
   */
  public void test_MOVE_reorder() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new CardLayout());",
            "    {",
            "      JButton button_1 = new JButton();",
            "      add(button_1, '111');",
            "    }",
            "    {",
            "      JButton button_2 = new JButton();",
            "      add(button_2, '222');",
            "    }",
            "  }",
            "}");
    panel.refresh();
    CardLayoutInfo layout = (CardLayoutInfo) panel.getLayout();
    ComponentInfo button_1 = getJavaInfoByName("button_1");
    ComponentInfo button_2 = getJavaInfoByName("button_2");
    //
    layout.command_MOVE(button_2, button_1);
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new CardLayout());",
        "    {",
        "      JButton button_2 = new JButton();",
        "      add(button_2, '222');",
        "    }",
        "    {",
        "      JButton button_1 = new JButton();",
        "      add(button_1, '111');",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for reparenting, normal variable.
   */
  public void test_MOVE_reparent_variable() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new CardLayout());",
            "    {",
            "      JPanel panel = new JPanel();",
            "      add(panel, BorderLayout.NORTH);",
            "      {",
            "        JButton button = new JButton();",
            "        panel.add(button);",
            "      }",
            "    }",
            "  }",
            "}");
    panel.refresh();
    CardLayoutInfo layout = (CardLayoutInfo) panel.getLayout();
    ComponentInfo button = getJavaInfoByName("button");
    //
    layout.command_MOVE(button, null);
    assertInstanceOf(InvocationChildAssociation.class, button.getAssociation());
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new CardLayout());",
        "    {",
        "      JPanel panel = new JPanel();",
        "      add(panel, BorderLayout.NORTH);",
        "    }",
        "    {",
        "      JButton button = new JButton();",
        "      add(button, '" + getAssociationName(button) + "');",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for reparenting, lazy variable.
   */
  public void test_MOVE_reparent_lazy() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private JPanel panel;",
            "  private JButton button;",
            "  public Test() {",
            "    setLayout(new CardLayout());",
            "    add(getPanel(), '111-222-333-444');",
            "  }",
            "  private JPanel getPanel() {",
            "    if (panel == null) {",
            "      panel = new JPanel();",
            "      panel.add(getButton());",
            "    }",
            "    return panel;",
            "  }",
            "  private JButton getButton() {",
            "    if (button == null) {",
            "      button = new JButton();",
            "    }",
            "    return button;",
            "  }",
            "}");
    panel.refresh();
    CardLayoutInfo layout = (CardLayoutInfo) panel.getLayout();
    ComponentInfo button = getJavaInfoByName("button");
    //
    layout.command_MOVE(button, null);
    assertInstanceOf(InvocationChildAssociation.class, button.getAssociation());
    assertEditor(
        "public class Test extends JPanel {",
        "  private JPanel panel;",
        "  private JButton button;",
        "  public Test() {",
        "    setLayout(new CardLayout());",
        "    add(getPanel(), '111-222-333-444');",
        "    add(getButton(), '" + getAssociationName(button) + "');",
        "  }",
        "  private JPanel getPanel() {",
        "    if (panel == null) {",
        "      panel = new JPanel();",
        "    }",
        "    return panel;",
        "  }",
        "  private JButton getButton() {",
        "    if (button == null) {",
        "      button = new JButton();",
        "    }",
        "    return button;",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Showing
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_visibility_JPanel() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new CardLayout());",
            "    {",
            "      JButton button_0 = new JButton();",
            "      add(button_0, '0');",
            "    }",
            "    {",
            "      JButton button_1 = new JButton();",
            "      add(button_1, '1');",
            "    }",
            "  }",
            "}");
    panel.refresh();
    CardLayoutInfo layout = (CardLayoutInfo) panel.getLayout();
    // prepare components
    List<ComponentInfo> components = panel.getChildrenComponents();
    ComponentInfo button_0 = components.get(0);
    ComponentInfo button_1 = components.get(1);
    // currently "button_0" is displayed, so it is visible
    assertSame(button_0, layout.getCurrentComponent());
    assertVisible(button_0, true);
    assertVisible(button_1, false);
    // only "button_0" is in "graphical children"
    {
      List<ObjectInfo> children = panel.getPresentation().getChildrenGraphical();
      assertThat(children).containsExactly(button_0);
    }
  }

  public void test_visibility_JFrame() throws Exception {
    ContainerInfo frame =
        parseContainer(
            "public class Test extends JFrame {",
            "  public Test() {",
            "    getContentPane().setLayout(new CardLayout());",
            "    {",
            "      JButton button_0 = new JButton();",
            "      getContentPane().add(button_0, '0');",
            "    }",
            "    {",
            "      JButton button_1 = new JButton();",
            "      getContentPane().add(button_1, '1');",
            "    }",
            "  }",
            "}");
    frame.refresh();
    ContainerInfo contentPane = (ContainerInfo) frame.getChildrenComponents().get(0);
    CardLayoutInfo layout = (CardLayoutInfo) contentPane.getLayout();
    // prepare components
    List<ComponentInfo> components = contentPane.getChildrenComponents();
    ComponentInfo button_0 = components.get(0);
    ComponentInfo button_1 = components.get(1);
    // currently "button_0" is displayed, so it is visible
    assertSame(button_0, layout.getCurrentComponent());
    assertVisible(button_0, true);
    assertVisible(button_1, false);
  }

  public void test_show() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new CardLayout());",
            "    {",
            "      JButton button = new JButton();",
            "      add(button, '0');",
            "    }",
            "    {",
            "      JLabel label = new JLabel();",
            "      add(label, '1');",
            "    }",
            "    {",
            "      JTextField text = new JTextField();",
            "      add(text, '2');",
            "    }",
            "  }",
            "}");
    panel.refresh();
    // prepare components
    List<ComponentInfo> components = panel.getChildrenComponents();
    assertEquals(3, components.size());
    ComponentInfo button = components.get(0);
    ComponentInfo label = components.get(1);
    ComponentInfo text = components.get(2);
    // prepare layout
    CardLayoutInfo layout = (CardLayoutInfo) panel.getLayout();
    assertSame(button, layout.getCurrentComponent());
    assertSame(text, layout.getPrevComponent());
    assertSame(label, layout.getNextComponent());
    // currently "button" is displayed, so it is visible
    assertVisible(button, true);
    assertVisible(label, false);
    assertVisible(text, false);
    // show "text"
    layout.show(text);
    assertSame(text, layout.getCurrentComponent());
    assertSame(label, layout.getPrevComponent());
    assertSame(button, layout.getNextComponent());
    // now "text" is displayed, so "button" is not visible
    assertVisible(button, false);
    assertVisible(label, false);
    assertVisible(text, true);
  }

  public void test_selecting() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new CardLayout());",
            "    {",
            "      JButton button_0 = new JButton();",
            "      add(button_0, '0');",
            "    }",
            "    {",
            "      JPanel innerPanel = new JPanel();",
            "      add(innerPanel, '1');",
            "      {",
            "        JButton button_1 = new JButton();",
            "        innerPanel.add(button_1);",
            "      }",
            "    }",
            "  }",
            "}");
    panel.refresh();
    CardLayoutInfo layout = (CardLayoutInfo) panel.getLayout();
    // prepare components
    ComponentInfo button_0 = panel.getChildrenComponents().get(0);
    ContainerInfo innerPanel = (ContainerInfo) panel.getChildrenComponents().get(1);
    ComponentInfo button_1 = innerPanel.getChildrenComponents().get(0);
    // "button_0" is currently active
    assertSame(button_0, layout.getCurrentComponent());
    assertVisible(button_0, true);
    assertVisible(innerPanel, false);
    // select "button_0", already selected, so no refresh
    {
      boolean refreshFlag = notifySelecting(button_0);
      assertFalse(refreshFlag);
      assertSame(button_0, layout.getCurrentComponent());
    }
    // select "button_1", so "innerPanel" should be activated
    {
      boolean refreshFlag = notifySelecting(button_1);
      assertTrue(refreshFlag);
      panel.refresh();
      assertSame(innerPanel, layout.getCurrentComponent());
      assertVisible(button_0, false);
      assertVisible(innerPanel, true);
    }
  }

  /**
   * Test that association using {@link Container#add(String, Component)} also works.
   */
  public void test_selecting_deprecatedAdd() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new CardLayout());",
            "    {",
            "      JButton button_0 = new JButton();",
            "      add('0', button_0);",
            "    }",
            "    {",
            "      JButton button_1 = new JButton();",
            "      add('1', button_1);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    CardLayoutInfo layout = (CardLayoutInfo) panel.getLayout();
    // prepare components
    ComponentInfo button_0 = getJavaInfoByName("button_0");
    ComponentInfo button_1 = getJavaInfoByName("button_1");
    // "button_0" is currently active
    assertSame(button_0, layout.getCurrentComponent());
    assertVisible(button_0, true);
    assertVisible(button_1, false);
    // select "button_0", already selected, so no refresh
    {
      boolean refreshFlag = notifySelecting(button_0);
      assertFalse(refreshFlag);
      assertSame(button_0, layout.getCurrentComponent());
    }
    // select "button_1", so activate it
    {
      boolean refreshFlag = notifySelecting(button_1);
      assertTrue(refreshFlag);
      panel.refresh();
      assertSame(button_1, layout.getCurrentComponent());
      assertVisible(button_0, false);
      assertVisible(button_1, true);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Asserts visibility flag of {@link ComponentInfo} object.
   */
  private static void assertVisible(ComponentInfo component, boolean visible) {
    assertEquals(visible, component.getComponent().isVisible());
  }

  /**
   * @return the name used in {@link Container#add(java.awt.Component, Object)}.
   */
  static String getAssociationName(ComponentInfo component) {
    InvocationAssociation association = (InvocationAssociation) component.getAssociation();
    MethodInvocation invocation = association.getInvocation();
    StringLiteral nameLiteral = (StringLiteral) invocation.arguments().get(1);
    return nameLiteral.getLiteralValue();
  }
}