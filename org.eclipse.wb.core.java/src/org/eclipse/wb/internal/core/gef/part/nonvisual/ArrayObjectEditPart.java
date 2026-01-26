/*******************************************************************************
 * Copyright (c) 2011, 2026 Google, Inc. and others.
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
package org.eclipse.wb.internal.core.gef.part.nonvisual;

import org.eclipse.wb.core.gef.policy.selection.NonResizableSelectionEditPolicy;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.internal.core.model.nonvisual.AbstractArrayObjectInfo;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
/**
 * {@link EditPart} for <i>array objects</i> model.
 *
 * @author sablin_aa
 * @coverage gef.graphical
 */
public final class ArrayObjectEditPart extends GraphicalEditPart {
	private final AbstractArrayObjectInfo m_arrayInfo;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ArrayObjectEditPart(AbstractArrayObjectInfo arrayInfo) {
		m_arrayInfo = arrayInfo;
		setModel(arrayInfo);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public AbstractArrayObjectInfo getArrayInfo() {
		return m_arrayInfo;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Policy
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE, new NonResizableSelectionEditPolicy());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Figure
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected IFigure createFigure() {
		return new Figure();
	}

	@Override
	protected void refreshVisuals() {
		getFigure().setBounds(m_arrayInfo.getBounds());
	}
}