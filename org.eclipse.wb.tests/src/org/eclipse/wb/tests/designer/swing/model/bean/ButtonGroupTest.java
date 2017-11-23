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
package org.eclipse.wb.tests.designer.swing.model.bean;

import com.google.common.collect.ImmutableList;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.ObjectReferenceInfo;
import org.eclipse.wb.internal.swing.model.bean.ButtonGroupContainerInfo;
import org.eclipse.wb.internal.swing.model.bean.ButtonGroupInfo;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;
import org.eclipse.wb.tests.gef.UIPredicate;
import org.eclipse.wb.tests.gef.UIRunnable;
import org.eclipse.wb.tests.gef.UiContext;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;

/**
 * Test for {@link ButtonGroupInfo}.
 * 
 * @author scheglov_ke
 */
public class ButtonGroupTest extends SwingModelTest {
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
   * No {@link ButtonGroup}'s - no {@link ButtonGroupContainerInfo} and {@link ButtonGroupInfo}'s.
   */
  public void test_noButtonGroups() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    // no ButtonGroupContainerInfo and ButtonGroupInfo's
    assertEquals(0, panel.getChildren(ButtonGroupContainerInfo.class).size());
    assertEquals(0, ButtonGroupContainerInfo.getButtonGroups(panel).size());
    // still no ButtonGroupContainerInfo
    assertEquals(0, panel.getChildren(ButtonGroupContainerInfo.class).size());
  }

  /**
   * Test that we can parse {@link ButtonGroup} creation and its
   * {@link ButtonGroup#add(javax.swing.AbstractButton)} is executed.
   */
  public void test_parse() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private final ButtonGroup m_buttonGroup = new ButtonGroup();",
            "  public Test() {",
            "    {",
            "      JRadioButton button_0 = new JRadioButton();",
            "      add(button_0);",
            "      m_buttonGroup.add(button_0);",
            "    }",
            "    {",
            "      JRadioButton button_1 = new JRadioButton();",
            "      add(button_1);",
            "      m_buttonGroup.add(button_1);",
            "    }",
            "    {",
            "      JRadioButton button_2 = new JRadioButton();",
            "      add(button_2);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    ComponentInfo buttonInfo_0 = panel.getChildrenComponents().get(0);
    ComponentInfo buttonInfo_1 = panel.getChildrenComponents().get(1);
    ComponentInfo buttonInfo_2 = panel.getChildrenComponents().get(2);
    // test ButtonGroupContainerInfo
    {
      ButtonGroupContainerInfo container = ButtonGroupContainerInfo.get(panel);
      assertNotNull(container);
      assertEquals(1, container.getChildren().size());
      // ButtonGroupContainerInfo can not be deleted
      assertFalse(container.canDelete());
      container.delete();
      // presentation
      assertEquals("(button groups)", container.getPresentation().getText());
      assertNotNull(container.getPresentation().getIcon());
    }
    // test for ButtonGroup's
    {
      // try to ask ButtonGroup's
      List<ButtonGroupInfo> buttonGroups = ButtonGroupContainerInfo.getButtonGroups(panel);
      assertEquals(1, buttonGroups.size());
      // check sole ButtonGroupInfo
      ButtonGroupInfo buttonGroup = buttonGroups.get(0);
      assertThat(buttonGroup.getAssociation()).isNotNull();
      assertEquals("m_buttonGroup", buttonGroup.getVariableSupport().getName());
      // test that group has bound JRadioButton's
      {
        List<ObjectReferenceInfo> buttons = buttonGroup.getButtons();
        assertEquals(2, buttons.size());
        assertSame(buttonInfo_0, buttons.get(0).getObject());
        assertSame(buttonInfo_1, buttons.get(1).getObject());
      }
      // ...and in presentation/tree children too
      {
        List<ObjectInfo> buttons = buttonGroup.getPresentation().getChildrenTree();
        assertEquals(2, buttons.size());
        assertSame(buttonInfo_0, ((ObjectReferenceInfo) buttons.get(0)).getObject());
        assertSame(buttonInfo_1, ((ObjectReferenceInfo) buttons.get(1)).getObject());
      }
      // ...and when we ask using hasButton()
      {
        assertTrue(buttonGroup.hasButton(buttonInfo_0));
        assertTrue(buttonGroup.hasButton(buttonInfo_1));
        assertFalse(buttonGroup.hasButton(buttonInfo_2));
      }
    }
    // test that ButtonGroup works, so we installed it correctly
    {
      JRadioButton button_1 = (JRadioButton) buttonInfo_0.getComponent();
      JRadioButton button_2 = (JRadioButton) buttonInfo_1.getComponent();
      // no selection initially
      assertFalse(button_1.isSelected());
      assertFalse(button_2.isSelected());
      // select "button_1", "button_2" is not selected
      button_1.setSelected(true);
      assertTrue(button_1.isSelected());
      assertFalse(button_2.isSelected());
      // select "button_2", "button_1" is not selected anymore
      button_2.setSelected(true);
      assertFalse(button_1.isSelected());
      assertTrue(button_2.isSelected());
    }
  }

  /**
   * Test that we can parse custom {@link ButtonGroup}.
   */
  public void test_parse_customButtonGroup() throws Exception {
    setFileContentSrc(
        "test/MyButtonGroup.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyButtonGroup extends ButtonGroup {",
            "}"));
    waitForAutoBuild();
    // parse
    parseContainer(
        "public class Test extends JPanel {",
        "  private final MyButtonGroup m_buttonGroup = new MyButtonGroup();",
        "  public Test() {",
        "  }",
        "}");
    assertHierarchy(
        "{this: javax.swing.JPanel} {this} {}",
        "  {implicit-layout: java.awt.FlowLayout} {implicit-layout} {}",
        "  {org.eclipse.wb.internal.swing.model.bean.ButtonGroupContainerInfo}",
        "    {new: test.MyButtonGroup} {field-initializer: m_buttonGroup} {/new MyButtonGroup()/}");
  }

  /**
   * Add new {@link AbstractButton} to {@link ButtonGroupInfo}.
   */
  public void test_addButton_new() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private final ButtonGroup m_buttonGroup = new ButtonGroup();",
            "  public Test() {",
            "    {",
            "      JRadioButton button = new JRadioButton();",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    ComponentInfo button = panel.getChildrenComponents().get(0);
    ButtonGroupInfo buttonGroup = ButtonGroupContainerInfo.getButtonGroups(panel).get(0);
    // add button
    buttonGroup.addButton(button);
    assertEditor(
        "public class Test extends JPanel {",
        "  private final ButtonGroup m_buttonGroup = new ButtonGroup();",
        "  public Test() {",
        "    {",
        "      JRadioButton button = new JRadioButton();",
        "      m_buttonGroup.add(button);",
        "      add(button);",
        "    }",
        "  }",
        "}");
    // "button" now in "buttonGroup"
    {
      List<ObjectReferenceInfo> buttons = buttonGroup.getButtons();
      assertEquals(1, buttons.size());
      assertSame(button, buttons.get(0).getObject());
    }
  }

  /**
   * Add new {@link AbstractButton} to {@link ButtonGroupInfo}.<br>
   * Exclude first from existing {@link ButtonGroupInfo}.
   */
  public void test_addButton_inOtherGroup() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private final ButtonGroup m_buttonGroup_1 = new ButtonGroup();",
            "  private final ButtonGroup m_buttonGroup_2 = new ButtonGroup();",
            "  public Test() {",
            "    {",
            "      JRadioButton button = new JRadioButton();",
            "      m_buttonGroup_1.add(button);",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    // prepare models
    ComponentInfo button = panel.getChildrenComponents().get(0);
    ButtonGroupInfo buttonGroup_1 = ButtonGroupContainerInfo.getButtonGroups(panel).get(0);
    ButtonGroupInfo buttonGroup_2 = ButtonGroupContainerInfo.getButtonGroups(panel).get(1);
    assertTrue(buttonGroup_1.hasButton(button));
    assertFalse(buttonGroup_2.hasButton(button));
    // add button
    buttonGroup_2.addButton(button);
    assertEditor(
        "public class Test extends JPanel {",
        "  private final ButtonGroup m_buttonGroup_1 = new ButtonGroup();",
        "  private final ButtonGroup m_buttonGroup_2 = new ButtonGroup();",
        "  public Test() {",
        "    {",
        "      JRadioButton button = new JRadioButton();",
        "      m_buttonGroup_2.add(button);",
        "      add(button);",
        "    }",
        "  }",
        "}");
    // "button" now in "buttonGroup_2"
    assertFalse(buttonGroup_1.hasButton(button));
    assertTrue(buttonGroup_2.hasButton(button));
  }

  /**
   * Exclude {@link AbstractButton} to from any {@link ButtonGroupInfo}.
   */
  public void test_clearButton() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private final ButtonGroup m_buttonGroup = new ButtonGroup();",
            "  public Test() {",
            "    {",
            "      JRadioButton button = new JRadioButton();",
            "      m_buttonGroup.add(button);",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    // prepare models
    ComponentInfo button = panel.getChildrenComponents().get(0);
    ButtonGroupInfo buttonGroup = ButtonGroupContainerInfo.getButtonGroups(panel).get(0);
    assertTrue(buttonGroup.hasButton(button));
    // clear button
    ButtonGroupInfo.clearButton(button);
    assertEditor(
        "public class Test extends JPanel {",
        "  private final ButtonGroup m_buttonGroup = new ButtonGroup();",
        "  public Test() {",
        "    {",
        "      JRadioButton button = new JRadioButton();",
        "      add(button);",
        "    }",
        "  }",
        "}");
    assertFalse(buttonGroup.hasButton(button));
  }

  /**
   * Add {@link AbstractButton} to new {@link ButtonGroupInfo}.
   */
  public void test_newButtonGroup() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      JRadioButton button = new JRadioButton();",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // add new ButtonGroupInfo
    ButtonGroupInfo buttonGroup = ButtonGroupContainerInfo.add(panel, "javax.swing.ButtonGroup");
    assertEditor(
        "public class Test extends JPanel {",
        "  private final ButtonGroup buttonGroup = new ButtonGroup();",
        "  public Test() {",
        "    {",
        "      JRadioButton button = new JRadioButton();",
        "      add(button);",
        "    }",
        "  }",
        "}");
    // add button
    buttonGroup.addButton(button);
    assertEditor(
        "public class Test extends JPanel {",
        "  private final ButtonGroup buttonGroup = new ButtonGroup();",
        "  public Test() {",
        "    {",
        "      JRadioButton button = new JRadioButton();",
        "      buttonGroup.add(button);",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Context menu
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * If selected {@link ComponentInfo} is not {@link AbstractButton}, then no
   * <code>"Set ButtonGroup"</code> menu.
   */
  public void test_contextMenu_notButton() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private final ButtonGroup m_buttonGroup = new ButtonGroup();",
            "  public Test() {",
            "  }",
            "}");
    panel.refresh();
    // no "Set ButtonGroup" expected
    IMenuManager designerMenu = getContextMenu(panel);
    IMenuManager groupsMenu = findChildMenuManager(designerMenu, "Set ButtonGroup");
    assertNull(groupsMenu);
  }

  /**
   * Set {@link ButtonGroup} for single {@link ComponentInfo}.
   */
  public void test_contextMenu_setGroup_single() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private final ButtonGroup m_buttonGroup = new ButtonGroup();",
            "  public Test() {",
            "    {",
            "      JRadioButton button = new JRadioButton();",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    // prepare models
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // set "m_buttonGroup"
    IAction buttonGroupAction = getButtonGroupAction("m_buttonGroup", button);
    buttonGroupAction.setChecked(true);
    buttonGroupAction.run();
    assertEditor(
        "public class Test extends JPanel {",
        "  private final ButtonGroup m_buttonGroup = new ButtonGroup();",
        "  public Test() {",
        "    {",
        "      JRadioButton button = new JRadioButton();",
        "      m_buttonGroup.add(button);",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Set {@link ButtonGroup} for multiple {@link ComponentInfo}.
   */
  public void test_contextMenu_setGroup_multiple() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private final ButtonGroup m_buttonGroup = new ButtonGroup();",
            "  public Test() {",
            "    {",
            "      JRadioButton button_1 = new JRadioButton();",
            "      add(button_1);",
            "    }",
            "    {",
            "      JRadioButton button_2 = new JRadioButton();",
            "      add(button_2);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    // prepare models
    ComponentInfo button_1 = panel.getChildrenComponents().get(0);
    ComponentInfo button_2 = panel.getChildrenComponents().get(1);
    // when we ask context menu for "not first" selected component, returned Action does nothing
    {
      MenuManager designerMenu = getDesignerMenuManager();
      panel.getBroadcastObject().addContextMenu(
          ImmutableList.of(button_1, button_2),
          button_2,
          designerMenu);
      IMenuManager groupsMenu = findChildMenuManager(designerMenu, "Set ButtonGroup");
      IAction buttonGroupAction = findChildAction(groupsMenu, "m_buttonGroup");
      // no changes expected
      String expectedSource = m_lastEditor.getSource();
      buttonGroupAction.setChecked(true);
      buttonGroupAction.run();
      assertEditor(expectedSource, m_lastEditor);
    }
    // set "m_buttonGroup"
    IAction buttonGroupAction = getButtonGroupAction("m_buttonGroup", button_1, button_2);
    buttonGroupAction.setChecked(true);
    buttonGroupAction.run();
    assertEditor(
        "public class Test extends JPanel {",
        "  private final ButtonGroup m_buttonGroup = new ButtonGroup();",
        "  public Test() {",
        "    {",
        "      JRadioButton button_1 = new JRadioButton();",
        "      m_buttonGroup.add(button_1);",
        "      add(button_1);",
        "    }",
        "    {",
        "      JRadioButton button_2 = new JRadioButton();",
        "      m_buttonGroup.add(button_2);",
        "      add(button_2);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Exclude {@link AbstractButton} to from existing {@link ButtonGroupInfo}.
   */
  public void test_contextMenu_noGroup() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  private final ButtonGroup m_buttonGroup = new ButtonGroup();",
            "  public Test() {",
            "    {",
            "      JRadioButton button = new JRadioButton();",
            "      m_buttonGroup.add(button);",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    // prepare models
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // do exclude
    {
      IAction noGroupAction = getButtonGroupAction("None", button);
      noGroupAction.run();
    }
    // check source
    assertEditor(
        "public class Test extends JPanel {",
        "  private final ButtonGroup m_buttonGroup = new ButtonGroup();",
        "  public Test() {",
        "    {",
        "      JRadioButton button = new JRadioButton();",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Set new {@link ButtonGroup} for single {@link AbstractButton}.
   */
  public void test_contextMenu_newGroup() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      JRadioButton button = new JRadioButton();",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    // prepare models
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // set new ButtonGroup
    IAction newGroupAction = getButtonGroupAction("New standard", button);
    newGroupAction.run();
    assertEditor(
        "public class Test extends JPanel {",
        "  private final ButtonGroup buttonGroup = new ButtonGroup();",
        "  public Test() {",
        "    {",
        "      JRadioButton button = new JRadioButton();",
        "      buttonGroup.add(button);",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Set new custom {@link ButtonGroup} for single {@link AbstractButton}.
   */
  public void test_contextMenu_newGroup_custom() throws Exception {
    setFileContentSrc(
        "test/MyButtonGroup.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyButtonGroup extends ButtonGroup {",
            "}"));
    waitForAutoBuild();
    // parse
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      JRadioButton button = new JRadioButton();",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    // prepare models
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // set new ButtonGroup
    final IAction newGroupAction = getButtonGroupAction("New custom...", button);
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        newGroupAction.run();
      }
    }, new UIRunnable() {
      public void run(UiContext context) throws Exception {
        // set filter
        {
          context.useShell("Open type");
          Text filterText = context.findFirstWidget(Text.class);
          filterText.setText("MyButtonGroup");
        }
        // wait for types
        {
          final Table typesTable = context.findFirstWidget(Table.class);
          context.waitFor(new UIPredicate() {
            public boolean check() {
              return typesTable.getItems().length != 0;
            }
          });
        }
        // click OK
        context.clickButton("OK");
      }
    });
    assertEditor(
        "public class Test extends JPanel {",
        "  private final MyButtonGroup myButtonGroup = new MyButtonGroup();",
        "  public Test() {",
        "    {",
        "      JRadioButton button = new JRadioButton();",
        "      myButtonGroup.add(button);",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Request setting new custom {@link ButtonGroup}, but cancel.
   */
  public void test_contextMenu_newGroup_custom_cancel() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      JRadioButton button = new JRadioButton();",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    // prepare models
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // set new ButtonGroup
    final IAction newGroupAction = getButtonGroupAction("New custom...", button);
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        newGroupAction.run();
      }
    }, new UIRunnable() {
      public void run(UiContext context) throws Exception {
        // set filter
        {
          context.useShell("Open type");
          Text filterText = context.findFirstWidget(Text.class);
          filterText.setText("java.lang.Object");
        }
        // wait for types
        {
          final Table typesTable = context.findFirstWidget(Table.class);
          context.waitFor(new UIPredicate() {
            public boolean check() {
              return typesTable.getItems().length != 0;
            }
          });
        }
        // click OK, but not ButtonGroup, so ignored
        context.clickButton("OK");
      }
    });
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    {",
        "      JRadioButton button = new JRadioButton();",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Request setting new custom {@link ButtonGroup}, but cancel.
   */
  public void test_contextMenu_newGroup_custom_notButtonGroup() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "public class Test extends JPanel {",
            "  public Test() {",
            "    {",
            "      JRadioButton button = new JRadioButton();",
            "      add(button);",
            "    }",
            "  }",
            "}");
    panel.refresh();
    // prepare models
    ComponentInfo button = panel.getChildrenComponents().get(0);
    // set new ButtonGroup
    final IAction newGroupAction = getButtonGroupAction("New custom...", button);
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        newGroupAction.run();
      }
    }, new UIRunnable() {
      public void run(UiContext context) throws Exception {
        context.useShell("Open type");
        context.clickButton("Cancel");
      }
    });
    assertEditor(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    {",
        "      JRadioButton button = new JRadioButton();",
        "      add(button);",
        "    }",
        "  }",
        "}");
  }

  /**
   * @return the existing {@link IAction} from "Set ButtonGroup" context menu.
   */
  private static IAction getButtonGroupAction(String text, ComponentInfo... buttons)
      throws Exception {
    IMenuManager designerMenu = getContextMenu(buttons);
    IMenuManager groupMenu = findChildMenuManager(designerMenu, "Set ButtonGroup");
    IAction action = findChildAction(groupMenu, text);
    assertNotNull(action);
    return action;
  }
}
