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

import org.eclipse.wb.core.controls.Separator;
import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.association.InvocationVoidAssociation;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.menu.IMenuInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuItemInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuPolicy;
import org.eclipse.wb.internal.core.model.menu.MenuObjectInfoUtils;
import org.eclipse.wb.internal.rcp.model.jface.ApplicationWindowInfo;
import org.eclipse.wb.internal.rcp.model.jface.action.ActionContainerInfo;
import org.eclipse.wb.internal.rcp.model.jface.action.ActionContributionItemInfo;
import org.eclipse.wb.internal.rcp.model.jface.action.ActionInfo;
import org.eclipse.wb.internal.rcp.model.jface.action.ContributionItemInfo;
import org.eclipse.wb.internal.rcp.model.jface.action.MenuManagerInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.widgets.Menu;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

/**
 * Test for {@link MenuManagerInfo}.
 * 
 * @author scheglov_ke
 */
public class MenuManagerTest extends RcpModelTest {
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
   * {@link MenuManager} in simple SWT application.
   */
  public void test_standalone() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "import org.eclipse.jface.action.*;",
            "import org.eclipse.jface.window.*;",
            "public class Test extends Shell {",
            "  public Test() {",
            "    MenuManager menuManager = new MenuManager();",
            "  }",
            "}");
    assertHierarchy(
        "{this: org.eclipse.swt.widgets.Shell} {this} {}",
        "  {implicit-layout: absolute} {implicit-layout} {}");
    // even empty "menuManager" has non-zero size
    shell.refresh();
    assertNoErrors(shell);
  }

  /**
   * Empty {@link MenuManagerInfo}.
   */
  public void test_empty() throws Exception {
    ApplicationWindowInfo window =
        parseJavaInfo(
            "import org.eclipse.jface.action.*;",
            "import org.eclipse.jface.window.*;",
            "public class Test extends ApplicationWindow {",
            "  public Test(Shell parentShell) {",
            "    super(parentShell);",
            "    addMenuBar();",
            "  }",
            "  protected MenuManager createMenuManager() {",
            "    MenuManager menuManager = super.createMenuManager();",
            "    return menuManager;",
            "  }",
            "}");
    // check hierarchy
    assertHierarchy(
        "{this: org.eclipse.jface.window.ApplicationWindow} {this} {/addMenuBar()/}",
        "  {superInvocation: super.createMenuManager()} {local-unique: menuManager} {/super.createMenuManager()/ /menuManager/}");
    MenuManagerInfo menuManager = window.getChildren(MenuManagerInfo.class).get(0);
    IMenuInfo menuObject = MenuObjectInfoUtils.getMenuInfo(menuManager);
    // even empty "menuManager" has non-zero size
    window.refresh();
    assertThat(menuObject.getBounds().width).isGreaterThan(300);
    assertThat(menuObject.getBounds().height).isGreaterThan(18);
  }

  /**
   * Test for bounds of children {@link ContributionItemInfo}.
   */
  public void test_itemBounds() throws Exception {
    ApplicationWindowInfo window =
        parseJavaInfo(
            "import org.eclipse.jface.action.*;",
            "import org.eclipse.jface.window.*;",
            "public class Test extends ApplicationWindow {",
            "  private IAction action_1;",
            "  private IAction action_2;",
            "  public Test(Shell parentShell) {",
            "    super(parentShell);",
            "    createActions();",
            "    addMenuBar();",
            "  }",
            "  private void createActions() {",
            "    action_1 = new Action('Action 1') {",
            "    };",
            "    action_2 = new Action('Action 2') {",
            "    };",
            "  }",
            "  protected MenuManager createMenuManager() {",
            "    MenuManager menuManager = super.createMenuManager();",
            "    menuManager.add(action_1);",
            "    menuManager.add(action_2);",
            "    menuManager.add(action_1);",
            "    return menuManager;",
            "  }",
            "}");
    window.refresh();
    // prepare Action's
    ActionInfo action_1;
    ActionInfo action_2;
    {
      List<ActionInfo> actions = ActionContainerInfo.getActions(window);
      assertThat(actions).hasSize(2);
      action_1 = actions.get(0);
      action_2 = actions.get(1);
    }
    // prepare Item's
    MenuManagerInfo menuManager = window.getChildren(MenuManagerInfo.class).get(0);
    ActionContributionItemInfo item_1;
    ActionContributionItemInfo item_2;
    ActionContributionItemInfo item_3;
    {
      List<ContributionItemInfo> items = menuManager.getChildren(ContributionItemInfo.class);
      assertThat(items).hasSize(3);
      item_1 = (ActionContributionItemInfo) items.get(0);
      item_2 = (ActionContributionItemInfo) items.get(1);
      item_3 = (ActionContributionItemInfo) items.get(2);
    }
    // check for actions
    assertSame(action_1, item_1.getAction());
    assertSame(action_2, item_2.getAction());
    assertSame(action_1, item_3.getAction());
    // check bounds
    {
      Rectangle bounds_1 = item_1.getBounds();
      Rectangle bounds_2 = item_2.getBounds();
      Rectangle bounds_3 = item_3.getBounds();
      assertThat(bounds_1.height).isGreaterThanOrEqualTo(18);
      assertThat(bounds_1.height).isEqualTo(bounds_2.height);
      assertThat(bounds_1.height).isEqualTo(bounds_3.height);
    }
    // unsupported adapter
    assertNull(menuManager.getAdapter(List.class));
  }

  /**
   * Test for {@link MenuManager} without child {@link ContributionItem}'s.
   */
  public void test_emptySubMenu() throws Exception {
    ApplicationWindowInfo window =
        parseJavaInfo(
            "import org.eclipse.jface.action.*;",
            "import org.eclipse.jface.window.*;",
            "public class Test extends ApplicationWindow {",
            "  public Test(Shell parentShell) {",
            "    super(parentShell);",
            "    addMenuBar();",
            "  }",
            "  protected MenuManager createMenuManager() {",
            "    MenuManager menuManager = super.createMenuManager();",
            "    {",
            "      MenuManager subMenuManager = new MenuManager('Sub-menu');",
            "      menuManager.add(subMenuManager);",
            "    }",
            "    return menuManager;",
            "  }",
            "}");
    window.refresh();
    // prepare components
    MenuManagerInfo menuInfo;
    MenuManagerInfo subMenuInfo;
    {
      menuInfo = window.getChildren(MenuManagerInfo.class).get(0);
      List<AbstractComponentInfo> menuItems = menuInfo.getItems();
      assertThat(menuItems).hasSize(1);
      subMenuInfo = (MenuManagerInfo) menuItems.get(0);
    }
    // "subMenuInfo" has no items, but still has non-zero bounds
    {
      IMenuInfo menuObject = MenuObjectInfoUtils.getMenuInfo(subMenuInfo);
      assertThat(menuObject.getBounds().width).isGreaterThan(50);
      assertThat(menuObject.getBounds().height).isGreaterThan(20);
    }
  }

  /**
   * <code>setVisible(false)</code> causes no {@link Menu} for {@link MenuManager}, so we ignore it.
   */
  public void test_ignore_setVisible() throws Exception {
    ApplicationWindowInfo window =
        parseJavaInfo(
            "import org.eclipse.jface.action.*;",
            "import org.eclipse.jface.window.*;",
            "public class Test extends ApplicationWindow {",
            "  public Test(Shell parentShell) {",
            "    super(parentShell);",
            "    addMenuBar();",
            "  }",
            "  protected MenuManager createMenuManager() {",
            "    MenuManager menuManager = super.createMenuManager();",
            "    {",
            "      MenuManager subMenuManager = new MenuManager('Sub-menu');",
            "      menuManager.add(subMenuManager);",
            "      subMenuManager.setVisible(false);",
            "    }",
            "    return menuManager;",
            "  }",
            "}");
    window.refresh();
    assertNoErrors(window);
  }

  /**
   * <code>setRemoveAllWhenShown(true)</code> causes no {@link Menu} for {@link MenuManager}, so we
   * ignore it.
   */
  public void test_ignore_setRemoveAllWhenShown_true_() throws Exception {
    ApplicationWindowInfo window =
        parseJavaInfo(
            "import org.eclipse.jface.action.*;",
            "import org.eclipse.jface.window.*;",
            "public class Test extends ApplicationWindow {",
            "  public Test(Shell parentShell) {",
            "    super(parentShell);",
            "    addMenuBar();",
            "  }",
            "  protected MenuManager createMenuManager() {",
            "    MenuManager menuManager = super.createMenuManager();",
            "    {",
            "      MenuManager subMenuManager = new MenuManager('Sub-menu');",
            "      subMenuManager.setRemoveAllWhenShown(true);",
            "      menuManager.add(subMenuManager);",
            "    }",
            "    return menuManager;",
            "  }",
            "}");
    window.refresh();
    assertNoErrors(window);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IMenuInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_IMenuInfo_0() throws Exception {
    ApplicationWindowInfo window =
        parseJavaInfo(
            "import org.eclipse.jface.action.*;",
            "import org.eclipse.jface.window.*;",
            "public class Test extends ApplicationWindow {",
            "  private IAction action_1;",
            "  private IAction action_2;",
            "  public Test(Shell parentShell) {",
            "    super(parentShell);",
            "    createActions();",
            "    addMenuBar();",
            "  }",
            "  private void createActions() {",
            "    action_1 = new Action('Action 1') {",
            "    };",
            "    action_2 = new Action('Action 2') {",
            "    };",
            "  }",
            "  protected MenuManager createMenuManager() {",
            "    MenuManager menuManager = super.createMenuManager();",
            "    menuManager.add(action_1);",
            "    menuManager.add(action_2);",
            "    menuManager.add(action_1);",
            "    return menuManager;",
            "  }",
            "}");
    window.refresh();
    // prepare Item's
    MenuManagerInfo menuInfo = window.getChildren(MenuManagerInfo.class).get(0);
    ActionContributionItemInfo itemInfo_1;
    ActionContributionItemInfo itemInfo_2;
    ActionContributionItemInfo itemInfo_3;
    {
      List<ContributionItemInfo> items = menuInfo.getChildren(ContributionItemInfo.class);
      assertThat(items).hasSize(3);
      itemInfo_1 = (ActionContributionItemInfo) items.get(0);
      itemInfo_2 = (ActionContributionItemInfo) items.get(1);
      itemInfo_3 = (ActionContributionItemInfo) items.get(2);
    }
    // check "menuInfo"
    assertTrue(menuInfo.isBar());
    // check IMenuInfo for "menuInfo"
    {
      IMenuInfo menuObject = MenuObjectInfoUtils.getMenuInfo(menuInfo);
      assertSame(menuObject, menuObject.getModel());
      // presentation
      assertNull(menuObject.getImage());
      assertThat(menuObject.getBounds().width).isGreaterThan(400);
      assertThat(menuObject.getBounds().height).isGreaterThan(18);
      // access
      assertTrue(menuObject.isHorizontal());
    }
    // check items
    assertNotNull(itemInfo_1.getBounds());
    assertNotNull(itemInfo_2.getBounds());
    assertNotNull(itemInfo_3.getBounds());
  }

  /**
   * Test for {@link MenuManagerInfo} models.
   */
  public void test_IMenuInfo_1() throws Exception {
    ApplicationWindowInfo window =
        parseJavaInfo(
            "import org.eclipse.jface.action.*;",
            "import org.eclipse.jface.window.*;",
            "public class Test extends ApplicationWindow {",
            "  private IAction action_1;",
            "  private IAction action_2;",
            "  public Test(Shell parentShell) {",
            "    super(parentShell);",
            "    createActions();",
            "    addMenuBar();",
            "  }",
            "  private void createActions() {",
            "    action_1 = new Action('Action 1') {",
            "    };",
            "    action_2 = new Action('Action 2') {",
            "    };",
            "  }",
            "  protected MenuManager createMenuManager() {",
            "    MenuManager menuManager = super.createMenuManager();",
            "    menuManager.add(action_1);",
            "    {",
            "      MenuManager subMenuManager = new MenuManager('Sub-menu');",
            "      subMenuManager.add(action_2);",
            "      menuManager.add(subMenuManager);",
            "    }",
            "    return menuManager;",
            "  }",
            "}");
    window.refresh();
    // check hierarchy
    assertHierarchy(
        "{this: org.eclipse.jface.window.ApplicationWindow} {this} {/addMenuBar()/}",
        "  {superInvocation: super.createMenuManager()} {local-unique: menuManager} {/super.createMenuManager()/ /menuManager.add(action_1)/ /menuManager.add(subMenuManager)/ /menuManager/}",
        "    {void} {void} {/menuManager.add(action_1)/}",
        "    {new: org.eclipse.jface.action.MenuManager} {local-unique: subMenuManager} {/new MenuManager('Sub-menu')/ /subMenuManager.add(action_2)/ /menuManager.add(subMenuManager)/}",
        "      {void} {void} {/subMenuManager.add(action_2)/}",
        "  {org.eclipse.wb.internal.rcp.model.jface.action.ActionContainerInfo}",
        "    {new: org.eclipse.jface.action.Action} {field-unique: action_1} {/new Action('Action 1')/ /menuManager.add(action_1)/}",
        "    {new: org.eclipse.jface.action.Action} {field-unique: action_2} {/new Action('Action 2')/ /subMenuManager.add(action_2)/}");
    // prepare components
    MenuManagerInfo menuInfo;
    MenuManagerInfo subMenuInfo;
    ActionContributionItemInfo itemInfo_1;
    ActionContributionItemInfo itemInfo_2;
    {
      menuInfo = window.getChildren(MenuManagerInfo.class).get(0);
      List<AbstractComponentInfo> menuItems = menuInfo.getItems();
      assertThat(menuItems).hasSize(2);
      // children of "menuInfo"
      {
        itemInfo_1 = (ActionContributionItemInfo) menuItems.get(0);
        subMenuInfo = (MenuManagerInfo) menuItems.get(1);
        // child of "subMenuInfo"
        List<AbstractComponentInfo> subMenuItems = subMenuInfo.getItems();
        assertThat(subMenuItems).hasSize(1);
        itemInfo_2 = (ActionContributionItemInfo) subMenuItems.get(0);
      }
    }
    // check bar/not_bar
    assertTrue(menuInfo.isBar());
    assertFalse(subMenuInfo.isBar());
    // check "subMenuInfo"
    {
      assertNull(subMenuInfo.getImage());
      assertThat(subMenuInfo.getBounds().width).isGreaterThan(50);
      assertThat(subMenuInfo.getBounds().height).isGreaterThan(18);
    }
    // check "itemInfo"
    {
      assertNull(itemInfo_1.getImage());
      assertThat(itemInfo_1.getBounds().width).isGreaterThan(50);
      assertThat(itemInfo_1.getBounds().height).isGreaterThan(18);
    }
    // check IMenuInfo for "menuInfo"
    {
      IMenuInfo menuObject = MenuObjectInfoUtils.getMenuInfo(menuInfo);
      assertSame(menuObject, menuObject.getModel());
      // presentation
      assertNull(menuObject.getImage());
      assertThat(menuObject.getBounds().width).isGreaterThan(400);
      assertThat(menuObject.getBounds().height).isGreaterThan(18);
      // access
      assertTrue(menuObject.isHorizontal());
      // items
      List<IMenuItemInfo> items = menuObject.getItems();
      assertThat(items).hasSize(2);
      assertSame(subMenuInfo, items.get(1).getModel());
      // check IMenuItemInfo for "itemInfo_1"
      {
        IMenuItemInfo itemObject = items.get(0);
        assertSame(itemInfo_1, itemObject.getModel());
        // presentation
        assertNull(itemObject.getImage());
        assertSame(itemInfo_1.getImage(), itemObject.getImage());
        assertSame(itemInfo_1.getBounds(), itemObject.getBounds());
        assertThat(itemObject.getBounds().width).isGreaterThan(50);
        assertThat(itemObject.getBounds().height).isGreaterThan(18);
        // menu
        assertNull(itemObject.getMenu());
        assertSame(IMenuPolicy.NOOP, itemObject.getPolicy());
      }
    }
    // check IMenuItemInfo for "subMenuInfo"
    {
      IMenuItemInfo itemObject = MenuObjectInfoUtils.getMenuItemInfo(subMenuInfo);
      assertSame(subMenuInfo, itemObject.getModel());
      // presentation
      assertNull(itemObject.getImage());
      assertThat(itemObject.getBounds().width).isGreaterThan(50);
      assertThat(itemObject.getBounds().height).isGreaterThan(18);
      // access
      assertSame(MenuObjectInfoUtils.getMenuInfo(subMenuInfo), itemObject.getMenu());
      assertSame(IMenuPolicy.NOOP, itemObject.getPolicy());
    }
    // check IMenuInfo for "subMenuInfo"
    {
      IMenuInfo menuObject = MenuObjectInfoUtils.getMenuInfo(subMenuInfo);
      assertSame(menuObject, menuObject.getModel());
      // presentation
      assertNotNull(menuObject.getImage());
      assertThat(menuObject.getBounds().width).isGreaterThan(50);
      assertThat(menuObject.getBounds().height).isGreaterThan(18);
      // access
      assertFalse(menuObject.isHorizontal());
      // items
      List<IMenuItemInfo> items = menuObject.getItems();
      assertThat(items).hasSize(1);
      assertSame(itemInfo_2, items.get(0).getModel());
    }
  }

  /**
   * Test for {@link MenuManagerInfo} and "create" {@link ActionInfo}.
   */
  public void test_IMenuInfo_CREATE_action() throws Exception {
    ApplicationWindowInfo window =
        parseJavaInfo(
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
            "    action = new Action('Action') {",
            "    };",
            "  }",
            "  protected MenuManager createMenuManager() {",
            "    MenuManager menuManager = super.createMenuManager();",
            "    return menuManager;",
            "  }",
            "}");
    window.refresh();
    // prepare components
    MenuManagerInfo menuInfo = window.getChildren(MenuManagerInfo.class).get(0);
    ActionInfo actionInfo = ActionContainerInfo.getActions(window).get(0);
    // use IMenuInfo for "menuInfo"
    IMenuInfo menuObject = MenuObjectInfoUtils.getMenuInfo(menuInfo);
    IMenuPolicy menuPolicy = menuObject.getPolicy();
    assertFalse(menuPolicy.validateCreate(this));
    assertTrue(menuPolicy.validateCreate(actionInfo));
    menuPolicy.commandCreate(actionInfo, null);
    assertSame(
        actionInfo,
        ((ActionContributionItemInfo) MenuObjectInfoUtils.m_selectingObject.getToolkitModel()).getAction());
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
        "    action = new Action('Action') {",
        "    };",
        "  }",
        "  protected MenuManager createMenuManager() {",
        "    MenuManager menuManager = super.createMenuManager();",
        "    menuManager.add(action);",
        "    return menuManager;",
        "  }",
        "}");
  }

  /**
   * Test for {@link MenuManagerInfo} and "create" {@link ActionInfo}.
   */
  public void test_IMenuInfo_CREATE_actionIntoSubMenu() throws Exception {
    ApplicationWindowInfo window =
        parseJavaInfo(
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
            "    action = new Action('Action') {",
            "    };",
            "  }",
            "  protected MenuManager createMenuManager() {",
            "    MenuManager menuManager = super.createMenuManager();",
            "    {",
            "      MenuManager subMenuManager = new MenuManager('Sub-menu');",
            "      menuManager.add(subMenuManager);",
            "    }",
            "    return menuManager;",
            "  }",
            "}");
    window.refresh();
    // prepare components
    MenuManagerInfo menuInfo = window.getChildren(MenuManagerInfo.class).get(0);
    MenuManagerInfo subMenuInfo = (MenuManagerInfo) menuInfo.getItems().get(0);
    ActionInfo actionInfo = ActionContainerInfo.getActions(window).get(0);
    // use IMenuInfo for "menuInfo"
    IMenuInfo menuObject = MenuObjectInfoUtils.getMenuInfo(subMenuInfo);
    IMenuPolicy menuPolicy = menuObject.getPolicy();
    assertFalse(menuPolicy.validateCreate(this));
    assertTrue(menuPolicy.validateCreate(actionInfo));
    menuPolicy.commandCreate(actionInfo, null);
    assertSame(
        actionInfo,
        ((ActionContributionItemInfo) MenuObjectInfoUtils.m_selectingObject.getToolkitModel()).getAction());
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
        "    action = new Action('Action') {",
        "    };",
        "  }",
        "  protected MenuManager createMenuManager() {",
        "    MenuManager menuManager = super.createMenuManager();",
        "    {",
        "      MenuManager subMenuManager = new MenuManager('Sub-menu');",
        "      menuManager.add(subMenuManager);",
        "      subMenuManager.add(action);",
        "    }",
        "    return menuManager;",
        "  }",
        "}");
  }

  /**
   * Test for {@link MenuManagerInfo} and "create" {@link MenuManagerInfo}.
   */
  public void test_IMenuInfo_CREATE_menuManager() throws Exception {
    ApplicationWindowInfo window =
        parseJavaInfo(
            "import org.eclipse.jface.action.*;",
            "import org.eclipse.jface.window.*;",
            "public class Test extends ApplicationWindow {",
            "  public Test(Shell parentShell) {",
            "    super(parentShell);",
            "    addMenuBar();",
            "  }",
            "  protected MenuManager createMenuManager() {",
            "    MenuManager menuManager = super.createMenuManager();",
            "    return menuManager;",
            "  }",
            "}");
    window.refresh();
    MenuManagerInfo menuInfo = window.getChildren(MenuManagerInfo.class).get(0);
    // use IMenuInfo for "menuInfo"
    IMenuInfo menuObject = MenuObjectInfoUtils.getMenuInfo(menuInfo);
    IMenuPolicy menuPolicy = menuObject.getPolicy();
    // add new MenuManager_Info
    MenuManagerInfo newManager = createJavaInfo("org.eclipse.jface.action.MenuManager");
    assertTrue(menuPolicy.validateCreate(newManager));
    menuPolicy.commandCreate(newManager, null);
    assertSame(newManager, MenuObjectInfoUtils.m_selectingObject.getToolkitModel());
    assertEditor(
        "import org.eclipse.jface.action.*;",
        "import org.eclipse.jface.window.*;",
        "public class Test extends ApplicationWindow {",
        "  public Test(Shell parentShell) {",
        "    super(parentShell);",
        "    addMenuBar();",
        "  }",
        "  protected MenuManager createMenuManager() {",
        "    MenuManager menuManager = super.createMenuManager();",
        "    {",
        "      MenuManager menuManager_1 = new MenuManager('New MenuManager');",
        "      menuManager.add(menuManager_1);",
        "    }",
        "    return menuManager;",
        "  }",
        "}");
  }

  /**
   * Test for {@link MenuManagerInfo} and "create" {@link Separator}.
   */
  public void test_IMenuInfo_CREATE_separator() throws Exception {
    ApplicationWindowInfo window =
        parseJavaInfo(
            "import org.eclipse.jface.action.*;",
            "import org.eclipse.jface.window.*;",
            "public class Test extends ApplicationWindow {",
            "  public Test(Shell parentShell) {",
            "    super(parentShell);",
            "    addMenuBar();",
            "  }",
            "  protected MenuManager createMenuManager() {",
            "    MenuManager menuManager = super.createMenuManager();",
            "    return menuManager;",
            "  }",
            "}");
    window.refresh();
    MenuManagerInfo menuInfo = window.getChildren(MenuManagerInfo.class).get(0);
    // use IMenuInfo for "menuInfo"
    IMenuInfo menuObject = MenuObjectInfoUtils.getMenuInfo(menuInfo);
    IMenuPolicy menuPolicy = menuObject.getPolicy();
    // add new Separator
    ContributionItemInfo separator =
        (ContributionItemInfo) JavaInfoUtils.createJavaInfo(
            m_lastEditor,
            m_lastLoader.loadClass("org.eclipse.jface.action.Separator"),
            new ConstructorCreationSupport());
    assertTrue(menuPolicy.validateCreate(separator));
    menuPolicy.commandCreate(separator, null);
    assertSame(separator, MenuObjectInfoUtils.m_selectingObject.getToolkitModel());
    assertEditor(
        "import org.eclipse.jface.action.*;",
        "import org.eclipse.jface.window.*;",
        "public class Test extends ApplicationWindow {",
        "  public Test(Shell parentShell) {",
        "    super(parentShell);",
        "    addMenuBar();",
        "  }",
        "  protected MenuManager createMenuManager() {",
        "    MenuManager menuManager = super.createMenuManager();",
        "    menuManager.add(new Separator());",
        "    return menuManager;",
        "  }",
        "}");
  }

  /**
   * Test for {@link MenuManagerInfo} and "move".
   */
  public void test_IMenuInfo_MOVE_1() throws Exception {
    ApplicationWindowInfo window =
        parseJavaInfo(
            "import org.eclipse.jface.action.*;",
            "import org.eclipse.jface.window.*;",
            "public class Test extends ApplicationWindow {",
            "  public Test(Shell parentShell) {",
            "    super(parentShell);",
            "    addMenuBar();",
            "  }",
            "  protected MenuManager createMenuManager() {",
            "    MenuManager menuManager = super.createMenuManager();",
            "    menuManager.add(new Separator('Item 1'));",
            "    menuManager.add(new Separator('Item 2'));",
            "    return menuManager;",
            "  }",
            "  protected Control createContents(Composite parent) {",
            "    Composite container = (Composite) super.createContents(parent);",
            "    return container;",
            "  }",
            "}");
    window.refresh();
    // prepare components
    MenuManagerInfo menuInfo;
    ContributionItemInfo itemInfo_1;
    ContributionItemInfo itemInfo_2;
    {
      menuInfo = window.getChildren(MenuManagerInfo.class).get(0);
      List<AbstractComponentInfo> menuItems = menuInfo.getItems();
      assertThat(menuItems).hasSize(2);
      // children of "menuInfo"
      {
        itemInfo_1 = (ContributionItemInfo) menuItems.get(0);
        itemInfo_2 = (ContributionItemInfo) menuItems.get(1);
      }
    }
    // use IMenuInfo for "menuInfo"
    IMenuInfo menuObject = MenuObjectInfoUtils.getMenuInfo(menuInfo);
    IMenuPolicy menuPolicy = menuObject.getPolicy();
    // can not accept CompositeInfo
    {
      CompositeInfo containerInfo = window.getChildren(CompositeInfo.class).get(0);
      assertFalse(menuPolicy.validateMove(containerInfo));
    }
    // move "itemInfo_2"
    assertTrue(menuPolicy.validateMove(itemInfo_2));
    menuPolicy.commandMove(itemInfo_2, itemInfo_1);
    assertSame(itemInfo_2, MenuObjectInfoUtils.m_selectingObject.getToolkitModel());
    assertEditor(
        "import org.eclipse.jface.action.*;",
        "import org.eclipse.jface.window.*;",
        "public class Test extends ApplicationWindow {",
        "  public Test(Shell parentShell) {",
        "    super(parentShell);",
        "    addMenuBar();",
        "  }",
        "  protected MenuManager createMenuManager() {",
        "    MenuManager menuManager = super.createMenuManager();",
        "    menuManager.add(new Separator('Item 2'));",
        "    menuManager.add(new Separator('Item 1'));",
        "    return menuManager;",
        "  }",
        "  protected Control createContents(Composite parent) {",
        "    Composite container = (Composite) super.createContents(parent);",
        "    return container;",
        "  }",
        "}");
  }

  /**
   * Test for {@link MenuManagerInfo} and "move".<br>
   * Move {@link ActionContributionItemInfo} from one {@link MenuManagerInfo} into other.
   */
  public void test_IMenuInfo_MOVE_2() throws Exception {
    ApplicationWindowInfo window =
        parseJavaInfo(
            "import org.eclipse.jface.action.*;",
            "import org.eclipse.jface.window.*;",
            "public class Test extends ApplicationWindow {",
            "  private IAction action;",
            "  private IAction action_2;",
            "  public Test(Shell parentShell) {",
            "    super(parentShell);",
            "    createActions();",
            "    addMenuBar();",
            "  }",
            "  private void createActions() {",
            "    action = new Action() {",
            "    };",
            "  }",
            "  protected MenuManager createMenuManager() {",
            "    MenuManager menuManager = super.createMenuManager();",
            "    menuManager.add(action);",
            "    {",
            "      MenuManager subMenuManager = new MenuManager('Sub-menu');",
            "      menuManager.add(subMenuManager);",
            "    }",
            "    return menuManager;",
            "  }",
            "}");
    window.refresh();
    // prepare components
    MenuManagerInfo menuInfo;
    MenuManagerInfo subMenuInfo;
    ActionContributionItemInfo itemInfo;
    {
      menuInfo = window.getChildren(MenuManagerInfo.class).get(0);
      List<AbstractComponentInfo> menuItems = menuInfo.getItems();
      assertThat(menuItems).hasSize(2);
      // children of "menuInfo"
      {
        itemInfo = (ActionContributionItemInfo) menuItems.get(0);
        subMenuInfo = (MenuManagerInfo) menuItems.get(1);
      }
    }
    // check permissions
    {
      assertTrue(itemInfo.getCreationSupport().canReorder());
      assertTrue(itemInfo.getCreationSupport().canReparent());
    }
    // do move
    subMenuInfo.command_MOVE(itemInfo, null);
    assertEditor(
        "import org.eclipse.jface.action.*;",
        "import org.eclipse.jface.window.*;",
        "public class Test extends ApplicationWindow {",
        "  private IAction action;",
        "  private IAction action_2;",
        "  public Test(Shell parentShell) {",
        "    super(parentShell);",
        "    createActions();",
        "    addMenuBar();",
        "  }",
        "  private void createActions() {",
        "    action = new Action() {",
        "    };",
        "  }",
        "  protected MenuManager createMenuManager() {",
        "    MenuManager menuManager = super.createMenuManager();",
        "    {",
        "      MenuManager subMenuManager = new MenuManager('Sub-menu');",
        "      menuManager.add(subMenuManager);",
        "      subMenuManager.add(action);",
        "    }",
        "    return menuManager;",
        "  }",
        "}");
    // check new association
    {
      InvocationVoidAssociation association = (InvocationVoidAssociation) itemInfo.getAssociation();
      assertEquals("subMenuManager.add(action)", association.getSource());
      assertSame(m_lastEditor.getAstUnit(), association.getInvocation().getRoot());
    }
    // do refresh()
    window.refresh();
    assertNoErrors(window);
  }

  /**
   * Test for {@link MenuManagerInfo} and "move".<br>
   * Move {@link MenuManagerInfo} before other.
   */
  public void test_IMenuInfo_MOVE_3() throws Exception {
    ApplicationWindowInfo window =
        parseJavaInfo(
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
            "    action = new Action() {",
            "    };",
            "  }",
            "  protected MenuManager createMenuManager() {",
            "    MenuManager menuManager = super.createMenuManager();",
            "    {",
            "      MenuManager menuManager_1 = new MenuManager('111');",
            "      menuManager.add(menuManager_1);",
            "      menuManager_1.add(action);",
            "    }",
            "    {",
            "      MenuManager menuManager_2 = new MenuManager('222');",
            "      menuManager.add(menuManager_2);",
            "    }",
            "    return menuManager;",
            "  }",
            "}");
    window.refresh();
    // prepare components
    MenuManagerInfo menuManager = window.getChildren(MenuManagerInfo.class).get(0);
    MenuManagerInfo menuManager_1 = (MenuManagerInfo) menuManager.getItems().get(0);
    menuManager.command_MOVE(menuManager_1, null);
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
        "    action = new Action() {",
        "    };",
        "  }",
        "  protected MenuManager createMenuManager() {",
        "    MenuManager menuManager = super.createMenuManager();",
        "    {",
        "      MenuManager menuManager_2 = new MenuManager('222');",
        "      menuManager.add(menuManager_2);",
        "    }",
        "    {",
        "      MenuManager menuManager_1 = new MenuManager('111');",
        "      menuManager.add(menuManager_1);",
        "      menuManager_1.add(action);",
        "    }",
        "    return menuManager;",
        "  }",
        "}");
  }

  /**
   * Test for {@link MenuManagerInfo} and "paste".
   */
  public void test_IMenuInfo_PASTE() throws Exception {
    ApplicationWindowInfo window =
        parseJavaInfo(
            "import org.eclipse.jface.action.*;",
            "import org.eclipse.jface.window.*;",
            "public class Test extends ApplicationWindow {",
            "  public Test(Shell parentShell) {",
            "    super(parentShell);",
            "    addMenuBar();",
            "  }",
            "  protected MenuManager createMenuManager() {",
            "    MenuManager menuManager = super.createMenuManager();",
            "    return menuManager;",
            "  }",
            "  protected Control createContents(Composite parent) {",
            "    Composite container = (Composite) super.createContents(parent);",
            "    return container;",
            "  }",
            "}");
    window.refresh();
    // prepare components
    MenuManagerInfo menuInfo = window.getChildren(MenuManagerInfo.class).get(0);
    IMenuInfo menuObject = MenuObjectInfoUtils.getMenuInfo(menuInfo);
    IMenuPolicy menuPolicy = menuObject.getPolicy();
    // "paste" not implemented
    assertFalse(menuPolicy.validatePaste(null));
    {
      List<?> objects = menuPolicy.commandPaste(null, null);
      assertThat(objects).isEmpty();
    }
  }
}