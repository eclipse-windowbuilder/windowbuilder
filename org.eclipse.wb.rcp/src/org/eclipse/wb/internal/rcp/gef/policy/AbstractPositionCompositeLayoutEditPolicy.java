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
package org.eclipse.wb.internal.rcp.gef.policy;

import org.eclipse.wb.gef.tree.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.rcp.model.widgets.AbstractPositionCompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

import org.eclipse.draw2d.geometry.Insets;

/**
 * Implementation of {@link LayoutEditPolicy} for {@link AbstractPositionCompositeInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.gef.policy
 */
public abstract class AbstractPositionCompositeLayoutEditPolicy
extends
ControlPositionLayoutEditPolicy<String> {
	private final AbstractPositionCompositeInfo m_composite;

	////////////////////////////////////////////////////////////////////////////
	//
	// Composite
	//
	////////////////////////////////////////////////////////////////////////////
	public AbstractPositionCompositeLayoutEditPolicy(AbstractPositionCompositeInfo composite) {
		super(composite);
		m_composite = composite;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Positions
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Adds feedback position.
	 */
	protected final void addFeedback2(double px1,
			double py1,
			double px2,
			double py2,
			Insets insets,
			String hint,
			String methodName) {
		if (m_composite.getControl(methodName) == null) {
			addFeedback(px1, py1, px2, py2, insets, hint, methodName);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Implementation of commands
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void command_CREATE(ControlInfo component, String data) throws Exception {
		m_composite.command_CREATE(component, data);
	}

	@Override
	protected void command_MOVE(ControlInfo component, String data) throws Exception {
		m_composite.command_MOVE(component, data);
	}
}
