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
package org.eclipse.wb.internal.swing.model;

import org.eclipse.wb.internal.core.parser.IParseRealm;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.swt.SwtCallable;

import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

/**
 * Default implementation of the parse realm backed by the AWT event dispatcher
 * thread.
 */
public class SwingParseRealm implements IParseRealm {
	private static IParseRealm INSTANCE = new SwingParseRealm();

	private SwingParseRealm() {
	}

	public static IParseRealm getRealm() {
		return INSTANCE;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T syncCall(SwtCallable<T, Exception> c) throws Exception {
		Object[] result = new Object[1];
		try {
			SwingUtilities.invokeAndWait(() -> {
				result[0] = ExecutionUtils.runObject(c::call);
			});
		} catch (InvocationTargetException e) {
			ReflectionUtils.propagate(e.getCause());
		}
		return (T) result[0];
	}
}
