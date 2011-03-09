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

import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.support.MenuSupport;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.swt.SWT;

import java.lang.reflect.Constructor;

/**
 * Tests for {@link MenuSupport}.
 * 
 * @author mitin_aa
 */
public class MenuSupportTest extends RcpModelTest {
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
  private Object m_shellObject;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    // parse for context
    CompositeInfo shellInfo =
        parseComposite(
            "// filler filler filler",
            "public class Test extends Shell {",
            "  public Test() {",
            "  }",
            "}");
    shellInfo.refresh();
    m_shellObject = shellInfo.getObject();
  }

  @Override
  protected void tearDown() throws Exception {
    m_shellObject = null;
    super.tearDown();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Classes
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link MenuSupport#getMenuClass()}.
   */
  public void test_getMenuClass() throws Exception {
    Class<?> menuClass = MenuSupport.getMenuClass();
    assertNotNull(menuClass);
    assertEquals("org.eclipse.swt.widgets.Menu", menuClass.getName());
  }

  /**
   * Test for {@link MenuSupport#isMenuClass(Class)}.
   */
  public void test_isMenuClass() throws Exception {
    assertFalse(MenuSupport.isMenuClass(Object.class));
    assertTrue(MenuSupport.isMenuClass(MenuSupport.getMenuClass()));
  }

  /**
   * Test for {@link MenuSupport#getMenuItemClass()}.
   */
  public void test_getMenuItemClass() throws Exception {
    Class<?> menuItemClass = MenuSupport.getMenuItemClass();
    assertNotNull(menuItemClass);
    assertEquals("org.eclipse.swt.widgets.MenuItem", menuItemClass.getName());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Menu
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link MenuSupport#isMenu(Object)}.
   */
  public void test_isMenu() throws Exception {
    assertFalse(MenuSupport.isMenu(this));
    // real Menu
    Object menuBar =
        ReflectionUtils.getConstructorBySignature(
            MenuSupport.getMenuClass(),
            "<init>(org.eclipse.swt.widgets.Decorations,int)").newInstance(m_shellObject, SWT.BAR);
    assertTrue(MenuSupport.isMenu(menuBar));
  }

  /**
   * Test for {@link MenuSupport#getMenu(Object)}.
   */
  public void test_getMenu() throws Exception {
    Object menuBar =
        ReflectionUtils.getConstructorBySignature(
            MenuSupport.getMenuClass(),
            "<init>(org.eclipse.swt.widgets.Decorations,int)").newInstance(m_shellObject, SWT.BAR);
    Object menuItem =
        ReflectionUtils.getConstructorBySignature(
            MenuSupport.getMenuItemClass(),
            "<init>(org.eclipse.swt.widgets.Menu,int)").newInstance(menuBar, SWT.CASCADE);
    // set sub-menu
    Object subMenu;
    {
      Constructor<?> constructor =
          ReflectionUtils.getConstructor(MenuSupport.getMenuClass(), MenuSupport.getMenuItemClass());
      subMenu = constructor.newInstance(menuItem);
      ReflectionUtils.invokeMethod2(menuItem, "setMenu", MenuSupport.getMenuClass(), subMenu);
    }
    // check getMenu()
    assertSame(subMenu, MenuSupport.getMenu(menuItem));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Items
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link MenuSupport#getItems(Object)}.
   */
  public void test_getItems() throws Exception {
    Object menuBar =
        ReflectionUtils.getConstructorBySignature(
            MenuSupport.getMenuClass(),
            "<init>(org.eclipse.swt.widgets.Decorations,int)").newInstance(m_shellObject, SWT.BAR);
    Object menuItem_1 =
        ReflectionUtils.getConstructorBySignature(
            MenuSupport.getMenuItemClass(),
            "<init>(org.eclipse.swt.widgets.Menu,int)").newInstance(menuBar, SWT.CASCADE);
    Object menuItem_2 =
        ReflectionUtils.getConstructorBySignature(
            MenuSupport.getMenuItemClass(),
            "<init>(org.eclipse.swt.widgets.Menu,int)").newInstance(menuBar, SWT.CASCADE);
    // check getItems()
    Object[] items = MenuSupport.getItems(menuBar);
    assertEquals(2, items.length);
    assertSame(menuItem_1, items[0]);
    assertSame(menuItem_2, items[1]);
  }

  /**
   * Test for {@link MenuSupport#addPlaceholder(Object)}.
   */
  public void test_addPlaceHolder() throws Exception {
    Object menuBar =
        ReflectionUtils.getConstructorBySignature(
            MenuSupport.getMenuClass(),
            "<init>(org.eclipse.swt.widgets.Decorations,int)").newInstance(m_shellObject, SWT.BAR);
    // initially no items
    assertEquals(0, MenuSupport.getItems(menuBar).length);
    // add placeholder item
    MenuSupport.addPlaceholder(menuBar);
    assertEquals(1, MenuSupport.getItems(menuBar).length);
  }
}