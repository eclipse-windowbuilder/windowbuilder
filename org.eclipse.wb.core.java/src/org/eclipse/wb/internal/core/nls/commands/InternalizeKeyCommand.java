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

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.StringLiteral;

import java.util.HashSet;
import java.util.Set;

/**
 * Command for internalizing (removing) key, i.e. replace externalized {@link Expression} with
 * {@link StringLiteral}.
 *
 * @author scheglov_ke
 * @coverage core.nls
 */
public final class InternalizeKeyCommand extends AbstractCommand {
	private final String m_key;
	private Set<String> m_keys;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public InternalizeKeyCommand(IEditableSource editableSource, String key) {
		super(editableSource);
		m_key = key;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public Set<String> getKeys() {
		if (m_keys == null) {
			m_keys = new HashSet<>();
			m_keys.add(m_key);
		}
		return m_keys;
	}
}
