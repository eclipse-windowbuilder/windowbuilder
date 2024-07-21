/*******************************************************************************
 * Copyright (c) 2024 Patrick Ziegler and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Patrick Ziegler - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.core.editor.constants;

import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;

import org.eclipse.jface.resource.ImageDescriptor;

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
	public static ImageDescriptor ALIGNMENT_H_MENU_CENTER = of("alignment/h/menu/center.png");
	public static ImageDescriptor ALIGNMENT_H_MENU_DEFAULT = of("alignment/h/menu/default.png");
	public static ImageDescriptor ALIGNMENT_H_MENU_DELETE = of("alignment/h/menu/delete.png");
	public static ImageDescriptor ALIGNMENT_H_MENU_FILL = of("alignment/h/menu/fill.png");
	public static ImageDescriptor ALIGNMENT_H_MENU_GROW = of("alignment/h/menu/grow.png");
	public static ImageDescriptor ALIGNMENT_H_MENU_LEADING = of("alignment/h/menu/leading.png");
	public static ImageDescriptor ALIGNMENT_H_MENU_LEFT = of("alignment/h/menu/left.png");
	public static ImageDescriptor ALIGNMENT_H_MENU_RIGHT = of("alignment/h/menu/right.png");
	public static ImageDescriptor ALIGNMENT_H_MENU_TRAILING = of("alignment/h/menu/trailing.png");
	public static ImageDescriptor ALIGNMENT_H_MENU_UNKNOWN = of("alignment/h/menu/unknown.png");

	public static ImageDescriptor ALIGNMENT_H_SMALL_CENTER = of("alignment/h/small/center.png");
	public static ImageDescriptor ALIGNMENT_H_SMALL_DEFAULT = of("alignment/h/small/default.png");
	public static ImageDescriptor ALIGNMENT_H_SMALL_FILL = of("alignment/h/small/fill.png");
	public static ImageDescriptor ALIGNMENT_H_SMALL_GROW = of("alignment/h/small/grow.png");
	public static ImageDescriptor ALIGNMENT_H_SMALL_LEADING = of("alignment/h/small/leading.png");
	public static ImageDescriptor ALIGNMENT_H_SMALL_LEFT = of("alignment/h/small/left.png");
	public static ImageDescriptor ALIGNMENT_H_SMALL_RIGHT = of("alignment/h/small/right.png");
	public static ImageDescriptor ALIGNMENT_H_SMALL_TRAILING = of("alignment/h/small/trailing.png");
	public static ImageDescriptor ALIGNMENT_H_SMALL_UNKNOWN = of("alignment/h/small/unknown.png");

	public static ImageDescriptor ALIGNMENT_V_MENU_BOTTOM = of("alignment/v/menu/bottom.png");
	public static ImageDescriptor ALIGNMENT_V_MENU_CENTER = of("alignment/v/menu/center.png");
	public static ImageDescriptor ALIGNMENT_V_MENU_DEFAULT = of("alignment/v/menu/default.png");
	public static ImageDescriptor ALIGNMENT_V_MENU_DELETE = of("alignment/v/menu/delete.png");
	public static ImageDescriptor ALIGNMENT_V_MENU_FILL = of("alignment/v/menu/fill.png");
	public static ImageDescriptor ALIGNMENT_V_MENU_GROW = of("alignment/v/menu/grow.png");
	public static ImageDescriptor ALIGNMENT_V_MENU_TOP = of("alignment/v/menu/top.png");
	public static ImageDescriptor ALIGNMENT_V_MENU_UNKNOWN = of("alignment/v/menu/unknown.png");

	public static ImageDescriptor ALIGNMENT_V_SMALL_BOTTOM = of("alignment/v/small/bottom.png");
	public static ImageDescriptor ALIGNMENT_V_SMALL_CENTER = of("alignment/v/small/center.png");
	public static ImageDescriptor ALIGNMENT_V_SMALL_DEFAULT = of("alignment/v/small/default.png");
	public static ImageDescriptor ALIGNMENT_V_SMALL_FILL = of("alignment/v/small/fill.png");
	public static ImageDescriptor ALIGNMENT_V_SMALL_GROW = of("alignment/v/small/grow.png");
	public static ImageDescriptor ALIGNMENT_V_SMALL_TOP = of("alignment/v/small/top.png");
	public static ImageDescriptor ALIGNMENT_V_SMALL_UNKNOWN = of("alignment/v/small/unknown.png");

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
		return ExecutionUtils.runObjectLog(() -> {
			URI uri = URI.create("platform:/plugin/" + bundle.getSymbolicName() + "/icons/" + fileName);
			URL url = uri.toURL();
			return ImageDescriptor.createFromURL(url);
		}, ImageDescriptor.getMissingImageDescriptor());
	}
}
