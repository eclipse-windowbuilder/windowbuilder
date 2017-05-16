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

import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.PrefixExpression;

import org.apache.commons.lang.ObjectUtils;

import java.util.List;

/**
 * Implementation of {@link IExpressionEvaluator} for "boolean" type.
 *
 * @author scheglov_ke
 * @coverage core.evaluation
 */
public final class BooleanEvaluator implements IExpressionEvaluator {
  ////////////////////////////////////////////////////////////////////////////
  //
  // IExpressionEvaluator
  //
  ////////////////////////////////////////////////////////////////////////////
  public Object evaluate(EvaluationContext context,
      Expression expression,
      ITypeBinding typeBinding,
      String typeQualifiedName) throws Exception {
    // boolean expression
    if ("boolean".equals(typeQualifiedName)) {
      // single literal
      if (expression instanceof BooleanLiteral) {
        BooleanLiteral numberLiteral = (BooleanLiteral) expression;
        return numberLiteral.booleanValue() ? Boolean.TRUE : Boolean.FALSE;
      }
      // prefix expression (!)
      if (expression instanceof PrefixExpression) {
        PrefixExpression prefixExpression = (PrefixExpression) expression;
        // only "!" is possible prefix operator for string's
        Assert.isTrue(prefixExpression.getOperator() == PrefixExpression.Operator.NOT);
        // prepare operand value
        Expression operand = prefixExpression.getOperand();
        boolean operandValue = getBooleanValue(context, operand);
        // return result
        return !operandValue;
      }
      // infix expression (&&, ||)
      if (expression instanceof InfixExpression) {
        InfixExpression infixExpression = (InfixExpression) expression;
        Operator operator = infixExpression.getOperator();
        Expression leftOperand = infixExpression.getLeftOperand();
        Expression rightOperand = infixExpression.getRightOperand();
        // compare
        if (operator == InfixExpression.Operator.EQUALS
            || operator == InfixExpression.Operator.NOT_EQUALS) {
          Object leftObject = AstEvaluationEngine.evaluate(context, leftOperand);
          Object rightObject = AstEvaluationEngine.evaluate(context, rightOperand);
          // ==
          if (operator == InfixExpression.Operator.EQUALS) {
            return ObjectUtils.equals(leftObject, rightObject);
          }
          // !=
          if (operator == InfixExpression.Operator.NOT_EQUALS) {
            return !ObjectUtils.equals(leftObject, rightObject);
          }
        }
        // prepare operands
        boolean operands[];
        {
          List<Expression> extendedOperands = DomGenerics.extendedOperands(infixExpression);
          operands = new boolean[2 + extendedOperands.size()];
          // evaluate usual operands
          operands[0] = getBooleanValue(context, leftOperand);
          operands[1] = getBooleanValue(context, rightOperand);
          // evaluate extended operands
          for (int i = 0; i < extendedOperands.size(); i++) {
            Expression operandExpression = extendedOperands.get(i);
            operands[2 + i] = getBooleanValue(context, operandExpression);
          }
        }
        // process each operand
        boolean value = operands[0];
        for (boolean operand : operands) {
          if (operator == InfixExpression.Operator.CONDITIONAL_AND) {
            value &= operand;
          } else if (operator == InfixExpression.Operator.CONDITIONAL_OR) {
            value |= operand;
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
   * Converts given {@link Expression} into "boolean" value.
   */
  private static boolean getBooleanValue(EvaluationContext context, Expression expression)
      throws Exception {
    Boolean value = (Boolean) AstEvaluationEngine.evaluate(context, expression);
    return value.booleanValue();
  }
}
