/************************************************************************
 *                                                                      *
 *  DDDD     SSSS    AAA        Daten- und Systemtechnik Aachen GmbH    *
 *  D   D   SS      A   A       Pascalstrasse 28                        *
 *  D   D    SSS    AAAAA       52076 Aachen-Oberforstbach, Germany     *
 *  D   D      SS   A   A       Telefon: +49 (0)2408 / 9492-0           *
 *  DDDD    SSSS    A   A       Telefax: +49 (0)2408 / 9492-92          *
 *                                                                      *
 *                                                                      *
 *  (c) Copyright by DSA - all rights reserved                          *
 *                                                                      *
 ************************************************************************
 *
 * Initial Creation:
 *    Author      S4
 *    Created on  22 Jun 2022
 *
 ************************************************************************/
package org.eclipse.wb.internal.swt.model.property.editor.color;

import org.eclipse.wb.core.editor.color.ColorChooserPreferences;
import org.eclipse.wb.core.editor.constants.IColorChooserPreferenceConstants;

import org.eclipse.jface.resource.ImageDescriptor;

import org.osgi.service.prefs.BackingStoreException;

public class SwtColorChooserPreferences extends ColorChooserPreferences {
	String preferencePrefix = "SWT";
	public SwtColorChooserPreferences() {
		setPrefPrefix(preferencePrefix);
		setDefaultPreferenceMap();
	}

	public SwtColorChooserPreferences(String title) {
		super(title);
		setPrefPrefix(preferencePrefix);
		setDefaultPreferenceMap();
	}

	/**
	 * This annotation is requried to instruct WindowBuilder which constructor to
	 * run
	 *
	 * @wbp.parser.constructor
	 */
	public SwtColorChooserPreferences(String title, ImageDescriptor image) {
		super(title, image);
		setPrefPrefix(preferencePrefix);
		setDefaultPreferenceMap();
		setPersistedPreferencesToUI();
	}

	@Override
	protected void performApply() {
		super.performApply();
		// This ColorPropertyEditor could be null therefore a check is performed.
		// This method should be in the ColorPropertyEditor itself, but for some reason
		// it seems that
		// org.osgi.service.prefs.Preferences no longer has a propertyChange method.
		if (ColorPropertyEditor.INSTANCE != null) {
			ColorPropertyEditor.reloadColorDialog();
		}
		try {
			prefs.flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
	}

	private void setDefaultPreferenceMap() {
		defaultPreferences.put(prefPrefix + IColorChooserPreferenceConstants.P_CUSTOM_COLORS, true);
		defaultPreferences.put(prefPrefix + IColorChooserPreferenceConstants.P_SYSTEM_COLORS, true);
		defaultPreferences.put(prefPrefix + IColorChooserPreferenceConstants.P_AWT_COLORS, true);
		defaultPreferences.put(prefPrefix + IColorChooserPreferenceConstants.P_NAMED_COLORS, true);
		defaultPreferences.put(prefPrefix + IColorChooserPreferenceConstants.P_WEB_SAFE_COLORS, true);

	}

}
