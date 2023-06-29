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
package org.eclipse.wb.internal.swing.model.property.editor.color;

import org.eclipse.wb.core.editor.color.ColorChooserPreferences;
import org.eclipse.wb.core.editor.constants.IColorChooserPreferenceConstants;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Group;

import org.osgi.service.prefs.BackingStoreException;

public class SwingColorChooserPreferences extends ColorChooserPreferences {
	Button cbxIncludeSwingColors;
	Button cbxIncludeAwtColors;
	String preferencePrefix = "SWING";

	public SwingColorChooserPreferences() {
		setPrefPrefix(preferencePrefix);
		setDefaultPreferenceMap();

	}

	public SwingColorChooserPreferences(String title) {
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
	public SwingColorChooserPreferences(String title, ImageDescriptor image) {
		super(title, image);
		setPrefPrefix(preferencePrefix);
		setPersistedPreferencesToUI();
	}

	@Override
	protected void createPreferenceCheckboxes(Group groupLayout) {
		super.createPreferenceCheckboxes(groupLayout);
		cbxIncludeSwingColors = new Button(groupLayout, SWT.CHECK);
		cbxIncludeSwingColors.setText(IColorChooserPreferenceConstants.SWING_COLORS);
		cbxIncludeAwtColors = new Button(groupLayout, SWT.CHECK);
		cbxIncludeAwtColors.setText(IColorChooserPreferenceConstants.AWT_COLORS);
	}

	@Override
	protected void setPersistedPreferencesToUI() {
		super.setPersistedPreferencesToUI();
		cbxIncludeSwingColors
		.setSelection(prefs.getBoolean(prefPrefix + IColorChooserPreferenceConstants.P_SWING_COLORS,
				defaultPreferences.get(prefPrefix + IColorChooserPreferenceConstants.P_SWING_COLORS)));
		cbxIncludeAwtColors.setSelection(prefs.getBoolean(prefPrefix + IColorChooserPreferenceConstants.P_AWT_COLORS,
				defaultPreferences.get(prefPrefix + IColorChooserPreferenceConstants.P_AWT_COLORS)));

	}

	@Override
	protected void performApply() {
		super.performApply();
		prefs.putBoolean(prefPrefix + IColorChooserPreferenceConstants.P_SWING_COLORS,
				cbxIncludeSwingColors.getSelection());
		prefs.putBoolean(prefPrefix + IColorChooserPreferenceConstants.P_AWT_COLORS,
				cbxIncludeAwtColors.getSelection());
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

	@Override
	protected void performDefaults() {
		super.performDefaults();
		cbxIncludeSwingColors
		.setSelection(defaultPreferences.get(prefPrefix + IColorChooserPreferenceConstants.P_SWING_COLORS));
		cbxIncludeAwtColors
		.setSelection(defaultPreferences.get(prefPrefix + IColorChooserPreferenceConstants.P_AWT_COLORS));

		// Update the preferences to the default values
		setPreferenceDefaults();
	}

	@Override
	protected void setPreferenceDefaults() {
		super.setPreferenceDefaults();
		prefs.putBoolean(prefPrefix + IColorChooserPreferenceConstants.P_SWING_COLORS,
				defaultPreferences.get(prefPrefix + IColorChooserPreferenceConstants.P_SWING_COLORS));
		prefs.putBoolean(prefPrefix + IColorChooserPreferenceConstants.P_AWT_COLORS,
				defaultPreferences.get(prefPrefix + IColorChooserPreferenceConstants.P_AWT_COLORS));
	}

	private void setDefaultPreferenceMap() {
		defaultPreferences.put(prefPrefix + IColorChooserPreferenceConstants.P_SWING_COLORS, true);
		defaultPreferences.put(prefPrefix + IColorChooserPreferenceConstants.P_CUSTOM_COLORS, true);
		defaultPreferences.put(prefPrefix + IColorChooserPreferenceConstants.P_SYSTEM_COLORS, true);
		defaultPreferences.put(prefPrefix + IColorChooserPreferenceConstants.P_AWT_COLORS, true);
		defaultPreferences.put(prefPrefix + IColorChooserPreferenceConstants.P_NAMED_COLORS, true);
		defaultPreferences.put(prefPrefix + IColorChooserPreferenceConstants.P_WEB_SAFE_COLORS, true);
	}

}
