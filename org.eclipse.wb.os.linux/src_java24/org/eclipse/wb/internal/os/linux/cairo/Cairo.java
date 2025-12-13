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
package org.eclipse.wb.internal.os.linux.cairo;

import org.eclipse.wb.internal.os.linux.Native;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

public class Cairo extends Native {
	private static final SymbolLookup CAIRO = SymbolLookup.libraryLookup("libcairo.so.2", Arena.ofAuto());

	private static final MethodHandle cairo_clip = createHandle(CAIRO, "cairo_clip",
			FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));

	private static final MethodHandle cairo_create = createHandle(CAIRO, "cairo_create",
			FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS));

	private static final MethodHandle cairo_destroy = createHandle(CAIRO, "cairo_destroy",
			FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));

	private static final MethodHandle cairo_image_surface_create = createHandle(CAIRO, "cairo_image_surface_create",
			FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT));

	private static final MethodHandle cairo_paint = createHandle(CAIRO, "cairo_paint",
			FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));

	private static final MethodHandle cairo_region_destroy = createHandle(CAIRO, "cairo_region_destroy",
			FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));

	private static final MethodHandle cairo_set_operator = createHandle(CAIRO, "cairo_set_operator",
			FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT));

	private static final MethodHandle cairo_surface_flush = createHandle(CAIRO, "cairo_surface_flush",
			FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));

	public static void cairo_clip(CairoContext cr) {
		MemorySegment segment = MemorySegment.ofAddress(cr.handle());
		runSafe(() -> cairo_clip.invoke(segment));
	}

	public static CairoContext cairo_create(CairoSurface target) {
		MemorySegment segment = MemorySegment.ofAddress(target.handle());
		MemorySegment handle = (MemorySegment) callSafe(() -> cairo_create.invoke(segment));
		return new CairoContext(handle.address());
	}

	public static void cairo_destroy(CairoContext cr) {
		MemorySegment segment = MemorySegment.ofAddress(cr.handle());
		runSafe(() -> cairo_destroy.invoke(segment));
	}

	public static CairoSurface cairo_image_surface_create(CairoFormat format, int width, int height) {
		MemorySegment handle = (MemorySegment) callSafe(() -> cairo_image_surface_create.invoke(format.getValue(), width, height));
		return new CairoSurface(handle.address());
	}

	public static void cairo_paint(CairoContext cr) {
		MemorySegment segment = MemorySegment.ofAddress(cr.handle());
		runSafe(() -> cairo_paint.invoke(segment));
	}

	public static void cairo_region_destroy(CairoRegion region) {
		MemorySegment segment = MemorySegment.ofAddress(region.handle());
		runSafe(() -> cairo_region_destroy.invoke(segment));
	}

	public static void cairo_set_operator(CairoContext cr, CairoOperator op) {
		MemorySegment segment = MemorySegment.ofAddress(cr.handle());
		runSafe(() -> cairo_set_operator.invoke(segment, op.getValue()));
	}

	public static void cairo_surface_flush(CairoSurface surface) {
		MemorySegment segment = MemorySegment.ofAddress(surface.handle());
		runSafe(() -> cairo_surface_flush.invoke(segment));
	}
}
