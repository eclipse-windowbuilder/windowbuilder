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
package org.eclipse.wb.core.model.association;

import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;

import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;

/**
 * {@link Association} for {@link MethodInvocation} as separate {@link ExpressionStatement}.
 *
 * @author scheglov_ke
 * @coverage core.model.association
 */
public abstract class InvocationAssociation extends Association {
	protected MethodInvocation m_invocation;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	////////////////////////////////////////////////////////////////////////////
	protected InvocationAssociation() {
	}

	protected InvocationAssociation(MethodInvocation invocation) {
		m_invocation = invocation;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the underlying {@link MethodInvocation}.
	 */
	public final MethodInvocation getInvocation() {
		return m_invocation;
	}

	@Override
	public final Statement getStatement() {
		return AstNodeUtils.getEnclosingStatement(m_invocation);
	}

	@Override
	public final String getSource() {
		return m_editor.getSource(m_invocation);
	}
}
