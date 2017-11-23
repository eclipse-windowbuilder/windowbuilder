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

import org.eclipse.wb.internal.core.model.menu.IMenuItemInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuPolicy;
import org.eclipse.wb.internal.core.model.menu.MenuObjectInfoUtils;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.xml.model.property.GenericProperty;
import org.eclipse.wb.internal.xwt.model.widgets.WidgetInfo;
import org.eclipse.wb.internal.xwt.model.widgets.menu.MenuInfo;
import org.eclipse.wb.internal.xwt.model.widgets.menu.MenuItemInfo;
import org.eclipse.wb.internal.xwt.model.widgets.menu.MenuItemLiveManager;
import org.eclipse.wb.internal.xwt.model.widgets.menu.MenuItemStylePresentation;
import org.eclipse.wb.tests.designer.XWT.model.XwtModelTest;

import org.eclipse.swt.SWT;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

/**
 * Tests for {@link MenuItemInfo}.
 * 
 * @author scheglov_ke
 */
public class MenuItemTest extends XwtModelTest {
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
  // Parse
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for parsing {@link MenuInfo}, {@link MenuItemInfo} and its basic properties.
   */
  public void test_parse() throws Exception {
    parse(
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
    refresh();
    // prepare models
    MenuInfo menuBar = getObjectByName("menuBar");
    MenuItemInfo item_1 = getObjectByName("item_1");
    MenuItemInfo item_2 = getObjectByName("item_2");
    MenuInfo subMenu = getObjectByName("subMenu");
    // check items
    List<MenuItemInfo> items = menuBar.getItems();
    assertThat(items).containsExactly(item_1, item_2);
    // "item_1" has no subMenu
    assertSame(null, item_1.getSubMenu());
    // "item_2" has subMenu
    assertSame(subMenu, item_2.getSubMenu());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Style
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link MenuItemLiveManager}.
   */
  public void test_liveStyle() throws Exception {
    parse("<Shell/>");
    // NONE
    {
      WidgetInfo menuItem = createObject("org.eclipse.swt.widgets.MenuItem");
      int actualStyle = menuItem.getStyle();
      assertTrue(
          "SWT.PUSH bit expected, but " + Integer.toHexString(actualStyle) + " found.",
          (actualStyle & SWT.PUSH) == SWT.PUSH);
      assertFalse("Not SWT.CASCADE bit expected, but "
          + Integer.toHexString(actualStyle)
          + " found.", (actualStyle & SWT.CASCADE) == SWT.CASCADE);
    }
    // CHECK
    {
      WidgetInfo menuItem = createObject("org.eclipse.swt.widgets.MenuItem", "check");
      int actualStyle = menuItem.getStyle();
      assertTrue(
          "SWT.CHECK bit expected, but " + Integer.toHexString(actualStyle) + " found.",
          (actualStyle & SWT.CHECK) == SWT.CHECK);
    }
    // RADIO
    {
      WidgetInfo menuItem = createObject("org.eclipse.swt.widgets.MenuItem", "radio");
      int actualStyle = menuItem.getStyle();
      assertTrue(
          "SWT.RADIO bit expected, but " + Integer.toHexString(actualStyle) + " found.",
          (actualStyle & SWT.RADIO) == SWT.RADIO);
    }
    // CASCADE
    {
      WidgetInfo menuItem = createObject("org.eclipse.swt.widgets.MenuItem", "cascade");
      int actualStyle = menuItem.getStyle();
      assertTrue(
          "SWT.CASCADE bit expected, but " + Integer.toHexString(actualStyle) + " found.",
          (actualStyle & SWT.CASCADE) == SWT.CASCADE);
    }
  }

  /**
   * Test for {@link MenuItemStylePresentation}.
   */
  public void test_MenuItemStylePresentation() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.menuBar>",
        "    <Menu wbp:name='menuBar' x:style='BAR'>",
        "      <MenuItem x:Style='NONE'/>",
        "      <MenuItem x:Style='PUSH'/>",
        "      <MenuItem x:Style='CHECK'/>",
        "      <MenuItem x:Style='RADIO'/>",
        "      <MenuItem x:Style='SEPARATOR'/>",
        "    </Menu>",
        "  </Shell.menuBar>",
        "</Shell>");
    refresh();
    // prepare models
    MenuInfo menuBar = getObjectByName("menuBar");
    List<MenuItemInfo> menuItems = menuBar.getItems();
    MenuItemInfo itemDefault = menuItems.get(0);
    MenuItemInfo itemPush = menuItems.get(1);
    MenuItemInfo itemCheck = menuItems.get(2);
    MenuItemInfo itemRadio = menuItems.get(3);
    MenuItemInfo itemSeparator = menuItems.get(4);
    // test icons
    assertSame(itemDefault.getPresentation().getIcon(), itemPush.getPresentation().getIcon());
    assertNotSame(itemDefault.getPresentation().getIcon(), itemCheck.getPresentation().getIcon());
    assertNotSame(itemDefault.getPresentation().getIcon(), itemRadio.getPresentation().getIcon());
    assertNotSame(
        itemDefault.getPresentation().getIcon(),
        itemSeparator.getPresentation().getIcon());
    // test text
    assertEquals("MenuItem", itemDefault.getPresentation().getText());
    assertEquals("<separator>", itemSeparator.getPresentation().getText());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Impl
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test that {@link MenuItemInfo} can be adapted to {@link IMenuItemInfo}.
   */
  public void test_impl() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
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
    refresh();
    // prepare models
    MenuItemInfo itemInfo = getObjectByName("item");
    MenuInfo menuInfo = itemInfo.getSubMenu();
    // no adapter for random Class
    assertNull(itemInfo.getAdapter(List.class));
    // check "impl"
    {
      IMenuItemInfo itemObject = itemInfo.getAdapter(IMenuItemInfo.class);
      // IMenuItemInfo
      assertSame(itemInfo, itemObject.getModel());
      assertSame(MenuObjectInfoUtils.getMenuInfo(menuInfo), itemObject.getMenu());
      // presentation
      assertSame(itemInfo.getImage(), itemObject.getImage());
      assertSame(itemInfo.getBounds(), itemObject.getBounds());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Style changing
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Set {@link MenuItemInfo} style to SWT.CASCADE. A sub menu should be added.
   */
  public void test_setStyle_toCascade() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.menuBar>",
        "    <Menu x:style='BAR'>",
        "      <MenuItem wbp:name='item' x:Style='CHECK'/>",
        "    </Menu>",
        "  </Shell.menuBar>",
        "</Shell>");
    refresh();
    // set style to SWT.CASCADE
    ExecutionUtils.run(m_lastObject, new RunnableEx() {
      public void run() throws Exception {
        MenuItemInfo item = getObjectByName("item");
        GenericProperty styleProperty = (GenericProperty) item.getPropertyByTitle("Style");
        styleProperty.setExpression("CASCADE", Property.UNKNOWN_VALUE);
      }
    });
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.menuBar>",
        "    <Menu x:style='BAR'>",
        "      <MenuItem wbp:name='item' x:Style='CASCADE'>",
        "        <MenuItem.menu>",
        "          <Menu/>",
        "        </MenuItem.menu>",
        "      </MenuItem>",
        "    </Menu>",
        "  </Shell.menuBar>",
        "</Shell>");
  }

  /**
   * Set {@link MenuItemInfo} style to anything else except SWT.CASCADE and SWT.SEPARATOR.
   * <p>
   * Only the style should changed.
   */
  public void test_setStyle_noCascade() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.menuBar>",
        "    <Menu x:style='BAR'>",
        "      <MenuItem wbp:name='item' x:Style='RADIO'/>",
        "    </Menu>",
        "  </Shell.menuBar>",
        "</Shell>");
    refresh();
    // set style to SWT.CHECK
    ExecutionUtils.run(m_lastObject, new RunnableEx() {
      public void run() throws Exception {
        MenuItemInfo item = getObjectByName("item");
        GenericProperty styleProperty = (GenericProperty) item.getPropertyByTitle("Style");
        styleProperty.setExpression("CHECK", Property.UNKNOWN_VALUE);
      }
    });
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.menuBar>",
        "    <Menu x:style='BAR'>",
        "      <MenuItem wbp:name='item' x:Style='CHECK'/>",
        "    </Menu>",
        "  </Shell.menuBar>",
        "</Shell>");
  }

  /**
   * Set {@link MenuItemInfo} style from SWT.CASCADE style to anything else.
   * <p>
   * Sub menu should be removed.
   */
  public void test_setStyle_fromCascade() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.menuBar>",
        "    <Menu x:style='BAR'>",
        "      <MenuItem wbp:name='item' x:Style='CASCADE'>",
        "        <MenuItem.menu>",
        "          <Menu/>",
        "        </MenuItem.menu>",
        "      </MenuItem>",
        "    </Menu>",
        "  </Shell.menuBar>",
        "</Shell>");
    refresh();
    // set style to SWT.CHECK
    ExecutionUtils.run(m_lastObject, new RunnableEx() {
      public void run() throws Exception {
        MenuItemInfo item = getObjectByName("item");
        GenericProperty styleProperty = (GenericProperty) item.getPropertyByTitle("Style");
        styleProperty.setExpression("CHECK", Property.UNKNOWN_VALUE);
      }
    });
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.menuBar>",
        "    <Menu x:style='BAR'>",
        "      <MenuItem wbp:name='item' x:Style='CHECK'/>",
        "    </Menu>",
        "  </Shell.menuBar>",
        "</Shell>");
  }

  /**
   * Set {@link MenuItemInfo} style from SWT.CASCADE style to anything else.
   * <p>
   * No sub menu, so nothing to remove.
   */
  public void test_setStyle_fromCascade_noSubMenu() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.menuBar>",
        "    <Menu x:style='BAR'>",
        "      <MenuItem wbp:name='item' x:Style='CASCADE'/>",
        "    </Menu>",
        "  </Shell.menuBar>",
        "</Shell>");
    refresh();
    // set style to SWT.CHECK
    ExecutionUtils.run(m_lastObject, new RunnableEx() {
      public void run() throws Exception {
        MenuItemInfo item = getObjectByName("item");
        GenericProperty styleProperty = (GenericProperty) item.getPropertyByTitle("Style");
        styleProperty.setExpression("CHECK", Property.UNKNOWN_VALUE);
      }
    });
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.menuBar>",
        "    <Menu x:style='BAR'>",
        "      <MenuItem wbp:name='item' x:Style='CHECK'/>",
        "    </Menu>",
        "  </Shell.menuBar>",
        "</Shell>");
  }

  /**
   * Set {@link MenuItemInfo} style to SWT.SEPARATOR.
   * <p>
   * <code>setText()</code> should be removed.
   */
  public void test_setStyle_toSeparator() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.menuBar>",
        "    <Menu x:style='BAR'>",
        "      <MenuItem wbp:name='item' text='My item'/>",
        "    </Menu>",
        "  </Shell.menuBar>",
        "</Shell>");
    refresh();
    // set style to SWT.SEPARATOR
    ExecutionUtils.run(m_lastObject, new RunnableEx() {
      public void run() throws Exception {
        MenuItemInfo item = getObjectByName("item");
        GenericProperty styleProperty = (GenericProperty) item.getPropertyByTitle("Style");
        styleProperty.setExpression("SEPARATOR", Property.UNKNOWN_VALUE);
      }
    });
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.menuBar>",
        "    <Menu x:style='BAR'>",
        "      <MenuItem wbp:name='item' x:Style='SEPARATOR'/>",
        "    </Menu>",
        "  </Shell.menuBar>",
        "</Shell>");
  }

  /**
   * Set {@link MenuItemInfo} style from SWT.CASCADE to SWT.SEPARATOR.
   * <p>
   * Sub menu and <code>setText</code> should be removed because the separator doesn't need it.
   */
  public void test_setStyle_fromCascade_toSeparator() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.menuBar>",
        "    <Menu x:style='BAR'>",
        "      <MenuItem wbp:name='item' text='My item' x:Style='CASCADE'>",
        "        <MenuItem.menu>",
        "          <Menu/>",
        "        </MenuItem.menu>",
        "      </MenuItem>",
        "    </Menu>",
        "  </Shell.menuBar>",
        "</Shell>");
    refresh();
    // set style to SWT.SEPARATOR
    ExecutionUtils.run(m_lastObject, new RunnableEx() {
      public void run() throws Exception {
        MenuItemInfo item = getObjectByName("item");
        GenericProperty styleProperty = (GenericProperty) item.getPropertyByTitle("Style");
        styleProperty.setExpression("SEPARATOR", Property.UNKNOWN_VALUE);
      }
    });
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.menuBar>",
        "    <Menu x:style='BAR'>",
        "      <MenuItem wbp:name='item' x:Style='SEPARATOR'/>",
        "    </Menu>",
        "  </Shell.menuBar>",
        "</Shell>");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IMenuItemInfo operations
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * {@link MenuItemInfo} does not accept anything with create/paste.
   */
  public void test_IMenuItemInfo_noCreateOrPaste() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.menuBar>",
        "    <Menu x:style='BAR'>",
        "      <MenuItem wbp:name='item'/>",
        "    </Menu>",
        "  </Shell.menuBar>",
        "</Shell>");
    refresh();
    String source = m_lastContext.getContent();
    // prepare models
    MenuItemInfo itemInfo = getObjectByName("item");
    IMenuItemInfo itemObject = MenuObjectInfoUtils.getMenuItemInfo(itemInfo);
    IMenuPolicy itemPolicy = itemObject.getPolicy();
    // no "create"
    {
      MenuInfo newMenuInfo = createObject("org.eclipse.swt.widgets.Menu");
      assertFalse(itemPolicy.validateCreate(newMenuInfo));
      itemPolicy.commandCreate(newMenuInfo, null);
    }
    // no "paste"
    {
      Object anyObject = new Object();
      assertFalse(itemPolicy.validatePaste(anyObject));
      itemPolicy.commandPaste(anyObject, null);
    }
    // no changes
    assertEquals(source, m_lastContext.getContent());
  }

  /**
   * {@link MenuInfo} can be moved on {@link MenuItemInfo}.
   */
  public void test_IMenuItemInfo_move() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.menuBar>",
        "    <Menu wbp:name='bar' x:style='BAR'>",
        "      <MenuItem wbp:name='item_1' x:Style='CHECK'/>",
        "      <MenuItem wbp:name='item_2' x:Style='CASCADE'>",
        "        <MenuItem.menu>",
        "          <Menu wbp:name='subMenu'/>",
        "        </MenuItem.menu>",
        "      </MenuItem>",
        "    </Menu>",
        "  </Shell.menuBar>",
        "</Shell>");
    refresh();
    // prepare models
    MenuInfo barInfo = getObjectByName("bar");
    MenuItemInfo itemInfo_1 = getObjectByName("item_1");
    MenuItemInfo itemInfo_2 = getObjectByName("item_2");
    final MenuInfo subMenuInfo = getObjectByName("subMenu");
    // prepare menu objects
    final IMenuItemInfo itemObject_1 = MenuObjectInfoUtils.getMenuItemInfo(itemInfo_1);
    final IMenuPolicy itemPolicy_1 = itemObject_1.getPolicy();
    final IMenuItemInfo itemObject_2 = MenuObjectInfoUtils.getMenuItemInfo(itemInfo_2);
    final IMenuPolicy itemPolicy_2 = itemObject_2.getPolicy();
    // can not move just random object
    assertFalse(itemPolicy_2.validateMove(new Object()));
    // can not move "bar" on its own child "item_2"
    assertFalse(itemPolicy_2.validateMove(barInfo));
    // move "subMenu" from "item_2" to "item_1"
    ExecutionUtils.run(m_lastObject, new RunnableEx() {
      public void run() throws Exception {
        assertTrue(itemPolicy_1.validateMove(subMenuInfo));
        itemPolicy_1.commandMove(subMenuInfo, null);
      }
    });
    assertXML(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.menuBar>",
        "    <Menu wbp:name='bar' x:style='BAR'>",
        "      <MenuItem wbp:name='item_1' x:Style='CASCADE'>",
        "        <MenuItem.menu>",
        "          <Menu wbp:name='subMenu'/>",
        "        </MenuItem.menu>",
        "      </MenuItem>",
        "      <MenuItem wbp:name='item_2'/>",
        "    </Menu>",
        "  </Shell.menuBar>",
        "</Shell>");
  }
}