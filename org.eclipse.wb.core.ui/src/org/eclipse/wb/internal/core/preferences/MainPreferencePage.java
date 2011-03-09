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
      label.setText("Close and re-open any editors to see the effects of these preferences.");
      GridDataFactory.create(label).spanH(2).alignHF();
      PreferenceLinkArea link =
          new PreferenceLinkArea(getFieldEditorParent(),
              SWT.NONE,
              "org.eclipse.jdt.ui.preferences.CodeFormatterPreferencePage",
              "See <a>''{0}''</a> to modify the Eclipse formatting preferences.",
              (IWorkbenchPreferenceContainer) getContainer(),
              null);
      GridDataFactory.create(link.getControl()).spanH(2).alignHF();
    }
    // editor layout mode
    {
      ComboFieldEditor editorLayout =
          new ComboFieldEditor(P_EDITOR_LAYOUT,
              "Editor layout:",
              new String[][]{
                  new String[]{
                      "On separate notebook tabs (Source first)",
                      "" + V_EDITOR_LAYOUT_PAGES_SOURCE},
                  new String[]{
                      "On separate notebook tabs (Design first)",
                      "" + V_EDITOR_LAYOUT_PAGES_DESIGN},
                  new String[]{
                      "Above each other with a split pane",
                      "" + V_EDITOR_LAYOUT_SPLIT_VERTICAL},
                  new String[]{
                      "Side by side with a split pane",
                      "" + V_EDITOR_LAYOUT_SPLIT_HORIZONTAL}},
              getFieldEditorParent());
      addField(editorLayout);
      // sync delay
      IntegerFieldEditor syncDelay =
          new IntegerFieldEditor(P_EDITOR_LAYOUT_SYNC_DELAY,
              "Sync Delay (ms):",
              getFieldEditorParent());
      syncDelay.setErrorMessage("Default syncronization delay in milliseconds must be an integer value in 250-10000 range.");
      syncDelay.setEmptyStringAllowed(false);
      syncDelay.setValidRange(-1, Integer.MAX_VALUE);
      syncDelay.getTextControl(getFieldEditorParent()).setToolTipText(
          "Set the default syncronization delay in milliseconds, -1 for syncronization on save");
      addField(syncDelay);
    }
    // other
    ComboFieldEditor widgetTreeDblClickActionEditor =
        new ComboFieldEditor(P_EDITOR_TREE_DBL_CLICK_ACTION,
            "Double-click on component tree to:",
            new String[][]{
                new String[]{
                    "Open editor at position of this widget",
                    "" + V_EDITOR_TREE_OPEN_WIDGET_IN_EDITOR},
                new String[]{
                    "Create/open default event listener",
                    "" + V_EDITOR_TREE_CREATE_LISTENER},
                new String[]{
                    "Initiate widget's variable rename",
                    "" + V_EDITOR_TREE_INITIATE_RENAME},},
            getFieldEditorParent());
    addField(widgetTreeDblClickActionEditor);
    //
    addField(new BooleanFieldEditor(P_EDITOR_RECOGNIZE_GUI,
        "Associate WindowBuilder editor with automatically recognized Java GUI files",
        getFieldEditorParent()));
    addField(new BooleanFieldEditor(P_EDITOR_MAX_DESIGN,
        "Maximize editor on \"Design\" page activation",
        getFieldEditorParent()));
    addField(new BooleanFieldEditor(P_EDITOR_FORMAT_ON_SAVE,
        "Format source code (and reparse) on editor save",
        getFieldEditorParent()));
    addField(new BooleanFieldEditor(P_EDITOR_GOTO_DEFINITION_ON_SELECTION,
        "Go to component definition in source on selection",
        getFieldEditorParent()));
    //
    addField(new BooleanFieldEditor(P_COMMON_PALETTE_ADD_CHOSEN,
        "Automatically add to palette when using Choose Component",
        getFieldEditorParent()));
    addField(new BooleanFieldEditor(P_COMMON_ACCEPT_NON_VISUAL_BEANS,
        "Accept drop non-visual beans to design canvas",
        getFieldEditorParent()));
    addField(new BooleanFieldEditor(P_COMMON_SHOW_DEBUG_INFO,
        "Show debug information in console",
        getFieldEditorParent()));
    addField(new BooleanFieldEditor(P_COMMON_SHOW_VERSION_WARNING,
        "Show warning for incompatible Eclipse/WindowBuilder versions",
        getFieldEditorParent()));
    if (EnvironmentUtils.IS_LINUX) {
      addField(new BooleanFieldEditor(P_COMMON_LINUX_DISABLE_SCREENSHOT_WORKAROUNDS,
          "Disable Preview Window flickering workarounds (Linux only)",
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