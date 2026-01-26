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
package org.eclipse.wb.internal.swt.gef.part;

import org.eclipse.wb.internal.swt.gef.policy.menu.MenuBarDropLayoutEditPolicy;
import org.eclipse.wb.internal.swt.model.widgets.ShellInfo;

import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for {@link ShellInfo}.
 *
 * @author scheglov_ke
 * @coverage swt.gef.part
 */
public class ShellEditPart extends CompositeEditPart {
	private final ShellInfo m_shell;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ShellEditPart(ShellInfo shell) {
		super(shell);
		m_shell = shell;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Policies
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void createEditPolicies() {
		super.createEditPolicies();
		// support for dropping menu bar when this EditPart is Shell
		if (canAcceptMenuBar()) {
			installEditPolicy(new MenuBarDropLayoutEditPolicy(m_shell));
		}
	}

	private boolean canAcceptMenuBar() {
		// sometimes Shell is exposed as part of complex object, for example ApplicationWindow
		if (!m_shell.isRoot()) {
			return false;
		}
		// OK
		return true;
	}
}