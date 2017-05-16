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
package org.eclipse.wb.internal.core.editor.actions;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.editor.multi.DesignerEditor;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

/**
 * Abstract superclass for actions of {@link DesignerEditor}.
 *
 * @author scheglov_ke
 * @coverage core.editor.action
 */
public abstract class EditorRelatedAction extends Action implements IEditorActionDelegate {
  private DesignerEditor m_editor;

  ////////////////////////////////////////////////////////////////////////////
  //
  // IEditorActionDelegate
  //
  ////////////////////////////////////////////////////////////////////////////
  public final void setActiveEditor(IAction action, IEditorPart editor) {
    m_editor = null;
    if (editor instanceof DesignerEditor) {
      m_editor = (DesignerEditor) editor;
    }
    setEnabled(m_editor != null);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IActionDelegate
  //
  ////////////////////////////////////////////////////////////////////////////
  public void selectionChanged(IAction action, ISelection selection) {
  }

  public void run(IAction action) {
    run();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the active {@link DesignerEditor}.
   */
  protected final DesignerEditor getEditor() {
    DesignerEditor designerEditor = m_editor;
    if (designerEditor == null) {
      designerEditor = getActiveEditor();
    }
    //
    return designerEditor;
  }

  /**
   * @return the active {@link DesignerEditor}.
   */
  static DesignerEditor getActiveEditor() {
    IEditorPart editor =
        DesignerPlugin.getActiveWorkbenchWindow().getActivePage().getActiveEditor();
    if (editor != null && editor instanceof DesignerEditor) {
      return (DesignerEditor) editor;
    }
    return null;
  }
}