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

import org.eclipse.wb.core.model.JavaInfo;

import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;

/**
 * Implementation of {@link Association} for {@link JavaInfo} passed as argument of
 * {@link SuperConstructorInvocation}.
 * <p>
 * For example: <code><pre>
 *   public class Test extends JPanel {
 *     public Test() {
 *       this(new BorderLayout());
 *     }
 *   }
 * </pre><code>
 *
 * @author scheglov_ke
 * @coverage core.model.association
 */
public final class SuperConstructorArgumentAssociation extends Association {
	private final SuperConstructorInvocation m_invocation;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public SuperConstructorArgumentAssociation(SuperConstructorInvocation invocation) {
		m_invocation = invocation;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Statement getStatement() {
		return m_invocation;
	}

	@Override
	public String getSource() {
		return m_editor.getSource(m_invocation);
	}

	@Override
	public boolean canDelete() {
		return false;
	}
}
