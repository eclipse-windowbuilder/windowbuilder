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
package org.eclipse.wb.internal.core.gefTree.policy.menu;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.core.requests.CreateRequest;
import org.eclipse.wb.gef.core.requests.PasteRequest;
import org.eclipse.wb.gef.tree.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.core.model.menu.IMenuInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuPolicy;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;

import java.util.List;

/**
 * {@link LayoutEditPolicy} for {@link IMenuInfo}.
 *
 * @author mitin_aa
 * @author scheglov_ke
 * @coverage core.gefTree.menu
 */
public final class MenuLayoutEditPolicy extends LayoutEditPolicy {
	private final ObjectInfo m_menuInfo;
	private final IMenuInfo m_menuObject;
	private final IMenuPolicy m_menuPolicy;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public MenuLayoutEditPolicy(ObjectInfo menuInfo, IMenuInfo menuObject) {
		m_menuInfo = menuInfo;
		m_menuObject = menuObject;
		m_menuPolicy = m_menuObject.getPolicy();
	}

	/////////////////////////////////////////////////////////////////////
	//
	// LayoutEditPolicy
	//
	/////////////////////////////////////////////////////////////////////
	@Override
	protected boolean isGoodReferenceChild(Request request, EditPart editPart) {
		return true;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Commands
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected Command getCreateCommand(final Object newObject, final Object referenceObject) {
		return new EditCommand(m_menuInfo) {
			@Override
			public void executeEdit() throws Exception {
				m_menuPolicy.commandCreate(newObject, referenceObject);
			}
		};
	}

	@Override
	protected Command getPasteCommand(final PasteRequest request, final Object referenceObject) {
		return new EditCommand(m_menuInfo) {
			@Override
			public void executeEdit() throws Exception {
				m_menuPolicy.commandPaste(request.getMemento(), referenceObject);
			}
		};
	}

	@Override
	protected Command getMoveCommand(final List<? extends EditPart> moveParts, final Object referenceObject) {
		return new EditCommand(m_menuInfo) {
			@Override
			public void executeEdit() throws Exception {
				for (EditPart editPart : moveParts) {
					m_menuPolicy.commandMove(editPart.getModel(), referenceObject);
				}
			}
		};
	}

	@Override
	protected Command getAddCommand(List<? extends EditPart> addParts, Object referenceObject) {
		return getMoveCommand(addParts, referenceObject);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Validator
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected ILayoutRequestValidator getRequestValidator() {
		return VALIDATOR;
	}

	private final ILayoutRequestValidator VALIDATOR = new ILayoutRequestValidator() {
		@Override
		public boolean validateCreateRequest(EditPart host, CreateRequest request) {
			return m_menuPolicy.validateCreate(request.getNewObject());
		}

		@Override
		public boolean validatePasteRequest(EditPart host, PasteRequest request) {
			return m_menuPolicy.validatePaste(request.getMemento());
		}

		@Override
		public boolean validateMoveRequest(EditPart host, ChangeBoundsRequest request) {
			for (EditPart editPart : request.getEditParts()) {
				if (!m_menuPolicy.validateMove(editPart.getModel())) {
					return false;
				}
			}
			return true;
		}

		@Override
		public boolean validateAddRequest(EditPart host, ChangeBoundsRequest request) {
			return validateMoveRequest(host, request);
		}
	};
}
