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
package org.eclipse.wb.internal.core.preferences;

import org.eclipse.wb.internal.core.DesignerPlugin;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Initializer for default Designer preferences.
 *
 * @author scheglov_ke
 * @coverage core.preferences
 */
public final class PreferenceInitializer2 extends AbstractPreferenceInitializer
    implements
      IPreferenceConstants {
  @Override
  public void initializeDefaultPreferences() {
    IPreferenceStore preferences = DesignerPlugin.getDefault().getPreferenceStore();
    // common
    preferences.setDefault(P_COMMON_PALETTE_ADD_CHOSEN, true);
    preferences.setDefault(P_COMMON_ACCEPT_NON_VISUAL_BEANS, true);
    preferences.setDefault(P_COMMON_SHOW_DEBUG_INFO, false);
    preferences.setDefault(P_COMMON_SHOW_VERSION_WARNING, false);
    // linux only
    preferences.setDefault(P_COMMON_LINUX_DISABLE_SCREENSHOT_WORKAROUNDS, false);
    // editor layout
    preferences.setDefault(P_EDITOR_LAYOUT, V_EDITOR_LAYOUT_PAGES_SOURCE);
    preferences.setDefault(P_EDITOR_LAYOUT_SYNC_DELAY, 1000);
    // editor
    preferences.setDefault(P_EDITOR_RECOGNIZE_GUI, false);
    preferences.setDefault(P_EDITOR_MAX_DESIGN, false);
    preferences.setDefault(P_EDITOR_FORMAT_ON_SAVE, false);
    preferences.setDefault(P_EDITOR_GOTO_DEFINITION_ON_SELECTION, true);
    preferences.setDefault(P_EDITOR_TREE_DBL_CLICK_ACTION, V_EDITOR_TREE_OPEN_WIDGET_IN_EDITOR);
    // highlight visited/executed lines
    preferences.setDefault(P_HIGHLIGHT_VISITED, false);
    //PreferenceConverter.setDefault(preferences, P_HIGHLIGHT_VISITED_COLOR, new RGB(235, 255, 235));
    preferences.setDefault(P_HIGHLIGHT_VISITED_COLOR, "235,255,235");
    // code parsing
    preferences.setDefault(P_CODE_HIDE_BEGIN, "$hide>>$");
    preferences.setDefault(P_CODE_HIDE_END, "$hide<<$");
    preferences.setDefault(P_CODE_HIDE_LINE, "$hide$");
    preferences.setDefault(P_CODE_STRICT_EVALUATE, false);
  }
}
