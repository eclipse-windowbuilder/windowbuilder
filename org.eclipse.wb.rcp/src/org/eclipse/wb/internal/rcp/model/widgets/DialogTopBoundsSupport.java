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
package org.eclipse.wb.internal.rcp.model.widgets;

import org.eclipse.wb.internal.core.model.TopBoundsSupport;

/**
 * Implementation of {@link TopBoundsSupport} for {@link DialogInfo}.
 *
 * @author scheglov_ke
 * @author sablin_aa
 * @coverage rcp.model.widgets
 */
public final class DialogTopBoundsSupport extends TopBoundsSupport {
	private final TopBoundsSupport m_shell;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public DialogTopBoundsSupport(DialogInfo dialog) {
		super(dialog);
		m_shell = dialog.getShellInfo().getTopBoundsSupport();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// TopBoundsSupport
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void apply() throws Exception {
		m_shell.apply();
	}

	@Override
	public void setSize(int width, int height) throws Exception {
		m_shell.setSize(width, height);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Show
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean show() throws Exception {
		return m_shell.show();
	}
}