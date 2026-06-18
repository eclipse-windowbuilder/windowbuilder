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

import org.eclipse.wb.internal.os.linux.Cairo;
import org.eclipse.wb.internal.os.linux.GTK;
import org.eclipse.wb.internal.os.linux.GtkAllocation;
import org.eclipse.wb.internal.os.linux.GtkWidget;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

/**
 * The GTK toolkit compatible with GTK 3.x
 */
public final class GTK3 extends GTK {

	private static class InstanceHolder {
		private static final MethodHandle gtk_window_set_keep_above = createHandle(GTK, "gtk_window_set_keep_above",
				FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_BOOLEAN));

		private static final MethodHandle gtk_widget_get_preferred_size = createHandle(GTK, "gtk_widget_get_preferred_size",
				FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS));

		private static final MethodHandle gtk_widget_size_allocate = createHandle(GTK, "gtk_widget_size_allocate",
				FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS));

		private static final MethodHandle gtk_widget_draw = createHandle(GTK, "gtk_widget_draw",
				FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS));

		private static final MethodHandle gtk_widget_show_now = createHandle(GTK, "gtk_widget_show_now",
				FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));
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

	/**
	 * Retrieves the minimum and natural size of a widget, taking into account the
	 * widget’s preference for height-for-width management.
	 *
	 * This is used to retrieve a suitable size by container widgets which do not
	 * impose any restrictions on the child placement. It can be used to deduce
	 * toplevel window and menu sizes as well as child widgets in free-form
	 * containers such as {@code GtkFixed}.
	 *
	 * Handle with care. Note that the natural height of a height-for-width widget
	 * will generally be a smaller size than the minimum height, since the required
	 * height for the natural width is generally smaller than the required height
	 * for the minimum width.
	 *
	 * Use <a href=
	 * "https://docs.gtk.org/gtk4/method.Widget.measure.html">gtk_widget_measure()</a>
	 * if you want to support baseline alignment.
	 *
	 * @param minimum_size Location for storing the minimum size. Can be
	 *                     {@code NULL}.
	 * @param natural_size Location for storing the natural size. Can be
	 *                     {@code NULL}.
	 */
	public static void gtk_widget_get_preferred_size(GtkWidget widget, GtkRequisition minimum_size, GtkRequisition natural_size) {
		runSafe(() -> InstanceHolder.gtk_widget_get_preferred_size.invoke(widget.segment(), minimum_size.segment(), natural_size.segment()));
	}

	/**
	 * This function is only used by {@code GtkContainer} subclasses, to assign a
	 * size and position to their child widgets.
	 *
	 * In this function, the allocation may be adjusted. It will be forced to a 1x1
	 * minimum size, and the adjust_size_allocation virtual method on the child will
	 * be used to adjust the allocation. Standard adjustments include removing the
	 * widget’s margins, and applying the widget’s {@code GtkWidget:halign} and
	 * {@code GtkWidget:valign} properties.
	 *
	 * For baseline support in containers you need to use
	 * {@code gtk_widget_size_allocate_with_baseline()} instead.
	 *
	 * @param allocation Position and size to be allocated to {@code widget}.
	 */
	public static void gtk_widget_size_allocate(GtkWidget widget, GtkAllocation allocation) {
		runSafe(() -> InstanceHolder.gtk_widget_size_allocate.invoke(widget.segment(), allocation.segment()));
	}

	/**
	 * Draws {@code widget} to {@code cr}. The top left corner of the widget will be
	 * drawn to the currently set origin point of {@code cr}.
	 *
	 * You should pass a cairo context as {@code cr} argument that is in an original
	 * state. Otherwise the resulting drawing is undefined. For example changing the
	 * operator using {@code cairo_set_operator()} or the line width using
	 * {@code cairo_set_line_width()} might have unwanted side effects. You may
	 * however change the context’s transform matrix - like with
	 * {@code cairo_scale(),}, {@code cairo_translate()} or
	 * {@code cairo_set_matrix()} and clip region with {@code cairo_clip()} prior to
	 * calling this function. Also, it is fine to modify the context with
	 * {@code cairo_save()} and {@code cairo_push_group()} prior to calling this
	 * function.
	 *
	 * Note that special-purpose widgets may contain special code for rendering to
	 * the screen and might appear differently on screen and when rendered using
	 * gtk_widget_draw().
	 *
	 * @param cr     A cairo context to draw to.
	 */
	public static void gtk_widget_draw(GtkWidget widget, Cairo cr) {
		runSafe(() -> InstanceHolder.gtk_widget_draw.invoke(widget.segment(), cr.segment()));
	}
}
