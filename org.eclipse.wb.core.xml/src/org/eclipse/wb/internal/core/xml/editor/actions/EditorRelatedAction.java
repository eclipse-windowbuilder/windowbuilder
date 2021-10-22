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
package org.eclipse.wb.internal.core.xml.editor.actions;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.xml.editor.AbstractXmlEditor;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

/**
 * Abstract superclass for actions of {@link AbstractXmlEditor}.
 *
 * @author scheglov_ke
 * @coverage XML.editor.action
 */
public abstract class EditorRelatedAction extends Action implements IEditorActionDelegate {
  private AbstractXmlEditor m_editor;

  ////////////////////////////////////////////////////////////////////////////
  //
  // IEditorActionDelegate
  //
  ////////////////////////////////////////////////////////////////////////////
  public final void setActiveEditor(IAction action, IEditorPart editor) {
    m_editor = null;
    if (editor instanceof AbstractXmlEditor) {
      m_editor = (AbstractXmlEditor) editor;
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
   * @return the active {@link AbstractXmlEditor}.
   */
  protected final AbstractXmlEditor getEditor() {
    AbstractXmlEditor designerEditor = m_editor;
    if (designerEditor == null) {
      designerEditor = getActiveEditor();
    }
    return designerEditor;
  }

  /**
   * @return the active {@link AbstractXmlEditor}.
   */
  public static AbstractXmlEditor getActiveEditor() {
    IEditorPart editor =
        DesignerPlugin.getActiveWorkbenchWindow().getActivePage().getActiveEditor();
    if (editor != null && editor instanceof AbstractXmlEditor) {
      return (AbstractXmlEditor) editor;
    }
    return null;
  }
}