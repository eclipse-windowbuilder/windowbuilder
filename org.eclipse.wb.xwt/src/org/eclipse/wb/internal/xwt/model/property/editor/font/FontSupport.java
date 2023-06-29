/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.xwt.model.property.editor.font;

import com.google.common.collect.Sets;

import org.eclipse.wb.os.OSSupport;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import java.util.Arrays;
import java.util.Set;

/**
 * Helper for working with {@link Font}.
 *
 * @author scheglov_ke
 * @coverage XWT.support
 */
public class FontSupport {
	////////////////////////////////////////////////////////////////////////////
	//
	// Font
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return names of all fonts into system.
	 */
	public static String[] getFontFamilies() throws Exception {
		Set<String> families = Sets.newTreeSet();
		// add all font families
		families.addAll(getFontFamilies(true));
		families.addAll(getFontFamilies(false));
		// sort names
		String[] sortFamilies = families.toArray(new String[families.size()]);
		Arrays.sort(sortFamilies);
		return sortFamilies;
	}

	private static Set<String> getFontFamilies(boolean scalable) throws Exception {
		Set<String> families = Sets.newTreeSet();
		//
		FontData[] fontList = Display.getDefault().getFontList(null, scalable);
		for (FontData fontData : fontList) {
			families.add(fontData.getName());
		}
		//
		return families;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Text
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return text for style: e.x., <code>BOLD</code>.
	 */
	public static String getFontStyleText(int style) throws Exception {
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

	/**
	 * @return source code of style: e.x., <code>BOLD | ITALIC</code>.
	 */
	public static String getFontStyleSource(int style) throws Exception {
		boolean bold = (style & SWT.BOLD) != 0;
		boolean italic = (style & SWT.ITALIC) != 0;
		if (bold && italic) {
			return "BOLD | ITALIC";
		}
		if (bold) {
			return "BOLD";
		}
		if (italic) {
			return "ITALIC";
		}
		return "";
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Preview
	//
	////////////////////////////////////////////////////////////////////////////
	private static final FontPreviewShell m_fontPreviewShell = new FontPreviewShell();

	/**
	 * @return the {@link Image} with graphical presentation of {@link Font}.
	 */
	public static Image getFontPreview(Font font) throws Exception {
		m_fontPreviewShell.updateFont(font);
		return OSSupport.get().makeShot(m_fontPreviewShell);
	}
}