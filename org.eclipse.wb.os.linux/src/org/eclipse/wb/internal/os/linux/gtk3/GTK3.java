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

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

public final class GTK3 {
	private static class InstanceHolder {
		private static final Linker linker = Linker.nativeLinker();
		private static final SymbolLookup gtk = SymbolLookup.libraryLookup("libgtk-3.so", Arena.ofAuto());

		private static MethodHandle createHandle(String name, FunctionDescriptor descriptor) {
			MemorySegment symbol = gtk.find(name).orElseThrow(UnsatisfiedLinkError::new);
			return InstanceHolder.linker.downcallHandle(symbol, descriptor);
		}

		static final MethodHandle gtk_get_major_version = createHandle("gtk_get_major_version",
				FunctionDescriptor.of(ValueLayout.JAVA_INT));

		static final MethodHandle gtk_get_minor_version = createHandle("gtk_get_minor_version",
				FunctionDescriptor.of(ValueLayout.JAVA_INT));

		static final MethodHandle gtk_get_micro_version = createHandle("gtk_get_micro_version",
				FunctionDescriptor.of(ValueLayout.JAVA_INT));
	}

	public static int gtk_get_major_version() throws Throwable {
		return (int) InstanceHolder.gtk_get_major_version.invoke();
	}

	public static int gtk_get_minor_version() throws Throwable {
		return (int) InstanceHolder.gtk_get_minor_version.invoke();
	}

	public static int gtk_get_micro_version() throws Throwable {
		return (int) InstanceHolder.gtk_get_micro_version.invoke();
	}
}
