/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
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
package org.eclipse.wb.internal.layout.group.model;

import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.core.model.ObjectInfoUtils;

import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;

import org.netbeans.modules.form.layoutdesign.LayoutComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility functions for work with GL.
 *
 * @author mitin_aa
 */
public final class GroupLayoutUtils {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	private GroupLayoutUtils() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link LayoutComponent} instance associated with given
	 *         {@link AbstractComponentInfo} instance. May return <code>null</code>.
	 */
	public static LayoutComponent getLayoutComponent(IGroupLayoutInfo layout,
			IAbstractComponentInfo component) {
		return layout.getLayoutModel().getLayoutComponent(
				ObjectInfoUtils.getId(component.getUnderlyingModel()));
	}

	/**
	 * @return the {@link List} of ids corresponding to list of {@link AbstractComponentInfo}.
	 */
	public static <C extends IAbstractComponentInfo> List<String> getIdsList(final List<C> components) {
		final List<String> idsList = new ArrayList<>();
		for (C component : components) {
			idsList.add(ObjectInfoUtils.getId(component.getUnderlyingModel()));
		}
		return idsList;
	}

	/**
	 * @return the draw2d rectangle by doing a union of AWT rectangles.
	 */
	public static org.eclipse.draw2d.geometry.Rectangle getRectangleUnion(final java.awt.Rectangle[] boundsArray) {
		org.eclipse.draw2d.geometry.Rectangle unionBounds = get(boundsArray[0]);
		for (int i = 1; i < boundsArray.length; i++) {
			unionBounds.union(get(boundsArray[i]));
		}
		return unionBounds;
	}

	/**
	 * @return the bounds in GroupLayout container client area coordinates.
	 */
	public static java.awt.Rectangle getBoundsInLayout(IGroupLayoutInfo layout,
			IAbstractComponentInfo model) {
		Rectangle modelBounds = model.getModelBounds().getCopy();
		Insets insets = layout.getContainerInsets();
		modelBounds.performTranslate(-insets.left, -insets.top);
		return get(modelBounds);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// system transformation
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the draw2d {@link org.eclipse.draw2d.geometry.Rectangle} for given AWT
	 *         {@link java.awt.Rectangle}.
	 */
	public static org.eclipse.draw2d.geometry.Rectangle get(java.awt.Rectangle o) {
		return new org.eclipse.draw2d.geometry.Rectangle(o.x, o.y, o.width, o.height);
	}

	public static java.awt.Rectangle get(org.eclipse.draw2d.geometry.Rectangle o) {
		return new java.awt.Rectangle(o.x, o.y, o.width, o.height);
	}
}
