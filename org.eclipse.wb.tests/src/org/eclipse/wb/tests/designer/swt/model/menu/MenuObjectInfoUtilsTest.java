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

import org.eclipse.wb.internal.core.model.menu.IMenuInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuItemInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuObjectInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuPopupInfo;
import org.eclipse.wb.internal.core.model.menu.MenuObjectInfoUtils;
import org.eclipse.wb.internal.core.utils.IAdaptable;
import org.eclipse.wb.internal.core.utils.IAdaptableFactory;
import org.eclipse.wb.tests.designer.TestUtils;
import org.eclipse.wb.tests.designer.tests.DesignerTestCase;

import static org.easymock.EasyMock.createStrictControl;
import static org.easymock.EasyMock.expect;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;

import java.util.List;

/**
 * Test for {@link MenuObjectInfoUtils}.
 * 
 * @author scheglov_ke
 */
public class MenuObjectInfoUtilsTest extends DesignerTestCase {
  ////////////////////////////////////////////////////////////////////////////
  //
  // setSelectingObject()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link MenuObjectInfoUtils#setSelectingObject(IMenuObjectInfo)}.
   */
  public void test_setSelectingObject_menuObject() throws Exception {
    IMocksControl control = createStrictControl();
    IMenuObjectInfo object = control.createMock(IMenuObjectInfo.class);
    //
    MenuObjectInfoUtils.setSelectingObject(object);
    assertSame(object, MenuObjectInfoUtils.m_selectingObject);
  }

  /**
   * Test for {@link MenuObjectInfoUtils#setSelectingObject(Object)}.
   */
  public void test_setSelectingObject_pureObject() throws Exception {
    IMocksControl control = EasyMock.createStrictControl();
    IAdaptable object = control.createMock(IAdaptable.class);
    IMenuItemInfo itemInfo = control.createMock(IMenuItemInfo.class);
    // prepare scenario
    expect(object.getAdapter(IMenuObjectInfo.class)).andReturn(itemInfo);
    control.replay();
    // validate
    MenuObjectInfoUtils.setSelectingObject(object);
    assertSame(itemInfo, MenuObjectInfoUtils.m_selectingObject);
    control.verify();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getMenuObjectInfo()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link MenuObjectInfoUtils#getMenuItemInfo(Object)}.<br>
   * Object does not implement {@link IAdaptable}.
   */
  public void test_getMenuObjectInfo_1() throws Exception {
    IMocksControl control = createStrictControl();
    Object object = control.createMock(Object.class);
    // prepare scenario
    control.replay();
    // validate
    assertNull(MenuObjectInfoUtils.getMenuObjectInfo(object));
    control.verify();
  }

  /**
   * Test for {@link MenuObjectInfoUtils#getMenuItemInfo(Object)}.<br>
   * Object implements {@link IAdaptable} and returns {@link IMenuItemInfo}.
   */
  public void test_getMenuObjectInfo_2() throws Exception {
    IMocksControl control = EasyMock.createStrictControl();
    IAdaptable object = control.createMock(IAdaptable.class);
    IMenuItemInfo itemInfo = control.createMock(IMenuItemInfo.class);
    // prepare scenario
    expect(object.getAdapter(IMenuObjectInfo.class)).andReturn(itemInfo);
    control.replay();
    // validate
    assertSame(itemInfo, MenuObjectInfoUtils.getMenuObjectInfo(object));
    control.verify();
  }

  /**
   * Test for {@link MenuObjectInfoUtils#getMenuItemInfo(Object)}.<br>
   * Object implements {@link IAdaptable} and returns {@link IMenuInfo}.
   */
  public void test_getMenuObjectInfo_3() throws Exception {
    IMocksControl control = EasyMock.createStrictControl();
    IAdaptable object = control.createMock(IAdaptable.class);
    IMenuInfo menuInfo = control.createMock(IMenuInfo.class);
    // prepare scenario
    expect(object.getAdapter(IMenuObjectInfo.class)).andReturn(menuInfo);
    control.replay();
    // validate
    assertSame(menuInfo, MenuObjectInfoUtils.getMenuObjectInfo(object));
    control.verify();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getMenuItemInfo()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link MenuObjectInfoUtils#getMenuItemInfo(Object)}.<br>
   * Object does not implement {@link IAdaptable}.
   */
  public void test_getMenuItemInfo_1() throws Exception {
    IMocksControl control = createStrictControl();
    Object object = control.createMock(Object.class);
    // prepare scenario
    control.replay();
    // validate
    assertNull(MenuObjectInfoUtils.getMenuItemInfo(object));
    control.verify();
  }

  /**
   * Test for {@link MenuObjectInfoUtils#getMenuItemInfo(Object)}.<br>
   * Object implements {@link IAdaptable} and returns {@link IMenuItemInfo}.
   */
  public void test_getMenuItemInfo_2() throws Exception {
    IMocksControl control = EasyMock.createStrictControl();
    IAdaptable object = control.createMock(IAdaptable.class);
    IMenuItemInfo itemInfo = control.createMock(IMenuItemInfo.class);
    // prepare scenario
    expect(object.getAdapter(IMenuItemInfo.class)).andReturn(itemInfo);
    control.replay();
    // validate
    assertSame(itemInfo, MenuObjectInfoUtils.getMenuItemInfo(object));
    control.verify();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getMenuItemInfo() with external IAdaptableFactory
  //
  ////////////////////////////////////////////////////////////////////////////
  private static IMenuItemInfo m_tmpItemInfo;

  /**
   * Test for {@link MenuObjectInfoUtils#getMenuItemInfo(Object)}.<br>
   * External {@link IAdaptableFactory}.
   */
  public void test_getMenuItemInfo_3() throws Exception {
    IMocksControl control = EasyMock.createStrictControl();
    IAdaptable object = control.createMock(IAdaptable.class);
    m_tmpItemInfo = control.createMock(IMenuItemInfo.class);
    //
    String pointId = "org.eclipse.wb.core.adaptableFactories";
    {
      String contribution = "  <factory class='" + MyAdaptableFactory.class.getName() + "'/>";
      TestUtils.addDynamicExtension(pointId, contribution);
    }
    try {
      // prepare scenario
      expect(object.getAdapter(IMenuItemInfo.class)).andReturn(null);
      control.replay();
      // validate
      assertSame(m_tmpItemInfo, MenuObjectInfoUtils.getMenuItemInfo(object));
      control.verify();
    } finally {
      TestUtils.removeDynamicExtension(pointId);
    }
  }

  /**
   * Test implementation of {@link IAdaptableFactory}.
   * 
   * @author scheglov_ke
   */
  public static final class MyAdaptableFactory implements IAdaptableFactory {
    public <T> T getAdapter(Object object, Class<T> adapter) {
      assertSame(IMenuItemInfo.class, adapter);
      return adapter.cast(m_tmpItemInfo);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getMenuInfo()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link MenuObjectInfoUtils#getMenuInfo(Object)}.<br>
   * Object does not implement {@link IAdaptable}.
   */
  public void test_getMenuInfo_1() throws Exception {
    IMocksControl control = createStrictControl();
    Object object = control.createMock(Object.class);
    // prepare scenario
    control.replay();
    // validate
    assertNull(MenuObjectInfoUtils.getMenuInfo(object));
    control.verify();
  }

  /**
   * Test for {@link MenuObjectInfoUtils#getMenuInfo(Object)}.<br>
   * Object implements {@link IAdaptable} and returns {@link IMenuInfo}.
   */
  public void test_getMenuInfo_2() throws Exception {
    IMocksControl control = EasyMock.createStrictControl();
    IAdaptable object = control.createMock(IAdaptable.class);
    IMenuInfo menuInfo = control.createMock(IMenuInfo.class);
    // prepare scenario
    expect(object.getAdapter(IMenuInfo.class)).andReturn(menuInfo);
    control.replay();
    // validate
    assertSame(menuInfo, MenuObjectInfoUtils.getMenuInfo(object));
    control.verify();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getMenuPopupInfo()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link MenuObjectInfoUtils#getMenuPopupInfo(Object)}.<br>
   * Object does not implement {@link IAdaptable}.
   */
  public void test_getMenuPopupInfo_1() throws Exception {
    IMocksControl control = createStrictControl();
    Object object = control.createMock(Object.class);
    // prepare scenario
    control.replay();
    // validate
    assertNull(MenuObjectInfoUtils.getMenuPopupInfo(object));
    control.verify();
  }

  /**
   * Test for {@link MenuObjectInfoUtils#getMenuPopupInfo(Object)}.<br>
   * Object implements {@link IAdaptable} and returns {@link IMenuPopupInfo}.
   */
  public void test_getMenuPopupInfo_2() throws Exception {
    IMocksControl control = EasyMock.createStrictControl();
    IAdaptable object = control.createMock(IAdaptable.class);
    IMenuPopupInfo popupInfo = control.createMock(IMenuPopupInfo.class);
    // prepare scenario
    expect(object.getAdapter(IMenuPopupInfo.class)).andReturn(popupInfo);
    control.replay();
    // validate
    assertSame(popupInfo, MenuObjectInfoUtils.getMenuPopupInfo(object));
    control.verify();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getSubMenu()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link MenuObjectInfoUtils#getSubMenu(IMenuObjectInfo)}.<br>
   * Use on {@link IMenuInfo}, that can not have {@link IMenuInfo}.
   */
  public void test_getSubMenu_1() throws Exception {
    IMocksControl control = EasyMock.createStrictControl();
    IMenuInfo menuInfo = control.createMock(IMenuInfo.class);
    // prepare scenario
    control.replay();
    // validate
    assertNull(MenuObjectInfoUtils.getSubMenu(menuInfo));
    control.verify();
  }

  /**
   * Test for {@link MenuObjectInfoUtils#getSubMenu(IMenuObjectInfo)}.<br>
   * Use on {@link IMenuPopupInfo}.
   */
  public void test_getSubMenu_2() throws Exception {
    IMocksControl control = EasyMock.createStrictControl();
    IMenuPopupInfo popupInfo = control.createMock(IMenuPopupInfo.class);
    IMenuInfo menuInfo = control.createMock(IMenuInfo.class);
    // prepare scenario
    expect(popupInfo.getMenu()).andReturn(menuInfo);
    control.replay();
    // validate
    assertSame(menuInfo, MenuObjectInfoUtils.getSubMenu(popupInfo));
    control.verify();
  }

  /**
   * Test for {@link MenuObjectInfoUtils#getSubMenu(IMenuObjectInfo)}.<br>
   * Use on {@link IMenuItemInfo}.
   */
  public void test_getSubMenu_3() throws Exception {
    IMocksControl control = EasyMock.createStrictControl();
    IMenuItemInfo itemInfo = control.createMock(IMenuItemInfo.class);
    IMenuInfo menuInfo = control.createMock(IMenuInfo.class);
    // prepare scenario
    expect(itemInfo.getMenu()).andReturn(menuInfo);
    control.replay();
    // validate
    assertSame(menuInfo, MenuObjectInfoUtils.getSubMenu(itemInfo));
    control.verify();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // isParentChild()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link MenuObjectInfoUtils#isParentChild(IMenuObjectInfo, IMenuObjectInfo)}.<br>
   * One or two <code>null</code> objects.
   */
  public void test_isParentChild_null() throws Exception {
    IMocksControl control = EasyMock.createStrictControl();
    IMenuObjectInfo parent = control.createMock(IMenuObjectInfo.class);
    IMenuObjectInfo child = control.createMock(IMenuObjectInfo.class);
    // two "null" objects
    {
      control.replay();
      assertFalse(MenuObjectInfoUtils.isParentChild(null, null));
      control.verify();
      control.reset();
    }
    // parent "null"
    {
      control.replay();
      assertFalse(MenuObjectInfoUtils.isParentChild(null, child));
      control.verify();
      control.reset();
    }
    // child "null"
    {
      control.replay();
      assertFalse(MenuObjectInfoUtils.isParentChild(parent, null));
      control.verify();
      control.reset();
    }
  }

  /**
   * Test for {@link MenuObjectInfoUtils#isParentChild(IMenuObjectInfo, IMenuObjectInfo)}.<br>
   * Two general {@link IMenuObjectInfo}'s.
   */
  public void test_isParentChild_genericObjects() throws Exception {
    IMocksControl control = EasyMock.createStrictControl();
    IMenuObjectInfo parent = control.createMock(IMenuObjectInfo.class);
    IMenuObjectInfo child = control.createMock(IMenuObjectInfo.class);
    // validate
    control.replay();
    assertFalse(MenuObjectInfoUtils.isParentChild(parent, child));
    control.verify();
  }

  /**
   * Test for {@link MenuObjectInfoUtils#isParentChild(IMenuObjectInfo, IMenuObjectInfo)}.<br>
   * Parent/child found.
   */
  public void test_isParentChild_hit() throws Exception {
    IMocksControl control = EasyMock.createStrictControl();
    IMenuObjectInfo object = control.createMock(IMenuObjectInfo.class);
    // validate
    control.replay();
    assertTrue(MenuObjectInfoUtils.isParentChild(object, object));
    control.verify();
  }

  /**
   * Test for {@link MenuObjectInfoUtils#isParentChild(IMenuObjectInfo, IMenuObjectInfo)}.<br>
   * {@link IMenuPopupInfo} and its menu.
   */
  public void test_isParentChild_popupWithMenu() throws Exception {
    IMocksControl control = EasyMock.createStrictControl();
    IMenuPopupInfo popup = control.createMock(IMenuPopupInfo.class);
    IMenuInfo menu = control.createMock(IMenuInfo.class);
    // prepare scenario
    expect(popup.getMenu()).andReturn(menu);
    control.replay();
    // validate
    assertTrue(MenuObjectInfoUtils.isParentChild(popup, menu));
    control.verify();
  }

  /**
   * Test for {@link MenuObjectInfoUtils#isParentChild(IMenuObjectInfo, IMenuObjectInfo)}.<br>
   * {@link IMenuItemInfo} and its menu.
   */
  public void test_isParentChild_itemWithMenu() throws Exception {
    IMocksControl control = EasyMock.createStrictControl();
    IMenuItemInfo item = control.createMock(IMenuItemInfo.class);
    IMenuInfo menu = control.createMock(IMenuInfo.class);
    // prepare scenario
    expect(item.getMenu()).andReturn(menu);
    control.replay();
    // validate
    assertTrue(MenuObjectInfoUtils.isParentChild(item, menu));
    control.verify();
  }

  /**
   * Test for {@link MenuObjectInfoUtils#isParentChild(IMenuObjectInfo, IMenuObjectInfo)}.<br>
   * {@link IMenuInfo} and some of its {@link IMenuItemInfo}.
   */
  public void test_isParentChild_menuWithItems() throws Exception {
    IMocksControl control = EasyMock.createStrictControl();
    IMenuInfo menu = control.createMock(IMenuInfo.class);
    IMenuItemInfo item_1 = control.createMock(IMenuItemInfo.class);
    IMenuItemInfo item_2 = control.createMock(IMenuItemInfo.class);
    IMenuItemInfo item_3 = control.createMock(IMenuItemInfo.class);
    List<IMenuItemInfo> items = ImmutableList.of(item_1, item_2, item_3);
    // prepare scenario
    expect(menu.getItems()).andReturn(items);
    expect(item_1.getMenu()).andReturn(null);
    control.replay();
    // validate
    assertTrue(MenuObjectInfoUtils.isParentChild(menu, item_2));
    control.verify();
  }
}
