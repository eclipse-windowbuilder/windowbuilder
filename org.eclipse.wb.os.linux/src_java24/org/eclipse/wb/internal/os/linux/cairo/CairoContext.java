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
package org.eclipse.wb.internal.os.linux.cairo;

import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.swt.graphics.GC;

import java.lang.foreign.MemorySegment;

/**
 * A {@code cairo_t} contains the current state of the rendering device,
 * including coordinates of yet to be drawn shapes.
 *
 * Cairo contexts, as {@code cairo_t} objects are named, are central to cairo
 * and all drawing with cairo is always done to a cairo_t object.
 */
public record CairoContext(MemorySegment segment) {
	/**
	 * Creates a new CairoContext instance associated with the GC object.
	 *
	 * @param gc The context to paint on.
	 * @return The Cairo context backed by the given GC.
	 */
	public static CairoContext from(GC gc) {
		long handle = getHandleValue(gc, "handle");
		return new CairoContext(MemorySegment.ofAddress(handle));
	}

	private static long getHandleValue(GC gc, String fieldName) {
		return ReflectionUtils.getFieldLong(gc, fieldName);
	}
}
