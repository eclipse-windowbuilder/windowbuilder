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
package org.eclipse.wb.tests.designer.XWT.model.widgets.menu;

import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.model.menu.IMenuInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuItemInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuPolicy;
import org.eclipse.wb.internal.core.model.menu.IMenuPopupInfo;
import org.eclipse.wb.internal.core.model.menu.MenuObjectInfoUtils;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.clipboard.XmlObjectMemento;
import org.eclipse.wb.internal.swt.model.widgets.menu.MenuStylePresentation;
import org.eclipse.wb.internal.swt.support.MenuSupport;
import org.eclipse.wb.internal.xwt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.xwt.model.widgets.ControlInfo;
import org.eclipse.wb.internal.xwt.model.widgets.WidgetInfo;
import org.eclipse.wb.internal.xwt.model.widgets.menu.MenuInfo;
import org.eclipse.wb.internal.xwt.model.widgets.menu.MenuItemInfo;
import org.eclipse.wb.internal.xwt.model.widgets.menu.MenuLiveManager;
import org.eclipse.wb.tests.designer.XWT.model.XwtModelTest;

import org.eclipse.swt.SWT;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;

/**
 * Tests for {@link MenuInfo}.
 * 
 * @author scheglov_ke
 */
public class MenuTest extends XwtModelTest {
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
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.menuBar>",
        "    <Menu wbp:name='menuBar' x:style='BAR'/>",
        "  </Shell.menuBar>",
        "  <Shell.menu>",
        "    <Menu wbp:name='menuPopup' x:style='POP_UP'>",
        "      <MenuItem x:Style='CASCADE'>",
        "        <MenuItem.menu>",
        "          <Menu wbp:name='menuCascade'/>",
        "        </MenuItem.menu>",
        "      </MenuItem>",
        "    </Menu>",
        "  </Shell.menu>",
        "</Shell>");
    refresh();
    // prepare models
    MenuInfo menuBar = getObjectByName("menuBar");
    MenuInfo menuPopup = getObjectByName("menuPopup");
    MenuInfo menuCascade = getObjectByName("menuCascade");
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
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.menuBar>",
        "    <Menu wbp:name='bar' x:style='BAR'/>",
        "  </Shell.menuBar>",
        "</Shell>");
    refresh();
    MenuInfo bar = getObjectByName("bar");
    // "bar" should not use client area offset of Shell, because "bar" itself is included in this offset
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
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.menuBar>",
        "    <Menu wbp:name='menu' x:style='BAR'/>",
        "  </Shell.menuBar>",
        "</Shell>");
    refresh();
    MenuInfo menu = getObjectByName("menu");
    assertTrue(menu.isBar());
    assertFalse(menu.isPopup());
  }

  /**
   * Test for {@link MenuInfo#isPopup()}.
   */
  public void test_isPopup() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.menu>",
        "    <Menu wbp:name='menu' x:style='POP_UP'/>",
        "  </Shell.menu>",
        "</Shell>");
    refresh();
    MenuInfo menu = getObjectByName("menu");
    assertTrue(menu.isPopup());
    assertFalse(menu.isBar());
  }

  /**
   * Test for {@link MenuLiveManager}.
   */
  public void test_liveStyle() throws Exception {
    parse("<Shell/>");
    // BAR
    {
      WidgetInfo menu = createObject("org.eclipse.swt.widgets.Menu", "bar");
      int actualStyle = menu.getStyle();
      assertTrue(
          "SWT.BAR bit expected, but " + Integer.toHexString(actualStyle) + " found.",
          (actualStyle & SWT.BAR) == SWT.BAR);
    }
    // POP_UP
    {
      WidgetInfo menu = createObject("org.eclipse.swt.widgets.Menu", null);
      int actualStyle = menu.getStyle();
      assertTrue(
          "SWT.POP_UP bit expected, but " + Integer.toHexString(actualStyle) + " found.",
          (actualStyle & SWT.POP_UP) == SWT.POP_UP);
    }
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
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.menuBar>",
        "    <Menu wbp:name='menu' x:style='BAR'/>",
        "  </Shell.menuBar>",
        "</Shell>");
    refresh();
    MenuInfo menuInfo = getObjectByName("menu");
    assertNull(menuInfo.getAdapter(List.class));
  }

  /**
   * Test for {@link IMenuInfo} of "bar" {@link MenuInfo}.
   */
  public void test_impl_IMenuInfo_bar() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.menuBar>",
        "    <Menu wbp:name='menuBar' x:style='BAR'>",
        "      <MenuItem text='Item'/>",
        "    </Menu>",
        "  </Shell.menuBar>",
        "</Shell>");
    refresh();
    // prepare models
    MenuInfo menuInfo = getObjectByName("menuBar");
    List<MenuItemInfo> itemInfos = menuInfo.getItems();
    // test IMenuInfo
    IMenuInfo menuObject = menuInfo.getAdapter(IMenuInfo.class);
    assertNotNull(menuObject);
    assertSame(menuInfo, menuObject.getModel());
    assertSame(menuInfo.getImage(), menuObject.getImage());
    assertSame(menuInfo.getBounds(), menuObject.getBounds());
    assertTrue(menuObject.isHorizontal());
    {
      List<IMenuItemInfo> itemObjects = menuObject.getItems();
      assertThat(itemObjects).hasSize(1);
      assertSame(itemInfos.get(0), itemObjects.get(0).getModel());
    }
  }

  /**
   * Test for {@link IMenuInfo} of "popup" {@link MenuInfo}.
   */
  public void test_impl_IMenuInfo_popup() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.menu>",
        "    <Menu wbp:name='menu' x:style='POP_UP'/>",
        "  </Shell.menu>",
        "</Shell>");
    refresh();
    MenuInfo menuInfo = getObjectByName("menu");
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
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.menu>",
        "    <Menu wbp:name='menu' x:style='POP_UP'/>",
        "  </Shell.menu>",
        "</Shell>");
    refresh();
    MenuInfo menuInfo = getObjectByName("menu");
    // test IMenuInfo
    IMenuInfo menuObject = menuInfo.getAdapter(IMenuInfo.class);
    IMenuPopupInfo popupObject = menuInfo.getAdapter(IMenuPopupInfo.class);
    assertNotNull(popupObject);
    assertSame(menuInfo, popupObject.getModel());
    assertSame(menuInfo.getPresentation().getIcon(), popupObject.getImage());
    assertEquals(16, popupObject.getBounds().width);
    assertEquals(16, popupObject.getBounds().height);
    assertSame(IMenuPolicy.NOOP, popupObject.getPolicy());
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
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.menu>",
        "    <Menu wbp:name='menu' x:style='POP_UP'/>",
        "  </Shell.menu>",
        "</Shell>");
    refresh();
    MenuInfo menuInfo = getObjectByName("menu");
    IMenuInfo menuObject = MenuObjectInfoUtils.getMenuInfo(menuInfo);
    IMenuPolicy menuPolicy = menuObject.getPolicy();
    // don't allow adding non-menu components
    {
      XmlObjectInfo button = createObject("org.eclipse.swt.widgets.Button");
      assertFalse(menuPolicy.validateCreate(button));
    }
    // add new MenuItem
    XmlObjectInfo itemInfo = createObject("org.eclipse.swt.widgets.MenuItem");
    assertTrue(menuPolicy.validateCreate(itemInfo));
    menuPolicy.commandCreate(itemInfo, null);
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.menu>",
        "    <Menu wbp:name='menu' x:style='POP_UP'>",
        "      <MenuItem text='New Item'/>",
        "    </Menu>",
        "  </Shell.menu>",
        "</Shell>");
  }

  /**
   * Tests for {@link IMenuInfo#commandCreate(Object, IMenuItemInfo)} with reference
   * {@link IMenuItemInfo}.
   */
  public void test_IMenuInfo_create_2() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "    <Menu wbp:name='menu' x:style='POP_UP'>",
        "      <MenuItem wbp:name='existingItem'/>",
        "    </Menu>",
        "</Shell>");
    refresh();
    MenuInfo menuInfo = getObjectByName("menu");
    MenuItemInfo itemInfo = getObjectByName("existingItem");
    // prepare menu objects
    IMenuInfo menuObject = MenuObjectInfoUtils.getMenuInfo(menuInfo);
    IMenuPolicy menuPolicy = menuObject.getPolicy();
    // add new MenuItem
    XmlObjectInfo newItemInfo = createObject("org.eclipse.swt.widgets.MenuItem");
    assertTrue(menuPolicy.validateCreate(itemInfo));
    menuPolicy.commandCreate(newItemInfo, itemInfo);
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "    <Menu wbp:name='menu' x:style='POP_UP'>",
        "      <MenuItem text='New Item'/>",
        "      <MenuItem wbp:name='existingItem'/>",
        "    </Menu>",
        "</Shell>");
  }

  /**
   * Test for adding {@link MenuItemInfo} with <code>CASCADE</code>.
   */
  public void test_IMenuInfo_create_3() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.menu>",
        "    <Menu wbp:name='menu' x:style='POP_UP'/>",
        "  </Shell.menu>",
        "</Shell>");
    refresh();
    MenuInfo menuInfo = getObjectByName("menu");
    IMenuInfo menuObject = MenuObjectInfoUtils.getMenuInfo(menuInfo);
    IMenuPolicy menuPolicy = menuObject.getPolicy();
    // add new MenuItem
    XmlObjectInfo itemInfo = createObject("org.eclipse.swt.widgets.MenuItem", "cascade");
    assertTrue(menuPolicy.validateCreate(itemInfo));
    menuPolicy.commandCreate(itemInfo, null);
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.menu>",
        "    <Menu wbp:name='menu' x:style='POP_UP'>",
        "      <MenuItem x:Style='CASCADE' text='New SubMenu'>",
        "        <MenuItem.menu>",
        "          <Menu/>",
        "        </MenuItem.menu>",
        "      </MenuItem>",
        "    </Menu>",
        "  </Shell.menu>",
        "</Shell>");
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
  public void test_IMenuInfo_paste_1() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "    <Menu wbp:name='menu' x:style='POP_UP'>",
        "      <MenuItem wbp:name='existingItem' text='Item'/>",
        "    </Menu>",
        "</Shell>");
    refresh();
    MenuInfo menuInfo = getObjectByName("menu");
    MenuItemInfo itemInfo = getObjectByName("existingItem");
    IMenuInfo menuObject = MenuObjectInfoUtils.getMenuInfo(menuInfo);
    final IMenuPolicy menuPolicy = menuObject.getPolicy();
    // do copy/paste
    XmlObjectMemento memento = XmlObjectMemento.createMemento(itemInfo);
    List<XmlObjectMemento> mementoList = Collections.singletonList(memento);
    assertTrue(menuPolicy.validatePaste(mementoList));
    menuPolicy.commandPaste(mementoList, null);
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "    <Menu wbp:name='menu' x:style='POP_UP'>",
        "      <MenuItem wbp:name='existingItem' text='Item'/>",
        "      <MenuItem text='Item'/>",
        "    </Menu>",
        "</Shell>");
  }

  /**
   * Don't allow "paste" for non-menu components.
   */
  public void test_IMenuInfo_paste_2() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "    <Menu wbp:name='menu' x:style='POP_UP'/>",
        "    <Button wbp:name='button'/>",
        "</Shell>");
    refresh();
    MenuInfo menuInfo = getObjectByName("menu");
    ControlInfo buttonInfo = getObjectByName("button");
    IMenuInfo menuObject = MenuObjectInfoUtils.getMenuInfo(menuInfo);
    IMenuPolicy menuPolicy = menuObject.getPolicy();
    // do copy/paste
    XmlObjectMemento memento = XmlObjectMemento.createMemento(buttonInfo);
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
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "    <Menu wbp:name='menu' x:style='POP_UP'>",
        "      <MenuItem wbp:name='item_1'/>",
        "      <MenuItem wbp:name='item_2'/>",
        "    </Menu>",
        "</Shell>");
    refresh();
    MenuInfo menuInfo = getObjectByName("menu");
    MenuItemInfo itemInfo_1 = getObjectByName("item_1");
    MenuItemInfo itemInfo_2 = getObjectByName("item_2");
    // prepare menu objects
    IMenuInfo menuObject = MenuObjectInfoUtils.getMenuInfo(menuInfo);
    IMenuPolicy menuPolicy = menuObject.getPolicy();
    // move "item_2" before "item_1"
    assertTrue(menuPolicy.validateMove(itemInfo_2));
    menuPolicy.commandMove(itemInfo_2, itemInfo_1);
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "    <Menu wbp:name='menu' x:style='POP_UP'>",
        "      <MenuItem wbp:name='item_2'/>",
        "      <MenuItem wbp:name='item_1'/>",
        "    </Menu>",
        "</Shell>");
  }

  /**
   * Don't allow moving non-menu component on {@link IMenuInfo}.
   */
  public void test_IMenuInfo_move_2() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "    <Menu wbp:name='menu' x:style='POP_UP'/>",
        "    <Button wbp:name='button'/>",
        "</Shell>");
    refresh();
    MenuInfo menuInfo = getObjectByName("menu");
    ControlInfo buttonInfo = getObjectByName("button");
    IMenuInfo menuObject = MenuObjectInfoUtils.getMenuInfo(menuInfo);
    IMenuPolicy menuPolicy = menuObject.getPolicy();
    // can not move
    assertFalse(menuPolicy.validateMove(buttonInfo));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Menu_Info.CREATE
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Add "bar" menu.
   */
  public void test_commandCreate_bar() throws Exception {
    final CompositeInfo shell = parse("<Shell/>");
    refresh();
    // add Menu
    ExecutionUtils.run(m_lastObject, new RunnableEx() {
      public void run() throws Exception {
        MenuInfo newMenuBar = createObject("org.eclipse.swt.widgets.Menu", "bar");
        newMenuBar.commandCreate(shell);
      }
    });
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.menuBar>",
        "    <Menu x:Style='BAR'/>",
        "  </Shell.menuBar>",
        "</Shell>");
  }

  /**
   * Add "popup" menu.
   */
  public void test_commandCreate_popup() throws Exception {
    final CompositeInfo shell = parse("<Shell/>");
    refresh();
    // add Menu
    ExecutionUtils.run(m_lastObject, new RunnableEx() {
      public void run() throws Exception {
        MenuInfo newMenuBar = createObject("org.eclipse.swt.widgets.Menu");
        newMenuBar.commandCreate(shell);
      }
    });
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.menu>",
        "    <Menu/>",
        "  </Shell.menu>",
        "</Shell>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Menu_Info.PASTE
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for copy/paste {@link MenuInfo} with {@link MenuItemInfo} and sub-menu.
   */
  public void test_commandPaste() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <RowLayout/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_1'>",
        "    <Button.menu>",
        "      <Menu>",
        "        <MenuItem text='My item' x:Style='CASCADE'>",
        "          <MenuItem.menu>",
        "            <Menu enabled='false'/>",
        "          </MenuItem.menu>",
        "        </MenuItem>",
        "      </Menu>",
        "    </Button.menu>",
        "  </Button>",
        "  <Button wbp:name='button_2'/>",
        "</Shell>");
    refresh();
    final ControlInfo button_1 = getObjectByName("button_1");
    final ControlInfo button_2 = getObjectByName("button_2");
    // do paste on "button_2"
    {
      MenuInfo menu = button_1.getChildren(MenuInfo.class).get(0);
      doCopyPaste(menu, new PasteProcedure<MenuInfo>() {
        public void run(MenuInfo copy) throws Exception {
          copy.commandCreate(button_2);
        }
      });
    }
    assertXML(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <RowLayout/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_1'>",
        "    <Button.menu>",
        "      <Menu>",
        "        <MenuItem text='My item' x:Style='CASCADE'>",
        "          <MenuItem.menu>",
        "            <Menu enabled='false'/>",
        "          </MenuItem.menu>",
        "        </MenuItem>",
        "      </Menu>",
        "    </Button.menu>",
        "  </Button>",
        "  <Button wbp:name='button_2'>",
        "    <Button.menu>",
        "      <Menu>",
        "        <MenuItem text='My item' x:Style='CASCADE'>",
        "          <MenuItem.menu>",
        "            <Menu enabled='false'/>",
        "          </MenuItem.menu>",
        "        </MenuItem>",
        "      </Menu>",
        "    </Button.menu>",
        "  </Button>",
        "</Shell>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Menu_Info.MOVE
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_commandMove_fromItem_toItem() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.menuBar>",
        "    <Menu wbp:name='menuBar' x:style='BAR'>",
        "      <MenuItem wbp:name='item_1' x:Style='CASCADE'>",
        "        <MenuItem.menu>",
        "          <Menu wbp:name='subMenu'/>",
        "        </MenuItem.menu>",
        "      </MenuItem>",
        "      <MenuItem wbp:name='item_2'/>",
        "    </Menu>",
        "  </Shell.menuBar>",
        "</Shell>");
    refresh();
    // prepare models
    final MenuItemInfo item_1 = getObjectByName("item_1");
    final MenuItemInfo item_2 = getObjectByName("item_2");
    final MenuInfo subMenu = item_1.getSubMenu();
    // do move
    ExecutionUtils.run(m_lastObject, new RunnableEx() {
      public void run() throws Exception {
        subMenu.commandMove(item_2);
      }
    });
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.menuBar>",
        "    <Menu wbp:name='menuBar' x:style='BAR'>",
        "      <MenuItem wbp:name='item_1'/>",
        "      <MenuItem wbp:name='item_2' x:Style='CASCADE'>",
        "        <MenuItem.menu>",
        "          <Menu wbp:name='subMenu'/>",
        "        </MenuItem.menu>",
        "      </MenuItem>",
        "    </Menu>",
        "  </Shell.menuBar>",
        "</Shell>");
  }

  public void test_commandMove_fromItem_toControl() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <RowLayout/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button'/>",
        "  <Shell.menuBar>",
        "    <Menu x:style='BAR'>",
        "      <MenuItem x:Style='CASCADE'>",
        "        <MenuItem.menu>",
        "          <Menu wbp:name='subMenu'/>",
        "        </MenuItem.menu>",
        "      </MenuItem>",
        "    </Menu>",
        "  </Shell.menuBar>",
        "</Shell>");
    refresh();
    // prepare models
    final ControlInfo button = getObjectByName("button");
    final MenuInfo subMenu = getObjectByName("subMenu");
    // do move
    ExecutionUtils.run(m_lastObject, new RunnableEx() {
      public void run() throws Exception {
        subMenu.commandMove(button);
      }
    });
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <RowLayout/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button'>",
        "    <Button.menu>",
        "      <Menu wbp:name='subMenu'/>",
        "    </Button.menu>",
        "  </Button>",
        "  <Shell.menuBar>",
        "    <Menu x:style='BAR'>",
        "      <MenuItem/>",
        "    </Menu>",
        "  </Shell.menuBar>",
        "</Shell>");
  }

  public void test_commandMove_fromControl_toItem() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <RowLayout/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button'>",
        "    <Button.menu>",
        "      <Menu wbp:name='subMenu'/>",
        "    </Button.menu>",
        "  </Button>",
        "  <Shell.menuBar>",
        "    <Menu x:style='BAR'>",
        "      <MenuItem wbp:name='item'/>",
        "    </Menu>",
        "  </Shell.menuBar>",
        "</Shell>");
    refresh();
    // prepare models
    final MenuInfo subMenu = getObjectByName("subMenu");
    final MenuItemInfo item = getObjectByName("item");
    // do move
    ExecutionUtils.run(m_lastObject, new RunnableEx() {
      public void run() throws Exception {
        subMenu.commandMove(item);
      }
    });
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <RowLayout/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button'/>",
        "  <Shell.menuBar>",
        "    <Menu x:style='BAR'>",
        "      <MenuItem wbp:name='item' x:Style='CASCADE'>",
        "        <MenuItem.menu>",
        "          <Menu wbp:name='subMenu'/>",
        "        </MenuItem.menu>",
        "      </MenuItem>",
        "    </Menu>",
        "  </Shell.menuBar>",
        "</Shell>");
  }

  public void test_commandMove_fromControl_toControl() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <RowLayout/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_1'>",
        "    <Button.menu>",
        "      <Menu wbp:name='menu'/>",
        "    </Button.menu>",
        "  </Button>",
        "  <Button wbp:name='button_2'/>",
        "</Shell>");
    refresh();
    // prepare models
    final ControlInfo button_1 = getObjectByName("button_1");
    final ControlInfo button_2 = getObjectByName("button_2");
    final MenuInfo menu = button_1.getChildren(MenuInfo.class).get(0);
    // do move
    ExecutionUtils.run(m_lastObject, new RunnableEx() {
      public void run() throws Exception {
        menu.commandMove(button_2);
      }
    });
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <RowLayout/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button_1'/>",
        "  <Button wbp:name='button_2'>",
        "    <Button.menu>",
        "      <Menu wbp:name='menu'/>",
        "    </Button.menu>",
        "  </Button>",
        "</Shell>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Misc
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for adding a placeholder to menu without items.
   */
  public void test_placeHolder() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.menuBar>",
        "    <Menu wbp:name='menuBar' x:style='BAR'/>",
        "  </Shell.menuBar>",
        "</Shell>");
    refresh();
    MenuInfo menuInfo = getObjectByName("menuBar");
    // no "item" models
    assertThat(menuInfo.getItems()).isEmpty();
    // even empty "menu" has "item" object
    Object menuObject = menuInfo.getObject();
    assertThat(MenuSupport.getItems(menuObject)).hasSize(1);
  }

  /**
   * Test fetching menu bar bounds and items bounds.
   */
  public void test_fetchVisualDataBar() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.menuBar>",
        "    <Menu wbp:name='menu' x:style='BAR'>",
        "      <MenuItem wbp:name='item_1'/>",
        "      <MenuItem wbp:name='item_2'/>",
        "    </Menu>",
        "  </Shell.menuBar>",
        "</Shell>");
    refresh();
    MenuInfo menu = getObjectByName("menu");
    // menu bar has bounds, but has no image
    assertNotNull(menu.getModelBounds());
    // menu bar in OSX has synthetic image which drawn above design canvas. 
    if (EnvironmentUtils.IS_MAC) {
      assertNotNull(menu.getImage());
    } else {
      assertNull(menu.getImage());
    }
    // items have bounds, but not image
    List<MenuItemInfo> items = menu.getItems();
    assertThat(items).hasSize(2);
    for (MenuItemInfo item : items) {
      assertNotNull(item.getModelBounds());
      assertNull(item.getImage());
    }
  }

  /**
   * Test fetching cascaded/popup menu bounds/image and items bounds.
   */
  public void test_fetchVisualDataCascaded() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.menuBar>",
        "    <Menu wbp:name='bar' x:style='BAR'>",
        "      <MenuItem x:Style='CASCADE'>",
        "        <MenuItem.menu>",
        "          <Menu wbp:name='subMenu'>",
        "            <MenuItem wbp:name='item_1'/>",
        "            <MenuItem wbp:name='item_2'/>",
        "          </Menu>",
        "        </MenuItem.menu>",
        "      </MenuItem>",
        "    </Menu>",
        "  </Shell.menuBar>",
        "</Shell>");
    refresh();
    MenuInfo subMenu = getObjectByName("subMenu");
    assertNotNull(subMenu.getImage());
    // check items
    List<MenuItemInfo> items = subMenu.getItems();
    assertThat(items).hasSize(2);
    for (MenuItemInfo item : items) {
      assertNotNull(item.getModelBounds());
      assertNull(item.getImage());
    }
  }

  public void test_menuAboveOtherGraphicalChildren() throws Exception {
    CompositeInfo shell =
        parse(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "<Shell>",
            "  <Shell.layout>",
            "    <RowLayout/>",
            "  </Shell.layout>",
            "  <Button wbp:name='button'/>",
            "  <Shell.menuBar>",
            "    <Menu wbp:name='menu' x:style='BAR'/>",
            "  </Shell.menuBar>",
            "</Shell>");
    assertHierarchy(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <RowLayout>",
        "  <Menu wbp:name='menu' x:style='BAR'>",
        "  <Button wbp:name='button'>",
        "    virtual-LayoutData: org.eclipse.swt.layout.RowData");
    refresh();
    // prepare models
    MenuInfo menu = getObjectByName("menu");
    ControlInfo button = getObjectByName("button");
    assertThat(shell.getPresentation().getChildrenGraphical()).containsExactly(menu, button);
  }
}