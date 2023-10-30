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

import org.eclipse.wb.internal.core.model.menu.IMenuInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuItemInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuObjectInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuPopupInfo;
import org.eclipse.wb.internal.core.model.menu.MenuObjectInfoUtils;
import org.eclipse.wb.internal.core.utils.IAdaptable;
import org.eclipse.wb.internal.core.utils.IAdaptableFactory;
import org.eclipse.wb.tests.designer.TestUtils;
import org.eclipse.wb.tests.designer.tests.DesignerTestCase;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.Test;

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
	@Test
	public void test_setSelectingObject_menuObject() throws Exception {
		IMenuObjectInfo object = mock(IMenuObjectInfo.class);
		//
		MenuObjectInfoUtils.setSelectingObject(object);
		assertSame(object, MenuObjectInfoUtils.m_selectingObject);
	}

	/**
	 * Test for {@link MenuObjectInfoUtils#setSelectingObject(Object)}.
	 */
	@Test
	public void test_setSelectingObject_pureObject() throws Exception {
		IAdaptable object = mock(IAdaptable.class);
		IMenuItemInfo itemInfo = mock(IMenuItemInfo.class);
		// prepare scenario
		when(object.getAdapter(IMenuObjectInfo.class)).thenReturn(itemInfo);
		// validate
		MenuObjectInfoUtils.setSelectingObject(object);
		assertSame(itemInfo, MenuObjectInfoUtils.m_selectingObject);
		//
		verify(object).getAdapter(IMenuObjectInfo.class);
		verifyNoMoreInteractions(object);
		verifyNoInteractions(itemInfo);
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
	@Test
	public void test_getMenuObjectInfo_1() throws Exception {
		Object object = mock(Object.class);
		// validate
		assertNull(MenuObjectInfoUtils.getMenuObjectInfo(object));
		//
		verifyNoInteractions(object);
	}

	/**
	 * Test for {@link MenuObjectInfoUtils#getMenuItemInfo(Object)}.<br>
	 * Object implements {@link IAdaptable} and returns {@link IMenuItemInfo}.
	 */
	@Test
	public void test_getMenuObjectInfo_2() throws Exception {
		IAdaptable object = mock(IAdaptable.class);
		IMenuItemInfo itemInfo = mock(IMenuItemInfo.class);
		// prepare scenario
		when(object.getAdapter(IMenuObjectInfo.class)).thenReturn(itemInfo);
		// validate
		assertSame(itemInfo, MenuObjectInfoUtils.getMenuObjectInfo(object));
		//
		verify(object).getAdapter(IMenuObjectInfo.class);
		verifyNoMoreInteractions(object);
		verifyNoInteractions(itemInfo);
	}

	/**
	 * Test for {@link MenuObjectInfoUtils#getMenuItemInfo(Object)}.<br>
	 * Object implements {@link IAdaptable} and returns {@link IMenuInfo}.
	 */
	@Test
	public void test_getMenuObjectInfo_3() throws Exception {
		IAdaptable object = mock(IAdaptable.class);
		IMenuInfo menuInfo = mock(IMenuInfo.class);
		// prepare scenario
		when(object.getAdapter(IMenuObjectInfo.class)).thenReturn(menuInfo);
		// validate
		assertSame(menuInfo, MenuObjectInfoUtils.getMenuObjectInfo(object));
		//
		verify(object).getAdapter(IMenuObjectInfo.class);
		verifyNoMoreInteractions(object);
		verifyNoInteractions(menuInfo);
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
	@Test
	public void test_getMenuItemInfo_1() throws Exception {
		Object object = mock(Object.class);
		// validate
		assertNull(MenuObjectInfoUtils.getMenuItemInfo(object));
		//
		verifyNoInteractions(object);
	}

	/**
	 * Test for {@link MenuObjectInfoUtils#getMenuItemInfo(Object)}.<br>
	 * Object implements {@link IAdaptable} and returns {@link IMenuItemInfo}.
	 */
	@Test
	public void test_getMenuItemInfo_2() throws Exception {
		IAdaptable object = mock(IAdaptable.class);
		IMenuItemInfo itemInfo = mock(IMenuItemInfo.class);
		// prepare scenario
		when(object.getAdapter(IMenuItemInfo.class)).thenReturn(itemInfo);
		// validate
		assertSame(itemInfo, MenuObjectInfoUtils.getMenuItemInfo(object));
		//
		verify(object).getAdapter(IMenuItemInfo.class);
		verifyNoMoreInteractions(object);
		verifyNoInteractions(itemInfo);
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
	@Test
	public void test_getMenuItemInfo_3() throws Exception {
		IAdaptable object = mock(IAdaptable.class);
		m_tmpItemInfo = mock(IMenuItemInfo.class);
		//
		String pointId = "org.eclipse.wb.core.adaptableFactories";
		{
			String contribution = "  <factory class='" + MyAdaptableFactory.class.getName() + "'/>";
			TestUtils.addDynamicExtension(pointId, contribution);
		}
		try {
			// validate
			assertSame(m_tmpItemInfo, MenuObjectInfoUtils.getMenuItemInfo(object));
			//
			verify(object).getAdapter(IMenuItemInfo.class);
			verifyNoMoreInteractions(object);
			verifyNoInteractions(m_tmpItemInfo);
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
		@Override
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
	@Test
	public void test_getMenuInfo_1() throws Exception {
		Object object = mock(Object.class);
		// validate
		assertNull(MenuObjectInfoUtils.getMenuInfo(object));
		//
		verifyNoInteractions(object);
	}

	/**
	 * Test for {@link MenuObjectInfoUtils#getMenuInfo(Object)}.<br>
	 * Object implements {@link IAdaptable} and returns {@link IMenuInfo}.
	 */
	@Test
	public void test_getMenuInfo_2() throws Exception {
		IAdaptable object = mock(IAdaptable.class);
		IMenuInfo menuInfo = mock(IMenuInfo.class);
		// prepare scenario
		when(object.getAdapter(IMenuInfo.class)).thenReturn(menuInfo);
		// validate
		assertSame(menuInfo, MenuObjectInfoUtils.getMenuInfo(object));
		//
		verify(object).getAdapter(IMenuInfo.class);
		verifyNoMoreInteractions(object);
		verifyNoInteractions(menuInfo);
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
	@Test
	public void test_getMenuPopupInfo_1() throws Exception {
		Object object = mock(Object.class);
		// validate
		assertNull(MenuObjectInfoUtils.getMenuPopupInfo(object));
		//
		verifyNoInteractions(object);
	}

	/**
	 * Test for {@link MenuObjectInfoUtils#getMenuPopupInfo(Object)}.<br>
	 * Object implements {@link IAdaptable} and returns {@link IMenuPopupInfo}.
	 */
	@Test
	public void test_getMenuPopupInfo_2() throws Exception {
		IAdaptable object = mock(IAdaptable.class);
		IMenuPopupInfo popupInfo = mock(IMenuPopupInfo.class);
		// prepare scenario
		when(object.getAdapter(IMenuPopupInfo.class)).thenReturn(popupInfo);
		// validate
		assertSame(popupInfo, MenuObjectInfoUtils.getMenuPopupInfo(object));
		//
		verify(object).getAdapter(IMenuPopupInfo.class);
		verifyNoMoreInteractions(object);
		verifyNoInteractions(popupInfo);
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
	@Test
	public void test_getSubMenu_1() throws Exception {
		IMenuInfo menuInfo = mock(IMenuInfo.class);
		// validate
		assertNull(MenuObjectInfoUtils.getSubMenu(menuInfo));
		//
		verifyNoInteractions(menuInfo);
	}

	/**
	 * Test for {@link MenuObjectInfoUtils#getSubMenu(IMenuObjectInfo)}.<br>
	 * Use on {@link IMenuPopupInfo}.
	 */
	@Test
	public void test_getSubMenu_2() throws Exception {
		IMenuPopupInfo popupInfo = mock(IMenuPopupInfo.class);
		IMenuInfo menuInfo = mock(IMenuInfo.class);
		// prepare scenario
		when(popupInfo.getMenu()).thenReturn(menuInfo);
		// validate
		assertSame(menuInfo, MenuObjectInfoUtils.getSubMenu(popupInfo));
		//
		verify(popupInfo).getMenu();
		verifyNoMoreInteractions(popupInfo);
		verifyNoInteractions(menuInfo);
	}

	/**
	 * Test for {@link MenuObjectInfoUtils#getSubMenu(IMenuObjectInfo)}.<br>
	 * Use on {@link IMenuItemInfo}.
	 */
	@Test
	public void test_getSubMenu_3() throws Exception {
		IMenuItemInfo itemInfo = mock(IMenuItemInfo.class);
		IMenuInfo menuInfo = mock(IMenuInfo.class);
		// prepare scenario
		when(itemInfo.getMenu()).thenReturn(menuInfo);
		// validate
		assertSame(menuInfo, MenuObjectInfoUtils.getSubMenu(itemInfo));
		//
		verify(itemInfo).getMenu();
		verifyNoMoreInteractions(itemInfo);
		verifyNoInteractions(menuInfo);
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
	@Test
	public void test_isParentChild_null() throws Exception {
		IMenuObjectInfo parent = mock(IMenuObjectInfo.class);
		IMenuObjectInfo child = mock(IMenuObjectInfo.class);
		// two "null" objects
		{
			assertFalse(MenuObjectInfoUtils.isParentChild(null, null));
			//
			verifyNoInteractions(parent);
			verifyNoInteractions(child);
		}
		// parent "null"
		{
			assertFalse(MenuObjectInfoUtils.isParentChild(null, child));
			//
			verifyNoInteractions(parent);
			verifyNoInteractions(child);
		}
		// child "null"
		{
			assertFalse(MenuObjectInfoUtils.isParentChild(parent, null));
			//
			verifyNoInteractions(parent);
			verifyNoInteractions(child);
		}
	}

	/**
	 * Test for {@link MenuObjectInfoUtils#isParentChild(IMenuObjectInfo, IMenuObjectInfo)}.<br>
	 * Two general {@link IMenuObjectInfo}'s.
	 */
	@Test
	public void test_isParentChild_genericObjects() throws Exception {
		IMenuObjectInfo parent = mock(IMenuObjectInfo.class);
		IMenuObjectInfo child = mock(IMenuObjectInfo.class);
		// validate
		assertFalse(MenuObjectInfoUtils.isParentChild(parent, child));
		//
		verifyNoInteractions(parent);
		verifyNoInteractions(child);
	}

	/**
	 * Test for {@link MenuObjectInfoUtils#isParentChild(IMenuObjectInfo, IMenuObjectInfo)}.<br>
	 * Parent/child found.
	 */
	@Test
	public void test_isParentChild_hit() throws Exception {
		IMenuObjectInfo object = mock(IMenuObjectInfo.class);
		// validate
		assertTrue(MenuObjectInfoUtils.isParentChild(object, object));
		//
		verifyNoInteractions(object);
	}

	/**
	 * Test for {@link MenuObjectInfoUtils#isParentChild(IMenuObjectInfo, IMenuObjectInfo)}.<br>
	 * {@link IMenuPopupInfo} and its menu.
	 */
	@Test
	public void test_isParentChild_popupWithMenu() throws Exception {
		IMenuPopupInfo popup = mock(IMenuPopupInfo.class);
		IMenuInfo menu = mock(IMenuInfo.class);
		// prepare scenario
		when(popup.getMenu()).thenReturn(menu);
		// validate
		assertTrue(MenuObjectInfoUtils.isParentChild(popup, menu));
		//
		verify(popup).getMenu();
		verifyNoMoreInteractions(popup);
		verifyNoInteractions(menu);
	}

	/**
	 * Test for {@link MenuObjectInfoUtils#isParentChild(IMenuObjectInfo, IMenuObjectInfo)}.<br>
	 * {@link IMenuItemInfo} and its menu.
	 */
	@Test
	public void test_isParentChild_itemWithMenu() throws Exception {
		IMenuItemInfo item = mock(IMenuItemInfo.class);
		IMenuInfo menu = mock(IMenuInfo.class);
		// prepare scenario
		when(item.getMenu()).thenReturn(menu);
		// validate
		assertTrue(MenuObjectInfoUtils.isParentChild(item, menu));
		//
		verify(item).getMenu();
		verifyNoMoreInteractions(item);
		verifyNoInteractions(menu);
	}

	/**
	 * Test for {@link MenuObjectInfoUtils#isParentChild(IMenuObjectInfo, IMenuObjectInfo)}.<br>
	 * {@link IMenuInfo} and some of its {@link IMenuItemInfo}.
	 */
	@Test
	public void test_isParentChild_menuWithItems() throws Exception {
		IMenuInfo menu = mock(IMenuInfo.class);
		IMenuItemInfo item_1 = mock(IMenuItemInfo.class);
		IMenuItemInfo item_2 = mock(IMenuItemInfo.class);
		IMenuItemInfo item_3 = mock(IMenuItemInfo.class);
		List<IMenuItemInfo> items = List.of(item_1, item_2, item_3);
		// prepare scenario
		when(menu.getItems()).thenReturn(items);
		when(item_1.getMenu()).thenReturn(null);
		// validate
		assertTrue(MenuObjectInfoUtils.isParentChild(menu, item_2));
		//
		verify(menu).getItems();
		verifyNoMoreInteractions(menu);
		verify(item_1).getMenu();
		verifyNoMoreInteractions(item_1);
		verifyNoInteractions(item_2);
		verifyNoInteractions(item_3);
	}
}
