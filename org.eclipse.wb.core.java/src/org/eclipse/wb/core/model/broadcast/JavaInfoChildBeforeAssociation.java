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

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.association.Association;

import org.eclipse.jdt.core.dom.Statement;

/**
 * Listener for force {@link Association} as terminal target for child adding.
 *
 * @author sablin_aa
 * @coverage core.model
 */
public class JavaInfoChildBeforeAssociation extends JavaEventListener {
	private final JavaInfo m_javaInfo;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public JavaInfoChildBeforeAssociation(JavaInfo javaInfo) {
		m_javaInfo = javaInfo;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Target
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Ensures that all children are added before association {@link Statement}.
	 */
	@Override
	public void target_isTerminalStatement(JavaInfo parent,
			JavaInfo child,
			Statement statement,
			boolean[] terminal) {
		if (parent == m_javaInfo && statement == m_javaInfo.getAssociation().getStatement()) {
			terminal[0] = true;
		}
	}
}