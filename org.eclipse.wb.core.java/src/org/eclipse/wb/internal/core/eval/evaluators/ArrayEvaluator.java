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
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;

import java.lang.reflect.Array;
import java.util.List;

/**
 * Implementation of {@link IExpressionEvaluator} for {@link ArrayCreation} and
 * {@link ArrayInitializer}.
 *
 * @author scheglov_ke
 * @coverage core.evaluation
 */
public final class ArrayEvaluator implements IExpressionEvaluator {
  ////////////////////////////////////////////////////////////////////////////
  //
  // IExpressionEvaluator
  //
  ////////////////////////////////////////////////////////////////////////////
  public Object evaluate(EvaluationContext context,
      Expression expression,
      ITypeBinding typeBinding,
      String typeQualifiedName) throws Exception {
    // evaluate ArrayCreation
    if (expression instanceof ArrayCreation) {
      ArrayCreation creation = (ArrayCreation) expression;
      ArrayInitializer initializer = creation.getInitializer();
      if (initializer != null) {
        return AstEvaluationEngine.evaluate(context, initializer);
      }
      return evaluateEmpty(context, typeBinding, creation);
    }
    // evaluate ArrayInitializer
    if (expression instanceof ArrayInitializer) {
      ArrayInitializer initializer = (ArrayInitializer) expression;
      return evaluateInitializer(context, typeBinding, initializer);
    }
    // evaluate ArrayAccess
    if (expression instanceof ArrayAccess) {
      ArrayAccess arrayAccess = (ArrayAccess) expression;
      Expression arrayExpression = arrayAccess.getArray();
      Expression indexExpression = arrayAccess.getIndex();
      Object arrayObject = AstEvaluationEngine.evaluate(context, arrayExpression);
      Object indexObject = AstEvaluationEngine.evaluate(context, indexExpression);
      if (indexObject instanceof Integer) {
        int index = ((Integer) indexObject).intValue();
        return Array.get(arrayObject, index);
      }
    }
    // we don't understand given expression
    return AstEvaluationEngine.UNKNOWN;
  }

  private static Object evaluateEmpty(EvaluationContext context,
      ITypeBinding typeBinding,
      ArrayCreation creation) throws Exception {
    // prepare type information
    ITypeBinding elementTypeBinding = typeBinding.getElementType();
    Assert.isNotNull(elementTypeBinding);
    String elementTypeName = AstNodeUtils.getFullyQualifiedName(elementTypeBinding, false);
    Class<?> elementType =
        ReflectionUtils.getClassByName(context.getClassLoader(), elementTypeName);
    // prepare dimensions
    List<Expression> dimensionExpressions = DomGenerics.dimensions(creation);
    int dimensions[] = new int[dimensionExpressions.size()];
    for (int i = 0; i < dimensions.length; i++) {
      Expression dimensionExpression = dimensionExpressions.get(i);
      dimensions[i] = (Integer) AstEvaluationEngine.evaluate(context, dimensionExpression);
    }
    // create Array
    return Array.newInstance(elementType, dimensions);
  }

  private static Object evaluateInitializer(EvaluationContext context,
      ITypeBinding typeBinding,
      ArrayInitializer initializer) throws Exception {
    // prepare type information
    ITypeBinding elementTypeBinding = typeBinding.getElementType();
    Assert.isNotNull(elementTypeBinding);
    String elementTypeName = AstNodeUtils.getFullyQualifiedName(elementTypeBinding, false);
    Class<?> elementType =
        ReflectionUtils.getClassByName(context.getClassLoader(), elementTypeName);
    // prepare top level array length
    List<Expression> expressions = DomGenerics.expressions(initializer);
    int length = expressions.size();
    // prepare top level array with correct number of dimensions
    int dimensions[] = new int[typeBinding.getDimensions()];
    dimensions[0] = length;
    // array of primitives
    if (typeBinding.getDimensions() == 1 && elementTypeBinding.isPrimitive()) {
      Object values = Array.newInstance(elementType, dimensions);
      for (int index = 0; index < length; index++) {
        Expression element = expressions.get(index);
        Object elementValue = AstEvaluationEngine.evaluate(context, element);
        if (elementType == boolean.class) {
          Array.setBoolean(values, index, ((Boolean) elementValue).booleanValue());
        } else if (elementType == char.class) {
          char charValue;
          if (elementValue instanceof Number) {
            charValue = (char) ((Number) elementValue).intValue();
          } else {
            charValue = ((Character) elementValue).charValue();
          }
          Array.setChar(values, index, charValue);
        } else {
          Number number = (Number) elementValue;
          if (elementType == byte.class) {
            Array.setByte(values, index, number.byteValue());
          } else if (elementType == short.class) {
            Array.setShort(values, index, number.shortValue());
          } else if (elementType == int.class) {
            Array.setInt(values, index, number.intValue());
          } else if (elementType == long.class) {
            Array.setLong(values, index, number.longValue());
          } else if (elementType == float.class) {
            Array.setFloat(values, index, number.floatValue());
          } else if (elementType == double.class) {
            Array.setDouble(values, index, number.doubleValue());
          }
        }
      }
      return values;
    }
    // array of Object's
    {
      Object values[] = (Object[]) Array.newInstance(elementType, dimensions);
      for (int index = 0; index < length; index++) {
        Expression element = expressions.get(index);
        Object value = AstEvaluationEngine.evaluate(context, element);
        values[index] = value;
      }
      return values;
    }
  }
}
