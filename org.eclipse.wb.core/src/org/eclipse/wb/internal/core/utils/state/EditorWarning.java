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
package org.eclipse.wb.internal.core.utils.state;

/**
 * Information about some warning happened in editor.
 *
 * @author scheglov_ke
 * @coverage core.model
 */
public final class EditorWarning {
	private final String m_message;
	private final Throwable m_exception;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public EditorWarning(String message) {
		this(message, null);
	}

	public EditorWarning(String message, Throwable exception) {
		m_message = message;
		m_exception = exception;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the message of warning.
	 */
	public String getMessage() {
		return m_message;
	}

	/**
	 * @return the exception associated with this warning, may be <code>null</code>.
	 */
	public Throwable getException() {
		return m_exception;
	}
}
