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
package org.eclipse.wb.internal.rcp.gef.policy.rcp.perspective.shortcuts;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.gef.policy.layout.flow.AbstractFlowLayoutEditPolicy;
import org.eclipse.wb.core.gef.policy.validator.LayoutRequestValidators;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.tree.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.rcp.gef.policy.rcp.perspective.ViewDropRequest;
import org.eclipse.wb.internal.rcp.model.rcp.PdeUtils.ViewInfo;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.PageLayoutInfo;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.shortcuts.FastViewContainerInfo;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.shortcuts.FastViewInfo;

import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;

/**
 * Implementation of {@link LayoutEditPolicy} for {@link FastViewContainerInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.gef.policy
 */
public final class FastViewContainerLayoutEditPolicy extends AbstractFlowLayoutEditPolicy {
	private static final ILayoutRequestValidator VALIDATOR =
			LayoutRequestValidators.modelType(FastViewInfo.class);
	private final PageLayoutInfo m_page;
	private final FastViewContainerInfo m_container;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public FastViewContainerLayoutEditPolicy(FastViewContainerInfo container) {
		m_container = container;
		m_page = container.getPage();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Requests
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected boolean isGoodReferenceChild(Request request, EditPart editPart) {
		return editPart.getModel() instanceof FastViewInfo;
	}

	@Override
	protected boolean isRequestCondition(Request request) {
		return super.isRequestCondition(request) || request instanceof ViewDropRequest;
	}

	@Override
	protected ILayoutRequestValidator getRequestValidator() {
		return VALIDATOR;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// AbstractFlowLayoutEditPolicy
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected boolean isHorizontal(Request request) {
		return true;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Commands
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected Command getCommand(Request request, Object referenceObject) {
		if (request instanceof final ViewDropRequest viewDrop_Request) {
			final ViewInfo viewInfo = viewDrop_Request.getView();
			final FastViewInfo reference = (FastViewInfo) referenceObject;
			return new EditCommand(m_page) {
				@Override
				protected void executeEdit() throws Exception {
					FastViewInfo newView = m_container.command_CREATE(viewInfo.getId(), reference);
					viewDrop_Request.setComponent(newView);
				}
			};
		}
		return super.getCommand(request, referenceObject);
	}

	@Override
	protected Command getMoveCommand(Object moveObject, Object referenceObject) {
		final FastViewInfo item = (FastViewInfo) moveObject;
		final FastViewInfo nextItem = (FastViewInfo) referenceObject;
		return new EditCommand(m_page) {
			@Override
			protected void executeEdit() throws Exception {
				m_container.command_MOVE(item, nextItem);
			}
		};
	}
}