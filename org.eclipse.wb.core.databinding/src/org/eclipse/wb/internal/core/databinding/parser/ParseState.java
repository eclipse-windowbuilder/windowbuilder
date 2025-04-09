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
package org.eclipse.wb.internal.core.databinding.parser;

import org.eclipse.wb.internal.core.databinding.model.IDatabindingsProvider;

import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * The parse-wide state. It contains information about current parse session.
 *
 * @author lobas_av
 * @coverage bindings.parser
 */
public final class ParseState {
	public final IDatabindingsProvider databindingsProvider;
	public final AbstractUIPlugin plugin;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ParseState(IDatabindingsProvider databindingsProvider, AbstractUIPlugin plugin) {
		this.databindingsProvider = databindingsProvider;
		this.plugin = plugin;
	}
}