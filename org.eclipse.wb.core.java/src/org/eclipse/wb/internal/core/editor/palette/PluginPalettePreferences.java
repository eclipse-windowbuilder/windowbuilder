/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
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
package org.eclipse.wb.internal.core.editor.palette;

import org.eclipse.wb.core.controls.palette.DesignerPaletteViewerPreferences;
import org.eclipse.wb.core.controls.palette.ICategory;
import org.eclipse.wb.core.controls.palette.IEntry;
import org.eclipse.wb.core.controls.palette.IPalettePreferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;

import java.util.Objects;

/**
 * Implementation of {@link IPalettePreferences} for {@link IPreferenceStore}.
 *
 * @author scheglov_ke
 * @coverage core.editor.palette.ui
 */
public final class PluginPalettePreferences extends DesignerPaletteViewerPreferences {
	private FontDescriptor m_categoryFont;
	private FontDescriptor m_entryFont;

	////////////////////////////////////////////////////////////////////////////
	//
	// Keys
	//
	////////////////////////////////////////////////////////////////////////////
	private String m_categoryFontKey;
	private String m_entryFontKey;
	private String m_minColumnsKey;

	/**
	 * Sets the prefix for preference keys.
	 */
	public void setPrefix(String prefix) {
		// prepare keys
		m_categoryFontKey = prefix + ".category.font";
		m_entryFontKey = prefix + ".entry.font";
		m_minColumnsKey = prefix + ".columns.min";
		// set default values
		{
			{
				FontData defaultFontData = Display.getDefault().getSystemFont().getFontData()[0];
				FontData boldFontData =
						new FontData(defaultFontData.getName(), defaultFontData.getHeight(), SWT.BOLD);
				PreferenceConverter.setDefault(getPreferenceStore(), m_categoryFontKey, boldFontData);
			}
			{
				FontData[] defaultFontData = Display.getDefault().getSystemFont().getFontData();
				PreferenceConverter.setDefault(getPreferenceStore(), m_entryFontKey, defaultFontData);
			}
		}
		getPreferenceStore().setDefault(m_minColumnsKey, 2);
	}

	@Override
	protected void handlePreferenceStorePropertyChanged(String property) {
		if (property.equals(m_categoryFontKey)) {
			firePropertyChanged(property, getCategoryFontDescriptor());
		} else if (property.equals(m_entryFontKey)) {
			firePropertyChanged(property, getEntryFontDescriptor());
		} else if (property.equals(m_minColumnsKey)) {
			firePropertyChanged(property, getMinColumns());
		} else {
			super.handlePreferenceStorePropertyChanged(property);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IPalettePreferences
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public FontDescriptor getCategoryFontDescriptor() {
		if (m_categoryFont == null) {
			FontData[] fontDataArray = PreferenceConverter.getFontDataArray(getPreferenceStore(), m_categoryFontKey);
			m_categoryFont = FontDescriptor.createFrom(fontDataArray);
		}
		return m_categoryFont;
	}

	@Override
	public FontDescriptor getEntryFontDescriptor() {
		if (m_entryFont == null) {
			FontData[] fontDataArray = PreferenceConverter.getFontDataArray(getPreferenceStore(), m_entryFontKey);
			m_entryFont = FontDescriptor.createFrom(fontDataArray);
		}
		return m_entryFont;
	}

	@Override
	public int getMinColumns() {
		return getPreferenceStore().getInt(m_minColumnsKey);
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
		PreferenceConverter.setValue(getPreferenceStore(), m_categoryFontKey, fontDataArray);
		m_categoryFont = null;
	}

	/**
	 * Sets the {@link FontData} for {@link IEntry}.
	 */
	public void setEntryFont(FontData[] fontDataArray) {
		PreferenceConverter.setValue(getPreferenceStore(), m_entryFontKey, fontDataArray);
		m_entryFont = null;
	}

	/**
	 * Sets the minimal number of columns for {@link ICategory}.
	 */
	public void setMinColumns(int minColumns) {
		getPreferenceStore().setValue(m_minColumnsKey, minColumns);
	}

	/**
	 * @return {@code true} if the given key matches {@link #m_categoryFontKey}
	 */
	public boolean isCategoryPropertyKey(String key) {
		return Objects.equals(key, m_categoryFontKey);
	}

	/**
	 * @return {@code true} if the given key matches {@link #m_entryFontKey}
	 */
	public boolean isEntryPropertyKey(String key) {
		return Objects.equals(key, m_entryFontKey);
	}
}
