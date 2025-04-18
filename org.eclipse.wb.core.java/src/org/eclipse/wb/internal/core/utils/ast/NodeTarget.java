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
package org.eclipse.wb.internal.core.utils.ast;

import org.eclipse.jdt.core.dom.ASTNode;

/**
 * {@link NodeTarget} contains information about location for placing {@link ASTNode}.
 * <p>
 * Currently it is "union" of {@link StatementTarget} and {@link BodyDeclarationTarget}.
 *
 * @author scheglov_ke
 * @coverage core.util.ast
 */
public final class NodeTarget {
	private final StatementTarget m_statementTarget;
	private final BodyDeclarationTarget m_bodyDeclarationTarget;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	////////////////////////////////////////////////////////////////////////////
	public NodeTarget(StatementTarget statementTarget) {
		m_statementTarget = statementTarget;
		m_bodyDeclarationTarget = null;
	}

	public NodeTarget(BodyDeclarationTarget bodyDeclarationTarget) {
		m_statementTarget = null;
		m_bodyDeclarationTarget = bodyDeclarationTarget;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Object
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String toString() {
		return m_statementTarget != null
				? m_statementTarget.toString()
						: m_bodyDeclarationTarget.toString();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Accessor
	//
	////////////////////////////////////////////////////////////////////////////
	public StatementTarget getStatementTarget() {
		return m_statementTarget;
	}

	public BodyDeclarationTarget getBodyDeclarationTarget() {
		return m_bodyDeclarationTarget;
	}
}
