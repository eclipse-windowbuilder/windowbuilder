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
package org.eclipse.wb.tests.designer.rcp.model.jface;

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.internal.core.model.menu.IMenuInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuItemInfo;
import org.eclipse.wb.internal.core.model.menu.MenuObjectInfoUtils;
import org.eclipse.wb.internal.rcp.gef.policy.jface.action.ActionDropTool;
import org.eclipse.wb.internal.rcp.model.jface.ApplicationWindowInfo;
import org.eclipse.wb.internal.rcp.model.jface.action.ActionContainerInfo;
import org.eclipse.wb.internal.rcp.model.jface.action.ActionContributionItemInfo;
import org.eclipse.wb.internal.rcp.model.jface.action.ActionInfo;
import org.eclipse.wb.internal.rcp.model.jface.action.MenuManagerInfo;
import org.eclipse.wb.tests.designer.rcp.RcpGefTest;
import org.eclipse.wb.tests.designer.swt.model.menu.MenuFeedbackTester;
import org.eclipse.wb.tests.gef.GraphicalRobot;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for {@link MenuManagerInfo} in GEF.
 * 
 * @author scheglov_ke
 */
public class MenuManagerGefTest extends RcpGefTest {
  private MenuFeedbackTester menuTester;

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
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void fetchContentFields() {
    super.fetchContentFields();
    menuTester = new MenuFeedbackTester(canvas);
  }

  @Override
  protected void tearDown() throws Exception {
    menuTester = null;
    super.tearDown();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Use existing {@link ActionInfo}, place it on {@link MenuManagerInfo}.
   */
  public void test_CREATE() throws Exception {
    ApplicationWindowInfo window =
        (ApplicationWindowInfo) openJavaInfo(
            "import org.eclipse.jface.action.*;",
            "import org.eclipse.jface.window.*;",
            "public class Test extends ApplicationWindow {",
            "  private IAction action;",
            "  public Test(Shell parentShell) {",
            "    super(parentShell);",
            "    createActions();",
            "    addMenuBar();",
            "  }",
            "  private void createActions() {",
            "    action = new Action(\"The Action\") {",
            "    };",
            "  }",
            "  protected MenuManager createMenuManager() {",
            "    MenuManager menuManager = super.createMenuManager();",
            "    {",
            "      MenuManager menuManager_1 = new MenuManager(\"Menu 1\");",
            "      menuManager.add(menuManager_1);",
            "    }",
            "    return menuManager;",
            "  }",
            "}");
    MenuManagerInfo menuManagerInfo = window.getChildren(MenuManagerInfo.class).get(0);
    MenuManagerInfo menuManagerInfo_1 = (MenuManagerInfo) menuManagerInfo.getItems().get(0);
    // prepare Menu models
    IMenuItemInfo menuManager_ItemObject_1 = MenuObjectInfoUtils.getMenuItemInfo(menuManagerInfo_1);
    IMenuInfo menuManager_MenuObject_1 = MenuObjectInfoUtils.getMenuInfo(menuManagerInfo_1);
    assertNotNull(menuManager_ItemObject_1);
    assertNotNull(menuManager_MenuObject_1);
    // prepare EditPart's
    EditPart menuManagerPart = canvas.getEditPart(menuManagerInfo);
    GraphicalEditPart menuManager_itemPart_1 = canvas.getEditPart(menuManagerInfo_1);
    assertNotNull(menuManagerPart);
    assertNotNull(menuManager_itemPart_1);
    // initially sub-menu's are not visible
    assertThat(menuManager_itemPart_1.getChildren()).isEmpty();
    canvas.assertNullEditPart(menuManager_MenuObject_1);
    // load Action_DropTool
    {
      ActionInfo action = ActionContainerInfo.getActions(window).get(0);
      ActionDropTool tool = new ActionDropTool(action);
      m_viewerCanvas.getEditDomain().setActiveTool(tool);
    }
    // drop "action" into "menuManager_1"
    EditPart menuManager_menuPart_1;
    {
      // move on "menuManager_1" item: so menu shown
      canvas.moveTo(
          menuManager_itemPart_1,
          menuManager_itemPart_1.getFigure().getSize().width / 2,
          0);
      menuTester.assertMenuFeedbacks(GraphicalRobot.getTargetPredicate(menuManager_itemPart_1));
      menuManager_menuPart_1 = canvas.getEditPart(menuManager_MenuObject_1);
      // move on menu
      canvas.moveTo(menuManager_menuPart_1);
      menuTester.assertMenuFeedbacks(canvas.getEmptyFlowContainerPredicate(
          menuManager_menuPart_1,
          false));
      // click to finish creation on "menuManager_1"
      canvas.click();
    }
    // check source
    assertEditor(
        "import org.eclipse.jface.action.*;",
        "import org.eclipse.jface.window.*;",
        "public class Test extends ApplicationWindow {",
        "  private IAction action;",
        "  public Test(Shell parentShell) {",
        "    super(parentShell);",
        "    createActions();",
        "    addMenuBar();",
        "  }",
        "  private void createActions() {",
        "    action = new Action('The Action') {",
        "    };",
        "  }",
        "  protected MenuManager createMenuManager() {",
        "    MenuManager menuManager = super.createMenuManager();",
        "    {",
        "      MenuManager menuManager_1 = new MenuManager('Menu 1');",
        "      menuManager.add(menuManager_1);",
        "      menuManager_1.add(action);",
        "    }",
        "    return menuManager;",
        "  }",
        "}");
  }

  public void test_MOVE() throws Exception {
    ApplicationWindowInfo window =
        (ApplicationWindowInfo) openJavaInfo(
            "import org.eclipse.jface.action.*;",
            "import org.eclipse.jface.window.*;",
            "public class Test extends ApplicationWindow {",
            "  private IAction action;",
            "  public Test(Shell parentShell) {",
            "    super(parentShell);",
            "    createActions();",
            "    addMenuBar();",
            "  }",
            "  private void createActions() {",
            "    action = new Action(\"The Action\") {",
            "    };",
            "  }",
            "  protected MenuManager createMenuManager() {",
            "    MenuManager menuManager = super.createMenuManager();",
            "    {",
            "      MenuManager menuManager_1 = new MenuManager(\"Menu 1\");",
            "      menuManager_1.add(action);",
            "      menuManager.add(menuManager_1);",
            "    }",
            "    {",
            "      MenuManager menuManager_2 = new MenuManager(\"Menu 2\");",
            "      menuManager.add(menuManager_2);",
            "    }",
            "    return menuManager;",
            "  }",
            "}");
    MenuManagerInfo menuManagerInfo = window.getChildren(MenuManagerInfo.class).get(0);
    MenuManagerInfo menuManagerInfo_1 = (MenuManagerInfo) menuManagerInfo.getItems().get(0);
    MenuManagerInfo menuManagerInfo_2 = (MenuManagerInfo) menuManagerInfo.getItems().get(1);
    ActionContributionItemInfo itemInfo =
        (ActionContributionItemInfo) menuManagerInfo_1.getItems().get(0);
    // prepare Menu models
    IMenuItemInfo menuManager_ItemObject_1 = MenuObjectInfoUtils.getMenuItemInfo(menuManagerInfo_1);
    IMenuItemInfo menuManager_ItemObject_2 = MenuObjectInfoUtils.getMenuItemInfo(menuManagerInfo_2);
    IMenuInfo menuManager_MenuObject_1 = MenuObjectInfoUtils.getMenuInfo(menuManagerInfo_1);
    IMenuInfo menuManager_MenuObject_2 = MenuObjectInfoUtils.getMenuInfo(menuManagerInfo_2);
    IMenuItemInfo item_ItemObject = menuManager_MenuObject_1.getItems().get(0);
    assertNotNull(menuManager_ItemObject_1);
    assertNotNull(menuManager_ItemObject_2);
    assertNotNull(menuManager_MenuObject_1);
    assertNotNull(menuManager_MenuObject_2);
    assertNotNull(item_ItemObject);
    // prepare EditPart's
    EditPart menuManagerPart = canvas.getEditPart(menuManagerInfo);
    GraphicalEditPart menuManager_itemPart_1 = canvas.getEditPart(menuManagerInfo_1);
    GraphicalEditPart menuManager_itemPart_2 = canvas.getEditPart(menuManagerInfo_2);
    assertNotNull(menuManagerPart);
    assertNotNull(menuManager_itemPart_1);
    assertNotNull(menuManager_itemPart_2);
    // initially sub-menu's are not visible
    assertThat(menuManager_itemPart_1.getChildren()).isEmpty();
    assertThat(menuManager_itemPart_2.getChildren()).isEmpty();
    canvas.assertNullEditPart(menuManager_MenuObject_1);
    canvas.assertNullEditPart(menuManager_MenuObject_2);
    canvas.assertNullEditPart(itemInfo);
    // select "action" in tree: menu for "menuManager_1" appears
    GraphicalEditPart itemPart;
    {
      tree.select(itemInfo);
      // "menuManager_1" menu is visible
      EditPart menuManager_MenuPart_1 = canvas.getEditPart(menuManager_MenuObject_1);
      assertNotNull(menuManager_MenuPart_1);
      // "item" has EditPart
      itemPart = canvas.getEditPart(itemInfo);
      assertNotNull(itemPart);
      // check selection
      assertEquals(EditPart.SELECTED_PRIMARY, itemPart.getSelected());
    }
    // move "action" into "menuManager_2"
    EditPart menuManager_menuPart_2;
    {
      canvas.beginDrag(itemPart);
      // "menu" is not visible yet
      canvas.assertNullEditPart(menuManager_MenuObject_2);
      // move on "menuManager_2" item: so menu shown
      canvas.dragTo(
          menuManager_itemPart_2,
          menuManager_itemPart_2.getFigure().getSize().width / 2,
          0);
      menuTester.assertFeedback_selection_target(itemPart, menuManager_itemPart_2);
      menuManager_menuPart_2 = canvas.getEditPart(menuManager_MenuObject_2);
      assertNotNull(menuManager_menuPart_2);
      // move on menu
      canvas.dragTo(menuManager_menuPart_2);
      menuTester.assertFeedback_selection_emptyFlow(itemPart, menuManager_menuPart_2, false);
      // end drag, "item" moved on "menuManager_2"
      canvas.endDrag();
    }
    // check source
    assertEditor(
        "import org.eclipse.jface.action.*;",
        "import org.eclipse.jface.window.*;",
        "public class Test extends ApplicationWindow {",
        "  private IAction action;",
        "  public Test(Shell parentShell) {",
        "    super(parentShell);",
        "    createActions();",
        "    addMenuBar();",
        "  }",
        "  private void createActions() {",
        "    action = new Action('The Action') {",
        "    };",
        "  }",
        "  protected MenuManager createMenuManager() {",
        "    MenuManager menuManager = super.createMenuManager();",
        "    {",
        "      MenuManager menuManager_1 = new MenuManager('Menu 1');",
        "      menuManager.add(menuManager_1);",
        "    }",
        "    {",
        "      MenuManager menuManager_2 = new MenuManager('Menu 2');",
        "      menuManager.add(menuManager_2);",
        "      menuManager_2.add(action);",
        "    }",
        "    return menuManager;",
        "  }",
        "}");
  }
}
