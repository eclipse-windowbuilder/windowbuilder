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
package org.eclipse.wb.internal.swt.gef.part;

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.internal.swt.gef.policy.menu.MenuBarDropLayoutEditPolicy;
import org.eclipse.wb.internal.swt.model.widgets.ShellInfo;

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