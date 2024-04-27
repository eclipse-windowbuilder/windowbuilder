/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.gef.core.requests;

import org.eclipse.wb.gef.core.EditPart;

import org.eclipse.gef.Request;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link Request} from multiple {@link EditPart}s.
 *
 * @author lobas_av
 * @coverage gef.core
 */
public class GroupRequest extends Request {
	private List<EditPart> m_editParts;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Constructs an empty {@link GroupRequest}.
	 */
	public GroupRequest() {
	}

	/**
	 * Constructs a {@link GroupRequest} with the specified <i>type</i>.
	 */
	public GroupRequest(Object type) {
		super(type);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns all {@link EditPart}s making this {@link Request}.
	 */
	public List<EditPart> getEditParts() {
		return m_editParts;
	}

	/**
	 * Sets the {@link EditPart}s making this {@link Request}.
	 */
	public void setEditParts(List<EditPart> editParts) {
		m_editParts = editParts;
	}

	/**
	 * Add {@link EditPart} to this {@link Request}.
	 */
	public void setEditParts(EditPart editPart) {
		m_editParts = new ArrayList<>(List.of(editPart));
	}
}