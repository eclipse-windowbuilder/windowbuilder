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
	public static ImageDescriptor ALIGNMENT_V_MENU_BASELINE_ABOVE = of("alignment/v/menu/baseline_above.svg");
	public static ImageDescriptor ALIGNMENT_V_MENU_BASELINE_BELOW = of("alignment/v/menu/baseline_below.svg");
	public static ImageDescriptor ALIGNMENT_V_MENU_BASELINE = of("alignment/v/menu/baseline.svg");

	public static ImageDescriptor ALIGNMENT_V_SMALL_BASELINE_ABOVE = of("alignment/v/small/baseline_above.svg");
	public static ImageDescriptor ALIGNMENT_V_SMALL_BASELINE_BELOW = of("alignment/v/small/baseline_below.svg");
	public static ImageDescriptor ALIGNMENT_V_SMALL_BASELINE = of("alignment/v/small/baseline.svg");

	public static ImageDescriptor NAVIGATION_UP = of("navigation/up.svg");
	public static ImageDescriptor NAVIGATION_DOWN = of("navigation/down.svg");
	public static ImageDescriptor NAVIGATION_LEFT = of("navigation/left.svg");
	public static ImageDescriptor NAVIGATION_RIGHT = of("navigation/right.svg");

	private static ImageDescriptor of(String fileName) {
		return of(FrameworkUtil.getBundle(SwingImages.class), fileName);
	}
}
