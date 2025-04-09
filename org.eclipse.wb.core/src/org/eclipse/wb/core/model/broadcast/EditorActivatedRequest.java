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
package org.eclipse.wb.core.model.broadcast;

/**
 * Request object for {@link EditorActivatedListener#invoke(EditorActivatedRequest)}.
 *
 * @author scheglov_ke
 * @coverage core.model
 */
public final class EditorActivatedRequest {
	private boolean m_reparseRequested;
	private boolean m_refreshRequested;

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public boolean isReparseRequested() {
		return m_reparseRequested;
	}

	public boolean isRefreshRequested() {
		return m_refreshRequested;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Requesting
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Specifies that reparse should be performed.
	 */
	public void requestReparse() {
		m_reparseRequested = true;
	}

	/**
	 * Specifies that refresh should be performed.
	 */
	public void requestRefresh() {
		m_refreshRequested = true;
	}
}
