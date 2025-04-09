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
package org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders;

/**
 * This class used for route choose class events from <code>source</code>
 * {@link ChooseClassUiContentProvider} to <code>target</code> {@link ChooseClassUiContentProvider}.
 *
 * @author lobas_av
 * @coverage bindings.ui
 */
public class ChooseClassRouter {
	private final Runnable m_listener;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ChooseClassRouter(ChooseClassUiContentProvider source, Runnable listener) {
		m_listener = listener;
		source.setRouter(this);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Internal
	//
	////////////////////////////////////////////////////////////////////////////
	void handle() {
		m_listener.run();
	}
}