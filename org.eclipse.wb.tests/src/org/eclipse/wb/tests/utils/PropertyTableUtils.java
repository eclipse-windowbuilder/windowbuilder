/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.utils;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.table.PropertyTable;
import org.eclipse.wb.internal.core.model.property.table.PropertyTable.PropertyInfo;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.GraphicalEditPart;

import java.util.List;

/**
 * Utility class to access the internal fields and methods of a
 * {@link PropertyTable}.
 */
public class PropertyTableUtils {

	@SuppressWarnings("unchecked")
	private static List<? extends PropertyInfo> getProperties(PropertyTable propertyTable) {
		return (List<? extends PropertyInfo>) ReflectionUtils.getFieldObject(propertyTable, "m_properties");
	}

	/**
	 * @return the count of properties in "expanded" list.
	 */
	public static int getPropertiesCount(PropertyTable propertyTable) {
		return getProperties(propertyTable).size();
	}

	/**
	 * @return the {@link Property} from "expanded" list.
	 */
	public static Property getProperty(PropertyTable propertyTable, int index) {
		return getProperties(propertyTable).get(index).getProperty();
	}

	/**
	 * @return the location of state image (plus/minus) for given {@link Property}.
	 */
	public static org.eclipse.swt.graphics.Point getStateLocation(PropertyTable propertyTable, Property property)
			throws Exception {
		PropertyInfo propertyInfo = getPropertyInfo(propertyTable, property);
		if (propertyInfo != null) {
			GraphicalEditPart editPart = propertyTable.getEditPartForModel(propertyInfo);
			int x = (int) ReflectionUtils.invokeMethod(editPart, "getTitleX()");
			int y = getAbsoluteBounds(editPart).y();
			return new org.eclipse.swt.graphics.Point(x, y);
		}
		return null;
	}

	/**
	 * @return the location of state image (plus/minus) for given {@link Property}.
	 */
	public static org.eclipse.swt.graphics.Point getValueLocation(PropertyTable propertyTable, Property property) {
		PropertyInfo propertyInfo = getPropertyInfo(propertyTable, property);
		if (propertyInfo != null) {
			GraphicalEditPart editPart = propertyTable.getEditPartForModel(propertyInfo);
			int x = propertyTable.getSplitter() + 5;
			int y = getAbsoluteBounds(editPart).y();
			return new org.eclipse.swt.graphics.Point(x, y);
		}
		return null;
	}

	/**
	 * @return the bounds of the given edit part relative to the top right corner of
	 *         the viewport.
	 */
	private static Rectangle getAbsoluteBounds(GraphicalEditPart editPart) {
		IFigure figure = editPart.getFigure();
		Rectangle bounds = figure.getBounds().getCopy();
		figure.translateToAbsolute(bounds);
		return bounds;
	}

	/**
	 * @return the active {@link PropertyEditor}.
	 */
	public static PropertyEditor getActiveEditor(PropertyTable propertyTable) {
		return (PropertyEditor) ReflectionUtils.getFieldObject(propertyTable, "m_activeEditor");
	}

	/**
	 * @return the {@link PropertyInfo}for given {@link Property}.
	 */
	private static PropertyInfo getPropertyInfo(PropertyTable propertyTable, Property property) {
		for (PropertyInfo propertyInfo : getProperties(propertyTable)) {
			if (propertyInfo.getProperty() == property) {
				return propertyInfo;
			}
		}
		return null;
	}
}
