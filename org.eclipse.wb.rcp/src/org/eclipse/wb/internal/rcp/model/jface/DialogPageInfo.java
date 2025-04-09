/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
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
package org.eclipse.wb.internal.rcp.model.jface;

import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.internal.core.model.TopBoundsSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.swt.widgets.Shell;

/**
 * Abstract model for {@link DialogPage} subclasses.
 *
 * @author scheglov_ke
 * @coverage rcp.model.jface
 */
public abstract class DialogPageInfo extends AbstractComponentInfo {
	protected Shell m_shell;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public DialogPageInfo(AstEditor editor,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(editor, description, creationSupport);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// AbstractComponentInfo
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected TopBoundsSupport createTopBoundsSupport() {
		return new DialogPageTopBoundsSupport(this);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Hierarchy
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean canBeRoot() {
		return true;
	}

	@Override
	public Shell getComponentObject() {
		return m_shell;
	}

	/**
	 * @return the {@link DialogPageInfo}'s Shell.
	 */
	Shell getShell() {
		return m_shell;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Refresh
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void refresh_fetch() throws Exception {
		ControlInfo.refresh_fetch(this, new RunnableEx() {
			@Override
			public void run() throws Exception {
				DialogPageInfo.super.refresh_fetch();
			}
		});
	}
}
