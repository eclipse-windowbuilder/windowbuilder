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
package org.eclipse.wb.internal.swt.gef.policy.layout;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.gef.policy.PolicyUtils;
import org.eclipse.wb.core.gef.policy.validator.LayoutRequestValidators;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.core.requests.CreateRequest;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.swt.model.layout.LayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;

import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;

/**
 * Implementation of {@link LayoutEditPolicy} for dropping {@link LayoutInfo} on
 * {@link ContainerInfo}.
 *
 * @author scheglov_ke
 * @coverage swt.gef.policy
 */
public final class DropLayoutEditPolicy extends LayoutEditPolicy {
	private static final ILayoutRequestValidator VALIDATOR =
			LayoutRequestValidators.modelType(LayoutInfo.class);
	private final CompositeInfo m_composite;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public DropLayoutEditPolicy(CompositeInfo composite) {
		m_composite = composite;
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
	// Command
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected Command getCreateCommand(CreateRequest request) {
		final LayoutInfo newLayout = (LayoutInfo) request.getNewObject();
		return new EditCommand(m_composite) {
			@Override
			protected void executeEdit() throws Exception {
				m_composite.setLayout(newLayout);
			}
		};
	}
}