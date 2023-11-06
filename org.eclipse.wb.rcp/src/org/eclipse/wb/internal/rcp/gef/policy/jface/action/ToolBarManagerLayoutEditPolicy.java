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
package org.eclipse.wb.internal.rcp.gef.policy.jface.action;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.gef.policy.layout.flow.ObjectFlowLayoutEditPolicy;
import org.eclipse.wb.core.gef.policy.validator.LayoutRequestValidators;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.rcp.model.jface.action.ActionContributionItemInfo;
import org.eclipse.wb.internal.rcp.model.jface.action.ContributionItemInfo;
import org.eclipse.wb.internal.rcp.model.jface.action.ToolBarManagerInfo;

import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;

/**
 * {@link LayoutEditPolicy} for dropping items on {@link ToolBarManagerInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.gef.policy
 */
public final class ToolBarManagerLayoutEditPolicy
extends
ObjectFlowLayoutEditPolicy<ContributionItemInfo> {
	private final ToolBarManagerInfo m_manager;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ToolBarManagerLayoutEditPolicy(ToolBarManagerInfo composite) {
		super(composite);
		m_manager = composite;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected boolean isHorizontal(Request request) {
		return true;
	}

	@Override
	protected boolean isGoodReferenceChild(Request request, EditPart editPart) {
		return editPart.getModel() instanceof ContributionItemInfo;
	}

	@Override
	protected boolean isRequestCondition(Request request) {
		return super.isRequestCondition(request) || request instanceof ActionDropRequest;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Validation
	//
	////////////////////////////////////////////////////////////////////////////
	private static final ILayoutRequestValidator VALIDATOR =
			LayoutRequestValidators.modelType(ContributionItemInfo.class);

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
		if (request instanceof final ActionDropRequest actionRequest) {
			final ContributionItemInfo reference = (ContributionItemInfo) referenceObject;
			return new EditCommand(m_manager) {
				@Override
				protected void executeEdit() throws Exception {
					ActionContributionItemInfo newItem =
							m_manager.command_CREATE(actionRequest.getAction(), reference);
					actionRequest.setItem(newItem);
				}
			};
		}
		return super.getCommand(request, referenceObject);
	}

	@Override
	protected void command_CREATE(ContributionItemInfo newObject, ContributionItemInfo referenceObject)
			throws Exception {
		m_manager.command_CREATE(newObject, referenceObject);
	}

	@Override
	protected void command_MOVE(ContributionItemInfo object, ContributionItemInfo referenceObject)
			throws Exception {
		m_manager.command_MOVE(object, referenceObject);
	}
}
