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
package org.eclipse.wb.internal.core.eval.evaluators;

import org.eclipse.wb.core.eval.AstEvaluationEngine;
import org.eclipse.wb.core.eval.EvaluationContext;
import org.eclipse.wb.core.eval.IExpressionEvaluator;

import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * Implementation of {@link IExpressionEvaluator} for {@link ConditionalExpression}.
 *
 * @author scheglov_ke
 * @coverage core.evaluation
 */
public final class ConditionalExpressionEvaluator implements IExpressionEvaluator {
  ////////////////////////////////////////////////////////////////////////////
  //
  // IExpressionEvaluator
  //
  ////////////////////////////////////////////////////////////////////////////
  public Object evaluate(EvaluationContext context,
      Expression expression,
      ITypeBinding typeBinding,
      String typeQualifiedName) throws Exception {
    if (expression instanceof ConditionalExpression) {
      ConditionalExpression conditionalExpression = (ConditionalExpression) expression;
      // evaluate condition
      Expression conditionExpression = conditionalExpression.getExpression();
      boolean condition = (Boolean) AstEvaluationEngine.evaluate(context, conditionExpression);
      // prepare Expression to evaluate
      Expression resultExpression;
      if (condition) {
        resultExpression = conditionalExpression.getThenExpression();
      } else {
        resultExpression = conditionalExpression.getElseExpression();
      }
      // evaluate result
      return AstEvaluationEngine.evaluate(context, resultExpression);
    }
    // we don't understand given expression
    return AstEvaluationEngine.UNKNOWN;
  }
}
