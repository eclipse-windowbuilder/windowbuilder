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
package org.eclipse.wb.internal.os.linux;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

/**
 * This class contains native functions for various libraries.
 */
public class OS extends Native {

	protected static final SymbolLookup GOBJECT;

	static {
		GOBJECT = SymbolLookup.libraryLookup("libgobject-2.0.so.0", Arena.ofAuto());
	}

	private static class InstanceHolder {
		private static final MethodHandle g_object_unref = createHandle(GOBJECT, "g_object_unref",
				FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));
	}

	public static void g_object_unref(GObject object) {
		runSafe(() -> InstanceHolder.g_object_unref.invoke(object.segment()));
	}
}
