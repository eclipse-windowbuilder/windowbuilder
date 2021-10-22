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
package org.eclipse.wb.tests.designer.swing.model.layout.MigLayout;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.utils.ui.MenuIntersector;
import org.eclipse.wb.internal.swing.MigLayout.model.MigLayoutSurroundProcessor;
import org.eclipse.wb.internal.swing.MigLayout.model.MigLayoutSurroundSupport;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;

import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

/**
 * Tests for {@link MigLayoutSurroundSupport} and {@link MigLayoutSurroundProcessor}.
 *
 * @author scheglov_ke
 */
public class MigLayoutSurroundSupportTest extends AbstractMigLayoutTest {
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
  // Test
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Bad: two components on diagonal, and other component in same rectangle.
   */
  public void test_0() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new MigLayout());",
            "    {",
            "      JButton button_00 = new JButton();",
            "      add(button_00, 'cell 0 0');",
            "    }",
            "    {",
            "      JButton button_BAD = new JButton();",
            "      add(button_BAD, 'cell 0 1');",
            "    }",
            "    {",
            "      JButton button_11 = new JButton();",
            "      add(button_11, 'cell 1 1');",
            "    }",
            "  }",
            "}");
    panel.refresh();
    ComponentInfo button_00 = getButtons(panel).get(0);
    ComponentInfo button_11 = getButtons(panel).get(2);
    // no surround
    assertNoSurroundManager(panel, ImmutableList.of(button_00, button_11));
  }

  /**
   * Wrap {@link JTable} with {@link JScrollPane}.
   */
  public void test_1() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new MigLayout());",
            "    {",
            "      JTable table = new JTable();",
            "      add(table, 'cell 0 0');",
            "    }",
            "  }",
            "}");
    panel.refresh();
    ComponentInfo table = panel.getChildrenComponents().get(0);
    // run action
    runSurround("javax.swing.JScrollPane", table);
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new MigLayout('', '[]', '[]'));",
        "    {",
        "      JScrollPane scrollPane = new JScrollPane();",
        "      add(scrollPane, 'cell 0 0,grow');",
        "      {",
        "        JTable table = new JTable();",
        "        scrollPane.setViewportView(table);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  /**
   * Good: two components in single row, no other components.
   */
  public void test_2() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new MigLayout('', '[][]', '[]'));",
            "    {",
            "      JButton button_00 = new JButton();",
            "      add(button_00, 'cell 0 0');",
            "    }",
            "    {",
            "      JButton button_10 = new JButton();",
            "      add(button_10, 'cell 1 0');",
            "    }",
            "  }",
            "}");
    panel.refresh();
    ComponentInfo button_00 = getButtons(panel).get(0);
    ComponentInfo button_10 = getButtons(panel).get(1);
    // run action
    runSurround_Composite(button_00, button_10);
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new MigLayout('', '[]', '[]'));",
        "    {",
        "      JPanel panel = new JPanel();",
        "      add(panel, 'cell 0 0,grow');",
        "      panel.setLayout(new MigLayout('', '[][]', '[]'));",
        "      {",
        "        JButton button_00 = new JButton();",
        "        panel.add(button_00, 'cell 0 0');",
        "      }",
        "      {",
        "        JButton button_10 = new JButton();",
        "        panel.add(button_10, 'cell 1 0');",
        "      }",
        "    }",
        "  }",
        "}");
  }

  /**
   * Good: two components on diagonal, no other components.
   */
  public void test_3() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new MigLayout('', '[][]', '[][]'));",
            "    {",
            "      JButton button_00 = new JButton();",
            "      add(button_00, 'cell 0 0');",
            "    }",
            "    {",
            "      JButton button_11 = new JButton();",
            "      add(button_11, 'cell 1 1');",
            "    }",
            "  }",
            "}");
    panel.refresh();
    ComponentInfo button_00 = getButtons(panel).get(0);
    ComponentInfo button_11 = getButtons(panel).get(1);
    // run action
    runSurround_Composite(button_00, button_11);
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new MigLayout('', '[]', '[]'));",
        "    {",
        "      JPanel panel = new JPanel();",
        "      add(panel, 'cell 0 0,grow');",
        "      panel.setLayout(new MigLayout('', '[][]', '[][]'));",
        "      {",
        "        JButton button_00 = new JButton();",
        "        panel.add(button_00, 'cell 0 0');",
        "      }",
        "      {",
        "        JButton button_11 = new JButton();",
        "        panel.add(button_11, 'cell 1 1');",
        "      }",
        "    }",
        "  }",
        "}");
  }

  /**
   * Good: three components, one spanned horizontally.
   */
  public void test_4() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    setLayout(new MigLayout('', '[][]', '[][]'));",
            "    {",
            "      JButton button_00 = new JButton();",
            "      add(button_00, 'cell 0 0');",
            "    }",
            "    {",
            "      JButton button_10 = new JButton();",
            "      add(button_10, 'cell 1 0');",
            "    }",
            "    {",
            "      JButton button_01 = new JButton();",
            "      add(button_01, 'cell 0 1 2 1,growx,aligny bottom');",
            "    }",
            "  }",
            "}");
    panel.refresh();
    ComponentInfo button_00 = getButtons(panel).get(0);
    ComponentInfo button_10 = getButtons(panel).get(1);
    ComponentInfo button_01 = getButtons(panel).get(2);
    // run action
    runSurround_Composite(button_00, button_10, button_01);
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    setLayout(new MigLayout('', '[]', '[]'));",
        "    {",
        "      JPanel panel = new JPanel();",
        "      add(panel, 'cell 0 0,grow');",
        "      panel.setLayout(new MigLayout('', '[][]', '[][]'));",
        "      {",
        "        JButton button_00 = new JButton();",
        "        panel.add(button_00, 'cell 0 0');",
        "      }",
        "      {",
        "        JButton button_10 = new JButton();",
        "        panel.add(button_10, 'cell 1 0');",
        "      }",
        "      {",
        "        JButton button_01 = new JButton();",
        "        panel.add(button_01, 'cell 0 1 2 1,growx,aligny bottom');",
        "      }",
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
   * @return the {@link ComponentInfo} models for {@link JButton} components.
   */
  private static List<ComponentInfo> getButtons(ContainerInfo parent) {
    List<ComponentInfo> buttons = Lists.newArrayList();
    for (ComponentInfo control : parent.getChildrenComponents()) {
      if (control.getDescription().getComponentClass().getName().equals("javax.swing.JButton")) {
        buttons.add(control);
      }
    }
    return buttons;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the "Surround with" {@link IMenuManager} for given {@link ComponentInfo}'s.
   */
  private static IMenuManager createSurroundManager(ObjectInfo object,
      List<? extends ObjectInfo> objects) throws Exception {
    IMenuManager resultMenuManager;
    if (objects.size() < 2) {
      resultMenuManager = getDesignerMenuManager();
      object.getBroadcastObject().addContextMenu(objects, object, resultMenuManager);
    } else {
      resultMenuManager = new MenuManager();
      // handle multi selection
      List<IMenuManager> managers = Lists.newArrayList();
      for (ObjectInfo object_ : objects) {
        IMenuManager manager = getDesignerMenuManager();
        object.getBroadcastObject().addContextMenu(objects, object_, manager);
        managers.add(manager);
      }
      // select common parts
      MenuIntersector.merge(resultMenuManager, managers);
    }
    // select "Surround with" sub-menu
    return findChildMenuManager(resultMenuManager, "Surround with");
  }

  /**
   * @return the surround {@link IAction} with given title.
   */
  private static IAction getSurroundAction(String actionText, ObjectInfo... objects)
      throws Exception {
    assertFalse(objects.length == 0);
    IMenuManager surroundManager = createSurroundManager(objects[0], ImmutableList.copyOf(objects));
    assertNotNull(surroundManager);
    return findChildAction(surroundManager, actionText);
  }

  /**
   * Asserts that there are no "Surround with" {@link IMenuManager} for given input.
   */
  private static void assertNoSurroundManager(ObjectInfo object, List<? extends ObjectInfo> objects)
      throws Exception {
    IMenuManager surroundManager = createSurroundManager(object, objects);
    assertNull(surroundManager);
  }

  /**
   * Runs action from "Surround with" {@link IMenuManager}.
   */
  private static void runSurround(String actionText, ObjectInfo... objects) throws Exception {
    IAction surroundAction = getSurroundAction(actionText, objects);
    assertNotNull(surroundAction);
    // run action
    surroundAction.run();
  }

  /**
   * Runs action from "Surround with" {@link IMenuManager} for {@link JPanel}.
   */
  private static void runSurround_Composite(ObjectInfo... objects) throws Exception {
    runSurround("javax.swing.JPanel", objects);
  }
}
