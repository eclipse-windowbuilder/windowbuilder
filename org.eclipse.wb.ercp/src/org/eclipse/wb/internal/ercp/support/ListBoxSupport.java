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
package org.eclipse.wb.internal.ercp.support;

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swt.support.AbstractSupport;
import org.eclipse.wb.internal.swt.support.ControlSupport;

import java.lang.reflect.Array;

/**
 * Stub class for using eSWT {@link org.eclipse.ercp.swt.mobile.ListBox} in another
 * {@link ClassLoader}.
 * 
 * @author lobas_av
 * @coverage ercp.support
 */
public class ListBoxSupport extends AbstractSupport {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Bounds
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return bounds for {@link org.eclipse.ercp.swt.mobile.ListBoxItem} with given
   *         <code>index</code>.
   */
  public static Rectangle getItemBounds(Object listBox, int index) throws Exception {
    // prepare internal listBox data
    Object listCells = ReflectionUtils.getFieldObject(listBox, "listCells");
    int columns = ReflectionUtils.getFieldInt(listBox, "fColumns");
    int target = columns * index;
    // calculate bounds
    Rectangle bounds = ControlSupport.getBounds(Array.get(listCells, target));
    for (int i = target + 1; i < target + columns; i++) {
      bounds.union(ControlSupport.getBounds(Array.get(listCells, i)));
    }
    // border shift
    int border = (Integer) ReflectionUtils.invokeMethod2(listBox, "getBorderWidth");
    bounds.translate(border, border);
    //
    return bounds;
  }
}