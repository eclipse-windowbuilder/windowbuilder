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
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.QualifiedName;

import java.lang.reflect.Array;
import java.lang.reflect.Field;

/**
 * Implementation of {@link IExpressionEvaluator} for {@link QualifiedName}.
 *
 * @author scheglov_ke
 * @coverage core.evaluation
 */
public final class QualifiedNameEvaluator implements IExpressionEvaluator {
  ////////////////////////////////////////////////////////////////////////////
  //
  // IExpressionEvaluator
  //
  ////////////////////////////////////////////////////////////////////////////
  public Object evaluate(EvaluationContext context,
      Expression expression,
      ITypeBinding typeBinding,
      String typeQualifiedName) throws Exception {
    if (expression instanceof QualifiedName) {
      QualifiedName qualifiedName = (QualifiedName) expression;
      Name qualifier = qualifiedName.getQualifier();
      String fieldName = qualifiedName.getName().getIdentifier();
      // check for: array[].length
      if ("length".equals(fieldName)) {
        Object arrayValue = AstEvaluationEngine.evaluate(context, qualifier);
        int length = Array.getLength(arrayValue);
        return length;
      } else {
        // prepare class
        Class<?> qualifierClass;
        {
          String qualifierClassName = AstNodeUtils.getFullyQualifiedName(qualifier, true);
          qualifierClass = context.getClassLoader().loadClass(qualifierClassName);
        }
        // prepare field
        Field field = ReflectionUtils.getFieldByName(qualifierClass, fieldName);
        Assert.isNotNull(field);
        // return static value
        if (ReflectionUtils.isStatic(field)) {
          return field.get(null);
        }
        // return non-static value
        Object qualifierValue = AstEvaluationEngine.evaluate(context, qualifier);
        return field.get(qualifierValue);
      }
    }
    // we don't understand given expression
    return AstEvaluationEngine.UNKNOWN;
  }
}
