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
import org.eclipse.wb.internal.core.UiMessages;

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
            UiMessages.ParsingPreferencePage_hideBlockBegin,
            getFieldEditorParent());
    m_hideEndEditor =
        new StringFieldEditor(P_CODE_HIDE_END,
            UiMessages.ParsingPreferencePage_hideBlockEnd,
            getFieldEditorParent());
    m_hideLineEditor =
        new StringFieldEditor(P_CODE_HIDE_LINE,
            UiMessages.ParsingPreferencePage_hideSingle,
            getFieldEditorParent());
    m_hideBeginEditor.setEmptyStringAllowed(false);
    m_hideEndEditor.setEmptyStringAllowed(false);
    m_hideLineEditor.setEmptyStringAllowed(false);
    addField(m_hideBeginEditor);
    addField(m_hideEndEditor);
    addField(m_hideLineEditor);
    // evaluation
    addField(new BooleanFieldEditor(P_CODE_STRICT_EVALUATE,
        UiMessages.ParsingPreferencePage_strictEvaluate,
        getFieldEditorParent()));
    // highlight visited/executed lines
    addField(new BooleanFieldEditor(P_HIGHLIGHT_VISITED,
        UiMessages.ParsingPreferencePage_highlightVisitedLines,
        getFieldEditorParent()));
    addField(new ColorFieldEditor(P_HIGHLIGHT_VISITED_COLOR,
        UiMessages.ParsingPreferencePage_highlightVisitedLinesColor,
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
