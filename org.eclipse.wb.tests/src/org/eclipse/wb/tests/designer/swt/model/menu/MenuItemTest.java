/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.designer.swt.model.menu;

import org.eclipse.wb.core.editor.IDesignPageSite;
import org.eclipse.wb.internal.core.editor.DesignPageSite;
import org.eclipse.wb.internal.core.model.menu.IMenuItemInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuPolicy;
import org.eclipse.wb.internal.core.model.menu.MenuObjectInfoUtils;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.event.EventsProperty;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.menu.MenuInfo;
import org.eclipse.wb.internal.swt.model.widgets.menu.MenuItemInfo;
import org.eclipse.wb.internal.swt.model.widgets.menu.MenuItemStylePresentation;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.swt.SWT;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.mockito.ArgumentMatchers;

import java.util.List;

/**
 * Tests for {@link MenuItemInfo}.
 *
 * @author mitin_aa
 */
public class MenuItemTest extends RcpModelTest {
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
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    Menu menu = new Menu(this, SWT.BAR);",
						"    setMenuBar(menu);",
						"    {",
						"      MenuItem item_1 = new MenuItem(menu, SWT.NONE);",
						"    }",
						"    {",
						"      MenuItem item_2 = new MenuItem(menu, SWT.CASCADE);",
						"      {",
						"        Menu subMenu = new Menu(item_2);",
						"        item_2.setMenu(subMenu);",
						"      }",
						"    }",
						"  }",
						"}");
		shell.refresh();
		// prepare models
		MenuInfo menuBar = shell.getChildren(MenuInfo.class).get(0);
		assertEquals(2, menuBar.getChildrenItems().size());
		MenuItemInfo menuItem_1 = menuBar.getChildrenItems().get(0);
		MenuItemInfo menuItem_2 = menuBar.getChildrenItems().get(1);
		// "item_1" has no subMenu
		assertNull(menuItem_1.getSubMenu());
		// "item_2" has subMenu
		assertNotNull(menuItem_2.getSubMenu());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// MenuItemStylePresentation
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link MenuItemStylePresentation}.
	 */
	public void test_MenuItemStylePresentation() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    Menu menu = new Menu(this, SWT.BAR);",
						"    setMenuBar(menu);",
						"    {",
						"      MenuItem item = new MenuItem(menu, SWT.NONE);",
						"    }",
						"    {",
						"      MenuItem item = new MenuItem(menu, SWT.PUSH);",
						"    }",
						"    {",
						"      MenuItem item = new MenuItem(menu, SWT.CHECK);",
						"    }",
						"    {",
						"      MenuItem item = new MenuItem(menu, SWT.RADIO);",
						"    }",
						"    {",
						"      MenuItem item = new MenuItem(menu, SWT.SEPARATOR);",
						"    }",
						"  }",
						"}");
		shell.refresh();
		// prepare models
		MenuInfo menuBar = shell.getChildren(MenuInfo.class).get(0);
		List<MenuItemInfo> menuItems = menuBar.getChildrenItems();
		MenuItemInfo menuItemDefault = menuItems.get(0);
		MenuItemInfo menuItemPush = menuItems.get(1);
		MenuItemInfo menuItemCheck = menuItems.get(2);
		MenuItemInfo menuItemRadio = menuItems.get(3);
		MenuItemInfo menuItemSeparator = menuItems.get(4);
		// test icons
		assertSame(
				menuItemDefault.getPresentation().getIcon(),
				menuItemPush.getPresentation().getIcon());
		assertNotSame(
				menuItemDefault.getPresentation().getIcon(),
				menuItemCheck.getPresentation().getIcon());
		assertNotSame(
				menuItemDefault.getPresentation().getIcon(),
				menuItemRadio.getPresentation().getIcon());
		assertNotSame(
				menuItemDefault.getPresentation().getIcon(),
				menuItemSeparator.getPresentation().getIcon());
		// test text
		assertEquals("item", menuItemDefault.getPresentation().getText());
		assertEquals("<separator>", menuItemSeparator.getPresentation().getText());
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
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    Menu bar = new Menu(this, SWT.BAR);",
						"    setMenuBar(bar);",
						"    {",
						"      MenuItem item = new MenuItem(bar, SWT.CASCADE);",
						"      {",
						"        Menu menu = new Menu(item);",
						"        item.setMenu(menu);",
						"      }",
						"    }",
						"  }",
						"}");
		shell.refresh();
		// prepare models
		MenuInfo barInfo = shell.getChildren(MenuInfo.class).get(0);
		MenuItemInfo itemInfo = barInfo.getChildrenItems().get(0);
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
			assertSame(itemInfo.getImage(), itemObject.getImageDescriptor());
			assertSame(itemInfo.getBounds(), itemObject.getBounds());
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Style changing XXX
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Set {@link MenuItemInfo} style to {@link SWT#NONE}.
	 */
	public void test_setStyle_toNONE() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    Menu menu = new Menu(this, SWT.BAR);",
						"    setMenuBar(menu);",
						"    {",
						"      MenuItem item = new MenuItem(menu, SWT.CHECK);",
						"    }",
						"  }",
						"}");
		shell.refresh();
		// prepare models
		MenuInfo menuBar = shell.getChildren(MenuInfo.class).get(0);
		MenuItemInfo menuItem = menuBar.getChildrenItems().get(0);
		// set style to SWT.CASCADE
		GenericProperty styleProperty = (GenericProperty) menuItem.getPropertyByTitle("Style");
		styleProperty.setExpression(null, Property.UNKNOWN_VALUE);
		// test results
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    Menu menu = new Menu(this, SWT.BAR);",
				"    setMenuBar(menu);",
				"    {",
				"      MenuItem item = new MenuItem(menu, SWT.NONE);",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Set {@link MenuItemInfo} style to {@link SWT#CASCADE}. A sub menu should be added.
	 */
	public void test_setStyle_toCascade() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    Menu menu = new Menu(this, SWT.BAR);",
						"    setMenuBar(menu);",
						"    {",
						"      MenuItem item = new MenuItem(menu, SWT.CHECK);",
						"    }",
						"  }",
						"}");
		shell.refresh();
		// prepare models
		MenuInfo menuBar = shell.getChildren(MenuInfo.class).get(0);
		MenuItemInfo menuItem = menuBar.getChildrenItems().get(0);
		// set style to SWT.CASCADE
		GenericProperty styleProperty = (GenericProperty) menuItem.getPropertyByTitle("Style");
		styleProperty.setExpression("org.eclipse.swt.SWT.CASCADE", Property.UNKNOWN_VALUE);
		// test results
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    Menu menu = new Menu(this, SWT.BAR);",
				"    setMenuBar(menu);",
				"    {",
				"      MenuItem item = new MenuItem(menu, SWT.CASCADE);",
				"      {",
				"        Menu menu_1 = new Menu(item);",
				"        item.setMenu(menu_1);",
				"      }",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Set {@link MenuItemInfo} style to anything else except {@link SWT#CASCADE} and
	 * {@link SWT#SEPARATOR}.
	 * <p>
	 * Only the style should changed.
	 */
	public void test_setStyle_noCascade() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    Menu menu = new Menu(this, SWT.BAR);",
						"    setMenuBar(menu);",
						"    {",
						"      MenuItem item = new MenuItem(menu, SWT.RADIO);",
						"    }",
						"  }",
						"}");
		shell.refresh();
		// prepare models
		MenuInfo menuBar = shell.getChildren(MenuInfo.class).get(0);
		MenuItemInfo menuItem = menuBar.getChildrenItems().get(0);
		// set style to SWT.CHECK
		GenericProperty styleProperty = (GenericProperty) menuItem.getPropertyByTitle("Style");
		styleProperty.setExpression("org.eclipse.swt.SWT.CHECK", Property.UNKNOWN_VALUE);
		// test results
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    Menu menu = new Menu(this, SWT.BAR);",
				"    setMenuBar(menu);",
				"    {",
				"      MenuItem item = new MenuItem(menu, SWT.CHECK);",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Set {@link MenuItemInfo} style from {@link SWT#CASCADE} style to anything else.
	 * <p>
	 * Sub menu should be removed.
	 */
	public void test_setStyle_fromCascade() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    Menu menu = new Menu(this, SWT.BAR);",
						"    setMenuBar(menu);",
						"    {",
						"      MenuItem item = new MenuItem(menu, SWT.CASCADE);",
						"      {",
						"        Menu menu_1 = new Menu(item);",
						"        item.setMenu(menu_1);",
						"      }",
						"    }",
						"  }",
						"}");
		shell.refresh();
		// prepare models
		MenuInfo menuBar = shell.getChildren(MenuInfo.class).get(0);
		MenuItemInfo menuItem = menuBar.getChildrenItems().get(0);
		// set style to SWT.RADIO
		GenericProperty styleProperty = (GenericProperty) menuItem.getPropertyByTitle("Style");
		styleProperty.setExpression("org.eclipse.swt.SWT.RADIO", Property.UNKNOWN_VALUE);
		// test results
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    Menu menu = new Menu(this, SWT.BAR);",
				"    setMenuBar(menu);",
				"    {",
				"      MenuItem item = new MenuItem(menu, SWT.RADIO);",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Set {@link MenuItemInfo} style from {@link SWT#CASCADE} style to anything else.
	 * <p>
	 * No sub menu, so nothing to remove.
	 */
	public void test_setStyle_fromCascade_noSubMenu() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    Menu menu = new Menu(this, SWT.BAR);",
						"    setMenuBar(menu);",
						"    {",
						"      MenuItem item = new MenuItem(menu, SWT.CASCADE);",
						"    }",
						"  }",
						"}");
		shell.refresh();
		// prepare models
		MenuInfo menuBar = shell.getChildren(MenuInfo.class).get(0);
		MenuItemInfo menuItem = menuBar.getChildrenItems().get(0);
		// set style to SWT.RADIO
		GenericProperty styleProperty = (GenericProperty) menuItem.getPropertyByTitle("Style");
		styleProperty.setExpression("org.eclipse.swt.SWT.RADIO", Property.UNKNOWN_VALUE);
		// test results
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    Menu menu = new Menu(this, SWT.BAR);",
				"    setMenuBar(menu);",
				"    {",
				"      MenuItem item = new MenuItem(menu, SWT.RADIO);",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Set {@link MenuItemInfo} style to {@link SWT#SEPARATOR}.
	 * <p>
	 * <code>setText()</code> should be removed.
	 */
	public void test_setStyle_toSeparator() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    Menu menu = new Menu(this, SWT.BAR);",
						"    setMenuBar(menu);",
						"    {",
						"      MenuItem item = new MenuItem(menu, SWT.NONE);",
						"      item.setText('text');",
						"    }",
						"  }",
						"}");
		shell.refresh();
		// prepare models
		MenuInfo menuBar = shell.getChildren(MenuInfo.class).get(0);
		MenuItemInfo menuItem = menuBar.getChildrenItems().get(0);
		// set style to SWT.SEPARATOR
		GenericProperty styleProperty = (GenericProperty) menuItem.getPropertyByTitle("Style");
		styleProperty.setExpression("org.eclipse.swt.SWT.SEPARATOR", Property.UNKNOWN_VALUE);
		// test results
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    Menu menu = new Menu(this, SWT.BAR);",
				"    setMenuBar(menu);",
				"    {",
				"      MenuItem item = new MenuItem(menu, SWT.SEPARATOR);",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Set {@link MenuItemInfo} style from {@link SWT#CASCADE} to {@link SWT#SEPARATOR}.
	 * <p>
	 * Sub menu and <code>setText</code> should be removed because the separator doesn't need it.
	 */
	public void test_setStyle_fromCascade_toSeparator() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    Menu menu = new Menu(this, SWT.BAR);",
						"    setMenuBar(menu);",
						"    {",
						"      MenuItem item = new MenuItem(menu, SWT.CASCADE);",
						"      item.setText('Sub Menu Item');",
						"      {",
						"        Menu menu_1 = new Menu(item);",
						"        item.setMenu(menu_1);",
						"      }",
						"    }",
						"  }",
						"}");
		shell.refresh();
		// prepare models
		MenuInfo menuBar = shell.getChildren(MenuInfo.class).get(0);
		MenuItemInfo menuItem = menuBar.getChildrenItems().get(0);
		// set style to SWT.SEPARATOR
		GenericProperty styleProperty = (GenericProperty) menuItem.getPropertyByTitle("Style");
		styleProperty.setExpression("org.eclipse.swt.SWT.SEPARATOR", Property.UNKNOWN_VALUE);
		// test results
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    Menu menu = new Menu(this, SWT.BAR);",
				"    setMenuBar(menu);",
				"    {",
				"      MenuItem item = new MenuItem(menu, SWT.SEPARATOR);",
				"    }",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Adding selection listener
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test adding a selection listener to menu item.
	 */
	public void DISABLE_test_addSelectionListener() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    {",
						"      Menu menu = new Menu(this, SWT.BAR);",
						"      setMenuBar(menu);",
						"      {",
						"        MenuItem item = new MenuItem(menu, SWT.NONE);",
						"      }",
						"    }",
						"  }",
						"}");
		shell.refresh();
		// prepare models
		MenuInfo menuBar = shell.getChildren(MenuInfo.class).get(0);
		MenuItemInfo menuItem = menuBar.getChildrenItems().get(0);
		// set mock for DesignPageSite
		IDesignPageSite pageSite;
		{
			pageSite = mock(IDesignPageSite.class);
			DesignPageSite.Helper.setSite(shell, pageSite);
		}
		// add selection listener
		EventsProperty eventsProperty = (EventsProperty) menuItem.getPropertyByTitle("Events");
		eventsProperty.openStubMethod("selection/widgetSelected");
		waitEventLoop(0);
		// test results
		verify(pageSite).openSourcePosition(ArgumentMatchers.anyInt());
		verifyNoMoreInteractions(pageSite);
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    {",
				"      Menu menu = new Menu(this, SWT.BAR);",
				"      setMenuBar(menu);",
				"      {",
				"        MenuItem item = new MenuItem(menu, SWT.NONE);",
				"        item.addSelectionListener(new SelectionAdapter() {",
				"          @Override",
				"          public void widgetSelected(SelectionEvent e) {",
				"          }",
				"        });",
				"      }",
				"    }",
				"  }",
				"}");
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
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    Menu menu = new Menu(this, SWT.BAR);",
						"    setMenuBar(menu);",
						"    {",
						"      MenuItem item = new MenuItem(menu, SWT.NONE);",
						"    }",
						"  }",
						"}");
		shell.refresh();
		String source = m_lastEditor.getSource();
		// prepare models
		MenuInfo barInfo = shell.getChildren(MenuInfo.class).get(0);
		MenuItemInfo itemInfo = barInfo.getChildren(MenuItemInfo.class).get(0);
		IMenuItemInfo itemObject = MenuObjectInfoUtils.getMenuItemInfo(itemInfo);
		IMenuPolicy itemPolicy = itemObject.getPolicy();
		// no "create"
		{
			MenuInfo newMenuInfo = createJavaInfo("org.eclipse.swt.widgets.Menu");
			assertFalse(itemPolicy.validateCreate(newMenuInfo));
			itemPolicy.commandCreate(newMenuInfo, null);
			assertEditor(source, m_lastEditor);
		}
		// no "paste"
		{
			Object anyObject = new Object();
			assertFalse(itemPolicy.validatePaste(anyObject));
			itemPolicy.commandPaste(anyObject, null);
			assertEditor(source, m_lastEditor);
		}
	}

	/**
	 * {@link MenuInfo} can be moved on {@link MenuItemInfo}.
	 */
	public void test_IMenuItemInfo_move() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    Menu menu = new Menu(this, SWT.BAR);",
						"    setMenuBar(menu);",
						"    {",
						"      MenuItem item_1 = new MenuItem(menu, SWT.CASCADE);",
						"      {",
						"        Menu subMenu = new Menu(item_1);",
						"        item_1.setMenu(subMenu);",
						"        {",
						"          MenuItem subItem = new MenuItem(subMenu, SWT.NONE);",
						"        }",
						"      }",
						"    }",
						"    {",
						"      MenuItem item_2 = new MenuItem(menu, SWT.NONE);",
						"    }",
						"  }",
						"}");
		shell.refresh();
		// prepare models
		MenuInfo barInfo = shell.getChildren(MenuInfo.class).get(0);
		MenuItemInfo itemInfo_1 = barInfo.getChildrenItems().get(0);
		MenuInfo subMenuInfo = itemInfo_1.getSubMenu();
		MenuItemInfo subItemInfo = subMenuInfo.getChildrenItems().get(0);
		MenuItemInfo itemInfo_2 = barInfo.getChildrenItems().get(1);
		// prepare menu objects
		IMenuItemInfo subItemObject = MenuObjectInfoUtils.getMenuItemInfo(subItemInfo);
		IMenuItemInfo itemObject_2 = MenuObjectInfoUtils.getMenuItemInfo(itemInfo_2);
		IMenuPolicy subItemPolicy = subItemObject.getPolicy();
		IMenuPolicy itemPolicy_2 = itemObject_2.getPolicy();
		// can not move just random object
		assertFalse(subItemPolicy.validateMove(new Object()));
		// can not move "subMenu" on its own child "subItem"
		assertFalse(subItemPolicy.validateMove(subMenuInfo));
		// move "subMenu" from "item_1" to "item_2"
		assertTrue(itemPolicy_2.validateMove(subMenuInfo));
		itemPolicy_2.commandMove(subMenuInfo, null);
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    Menu menu = new Menu(this, SWT.BAR);",
				"    setMenuBar(menu);",
				"    {",
				"      MenuItem item_1 = new MenuItem(menu, SWT.NONE);",
				"    }",
				"    {",
				"      MenuItem item_2 = new MenuItem(menu, SWT.CASCADE);",
				"      {",
				"        Menu subMenu = new Menu(item_2);",
				"        item_2.setMenu(subMenu);",
				"        {",
				"          MenuItem subItem = new MenuItem(subMenu, SWT.NONE);",
				"        }",
				"      }",
				"    }",
				"  }",
				"}");
	}
}