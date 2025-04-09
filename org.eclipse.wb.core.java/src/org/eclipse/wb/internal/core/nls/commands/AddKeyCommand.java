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
package org.eclipse.wb.internal.core.nls.commands;

import org.eclipse.wb.internal.core.nls.edit.IEditableSource;

/**
 * Command for adding new key into source.
 *
 * @author scheglov_ke
 * @coverage core.nls
 */
public final class AddKeyCommand extends AbstractCommand {
	private final String m_key;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public AddKeyCommand(IEditableSource editableSource, String key) {
		super(editableSource);
		m_key = key;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public String getKey() {
		return m_key;
	}
}
