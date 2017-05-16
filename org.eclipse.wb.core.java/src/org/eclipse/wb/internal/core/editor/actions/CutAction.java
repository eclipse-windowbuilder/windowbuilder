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

import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ui.actions.ActionFactory;

import java.util.List;

/**
 * Implementation of {@link Action} for {@link ActionFactory#CUT}.
 *
 * @author scheglov_ke
 * @coverage core.editor.action
 */
public class CutAction extends Action {
  private final IEditPartViewer m_viewer;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CutAction(IEditPartViewer viewer) {
    m_viewer = viewer;
    m_viewer.addSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent event) {
        firePropertyChange(ENABLED, null, isEnabled() ? Boolean.TRUE : Boolean.FALSE);
      }
    });
    // copy presentation
    ActionUtils.copyPresentation(this, ActionFactory.CUT);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Action
  //
  ////////////////////////////////////////////////////////////////////////////
  private Command m_command;

  @Override
  public void run() {
    ExecutionUtils.runLog(new RunnableEx() {
      public void run() throws Exception {
        // copy
        {
          List<EditPart> editParts = m_viewer.getSelectedEditParts();
          List<JavaInfoMemento> m_mementos = CopyAction.getMementos(editParts);
          CopyAction.doCopy(m_mementos);
        }
        // delete
        m_viewer.getEditDomain().executeCommand(m_command);
      }
    });
  }

  @Override
  public boolean isEnabled() {
    List<EditPart> editParts = m_viewer.getSelectedEditParts();
    m_command = DeleteAction.getCommand(editParts);
    return CopyAction.hasMementos(editParts) && m_command != null;
  }
}
