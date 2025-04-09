/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.utils.check;

/**
 * <code>AssertionFailedException</code> is a runtime exception thrown by some of the methods in
 * <code>Assert</code>.
 *
 * @author scheglov_ke
 * @coverage core.util
 */
public final class AssertionFailedException extends RuntimeException {
	private static final long serialVersionUID = 0L;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Constructs a new exception with the given message.
	 *
	 * @param detail
	 *          the message
	 */
	public AssertionFailedException(String detail) {
		super(detail);
	}
}
