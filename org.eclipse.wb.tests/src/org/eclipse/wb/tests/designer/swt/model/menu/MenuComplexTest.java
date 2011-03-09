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

import com.google.common.collect.ImmutableList;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.draw2d.IPositionConstants;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.tools.CreationTool;
import org.eclipse.wb.gef.core.tools.PasteTool;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.internal.swt.model.widgets.menu.MenuInfo;
import org.eclipse.wb.internal.swt.model.widgets.menu.MenuItemInfo;
import org.eclipse.wb.tests.designer.rcp.RcpGefTest;

/**
 * Tests for "popup" with several item's, sub-menu's, etc.
 * 
 * @author scheglov_ke
 */
public class MenuComplexTest extends RcpGefTest {
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
   * When we select "item" in components tree, it shows drop-down and becomes selected in GEF.<br>
   * Even if this "item" is deep in menu hierarchy.
   */
  public void test_selectDeepItemInTree() throws Exception {
    CompositeInfo shellInfo =
        openComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    Menu popup = new Menu(this);",
            "    setMenu(popup);",
            "    {",
            "      MenuItem item_1 = new MenuItem(popup, SWT.CASCADE);",
            "      item_1.setText('Item 1');",
            "      {",
            "        Menu menu_1 = new Menu(item_1);",
            "        item_1.setMenu(menu_1);",
            "        {",
            "          MenuItem item_2 = new MenuItem(menu_1, SWT.NONE);",
            "          item_2.setText('Item 2');",
            "        }",
            "      }",
            "    }",
            "  }",
            "}");
    MenuInfo popupInfo = shellInfo.getChildren(MenuInfo.class).get(0);
    MenuItemInfo item_1 = popupInfo.getChildrenItems().get(0);
    MenuInfo menu_1 = item_1.getChildren(MenuInfo.class).get(0);
    MenuItemInfo item_2 = menu_1.getChildrenItems().get(0);
    // prepare EditPart's
    EditPart popupPart = canvas.getEditPart(popupInfo);
    // select "item_2" in tree: drop-down appears, with selected "item_2"
    EditPart dropPart;
    EditPart itemPart_1;
    EditPart menuPart_1;
    EditPart itemPart_2;
    {
      tree.select(item_2);
      dropPart = popupPart.getChildren().get(0);
      itemPart_1 = canvas.getEditPart(item_1);
      menuPart_1 = canvas.getEditPart(menu_1);
      itemPart_2 = canvas.getEditPart(item_2);
      assertNotNull(dropPart);
      assertNotNull(itemPart_1);
      assertNotNull(menuPart_1);
      assertNotNull(itemPart_2);
      // check selection
      assertEquals(EditPart.SELECTED_PRIMARY, itemPart_2.getSelected());
    }
    // select "shell": menu EditPart's removed
    tree.select(shellInfo);
    assertTrue(popupPart.getChildren().isEmpty());
    canvas.assertNullEditPart(item_1);
    canvas.assertNullEditPart(menu_1);
    canvas.assertNullEditPart(item_2);
  }

  /**
   * When we select "subMenu", its parent "item" should paint thick line around himself.
   */
  public void test_selectSubMenu() throws Exception {
    CompositeInfo shellInfo =
        openComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    Menu bar = new Menu(this, SWT.BAR);",
            "    setMenuBar(bar);",
            "    {",
            "      MenuItem item = new MenuItem(bar, SWT.CASCADE);",
            "      item.setText('Item');",
            "      {",
            "        Menu menu = new Menu(item);",
            "        item.setMenu(menu);",
            "      }",
            "    }",
            "  }",
            "}");
    MenuInfo barInfo = shellInfo.getChildren(MenuInfo.class).get(0);
    MenuItemInfo item = barInfo.getChildrenItems().get(0);
    MenuInfo menu = item.getChildren(MenuInfo.class).get(0);
    // initially "menu" is not visible
    canvas.assertNullEditPart(menu);
    // select "menu" and wait paint, just to cover this code
    tree.select(menu);
    assertNotNull(canvas.getEditPart(menu));
    waitEventLoop(0);
  }

  /**
   * "Menu" of "item" on "bar" is located directly below "item" (in absolute coordinates).
   */
  public void test_barSubMenuLocation() throws Exception {
    CompositeInfo shellInfo =
        openComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    Menu bar = new Menu(this, SWT.BAR);",
            "    setMenuBar(bar);",
            "    {",
            "      MenuItem item = new MenuItem(bar, SWT.CASCADE);",
            "      item.setText('Item');",
            "      {",
            "        Menu menu = new Menu(item);",
            "        item.setMenu(menu);",
            "      }",
            "    }",
            "  }",
            "}");
    MenuInfo barInfo = shellInfo.getChildren(MenuInfo.class).get(0);
    MenuItemInfo itemInfo = barInfo.getChildrenItems().get(0);
    MenuInfo menuInfo = itemInfo.getChildren(MenuInfo.class).get(0);
    // select "item": "menu" appears
    tree.select(itemInfo);
    // drop-down is located directly below "popup" (in absolute coordinates)
    assertEquals(
        canvas.getAbsoluteBounds(itemInfo).getBottomLeft(),
        canvas.getEditPart(menuInfo).getFigure().getLocation());
  }

  /**
   * "Menu" of "item" on "popup" is located on the right from "item" (in absolute coordinates).
   */
  public void test_popupSubMenuLocation() throws Exception {
    CompositeInfo shellInfo =
        openComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    Menu popup = new Menu(this);",
            "    setMenu(bar);",
            "    {",
            "      MenuItem item = new MenuItem(popup, SWT.CASCADE);",
            "      item.setText('Item');",
            "      {",
            "        Menu menu = new Menu(item);",
            "        item.setMenu(menu);",
            "      }",
            "    }",
            "  }",
            "}");
    MenuInfo popupInfo = shellInfo.getChildren(MenuInfo.class).get(0);
    MenuItemInfo itemInfo = popupInfo.getChildrenItems().get(0);
    MenuInfo menuInfo = itemInfo.getChildren(MenuInfo.class).get(0);
    // select "item": "menu" appears
    tree.select(itemInfo);
    // check "menu" bounds
    assertEquals(
        canvas.getAbsoluteBounds(itemInfo).getTopRight().getTranslated(-3, -2),
        canvas.getEditPart(menuInfo).getFigure().getLocation());
  }

  /**
   * Check that when there are two {@link ControlInfo}'s and each has "popup", selecting one "popup"
   * causes dropping only this "popup", but not second one.
   */
  public void test_twoPopupMenus() throws Exception {
    CompositeInfo shellInfo =
        openComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    {",
            "      Button button_1 = new Button(this, SWT.NONE);",
            "      button_1.setText('Button 1');",
            "      {",
            "        Menu popup_1 = new Menu(button_1);",
            "        button_1.setMenu(popup_1);",
            "      }",
            "    }",
            "    {",
            "      Button button_2 = new Button(this, SWT.NONE);",
            "      button_2.setText('Button 2');",
            "      {",
            "        Menu popup_2 = new Menu(button_2);",
            "        button_2.setMenu(popup_2);",
            "      }",
            "    }",
            "  }",
            "}");
    ControlInfo button_1 = shellInfo.getChildrenControls().get(0);
    ControlInfo button_2 = shellInfo.getChildrenControls().get(1);
    MenuInfo popup_1 = button_1.getChildren(MenuInfo.class).get(0);
    MenuInfo popup_2 = button_2.getChildren(MenuInfo.class).get(0);
    // prepare EditPart's
    EditPart popupPart_1 = canvas.getEditPart(popup_1);
    EditPart popupPart_2 = canvas.getEditPart(popup_2);
    // initially no drop-down's
    assertEquals(0, popupPart_1.getChildren().size());
    assertEquals(0, popupPart_2.getChildren().size());
    // select "popup_1": its drop-down should be displayed
    {
      tree.select(popup_1);
      assertEquals(1, popupPart_1.getChildren().size());
      assertEquals(0, popupPart_2.getChildren().size());
    }
    // now select "popup_2": its drop-down displayed, but drop-down for "popup_1" should be closed
    {
      tree.select(popup_2);
      assertEquals(0, popupPart_1.getChildren().size());
      assertEquals(1, popupPart_2.getChildren().size());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Delete
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * When we delete item selection moves on item above.
   */
  public void test_deleteUp() throws Exception {
    CompositeInfo shellInfo =
        openComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    Menu popup = new Menu(this);",
            "    setMenu(popup);",
            "    new MenuItem(popup, SWT.NONE).setText('Item 1');",
            "    new MenuItem(popup, SWT.NONE).setText('Item 2');",
            "    new MenuItem(popup, SWT.NONE).setText('Item 3');",
            "  }",
            "}");
    MenuInfo popupInfo = shellInfo.getChildren(MenuInfo.class).get(0);
    MenuItemInfo item_1 = popupInfo.getChildrenItems().get(0);
    MenuItemInfo item_2 = popupInfo.getChildrenItems().get(1);
    EditPart popupPart = canvas.getEditPart(popupInfo);
    // select "item_2" in tree: drop-down appears, with selected "item_2"
    EditPart itemPart_1;
    EditPart itemPart_2;
    EditPart dropPart;
    {
      tree.select(item_2);
      // prepare EditPart's
      dropPart = popupPart.getChildren().get(0);
      assertEquals(3, dropPart.getChildren().size());
      itemPart_1 = canvas.getEditPart(item_1);
      itemPart_2 = canvas.getEditPart(item_2);
      assertNotNull(dropPart);
      assertNotNull(itemPart_1);
      assertNotNull(itemPart_2);
      // check selection
      assertEquals(EditPart.SELECTED_PRIMARY, itemPart_2.getSelected());
    }
    // delete "item_2": selection should be moved to "item_1"
    item_2.delete();
    {
      assertSame(dropPart, popupPart.getChildren().get(0));
      assertSame(itemPart_1, canvas.getEditPart(item_1));
      assertEquals(EditPart.SELECTED_PRIMARY, itemPart_1.getSelected());
    }
  }

  /**
   * When we delete first item selection moves on item below.
   */
  public void test_deleteFirst() throws Exception {
    CompositeInfo shellInfo =
        openComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    Menu popup = new Menu(this);",
            "    setMenu(popup);",
            "    new MenuItem(popup, SWT.NONE).setText('Item 1');",
            "    new MenuItem(popup, SWT.NONE).setText('Item 2');",
            "    new MenuItem(popup, SWT.NONE).setText('Item 3');",
            "  }",
            "}");
    MenuInfo popupInfo = shellInfo.getChildren(MenuInfo.class).get(0);
    MenuItemInfo item_1 = popupInfo.getChildrenItems().get(0);
    MenuItemInfo item_2 = popupInfo.getChildrenItems().get(1);
    EditPart popupPart = canvas.getEditPart(popupInfo);
    // select "item_1" in tree: drop-down appears, with selected "item_1"
    EditPart itemPart_1;
    EditPart itemPart_2;
    EditPart dropPart;
    {
      tree.select(item_1);
      // prepare EditPart's
      dropPart = popupPart.getChildren().get(0);
      assertEquals(3, dropPart.getChildren().size());
      itemPart_1 = canvas.getEditPart(item_1);
      itemPart_2 = canvas.getEditPart(item_2);
      assertNotNull(dropPart);
      assertNotNull(itemPart_1);
      assertNotNull(itemPart_2);
      // check selection
      assertEquals(EditPart.SELECTED_PRIMARY, itemPart_1.getSelected());
    }
    // delete "item_1": selection should be moved to "item_2"
    item_1.delete();
    {
      assertSame(dropPart, popupPart.getChildren().get(0));
      assertSame(itemPart_2, canvas.getEditPart(item_2));
      assertEquals(EditPart.SELECTED_PRIMARY, itemPart_2.getSelected());
    }
  }

  /**
   * When we delete sole item selection moves on parent.
   */
  public void test_deleteSole() throws Exception {
    CompositeInfo shellInfo =
        openComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    Menu popup = new Menu(this);",
            "    setMenu(popup);",
            "    new MenuItem(popup, SWT.NONE);",
            "  }",
            "}");
    MenuInfo popupInfo = shellInfo.getChildren(MenuInfo.class).get(0);
    MenuItemInfo item = popupInfo.getChildrenItems().get(0);
    EditPart popupPart = canvas.getEditPart(popupInfo);
    // select "item" in tree: drop-down appears, with selected "item"
    EditPart itemPart;
    EditPart dropPart;
    {
      tree.select(item);
      // prepare EditPart's
      dropPart = popupPart.getChildren().get(0);
      itemPart = canvas.getEditPart(item);
      assertNotNull(dropPart);
      assertNotNull(itemPart);
      // check selection
      assertEquals(EditPart.SELECTED_PRIMARY, itemPart.getSelected());
    }
    // delete "item": selection should be moved to "popup"
    item.delete();
    {
      assertSame(dropPart, popupPart.getChildren().get(0));
      assertEquals(EditPart.SELECTED_PRIMARY, popupPart.getSelected());
    }
  }

  /**
   * Test for deleting "item" with "subMenu"
   */
  public void test_deleteItemWithSubmenu() throws Exception {
    CompositeInfo shellInfo =
        openComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    Menu bar = new Menu(this, SWT.BAR);",
            "    setMenuBar(bar);",
            "    {",
            "      MenuItem item_1 = new MenuItem(bar, SWT.NONE);",
            "      item_1.setText('Item 1');",
            "    }",
            "    {",
            "      MenuItem item_2 = new MenuItem(bar, SWT.CASCADE);",
            "      item_2.setText('Item 2');",
            "      {",
            "        Menu menu_2 = new Menu(item_2);",
            "        item_2.setMenu(menu_2);",
            "      }",
            "    }",
            "  }",
            "}");
    MenuInfo barInfo = shellInfo.getChildren(MenuInfo.class).get(0);
    MenuItemInfo item_1 = barInfo.getChildrenItems().get(0);
    MenuItemInfo item_2 = barInfo.getChildrenItems().get(1);
    MenuInfo menu_2 = item_2.getChildren(MenuInfo.class).get(0);
    // select "item_2" in tree: "menu_2" appears
    {
      tree.select(item_2);
      canvas.assertPrimarySelected(item_2);
      canvas.assertNotNullEditPart(menu_2);
    }
    // delete "item_2": selection should be moved on "item_1"
    item_2.delete();
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    Menu bar = new Menu(this, SWT.BAR);",
        "    setMenuBar(bar);",
        "    {",
        "      MenuItem item_1 = new MenuItem(bar, SWT.NONE);",
        "      item_1.setText('Item 1');",
        "    }",
        "  }",
        "}");
    canvas.assertPrimarySelected(item_1);
    assertSelectionModels(item_1);
  }

  /**
   * Test for deleting "bar" with "item" with "subMenu"
   */
  public void test_deleteBarWithSubmenu() throws Exception {
    CompositeInfo shellInfo =
        openComposite(
            "// filler filler filler",
            "public class Test extends Shell {",
            "  public Test() {",
            "    Menu bar = new Menu(this, SWT.BAR);",
            "    setMenuBar(bar);",
            "    {",
            "      MenuItem item = new MenuItem(bar, SWT.CASCADE);",
            "      item.setText('Item');",
            "      {",
            "        Menu menu = new Menu(item);",
            "        item.setMenu(menu);",
            "      }",
            "    }",
            "  }",
            "}");
    MenuInfo barInfo = shellInfo.getChildren(MenuInfo.class).get(0);
    // delete "bar": no any problems expected
    barInfo.delete();
    assertEditor(
        "// filler filler filler",
        "public class Test extends Shell {",
        "  public Test() {",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Create
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Add new item deep into popup menu hierarchy.
   */
  public void test_CREATE_popup_newItem_deep() throws Exception {
    CompositeInfo shellInfo =
        openComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    Menu popup = new Menu(this);",
            "    setMenu(popup);",
            "    {",
            "      MenuItem item_1 = new MenuItem(popup, SWT.CASCADE);",
            "      item_1.setText('Item 1');",
            "      {",
            "        Menu menu_1 = new Menu(item_1);",
            "        item_1.setMenu(menu_1);",
            "        {",
            "          MenuItem item_2 = new MenuItem(menu_1, SWT.NONE);",
            "          item_2.setText('Item 2');",
            "        }",
            "      }",
            "    }",
            "  }",
            "}");
    MenuInfo popupInfo = shellInfo.getChildren(MenuInfo.class).get(0);
    MenuItemInfo item_1 = popupInfo.getChildrenItems().get(0);
    MenuInfo menu_1 = item_1.getChildren(MenuInfo.class).get(0);
    MenuItemInfo item_2 = menu_1.getChildrenItems().get(0);
    // prepare EditPart's
    EditPart popupPart = canvas.getEditPart(popupInfo);
    // begin creating MenuItem
    loadCreationTool("org.eclipse.swt.widgets.MenuItem");
    // move on "popup": drop-down appears
    GraphicalEditPart itemPart_1;
    {
      canvas.moveTo(popupPart);
      menuTester.assertMenuTargetFeedback(popupPart);
      itemPart_1 = canvas.getEditPart(item_1);
    }
    // move on upper part of "item_1": add before feedback
    {
      canvas.moveTo(itemPart_1, 1, 1);
      menuTester.assertMenuLineFeedback(itemPart_1, IPositionConstants.TOP);
      canvas.assertNullEditPart(menu_1);
    }
    // move on lower part of "item_1": add after feedback
    {
      canvas.moveTo(itemPart_1, 1, -1);
      menuTester.assertMenuLineFeedback(itemPart_1, IPositionConstants.BOTTOM);
      canvas.assertNullEditPart(menu_1);
    }
    // move on center of "item_1": target feedback and menu_1/item_2 EditPart's
    EditPart menuPart_1;
    EditPart itemPart_2;
    {
      canvas.moveTo(itemPart_1, 1, itemPart_1.getFigure().getSize().height / 2);
      menuTester.assertMenuTargetFeedback(itemPart_1);
      // prepare EditPart's
      menuPart_1 = canvas.getEditPart(menu_1);
      itemPart_2 = canvas.getEditPart(item_2);
      assertNotNull(menuPart_1);
      assertNotNull(itemPart_2);
    }
  }

  /**
   * Add new item before existing "item".
   */
  public void test_CREATE_bar_newItem_before() throws Exception {
    CompositeInfo shellInfo =
        openComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    Menu bar = new Menu(this, SWT.BAR);",
            "    setMenuBar(bar);",
            "    {",
            "      MenuItem item_1 = new MenuItem(bar, SWT.NONE);",
            "      item_1.setText('Item 1');",
            "    }",
            "  }",
            "}");
    MenuInfo barInfo = shellInfo.getChildren(MenuInfo.class).get(0);
    MenuItemInfo item = barInfo.getChildrenItems().get(0);
    // begin creating MenuItem
    loadCreationTool("org.eclipse.swt.widgets.MenuItem");
    // move before "item": add before feedback
    {
      canvas.moveTo(item, 1, 1);
      menuTester.assertMenuLineFeedback(item, IPositionConstants.LEFT);
    }
    // do click
    canvas.click();
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    Menu bar = new Menu(this, SWT.BAR);",
        "    setMenuBar(bar);",
        "    {",
        "      MenuItem menuItem = new MenuItem(bar, SWT.NONE);",
        "      menuItem.setText('New Item');",
        "    }",
        "    {",
        "      MenuItem item_1 = new MenuItem(bar, SWT.NONE);",
        "      item_1.setText('Item 1');",
        "    }",
        "  }",
        "}");
  }

  /**
   * Add new item after existing "item".
   */
  public void test_CREATE_bar_newItem_after() throws Exception {
    CompositeInfo shellInfo =
        openComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    Menu bar = new Menu(this, SWT.BAR);",
            "    setMenuBar(bar);",
            "    {",
            "      MenuItem item_1 = new MenuItem(bar, SWT.NONE);",
            "      item_1.setText('Item 1');",
            "    }",
            "  }",
            "}");
    MenuInfo barInfo = shellInfo.getChildren(MenuInfo.class).get(0);
    MenuItemInfo item = barInfo.getChildrenItems().get(0);
    // prepare EditPart's
    EditPart itemPart = canvas.getEditPart(item);
    // begin creating MenuItem
    loadCreationTool("org.eclipse.swt.widgets.MenuItem");
    // move after "item": add after feedback
    {
      canvas.moveTo(itemPart, -1, 1);
      menuTester.assertMenuLineFeedback(itemPart, IPositionConstants.RIGHT);
    }
    // do click
    canvas.click();
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    Menu bar = new Menu(this, SWT.BAR);",
        "    setMenuBar(bar);",
        "    {",
        "      MenuItem item_1 = new MenuItem(bar, SWT.NONE);",
        "      item_1.setText('Item 1');",
        "    }",
        "    {",
        "      MenuItem menuItem = new MenuItem(bar, SWT.NONE);",
        "      menuItem.setText('New Item');",
        "    }",
        "  }",
        "}");
  }

  /**
   * Add new "item" into bar "subMenu"
   */
  public void test_CREATE_bar_submenu() throws Exception {
    CompositeInfo shellInfo =
        openComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    Menu bar = new Menu(this, SWT.BAR);",
            "    setMenuBar(bar);",
            "    {",
            "      MenuItem item = new MenuItem(bar, SWT.CASCADE);",
            "      item.setText('Item');",
            "      {",
            "        Menu menu = new Menu(item);",
            "        item.setMenu(menu);",
            "      }",
            "    }",
            "  }",
            "}");
    MenuInfo barInfo = shellInfo.getChildren(MenuInfo.class).get(0);
    MenuItemInfo itemInfo = barInfo.getChildrenItems().get(0);
    MenuInfo menuInfo = itemInfo.getChildren(MenuInfo.class).get(0);
    // prepare EditPart's
    GraphicalEditPart itemPart = canvas.getEditPart(itemInfo);
    // begin creating MenuItem
    JavaInfo newItemInfo = loadCreationTool("org.eclipse.swt.widgets.MenuItem");
    // initially "menu" is not visible
    canvas.assertNullEditPart(menuInfo);
    // move before "item": add before feedback, "menu" still not visible
    {
      canvas.moveTo(itemPart, 1, 1);
      menuTester.assertMenuLineFeedback(itemPart, IPositionConstants.LEFT);
      canvas.assertNullEditPart(menuInfo);
    }
    // move on middle part of "item": target feedback and "menu" becomes visible
    EditPart menuPart;
    {
      canvas.moveTo(itemPart, itemPart.getFigure().getSize().width / 2, 1);
      menuTester.assertMenuTargetFeedback(itemPart);
      menuPart = canvas.getEditPart(menuInfo);
      assertNotNull(menuPart);
    }
    // move on "menu": no any menu feedbacks
    {
      canvas.moveTo(menuPart, 1, 1);
    }
    // do click
    canvas.click();
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    Menu bar = new Menu(this, SWT.BAR);",
        "    setMenuBar(bar);",
        "    {",
        "      MenuItem item = new MenuItem(bar, SWT.CASCADE);",
        "      item.setText('Item');",
        "      {",
        "        Menu menu = new Menu(item);",
        "        item.setMenu(menu);",
        "        {",
        "          MenuItem menuItem = new MenuItem(menu, SWT.NONE);",
        "          menuItem.setText('New Item');",
        "        }",
        "      }",
        "    }",
        "  }",
        "}");
    // "menu" still visible and "newItem" has EditPart and selected
    {
      assertSame(menuPart, canvas.getEditPart(menuInfo));
      EditPart newItemPart = canvas.getEditPart(newItemInfo);
      assertSame(menuPart, newItemPart.getParent());
      canvas.assertPrimarySelected(newItemPart);
    }
  }

  /**
   * Menu bar ignore non-menu widgets.
   */
  public void test_CREATE_bar_notItem() throws Exception {
    CompositeInfo shellInfo =
        openComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    Menu bar = new Menu(this, SWT.BAR);",
            "    setMenuBar(bar);",
            "    {",
            "      MenuItem item_1 = new MenuItem(bar, SWT.NONE);",
            "      item_1.setText('Item 1');",
            "    }",
            "  }",
            "}");
    MenuInfo barInfo = shellInfo.getChildren(MenuInfo.class).get(0);
    // prepare EditPart's
    EditPart barPart = canvas.getEditPart(barInfo);
    // load non-menu widget
    loadCreationTool("org.eclipse.swt.widgets.Button");
    // move on bar, no menu feedback
    canvas.moveTo(barPart, 1, 1);
    menuTester.assertMenuNoFeedbacks();
  }

  /**
   * Paste copy of "item" on "bar".
   */
  public void test_PASTE_item() throws Exception {
    CompositeInfo shellInfo =
        openComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    Menu bar = new Menu(this, SWT.BAR);",
            "    setMenuBar(bar);",
            "    {",
            "      MenuItem item = new MenuItem(bar, SWT.NONE);",
            "      item.setText('item');",
            "    }",
            "  }",
            "}");
    MenuInfo barInfo = shellInfo.getChildren(MenuInfo.class).get(0);
    MenuItemInfo itemInfo = barInfo.getChildrenItems().get(0);
    // prepare parts
    GraphicalEditPart itemPart = canvas.getEditPart(itemInfo);
    // load "paste" tool
    {
      JavaInfoMemento memento = JavaInfoMemento.createMemento(itemInfo);
      PasteTool pasteTool = new PasteTool(ImmutableList.of(memento));
      m_viewerCanvas.getEditDomain().setActiveTool(pasteTool);
    }
    // move before "item": add before feedback
    {
      canvas.moveTo(itemPart, 1, 1);
      menuTester.assertMenuLineFeedback(itemPart, IPositionConstants.LEFT);
    }
    // do click
    canvas.click();
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    Menu bar = new Menu(this, SWT.BAR);",
        "    setMenuBar(bar);",
        "    {",
        "      MenuItem menuItem = new MenuItem(bar, SWT.NONE);",
        "      menuItem.setText('item');",
        "    }",
        "    {",
        "      MenuItem item = new MenuItem(bar, SWT.NONE);",
        "      item.setText('item');",
        "    }",
        "  }",
        "}");
  }

  /**
   * Try to paste non-menu widget.
   */
  public void test_PASTE_notItem() throws Exception {
    CompositeInfo shellInfo =
        openComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    Menu bar = new Menu(this, SWT.BAR);",
            "    setMenuBar(bar);",
            "    setLayout(new RowLayout());",
            "    Button button = new Button(this, SWT.NONE);",
            "  }",
            "}");
    MenuInfo barInfo = shellInfo.getChildren(MenuInfo.class).get(0);
    ControlInfo buttonInfo = shellInfo.getChildrenControls().get(0);
    // prepare parts
    EditPart barPart = canvas.getEditPart(barInfo);
    // load "paste" tool
    {
      JavaInfoMemento memento = JavaInfoMemento.createMemento(buttonInfo);
      PasteTool pasteTool = new PasteTool(ImmutableList.of(memento));
      m_viewerCanvas.getEditDomain().setActiveTool(pasteTool);
    }
    // move on "bar"
    {
      canvas.moveTo(barPart, 1, 1);
      menuTester.assertMenuNoFeedbacks();
    }
    // do click
    canvas.click();
    // "paste" ignored by menu, so passed to "shell" and handled by RowLayout
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    Menu bar = new Menu(this, SWT.BAR);",
        "    setMenuBar(bar);",
        "    setLayout(new RowLayout());",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "    }",
        "    Button button = new Button(this, SWT.NONE);",
        "  }",
        "}");
  }

  /**
   * When we try to paste non-menu widget, "popup" even does not show drop-down.
   */
  public void test_PASTE_notItem2() throws Exception {
    CompositeInfo shellInfo =
        openComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    Menu popup = new Menu(this);",
            "    setMenu(popup);",
            "    setLayout(new RowLayout());",
            "    Button button = new Button(this, SWT.NONE);",
            "  }",
            "}");
    MenuInfo popupInfo = shellInfo.getChildren(MenuInfo.class).get(0);
    ControlInfo buttonInfo = shellInfo.getChildrenControls().get(0);
    // prepare parts
    EditPart popupPart = canvas.getEditPart(popupInfo);
    // load "paste" tool
    {
      JavaInfoMemento memento = JavaInfoMemento.createMemento(buttonInfo);
      PasteTool pasteTool = new PasteTool(ImmutableList.of(memento));
      m_viewerCanvas.getEditDomain().setActiveTool(pasteTool);
    }
    // initially "popup" has no drop-down
    assertEquals(0, popupPart.getChildren().size());
    // move on "popup": no menu feedback, no drop-down
    {
      canvas.moveTo(popupPart, 1, 1);
      menuTester.assertMenuNoFeedbacks();
      assertEquals(0, popupPart.getChildren().size());
    }
    // do click
    canvas.click();
    // "paste" ignored by "popup", so passed to "shell" and handled by RowLayout
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    Menu popup = new Menu(this);",
        "    setMenu(popup);",
        "    setLayout(new RowLayout());",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "    }",
        "    Button button = new Button(this, SWT.NONE);",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // MOVE, ADD
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Move one item before other.
   */
  public void test_MOVE_item() throws Exception {
    CompositeInfo shellInfo =
        openComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    Menu bar = new Menu(this, SWT.BAR);",
            "    setMenuBar(bar);",
            "    {",
            "      MenuItem menuItem = new MenuItem(bar, SWT.NONE);",
            "      menuItem.setText('Item 1');",
            "    }",
            "    {",
            "      MenuItem menuItem = new MenuItem(bar, SWT.NONE);",
            "      menuItem.setText('Item 2');",
            "    }",
            "  }",
            "}");
    MenuInfo barInfo = shellInfo.getChildren(MenuInfo.class).get(0);
    MenuItemInfo item_1 = barInfo.getChildrenItems().get(0);
    MenuItemInfo item_2 = barInfo.getChildrenItems().get(1);
    // prepare EditPart's
    EditPart itemPart_1 = canvas.getEditPart(item_1);
    EditPart itemPart_2 = canvas.getEditPart(item_2);
    // move "item_2" before "item_1"
    {
      canvas.beginDrag(itemPart_2).dragTo(itemPart_1);
      menuTester.assertFeedback_selection_line(itemPart_2, itemPart_1, IPositionConstants.LEFT);
      // again just selection feedback
      canvas.endDrag();
      menuTester.assertFeedback_selection(itemPart_2);
    }
    // check result
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    Menu bar = new Menu(this, SWT.BAR);",
        "    setMenuBar(bar);",
        "    {",
        "      MenuItem menuItem = new MenuItem(bar, SWT.NONE);",
        "      menuItem.setText('Item 2');",
        "    }",
        "    {",
        "      MenuItem menuItem = new MenuItem(bar, SWT.NONE);",
        "      menuItem.setText('Item 1');",
        "    }",
        "  }",
        "}");
    // "item_2" still same and selected
    {
      assertSame(itemPart_2, canvas.getEditPart(item_2));
      assertEquals(EditPart.SELECTED_PRIMARY, itemPart_2.getSelected());
    }
  }

  /**
   * Move item into new menu.
   */
  public void test_ADD_item() throws Exception {
    CompositeInfo shellInfo =
        openComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    Menu bar = new Menu(this, SWT.BAR);",
            "    setMenuBar(bar);",
            "    {",
            "      MenuItem item_1 = new MenuItem(bar, SWT.CASCADE);",
            "      item_1.setText('Item 1');",
            "      {",
            "        Menu menu_1 = new Menu(item_1);",
            "        item_1.setMenu(menu_1);",
            "        {",
            "          MenuItem item_2 = new MenuItem(menu_1, SWT.NONE);",
            "          item_2.setText('Item 2');",
            "        }",
            "      }",
            "    }",
            "  }",
            "}");
    MenuInfo barInfo = shellInfo.getChildren(MenuInfo.class).get(0);
    MenuItemInfo item_1 = barInfo.getChildrenItems().get(0);
    MenuInfo menu_1 = item_1.getChildren(MenuInfo.class).get(0);
    MenuItemInfo item_2 = menu_1.getChildrenItems().get(0);
    // prepare EditPart's
    EditPart itemPart_1 = canvas.getEditPart(item_1);
    // select "item_2" in tree
    EditPart itemPart_2;
    {
      tree.select(item_2);
      itemPart_2 = canvas.getEditPart(item_2);
      assertEquals(EditPart.SELECTED_PRIMARY, itemPart_2.getSelected());
    }
    // move "item_2" before "item_1" on "bar"
    {
      canvas.beginDrag(itemPart_2).dragTo(itemPart_1);
      menuTester.assertFeedback_selection_line(itemPart_2, itemPart_1, IPositionConstants.LEFT);
      // again just selection feedback
      canvas.endDrag();
      itemPart_2 = canvas.getEditPart(item_2);
      menuTester.assertFeedback_selection(itemPart_2);
    }
    // check result
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    Menu bar = new Menu(this, SWT.BAR);",
        "    setMenuBar(bar);",
        "    {",
        "      MenuItem item_2 = new MenuItem(bar, SWT.NONE);",
        "      item_2.setText('Item 2');",
        "    }",
        "    {",
        "      MenuItem item_1 = new MenuItem(bar, SWT.CASCADE);",
        "      item_1.setText('Item 1');",
        "      {",
        "        Menu menu_1 = new Menu(item_1);",
        "        item_1.setMenu(menu_1);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  /**
   * Move item into its own menu is disabled.
   */
  public void test_ADD_badOnOwnMenu() throws Exception {
    CompositeInfo shellInfo =
        openComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    Menu bar = new Menu(this, SWT.BAR);",
            "    setMenuBar(bar);",
            "    {",
            "      MenuItem item = new MenuItem(bar, SWT.CASCADE);",
            "      item.setText('Item');",
            "      {",
            "        Menu menu = new Menu(item);",
            "        item.setMenu(menu);",
            "      }",
            "    }",
            "  }",
            "}");
    String source = m_lastEditor.getSource();
    MenuInfo barInfo = shellInfo.getChildren(MenuInfo.class).get(0);
    MenuItemInfo item = barInfo.getChildrenItems().get(0);
    MenuInfo menu = item.getChildren(MenuInfo.class).get(0);
    // select "item" in tree: "menu" has EditPart
    tree.select(item);
    // try to move "item" on "menu"
    {
      canvas.beginDrag(item).dragTo(menu);
      // bad move, so no line feedback, only selection
      menuTester.assertFeedback_selection(item);
      // again just selection feedback
      canvas.endDrag();
      menuTester.assertFeedback_selection(item);
    }
    // source not changed
    assertEditor(source, m_lastEditor);
  }

  /**
   * Movement of non-menu object on menu is disabled.
   */
  public void test_ADD_badNotMenu() throws Exception {
    CompositeInfo shellInfo =
        openComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    Menu bar = new Menu(this, SWT.BAR);",
            "    setMenuBar(bar);",
            "    setLayout(new RowLayout());",
            "    Button button = new Button(this, SWT.NONE);",
            "  }",
            "}");
    String source = m_lastEditor.getSource();
    MenuInfo barInfo = shellInfo.getChildren(MenuInfo.class).get(0);
    ControlInfo buttonInfo = shellInfo.getChildrenControls().get(0);
    // try to move "button" on "bar"
    {
      canvas.beginDrag(buttonInfo).dragTo(barInfo);
      // bad move, so no menu feedbacks
      menuTester.assertMenuNoFeedbacks();
      canvas.endDrag();
      menuTester.assertMenuNoFeedbacks();
    }
    // source not changed
    assertEditor(source, m_lastEditor);
  }

  /**
   * Move "item" from "popup" on drop-down of "bar".
   */
  public void test_ADD_fromPopup_onBar() throws Exception {
    CompositeInfo shellInfo =
        openComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    {",
            "      Menu bar = new Menu(this, SWT.BAR);",
            "      setMenuBar(bar);",
            "      new MenuItem(bar, SWT.NONE).setText('Long item to shift next one');",
            "      {",
            "        MenuItem item = new MenuItem(bar, SWT.CASCADE);",
            "        item.setText('Item');",
            "        {",
            "          Menu menu = new Menu(item);",
            "          item.setMenu(menu);",
            "        }",
            "      }",
            "    }",
            "    {",
            "      Menu popup = new Menu(this);",
            "      setMenu(popup);",
            "      {",
            "        MenuItem item_2 = new MenuItem(popup, SWT.NONE);",
            "        item_2.setText('Item 2');",
            "      }",
            "    }",
            "  }",
            "}");
    MenuInfo barInfo = shellInfo.getChildren(MenuInfo.class).get(0);
    MenuInfo popupInfo = shellInfo.getChildren(MenuInfo.class).get(1);
    MenuItemInfo itemInfo = barInfo.getChildrenItems().get(1);
    MenuInfo menuInfo = itemInfo.getChildren(MenuInfo.class).get(0);
    MenuItemInfo itemInfo_2 = popupInfo.getChildrenItems().get(0);
    // prepare EditPart's
    EditPart popupPart = canvas.getEditPart(popupInfo);
    GraphicalEditPart itemPart = canvas.getEditPart(itemInfo);
    // select "item_2" in tree: "popup" shows drop-down
    EditPart itemPart_2;
    EditPart popupMenuPart;
    {
      tree.select(itemInfo_2);
      popupMenuPart = popupPart.getChildren().get(0);
      assertNotNull(popupMenuPart);
      itemPart_2 = canvas.getEditPart(itemInfo_2);
    }
    // move "item_2" on "menu"
    EditPart menuPart;
    {
      canvas.beginDrag(itemPart_2);
      // "menu" is not visible yet
      canvas.assertNullEditPart(menuInfo);
      {
        // move on "item"
        canvas.dragTo(itemPart, itemPart.getFigure().getSize().width / 2, 0);
        menuTester.assertFeedback_selection_target(itemPart_2, itemPart);
      }
      // so "menu" shown
      menuPart = canvas.getEditPart(menuInfo);
      assertNotNull(menuPart);
      {
        // and move on "menu"
        canvas.target(shellInfo).outY(-5).drag();
        canvas.target(menuInfo).inX(5).drag();
        canvas.target(menuInfo).in(5, 5).drag();
        menuTester.assertFeedback_selection_emptyFlow(itemPart_2, menuPart, false);
      }
      // end drag, "item_2" moved on "menu"
      canvas.endDrag();
    }
    // check source
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    {",
        "      Menu bar = new Menu(this, SWT.BAR);",
        "      setMenuBar(bar);",
        "      new MenuItem(bar, SWT.NONE).setText(\"Long item to shift next one\");",
        "      {",
        "        MenuItem item = new MenuItem(bar, SWT.CASCADE);",
        "        item.setText(\"Item\");",
        "        {",
        "          Menu menu = new Menu(item);",
        "          item.setMenu(menu);",
        "          {",
        "            MenuItem item_2 = new MenuItem(menu, SWT.NONE);",
        "            item_2.setText(\"Item 2\");",
        "          }",
        "        }",
        "      }",
        "    }",
        "    {",
        "      Menu popup = new Menu(this);",
        "      setMenu(popup);",
        "    }",
        "  }",
        "}");
    {
      // "menu" still visible
      assertSame(menuPart, canvas.getEditPart(menuInfo));
      // "popup" closed
      assertEquals(0, popupPart.getChildren().size());
      // "item_2" has new part and selected
      EditPart newItemPart_2 = canvas.getEditPart(itemInfo_2);
      assertNotSame(newItemPart_2, itemPart_2);
      assertEquals(EditPart.SELECTED_PRIMARY, newItemPart_2.getSelected());
    }
  }

  /**
   * When we move {@link CreationTool} over "item_1" with drop-down, then move on "item_2", first
   * drop-down should be closed. So, only one (well, two, including selecting) menu hierarchy can be
   * visible at same time.
   */
  public void test_showOnlyOneDropDownOnCanvas() throws Exception {
    CompositeInfo shellInfo =
        openComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    Menu bar = new Menu(this, SWT.BAR);",
            "    setMenuBar(bar);",
            "    {",
            "      MenuItem item_1 = new MenuItem(bar, SWT.CASCADE);",
            "      item_1.setText('Item 1');",
            "      {",
            "        Menu menu_1 = new Menu(item_1);",
            "        item_1.setMenu(menu_1);",
            "      }",
            "    }",
            "    {",
            "      MenuItem item_2 = new MenuItem(bar, SWT.CASCADE);",
            "      item_2.setText('Item 2');",
            "    }",
            "  }",
            "}");
    MenuInfo barInfo = shellInfo.getChildren(MenuInfo.class).get(0);
    MenuItemInfo itemInfo_1 = barInfo.getChildrenItems().get(0);
    MenuItemInfo itemInfo_2 = barInfo.getChildrenItems().get(1);
    MenuInfo menuInfo_1 = itemInfo_1.getSubMenu();
    // prepare EditPart's
    GraphicalEditPart shellPart = canvas.getEditPart(shellInfo);
    GraphicalEditPart itemPart_1 = canvas.getEditPart(itemInfo_1);
    GraphicalEditPart itemPart_2 = canvas.getEditPart(itemInfo_2);
    // begin creating MenuItem
    loadCreationTool("org.eclipse.swt.widgets.MenuItem");
    // initially "menu_1" is not visible
    canvas.assertNullEditPart(menuInfo_1);
    // move on center of "item_1": so "menu_1" becomes visible
    GraphicalEditPart menuPart_1;
    {
      canvas.moveTo(itemPart_1, itemPart_1.getFigure().getSize().width / 2, 0);
      menuPart_1 = canvas.getEditPart(menuInfo_1);
      assertNotNull(menuPart_1);
    }
    // move on "menu_1" and then on "shell": "menu_1" should be still visible
    {
      canvas.moveTo(menuPart_1);
      assertSame(menuPart_1, canvas.getEditPart(menuInfo_1));
      canvas.moveTo(shellPart, 100, 100);
      assertSame(menuPart_1, canvas.getEditPart(menuInfo_1));
    }
    // move on "item_2": "menu_1" should be closed
    {
      canvas.moveTo(itemPart_2, itemPart_2.getFigure().getSize().width / 2, 0);
      canvas.assertNullEditPart(menuInfo_1);
    }
  }
}
