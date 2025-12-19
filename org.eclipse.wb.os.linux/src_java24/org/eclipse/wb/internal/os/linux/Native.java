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

import org.apache.commons.lang3.function.FailableRunnable;
import org.apache.commons.lang3.function.FailableSupplier;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.invoke.MethodHandle;

/**
 * Base class for all native methods calls using the FFM API.
 */
public abstract class Native {

	private static final Linker LINKER = Linker.nativeLinker();

	protected static final boolean isGtk4() {
		return "1".equals(System.getenv("SWT_GTK4"));
	}

	protected static MethodHandle createHandle(SymbolLookup sl, String name, FunctionDescriptor descriptor) {
		MemorySegment symbol = sl.find(name).orElseThrow(UnsatisfiedLinkError::new);
		return LINKER.downcallHandle(symbol, descriptor);
	}

	protected static Object callSafe(FailableSupplier<Object, Throwable> s) {
		try {
			return s.get();
		} catch (Error e) {
			throw e;
		} catch (Throwable t) {
			throw new GtkRuntimeException(t);
		}
	}

	protected static void runSafe(FailableRunnable<Throwable> r) {
		try {
			r.run();
		} catch (Error e) {
			throw e;
		} catch (Throwable t) {
			throw new GtkRuntimeException(t);
		}
	}
}
