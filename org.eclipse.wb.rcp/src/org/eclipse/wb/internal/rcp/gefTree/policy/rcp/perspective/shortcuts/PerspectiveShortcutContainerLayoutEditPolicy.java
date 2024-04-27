/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.rcp.gefTree.policy.rcp.perspective.shortcuts;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.gef.policy.validator.LayoutRequestValidators;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.core.requests.PasteRequest;
import org.eclipse.wb.gef.tree.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.rcp.gef.policy.rcp.perspective.PerspectiveDropRequest;
import org.eclipse.wb.internal.rcp.model.rcp.PdeUtils.PerspectiveInfo;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.PageLayoutInfo;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.shortcuts.PerspectiveShortcutContainerInfo;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.shortcuts.PerspectiveShortcutInfo;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;

import java.util.List;

/**
 * Implementation of {@link LayoutEditPolicy} for {@link PerspectiveShortcutContainerInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.gefTree.policy
 */
public final class PerspectiveShortcutContainerLayoutEditPolicy extends LayoutEditPolicy {
	private static final ILayoutRequestValidator VALIDATOR =
			LayoutRequestValidators.modelType(PerspectiveShortcutInfo.class);
	private final PageLayoutInfo m_page;
	private final PerspectiveShortcutContainerInfo m_container;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public PerspectiveShortcutContainerLayoutEditPolicy(PerspectiveShortcutContainerInfo container) {
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
		return editPart.getModel() instanceof PerspectiveShortcutInfo;
	}

	@Override
	protected boolean isRequestCondition(Request request) {
		return super.isRequestCondition(request) || request instanceof PerspectiveDropRequest;
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
		if (request instanceof final PerspectiveDropRequest perspectiveDrop_Request) {
			final PerspectiveInfo perspectiveInfo = perspectiveDrop_Request.getPerspective();
			final PerspectiveShortcutInfo reference = (PerspectiveShortcutInfo) referenceObject;
			return new EditCommand(m_page) {
				@Override
				protected void executeEdit() throws Exception {
					PerspectiveShortcutInfo newPerspective =
							m_container.command_CREATE(perspectiveInfo.getId(), reference);
					perspectiveDrop_Request.setComponent(newPerspective);
				}
			};
		}
		return super.getCommand(request, referenceObject);
	}

	@Override
	protected Command getCreateCommand(Object newObject, Object referenceObject) {
		return null;
	}

	@Override
	protected Command getPasteCommand(PasteRequest request, Object referenceObject) {
		return null;
	}

	@Override
	protected Command getMoveCommand(final List<? extends EditPart> moveParts, Object referenceObject) {
		final PerspectiveShortcutInfo nextItem = (PerspectiveShortcutInfo) referenceObject;
		return new EditCommand(m_page) {
			@Override
			protected void executeEdit() throws Exception {
				for (EditPart movePart : moveParts) {
					PerspectiveShortcutInfo item = (PerspectiveShortcutInfo) movePart.getModel();
					m_container.command_MOVE(item, nextItem);
				}
			}
		};
	}

	@Override
	protected Command getAddCommand(List<? extends EditPart> addParts, Object referenceObject) {
		return getMoveCommand(addParts, referenceObject);
	}
}