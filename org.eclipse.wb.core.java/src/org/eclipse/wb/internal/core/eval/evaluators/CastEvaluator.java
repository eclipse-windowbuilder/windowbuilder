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
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;

import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * Implementation of {@link IExpressionEvaluator} for {@link CastExpression}.
 *
 * @author scheglov_ke
 * @coverage core.evaluation
 */
public final class CastEvaluator implements IExpressionEvaluator {
  ////////////////////////////////////////////////////////////////////////////
  //
  // IExpressionEvaluator
  //
  ////////////////////////////////////////////////////////////////////////////
  public Object evaluate(EvaluationContext context,
      Expression expression,
      ITypeBinding typeBinding,
      String typeQualifiedName) throws Exception {
    if (expression instanceof CastExpression) {
      CastExpression castExpression = (CastExpression) expression;
      // prepare value to cast
      Object value = AstEvaluationEngine.evaluate(context, castExpression.getExpression());
      // do cast for primities
      if (castExpression.getType().isPrimitiveType()) {
        Number number = (Number) value;
        String name = AstNodeUtils.getFullyQualifiedName(castExpression.getType(), true);
        if ("byte".equals(name)) {
          return number.byteValue();
        }
        if ("short".equals(name)) {
          return number.shortValue();
        }
        if ("char".equals(name)) {
          return (char) number.intValue();
        }
        if ("int".equals(name)) {
          return number.intValue();
        }
        if ("long".equals(name)) {
          return number.longValue();
        }
        if ("float".equals(name)) {
          return number.floatValue();
        }
        if ("double".equals(name)) {
          return number.doubleValue();
        }
      }
      // don't need cast for objects
      return value;
    }
    // we don't understand given expression
    return AstEvaluationEngine.UNKNOWN;
  }
}
