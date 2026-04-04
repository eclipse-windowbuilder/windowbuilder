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
package org.eclipse.wb.internal.rcp.nebula.collapsiblebuttons;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.gef.policy.layout.flow.AbstractFlowLayoutEditPolicy;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator.LayoutRequestValidatorStubFalse;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.ChangeBoundsRequest;

/**
 * {@link LayoutEditPolicy} for dropping buttons on {@link CollapsibleButtonsEditPart}.
 *
 * @author sablin_aa
 * @coverage nebula.gef
 */
public final class CollapsibleButtonsLayoutEditPolicy extends AbstractFlowLayoutEditPolicy {
	private final CollapsibleButtonsInfo m_collButtons;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public CollapsibleButtonsLayoutEditPolicy(CollapsibleButtonsInfo collButtons) {
		m_collButtons = collButtons;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected boolean isHorizontal(Request request) {
		return false;
	}

	@Override
	protected boolean isGoodReferenceChild(Request request, EditPart editPart) {
		return editPart.getModel() instanceof ControlInfo;
	}

	@Override
	protected boolean isRequestCondition(Request request) {
		return super.isRequestCondition(request) || request instanceof CollapsibleButtonDropRequest;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Validation
	//
	////////////////////////////////////////////////////////////////////////////
	private static final ILayoutRequestValidator VALIDATOR = new LayoutRequestValidatorStubFalse() {
		@Override
		public boolean validateMoveRequest(EditPart host, ChangeBoundsRequest request) {
			return true;
		}
	};

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
	protected Command getCommand(Request request, Object referenceObject) {
		if (CollapsibleButtonDropRequest.TYPE.equals(request.getType())) {
			return getDropCommand((CollapsibleButtonDropRequest) request, referenceObject);
		}
		return super.getCommand(request, referenceObject);
	}

	private Command getDropCommand(CollapsibleButtonDropRequest buttonRequest, Object referenceObject) {
		final ControlInfo reference = (ControlInfo) referenceObject;
		return new EditCommand(m_collButtons) {
			@Override
			protected void executeEdit() throws Exception {
				ControlInfo newButton = CollapsibleButtonsInfo.createButton(m_collButtons, reference);
				buttonRequest.setButton(newButton);
			}
		};
	}

	@Override
	protected Command getMoveCommand(Object moveObject, Object referenceObject) {
		final ControlInfo button = (ControlInfo) moveObject;
		final ControlInfo reference = (ControlInfo) referenceObject;
		return new EditCommand(m_collButtons) {
			@Override
			protected void executeEdit() throws Exception {
				CollapsibleButtonsInfo.moveButton(button, reference);
			}
		};
	}
}
