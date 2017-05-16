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
import org.eclipse.wb.core.eval.ExecutionFlowDescription;
import org.eclipse.wb.core.eval.ExecutionFlowUtils2;
import org.eclipse.wb.core.eval.ExpressionValue;
import org.eclipse.wb.core.eval.IExpressionEvaluator;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;

import java.util.List;

/**
 * Implementation of {@link IExpressionEvaluator} for "int" type.
 *
 * @author scheglov_ke
 * @coverage core.evaluation
 */
public final class IntegerEvaluator implements IExpressionEvaluator {
  ////////////////////////////////////////////////////////////////////////////
  //
  // IExpressionEvaluator
  //
  ////////////////////////////////////////////////////////////////////////////
  public Object evaluate(EvaluationContext context,
      Expression expression,
      ITypeBinding typeBinding,
      String typeQualifiedName) throws Exception {
    // integer expression
    if ("int".equals(typeQualifiedName)) {
      // single number literal
      if (expression instanceof NumberLiteral) {
        NumberLiteral numberLiteral = (NumberLiteral) expression;
        String token = numberLiteral.getToken();
        // hex
        if (token.startsWith("0x")) {
          return Integer.valueOf(token.substring(2), 16);
        }
        // oct
        if (token.startsWith("0")) {
          return Integer.valueOf(token, 8);
        }
        // decimal
        return Integer.valueOf(token);
      }
      // prefix expression (+, -, ~)
      if (expression instanceof PrefixExpression) {
        PrefixExpression prefixExpression = (PrefixExpression) expression;
        PrefixExpression.Operator operator = prefixExpression.getOperator();
        //
        Expression operand = prefixExpression.getOperand();
        int operandValue = getIntegerValue(context, operand);
        // +
        if (operator == PrefixExpression.Operator.PLUS) {
          return +operandValue;
        }
        // -
        if (operator == PrefixExpression.Operator.MINUS) {
          return -operandValue;
        }
        // ~
        if (operator == PrefixExpression.Operator.COMPLEMENT) {
          return ~operandValue;
        }
      }
      // postfix expression (++, --)
      if (expression instanceof PostfixExpression) {
        PostfixExpression postfixExpression = (PostfixExpression) expression;
        PostfixExpression.Operator operator = postfixExpression.getOperator();
        //
        Expression operand = postfixExpression.getOperand();
        {
          ExecutionFlowDescription flowDescription = context.getFlowDescription();
          ExpressionValue value = ExecutionFlowUtils2.getValuePrev(flowDescription, operand);
          if (value != null) {
            operand = value.getExpression();
          }
        }
        int operandValue = getIntegerValue(context, operand);
        // ++
        if (operator == PostfixExpression.Operator.INCREMENT) {
          return operandValue + 1;
        }
        // --
        if (operator == PostfixExpression.Operator.DECREMENT) {
          return operandValue - 1;
        }
      }
      // infix expression (+, -, *, /, %, |, &)
      if (expression instanceof InfixExpression) {
        InfixExpression infixExpression = (InfixExpression) expression;
        // prepare operands
        int operands[];
        {
          List<Expression> extendedOperands = DomGenerics.extendedOperands(infixExpression);
          operands = new int[2 + extendedOperands.size()];
          // evaluate usual operands
          operands[0] = getIntegerValue(context, infixExpression.getLeftOperand());
          operands[1] = getIntegerValue(context, infixExpression.getRightOperand());
          // evaluate extended operands
          for (int i = 0; i < extendedOperands.size(); i++) {
            Expression operandExpression = extendedOperands.get(i);
            operands[2 + i] = getIntegerValue(context, operandExpression);
          }
        }
        // process each operand
        int value = operands[0];
        Operator operator = infixExpression.getOperator();
        for (int i = 1; i < operands.length; i++) {
          int operand = operands[i];
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
          } else if (operator == InfixExpression.Operator.OR) {
            value |= operand;
          } else if (operator == InfixExpression.Operator.XOR) {
            value ^= operand;
          } else if (operator == InfixExpression.Operator.AND) {
            value &= operand;
          } else if (operator == InfixExpression.Operator.LEFT_SHIFT) {
            value <<= operand;
          } else if (operator == InfixExpression.Operator.RIGHT_SHIFT_SIGNED) {
            value >>= operand;
          } else if (operator == InfixExpression.Operator.RIGHT_SHIFT_UNSIGNED) {
            value >>>= operand;
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
   * Converts given {@link Expression} into "int" value.
   */
  private static int getIntegerValue(EvaluationContext context, Expression expression)
      throws Exception {
    Object value = AstEvaluationEngine.evaluate(context, expression);
    // Character
    if (value instanceof Character) {
      Character character = (Character) value;
      return character.charValue();
    }
    // Number
    Number number = (Number) value;
    return number.intValue();
  }
}
