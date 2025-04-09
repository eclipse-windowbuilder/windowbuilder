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
package org.eclipse.wb.os;

/**
 * Error thrown when no appropriate {@link OSSupport} instance found for runtime OS or WS.
 *
 * @author mitin_aa
 * @coverage os.core
 */
public class OSSupportError extends Error {
	private static final long serialVersionUID = 1L;
	public static final int ERROR_CODE = 900;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	////////////////////////////////////////////////////////////////////////////
	public OSSupportError(String string, Throwable e) {
		super(string, e);
	}

	public OSSupportError(String string) {
		super(string);
	}
}
