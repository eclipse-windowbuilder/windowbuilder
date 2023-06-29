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
package org.eclipse.wb.internal.rcp.gef.part.widgets;

import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.Graphics;
import org.eclipse.wb.draw2d.IColorConstants;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.graphical.policies.TerminatorLayoutEditPolicy;
import org.eclipse.wb.internal.rcp.gef.GefMessages;
import org.eclipse.wb.internal.rcp.gef.policy.widgets.ScrolledCompositeLayoutEditPolicy;
import org.eclipse.wb.internal.rcp.model.widgets.ScrolledCompositeInfo;
import org.eclipse.wb.internal.swt.gef.part.CompositeEditPart;

import org.eclipse.draw2d.geometry.Dimension;

/**
 * {@link EditPart} for {@link ScrolledCompositeInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.gef.part
 */
public final class ScrolledCompositeEditPart extends CompositeEditPart {
	private final ScrolledCompositeInfo m_composite;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ScrolledCompositeEditPart(ScrolledCompositeInfo composite) {
		super(composite);
		m_composite = composite;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Figure
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void drawCustomBorder(Figure figure, Graphics graphics) {
		super.drawCustomBorder(figure, graphics);
		if (!m_composite.hasRequired_setContent()) {
			String message = GefMessages.ScrolledCompositeEditPart_setContentWarning;
			Dimension extent = graphics.getTextExtent(message);
			graphics.setForegroundColor(IColorConstants.red);
			graphics.drawText(
					message,
					(figure.getSize().width - extent.width) / 2,
					(figure.getSize().height - extent.height) / 2);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Policy
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void refreshEditPolicies() {
		super.refreshEditPolicies();
		installEditPolicy(new ScrolledCompositeLayoutEditPolicy(m_composite));
		installEditPolicy(new TerminatorLayoutEditPolicy());
	}
}
