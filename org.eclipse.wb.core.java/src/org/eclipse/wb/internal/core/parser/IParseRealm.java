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
package org.eclipse.wb.internal.core.parser;

import org.eclipse.swt.SwtCallable;

/**
 * The parse realm defines the thread in which a compilation unit is evaluated.
 * Some UI toolkit require their components to be accessed from within their UI
 * thread (most noticeably SWT) while other don't (like Swing). However,
 * creating or accessing those components outside of their UI thread may still
 * cause errors and potentially deadlocks (when accessed from a different UI
 * thread).
 */
public interface IParseRealm {
	// SwtCallable instead of Callable for compatibility with Display.syncCall(...)
	<T> T syncCall(SwtCallable<T, Exception> c) throws Exception;

	/**
	 * @return true if the caller is executing in this realm.
	 */
	public boolean isCurrent();
}
