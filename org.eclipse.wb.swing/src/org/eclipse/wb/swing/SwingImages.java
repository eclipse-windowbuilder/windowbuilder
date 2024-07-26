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
package org.eclipse.wb.swing;

import org.eclipse.wb.core.editor.constants.CoreImages;

import org.eclipse.jface.resource.ImageDescriptor;

import org.osgi.framework.FrameworkUtil;

/**
 * Utility class that contains all images that are used accross all Swing
 * components. Each image is stored as a PNG and exists in normal (96DPI) and
 * high(192DPI) resolution.
 *
 * @noreference This class is not intended to be referenced by clients.
 * @noextend This class is not intended to be extended by clients.
 */
public class SwingImages extends CoreImages {
	public static ImageDescriptor ALIGNMENT_V_MENU_BASELINE_ABOVE = of("alignment/v/menu/baseline_above.png");
	public static ImageDescriptor ALIGNMENT_V_MENU_BASELINE_BELOW = of("alignment/v/menu/baseline_below.png");
	public static ImageDescriptor ALIGNMENT_V_MENU_BASELINE = of("alignment/v/menu/baseline.png");

	public static ImageDescriptor ALIGNMENT_V_SMALL_BASELINE_ABOVE = of("alignment/v/small/baseline_above.png");
	public static ImageDescriptor ALIGNMENT_V_SMALL_BASELINE_BELOW = of("alignment/v/small/baseline_below.png");
	public static ImageDescriptor ALIGNMENT_V_SMALL_BASELINE = of("alignment/v/small/baseline.png");

	public static ImageDescriptor NAVIGATION_UP = of("navigation/up.png");
	public static ImageDescriptor NAVIGATION_DOWN = of("navigation/down.png");
	public static ImageDescriptor NAVIGATION_LEFT = of("navigation/left.png");
	public static ImageDescriptor NAVIGATION_RIGHT = of("navigation/right.png");

	private static ImageDescriptor of(String fileName) {
		return of(FrameworkUtil.getBundle(SwingImages.class), fileName);
	}
}
