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
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;
import java.lang.foreign.ValueLayout;

/**
 * A GdkRectangle data type for representing rectangles.
 *
 * GdkRectangle is identical to cairo_rectangle_t. Together with Cairo’s
 * cairo_region_t data type, these are the central types for representing sets
 * of pixels.
 *
 * The intersection of two rectangles can be computed with
 * gdk_rectangle_intersect(); to find the union of two rectangles use
 * gdk_rectangle_union().
 *
 * The cairo_region_t type provided by Cairo is usually used for managing
 * non-rectangular clipping of graphical operations.
 *
 * The Graphene library has a number of other data types for regions and volumes
 * in 2D and 3D.
 */
public sealed class GdkRectangle permits GtkAllocation {
	private static final StructLayout LAYOUT = MemoryLayout.structLayout( //
			ValueLayout.JAVA_INT.withName("x"), //
			ValueLayout.JAVA_INT.withName("y"), //
			ValueLayout.JAVA_INT.withName("width"), //
			ValueLayout.JAVA_INT.withName("height"));

	private final MemorySegment segment;

	public GdkRectangle(Arena arena) {
		segment = arena.allocate(LAYOUT);
	}

	public final int x() {
		return segment.getAtIndex(ValueLayout.JAVA_INT, 0);
	}

	public final int y() {
		return segment.getAtIndex(ValueLayout.JAVA_INT, 1);
	}

	public final int width() {
		return segment.getAtIndex(ValueLayout.JAVA_INT, 2);
	}

	public final int height() {
		return segment.getAtIndex(ValueLayout.JAVA_INT, 3);
	}

	/**
	 * The underlying memory segment allocated by this rectangle.
	 */
	public final MemorySegment segment() {
		return segment;
	}
}
