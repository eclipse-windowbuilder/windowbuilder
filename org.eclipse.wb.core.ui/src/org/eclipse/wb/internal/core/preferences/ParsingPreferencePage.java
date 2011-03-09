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

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * {@link PreferencePage} for code parsing settings.
 * 
 * @author scheglov_ke
 * @coverage core.preferences.ui
 */
public final class ParsingPreferencePage extends FieldEditorPreferencePage
    implements
      IWorkbenchPreferencePage,
      IPreferenceConstants {
  private StringFieldEditor m_hideBeginEditor;
  private StringFieldEditor m_hideEndEditor;
  private StringFieldEditor m_hideLineEditor;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ParsingPreferencePage() {
    super(GRID);
    setPreferenceStore(DesignerPlugin.getDefault().getPreferenceStore());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // FieldEditorPreferencePage
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createFieldEditors() {
    m_hideBeginEditor =
        new StringFieldEditor(P_CODE_HIDE_BEGIN,
            "Begin hidden code block tag:",
            getFieldEditorParent());
    m_hideEndEditor =
        new StringFieldEditor(P_CODE_HIDE_END, "End hidden code block tag:", getFieldEditorParent());
    m_hideLineEditor =
        new StringFieldEditor(P_CODE_HIDE_LINE,
            "Single hidden code line tag:",
            getFieldEditorParent());
    m_hideBeginEditor.setEmptyStringAllowed(false);
    m_hideEndEditor.setEmptyStringAllowed(false);
    m_hideLineEditor.setEmptyStringAllowed(false);
    addField(m_hideBeginEditor);
    addField(m_hideEndEditor);
    addField(m_hideLineEditor);
    // evaluation
    addField(new BooleanFieldEditor(P_CODE_STRICT_EVALUATE,
        "Strict evaluation mode (require using @wbp tags for parameters, etc)",
        getFieldEditorParent()));
    // highlight visited/executed lines
    addField(new BooleanFieldEditor(P_HIGHLIGHT_VISITED,
        "Highlight visited/executed lines in source after parse",
        getFieldEditorParent()));
    addField(new ColorFieldEditor(P_HIGHLIGHT_VISITED_COLOR,
        "Visited line highlight color:",
        getFieldEditorParent()));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // PreferencePage
  //
  ////////////////////////////////////////////////////////////////////////////
  public void init(IWorkbench workbench) {
  }
}
