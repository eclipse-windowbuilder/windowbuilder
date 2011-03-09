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
package org.eclipse.wb.internal.rcp.model.widgets.exposed;

import org.eclipse.wb.internal.core.model.JavaInfoUtils.HierarchyProvider;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swt.support.AbstractSupport;

import org.eclipse.swt.widgets.Table;

import org.apache.commons.lang.ArrayUtils;

/**
 * Implementation of {@link HierarchyProvider} for SWT items.
 * 
 * @author scheglov_ke
 * @coverage rcp.model.widgets
 */
public final class ItemsHierarchyProvider extends HierarchyProvider {
  ////////////////////////////////////////////////////////////////////////////
  //
  // HierarchyProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Object getParentObject(Object object) throws Exception {
    if (isSuccessorOf(object, "org.eclipse.swt.widgets.TreeColumn")) {
      return ReflectionUtils.invokeMethod(object, "getParent()");
    }
    return null;
  }

  @Override
  public Object[] getChildrenObjects(Object object) throws Exception {
    if (AbstractSupport.is_RCP() && isSuccessorOf(object, "org.eclipse.swt.widgets.Tree")) {
      return getItems(object, "getColumns");
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