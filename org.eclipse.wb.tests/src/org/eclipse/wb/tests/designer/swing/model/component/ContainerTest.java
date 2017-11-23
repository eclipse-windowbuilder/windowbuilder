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

import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.association.ImplicitObjectAssociation;
import org.eclipse.wb.core.model.association.InvocationChildAssociation;
import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;
import org.eclipse.wb.internal.core.model.order.ComponentOrderBeforeSibling;
import org.eclipse.wb.internal.core.preferences.IPreferenceConstants;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.FlowLayoutInfo;
import org.eclipse.wb.internal.swing.model.layout.GridLayoutInfo;
import org.eclipse.wb.internal.swing.model.layout.ImplicitLayoutCreationSupport;
import org.eclipse.wb.internal.swing.model.layout.ImplicitLayoutVariableSupport;
import org.eclipse.wb.internal.swing.model.layout.LayoutInfo;
import org.eclipse.wb.internal.swing.model.layout.absolute.AbsoluteLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.tests.designer.core.PreferencesRepairer;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.preference.IPreferenceStore;

import com.jgoodies.forms.layout.FormLayout;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.lang.StringUtils;

import java.awt.Component;
import java.awt.Container;
import java.awt.LayoutManager;

/**
 * Test for {@link ContainerInfo}.
 * 
 * @author scheglov_ke
 */
public class ContainerTest extends SwingModelTest {
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
  // Association
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for "normal" Swing association using {@link Container#add(Component)} method.
   */
  public void test_association_noConstraints() throws Exception {
    parseContainer(
        "class Test extends JPanel {",
        "  Test() {",
        "    JButton button = new JButton();",
        "    add(button);",
        "  }",
        "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(button)/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: javax.swing.JButton} {local-unique: button} {/new JButton()/ /add(button)/}");
    ComponentInfo button = getJavaInfoByName("button");
    assertEquals("add(button)", ((InvocationChildAssociation) button.getAssociation()).getSource());
  }

  /**
   * Test for "deprecated" Swing association using {@link Container#add(String, Component)} method.
   */
  public void test_association_addDeprecated() throws Exception {
    parseContainer(
        "class Test extends JPanel {",
        "  Test() {",
        "    JButton button = new JButton();",
        "    add('name', button);",
        "  }",
        "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add('name', button)/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: javax.swing.JButton} {local-unique: button} {/new JButton()/ /add('name', button)/}");
    ComponentInfo button = getJavaInfoByName("button");
    assertEquals("add(\"name\", button)", button.getAssociation().getSource());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for implicit {@link LayoutInfo}.
   */
  public void test_implicitLayout() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    LayoutInfo layout = panel.getLayout();
    assertInstanceOf(ImplicitLayoutCreationSupport.class, layout.getCreationSupport());
    assertInstanceOf(ImplicitLayoutVariableSupport.class, layout.getVariableSupport());
    assertInstanceOf(ImplicitObjectAssociation.class, layout.getAssociation());
  }

  /**
   * Test for insets for Swing components.<br>
   * In AWT/Swing {@link AbstractComponentInfo#getClientAreaInsets()} is usually empty, see comment
   * for {@link ContainerInfo#getInsets()}.
   */
  public void test_getInsets_getClientAreaInsets() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test extends JPanel {",
            "  Test() {",
            "    {",
            "      JButton button_1 = new JButton();",
            "      add(button_1);",
            "      button_1.setBorder(new LineBorder(Color.RED, 10));",
            "    }",
            "    {",
            "      Button button_2 = new Button();",
            "      add(button_2);",
            "    }",
            "  }",
            "}");
    assert_creation(panel);
    assertEquals(new Insets(0, 0, 0, 0), panel.getClientAreaInsets());
    assertEquals(new Insets(0, 0, 0, 0), panel.getInsets());
    // button_1 (Swing)
    {
      ContainerInfo button = (ContainerInfo) panel.getChildrenComponents().get(0);
      assertEquals(new Insets(0, 0, 0, 0), button.getClientAreaInsets());
      assertEquals(new Insets(10, 10, 10, 10), button.getInsets());
    }
    // button_2 (AWT)
    {
      ComponentInfo button = panel.getChildrenComponents().get(1);
      assertEquals(new Insets(0, 0, 0, 0), button.getClientAreaInsets());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // shouldDrawDotsBorder()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ContainerInfo#shouldDrawDotsBorder()}.
   */
  public void test_shouldDrawDotsBorder() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    add(new Container());",
            "    add(new JPanel());",
            "    add(new JTabbedPane());",
            "    {",
            "      JPanel panel = new JPanel();",
            "      panel.setBorder(new LineBorder(Color.red));",
            "      add(panel);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    // prepare containers
    ContainerInfo container_AWT = (ContainerInfo) panel.getChildrenComponents().get(0);
    ContainerInfo container_JPanel = (ContainerInfo) panel.getChildrenComponents().get(1);
    ContainerInfo container_JTabbedPane = (ContainerInfo) panel.getChildrenComponents().get(2);
    ContainerInfo container_JPanel_withBorder =
        (ContainerInfo) panel.getChildrenComponents().get(3);
    // do checks
    assertFalse(panel.shouldDrawDotsBorder());
    assertTrue(container_AWT.shouldDrawDotsBorder());
    assertTrue(container_JPanel.shouldDrawDotsBorder());
    assertFalse(container_JTabbedPane.shouldDrawDotsBorder());
    assertFalse(container_JPanel_withBorder.shouldDrawDotsBorder());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // setLayout()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ContainerInfo#setLayout(LayoutInfo)}.
   */
  public void test_setLayout() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setEnabled(true);",
            "    add(new JButton());",
            "  }",
            "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/setEnabled(true)/ /add(new JButton())/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: javax.swing.JButton} {empty} {/add(new JButton())/}");
    panel.refresh();
    // prepare new Layout
    LayoutInfo newLayout = createJavaInfo("java.awt.GridLayout");
    assertInstanceOf(ComponentOrderBeforeSibling.class, newLayout.getDescription().getOrder());
    // set Layout
    panel.setLayout(newLayout);
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setEnabled(true);",
        "    setLayout(new GridLayout(0, 1, 0, 0));",
        "    add(new JButton());",
        "  }",
        "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/setEnabled(true)/ /add(new JButton())/ /setLayout(new GridLayout(0, 1, 0, 0))/}",
        "  {new: java.awt.GridLayout} {empty} {/setLayout(new GridLayout(0, 1, 0, 0))/}",
        "  {new: javax.swing.JButton} {empty} {/add(new JButton())/}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Layout inheritance/default
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link IPreferenceConstants#P_LAYOUT_OF_PARENT}, that enables layout inheritance.
   */
  public void test_inheritParentLayout() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new GridLayout());",
            "  }",
            "}");
    GridLayoutInfo parentLayout = (GridLayoutInfo) panel.getLayout();
    // prepare preferences
    PreferencesRepairer preferencesRepairer;
    {
      IPreferenceStore preferences = panel.getDescription().getToolkit().getPreferences();
      preferencesRepairer = new PreferencesRepairer(preferences);
      // no inheritance by default
      assertFalse(preferences.getBoolean(IPreferenceConstants.P_LAYOUT_OF_PARENT));
      // enable inheritance
      preferencesRepairer.setValue(IPreferenceConstants.P_LAYOUT_OF_PARENT, true);
    }
    // add new JPanel
    ContainerInfo newPanel;
    try {
      newPanel = createJavaInfo("javax.swing.JPanel");
      // do add
      try {
        panel.startEdit();
        parentLayout.add(newPanel, null);
      } finally {
        panel.endEdit();
      }
      // execute pending async's
      waitEventLoop(1);
    } finally {
      preferencesRepairer.restore();
    }
    // check result
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new GridLayout());",
        "    {",
        "      JPanel panel = new JPanel();",
        "      add(panel);",
        "      panel.setLayout(new GridLayout(1, 0, 0, 0));",
        "    }",
        "  }",
        "}");
    assertNotSame(parentLayout, newPanel.getLayout());
  }

  /**
   * Test for {@link IPreferenceConstants#P_LAYOUT_OF_PARENT}, that enables layout inheritance.
   */
  public void test_inheritParentLayout_null() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(null);",
            "  }",
            "}");
    AbsoluteLayoutInfo parentLayout = (AbsoluteLayoutInfo) panel.getLayout();
    // prepare preferences
    PreferencesRepairer preferencesRepairer;
    {
      IPreferenceStore preferences = panel.getDescription().getToolkit().getPreferences();
      preferencesRepairer = new PreferencesRepairer(preferences);
      // no inheritance by default
      assertFalse(preferences.getBoolean(IPreferenceConstants.P_LAYOUT_OF_PARENT));
      // enable inheritance
      preferencesRepairer.setValue(IPreferenceConstants.P_LAYOUT_OF_PARENT, true);
    }
    // add new JPanel
    ContainerInfo newPanel;
    try {
      newPanel = createJavaInfo("javax.swing.JPanel");
      // do add
      try {
        panel.startEdit();
        parentLayout.command_CREATE(newPanel, null);
      } finally {
        panel.endEdit();
      }
      // execute pending async's
      waitEventLoop(1);
    } finally {
      preferencesRepairer.restore();
    }
    // check result
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(null);",
        "    {",
        "      JPanel panel = new JPanel();",
        "      add(panel);",
        "      panel.setLayout(null);",
        "    }",
        "  }",
        "}");
    assertNotSame(parentLayout, newPanel.getLayout());
  }

  /**
   * Test for {@link IPreferenceConstants#P_LAYOUT_DEFAULT}, i.e. installation for default layout.
   */
  public void test_setDefaultLayout() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    FlowLayoutInfo flowLayout = (FlowLayoutInfo) panel.getLayout();
    // prepare preferences
    PreferencesRepairer preferencesRepairer;
    {
      IPreferenceStore preferences = panel.getDescription().getToolkit().getPreferences();
      preferencesRepairer = new PreferencesRepairer(preferences);
      // no default layout by default
      assertTrue(StringUtils.isEmpty(preferences.getString(IPreferenceConstants.P_LAYOUT_DEFAULT)));
      // use BorderLayout as default
      preferencesRepairer.setValue(IPreferenceConstants.P_LAYOUT_DEFAULT, "borderLayout");
    }
    // add new JPanel
    ContainerInfo newPanel;
    try {
      newPanel = createJavaInfo("javax.swing.JPanel");
      // do add
      try {
        panel.startEdit();
        flowLayout.add(newPanel, null);
      } finally {
        panel.endEdit();
      }
      // execute pending async's
      waitEventLoop(1);
    } finally {
      preferencesRepairer.restore();
    }
    // check result
    assertEditor(
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "    {",
        "      JPanel panel = new JPanel();",
        "      add(panel);",
        "      panel.setLayout(new BorderLayout(0, 0));",
        "    }",
        "  }",
        "}");
    assertNotSame(flowLayout, newPanel.getLayout());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Clipboard
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for copy/paste.
   */
  public void test_clipboard() throws Exception {
    String[] lines1 =
        {
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      JPanel inner = new JPanel();",
            "      inner.setLayout(new GridLayout(1, 0));",
            "      add(inner);",
            "    }",
            "  }",
            "}"};
    final ContainerInfo panel = parseContainer(lines1);
    panel.refresh();
    // prepare memento
    final JavaInfoMemento memento;
    {
      ComponentInfo inner = panel.getChildrenComponents().get(0);
      memento = JavaInfoMemento.createMemento(inner);
    }
    // add copy
    ExecutionUtils.run(panel, new RunnableEx() {
      public void run() throws Exception {
        ContainerInfo copy = (ContainerInfo) memento.create(panel);
        ((FlowLayoutInfo) panel.getLayout()).add(copy, null);
        memento.apply();
      }
    });
    String[] lines =
        {
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      JPanel inner = new JPanel();",
            "      inner.setLayout(new GridLayout(1, 0));",
            "      add(inner);",
            "    }",
            "    {",
            "      JPanel panel = new JPanel();",
            "      add(panel);",
            "      panel.setLayout(new GridLayout(1, 0));",
            "    }",
            "  }",
            "}"};
    assertEditor(lines);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // "Set Layout" action in context menu
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * No "Set Layout" sub-menu if {@link ContainerInfo} has no layout.
   */
  public void test_setLayoutMenu_0() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    add(new JButton());",
            "  }",
            "}");
    ContainerInfo button = (ContainerInfo) panel.getChildrenComponents().get(0);
    // no layout
    assertFalse(button.hasLayout());
    // ...so, no "Set layout" menu
    {
      IMenuManager menuManager = getContextMenu(button);
      IMenuManager layoutManager = findChildMenuManager(menuManager, "Set layout");
      assertNull(layoutManager);
    }
  }

  /**
   * Test that {@link CompositeInfo} contributes "Set layout" sub-menu in context menu.
   */
  public void test_setLayoutMenu_1() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    assertTrue(panel.hasLayout());
    // prepare "Set Layout" menu manager
    IMenuManager layoutManager = get_SetLayout_MenuManager(panel);
    // check for existing actions
    assertChildAction_existsEnabled(layoutManager, "FlowLayout", true);
    assertChildAction_existsEnabled(layoutManager, "BorderLayout", true);
    assertChildAction_existsEnabled(layoutManager, "BoxLayout", true);
    assertChildAction_existsEnabled(layoutManager, "GridLayout", true);
    assertChildAction_existsEnabled(layoutManager, "CardLayout", true);
    assertChildAction_existsEnabled(layoutManager, "GridBagLayout", true);
    // use one of the actions to set new layout
    {
      IAction action = findChildAction(layoutManager, "GridLayout");
      action.run();
      assertEditor(
          "// filler filler filler",
          "public class Test extends JPanel {",
          "  public Test() {",
          "    setLayout(new GridLayout(1, 0, 0, 0));",
          "  }",
          "}");
    }
    // set "absolute" layout
    {
      IAction action = findChildAction(layoutManager, "Absolute layout");
      action.run();
      assertEditor(
          "// filler filler filler",
          "public class Test extends JPanel {",
          "  public Test() {",
          "    setLayout(null);",
          "  }",
          "}");
    }
  }

  /**
   * Tests that {@link ContainerInfo} has item <code>"FormLayout"</code> and it works on clean
   * project, without added {@link FormLayout} jar.
   */
  public void test_setLayoutMenu_2() throws Exception {
    do_projectDispose();
    do_projectCreate();
    try {
      ContainerInfo panel =
          parseContainer(
              "// filler filler filler",
              "public class Test extends JPanel {",
              "  public Test() {",
              "  }",
              "}");
      assertTrue(panel.hasLayout());
      IMenuManager layoutManager = get_SetLayout_MenuManager(panel);
      // use one of the actions to set new layout
      {
        IAction action = findChildAction(layoutManager, "JGoodies FormLayout");
        action.run();
        assertEditor(
            "import com.jgoodies.forms.layout.FormLayout;",
            "import com.jgoodies.forms.layout.ColumnSpec;",
            "import com.jgoodies.forms.layout.RowSpec;",
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new FormLayout(new ColumnSpec[] {},",
            "      new RowSpec[] {}));",
            "  }",
            "}");
      }
    } finally {
      do_projectDispose();
    }
  }

  private static IMenuManager get_SetLayout_MenuManager(ContainerInfo panel) throws Exception {
    IMenuManager menuManager = getContextMenu(panel);
    IMenuManager layoutManager = findChildMenuManager(menuManager, "Set layout");
    return layoutManager;
  }

  private static void assertChildAction_existsEnabled(IMenuManager layoutManager,
      String text,
      boolean enabled) {
    IAction action = findChildAction(layoutManager, text);
    assertThat(action).isNotNull();
    assertThat(action.isEnabled()).isEqualTo(enabled);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // hasLayout()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ContainerInfo#hasLayout()}.
   * <p>
   * No layout for placeholder.
   */
  public void test_hasLayout_placeholder() throws Exception {
    setFileContentSrc(
        "test/MyContainer.java",
        getTestSource(
            "// filler filler filler filler filler",
            "public class MyContainer extends JPanel {",
            "  public MyContainer() {",
            "    throw new IllegalStateException('actual');",
            "  }",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "    add(new MyContainer());",
            "  }",
            "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {/add(new MyContainer())/}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {new: test.MyContainer} {empty} {/add(new MyContainer())/}");
    panel.refresh();
    ContainerInfo placeholder = (ContainerInfo) panel.getChildrenComponents().get(0);
    // no Layout for placeholder
    assertFalse(placeholder.hasLayout());
    // ...and can not set new
    assertFalse(placeholder.canSetLayout());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // canSetLayout()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ContainerInfo#canSetLayout()}.
   */
  public void test_canSetLayout_enabled() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    // has Layout
    assertTrue(panel.hasLayout());
    // can change it
    assertTrue(panel.canSetLayout());
  }

  /**
   * Test for {@link ContainerInfo#canSetLayout()}.
   * <p>
   * If execution of {@link Container#setLayout(java.awt.LayoutManager)} is disabled, then we can
   * not set new layout.
   */
  public void test_canSetLayout_disabled() throws Exception {
    prepareMyPanel_disabledSetLayout();
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class Test extends MyPanel {",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    // has Layout
    assertTrue(panel.hasLayout());
    // ...but can not change it
    assertFalse(panel.canSetLayout());
    // so, no "Set layout" context menu
    {
      IMenuManager manager = get_SetLayout_MenuManager(panel);
      assertNull(manager);
    }
  }

  /**
   * Prepares <code>test.MyPanel</code> class which does not accept new {@link LayoutManager}.
   */
  public static void prepareMyPanel_disabledSetLayout() throws Exception {
    setFileContentSrc(
        "test/MyPanel.java",
        getSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "package test;",
            "import javax.swing.JPanel;",
            "public class MyPanel extends JPanel {",
            "  public MyPanel() {",
            "  }",
            "}"));
    setFileContentSrc(
        "test/MyPanel.wbp-component.xml",
        getSourceDQ(
            "<?xml version='1.0' encoding='UTF-8'?>",
            "<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
            "  <methods>",
            "    <methods-exclude signature='setLayout(java.awt.LayoutManager)'/>",
            "  </methods>",
            "</component>"));
    waitForAutoBuild();
  }
}
