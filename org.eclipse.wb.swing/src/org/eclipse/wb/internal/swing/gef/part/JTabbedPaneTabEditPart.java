/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
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
package org.eclipse.wb.internal.swing.gef.part;

import org.eclipse.wb.core.gef.policy.selection.NonResizableSelectionEditPolicy;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.internal.swing.model.component.JTabbedPaneInfo;
import org.eclipse.wb.internal.swing.model.component.JTabbedPaneTabInfo;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;

/**
 * The {@link EditPart} for {@link JTabbedPaneInfo}.
 *
 * @author scheglov_ke
 * @coverage swing.gef.part
 */
public final class JTabbedPaneTabEditPart extends GraphicalEditPart {
	private JTabbedPaneTabInfo m_component;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public JTabbedPaneTabEditPart(JTabbedPaneTabInfo component) {
		m_component = component;
		setModel(m_component);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Model
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void updateModel() {
		m_component = (JTabbedPaneTabInfo) getModel();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Figure
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected Figure createFigure() {
		return new Figure();
	}

	@Override
	protected void refreshVisuals() {
		Rectangle bounds = m_component.getBounds();
		getFigure().setBounds(bounds);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Requests
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE, new NonResizableSelectionEditPolicy());
	}

	@Override
	public void performRequest(Request request) {
		if (request.getType() == RequestConstants.REQ_OPEN) {
			m_component.getPane().setActiveComponent(m_component.getComponent());
		}
	}
}
