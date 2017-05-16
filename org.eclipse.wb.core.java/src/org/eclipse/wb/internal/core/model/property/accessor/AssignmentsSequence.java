/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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