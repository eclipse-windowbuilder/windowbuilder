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
package org.eclipse.wb.internal.swing.gef.part.box;

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.internal.swing.gef.policy.component.box.StrutDirectRigidEditPolicy;
import org.eclipse.wb.internal.swing.gef.policy.component.box.StrutSelectionRigidEditPolicy;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPolicy;

import javax.swing.Box;

/**
 * The {@link EditPart} for {@link Box#createRigidArea(java.awt.Dimension)}.
 *
 * @author scheglov_ke
 * @coverage swing.gef.part
 */
public final class BoxRigidAreaEditPart extends BoxEditPart {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public BoxRigidAreaEditPart(ComponentInfo component) {
		super(component);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Policy
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void createEditPolicies() {
		super.createEditPolicies();
		installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE, new StrutSelectionRigidEditPolicy(m_component));
		installEditPolicy(new StrutDirectRigidEditPolicy(m_component));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Figure
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected IFigure createFigure() {
		return new Figure() {
			@Override
			protected void paintFigure(Graphics graphics) {
				super.paintFigure(graphics);
				Rectangle r = getClientArea();
				BoxStrutHorizontalEditPart.draw(graphics, r);
				BoxStrutVerticalEditPart.draw(graphics, r);
			}
		};
	}
}
