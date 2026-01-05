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
package org.eclipse.wb.internal.rcp.gefTree.policy.rcp.perspective.shortcuts;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.gef.policy.validator.LayoutRequestValidators;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.tree.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.rcp.gef.policy.rcp.perspective.ViewDropRequest;
import org.eclipse.wb.internal.rcp.model.rcp.PdeUtils.ViewInfo;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.PageLayoutInfo;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.shortcuts.FastViewContainerInfo;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.shortcuts.FastViewInfo;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;

import java.util.List;

/**
 * Implementation of {@link LayoutEditPolicy} for {@link FastViewContainerInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.gefTree.policy
 */
@Deprecated(since = "1.9.1400", forRemoval = true)
public final class FastViewContainerLayoutEditPolicy extends LayoutEditPolicy {
	@SuppressWarnings("removal")
	private static final ILayoutRequestValidator VALIDATOR =
			LayoutRequestValidators.modelType(FastViewInfo.class);
	private final PageLayoutInfo m_page;
	@SuppressWarnings("removal")
	private final FastViewContainerInfo m_container;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	@Deprecated
	@SuppressWarnings("removal")
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
	@Deprecated
	@SuppressWarnings("removal")
	protected boolean isGoodReferenceChild(Request request, EditPart editPart) {
		return editPart.getModel() instanceof FastViewInfo;
	}

	@Override
	@Deprecated
	protected boolean isRequestCondition(Request request) {
		return super.isRequestCondition(request) || request instanceof ViewDropRequest;
	}

	@Override
	@Deprecated
	protected ILayoutRequestValidator getRequestValidator() {
		return VALIDATOR;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Commands
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	@Deprecated
	@SuppressWarnings("removal")
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
	@Deprecated
	@SuppressWarnings("removal")
	protected Command getMoveCommand(final List<? extends EditPart> moveParts, Object referenceObject) {
		final FastViewInfo nextItem = (FastViewInfo) referenceObject;
		return new EditCommand(m_page) {
			@Override
			protected void executeEdit() throws Exception {
				for (EditPart movePart : moveParts) {
					FastViewInfo item = (FastViewInfo) movePart.getModel();
					m_container.command_MOVE(item, nextItem);
				}
			}
		};
	}
}