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
package org.eclipse.wb.gef.core.tools;

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.core.requests.PasteRequest;

import org.eclipse.gef.Request;
import org.eclipse.jface.viewers.StructuredSelection;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@link PasteTool} creates new {@link EditPart EditParts} via from memento.
 *
 * @author lobas_av
 * @author scheglov_ke
 * @coverage gef.core
 */
public class PasteTool extends AbstractCreationTool {
	private final Object m_memento;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public PasteTool(Object memento) {
		m_memento = memento;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns object with paste info.
	 */
	public final Object getMemento() {
		return m_memento;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Request
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Creates a {@link PasteRequest} and sets this memento object on the request.
	 */
	@Override
	protected Request createTargetRequest() {
		return new PasteRequest(m_memento);
	}

	@Override
	protected void selectAddedObjects() {
		final IEditPartViewer viewer = getCurrentViewer();
		// prepare pasted EditPart's
		List<EditPart> editParts = new ArrayList<>();
		{
			PasteRequest request = (PasteRequest) getTargetRequest();
			for (Object model : request.getObjects()) {
				editParts.add((EditPart) viewer.getEditPartRegistry().get(model));
			}
		}
		// select EditPart's
		viewer.setSelection(new StructuredSelection(editParts));
	}
}