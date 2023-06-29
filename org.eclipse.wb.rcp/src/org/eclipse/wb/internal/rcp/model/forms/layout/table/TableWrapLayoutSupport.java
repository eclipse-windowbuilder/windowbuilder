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
package org.eclipse.wb.internal.rcp.model.forms.layout.table;

import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swt.support.AbstractSupport;
import org.eclipse.wb.internal.swt.support.PointSupport;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import java.util.Map;

/**
 * Support for using {@link TableWrapLayout} in another {@link ClassLoader}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.forms
 */
public class TableWrapLayoutSupport extends AbstractSupport {
	////////////////////////////////////////////////////////////////////////////
	//
	// TableWrapLayout
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link Point} with column/row for given {@link org.eclipse.swt.widgets.Control} and
	 *         {@link TableWrapLayout} objects.
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	public static Point getXY(Object layout, Object control) throws Exception {
		Map<Object, Object> map = (Map) ReflectionUtils.getFieldObject(layout, "m_controlToXY");
		Object point = map.get(control);
		return point != null ? PointSupport.getPoint(point) : null;
	}

	/**
	 * @return the column origins for given {@link TableWrapLayout} object.
	 */
	public static int[] getColumnOrigins(Object layout) throws Exception {
		return (int[]) ReflectionUtils.getFieldObject(layout, "m_columnOrigins");
	}

	/**
	 * @return the column widths for given {@link TableWrapLayout} object.
	 */
	public static int[] getColumnWidths(Object layout) throws Exception {
		return (int[]) ReflectionUtils.getFieldObject(layout, "m_columnWidths");
	}

	/**
	 * @return the row origins for given {@link TableWrapLayout} object.
	 */
	public static int[] getRowOrigins(Object layout) throws Exception {
		return (int[]) ReflectionUtils.getFieldObject(layout, "m_rowOrigins");
	}

	/**
	 * @return the row heights for given {@link TableWrapLayout} object.
	 */
	public static int[] getRowHeights(Object layout) throws Exception {
		return (int[]) ReflectionUtils.getFieldObject(layout, "m_rowHeights");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// TableWrapData
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Create new {@link TableWrapData}.
	 */
	public static Object createTableWrapData() throws Exception {
		return loadClass("org.eclipse.ui.forms.widgets.TableWrapData").newInstance();
	}
}