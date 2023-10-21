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
package org.eclipse.wb.internal.xwt.gefTree.policy;

import org.eclipse.wb.core.gefTree.policy.ObjectLayoutEditPolicy;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.xwt.gef.ControlsLayoutRequestValidator;
import org.eclipse.wb.internal.xwt.model.widgets.AbstractPositionInfo;
import org.eclipse.wb.internal.xwt.model.widgets.ControlInfo;

import org.eclipse.gef.commands.Command;

import java.util.List;

/**
 * {@link LayoutEditPolicy} for dropping {@link ControlInfo} on {@link AbstractPositionInfo}.
 *
 * @author scheglov_ke
 * @coverage XWT.gefTree.policy
 */
public final class AbstractPositionLayoutEditPolicy extends ObjectLayoutEditPolicy<ControlInfo> {
	private static final ILayoutRequestValidator VALIDATOR = ControlsLayoutRequestValidator.INSTANCE;
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
		return VALIDATOR;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Commands
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected Command getAddCommand(List<EditPart> addParts, Object referenceObject) {
		if (addParts.size() != 1) {
			return null;
		}
		return super.getAddCommand(addParts, referenceObject);
	}

	@Override
	protected void command_CREATE(ControlInfo control, ControlInfo reference) throws Exception {
		m_position.command_CREATE(control);
	}

	@Override
	protected void command_MOVE(ControlInfo control, ControlInfo reference) throws Exception {
		m_position.command_MOVE(control);
	}
}