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

import org.eclipse.wb.internal.os.linux.GDK;
import org.eclipse.wb.internal.os.linux.cairo.CairoContext;
import org.eclipse.wb.internal.os.linux.cairo.CairoRegion;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

/**
 * The GDK toolkit compatible with GDK 3.x
 */
public class GDK3 extends GDK {

	private static class InstanceHolder {
		private static final MethodHandle gdk_cairo_set_source_window = createHandle(GDK, "gdk_cairo_set_source_window",
				FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE));

		private static final MethodHandle gdk_window_get_height = createHandle(GDK, "gdk_window_get_height",
				FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS));

		private static final MethodHandle gdk_window_get_visible_region = createHandle(GDK, "gdk_window_get_visible_region",
				FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS));

		private static final MethodHandle gdk_window_get_width = createHandle(GDK, "gdk_window_get_width",
				FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS));

		private static final MethodHandle gdk_window_is_visible = createHandle(GDK, "gdk_window_is_visible",
				FunctionDescriptor.of(ValueLayout.JAVA_BOOLEAN, ValueLayout.ADDRESS));

		private static final MethodHandle gdk_window_process_updates = createHandle(GDK, "gdk_window_process_updates",
				FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_BOOLEAN));
	}

	/**
	 * Sets the given window as the source pattern for {@code cr}.
	 *
	 * The pattern has an extend mode of {@code CAIRO_EXTEND_NONE} and is aligned so
	 * that the origin of window is {@code x}, {@code y}. The window contains all
	 * its subwindows when rendering.
	 *
	 * Note that the contents of window are undefined outside of the visible part of
	 * {@code window}, so use this function with care.
	 *
	 * @param cr     A cairo context.
	 * @param window A {@link GdkWindow}.
	 * @param x      X coordinate of location to place upper left corner of
	 *               {@code window}.
	 * @param y      Y coordinate of location to place upper left corner of
	 *               {@code window}.
	 */
	public static void gdk_cairo_set_source_window(CairoContext cr, GdkWindow window, double x, double y) {
		runSafe(() -> InstanceHolder.gdk_cairo_set_source_window.invoke(cr.segment(), window.segment(), x, y));
	}

	/**
	 * Returns the height of the given window.
	 *
	 * On the X11 platform the returned size is the size reported in the
	 * most-recently-processed configure event, rather than the current size on the
	 * X server.
	 *
	 * @return The height of {@code window}.
	 */
	public static int gdk_window_get_height(GdkWindow window) {
		return (int) callSafe(() -> InstanceHolder.gdk_window_get_height.invoke(window.segment()));
	}

	/**
	 * Computes the region of the {@code window} that is potentially visible. This
	 * does not necessarily take into account if the window is obscured by other
	 * windows, but no area outside of this region is visible.
	 *
	 * @return A {@link CairoRegion}. This must be freed with cairo_region_destroy()
	 *         when you are done.
	 */
	public static CairoRegion gdk_window_get_visible_region(GdkWindow window) {
		MemorySegment handle = (MemorySegment) callSafe(() -> InstanceHolder.gdk_window_get_visible_region.invoke(window.segment()));
		return new CairoRegion(handle);
	}

	/**
	 * Returns the width of the given window.
	 *
	 * On the X11 platform the returned size is the size reported in the
	 * most-recently-processed configure event, rather than the current size on the
	 * X server.
	 *
	 * @return The width of {@code window}.
	 */
	public static int gdk_window_get_width(GdkWindow window) {
		return (int) callSafe(() -> InstanceHolder.gdk_window_get_width.invoke(window.segment()));
	}

	/**
	 * Checks whether the window has been mapped (with {@code gdk_window_show()} or
	 * {@code gdk_window_show_unraised())}.
	 *
	 * @return {@code true} if the window is mapped.
	 */
	public static boolean gdk_window_is_visible(GdkWindow window) {
		return (boolean) callSafe(() -> InstanceHolder.gdk_window_is_visible.invoke(window.segment()));
	}

	/**
	 * Sends one or more expose events to {@code window}. The areas in each expose
	 * event will cover the entire update area for the window (see
	 * {@code gdk_window_invalidate_region()} for details). Normally GDK calls
	 * {@code gdk_window_process_all_updates()} on your behalf, so there’s no need
	 * to call this function unless you want to force expose events to be delivered
	 * immediately and synchronously (vs. the usual case, where GDK delivers them in
	 * an idle handler). Occasionally this is useful to produce nicer scrolling
	 * behavior, for example.
	 *
	 * @param update_children Whether to also process updates for child windows.
	 */
	public static void gdk_window_process_updates(GdkWindow window, boolean update_children) {
		runSafe(() -> InstanceHolder.gdk_window_process_updates.invoke(window.segment(), update_children));
	}
}
