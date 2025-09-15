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

import org.eclipse.wb.internal.os.linux.GtkAllocation;
import org.eclipse.wb.internal.os.linux.GtkRuntimeException;
import org.eclipse.wb.internal.os.linux.GtkWidget;

import org.apache.commons.lang3.function.FailableRunnable;
import org.apache.commons.lang3.function.FailableSupplier;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

public final class GTK3 {
	private static class InstanceHolder {
		private static final Linker LINKER = Linker.nativeLinker();
		private static final SymbolLookup GTK = SymbolLookup.libraryLookup("libgtk-3.so", Arena.ofAuto());
		private static final SymbolLookup GDK = SymbolLookup.libraryLookup("libgdk-3.so", Arena.ofAuto());

		private static MethodHandle createHandle(SymbolLookup sl, String name, FunctionDescriptor descriptor) {
			MemorySegment symbol = sl.find(name).orElseThrow(UnsatisfiedLinkError::new);
			return LINKER.downcallHandle(symbol, descriptor);
		}

		static final MethodHandle gtk_get_major_version = createHandle(GTK, "gtk_get_major_version",
				FunctionDescriptor.of(ValueLayout.JAVA_INT));

		static final MethodHandle gtk_get_minor_version = createHandle(GTK, "gtk_get_minor_version",
				FunctionDescriptor.of(ValueLayout.JAVA_INT));

		static final MethodHandle gtk_get_micro_version = createHandle(GTK, "gtk_get_micro_version",
				FunctionDescriptor.of(ValueLayout.JAVA_INT));

		static final MethodHandle gtk_widget_get_allocation = createHandle(GTK, "gtk_widget_get_allocation",
				FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS));

		static final MethodHandle gtk_widget_get_opacity = createHandle(GTK, "gtk_widget_get_opacity",
				FunctionDescriptor.of(ValueLayout.JAVA_DOUBLE, ValueLayout.ADDRESS));

		static final MethodHandle gtk_widget_set_opacity = createHandle(GTK, "gtk_widget_set_opacity",
				FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_DOUBLE));

		static final MethodHandle gtk_widget_is_composited = createHandle(GTK, "gtk_widget_is_composited",
				FunctionDescriptor.of(ValueLayout.JAVA_BOOLEAN, ValueLayout.ADDRESS));

		static final MethodHandle gdk_window_is_visible = createHandle(GDK, "gdk_window_is_visible",
				FunctionDescriptor.of(ValueLayout.JAVA_BOOLEAN, ValueLayout.ADDRESS));

	}

	/**
	 * @return The major version number of the GTK+ library.
	 */
	public static int gtk_get_major_version() throws Throwable {
		return (int) InstanceHolder.gtk_get_major_version.invoke();
	}

	/**
	 * @return The minor version number of the GTK+ library.
	 */
	public static int gtk_get_minor_version() throws Throwable {
		return (int) InstanceHolder.gtk_get_minor_version.invoke();
	}

	/**
	 * @return The micro version number of the GTK+ library.
	 */
	public static int gtk_get_micro_version() throws Throwable {
		return (int) InstanceHolder.gtk_get_micro_version.invoke();
	}

	/**
	 * Retrieves the widget’s allocation.
	 *
	 * Note, when implementing a {@code GtkContainer}: a widget’s allocation will be
	 * its “adjusted” allocation, that is, the widget’s parent container typically
	 * calls {@code gtk_widget_size_allocate()} with an allocation, and that
	 * allocation is then adjusted (to handle margin and alignment for example)
	 * before assignment to the widget. {@code gtk_widget_get_allocation()} returns
	 * the adjusted allocation that was actually assigned to the widget. The
	 * adjusted allocation is guaranteed to be completely contained within the
	 * {@code gtk_widget_size_allocate()} allocation, however. So a
	 * {@code GtkContainer} is guaranteed that its children stay inside the assigned
	 * bounds, but not that they have exactly the bounds the container assigned.
	 * There is no way to get the original allocation assigned by
	 * {@code gtk_widget_size_allocate()}, since it isn’t stored; if a container
	 * implementation needs that information it will have to track it itself.
	 */
	public static void gtk_widget_get_allocation(GtkWidget widget, GtkAllocation allocation) {
		MemorySegment segment = MemorySegment.ofAddress(widget.handle());
		runSafe(() -> InstanceHolder.gtk_widget_get_allocation.invoke(segment, allocation.segment()));
	}

	/**
	 * Fetches the requested opacity for the widget.
	 *
	 * @return The requested opacity for this widget.
	 */
	public static double gtk_widget_get_opacity(GtkWidget widget) {
		MemorySegment segment = MemorySegment.ofAddress(widget.handle());
		return (double) callSafe(() -> InstanceHolder.gtk_widget_get_opacity.invoke(segment));
	}

	/**
	 * Requests the widget to be rendered partially transparent. An opacity of 0 is
	 * fully transparent and an opacity of 1 is fully opaque.
	 *
	 * Opacity works on both toplevel widgets and child widgets, although there are
	 * some limitations: For toplevel widgets, applying opacity depends on the
	 * capabilities of the windowing system. On X11, this has any effect only on X
	 * displays with a compositing manager, see {@code gdk_display_is_composited()}.
	 * On Windows and Wayland it will always work, although setting a window’s
	 * opacity after the window has been shown may cause some flicker.
	 *
	 * Note that the opacity is inherited through inclusion — if you set a toplevel
	 * to be partially translucent, all of its content will appear translucent,
	 * since it is ultimatively rendered on that toplevel. The opacity value itself
	 * is not inherited by child widgets (since that would make widgets deeper in
	 * the hierarchy progressively more translucent). As a consequence,
	 * {@code GtkPopover} instances and other {@code GtkNative} widgets with their
	 * own surface will use their own opacity value, and thus by default appear
	 * non-translucent, even if they are attached to a toplevel that is translucent.
	 *
	 * @param opacity Desired opacity, between 0 and 1.
	 */
	public static void gtk_widget_set_opacity(GtkWidget widget, double opacity) {
		MemorySegment segment = MemorySegment.ofAddress(widget.handle());
		runSafe(() -> InstanceHolder.gtk_widget_set_opacity.invoke(segment, opacity));
	}

	/**
	 * Whether {@code widget} can rely on having its alpha channel drawn correctly.
	 * On X11 this function returns whether a compositing manager is running for
	 * {@code widget} screen.
	 *
	 * @return {@code true} if the widget can rely on its alpha channel being drawn
	 *         correctly.
	 */
	@Deprecated(since = "3.22")
	public static boolean gtk_widget_is_composited(GtkWidget widget) {
		MemorySegment segment = MemorySegment.ofAddress(widget.handle());
		return (boolean) callSafe(() -> InstanceHolder.gtk_widget_is_composited.invoke(segment));
	}

	/**
	 * Checks whether the window has been mapped (with {@code gdk_window_show()} or
	 * {@code gdk_window_show_unraised())}.
	 *
	 * @return {@code true} if the window is mapped.
	 */
	public static boolean gdk_window_is_visible(GdkWindow window) throws Throwable {
		MemorySegment segment = MemorySegment.ofAddress(window.handle());
		return (boolean) InstanceHolder.gdk_window_is_visible.invoke(segment);
	}

	private static Object callSafe(FailableSupplier<Object, Throwable> s) {
		try {
			return s.get();
		} catch (Error e) {
			throw e;
		} catch (Throwable t) {
			throw new GtkRuntimeException(t);
		}
	}

	private static void runSafe(FailableRunnable<Throwable> r) {
		try {
			r.run();
		} catch (Error e) {
			throw e;
		} catch (Throwable t) {
			throw new GtkRuntimeException(t);
		}
	}
}
