/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc.
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
import org.eclipse.wb.internal.rcp.model.rcp.perspective.shortcuts.ViewShortcutContainerInfo;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.shortcuts.ViewShortcutInfo;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;

import java.util.List;

/**
 * Implementation of {@link LayoutEditPolicy} for {@link ViewShortcutContainerInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.gefTree.policy
 */
public final class ViewShortcutContainerLayoutEditPolicy extends LayoutEditPolicy {
	private static final ILayoutRequestValidator VALIDATOR =
			LayoutRequestValidators.modelType(ViewShortcutInfo.class);
	private final PageLayoutInfo m_page;
	private final ViewShortcutContainerInfo m_container;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ViewShortcutContainerLayoutEditPolicy(ViewShortcutContainerInfo container) {
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
		return editPart.getModel() instanceof ViewShortcutInfo;
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
	// Commands
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected Command getCommand(Request request, Object referenceObject) {
		if (request instanceof final ViewDropRequest viewDrop_Request) {
			final ViewInfo viewInfo = viewDrop_Request.getView();
			final ViewShortcutInfo reference = (ViewShortcutInfo) referenceObject;
			return new EditCommand(m_page) {
				@Override
				protected void executeEdit() throws Exception {
					ViewShortcutInfo newView = m_container.command_CREATE(viewInfo.getId(), reference);
					viewDrop_Request.setComponent(newView);
				}
			};
		}
		return super.getCommand(request, referenceObject);
	}

	@Override
	protected Command getMoveCommand(final List<? extends EditPart> moveParts, Object referenceObject) {
		final ViewShortcutInfo nextItem = (ViewShortcutInfo) referenceObject;
		return new EditCommand(m_page) {
			@Override
			protected void executeEdit() throws Exception {
				for (EditPart movePart : moveParts) {
					ViewShortcutInfo item = (ViewShortcutInfo) movePart.getModel();
					m_container.command_MOVE(item, nextItem);
				}
			}
		};
	}
}