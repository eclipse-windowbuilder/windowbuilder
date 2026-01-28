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
package org.eclipse.wb.internal.os.linux;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

/**
 * The GTK toolkit
 */
public abstract class GTK extends Native {
	protected static final SymbolLookup GTK;

	static {
		if (isGtk4()) {
			GTK = SymbolLookup.libraryLookup("libgtk-4.so.0", Arena.ofAuto());
		} else {
			GTK = SymbolLookup.libraryLookup("libgtk-3.so.0", Arena.ofAuto());
		}
	}

	private static class InstanceHolder {
		private static final MethodHandle gtk_widget_get_allocated_baseline = createHandle(GTK, "gtk_widget_get_allocated_baseline",
				FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS));

		private static final MethodHandle gtk_get_major_version = createHandle(GTK, "gtk_get_major_version",
				FunctionDescriptor.of(ValueLayout.JAVA_INT));

		private static final MethodHandle gtk_get_minor_version = createHandle(GTK, "gtk_get_minor_version",
				FunctionDescriptor.of(ValueLayout.JAVA_INT));

		private static final MethodHandle gtk_get_micro_version = createHandle(GTK, "gtk_get_micro_version",
				FunctionDescriptor.of(ValueLayout.JAVA_INT));

		private static final MethodHandle gtk_widget_get_allocation = createHandle(GTK, "gtk_widget_get_allocation",
				FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS));

		private static final MethodHandle gtk_widget_hide = createHandle(GTK, "gtk_widget_hide",
				FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));
	}

	/**
	 * @return The major version number of the GTK+ library.
	 */
	public static int gtk_get_major_version() {
		return (int) callSafe(() -> InstanceHolder.gtk_get_major_version.invoke());
	}

	/**
	 * @return The minor version number of the GTK+ library.
	 */
	public static int gtk_get_minor_version() {
		return (int) callSafe(() -> InstanceHolder.gtk_get_minor_version.invoke());
	}

	/**
	 * @return The micro version number of the GTK+ library.
	 */
	public static int gtk_get_micro_version() {
		return (int) callSafe(() -> InstanceHolder.gtk_get_micro_version.invoke());
	}

	/**
	 * Retrieves the widget’s allocation.
	 *
	 * Note, when implementing a {@code GtkContainer}: a widget’s allocation will be
	 * its “adjusted” allocation, that is, the widget’s parent container typically
	 * calls {@code gtk_widget_size_allocate()} with an allocation, and that
	 * allocation is then adjusted (to handle margin and alignment for example)
	 * before assignment to the widget.
	 *
	 * {@code gtk_widget_get_allocation()} returns the adjusted allocation that was
	 * actually assigned to the widget. The adjusted allocation is guaranteed to be
	 * completely contained within the {@code gtk_widget_size_allocate()}
	 * allocation, however.
	 *
	 * So a {@code GtkContainer} is guaranteed that its children stay inside the
	 * assigned bounds, but not that they have exactly the bounds the container
	 * assigned. There is no way to get the original allocation assigned by
	 * {@code gtk_widget_size_allocate()}, since it isn’t stored; if a container
	 * implementation needs that information it will have to track it itself.
	 */
	public static void gtk_widget_get_allocation(GtkWidget widget, GtkAllocation allocation) {
		runSafe(() -> InstanceHolder.gtk_widget_get_allocation.invoke(widget.segment(), allocation.segment()));
	}

	/**
	 * Reverses the effects of {@code gtk_widget_show()}, causing the {@code widget}
	 * to be hidden (invisible to the user).
	 */
	public static void gtk_widget_hide(GtkWidget widget) {
		runSafe(() -> InstanceHolder.gtk_widget_hide.invoke(widget.segment()));
	}

	/**
	 * Returns the baseline that has currently been allocated to {@code widget} or
	 * -1, if none.
	 */
	public static int gtk_widget_get_allocated_baseline(GtkWidget widget) {
		return (int) callSafe(() -> InstanceHolder.gtk_widget_get_allocated_baseline.invoke(widget.segment()));
	}
}
