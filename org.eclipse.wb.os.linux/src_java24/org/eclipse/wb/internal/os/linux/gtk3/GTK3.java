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

import org.eclipse.wb.internal.os.linux.GTK;
import org.eclipse.wb.internal.os.linux.GtkWidget;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

/**
 * The GTK toolkit compatible with GTK 3.x
 */
public final class GTK3 extends GTK {

	private static class InstanceHolder {
		private static final MethodHandle gtk_widget_get_window = createHandle(GTK, "gtk_widget_get_window",
				FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS));

		private static final MethodHandle gtk_window_set_keep_above = createHandle(GTK, "gtk_window_set_keep_above",
				FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_BOOLEAN));

		private static final MethodHandle gtk_widget_show_now = createHandle(GTK, "gtk_widget_show_now",
				FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));
	}

	/**
	 * Returns the widget’s window if it is realized, {@code NULL} otherwise.
	 *
	 * @return {@code widget}’s window.
	 */
	public static GdkWindow gtk_widget_get_window(GtkWidget widget) {
		MemorySegment handle = (MemorySegment) callSafe(() -> InstanceHolder.gtk_widget_get_window.invoke(widget.segment()));
		return new GdkWindow(handle);
	}

	/**
	 * Shows a widget. If the widget is an unmapped toplevel widget (i.e. a
	 * {@code GtkWindow} that has not yet been shown), enter the main loop and wait
	 * for the window to actually be mapped. Be careful; because the main loop is
	 * running, anything can happen during this function.
	 */
	public static void gtk_widget_show_now(GtkWidget widget) {
		runSafe(() -> InstanceHolder.gtk_widget_show_now.invoke(widget.segment()));
	}

	/**
	 * Asks to keep {@code window} above, so that it stays on top. Note that you
	 * shouldn’t assume the window is definitely above afterward, because other
	 * entities (e.g. the user or [window manager][gtk-X11-arch]) could not keep it
	 * above, and not all window managers support keeping windows above. But
	 * normally the window will end kept above. Just don’t write code that crashes
	 * if not.
	 *
	 * It’s permitted to call this function before showing a window, in which case
	 * the window will be kept above when it appears onscreen initially.
	 *
	 * You can track the above state via the “window-state-event” signal on
	 * {@code GtkWidget}.
	 *
	 * Note that, according to the Extended Window Manager Hints Specification, the
	 * above state is mainly meant for user preferences and should not be used by
	 * applications e.g. for drawing attention to their dialogs.
	 *
	 * @param setting Whether to keep {@code window} above other windows.
	 */
	public static void gtk_window_set_keep_above(GtkWindow window, boolean setting) {
		runSafe(() -> InstanceHolder.gtk_window_set_keep_above.invoke(window.segment(), setting));
	}
}
