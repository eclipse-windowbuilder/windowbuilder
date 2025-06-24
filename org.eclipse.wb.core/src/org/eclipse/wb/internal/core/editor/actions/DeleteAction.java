/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
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

import org.eclipse.wb.core.gef.command.CompoundEditCommand;
import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.gef.core.IEditPartViewer;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ui.actions.ActionFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Implementation of {@link Action} for {@link ActionFactory#DELETE}.
 *
 * @author scheglov_ke
 * @coverage core.editor.action
 */
public class DeleteAction extends Action {
	private final IEditPartViewer m_viewer;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public DeleteAction(IEditPartViewer viewer) {
		m_viewer = viewer;
		m_viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				firePropertyChange(ENABLED, null, isEnabled() ? Boolean.TRUE : Boolean.FALSE);
			}
		});
		// copy presentation
		ActionUtils.copyPresentation(this, ActionFactory.DELETE);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Action
	//
	////////////////////////////////////////////////////////////////////////////
	private Command m_command;

	@Override
	public void run() {
		m_viewer.getEditDomain().getCommandStack().execute(m_command);
	}

	@Override
	public boolean isEnabled() {
		m_command = getCommand(m_viewer.getSelectedEditParts());
		return m_command != null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Command
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link Command} for deleting given {@link EditPart}'s.
	 */
	public static Command getCommand(List<? extends EditPart> editParts) {
		if (editParts.isEmpty()) {
			return null;
		}
		// prepare ObjectInfo's to delete
		List<ObjectInfo> objects = new ArrayList<>();
		for (EditPart editPart : editParts) {
			// prepare object
			ObjectInfo object;
			{
				Object model = editPart.getModel();
				if (!(model instanceof ObjectInfo)) {
					return null;
				}
				object = (ObjectInfo) model;
			}
			// Check that object is not deleted.
			// When we refresh GEF viewer after "delete", temporary there is situation when selection
			// contains already deleted objects.
			if (object.isDeleted()) {
				return null;
			}
			// check that ObjectInfo can be deleted
			if (!object.canDelete()) {
				return null;
			}
			// add model
			objects.add(object);
		}
		// don't delete separately children, if we delete their parents
		for (Iterator<ObjectInfo> I = objects.iterator(); I.hasNext();) {
			ObjectInfo object = I.next();
			if (object.getParent(objects) != null) {
				I.remove();
			}
		}
		// prepare compound command
		CompoundEditCommand command = new CompoundEditCommand(objects.get(0));
		for (final ObjectInfo object : objects) {
			command.add(new EditCommand(object) {
				@Override
				protected void executeEdit() throws Exception {
					object.delete();
				}
			});
		}
		return command;
	}
}
