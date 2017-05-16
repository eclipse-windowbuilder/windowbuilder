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
package org.eclipse.wb.core.eval;

import org.eclipse.jdt.core.dom.Expression;

/**
 * Value of {@link Expression}.
 *
 * @author scheglov_ke
 * @coverage core.evaluation
 */
public final class ExpressionValue {
  private final Expression m_expression;
  private Object m_model;
  private Object m_object = AstEvaluationEngine.UNKNOWN;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ExpressionValue(Expression expression) {
    m_expression = expression;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    String s = "EV(";
    if (m_expression != null) {
      s += m_expression;
    }
    s += ")";
    return s;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Expression
  //
  ////////////////////////////////////////////////////////////////////////////
  public boolean hasExpression() {
    return m_expression != null;
  }

  public Expression getExpression() {
    return m_expression;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Model
  //
  ////////////////////////////////////////////////////////////////////////////
  public boolean hasModel() {
    return m_model != null;
  }

  public Object getModel() {
    return m_model;
  }

  public void setModel(Object object) {
    m_model = object;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  public boolean hasObject() {
    return m_object != AstEvaluationEngine.UNKNOWN;
  }

  public Object getObject() {
    return m_object;
  }

  public void setObject(Object object) {
    m_object = object;
  }
}
