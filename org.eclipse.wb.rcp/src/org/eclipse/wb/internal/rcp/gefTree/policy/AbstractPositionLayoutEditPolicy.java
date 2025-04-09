/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
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
package org.eclipse.wb.internal.rcp.gefTree.policy;

import org.eclipse.wb.core.gefTree.policy.SingleObjectLayoutEditPolicy;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.tree.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.rcp.model.widgets.AbstractPositionInfo;
import org.eclipse.wb.internal.swt.gef.ControlsLayoutRequestValidator;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

/**
 * {@link LayoutEditPolicy} for dropping {@link ControlInfo} on {@link AbstractPositionInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.gefTree.policy
 */
public final class AbstractPositionLayoutEditPolicy
extends
SingleObjectLayoutEditPolicy<ControlInfo> {
	private final AbstractPositionInfo m_position;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public AbstractPositionLayoutEditPolicy(AbstractPositionInfo position) {
		super(position.getComposite());
		m_position = position;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Requests
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected ILayoutRequestValidator getRequestValidator() {
		return ControlsLayoutRequestValidator.INSTANCE;
	}

	@Override
	protected boolean isEmpty() {
		return true;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Commands
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void command_CREATE(ControlInfo control) throws Exception {
		m_position.command_CREATE(control);
	}

	@Override
	protected void command_ADD(ControlInfo control) throws Exception {
		m_position.command_MOVE(control);
	}
}