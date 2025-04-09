/*******************************************************************************
 * Copyright (c) 2021, 2021 DSA Daten- und Systemtechnik GmbH. (https://www.dsa.de)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marcel du Preez   - initial implementation
 *******************************************************************************/
package org.eclipse.wb.core.editor.color;

import org.eclipse.wb.core.editor.constants.IColorChooserPreferenceConstants;
import org.eclipse.wb.internal.core.model.ModelMessages;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import org.osgi.service.prefs.Preferences;

import java.util.HashMap;
import java.util.Map;

/**
 * ColorChooserPreferences page allows the user to set which color pickers are available in the
 * ColorPropertyEditor dialog. By default all color pickers are available.
 *
 * @author Marcel du Preez
 *
 */
public class ColorChooserPreferences extends PreferencePage implements IWorkbenchPreferencePage {
	protected Preferences preferences =
			InstanceScope.INSTANCE.getNode(IColorChooserPreferenceConstants.PREFERENCE_NODE);
	protected Preferences prefs = preferences.node(IColorChooserPreferenceConstants.PREFERENCE_NODE_1);
	Button cbxIncludeCustomColor;
	Button cbxIncludeSystemColor;
	Button cbxIncludeNamedColors;
	Button cbxIncludeWebSafeColors;
	protected String prefPrefix = ""; // This prefix will either be "SWT" or "SWING". This will be set in the
	// subclasses and thus distinguish between which preference is to be set
	protected final Map<String, Boolean> defaultPreferences = new HashMap<>();
	//	{
	//    private static final long serialVersionUID = 1L;
	//    {
	//			put(prefPrefix + IColorChooserPreferenceConstants.P_CUSTOM_COLORS, true);
	//			put(prefPrefix + IColorChooserPreferenceConstants.P_SYSTEM_COLORS, true);
	//			put(prefPrefix + IColorChooserPreferenceConstants.P_AWT_COLORS, true);
	//			put(prefPrefix + IColorChooserPreferenceConstants.P_SWING_COLORS, true);
	//			put(prefPrefix + IColorChooserPreferenceConstants.P_NAMED_COLORS, true);
	//			put(prefPrefix + IColorChooserPreferenceConstants.P_WEB_SAFE_COLORS, true);
	//    }
	//  };
	private Group grpInclude;

	public ColorChooserPreferences() {
	}

	public ColorChooserPreferences(String title) {
		super(title);
	}

	/**
	 * This annotation is requried to instruct WindowBuilder which constructor to run
	 *
	 * @wbp.parser.constructor
	 */
	public ColorChooserPreferences(String title, ImageDescriptor image) {
		super(title, image);
		setPersistedPreferencesToUI();
	}

	@Override
	public void init(IWorkbench workbench) {
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout(1, false));
		Composite composite = new Composite(container, SWT.NONE);
		RowLayout rl_composite = new RowLayout(SWT.HORIZONTAL);
		composite.setLayout(rl_composite);
		grpInclude = new Group(composite, SWT.NONE);
		grpInclude.setText(ModelMessages.ColorPreferenceChooser_Include);
		grpInclude.setLayout(new RowLayout(SWT.VERTICAL));
		createPreferenceCheckboxes(grpInclude);
		setPersistedPreferencesToUI();
		return container;
	}

	protected void createPreferenceCheckboxes(Group groupLayout) {
		cbxIncludeCustomColor = new Button(grpInclude, SWT.CHECK);
		cbxIncludeCustomColor.setText(IColorChooserPreferenceConstants.CUSTOM_COLORS);
		cbxIncludeSystemColor = new Button(grpInclude, SWT.CHECK);
		cbxIncludeSystemColor.setText(IColorChooserPreferenceConstants.SYSTEM_COLORS);
		cbxIncludeNamedColors = new Button(grpInclude, SWT.CHECK);
		cbxIncludeNamedColors.setText(IColorChooserPreferenceConstants.NAMED_COLORS);
		cbxIncludeWebSafeColors = new Button(grpInclude, SWT.CHECK);
		cbxIncludeWebSafeColors.setText(IColorChooserPreferenceConstants.WEBSAFE_COLORS);
	}

	//Initialize the check boxes to the persisted preferences
	protected void setPersistedPreferencesToUI() {
		cbxIncludeCustomColor.setSelection(
				prefs.getBoolean(
						prefPrefix + IColorChooserPreferenceConstants.P_CUSTOM_COLORS,
						defaultPreferences.get(prefPrefix + IColorChooserPreferenceConstants.P_CUSTOM_COLORS)));
		cbxIncludeSystemColor.setSelection(
				prefs.getBoolean(
						prefPrefix + IColorChooserPreferenceConstants.P_SYSTEM_COLORS,
						defaultPreferences.get(prefPrefix + IColorChooserPreferenceConstants.P_SYSTEM_COLORS)));
		cbxIncludeNamedColors.setSelection(
				prefs.getBoolean(
						prefPrefix + IColorChooserPreferenceConstants.P_NAMED_COLORS,
						defaultPreferences.get(prefPrefix + IColorChooserPreferenceConstants.P_NAMED_COLORS)));
		cbxIncludeWebSafeColors.setSelection(
				prefs.getBoolean(
						prefPrefix + IColorChooserPreferenceConstants.P_WEB_SAFE_COLORS,
						defaultPreferences.get(prefPrefix + IColorChooserPreferenceConstants.P_WEB_SAFE_COLORS)));
	}

	@Override
	protected void performApply() {
		prefs.putBoolean(
				prefPrefix + IColorChooserPreferenceConstants.P_CUSTOM_COLORS,
				cbxIncludeCustomColor.getSelection());
		prefs.putBoolean(
				prefPrefix + IColorChooserPreferenceConstants.P_SYSTEM_COLORS,
				cbxIncludeSystemColor.getSelection());
		prefs.putBoolean(
				prefPrefix + IColorChooserPreferenceConstants.P_NAMED_COLORS,
				cbxIncludeNamedColors.getSelection());
		prefs.putBoolean(
				prefPrefix + IColorChooserPreferenceConstants.P_WEB_SAFE_COLORS,
				cbxIncludeWebSafeColors.getSelection());
	}

	@Override
	public boolean performOk() {
		performApply();
		return true;
	}

	//Sets the preferences to the default values
	protected void setPreferenceDefaults() {
		prefs.putBoolean(
				prefPrefix + IColorChooserPreferenceConstants.P_CUSTOM_COLORS,
				defaultPreferences.get(prefPrefix + IColorChooserPreferenceConstants.P_CUSTOM_COLORS));
		prefs.putBoolean(
				prefPrefix + IColorChooserPreferenceConstants.P_SYSTEM_COLORS,
				defaultPreferences.get(prefPrefix + IColorChooserPreferenceConstants.P_SYSTEM_COLORS));
		prefs.putBoolean(
				prefPrefix + IColorChooserPreferenceConstants.P_NAMED_COLORS,
				defaultPreferences.get(prefPrefix + IColorChooserPreferenceConstants.P_NAMED_COLORS));
		prefs.putBoolean(
				prefPrefix + IColorChooserPreferenceConstants.P_WEB_SAFE_COLORS,
				defaultPreferences.get(prefPrefix + IColorChooserPreferenceConstants.P_WEB_SAFE_COLORS));
	}

	@Override
	protected void performDefaults() {
		//Update the UI to the default values
		cbxIncludeCustomColor.setSelection(
				defaultPreferences.get(prefPrefix + IColorChooserPreferenceConstants.P_CUSTOM_COLORS));
		cbxIncludeSystemColor.setSelection(
				defaultPreferences.get(prefPrefix + IColorChooserPreferenceConstants.P_SYSTEM_COLORS));
		cbxIncludeNamedColors.setSelection(
				defaultPreferences.get(prefPrefix + IColorChooserPreferenceConstants.P_NAMED_COLORS));
		cbxIncludeWebSafeColors.setSelection(
				defaultPreferences.get(prefPrefix + IColorChooserPreferenceConstants.P_WEB_SAFE_COLORS));

	}

	protected void setPrefPrefix(String prefPrefix) {
		this.prefPrefix = prefPrefix;
	}
}
