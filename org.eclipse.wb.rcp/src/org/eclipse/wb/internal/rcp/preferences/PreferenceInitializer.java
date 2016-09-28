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
package org.eclipse.wb.internal.rcp.preferences;

import org.eclipse.wb.internal.core.preferences.IPreferenceConstants;
import org.eclipse.wb.internal.rcp.ToolkitProvider;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Initializer for RCP preferences.
 *
 * @author scheglov_ke
 * @author mitin_aa
 * @coverage rcp.preferences
 */
public final class PreferenceInitializer extends AbstractPreferenceInitializer {
  @Override
  public void initializeDefaultPreferences() {
    IPreferenceStore preferences = ToolkitProvider.DESCRIPTION.getPreferences();
    // general
    preferences.setDefault(IPreferenceConstants.P_GENERAL_HIGHLIGHT_CONTAINERS, true);
    preferences.setDefault(IPreferenceConstants.P_GENERAL_TEXT_SUFFIX, true);
    preferences.setDefault(IPreferenceConstants.P_GENERAL_IMPORTANT_PROPERTIES_AFTER_ADD, false);
    preferences.setDefault(IPreferenceConstants.P_GENERAL_DIRECT_EDIT_AFTER_ADD, true);
    preferences.setDefault(IPreferenceConstants.P_GENERAL_DEFAULT_TOP_WIDTH, 450);
    preferences.setDefault(IPreferenceConstants.P_GENERAL_DEFAULT_TOP_HEIGHT, 300);
    // SWT specific
    preferences.setDefault(
        org.eclipse.wb.internal.swt.preferences.IPreferenceConstants.P_USE_RESOURCE_MANAGER,
        true);
    preferences.setDefault(IPreferenceConstants.P_STYLE_PROPERTY_CASCADE_POPUP, false);
    // variable names
    {
      preferences.setDefault(
          IPreferenceConstants.P_VARIABLE_TEXT_MODE,
          IPreferenceConstants.V_VARIABLE_TEXT_MODE_DEFAULT);
      preferences.setDefault(
          IPreferenceConstants.P_VARIABLE_TEXT_TEMPLATE,
          "${class_acronym}${text}");
      preferences.setDefault(IPreferenceConstants.P_VARIABLE_TEXT_WORDS_LIMIT, 3);
    }
    // NLS
    {
      preferences.setDefault(IPreferenceConstants.P_NLS_AUTO_EXTERNALIZE, true);
      preferences.setDefault(IPreferenceConstants.P_NLS_KEY_AS_STRING_VALUE_ONLY, false);
      preferences.setDefault(IPreferenceConstants.P_NLS_KEY_QUALIFIED_TYPE_NAME, false);
      preferences.setDefault(IPreferenceConstants.P_NLS_KEY_RENAME_WITH_VARIABLE, false);
      preferences.setDefault(IPreferenceConstants.P_NLS_KEY_HAS_STRING_VALUE, false);
      preferences.setDefault(IPreferenceConstants.P_NLS_KEY_AS_VALUE_PREFIX, "*");
    }
    // layout
    preferences.setDefault(IPreferenceConstants.P_LAYOUT_OF_PARENT, false);
    preferences.setDefault(
        org.eclipse.wb.internal.swt.preferences.IPreferenceConstants.P_LAYOUT_NAME_TEMPLATE,
        "${layoutAcronym}_${compositeName}");
    preferences.setDefault(
        org.eclipse.wb.internal.swt.preferences.IPreferenceConstants.P_LAYOUT_DATA_NAME_TEMPLATE,
        "${dataAcronym}_${controlName}");
    // GridLayout
    {
      preferences.setDefault(
          org.eclipse.wb.internal.swt.model.layout.grid.IPreferenceConstants.P_ENABLE_GRAB,
          true);
      preferences.setDefault(
          org.eclipse.wb.internal.swt.model.layout.grid.IPreferenceConstants.P_ENABLE_RIGHT_ALIGNMENT,
          true);
    }
    // Forms API
    {
      preferences.setDefault(
          org.eclipse.wb.internal.rcp.preferences.IPreferenceConstants.FORMS_PAINT_BORDERS,
          true);
      preferences.setDefault(
          org.eclipse.wb.internal.rcp.preferences.IPreferenceConstants.FORMS_ADAPT_CONTROL,
          true);
    }
    // PreferencePage
    {
      preferences.setDefault(
          org.eclipse.wb.internal.rcp.preferences.IPreferenceConstants.PREF_FIELD_USUAL_CODE,
          false);
    }
    // TableWrapLayout
    {
      preferences.setDefault(
          org.eclipse.wb.internal.rcp.model.forms.layout.table.IPreferenceConstants.P_ENABLE_GRAB,
          true);
      preferences.setDefault(
          org.eclipse.wb.internal.rcp.model.forms.layout.table.IPreferenceConstants.P_ENABLE_RIGHT_ALIGNMENT,
          true);
    }
    // FormLayout
    {
      preferences.setDefault(
          org.eclipse.wb.internal.swt.model.layout.form.IPreferenceConstants.PREF_FORMLAYOUT_MODE,
          org.eclipse.wb.internal.swt.model.layout.form.IPreferenceConstants.VAL_FORMLAYOUT_MODE_AUTO);
      // for classic mode
      preferences.setDefault(
          org.eclipse.wb.internal.swt.model.layout.form.IPreferenceConstants.PREF_SNAP_SENS,
          5);
      preferences.setDefault(
          org.eclipse.wb.internal.swt.model.layout.form.IPreferenceConstants.PREF_V_WINDOW_MARGIN,
          12);
      preferences.setDefault(
          org.eclipse.wb.internal.swt.model.layout.form.IPreferenceConstants.PREF_V_PERCENT_OFFSET,
          6);
      preferences.setDefault(
          org.eclipse.wb.internal.swt.model.layout.form.IPreferenceConstants.PREF_V_WIDGET_OFFSET,
          6);
      preferences.setDefault(
          org.eclipse.wb.internal.swt.model.layout.form.IPreferenceConstants.PREF_V_PERCENTS,
          "20 80");
      preferences.setDefault(
          org.eclipse.wb.internal.swt.model.layout.form.IPreferenceConstants.PREF_H_WINDOW_MARGIN,
          12);
      preferences.setDefault(
          org.eclipse.wb.internal.swt.model.layout.form.IPreferenceConstants.PREF_H_PERCENT_OFFSET,
          6);
      preferences.setDefault(
          org.eclipse.wb.internal.swt.model.layout.form.IPreferenceConstants.PREF_H_WIDGET_OFFSET,
          6);
      preferences.setDefault(
          org.eclipse.wb.internal.swt.model.layout.form.IPreferenceConstants.PREF_H_PERCENTS,
          "20 80");
      preferences.setDefault(
          org.eclipse.wb.internal.swt.model.layout.form.IPreferenceConstants.PREF_KEEP_ATTACHMENTS_STYLE,
          false);
    }
  }
}
