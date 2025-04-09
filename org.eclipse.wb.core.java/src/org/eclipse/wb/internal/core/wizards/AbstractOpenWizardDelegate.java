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
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.IWorkbenchWizard;

/**
 * Abstract {@link IWorkbenchWindowActionDelegate} that opens {@link IWizard}.
 *
 * @coverage core.wizards.ui
 */
public abstract class AbstractOpenWizardDelegate extends AbstractActionDelegate {
	////////////////////////////////////////////////////////////////////////////
	//
	// IActionDelegate
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void run(IAction action) {
		openWizard();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Wizard
	//
	////////////////////////////////////////////////////////////////////////////
	protected final void openWizard() {
		IWorkbenchWindow workbenchWindow = getWorkbenchWindow();
		IWizard wizard = createWizard();
		// initialize IWorkbenchWizard
		if (wizard instanceof IWorkbenchWizard) {
			((IWorkbenchWizard) wizard).init(workbenchWindow.getWorkbench(), getSelection());
		}
		// open Wizard UI
		WizardDialog dialog = new WizardDialog(workbenchWindow.getShell(), wizard);
		dialog.create();
		String title = wizard.getWindowTitle();
		if (title != null) {
			dialog.getShell().setText(title);
		}
		dialog.open();
	}

	/**
	 * Creates the specific wizard (to be implemented by a subclass).
	 */
	protected abstract IWizard createWizard();
}