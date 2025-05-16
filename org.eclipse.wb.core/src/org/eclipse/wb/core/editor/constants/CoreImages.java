/*******************************************************************************
 * Copyright (c) 2024, 2025 Patrick Ziegler and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Patrick Ziegler - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.core.editor.constants;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.swt.graphics.Image;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import java.net.URI;
import java.net.URL;

/**
 * Utility class that contains all images that are used across several
 * WindowBuilder plugins. Each image is stored as a PNG and exists in normal
 * (96DPI) and high(192DPI) resolution.
 *
 * @noreference This class is not intended to be referenced by clients.
 * @noextend This class is not intended to be extended by clients.
 */
public abstract class CoreImages {
	private static ResourceManager RESOURCE_MANAGER = new LocalResourceManager(JFaceResources.getResources());

	public static ImageDescriptor ALIGNMENT_H_MENU_CENTER = of("alignment/h/menu/center.svg");
	public static ImageDescriptor ALIGNMENT_H_MENU_DEFAULT = of("alignment/h/menu/default.svg");
	public static ImageDescriptor ALIGNMENT_H_MENU_DELETE = of("alignment/h/menu/delete.svg");
	public static ImageDescriptor ALIGNMENT_H_MENU_FILL = of("alignment/h/menu/fill.svg");
	public static ImageDescriptor ALIGNMENT_H_MENU_GROW = of("alignment/h/menu/grow.svg");
	public static ImageDescriptor ALIGNMENT_H_MENU_LEADING = of("alignment/h/menu/leading.svg");
	public static ImageDescriptor ALIGNMENT_H_MENU_LEFT = of("alignment/h/menu/left.svg");
	public static ImageDescriptor ALIGNMENT_H_MENU_RIGHT = of("alignment/h/menu/right.svg");
	public static ImageDescriptor ALIGNMENT_H_MENU_TRAILING = of("alignment/h/menu/trailing.svg");
	public static ImageDescriptor ALIGNMENT_H_MENU_UNKNOWN = of("alignment/h/menu/unknown.svg");

	public static ImageDescriptor ALIGNMENT_H_SMALL_CENTER = of("alignment/h/small/center.svg");
	public static ImageDescriptor ALIGNMENT_H_SMALL_DEFAULT = of("alignment/h/small/default.svg");
	public static ImageDescriptor ALIGNMENT_H_SMALL_FILL = of("alignment/h/small/fill.svg");
	public static ImageDescriptor ALIGNMENT_H_SMALL_GROW = of("alignment/h/small/grow.svg");
	public static ImageDescriptor ALIGNMENT_H_SMALL_LEADING = of("alignment/h/small/leading.svg");
	public static ImageDescriptor ALIGNMENT_H_SMALL_LEFT = of("alignment/h/small/left.svg");
	public static ImageDescriptor ALIGNMENT_H_SMALL_RIGHT = of("alignment/h/small/right.svg");
	public static ImageDescriptor ALIGNMENT_H_SMALL_TRAILING = of("alignment/h/small/trailing.svg");
	public static ImageDescriptor ALIGNMENT_H_SMALL_UNKNOWN = of("alignment/h/small/unknown.svg");

	public static ImageDescriptor ALIGNMENT_V_MENU_BOTTOM = of("alignment/v/menu/bottom.svg");
	public static ImageDescriptor ALIGNMENT_V_MENU_CENTER = of("alignment/v/menu/center.svg");
	public static ImageDescriptor ALIGNMENT_V_MENU_DEFAULT = of("alignment/v/menu/default.svg");
	public static ImageDescriptor ALIGNMENT_V_MENU_DELETE = of("alignment/v/menu/delete.svg");
	public static ImageDescriptor ALIGNMENT_V_MENU_FILL = of("alignment/v/menu/fill.svg");
	public static ImageDescriptor ALIGNMENT_V_MENU_GROW = of("alignment/v/menu/grow.svg");
	public static ImageDescriptor ALIGNMENT_V_MENU_TOP = of("alignment/v/menu/top.svg");
	public static ImageDescriptor ALIGNMENT_V_MENU_UNKNOWN = of("alignment/v/menu/unknown.svg");

	public static ImageDescriptor ALIGNMENT_V_SMALL_BOTTOM = of("alignment/v/small/bottom.svg");
	public static ImageDescriptor ALIGNMENT_V_SMALL_CENTER = of("alignment/v/small/center.svg");
	public static ImageDescriptor ALIGNMENT_V_SMALL_DEFAULT = of("alignment/v/small/default.svg");
	public static ImageDescriptor ALIGNMENT_V_SMALL_FILL = of("alignment/v/small/fill.svg");
	public static ImageDescriptor ALIGNMENT_V_SMALL_GROW = of("alignment/v/small/grow.svg");
	public static ImageDescriptor ALIGNMENT_V_SMALL_TOP = of("alignment/v/small/top.svg");
	public static ImageDescriptor ALIGNMENT_V_SMALL_UNKNOWN = of("alignment/v/small/unknown.svg");

	public static ImageDescriptor PROPERTIES_BOOLEAN_NULL = of("properties/BooleanNull.svg");
	public static ImageDescriptor PROPERTIES_BOOLEAN_UNKNOWN = of("properties/BooleanUnknown.svg");
	public static ImageDescriptor PROPERTIES_DOTS = of("properties/dots.svg");
	public static ImageDescriptor PROPERTIES_DOWN = of("properties/down.svg");
	public static ImageDescriptor PROPERTIES_DT = of("properties/dt.svg");
	public static ImageDescriptor PROPERTIES_FALSE = of("properties/false.svg");
	public static ImageDescriptor PROPERTIES_MINUS = of("properties/minus.svg");
	public static ImageDescriptor PROPERTIES_PLUS = of("properties/plus.svg");
	public static ImageDescriptor PROPERTIES_TRUE = of("properties/true.svg");

	public static ImageDescriptor LAYOUT_ABSOLUTE_ALIGN_H_CENTER_DISABLED = of("info/layout/absolute/align_h_center_disabled.svg");
	public static ImageDescriptor LAYOUT_ABSOLUTE_ALIGN_H_CENTER = of("info/layout/absolute/align_h_center.svg");
	public static ImageDescriptor LAYOUT_ABSOLUTE_ALIGN_H_CENTERS_DISABLED = of("info/layout/absolute/align_h_centers_disabled.svg");
	public static ImageDescriptor LAYOUT_ABSOLUTE_ALIGN_H_CENTERS = of("info/layout/absolute/align_h_centers.svg");
	public static ImageDescriptor LAYOUT_ABSOLUTE_ALIGN_H_LEFT_DISABLED = of("info/layout/absolute/align_h_left_disabled.svg");
	public static ImageDescriptor LAYOUT_ABSOLUTE_ALIGN_H_LEFT = of("info/layout/absolute/align_h_left.svg");
	public static ImageDescriptor LAYOUT_ABSOLUTE_ALIGN_H_RIGHT_DISABLED = of("info/layout/absolute/align_h_right_disabled.svg");
	public static ImageDescriptor LAYOUT_ABSOLUTE_ALIGN_H_RIGHT = of("info/layout/absolute/align_h_right.svg");
	public static ImageDescriptor LAYOUT_ABSOLUTE_ALIGN_H_SPACE_DISABLED = of("info/layout/absolute/align_h_space_disabled.svg");
	public static ImageDescriptor LAYOUT_ABSOLUTE_ALIGN_H_SPACE = of("info/layout/absolute/align_h_space.svg");
	public static ImageDescriptor LAYOUT_ABSOLUTE_ALIGN_HEIGHT_DISABLED = of("info/layout/absolute/align_height_disabled.svg");
	public static ImageDescriptor LAYOUT_ABSOLUTE_ALIGN_HEIGHT = of("info/layout/absolute/align_height.svg");
	public static ImageDescriptor LAYOUT_ABSOLUTE_ALIGN_V_BOTTOM_DISABLED = of("info/layout/absolute/align_v_bottom_disabled.svg");
	public static ImageDescriptor LAYOUT_ABSOLUTE_ALIGN_V_BOTTOM = of("info/layout/absolute/align_v_bottom.svg");
	public static ImageDescriptor LAYOUT_ABSOLUTE_ALIGN_V_CENTER_DISABLED = of("info/layout/absolute/align_v_center_disabled.svg");
	public static ImageDescriptor LAYOUT_ABSOLUTE_ALIGN_V_CENTER = of("info/layout/absolute/align_v_center.svg");
	public static ImageDescriptor LAYOUT_ABSOLUTE_ALIGN_V_CENTERS_DISABLED = of("info/layout/absolute/align_v_centers_disabled.svg");
	public static ImageDescriptor LAYOUT_ABSOLUTE_ALIGN_V_CENTERS = of("info/layout/absolute/align_v_centers.svg");
	public static ImageDescriptor LAYOUT_ABSOLUTE_ALIGN_V_SPACE_DISABLED = of("info/layout/absolute/align_v_space_disabled.svg");
	public static ImageDescriptor LAYOUT_ABSOLUTE_ALIGN_V_SPACE = of("info/layout/absolute/align_v_space.svg");
	public static ImageDescriptor LAYOUT_ABSOLUTE_ALIGN_V_TOP_DISABLED = of("info/layout/absolute/align_v_top_disabled.svg");
	public static ImageDescriptor LAYOUT_ABSOLUTE_ALIGN_V_TOP = of("info/layout/absolute/align_v_top.svg");
	public static ImageDescriptor LAYOUT_ABSOLUTE_ALIGN_WIDTH_DISABLED = of("info/layout/absolute/align_width_disabled.svg");
	public static ImageDescriptor LAYOUT_ABSOLUTE_ALIGN_WIDTH = of("info/layout/absolute/align_width.svg");
	public static ImageDescriptor LAYOUT_ABSOLUTE_BRING_FORWARD = of("info/layout/absolute/bring_forward.svg");
	public static ImageDescriptor LAYOUT_ABSOLUTE_BRING_TO_FRONT = of("info/layout/absolute/bring_to_front.svg");
	public static ImageDescriptor LAYOUT_ABSOLUTE_SEND_BACKWARD = of("info/layout/absolute/send_backward.svg");
	public static ImageDescriptor LAYOUT_ABSOLUTE_SEND_TO_BACK = of("info/layout/absolute/send_to_back.svg");
	public static ImageDescriptor LAYOUT_ABSOLUTE = of("info/layout/absolute/layout.svg");

	public static ImageDescriptor LAYOUT_FIT_TO_SIZE = of("info/layout/fit_to_size.svg");

	/**
	 * Images returned by this method must <b>not</b> be disposed.
	 *
	 * @return A shared {@link Image} of the given descriptor.
	 */
	public static Image getSharedImage(ImageDescriptor imageDescriptor) {
		return RESOURCE_MANAGER.create(imageDescriptor);
	}

	private static ImageDescriptor of(String fileName) {
		return of(FrameworkUtil.getBundle(CoreImages.class), fileName);
	}

	/**
	 * Utility method for easily accessing all images identified by this class. The
	 * images are expected to be in the {@code icons} folder of the given bundle.
	 * For convenience, subclasses should declare their own {@link #of(String)}
	 * method, using the bundle of containing the class as reference.
	 */
	protected static ImageDescriptor of(Bundle bundle, String fileName) {
		String effectiveFileName;
		if (!DesignerPlugin.isSvgSupported() && fileName.endsWith(".svg")) { //$NON-NLS-1$
			effectiveFileName = fileName.replaceFirst("\\.svg$", ".png"); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			effectiveFileName = fileName;
		}
		return ExecutionUtils.runObjectLog(() -> {
			URI uri = URI.create("platform:/plugin/" + bundle.getSymbolicName() + "/icons/" + effectiveFileName);
			URL url = uri.toURL();
			return ImageDescriptor.createFromURL(url);
		}, ImageDescriptor.getMissingImageDescriptor());
	}
}
