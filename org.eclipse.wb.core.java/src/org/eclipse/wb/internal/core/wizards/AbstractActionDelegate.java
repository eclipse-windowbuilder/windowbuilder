/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
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
package org.eclipse.wb.internal.core.wizards;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * Abstract {@link IWorkbenchWindowActionDelegate} that performs some operation.
 *
 * @author scheglov_ke
 * @coverage core.wizards.ui
 */
public abstract class AbstractActionDelegate
implements
IWorkbenchWindowActionDelegate,
IObjectActionDelegate {
	private IWorkbenchWindow m_window;
	private IStructuredSelection m_selection;

	////////////////////////////////////////////////////////////////////////////
	//
	// IWorkbenchWindowActionDelegate
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void init(IWorkbenchWindow window) {
		m_window = window;
	}

	@Override
	public void dispose() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IObjectActionDelegate
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		m_window = targetPart.getSite().getWorkbenchWindow();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IWorkbenchWindowActionDelegate + IObjectActionDelegate
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			m_selection = (IStructuredSelection) selection;
		} else {
			m_selection = null;
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Internal access
	//
	////////////////////////////////////////////////////////////////////////////
	protected final IWorkbenchWindow getWorkbenchWindow() {
		return m_window;
	}

	protected final IStructuredSelection getSelection() {
		return m_selection;
	}
}