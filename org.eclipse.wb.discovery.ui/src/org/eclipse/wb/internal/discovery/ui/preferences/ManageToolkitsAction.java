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
package org.eclipse.wb.internal.discovery.ui.preferences;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.dialogs.PreferencesUtil;

/**
 * This action opens the Toolkits preference page.
 *
 * @see ToolkitsPreferencePage
 */
public class ManageToolkitsAction implements IEditorActionDelegate {
  private IEditorPart editor;

  /**
   * Create a new ManageToolkitsAction.
   */
  public ManageToolkitsAction() {
  }

  public void selectionChanged(IAction action, ISelection selection) {
  }

  public void setActiveEditor(IAction action, IEditorPart targetEditor) {
    this.editor = targetEditor;
  }

  public void run(IAction action) {
    PreferenceDialog dialog =
        PreferencesUtil.createPreferenceDialogOn(
            editor.getSite().getShell(),
            ToolkitsPreferencePage.PREFERENCE_PAGE_ID,
            null,
            null);
    if (dialog != null) {
      dialog.open();
    }
  }
}
