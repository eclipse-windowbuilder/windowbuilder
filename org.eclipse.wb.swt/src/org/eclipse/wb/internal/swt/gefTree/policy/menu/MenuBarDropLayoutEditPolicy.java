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
package org.eclipse.wb.internal.swt.gefTree.policy.menu;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator.LayoutRequestValidatorStubFalse;
import org.eclipse.wb.gef.core.requests.CreateRequest;
import org.eclipse.wb.gef.tree.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.menu.MenuInfo;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;

/**
 * {@link LayoutEditPolicy} allowing drop "bar" {@link MenuInfo} on <code>Shell</code>.
 *
 * @author mitin_aa
 * @coverage swt.gefTree.policy.menu
 */
public class MenuBarDropLayoutEditPolicy extends LayoutEditPolicy {
	private final CompositeInfo m_shell;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public MenuBarDropLayoutEditPolicy(CompositeInfo shell) {
		m_shell = shell;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Policy/Validator
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected ILayoutRequestValidator getRequestValidator() {
		return VALIDATOR;
	}

	@Override
	protected boolean isGoodReferenceChild(Request request, EditPart editPart) {
		return false;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Command
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected Command getCreateCommand(final Object newObject, Object referenceObject) {
		final MenuInfo menu = (MenuInfo) newObject;
		return new EditCommand(m_shell) {
			@Override
			protected void executeEdit() throws Exception {
				menu.command_CREATE(m_shell);
			}
		};
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Validator instance
	//
	////////////////////////////////////////////////////////////////////////////
	private final ILayoutRequestValidator VALIDATOR = new LayoutRequestValidatorStubFalse() {
		@Override
		public boolean validateCreateRequest(EditPart host, CreateRequest request) {
			// only one "bar"
			for (MenuInfo menuInfo : m_shell.getChildren(MenuInfo.class)) {
				if (menuInfo.isBar()) {
					return false;
				}
			}
			// check object
			Object newObject = request.getNewObject();
			if (newObject instanceof MenuInfo) {
				return ((MenuInfo) newObject).isBar();
			}
			// unknown object
			return false;
		}
	};
}
