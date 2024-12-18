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

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/**
 * Stub class for using SWT {@link Font} in another {@link ClassLoader}.
 *
 * @author lobas_av
 * @coverage swt.support
 */
public class FontSupport extends AbstractSupport {
	////////////////////////////////////////////////////////////////////////////
	//
	// Font
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link Class} of {@link Font}.
	 */
	public static Class<?> getFontClass() {
		return loadClass("org.eclipse.swt.graphics.Font");
	}

	/**
	 * Create new font <code>new Font(null, name, size, style)</code>.
	 */
	public static Object createFont(String name, int size, Object style) throws Exception {
		Constructor<?> constructor =
				ReflectionUtils.getConstructorBySignature(
						getFontClass(),
						"<init>(org.eclipse.swt.graphics.Device,java.lang.String,int,int)");
		return constructor.newInstance(null, name, size, style);
	}

	/**
	 * @return the copy of given {@link Font}.
	 */
	public static Object getCopy(Object font) throws Exception {
		Object fontDataArray = ReflectionUtils.invokeMethod2(font, "getFontData");
		return ReflectionUtils.getConstructorBySignature(
				getFontClass(),
				"<init>(org.eclipse.swt.graphics.Device,org.eclipse.swt.graphics.FontData[])").newInstance(
						DesignerPlugin.getStandardDisplay(),
						fontDataArray);
	}

	/**
	 * @return <code>true</code> if given {@link Font} is disposed.
	 */
	public static boolean isDisposed(Object font) throws Exception {
		return (Boolean) ReflectionUtils.invokeMethod(font, "isDisposed()");
	}

	/**
	 * Invoke method <code>Font.dispose()</code> for font if it not disposed.
	 */
	public static void dispose(Object font) throws Exception {
		if (!isDisposed(font)) {
			ReflectionUtils.invokeMethod(font, "dispose()");
		}
	}

	/**
	 * @return names of all fonts into system.
	 */
	public static String[] getFontFamilies() throws Exception {
		Set<String> families = new TreeSet<>();
		// add all font families
		Collections.addAll(families, ToolkitSupport.getFontFamilies(false));
		Collections.addAll(families, ToolkitSupport.getFontFamilies(true));
		// add default font
		families.add(getFontName(getFontData(DesignerPlugin.getStandardDisplay().getSystemFont())));
		// sort names
		String[] sortFamilies = families.toArray(new String[families.size()]);
		Arrays.sort(sortFamilies);
		return sortFamilies;
	}

	/**
	 * Invoke method <code>Font.getFontData()</code> for given font.
	 */
	public static Object getFontData(Object font) throws Exception {
		Object/*<FontData[]>*/fontData = ReflectionUtils.invokeMethod(font, "getFontData()");
		return Array.get(fontData, 0);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// FontData
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Invoke method <code>FontData.getName()</code> for given font data.
	 */
	public static String getFontName(Object fontData) throws Exception {
		return (String) ReflectionUtils.invokeMethod(fontData, "getName()");
	}

	/**
	 * Invoke method <code>FontData.getHeight()</code> for given font data.
	 */
	public static int getFontSize(Object fontData) throws Exception {
		return (Integer) ReflectionUtils.invokeMethod(fontData, "getHeight()");
	}

	/**
	 * Invoke method <code>FontData.getStyle()</code> for given font data.
	 */
	public static int getFontStyle(Object fontData) throws Exception {
		return (Integer) ReflectionUtils.invokeMethod(fontData, "getStyle()");
	}

	/**
	 * @return source code of style: e.x., <code>SWT.BOLD</code>.
	 */
	public static String getFontStyleSource(Object fontData) throws Exception {
		int style = getFontStyle(fontData);
		boolean bold = (style & SWT.BOLD) != 0;
		boolean italic = (style & SWT.ITALIC) != 0;
		if (bold && italic) {
			return "org.eclipse.swt.SWT.BOLD | org.eclipse.swt.SWT.ITALIC";
		}
		if (bold) {
			return "org.eclipse.swt.SWT.BOLD";
		}
		if (italic) {
			return "org.eclipse.swt.SWT.ITALIC";
		}
		return "org.eclipse.swt.SWT.NORMAL";
	}

	/**
	 * @return text for style: e.x., <code>BOLD</code>.
	 */
	public static String getFontStyleText(Object fontData) throws Exception {
		int style = getFontStyle(fontData);
		boolean bold = (style & SWT.BOLD) != 0;
		boolean italic = (style & SWT.ITALIC) != 0;
		if (bold && italic) {
			return "BOLD ITALIC";
		}
		if (bold) {
			return "BOLD";
		}
		if (italic) {
			return "ITALIC";
		}
		return "";
	}
}