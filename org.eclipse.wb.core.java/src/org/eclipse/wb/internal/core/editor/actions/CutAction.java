/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.editor.actions;

import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
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
			@Override
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
			@Override
			public void run() throws Exception {
				// copy
				{
					List<? extends EditPart> editParts = m_viewer.getSelectedEditParts();
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
		List<? extends EditPart> editParts = m_viewer.getSelectedEditParts();
		m_command = DeleteAction.getCommand(editParts);
		return CopyAction.hasMementos(editParts) && m_command != null;
	}
}
