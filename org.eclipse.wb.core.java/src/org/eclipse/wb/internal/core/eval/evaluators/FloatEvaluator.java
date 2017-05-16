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
 * Implementation of {@link IExpressionEvaluator} for "float" type.
 *
 * @author scheglov_ke
 * @coverage core.evaluation
 */
public final class FloatEvaluator implements IExpressionEvaluator {
  ////////////////////////////////////////////////////////////////////////////
  //
  // IExpressionEvaluator
  //
  ////////////////////////////////////////////////////////////////////////////
  public Object evaluate(EvaluationContext context,
      Expression expression,
      ITypeBinding typeBinding,
      String typeQualifiedName) throws Exception {
    // float expression
    if ("float".equals(typeQualifiedName)) {
      // single number literal
      if (expression instanceof NumberLiteral) {
        NumberLiteral numberLiteral = (NumberLiteral) expression;
        String token = numberLiteral.getToken();
        // remove trailing 'F'/'f'
        token = StringUtils.stripEnd(token, "Ff");
        // parse
        return Float.valueOf(token);
      }
      // prefix expression (+, -)
      if (expression instanceof PrefixExpression) {
        PrefixExpression prefixExpression = (PrefixExpression) expression;
        PrefixExpression.Operator operator = prefixExpression.getOperator();
        //
        Expression operand = prefixExpression.getOperand();
        float operandValue = getFloatValue(context, operand);
        // +
        if (operator == PrefixExpression.Operator.PLUS) {
          return +operandValue;
        }
        // -
        if (operator == PrefixExpression.Operator.MINUS) {
          return -operandValue;
        }
      }
      // infix expression (+, -, *, /, %)
      if (expression instanceof InfixExpression) {
        InfixExpression infixExpression = (InfixExpression) expression;
        // prepare operands
        float operands[];
        {
          List<Expression> extendedOperands = DomGenerics.extendedOperands(infixExpression);
          operands = new float[2 + extendedOperands.size()];
          // evaluate usual operands
          operands[0] = getFloatValue(context, infixExpression.getLeftOperand());
          operands[1] = getFloatValue(context, infixExpression.getRightOperand());
          // evaluate extended operands
          for (int i = 0; i < extendedOperands.size(); i++) {
            Expression operandExpression = extendedOperands.get(i);
            operands[2 + i] = getFloatValue(context, operandExpression);
          }
        }
        // process each operand
        float value = operands[0];
        Operator operator = infixExpression.getOperator();
        for (int i = 1; i < operands.length; i++) {
          float operand = operands[i];
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
        return value;
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
   * Converts given {@link Expression} into "float" value.
   */
  private static float getFloatValue(EvaluationContext context, Expression expression)
      throws Exception {
    Object value = AstEvaluationEngine.evaluate(context, expression);
    // Character
    if (value instanceof Character) {
      Character character = (Character) value;
      return character.charValue();
    }
    // Number
    Number number = (Number) value;
    return number.floatValue();
  }
}
