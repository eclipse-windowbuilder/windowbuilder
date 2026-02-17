/*******************************************************************************
 * Copyright (c) 2025 Patrick Ziegler and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Patrick Ziegler - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.os.linux.gtk3;

import org.eclipse.wb.internal.os.linux.GtkWidget;

import org.eclipse.swt.widgets.Shell;

/**
 * A GtkWindow is a toplevel window which can contain other widgets. Windows
 * normally have decorations that are under the control of the windowing system
 * and allow the user to manipulate the window (resize it, move it, close it,…).
 */
public class GtkWindow extends GtkWidget {
	public GtkWindow(Shell shell) {
		super(shell);
	}
}
