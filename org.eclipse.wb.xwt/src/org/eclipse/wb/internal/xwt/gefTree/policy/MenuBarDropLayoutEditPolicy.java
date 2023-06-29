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
package org.eclipse.wb.internal.xwt.gefTree.policy;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.tree.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.xwt.gef.policy.MenuBarDropLayoutEditPolicy.MenuBarDrop_Validator;
import org.eclipse.wb.internal.xwt.model.widgets.ShellInfo;
import org.eclipse.wb.internal.xwt.model.widgets.menu.MenuInfo;

/**
 * {@link LayoutEditPolicy} allowing drop "bar" {@link MenuInfo} on {@link ShellInfo}.
 *
 * @author mitin_aa
 * @coverage XWT.gefTree.policy
 */
public class MenuBarDropLayoutEditPolicy extends LayoutEditPolicy {
	private final ShellInfo m_shell;
	private final ILayoutRequestValidator m_validator;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public MenuBarDropLayoutEditPolicy(ShellInfo shell) {
		m_shell = shell;
		m_validator = new MenuBarDrop_Validator(shell);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Policy/Validator
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected ILayoutRequestValidator getRequestValidator() {
		return m_validator;
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
				menu.commandCreate(m_shell);
			}
		};
	}
}
