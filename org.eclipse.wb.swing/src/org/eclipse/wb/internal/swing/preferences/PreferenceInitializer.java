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
package org.eclipse.wb.internal.swing.preferences;

import org.eclipse.wb.internal.swing.ToolkitProvider;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Initializer for Swing preferences.
 *
 * @author scheglov_ke
 * @coverage swing.preferences
 */
public final class PreferenceInitializer extends AbstractPreferenceInitializer
    implements
      org.eclipse.wb.internal.swing.preferences.IPreferenceConstants {
  @Override
  public void initializeDefaultPreferences() {
    IPreferenceStore preferences = ToolkitProvider.DESCRIPTION.getPreferences();
    // general
    preferences.setDefault(P_GENERAL_HIGHLIGHT_CONTAINERS, true);
    preferences.setDefault(P_GENERAL_TEXT_SUFFIX, true);
    preferences.setDefault(P_GENERAL_IMPORTANT_PROPERTIES_AFTER_ADD, false);
    preferences.setDefault(P_GENERAL_DIRECT_EDIT_AFTER_ADD, true);
    preferences.setDefault(P_GENERAL_DEFAULT_TOP_WIDTH, 450);
    preferences.setDefault(P_GENERAL_DEFAULT_TOP_HEIGHT, 300);
    // variable names
    {
      preferences.setDefault(P_VARIABLE_TEXT_MODE, V_VARIABLE_TEXT_MODE_DEFAULT);
      preferences.setDefault(P_VARIABLE_TEXT_TEMPLATE, "${class_acronym}${text}");
      preferences.setDefault(P_VARIABLE_TEXT_WORDS_LIMIT, 3);
    }
    // NLS
    {
      preferences.setDefault(P_NLS_AUTO_EXTERNALIZE, true);
      preferences.setDefault(P_NLS_KEY_AS_STRING_VALUE_ONLY, false);
      preferences.setDefault(P_NLS_KEY_QUALIFIED_TYPE_NAME, false);
      preferences.setDefault(P_NLS_KEY_RENAME_WITH_VARIABLE, false);
      preferences.setDefault(P_NLS_KEY_HAS_STRING_VALUE, false);
      preferences.setDefault(P_NLS_KEY_AS_VALUE_PREFIX, "*");
    }
    // layouts
    preferences.setDefault(P_LAYOUT_OF_PARENT, false);
    preferences.setDefault(P_LAYOUT_NAME_TEMPLATE, "${layoutAcronym}_${containerName}");
  }
}
