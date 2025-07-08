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
package org.eclipse.wb.internal.rcp.gef.part.rcp.perspective.shortcuts;

import org.eclipse.wb.core.gef.policy.selection.NonResizableSelectionEditPolicy;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.shortcuts.AbstractShortcutContainerInfo;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPolicy;

import java.util.Collections;
import java.util.List;

/**
 * {@link EditPart} for {@link AbstractShortcutContainerInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.gef.part
 */
abstract class AbstractShortcutContainerEditPart extends GraphicalEditPart {
	private final AbstractShortcutContainerInfo m_container;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public AbstractShortcutContainerEditPart(AbstractShortcutContainerInfo container) {
		m_container = container;
		setModel(m_container);
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
		Rectangle bounds = m_container.getBounds();
		getFigure().setBounds(bounds);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Policies
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE, new NonResizableSelectionEditPolicy());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Children
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected List<?> getModelChildren() {
		return ExecutionUtils.runObjectLog(() -> m_container.getPresentation().getChildrenGraphical(), Collections.emptyList());
	}
}
