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
package org.eclipse.wb.internal.os.linux.gtk4;

import org.eclipse.wb.internal.os.linux.GTK;
import org.eclipse.wb.internal.os.linux.GtkWidget;
import org.eclipse.wb.internal.os.linux.cairo.CairoContext;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

/**
 * The GTK toolkit compatible with GTK 4.x
 */
public class GTK4 extends GTK {

	private static class InstanceHolder {
		private static final MethodHandle gtk_widget_get_height = createHandle(GTK, "gtk_widget_get_height",
				FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS));

		private static final MethodHandle gtk_widget_get_width = createHandle(GTK, "gtk_widget_get_width",
				FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS));

		private static final MethodHandle gtk_widget_paintable_new = createHandle(GTK, "gtk_widget_paintable_new",
				FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS));

		private static final MethodHandle gtk_snapshot_new = createHandle(GTK, "gtk_snapshot_new",
				FunctionDescriptor.of(ValueLayout.ADDRESS));

		private static final MethodHandle gtk_snapshot_free_to_node = createHandle(GTK, "gtk_snapshot_free_to_node",
				FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS));

		private static final MethodHandle gsk_render_node_draw = createHandle(GTK, "gsk_render_node_draw",
				FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS));

		private static final MethodHandle gsk_render_node_unref = createHandle(GTK, "gsk_render_node_unref",
				FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));

		private static final MethodHandle gdk_paintable_snapshot = createHandle(GTK, "gdk_paintable_snapshot",
				FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE));
	}

	/**
	 * Returns the content height of the widget. This function returns the height
	 * passed to its size-allocate implementation, which is the height you should be
	 * using in <a href=
	 * "https://docs.gtk.org/gtk4/vfunc.Widget.snapshot.html">Gtk.WidgetClass.snapshot</a>.
	 *
	 * For pointer events, see <a href=
	 * "https://docs.gtk.org/gtk4/method.Widget.contains.html">gtk_widget_contains()</a>.
	 *
	 * To learn more about widget sizes, see the coordinate system <a
	 * href="https://docs.gtk.org/gtk4/coordinates.html" overview</a>.
	 *
	 * @param widget
	 * @return The height of {@code widget}.
	 */
	public static int gtk_widget_get_height(GtkWidget widget) {
		return (int) callSafe(() -> InstanceHolder.gtk_widget_get_height.invoke(widget.segment()));
	}

	/**
	 * Returns the content width of the widget. This function returns the width
	 * passed to its size-allocate implementation, which is the width you should be
	 * using in <a href=
	 * "https://docs.gtk.org/gtk4/vfunc.Widget.snapshot.html">Gtk.WidgetClass.snapshot</a>.
	 *
	 * For pointer events, see <a href=
	 * "https://docs.gtk.org/gtk4/method.Widget.contains.html">gtk_widget_contains()</a>.
	 *
	 * To learn more about widget sizes, see the coordinate system <a
	 * href="https://docs.gtk.org/gtk4/coordinates.html" overview</a>.
	 *
	 * @param widget
	 * @return The width of {@code widget}.
	 */
	public static int gtk_widget_get_width(GtkWidget widget) {
		return (int) callSafe(() -> InstanceHolder.gtk_widget_get_width.invoke(widget.segment()));
	}

	public static GtkPaintable gtk_widget_paintable_new(GtkWidget widget) {
		MemorySegment segment = (MemorySegment) callSafe(() -> InstanceHolder.gtk_widget_paintable_new.invoke(widget.segment()));
		return new GtkPaintable(segment);
	}

	public static GtkSnapshot gtk_snapshot_new() {
		MemorySegment segment = (MemorySegment) callSafe(() -> InstanceHolder.gtk_snapshot_new.invoke());
		return new GtkSnapshot(segment);
	}

	public static GskRenderNode gtk_snapshot_free_to_node(GtkSnapshot snapshot) {
		MemorySegment segment = (MemorySegment) callSafe(() -> InstanceHolder.gtk_snapshot_free_to_node.invoke(snapshot.segment()));
		return new GskRenderNode(segment);
	}

	public static void gsk_render_node_draw(GskRenderNode node, CairoContext cr) {
		runSafe(() -> InstanceHolder.gsk_render_node_draw.invoke(node.segment(), cr.segment()));
	}

	public static void gsk_render_node_unref(GskRenderNode node) {
		runSafe(() -> InstanceHolder.gsk_render_node_unref.invoke(node.segment()));
	}

	public static void gdk_paintable_snapshot(GdkPaintable paintable, GdkSnapshot snapshot, double width, double height) {
		runSafe(() -> InstanceHolder.gdk_paintable_snapshot.invoke(paintable.segment(), snapshot.segment(), width, height));
	}
}
