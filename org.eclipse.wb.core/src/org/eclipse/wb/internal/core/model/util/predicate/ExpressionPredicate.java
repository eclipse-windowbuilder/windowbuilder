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
package org.eclipse.wb.internal.core.model.util.predicate;

import com.google.common.base.Predicate;

import org.mvel2.MVEL;

/**
 * {@link Predicate} that evaluates its value using some script expressions, currently using MVEL.
 *
 * @author scheglov_ke
 * @coverage core.model.util
 */
public final class ExpressionPredicate<T> implements Predicate<T> {
  private final String m_expression;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ExpressionPredicate(String expression) {
    m_expression = expression;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    return m_expression;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Predicate
  //
  ////////////////////////////////////////////////////////////////////////////
  public boolean apply(T t) {
    return MVEL.evalToBoolean(m_expression, t);
  }
}
