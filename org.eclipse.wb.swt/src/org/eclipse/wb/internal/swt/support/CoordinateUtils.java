/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
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
package org.eclipse.wb.internal.swt.support;

import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.os.OSSupport;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;

/**
 * Utilities for SWT widget coordinates.
 *
 * @author lobas_av
 * @coverage swt.support
 */
public final class CoordinateUtils {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	private CoordinateUtils() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Bounds
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the location of given control in display coordinates.
	 */
	public static Point getDisplayLocation(Control composite) throws Exception {
		org.eclipse.swt.graphics.Rectangle bounds = composite.getBounds();
		int x = bounds.x;
		int y = bounds.y;
		return getDisplayLocation(composite, x, y);
	}

	/**
	 * @return the given location (in parent of given <code>composite</code>) in display coordinates.
	 */
	public static Point getDisplayLocation(Control composite, int x, int y) throws Exception {
		if (EnvironmentUtils.IS_LINUX && composite instanceof Shell shell) {
			// In GTK, the bounds of a shell return the top-left position of the window
			// manager. Because this manager is not part of the actual shell, we need to use
			// this little workaround to get the REAL position of the shell.
			// See: https://github.com/eclipse-platform/eclipse.platform.swt/issues/828
			Point point = composite.toDisplay(0, 0);
			y = point.y;
			Menu menuBar = shell.getMenuBar();
			if (menuBar != null) {
				var menuBounds = OSSupport.get().getMenuBarBounds(menuBar);
				y -= menuBounds.height;
			}
		}
		if (!(composite instanceof Shell)) {
			Composite parent = composite.getParent();
			if (parent != null) {
				Point location = parent.toDisplay(x, y);
				x = location.x;
				y = location.y;
			}
		}
		return new Point(x, y);
	}

	/**
	 * @return the bounds of <code>child</code> relative to <code>parent</code>.
	 */
	public static Rectangle getBounds(Control parent, Control child) throws Exception {
		Rectangle bounds = new Rectangle(child.getBounds());
		Point childLocation = CoordinateUtils.getDisplayLocation(child);
		Point parentLocation = CoordinateUtils.getDisplayLocation(parent);
		bounds.x = childLocation.x - parentLocation.x;
		bounds.y = childLocation.y - parentLocation.y;
		return bounds;
	}

	/**
	 * @return {@link Insets} for given composite. Practically we need here only shift of
	 *         <code>(0,0)</code> point of client area relative to the top-left corner of bounds.
	 */
	public static Insets getClientAreaInsets(Composite composite) throws Exception {
		// prepare top/left
		int top;
		int left;
		{
			Point displayLocation = getDisplayLocation(composite);
			Point clientAreaLocation = composite.toDisplay(0, 0);
			// tweak for right-to-left
			{
				Composite parentComposite = composite != null ? composite.getParent() : null;
				boolean isRTL = composite != null && (composite.getStyle() & SWT.RIGHT_TO_LEFT) != 0;
				boolean isParentRTL = parentComposite != null && (parentComposite.getStyle() & SWT.RIGHT_TO_LEFT) != 0;
				if (isRTL && !isParentRTL) {
					org.eclipse.swt.graphics.Rectangle clientArea = composite.getClientArea();
					clientAreaLocation.x -= clientArea.width;
				}
			}
			//
			top = Math.abs(clientAreaLocation.y - displayLocation.y);
			left = Math.abs(clientAreaLocation.x - displayLocation.x);
		}
		// if client area (0,0) is not shifted from top-left corner of bounds, then no insets at all
		if (top == 0 && left == 0) {
			return IFigure.NO_INSETS;
		}
		// prepare bottom/right
		org.eclipse.swt.graphics.Rectangle bounds = composite.getBounds();
		org.eclipse.swt.graphics.Rectangle clientArea = composite.getClientArea();
		int bottom = bounds.height - top - clientArea.height;
		int right = bounds.width - left - clientArea.width;
		// final insets
		return new Insets(top, left, bottom, right);
	}

	/**
	 * Returns the {@link Insets} that can be used to crop bounds of this {@link Composite} to produce
	 * a rectangle which describes the area of this {@link Composite} which is capable of displaying
	 * data (that is, not covered by the "trimmings").
	 * <p>
	 * Note, that this method is different from {@link #getClientAreaInsets()}. For example in
	 * {@link Group} point <code>(0,0)</code> is point on group border, but
	 * {@link Group#getClientArea()} returns size of border on sides. But still, if we <b>want</b> to
	 * place child {@link Control} exactly in top-left point of {@link Group}, we should use
	 * <code>(0,0)</code>. However if we want to place {@link Control} in <b>top-left of preferred
	 * location</b>, then {@link #getClientAreaInsets2()} should be used.
	 *
	 * @return the {@link Insets} for "displaying data" part of given {@link Composite}.
	 */
	public static Insets getClientAreaInsets2(Composite composite) throws Exception {
		// if client area (0,0) is shifted from top-left corner of bounds, then no need it additional insets
		{
			Insets trimInsets = getClientAreaInsets(composite);
			if (trimInsets.top != 0 || trimInsets.left != 0) {
				return IFigure.NO_INSETS;
			}
		}
		// prepare bounds/clientArea
		org.eclipse.swt.graphics.Rectangle bounds = composite.getBounds();
		org.eclipse.swt.graphics.Rectangle clientArea = composite.getClientArea();
		// prepare insets
		int top = clientArea.y;
		int left = clientArea.x;
		int bottom = bounds.height - top - clientArea.height;
		int right = bounds.width - left - clientArea.width;
		return new Insets(top, left, bottom, right);
	}
}