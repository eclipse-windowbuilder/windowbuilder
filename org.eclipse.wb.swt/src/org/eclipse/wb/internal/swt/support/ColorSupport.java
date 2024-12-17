/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.swt.support;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.ui.dialogs.color.ColorInfo;

import org.eclipse.swt.graphics.Color;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

/**
 * Stub class for using SWT {@link Color} in another {@link ClassLoader}.
 *
 * @author lobas_av
 * @coverage swt.support
 */
public class ColorSupport {
	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return red component of SWT {@link Color} object.
	 */
	private static int getRed(Object color) throws Exception {
		return (Integer) ReflectionUtils.invokeMethod(color, "getRed()");
	}

	/**
	 * @return green component of SWT {@link Color} object.
	 */
	private static int getGreen(Object color) throws Exception {
		return (Integer) ReflectionUtils.invokeMethod(color, "getGreen()");
	}

	/**
	 * @return blue component of SWT {@link Color} object.
	 */
	private static int getBlue(Object color) throws Exception {
		return (Integer) ReflectionUtils.invokeMethod(color, "getBlue()");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the copy of SWT {@link Color}.
	 */
	public static Object getCopy(Object color) throws Exception {
		Constructor<?> constructor =
				ReflectionUtils.getConstructorBySignature(
						color.getClass(),
						"<init>(org.eclipse.swt.graphics.Device,int,int,int)");
		return constructor.newInstance(null, getRed(color), getGreen(color), getBlue(color));
	}

	/**
	 * Convert SWT {@link Color} to SWT {@link Color}.
	 */
	public static Color getColor(Object color) throws Exception {
		return new Color(null, getRed(color), getGreen(color), getBlue(color));
	}

	/**
	 * Create string presentation of {@link Color}.
	 */
	public static String toString(Object color) throws Exception {
		return getRed(color) + "," + getGreen(color) + "," + getBlue(color);
	}

	/**
	 * @return <code>true</code> if given {@link Color} is disposed.
	 */
	public static boolean isDisposed(Object color) throws Exception {
		return (Boolean) ReflectionUtils.invokeMethod(color, "isDisposed()");
	}

	/**
	 * Invoke method <code>Color.dispose()</code> for color if it not disposed.
	 */
	public static void dispose(Object color) throws Exception {
		if (!isDisposed(color)) {
			ReflectionUtils.invokeMethod(color, "dispose()");
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ColorInfo
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return new {@link ColorInfo} using color RGB.
	 */
	public static ColorInfo createInfo(Object color) throws Exception {
		return new ColorInfo(getRed(color), getGreen(color), getBlue(color));
	}

	/**
	 * @return new {@link ColorInfo} using color RGB.
	 */
	public static ColorInfo createInfo(String name, Object color) throws Exception {
		return new ColorInfo(name, getRed(color), getGreen(color), getBlue(color));
	}

	/**
	 * @return new {@link ColorInfo} using <code>Display.getSystemColor()</code> key.
	 */
	public static ColorInfo createInfo(Field field) throws Exception {
		String name = field.getName();
		Color color = DesignerPlugin.getStandardDisplay().getSystemColor((int) field.get(null));
		ColorInfo colorInfo = createInfo(name, color);
		colorInfo.setData("org.eclipse.swt.SWT." + name);
		return colorInfo;
	}
}