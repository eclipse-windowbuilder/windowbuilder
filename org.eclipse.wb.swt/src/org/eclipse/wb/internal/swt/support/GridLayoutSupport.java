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
package org.eclipse.wb.internal.swt.support;

import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.swt.layout.GridLayout;

import java.util.Map;

/**
 * Stub class for using SWT {@link org.eclipse.swt.layout.GridLayout}'s in another
 * {@link ClassLoader}.
 *
 * @author lobas_av
 * @coverage swt.support
 */
public class GridLayoutSupport extends AbstractSupport {
  ////////////////////////////////////////////////////////////////////////////
  //
  // GridLayout
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link GridLayout#numColumns} value.
   */
  public static int getNumColumns(Object layout) {
    return ReflectionUtils.getFieldInt(layout, "numColumns");
  }

  /**
   * @return the {@link Point} with column/row for given {@link org.eclipse.swt.widgets.Control} and
   *         {@link org.eclipse.swt.layout.GridLayout} objects.
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public static Point getXY(Object layout, Object control) {
    Map<Object, Object> map = (Map) ReflectionUtils.getFieldObject(layout, "m_controlToXY");
    Object point = map.get(control);
    return point != null ? PointSupport.getPoint(point) : null;
  }

  /**
   * @return the {@link Point} with column/row for given {@link org.eclipse.swt.widgets.Control} and
   *         {@link org.eclipse.swt.layout.GridLayout} objects.
   */
  public static Dimension getWH(Object layout, Object control) {
    Object gridData2 =
        ReflectionUtils.invokeMethodEx(
            layout,
            "getLayoutData2(org.eclipse.swt.widgets.Control)",
            control);
    int width = ReflectionUtils.getFieldInt(gridData2, "horizontalSpan");
    int height = ReflectionUtils.getFieldInt(gridData2, "verticalSpan");
    return new Dimension(width, height);
  }

  /**
   * @return the column origins for given {@link org.eclipse.swt.layout.GridLayout} object.
   */
  public static int[] getColumnOrigins(Object layout) {
    return (int[]) ReflectionUtils.getFieldObject(layout, "m_columnOrigins");
  }

  /**
   * @return the column widths for given {@link org.eclipse.swt.layout.GridLayout} object.
   */
  public static int[] getColumnWidths(Object layout) {
    return (int[]) ReflectionUtils.getFieldObject(layout, "m_columnWidths");
  }

  /**
   * @return the row origins for given {@link org.eclipse.swt.layout.GridLayout} object.
   */
  public static int[] getRowOrigins(Object layout) {
    return (int[]) ReflectionUtils.getFieldObject(layout, "m_rowOrigins");
  }

  /**
   * @return the row heights for given {@link org.eclipse.swt.layout.GridLayout} object.
   */
  public static int[] getRowHeights(Object layout) {
    return (int[]) ReflectionUtils.getFieldObject(layout, "m_rowHeights");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GridData
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Create new {@link org.eclipse.swt.layout.GridData}.
   */
  public static Object createGridData() throws Exception {
    return loadClass("org.eclipse.swt.layout.GridData").newInstance();
  }
}