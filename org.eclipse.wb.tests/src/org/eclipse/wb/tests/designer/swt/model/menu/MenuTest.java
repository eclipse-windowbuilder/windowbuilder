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
package org.eclipse.wb.tests.designer.swt.model.menu;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.association.Association;
import org.eclipse.wb.core.model.association.CompoundAssociation;
import org.eclipse.wb.core.model.association.ConstructorParentAssociation;
import org.eclipse.wb.core.model.association.InvocationChildAssociation;
import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.menu.IMenuInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuItemInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuPolicy;
import org.eclipse.wb.internal.core.model.menu.IMenuPopupInfo;
import org.eclipse.wb.internal.core.model.menu.MenuObjectInfoUtils;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.internal.swt.model.widgets.menu.MenuInfo;
import org.eclipse.wb.internal.swt.model.widgets.menu.MenuItemInfo;
import org.eclipse.wb.internal.swt.model.widgets.menu.MenuStylePresentation;
import org.eclipse.wb.internal.swt.support.MenuSupport;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Decorations;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

import java.util.Collections;
import java.util.List;

/**
 * Tests for {@link MenuInfo}.
 *
 * @author mitin_aa
 */
public class MenuTest extends RcpModelTest {
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
  // MenuStylePresentation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test that {@link MenuStylePresentation} returns different icons for menus with different
   * styles.
   */
  public void test_MenuStylePresentation() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    {",
            "      Menu menuBar = new Menu(this, SWT.BAR);",
            "      setMenuBar(menuBar);",
            "    }",
            "    {",
            "      Menu popup = new Menu(this);",
            "      setMenu(popup);",
            "      {",
            "        MenuItem item = new MenuItem(popup, SWT.CASCADE);",
            "        {",
            "          Menu subMenu = new Menu(item);",
            "          item.setMenu(subMenu);",
            "        }",
            "      }",
            "    }",
            "  }",
            "}");
    shell.refresh();
    // prepare menu object models
    List<MenuInfo> menuChildren = shell.getChildren(MenuInfo.class);
    MenuInfo menuBar = menuChildren.get(0);
    MenuInfo menuPopup = menuChildren.get(1);
    MenuItemInfo item = menuPopup.getChildren(MenuItemInfo.class).get(0);
    MenuInfo menuCascade = item.getChildren(MenuInfo.class).get(0);
    // test icons
    assertNotSame(menuBar.getPresentation().getIcon(), menuPopup.getPresentation().getIcon());
    assertNotSame(menuPopup.getPresentation().getIcon(), menuCascade.getPresentation().getIcon());
    assertNotSame(menuCascade.getPresentation().getIcon(), menuBar.getPresentation().getIcon());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Bounds
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test that "bar" {@link MenuInfo} has reasonable bounds.
   */
  public void test_boundsBar() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    Menu mainMenu = new Menu(this, SWT.BAR);",
            "    setMenuBar(mainMenu);",
            "  }",
            "}");
    shell.refresh();
    MenuInfo bar = shell.getChildren(MenuInfo.class).get(0);
    // "bar" should not use client are offset of Shell, because "bar" itself is included in this offset
    assertEquals(bar.getBounds(), bar.getModelBounds());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Style
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link MenuInfo#isBar()}.
   */
  public void test_isBar() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    Menu mainMenu = new Menu(this, SWT.BAR);",
            "    setMenuBar(mainMenu);",
            "  }",
            "}");
    shell.refresh();
    MenuInfo menu = shell.getChildren(MenuInfo.class).get(0);
    assertTrue(menu.isBar());
    assertFalse(menu.isPopup());
  }

  /**
   * Test for {@link MenuInfo#isPopup()}.
   */
  public void test_isPopup() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    {",
            "      Menu menu = new Menu(this);",
            "      setMenu(menu);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    MenuInfo menu = shell.getChildren(MenuInfo.class).get(0);
    assertTrue(menu.isPopup());
    assertFalse(menu.isBar());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Impl
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for asking unsupported adapter.
   */
  public void test_impl_IMenuInfo_no() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    Menu menu = new Menu(this, SWT.BAR);",
            "    setMenuBar(menu);",
            "  }",
            "}");
    shell.refresh();
    MenuInfo menuInfo = shell.getChildren(MenuInfo.class).get(0);
    assertNull(menuInfo.getAdapter(List.class));
  }

  /**
   * Test for {@link IMenuInfo} of "bar" {@link MenuInfo}.
   */
  public void test_impl_IMenuInfo_bar() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    Menu menu = new Menu(this, SWT.BAR);",
            "    setMenuBar(menu);",
            "    {",
            "      MenuItem item = new MenuItem(menu, SWT.NONE);",
            "      item.setText('Item');",
            "    }",
            "  }",
            "}");
    shell.refresh();
    MenuInfo menuInfo = shell.getChildren(MenuInfo.class).get(0);
    List<MenuItemInfo> itemInfos = menuInfo.getChildrenItems();
    // test IMenuInfo
    IMenuInfo menuObject = menuInfo.getAdapter(IMenuInfo.class);
    assertNotNull(menuObject);
    assertSame(menuInfo, menuObject.getModel());
    assertSame(menuInfo.getImage(), menuObject.getImage());
    assertSame(menuInfo.getBounds(), menuObject.getBounds());
    assertTrue(menuObject.isHorizontal());
    {
      List<IMenuItemInfo> itemObjects = menuObject.getItems();
      assertEquals(1, itemObjects.size());
      assertSame(itemInfos.get(0), itemObjects.get(0).getModel());
    }
  }

  /**
   * Test for {@link IMenuInfo} of "popup" {@link MenuInfo}.
   */
  public void test_impl_IMenuInfo_popup() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    Menu menu = new Menu(this);",
            "    setMenu(menu);",
            "  }",
            "}");
    shell.refresh();
    MenuInfo menuInfo = shell.getChildren(MenuInfo.class).get(0);
    // test IMenuInfo
    IMenuInfo menuObject = menuInfo.getAdapter(IMenuInfo.class);
    assertNotNull(menuObject);
    assertSame(menuObject, menuObject.getModel());
    assertSame(menuInfo.getImage(), menuObject.getImage());
    assertSame(menuInfo.getBounds(), menuObject.getBounds());
    assertFalse(menuObject.isHorizontal());
  }

  /**
   * Test for {@link IMenuPopupInfo} of "popup" {@link MenuInfo}.
   */
  public void test_impl_IMenuPopupInfo_popup() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    Menu menu = new Menu(this);",
            "    setMenu(menu);",
            "    {",
            "      MenuItem item = new MenuItem(menu, SWT.NONE);",
            "      item.setText('Item');",
            "    }",
            "  }",
            "}");
    shell.refresh();
    MenuInfo menuInfo = shell.getChildren(MenuInfo.class).get(0);
    // test IMenuInfo
    IMenuInfo menuObject = menuInfo.getAdapter(IMenuInfo.class);
    IMenuPopupInfo popupObject = menuInfo.getAdapter(IMenuPopupInfo.class);
    assertNotNull(popupObject);
    assertSame(menuInfo, popupObject.getModel());
    assertSame(menuInfo.getPresentation().getIcon(), popupObject.getImage());
    assertEquals(16, popupObject.getBounds().width);
    assertEquals(16, popupObject.getBounds().height);
    assertSame(menuObject, popupObject.getMenu());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CREATE
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Tests for {@link IMenuInfo#validateCreate(Object)} and
   * {@link IMenuInfo#commandCreate(Object, IMenuItemInfo)}.
   */
  public void test_IMenuInfo_create_1() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    Menu menu = new Menu(this);",
            "    setMenu(menu);",
            "  }",
            "}");
    shell.refresh();
    MenuInfo menuInfo = shell.getChildren(MenuInfo.class).get(0);
    IMenuInfo menuObject = MenuObjectInfoUtils.getMenuInfo(menuInfo);
    IMenuPolicy menuPolicy = menuObject.getPolicy();
    // don't allow adding non-menu components
    {
      JavaInfo button = createJavaInfo("org.eclipse.swt.widgets.Button");
      assertFalse(menuPolicy.validateCreate(button));
    }
    // add new MenuItem
    JavaInfo itemInfo = createJavaInfo("org.eclipse.swt.widgets.MenuItem");
    assertTrue(menuPolicy.validateCreate(itemInfo));
    menuPolicy.commandCreate(itemInfo, null);
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    Menu menu = new Menu(this);",
        "    setMenu(menu);",
        "    {",
        "      MenuItem menuItem = new MenuItem(menu, SWT.NONE);",
        "      menuItem.setText('New Item');",
        "    }",
        "  }",
        "}");
  }

  /**
   * Tests for {@link IMenuInfo#commandCreate(Object, IMenuItemInfo)} with reference
   * {@link IMenuItemInfo}.
   */
  public void test_IMenuInfo_create_2() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    Menu menu = new Menu(this);",
            "    setMenu(menu);",
            "    {",
            "      MenuItem item = new MenuItem(menu, SWT.NONE);",
            "      item.setText('Item');",
            "    }",
            "  }",
            "}");
    shell.refresh();
    MenuInfo menuInfo = shell.getChildren(MenuInfo.class).get(0);
    MenuItemInfo itemInfo = menuInfo.getChildrenItems().get(0);
    // prepare menu objects
    IMenuInfo menuObject = MenuObjectInfoUtils.getMenuInfo(menuInfo);
    IMenuPolicy menuPolicy = menuObject.getPolicy();
    // add new MenuItem
    JavaInfo newItemInfo = createJavaInfo("org.eclipse.swt.widgets.MenuItem");
    assertTrue(menuPolicy.validateCreate(itemInfo));
    menuPolicy.commandCreate(newItemInfo, itemInfo);
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    Menu menu = new Menu(this);",
        "    setMenu(menu);",
        "    {",
        "      MenuItem menuItem = new MenuItem(menu, SWT.NONE);",
        "      menuItem.setText('New Item');",
        "    }",
        "    {",
        "      MenuItem item = new MenuItem(menu, SWT.NONE);",
        "      item.setText('Item');",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for adding {@link MenuItemInfo} with <code>CASCADE</code>.
   */
  public void test_IMenuInfo_create_3() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    Menu menu = new Menu(this);",
            "    setMenu(menu);",
            "  }",
            "}");
    shell.refresh();
    MenuInfo menuInfo = shell.getChildren(MenuInfo.class).get(0);
    IMenuInfo menuObject = MenuObjectInfoUtils.getMenuInfo(menuInfo);
    IMenuPolicy menuPolicy = menuObject.getPolicy();
    // add new MenuItem
    JavaInfo itemInfo = createJavaInfo("org.eclipse.swt.widgets.MenuItem", "cascade");
    assertTrue(menuPolicy.validateCreate(itemInfo));
    menuPolicy.commandCreate(itemInfo, null);
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    Menu menu = new Menu(this);",
        "    setMenu(menu);",
        "    {",
        "      MenuItem menuItem = new MenuItem(menu, SWT.CASCADE);",
        "      menuItem.setText('New SubMenu');",
        "      {",
        "        Menu menu_1 = new Menu(menuItem);",
        "        menuItem.setMenu(menu_1);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // PASTE
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Tests for {@link IMenuInfo#validatePaste(Object)} and
   * {@link IMenuInfo#commandPaste(Object, IMenuItemInfo)}.
   */
  public void DISABLE_test_IMenuInfo_paste_1() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    Menu menu = new Menu(this);",
            "    setMenu(menu);",
            "    {",
            "      MenuItem item = new MenuItem(menu, SWT.NONE);",
            "      item.setText('Item');",
            "    }",
            "  }",
            "}");
    shell.refresh();
    MenuInfo menuInfo = shell.getChildren(MenuInfo.class).get(0);
    MenuItemInfo itemInfo = menuInfo.getChildrenItems().get(0);
    IMenuInfo menuObject = MenuObjectInfoUtils.getMenuInfo(menuInfo);
    IMenuPolicy menuPolicy = menuObject.getPolicy();
    // do copy/paste
    JavaInfoMemento memento = JavaInfoMemento.createMemento(itemInfo);
    List<JavaInfoMemento> mementoList = Collections.singletonList(memento);
    assertTrue(menuPolicy.validatePaste(mementoList));
    menuPolicy.commandPaste(mementoList, null);
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    Menu menu = new Menu(this);",
        "    setMenu(menu);",
        "    {",
        "      MenuItem item = new MenuItem(menu, SWT.NONE);",
        "      item.setText('Item');",
        "    }",
        "    {",
        "      MenuItem menuItem = new MenuItem(menu, SWT.NONE);",
        "      menuItem.setText('Item');",
        "    }",
        "  }",
        "}");
  }

  /**
   * Don't allow "paste" for non-menu components.
   */
  public void test_IMenuInfo_paste_2() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    Menu menu = new Menu(this);",
            "    setMenu(menu);",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setText('Button');",
            "    }",
            "  }",
            "}");
    shell.refresh();
    MenuInfo menuInfo = shell.getChildren(MenuInfo.class).get(0);
    ControlInfo buttonInfo = shell.getChildrenControls().get(0);
    IMenuInfo menuObject = MenuObjectInfoUtils.getMenuInfo(menuInfo);
    IMenuPolicy menuPolicy = menuObject.getPolicy();
    // do copy/paste
    JavaInfoMemento memento = JavaInfoMemento.createMemento(buttonInfo);
    assertFalse(menuPolicy.validatePaste(Collections.singletonList(memento)));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // MOVE
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Tests for {@link IMenuInfo#validateMove(Object)} and
   * {@link IMenuInfo#commandMove(Object, IMenuItemInfo)}.
   */
  public void test_IMenuInfo_move_1() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    Menu menu = new Menu(this, SWT.BAR);",
            "    setMenuBar(menu);",
            "    {",
            "      MenuItem item_1 = new MenuItem(menu, SWT.NONE);",
            "      item_1.setText('Item 1');",
            "    }",
            "    {",
            "      MenuItem item_2 = new MenuItem(menu, SWT.NONE);",
            "      item_2.setText('Item 2');",
            "    }",
            "  }",
            "}");
    shell.refresh();
    MenuInfo menuInfo = shell.getChildren(MenuInfo.class).get(0);
    MenuItemInfo itemInfo_1 = menuInfo.getChildrenItems().get(0);
    MenuItemInfo itemInfo_2 = menuInfo.getChildrenItems().get(1);
    // prepare menu objects
    IMenuInfo menuObject = MenuObjectInfoUtils.getMenuInfo(menuInfo);
    IMenuPolicy menuPolicy = menuObject.getPolicy();
    // move "item_2" before "item_1"
    assertTrue(menuPolicy.validateMove(itemInfo_2));
    menuPolicy.commandMove(itemInfo_2, itemInfo_1);
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    Menu menu = new Menu(this, SWT.BAR);",
        "    setMenuBar(menu);",
        "    {",
        "      MenuItem item_2 = new MenuItem(menu, SWT.NONE);",
        "      item_2.setText('Item 2');",
        "    }",
        "    {",
        "      MenuItem item_1 = new MenuItem(menu, SWT.NONE);",
        "      item_1.setText('Item 1');",
        "    }",
        "  }",
        "}");
  }

  /**
   * Don't allow moving non-menu component on {@link IMenuInfo}.
   */
  public void test_IMenuInfo_move_2() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    Menu menu = new Menu(this, SWT.BAR);",
            "    setMenuBar(menu);",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setText('Button');",
            "    }",
            "  }",
            "}");
    shell.refresh();
    MenuInfo menuInfo = shell.getChildren(MenuInfo.class).get(0);
    ControlInfo buttonInfo = shell.getChildrenControls().get(0);
    IMenuInfo menuObject = MenuObjectInfoUtils.getMenuInfo(menuInfo);
    IMenuPolicy menuPolicy = menuObject.getPolicy();
    // can not move
    assertFalse(menuPolicy.validateMove(buttonInfo));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // MenuInfo.CREATE
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Add "bar" menu.
   */
  public void test_commandCreate_bar() throws Exception {
    // create shell
    final CompositeInfo shell =
        parseComposite(
            "// filler filler filler",
            "public class Test extends Shell {",
            "  public Test() {",
            "  }",
            "}");
    // create new menu
    final MenuInfo newMenuBar =
        (MenuInfo) JavaInfoUtils.createJavaInfo(
            m_lastEditor,
            MenuSupport.getMenuClass(),
            new ConstructorCreationSupport("bar", true));
    // add to shell
    ExecutionUtils.run(shell, new RunnableEx() {
      @Override
      public void run() throws Exception {
        newMenuBar.command_CREATE(shell);
      }
    });
    // test the result
    assertEditor(
        "// filler filler filler",
        "public class Test extends Shell {",
        "  public Test() {",
        "    {",
        "      Menu menu = new Menu(this, SWT.BAR);",
        "      setMenuBar(menu);",
        "    }",
        "  }",
        "}");
    // check association
    {
      CompoundAssociation compoundAssociation = (CompoundAssociation) newMenuBar.getAssociation();
      List<Association> associations = compoundAssociation.getAssociations();
      assertEquals(2, associations.size());
      {
        ConstructorParentAssociation association =
            (ConstructorParentAssociation) associations.get(0);
        assertSame(newMenuBar, association.getJavaInfo());
        assertEquals("new Menu(this, SWT.BAR)", association.getSource());
      }
      {
        InvocationChildAssociation association = (InvocationChildAssociation) associations.get(1);
        assertSame(newMenuBar, association.getJavaInfo());
        assertEquals("setMenuBar(menu)", association.getSource());
      }
    }
  }

  /**
   * Add "popup" menu.
   */
  public void test_commandCreate_popup() throws Exception {
    // create shell
    final CompositeInfo shell =
        parseComposite(
            "// filler filler filler",
            "public class Test extends Shell {",
            "  public Test() {",
            "  }",
            "}");
    // create new menu
    final MenuInfo newMenuPopup =
        (MenuInfo) JavaInfoUtils.createJavaInfo(
            m_lastEditor,
            MenuSupport.getMenuClass(),
            new ConstructorCreationSupport());
    // add to shell
    ExecutionUtils.run(shell, new RunnableEx() {
      @Override
      public void run() throws Exception {
        newMenuPopup.command_CREATE(shell);
      }
    });
    // test the result
    assertEditor(
        "// filler filler filler",
        "public class Test extends Shell {",
        "  public Test() {",
        "    {",
        "      Menu menu = new Menu(this);",
        "      setMenu(menu);",
        "    }",
        "  }",
        "}");
    // check association
    {
      CompoundAssociation compoundAssociation = (CompoundAssociation) newMenuPopup.getAssociation();
      List<Association> associations = compoundAssociation.getAssociations();
      assertEquals(2, associations.size());
      {
        ConstructorParentAssociation association =
            (ConstructorParentAssociation) associations.get(0);
        assertSame(newMenuPopup, association.getJavaInfo());
        assertEquals("new Menu(this)", association.getSource());
      }
      {
        InvocationChildAssociation association = (InvocationChildAssociation) associations.get(1);
        assertSame(newMenuPopup, association.getJavaInfo());
        assertEquals("setMenu(menu)", association.getSource());
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // MenuInfo.PASTE
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for copy/paste {@link MenuInfo} with {@link MenuItemInfo} and sub-menu.
   */
  public void DISABLE_test_commandPaste() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    {",
            "      Button button_1 = new Button(this, SWT.NONE);",
            "      button_1.setText('Button 1');",
            "      {",
            "        Menu menu = new Menu(button_1);",
            "        button_1.setMenu(menu);",
            "        {",
            "          MenuItem item = new MenuItem(menu, SWT.CASCADE);",
            "          item.setText('My item');",
            "          {",
            "            Menu subMenu = new Menu(item);",
            "            item.setMenu(subMenu);",
            "          }",
            "        }",
            "      }",
            "    }",
            "    {",
            "      Button button_2 = new Button(this, SWT.NONE);",
            "      button_2.setText('Button 2');",
            "    }",
            "  }",
            "}");
    shell.refresh();
    // prepare models
    ControlInfo button_1 = shell.getChildrenControls().get(0);
    ControlInfo button_2 = shell.getChildrenControls().get(1);
    MenuInfo menu = button_1.getChildren(MenuInfo.class).get(0);
    // do paste on "button_2"
    {
      JavaInfoMemento memento = JavaInfoMemento.createMemento(menu);
      MenuInfo menuCopy = (MenuInfo) memento.create(shell);
      menuCopy.command_CREATE(button_2);
      memento.apply();
    }
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new RowLayout());",
        "    {",
        "      Button button_1 = new Button(this, SWT.NONE);",
        "      button_1.setText('Button 1');",
        "      {",
        "        Menu menu = new Menu(button_1);",
        "        button_1.setMenu(menu);",
        "        {",
        "          MenuItem item = new MenuItem(menu, SWT.CASCADE);",
        "          item.setText('My item');",
        "          {",
        "            Menu subMenu = new Menu(item);",
        "            item.setMenu(subMenu);",
        "          }",
        "        }",
        "      }",
        "    }",
        "    {",
        "      Button button_2 = new Button(this, SWT.NONE);",
        "      button_2.setText('Button 2');",
        "      {",
        "        Menu menu = new Menu(button_2);",
        "        button_2.setMenu(menu);",
        "        {",
        "          MenuItem menuItem = new MenuItem(menu, SWT.CASCADE);",
        "          menuItem.setText('My item');",
        "          {",
        "            Menu menu_1 = new Menu(menuItem);",
        "            menuItem.setMenu(menu_1);",
        "          }",
        "        }",
        "      }",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // MenuInfo.MOVE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_commandMove_fromItem_toItem() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    Menu menuBar = new Menu(this, SWT.BAR);",
            "    setMenuBar(menuBar);",
            "    {",
            "      MenuItem item_1 = new MenuItem(menuBar, SWT.CASCADE);",
            "      item_1.setText('Item 1');",
            "      {",
            "        Menu subMenu = new Menu(item_1);",
            "        item_1.setMenu(subMenu);",
            "      }",
            "    }",
            "    {",
            "      MenuItem item_2 = new MenuItem(menuBar, SWT.NONE);",
            "      item_2.setText('Item 2');",
            "    }",
            "  }",
            "}");
    shell.refresh();
    // prepare models
    MenuInfo menuBar = (MenuInfo) shell.getChildrenJava().get(1);
    MenuItemInfo item_1 = menuBar.getChildrenItems().get(0);
    MenuItemInfo item_2 = menuBar.getChildrenItems().get(1);
    MenuInfo subMenu = item_1.getChildren(MenuInfo.class).get(0);
    // do move
    subMenu.command_ADD(item_2);
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    Menu menuBar = new Menu(this, SWT.BAR);",
        "    setMenuBar(menuBar);",
        "    {",
        "      MenuItem item_1 = new MenuItem(menuBar, SWT.NONE);",
        "      item_1.setText('Item 1');",
        "    }",
        "    {",
        "      MenuItem item_2 = new MenuItem(menuBar, SWT.CASCADE);",
        "      item_2.setText('Item 2');",
        "      {",
        "        Menu subMenu = new Menu(item_2);",
        "        item_2.setMenu(subMenu);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  public void test_commandMove_fromItem_toControl() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "    }",
            "    {",
            "      Menu menu = new Menu(this);",
            "      setMenu(menu);",
            "      {",
            "        MenuItem item = new MenuItem(menu, SWT.CASCADE);",
            "        {",
            "          Menu subMenu = new Menu(item);",
            "          item.setMenu(subMenu);",
            "        }",
            "      }",
            "    }",
            "  }",
            "}");
    shell.refresh();
    // prepare models
    ControlInfo button = shell.getChildrenControls().get(0);
    MenuInfo menu = shell.getChildren(MenuInfo.class).get(0);
    MenuItemInfo item = menu.getChildrenItems().get(0);
    MenuInfo subMenu = item.getChildren(MenuInfo.class).get(0);
    // do move
    subMenu.command_ADD(button);
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      {",
        "        Menu subMenu = new Menu(button);",
        "        button.setMenu(subMenu);",
        "      }",
        "    }",
        "    {",
        "      Menu menu = new Menu(this);",
        "      setMenu(menu);",
        "      {",
        "        MenuItem item = new MenuItem(menu, SWT.NONE);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  public void test_commandMove_fromControl_toItem() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      {",
            "        Menu subMenu = new Menu(button);",
            "        button.setMenu(subMenu);",
            "      }",
            "    }",
            "    {",
            "      Menu menu = new Menu(this);",
            "      setMenu(menu);",
            "      {",
            "        MenuItem item = new MenuItem(menu, SWT.NONE);",
            "      }",
            "    }",
            "  }",
            "}");
    shell.refresh();
    // prepare models
    ControlInfo button = shell.getChildrenControls().get(0);
    MenuInfo subMenu = button.getChildren(MenuInfo.class).get(0);
    MenuInfo menu = shell.getChildren(MenuInfo.class).get(0);
    MenuItemInfo item = menu.getChildrenItems().get(0);
    // do move
    subMenu.command_ADD(item);
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "    }",
        "    {",
        "      Menu menu = new Menu(this);",
        "      setMenu(menu);",
        "      {",
        "        MenuItem item = new MenuItem(menu, SWT.CASCADE);",
        "        {",
        "          Menu subMenu = new Menu(item);",
        "          item.setMenu(subMenu);",
        "        }",
        "      }",
        "    }",
        "  }",
        "}");
  }

  public void test_commandMove_fromControl_toControl() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    {",
            "      Button button_1 = new Button(this, SWT.NONE);",
            "      {",
            "        Menu menu = new Menu(button_1);",
            "        button_1.setMenu(menu);",
            "      }",
            "    }",
            "    {",
            "      Button button_2 = new Button(this, SWT.NONE);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    // prepare models
    ControlInfo button_1 = shell.getChildrenControls().get(0);
    ControlInfo button_2 = shell.getChildrenControls().get(1);
    MenuInfo menu = button_1.getChildren(MenuInfo.class).get(0);
    // do move
    menu.command_ADD(button_2);
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    {",
        "      Button button_1 = new Button(this, SWT.NONE);",
        "    }",
        "    {",
        "      Button button_2 = new Button(this, SWT.NONE);",
        "      {",
        "        Menu menu = new Menu(button_2);",
        "        button_2.setMenu(menu);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Misc
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Parses {@link Shell} for menu "bar".
   */
  private MenuInfo parseMenuBar() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    Menu menu = new Menu(this, SWT.BAR);",
            "    setMenuBar(menu);",
            "  }",
            "}");
    shell.refresh();
    return shell.getChildren(MenuInfo.class).get(0);
  }

  /**
   * There is bug in eSWT - Menu.isVisible() calls Menu.getVisible(). But Menu.getVisible() in turn
   * also calls Menu.isVisible(). So, when we call any of these methods, we have infinite recursion.
   * To fix this we disable default value fetching for "visible" property of Menu.
   */
  public void test_setVisible_default() throws Exception {
    MenuInfo menu = parseMenuBar();
    assertEquals(false, menu.getPropertyByTitle("visible").getValue());
  }

  /**
   * Test for {@link MenuInfo#getAdapter(Class)}.
   */
  public void test_getAdapter() throws Exception {
    MenuInfo menu = parseMenuBar();
    assertNull(menu.getAdapter(Integer.class));
  }

  /**
   * Test for adding a placeholder to menu without items.
   */
  public void test_placeHolder() throws Exception {
    MenuInfo menuInfo = parseMenuBar();
    // no "item" models
    assertEquals(0, menuInfo.getChildrenItems().size());
    // even empty "menu" has "item" object
    Object menuObject = menuInfo.getObject();
    Object[] items = MenuSupport.getItems(menuObject);
    assertEquals(1, items.length);
  }

  /**
   * Test fetching menu bar bounds and items bounds.
   */
  public void test_fetchVisualDataBar() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    Menu menu = new Menu(this, SWT.BAR);",
            "    setMenuBar(menu);",
            "    {",
            "      MenuItem item_1 = new MenuItem(menu, SWT.NONE);",
            "      item_1.setText('Item 2');",
            "    }",
            "    {",
            "      MenuItem item_2 = new MenuItem(menu, SWT.NONE);",
            "      item_2.setText('Item 2');",
            "    }",
            "  }",
            "}");
    shell.refresh();
    MenuInfo menu = shell.getChildren(MenuInfo.class).get(0);
    // menu bar has bounds, but has no image
    assertNotNull(menu.getModelBounds());
    if (EnvironmentUtils.IS_MAC) {
      // menu bar in OSX has synthetic image which drawn above design canvas.
      assertNotNull(menu.getImage());
    } else {
      assertNull(menu.getImage());
    }
    // items have bounds, but not image
    List<MenuItemInfo> items = menu.getChildrenItems();
    assertEquals(2, items.size());
    for (MenuItemInfo item : items) {
      assertNotNull(item.getModelBounds());
      assertNull(item.getImage());
    }
  }

  /**
   * Test fetching cascaded/popup menu bounds/image and items bounds.
   */
  public void test_fetchVisualDataCascaded() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    Menu menu = new Menu(this, SWT.BAR);",
            "    setMenuBar(menu);",
            "    {",
            "      MenuItem item = new MenuItem(menu, SWT.CASCADE);",
            "      item.setText('Item');",
            "      {",
            "        Menu subMenu = new Menu(item);",
            "        item.setMenu(subMenu);",
            "        {",
            "          MenuItem subItem_1 = new MenuItem(subMenu, SWT.NONE);",
            "          subItem_1.setText('SubItem 1');",
            "        }",
            "        {",
            "          MenuItem subItem_2 = new MenuItem(subMenu, SWT.NONE);",
            "          subItem_2.setText('SubItem 1');",
            "        }",
            "      }",
            "    }",
            "  }",
            "}");
    shell.refresh();
    MenuInfo menu = (MenuInfo) shell.getChildrenJava().get(1);
    MenuItemInfo menuItem = menu.getChildren(MenuItemInfo.class).get(0);
    MenuInfo subMenu = (MenuInfo) menuItem.getChildren().get(0);
    assertNotNull(subMenu.getImage());
    // check items
    List<MenuItemInfo> items = subMenu.getChildrenItems();
    assertEquals(2, items.size());
    for (MenuItemInfo item : items) {
      assertNotNull(item.getModelBounds());
      assertNull(item.getImage());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Special case
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * We should be able to constructor {@link Menu} with {@link Decorations} in parameter, and use
   * {@link MenuItem#setMenu(Menu)} to establish association.
   */
  public void test_parse_DecorationsInConstructor() throws Exception {
    parseComposite(
        "public class Test extends Shell {",
        "  public Test() {",
        "    Menu bar = new Menu(this, SWT.BAR);",
        "    setMenuBar(bar);",
        "    {",
        "      MenuItem item = new MenuItem(bar, SWT.NONE);",
        "      {",
        "        Menu dropDown = new Menu(this, SWT.DROP_DOWN);",
        "        item.setMenu(dropDown);",
        "      }",
        "    }",
        "  }",
        "}");
    assertHierarchy(
        "{this: org.eclipse.swt.widgets.Shell} {this} {/new Menu(this, SWT.BAR)/ /setMenuBar(bar)/ /new Menu(this, SWT.DROP_DOWN)/}",
        "  {implicit-layout: absolute} {implicit-layout} {}",
        "  {new: org.eclipse.swt.widgets.Menu} {local-unique: bar} {/new Menu(this, SWT.BAR)/ /setMenuBar(bar)/ /new MenuItem(bar, SWT.NONE)/}",
        "    {new: org.eclipse.swt.widgets.MenuItem} {local-unique: item} {/new MenuItem(bar, SWT.NONE)/ /item.setMenu(dropDown)/}",
        "      {new: org.eclipse.swt.widgets.Menu} {local-unique: dropDown} {/new Menu(this, SWT.DROP_DOWN)/ /item.setMenu(dropDown)/}");
  }

  public void test_separatorItem_hasNoVariable() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    Menu menu = new Menu(this);",
            "    setMenu(menu);",
            "  }",
            "}");
    shell.refresh();
    MenuInfo menuInfo = shell.getChildren(MenuInfo.class).get(0);
    IMenuInfo menuObject = MenuObjectInfoUtils.getMenuInfo(menuInfo);
    IMenuPolicy menuPolicy = menuObject.getPolicy();
    // add new MenuItem as separator
    JavaInfo itemInfo = createJavaInfo("org.eclipse.swt.widgets.MenuItem", "separator");
    assertTrue(menuPolicy.validateCreate(itemInfo));
    menuPolicy.commandCreate(itemInfo, null);
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    Menu menu = new Menu(this);",
        "    setMenu(menu);",
        "    {",
        "      new MenuItem(menu, SWT.SEPARATOR);",
        "    }",
        "  }",
        "}");
  }

  /**
   * It is valid to set same {@link Menu} for multiple {@link Control}s.
   */
  public void test_parse_sharedContextMenu() throws Exception {
    parseComposite(
        "public class Test extends Shell {",
        "  public Test() {",
        "    Button button_1 = new Button(this, SWT.NONE);",
        "    Button button_2 = new Button(this, SWT.NONE);",
        "    //",
        "    Menu menu = new Menu(this);",
        "    button_1.setMenu(menu);",
        "    button_2.setMenu(menu);",
        "  }",
        "}");
    assertHierarchy(
        "{this: org.eclipse.swt.widgets.Shell} {this} {/new Button(this, SWT.NONE)/ /new Button(this, SWT.NONE)/ /new Menu(this)/}",
        "  {implicit-layout: absolute} {implicit-layout} {}",
        "  {new: org.eclipse.swt.widgets.Button} {local-unique: button_1} {/new Button(this, SWT.NONE)/ /button_1.setMenu(menu)/}",
        "    {new: org.eclipse.swt.widgets.Menu} {local-unique: menu} {/new Menu(this)/ /button_1.setMenu(menu)/ /button_2.setMenu(menu)/}",
        "  {new: org.eclipse.swt.widgets.Button} {local-unique: button_2} {/new Button(this, SWT.NONE)/ /button_2.setMenu(menu)/}");
  }
}