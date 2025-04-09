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
package org.eclipse.wb.internal.core.model.property.accessor;

import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Expression;

import java.util.List;

/**
 * Container for assignments sequence such as <code>a.foo = b.bar = expr</code>.
 *
 * @author scheglov_ke
 * @coverage core.model.property.accessor
 */
public final class AssignmentsSequence {
	private final List<Assignment> m_assignments;
	private final Expression m_assignedExpression;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public AssignmentsSequence(List<Assignment> assignments, Expression assignedExpression) {
		m_assignments = assignments;
		m_assignedExpression = assignedExpression;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public List<Assignment> getAssignments() {
		return m_assignments;
	}

	public Expression getAssignedExpression() {
		return m_assignedExpression;
	}
}