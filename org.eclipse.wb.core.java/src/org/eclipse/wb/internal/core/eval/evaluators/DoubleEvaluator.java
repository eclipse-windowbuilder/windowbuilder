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

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PrefixExpression;

import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * Implementation of {@link IExpressionEvaluator} for "double" type.
 *
 * @author scheglov_ke
 * @coverage core.evaluation
 */
public final class DoubleEvaluator implements IExpressionEvaluator {
  ////////////////////////////////////////////////////////////////////////////
  //
  // IExpressionEvaluator
  //
  ////////////////////////////////////////////////////////////////////////////
  public Object evaluate(EvaluationContext context,
      Expression expression,
      ITypeBinding typeBinding,
      String typeQualifiedName) throws Exception {
    // double expression
    if ("double".equals(typeQualifiedName)) {
      // single number literal
      if (expression instanceof NumberLiteral) {
        NumberLiteral numberLiteral = (NumberLiteral) expression;
        String token = numberLiteral.getToken();
        // remove trailing 'D'/'d'
        token = StringUtils.stripEnd(token, "Dd");
        // parse
        return Double.valueOf(token);
      }
      // prefix expression (+, -)
      if (expression instanceof PrefixExpression) {
        PrefixExpression prefixExpression = (PrefixExpression) expression;
        PrefixExpression.Operator operator = prefixExpression.getOperator();
        //
        Expression operand = prefixExpression.getOperand();
        double operandValue = getDoubleValue(context, operand);
        // +
        if (operator == PrefixExpression.Operator.PLUS) {
          return new Double(operandValue);
        }
        // -
        if (operator == PrefixExpression.Operator.MINUS) {
          return new Double(-operandValue);
        }
      }
      // infix expression (+, -, *, /, %)
      if (expression instanceof InfixExpression) {
        InfixExpression infixExpression = (InfixExpression) expression;
        // prepare operands
        double operands[];
        {
          List<Expression> extendedOperands = DomGenerics.extendedOperands(infixExpression);
          operands = new double[2 + extendedOperands.size()];
          // evaluate usual operands
          operands[0] = getDoubleValue(context, infixExpression.getLeftOperand());
          operands[1] = getDoubleValue(context, infixExpression.getRightOperand());
          // evaluate extended operands
          for (int i = 0; i < extendedOperands.size(); i++) {
            Expression operandExpression = extendedOperands.get(i);
            operands[2 + i] = getDoubleValue(context, operandExpression);
          }
        }
        // process each operand
        double value = operands[0];
        Operator operator = infixExpression.getOperator();
        for (int i = 1; i < operands.length; i++) {
          double operand = operands[i];
          if (operator == InfixExpression.Operator.PLUS) {
            value += operand;
          } else if (operator == InfixExpression.Operator.MINUS) {
            value -= operand;
          } else if (operator == InfixExpression.Operator.TIMES) {
            value *= operand;
          } else if (operator == InfixExpression.Operator.DIVIDE) {
            value /= operand;
          } else if (operator == InfixExpression.Operator.REMAINDER) {
            value %= operand;
          }
        }
        // return final value as object
        return new Double(value);
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
   * Converts given {@link Expression} into "double" value.
   */
  private static double getDoubleValue(EvaluationContext context, Expression expression)
      throws Exception {
    Object value = AstEvaluationEngine.evaluate(context, expression);
    // Character
    if (value instanceof Character) {
      Character character = (Character) value;
      return character.charValue();
    }
    // Number
    Number number = (Number) value;
    return number.doubleValue();
  }
}
