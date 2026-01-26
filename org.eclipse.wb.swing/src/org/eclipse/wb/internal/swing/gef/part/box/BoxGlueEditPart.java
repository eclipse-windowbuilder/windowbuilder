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
package org.eclipse.wb.internal.swing.gef.part.box;

import org.eclipse.wb.internal.swing.gef.policy.component.box.GlueSelectionEditPolicy;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;

import javax.swing.Box;

/**
 * The {@link EditPart} for {@link Box#createGlue()}.
 *
 * @author scheglov_ke
 * @coverage swing.gef.part
 */
public final class BoxGlueEditPart extends BoxEditPart {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public BoxGlueEditPart(ComponentInfo component) {
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
		installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE, new GlueSelectionEditPolicy());
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
				int x = r.x + (r.width - SPRING_SIZE) / 2;
				int y = r.y + (r.height - SPRING_SIZE) / 2;
				BoxGlueHorizontalEditPart.draw(graphics, new Rectangle(r.x, y, r.width, SPRING_SIZE));
				BoxGlueVerticalEditPart.draw(graphics, new Rectangle(x, r.y, SPRING_SIZE, r.height));
			}
		};
	}
}
