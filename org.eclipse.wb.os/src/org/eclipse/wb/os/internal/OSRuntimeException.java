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
package org.eclipse.wb.os.internal;

/**
 * Wrapper for all exceptions thrown while accessing native code.
 */
public class OSRuntimeException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public OSRuntimeException(Throwable t) {
		super(t);
	}
}
