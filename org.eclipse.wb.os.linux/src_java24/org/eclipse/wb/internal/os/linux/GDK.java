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

import org.eclipse.wb.internal.os.linux.cairo.CairoContext;
import org.eclipse.wb.internal.os.linux.cairo.CairoRegion;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

public class GDK extends Native {
	protected static final SymbolLookup GDK;

	static {
		if (isGtk4()) {
			GDK = SymbolLookup.libraryLookup("libgdk-4.so.0", Arena.ofAuto());
		} else {
			GDK = SymbolLookup.libraryLookup("libgdk-3.so.0", Arena.ofAuto());
		}
	}

	private static class InstanceHolder {
		private static final MethodHandle gdk_cairo_region = createHandle(GDK, "gdk_cairo_region",
				FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS));
	}

	public static void gdk_cairo_region(CairoContext cr, CairoRegion region) {
		MemorySegment segment1 = MemorySegment.ofAddress(cr.handle());
		MemorySegment segment2 = MemorySegment.ofAddress(region.handle());
		runSafe(() -> InstanceHolder.gdk_cairo_region.invoke(segment1, segment2));
	}
}
