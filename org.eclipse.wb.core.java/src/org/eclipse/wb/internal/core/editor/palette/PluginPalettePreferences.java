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
package org.eclipse.wb.internal.core.editor.palette;

import org.eclipse.wb.core.controls.palette.ICategory;
import org.eclipse.wb.core.controls.palette.IEntry;
import org.eclipse.wb.core.controls.palette.IPalettePreferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;

/**
 * Implementation of {@link IPalettePreferences} for {@link IPreferenceStore}.
 *
 * @author scheglov_ke
 * @coverage core.editor.palette.ui
 */
public final class PluginPalettePreferences implements IPalettePreferences {
	private final IPreferenceStore m_store;
	private FontDescriptor m_categoryFont;
	private FontDescriptor m_entryFont;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public PluginPalettePreferences(IPreferenceStore store) {
		m_store = store;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Keys
	//
	////////////////////////////////////////////////////////////////////////////
	private String m_categoryFontKey;
	private String m_entryFontKey;
	private String m_onlyIconsKey;
	private String m_minColumnsKey;
	private String m_layoutsKey;

	/**
	 * Sets the prefix for preference keys.
	 */
	public void setPrefix(String prefix) {
		// prepare keys
		m_categoryFontKey = prefix + ".category.font";
		m_entryFontKey = prefix + ".entry.font";
		m_onlyIconsKey = prefix + ".onlyIcons";
		m_minColumnsKey = prefix + ".columns.min";
		m_layoutsKey = prefix + ".layouts.type";
		// set default values
		{
			{
				FontData defaultFontData = Display.getDefault().getSystemFont().getFontData()[0];
				FontData boldFontData =
						new FontData(defaultFontData.getName(), defaultFontData.getHeight(), SWT.BOLD);
				PreferenceConverter.setDefault(m_store, m_categoryFontKey, boldFontData);
			}
			{
				FontData[] defaultFontData = Display.getDefault().getSystemFont().getFontData();
				PreferenceConverter.setDefault(m_store, m_entryFontKey, defaultFontData);
			}
		}
		m_store.setDefault(m_onlyIconsKey, false);
		m_store.setDefault(m_minColumnsKey, 2);
		m_store.setDefault(m_layoutsKey, DesignerPalette.LIST_ICONS_TYPE);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IPalettePreferences
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public FontDescriptor getCategoryFontDescriptor() {
		if (m_categoryFont == null) {
			FontData[] fontDataArray = PreferenceConverter.getFontDataArray(m_store, m_categoryFontKey);
			m_categoryFont = FontDescriptor.createFrom(fontDataArray);
		}
		return m_categoryFont;
	}

	@Override
	public FontDescriptor getEntryFontDescriptor() {
		if (m_entryFont == null) {
			FontData[] fontDataArray = PreferenceConverter.getFontDataArray(m_store, m_entryFontKey);
			m_entryFont = FontDescriptor.createFrom(fontDataArray);
		}
		return m_entryFont;
	}

	@Override
	public boolean isOnlyIcons() {
		return m_store.getBoolean(m_onlyIconsKey);
	}

	@Override
	public int getMinColumns() {
		return m_store.getInt(m_minColumnsKey);
	}

	@Override
	public int getLayoutType() {
		return m_store.getInt(m_layoutsKey);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Sets the {@link FontData} for {@link ICategory}.
	 */
	public void setCategoryFont(FontData[] fontDataArray) {
		PreferenceConverter.setValue(m_store, m_categoryFontKey, fontDataArray);
	}

	/**
	 * Sets the {@link FontData} for {@link IEntry}.
	 */
	public void setEntryFont(FontData[] fontDataArray) {
		PreferenceConverter.setValue(m_store, m_entryFontKey, fontDataArray);
	}

	/**
	 * Specifies if only icons should be displayed for {@link IEntry}'s.
	 */
	public void setOnlyIcons(boolean onlyIcons) {
		m_store.setValue(m_onlyIconsKey, onlyIcons);
	}

	/**
	 * Sets the minimal number of columns for {@link ICategory}.
	 */
	public void setMinColumns(int minColumns) {
		m_store.setValue(m_minColumnsKey, minColumns);
	}

	/**
	 * Sets the minimal number of columns for {@link ICategory}.
	 */
	public void setLayoutType(int layoutTypes) {
		m_store.setValue(m_layoutsKey, layoutTypes);
	}
}
