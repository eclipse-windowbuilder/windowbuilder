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
package org.eclipse.wb.internal.swt.model.widgets.exposed;

import org.eclipse.wb.internal.core.model.JavaInfoUtils.HierarchyProvider;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.swt.widgets.Table;

import org.apache.commons.lang.ArrayUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of {@link HierarchyProvider} for SWT items.
 *
 * @author mitin_aa
 * @coverage swt.model.widgets
 */
public final class ItemsHierarchyProvider extends HierarchyProvider {
  ////////////////////////////////////////////////////////////////////////////
  //
  // HierarchyProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Object getParentObject(Object object) throws Exception {
    if (isSuccessorOf(object, "org.eclipse.swt.widgets.TableItem")
        || isSuccessorOf(object, "org.eclipse.swt.widgets.TableColumn")
        || isSuccessorOf(object, "org.eclipse.swt.widgets.TreeItem")
        || isSuccessorOf(object, "org.eclipse.swt.widgets.Menu")) {
      return ReflectionUtils.invokeMethod(object, "getParent()");
    }
    return null;
  }

  @Override
  public Object[] getChildrenObjects(Object object) throws Exception {
    if (isSuccessorOf(object, "org.eclipse.swt.widgets.Table")) {
      List<Object> childrenObjects = new ArrayList<>();
      Collections.addAll(childrenObjects, getItems(object, "getColumns"));
      Collections.addAll(childrenObjects, getItems(object, "getItems"));
      return childrenObjects.toArray(new Object[childrenObjects.size()]);
    }
    if (isSuccessorOf(object, "org.eclipse.swt.widgets.Tree")) {
      List<Object> childrenObjects = new ArrayList<>();
      Collections.addAll(childrenObjects, getItems(object, "getItems"));
      return childrenObjects.toArray(new Object[childrenObjects.size()]);
    }
    if (isSuccessorOf(object, "org.eclipse.swt.widgets.Menu")) {
      return getItems(object, "getItems");
    }
    // menu item may have a child menu
    if (isSuccessorOf(object, "org.eclipse.swt.widgets.MenuItem")) {
      Object menu = ReflectionUtils.invokeMethod(object, "getMenu()");
      if (menu != null) {
        return new Object[]{menu};
      }
    }
    // no children
    return ArrayUtils.EMPTY_OBJECT_ARRAY;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if given {@link Object} has compatible type.
   */
  private static boolean isSuccessorOf(Object object, String requiredClass) throws Exception {
    return ReflectionUtils.isSuccessorOf(object.getClass(), requiredClass);
  }

  /**
   * @return the result of "get array" method, such as {@link Table#getItems()}.
   */
  private static Object[] getItems(Object object, String methodName) throws Exception {
    return (Object[]) ReflectionUtils.invokeMethod2(object, methodName);
  }
}