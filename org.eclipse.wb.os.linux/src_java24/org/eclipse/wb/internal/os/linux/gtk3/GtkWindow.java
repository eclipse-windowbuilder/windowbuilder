/*******************************************************************************
 * Copyright (c) 2026 Patrick Ziegler and others.
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

import org.eclipse.swt.widgets.Widget;

/**
 * A GtkWindow is a toplevel window which can contain other widgets. Windows
 * normally have decorations that are under the control of the windowing system
 * and allow the user to manipulate the window (resize it, move it, close it,…).
 */
public class GtkWindow extends GtkWidget {
	protected GtkWindow(long handle) {
		super(handle);
	}

	/**
	 * @param widget The SWT widget to create this object from.
	 * @return A new {@link GtkWidget} instance backed by the given {@link Widget}
	 *         handle.
	 */
	public static GtkWindow from(Widget widget) {
		return new GtkWindow(getHandle(widget));
	}
}
