/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.xwt.model.property.editor.color;

import org.eclipse.wb.internal.core.utils.ui.dialogs.color.ColorInfo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.PlatformUI;
import org.eclipse.xwt.XWTMaps;
import org.eclipse.xwt.utils.NamedColorsUtil;

import org.apache.commons.lang.StringUtils;

/**
 * Helper for working with {@link Color}.
 *
 * @author scheglov_ke
 * @coverage XWT.support
 */
public class ColorSupport {
	////////////////////////////////////////////////////////////////////////////
	//
	// System colors
	//
	////////////////////////////////////////////////////////////////////////////
	private static ColorInfo[] m_systemColors;

	/**
	 * @return the {@link ColorInfo}s for {@link SWT} constants.
	 */
	public static synchronized ColorInfo[] getSystemColors() {
		if (m_systemColors == null) {
			String[] names = XWTMaps.getColorKeys().toArray(new String[0]);
			m_systemColors = new ColorInfo[names.length];
			for (int i = 0; i < names.length; i++) {
				String name = names[i];
				// prepare Color
				Color color;
				{
					int id = XWTMaps.getColor(name);
					color = PlatformUI.getWorkbench().getDisplay().getSystemColor(id);
				}
				// create ColorInfo
				{
					name = StringUtils.removeStart(name, "SWT.");
					ColorInfo colorInfo = new ColorInfo(name, color.getRGB());
					colorInfo.setData(name);
					m_systemColors[i] = colorInfo;
				}
			}
		}
		return m_systemColors;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Named colors
	//
	////////////////////////////////////////////////////////////////////////////
	private static ColorInfo[] m_namedColors;

	/**
	 * @return the {@link ColorInfo}s for named color constants in XWT.
	 */
	public static synchronized ColorInfo[] getNamedColors() {
		if (m_namedColors == null) {
			String[] names = NamedColorsUtil.getColorNames();
			m_namedColors = new ColorInfo[names.length];
			for (int i = 0; i < names.length; i++) {
				String name = names[i];
				Color color = NamedColorsUtil.getColor(name);
				{
					ColorInfo colorInfo = new ColorInfo(name, color.getRGB());
					colorInfo.setData(name);
					m_namedColors[i] = colorInfo;
				}
			}
		}
		return m_namedColors;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return red component of SWT {@link Color} object.
	 */
	private static int getRed(Color color) throws Exception {
		return color.getRed();
	}

	/**
	 * @return green component of SWT {@link Color} object.
	 */
	private static int getGreen(Color color) throws Exception {
		return color.getGreen();
	}

	/**
	 * @return blue component of SWT {@link Color} object.
	 */
	private static int getBlue(Color color) throws Exception {
		return color.getBlue();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return new {@link ColorInfo} using {@link Color}.
	 */
	public static ColorInfo createInfo(Color color) throws Exception {
		return new ColorInfo(getRed(color), getGreen(color), getBlue(color));
	}

	/**
	 * Create string presentation of {@link Color}.
	 */
	public static String toString(Color color) throws Exception {
		return getRed(color) + "," + getGreen(color) + "," + getBlue(color);
	}
}