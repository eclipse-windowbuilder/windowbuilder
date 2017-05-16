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
package org.eclipse.wb.internal.core.model.menu;

import org.eclipse.wb.internal.core.utils.IAdaptable;
import org.eclipse.wb.internal.core.utils.IAdaptableFactory;
import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;
import org.eclipse.wb.internal.core.utils.state.GlobalState;

import java.util.List;

/**
 * Utility method for {@link IMenuObjectInfo}.
 *
 * @author mitin_aa
 * @author scheglov_ke
 * @coverage core.model.menu
 */
public final class MenuObjectInfoUtils {
  public static IMenuObjectInfo m_selectingObject;

  /**
   * Sets {@link Object} (should be adaptable to {@link IMenuObjectInfo}) which should be selected
   * (and expanded) during refresh.
   */
  public static void setSelectingObject(Object object) {
    m_selectingObject = getMenuObjectInfo(object);
  }

  /**
   * Sets {@link IMenuObjectInfo} which should be selected (and expanded) during refresh.
   */
  public static void setSelectingObject(IMenuObjectInfo selectingObject) {
    m_selectingObject = selectingObject;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return {@link IMenuObjectInfo} for given object or <code>null</code> if object can't be
   *         adapted to {@link IMenuObjectInfo}.
   */
  public static IMenuObjectInfo getMenuObjectInfo(Object object) {
    return getAdapter(object, IMenuObjectInfo.class);
  }

  /**
   * @return {@link IMenuPopupInfo} for given object or <code>null</code> if object can't be adapted
   *         to {@link IMenuPopupInfo}.
   */
  public static IMenuPopupInfo getMenuPopupInfo(Object object) {
    return getAdapter(object, IMenuPopupInfo.class);
  }

  /**
   * @return {@link IMenuInfo} for given object or <code>null</code> if object can't be adapted to
   *         {@link IMenuInfo}.
   */
  public static IMenuInfo getMenuInfo(Object object) {
    return getAdapter(object, IMenuInfo.class);
  }

  /**
   * @return {@link IMenuItemInfo} for given object or <code>null</code> if object can't be adapted
   *         to {@link IMenuItemInfo}.
   */
  public static IMenuItemInfo getMenuItemInfo(Object object) {
    return getAdapter(object, IMenuItemInfo.class);
  }

  /**
   * @return {@link IMenuInfo} sub-menu of given {@link IMenuObjectInfo}, or <code>null</code> if
   *         object sub-menu.
   */
  public static IMenuInfo getSubMenu(IMenuObjectInfo object) {
    if (object instanceof IMenuPopupInfo) {
      return ((IMenuPopupInfo) object).getMenu();
    }
    if (object instanceof IMenuItemInfo) {
      return ((IMenuItemInfo) object).getMenu();
    }
    return null;
  }

  /**
   * @return <code>true</code> if given {@link IMenuObjectInfo} are direct/indirect parent and
   *         child.
   */
  public static boolean isParentChild(IMenuObjectInfo parent, IMenuObjectInfo child) {
    // identify
    if (parent == null || child == null) {
      return false;
    }
    if (parent == child) {
      return true;
    }
    // popup
    if (parent instanceof IMenuPopupInfo) {
      return isParentChild(((IMenuPopupInfo) parent).getMenu(), child);
    }
    // item
    if (parent instanceof IMenuItemInfo) {
      return isParentChild(((IMenuItemInfo) parent).getMenu(), child);
    }
    // menu
    if (parent instanceof IMenuInfo) {
      IMenuInfo parentMenu = (IMenuInfo) parent;
      for (IMenuItemInfo item : parentMenu.getItems()) {
        if (isParentChild(item, child)) {
          return true;
        }
      }
    }
    // no, not parent/child
    return false;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Validation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if given object is implicit (exposed), so can not be used as
   *         reference.
   */
  public static boolean isImplicitObject(Object reference) {
    return !GlobalState.getValidatorHelper().canReference(reference);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Internal utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @param object
   *          the {@link Object} to adapt.
   * @param adapter
   *          the type of adapter.
   *
   * @return the adapter of required type.
   */
  private static <T> T getAdapter(Object object, Class<T> adapter) {
    // check object itself
    if (object instanceof IAdaptable) {
      T adapted = ((IAdaptable) object).getAdapter(adapter);
      if (adapted != null) {
        return adapted;
      }
    }
    // check external IAdaptableFactory's
    List<IAdaptableFactory> adaptableFactories =
        ExternalFactoriesHelper.getElementsInstances(
            IAdaptableFactory.class,
            "org.eclipse.wb.core.adaptableFactories",
            "factory");
    for (IAdaptableFactory adaptableFactory : adaptableFactories) {
      T adapted = adaptableFactory.getAdapter(object, adapter);
      if (adapted != null) {
        return adapted;
      }
    }
    // can not adapt
    return null;
  }
}
