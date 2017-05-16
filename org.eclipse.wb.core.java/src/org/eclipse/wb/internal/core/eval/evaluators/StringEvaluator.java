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
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.check.Assert;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.StringLiteral;

import java.util.List;

/**
 * Implementation of {@link IExpressionEvaluator} for {@link String}.
 *
 * @author scheglov_ke
 * @coverage core.evaluation
 */
public final class StringEvaluator implements IExpressionEvaluator {
  ////////////////////////////////////////////////////////////////////////////
  //
  // IExpressionEvaluator
  //
  ////////////////////////////////////////////////////////////////////////////
  public Object evaluate(EvaluationContext context,
      Expression expression,
      ITypeBinding typeBinding,
      String typeQualifiedName) throws Exception {
    // string literal
    if (expression instanceof StringLiteral) {
      StringLiteral stringLiteral = (StringLiteral) expression;
      return stringLiteral.getLiteralValue();
    }
    // integer expression
    if ("java.lang.String".equals(typeQualifiedName)) {
      // infix expression (+)
      if (expression instanceof InfixExpression) {
        InfixExpression infixExpression = (InfixExpression) expression;
        // only "+" is possible infix operator for string's
        Assert.isTrue(infixExpression.getOperator() == InfixExpression.Operator.PLUS);
        // prepare operands
        String operands[];
        {
          List<Expression> extendedOperands = DomGenerics.extendedOperands(infixExpression);
          operands = new String[2 + extendedOperands.size()];
          // evaluate usual operands
          operands[0] = getStringValue(context, infixExpression.getLeftOperand());
          operands[1] = getStringValue(context, infixExpression.getRightOperand());
          // evaluate extended operands
          for (int i = 0; i < extendedOperands.size(); i++) {
            Expression operandExpression = extendedOperands.get(i);
            operands[2 + i] = getStringValue(context, operandExpression);
          }
        }
        // process each operand
        StringBuffer value = new StringBuffer();
        for (String operand : operands) {
          value.append(operand);
        }
        // return final value as object
        return value.toString();
      }
    }
    // we don't understand given expression
    return AstEvaluationEngine.UNKNOWN;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Converts given {@link Expression} into "String" value.
   */
  private static String getStringValue(EvaluationContext context, Expression expression)
      throws Exception {
    Object o = AstEvaluationEngine.evaluate(context, expression);
    if (o instanceof String) {
      return (String) o;
    }
    if (o == null) {
      return "null";
    }
    return o.toString();
  }
}
