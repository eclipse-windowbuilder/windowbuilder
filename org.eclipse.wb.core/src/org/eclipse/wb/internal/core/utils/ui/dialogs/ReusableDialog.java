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
package org.eclipse.wb.internal.core.utils.ui.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Dialog that does not disposes itself after close, so can be opened again.
 *
 * @author scheglov_ke
 * @coverage core.ui
 */
public class ReusableDialog extends Dialog {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	protected ReusableDialog(Shell parentShell) {
		super(parentShell);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Changes for reusing dialog
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public final int open() {
		// prepare Shell
		if (getShell() == null) {
			create();
		}
		Shell shell = getShell();
		// send event
		onBeforeOpen();
		// open the window
		shell.open();
		Display display = shell.getDisplay();
		while (shell.getVisible()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		// result code
		return getReturnCode();
	}

	@Override
	public final boolean close() {
		getShell().setVisible(false);
		return true;
	}

	/**
	 * This method is invoked directly before opening dialog. This is good place for initializing
	 * controls from data.
	 */
	protected void onBeforeOpen() {
	}
}
