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

import org.eclipse.wb.core.controls.jface.preference.ComboFieldEditor;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.UiMessages;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.dialogs.PreferenceLinkArea;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

/**
 * {@link PreferencePage} for editor layout settings.
 *
 * @author scheglov_ke
 * @coverage core.preferences.ui
 */
public final class MainPreferencePage extends FieldEditorPreferencePage
    implements
      IWorkbenchPreferencePage,
      IPreferenceConstants {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public MainPreferencePage() {
    super(GRID);
    setPreferenceStore(DesignerPlugin.getPreferences());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // FieldEditorPreferencePage
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createFieldEditors() {
    // hint
    {
      Label label = new Label(getFieldEditorParent(), SWT.NONE);
      label.setText(UiMessages.MainPreferencePage_closeEditorsWarning);
      GridDataFactory.create(label).spanH(2).alignHF();
      PreferenceLinkArea link =
          new PreferenceLinkArea(getFieldEditorParent(),
              SWT.NONE,
              "org.eclipse.jdt.ui.preferences.CodeFormatterPreferencePage",
              UiMessages.MainPreferencePage_formattingLink,
              (IWorkbenchPreferenceContainer) getContainer(),
              null);
      GridDataFactory.create(link.getControl()).spanH(2).alignHF();
    }
    // editor layout mode
    {
      ComboFieldEditor editorLayout =
          new ComboFieldEditor(P_EDITOR_LAYOUT, "Editor layout:", new String[][]{
              new String[]{
                  "On separate notebook tabs (Source first)",
                  "" + V_EDITOR_LAYOUT_PAGES_SOURCE},
              new String[]{
                  "On separate notebook tabs (Design first)",
                  "" + V_EDITOR_LAYOUT_PAGES_DESIGN},
              new String[]{
                  "Above each other with a split pane (Source first)",
                  "" + V_EDITOR_LAYOUT_SPLIT_VERTICAL_SOURCE},
              new String[]{
                  "Above each other with a split pane (Design first)",
                  "" + V_EDITOR_LAYOUT_SPLIT_VERTICAL_DESIGN},
              new String[]{
                  "Side by side with a split pane (Source first)",
                  "" + V_EDITOR_LAYOUT_SPLIT_HORIZONTAL_SOURCE},
              new String[]{
                  "Side by side with a split pane (Design first)",
                  "" + V_EDITOR_LAYOUT_SPLIT_HORIZONTAL_DESIGN},}, getFieldEditorParent());
      addField(editorLayout);
      // sync delay
      IntegerFieldEditor syncDelay =
          new IntegerFieldEditor(P_EDITOR_LAYOUT_SYNC_DELAY,
              UiMessages.MainPreferencePage_syncDelay,
              getFieldEditorParent());
      syncDelay.setErrorMessage(UiMessages.MainPreferencePage_syncDelayMessage);
      syncDelay.setEmptyStringAllowed(false);
      syncDelay.setValidRange(-1, Integer.MAX_VALUE);
      syncDelay.getTextControl(getFieldEditorParent()).setToolTipText(
          UiMessages.MainPreferencePage_syncDelayHint);
      addField(syncDelay);
    }
    // other
    ComboFieldEditor widgetTreeDblClickActionEditor =
        new ComboFieldEditor(P_EDITOR_TREE_DBL_CLICK_ACTION,
            UiMessages.MainPreferencePage_doubleClick,
            new String[][]{
                new String[]{
                    UiMessages.MainPreferencePage_doubleClickOpenEditor,
                    "" + V_EDITOR_TREE_OPEN_WIDGET_IN_EDITOR},
                new String[]{
                    UiMessages.MainPreferencePage_doubleClickOpenEventListener,
                    "" + V_EDITOR_TREE_CREATE_LISTENER},
                new String[]{
                    UiMessages.MainPreferencePage_doubleClickRename,
                    "" + V_EDITOR_TREE_INITIATE_RENAME},},
            getFieldEditorParent());
    addField(widgetTreeDblClickActionEditor);
    //
    addField(new BooleanFieldEditor(P_EDITOR_RECOGNIZE_GUI,
        UiMessages.MainPreferencePage_associateWithWB,
        getFieldEditorParent()));
    addField(new BooleanFieldEditor(P_EDITOR_MAX_DESIGN,
        UiMessages.MainPreferencePage_maximizeEditorOndesign,
        getFieldEditorParent()));
    addField(new BooleanFieldEditor(P_EDITOR_FORMAT_ON_SAVE,
        UiMessages.MainPreferencePage_formatOnSave,
        getFieldEditorParent()));
    addField(new BooleanFieldEditor(P_EDITOR_GOTO_DEFINITION_ON_SELECTION,
        UiMessages.MainPreferencePage_goInSourceOnSelection,
        getFieldEditorParent()));
    //
    addField(new BooleanFieldEditor(P_COMMON_PALETTE_ADD_CHOSEN,
        UiMessages.MainPreferencePage_autoCustomOnChoose,
        getFieldEditorParent()));
    addField(new BooleanFieldEditor(P_COMMON_ACCEPT_NON_VISUAL_BEANS,
        UiMessages.MainPreferencePage_supportNonVisualBeans,
        getFieldEditorParent()));
    addField(new BooleanFieldEditor(P_COMMON_SHOW_DEBUG_INFO,
        UiMessages.MainPreferencePage_showDebugOnConsole,
        getFieldEditorParent()));
    addField(new BooleanFieldEditor(P_COMMON_SHOW_VERSION_WARNING,
        UiMessages.MainPreferencePage_checkVersions,
        getFieldEditorParent()));
    if (EnvironmentUtils.IS_LINUX) {
      addField(new BooleanFieldEditor(P_COMMON_LINUX_DISABLE_SCREENSHOT_WORKAROUNDS,
          UiMessages.MainPreferencePage_disableLinuxWorkaround,
          getFieldEditorParent()));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // PreferencePage
  //
  ////////////////////////////////////////////////////////////////////////////
  public void init(IWorkbench workbench) {
  }

  @Override
  protected void performDefaults() {
    super.performDefaults();
    IPreferenceStore store = DesignerPlugin.getDefault().getPreferenceStore();
    store.setToDefault(P_EDITOR_LAYOUT);
    store.setToDefault(P_EDITOR_LAYOUT_SYNC_DELAY);
  }
}