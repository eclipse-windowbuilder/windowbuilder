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
package org.eclipse.wb.internal.rcp.gef.policy.jface;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.gef.policy.PolicyUtils;
import org.eclipse.wb.core.gef.policy.validator.LayoutRequestValidators;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.core.requests.CreateRequest;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.rcp.model.jface.ControlDecorationInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

import org.eclipse.gef.commands.Command;

import java.util.List;

/**
 * {@link LayoutEditPolicy} for dropping {@link ControlDecorationInfo} on {@link ControlInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.gef.policy
 */
public final class ControlDecorationDropLayoutEditPolicy extends LayoutEditPolicy {
	private static final ILayoutRequestValidator VALIDATOR =
			LayoutRequestValidators.modelType(ControlDecorationInfo.class);
	private final ControlInfo m_control;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ControlDecorationDropLayoutEditPolicy(ControlInfo control) {
		m_control = control;
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
	// Feedback
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void showLayoutTargetFeedback(Request request) {
		PolicyUtils.showBorderTargetFeedback(this);
	}

	@Override
	protected void eraseLayoutTargetFeedback(Request request) {
		PolicyUtils.eraseBorderTargetFeedback(this);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Commands
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected Command getCreateCommand(CreateRequest request) {
		if (isValidTargetControl()) {
			final ControlDecorationInfo decoration = (ControlDecorationInfo) request.getNewObject();
			return new EditCommand(m_control) {
				@Override
				protected void executeEdit() throws Exception {
					decoration.command_CREATE(m_control);
				}
			};
		}
		return null;
	}

	@Override
	protected Command getAddCommand(ChangeBoundsRequest request) {
		List<EditPart> editParts = request.getEditParts();
		if (isValidTargetControl() && editParts.size() == 1) {
			final ControlDecorationInfo decoration = (ControlDecorationInfo) editParts.get(0).getModel();
			return new EditCommand(m_control) {
				@Override
				protected void executeEdit() throws Exception {
					decoration.command_ADD(m_control);
				}
			};
		}
		return null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return <code>true</code> if this {@link ControlInfo} can accept {@link ControlDecorationInfo}
	 *         .
	 */
	private boolean isValidTargetControl() {
		return m_control.getParent() != null
				&& m_control.getChildren(ControlDecorationInfo.class).isEmpty();
	}
}