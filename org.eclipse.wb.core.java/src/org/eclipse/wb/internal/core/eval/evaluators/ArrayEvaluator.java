/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.eval.evaluators;

import org.eclipse.wb.core.eval.AstEvaluationEngine;
import org.eclipse.wb.core.eval.EvaluationContext;
import org.eclipse.wb.core.eval.ExecutionFlowUtils2;
import org.eclipse.wb.core.eval.IExpressionEvaluator;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Assignment;
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
	@Override
	public Object evaluate(EvaluationContext context,
			Expression expression,
			ITypeBinding typeBinding,
			String typeQualifiedName) throws Exception {
		// evaluate ArrayCreation
		if (expression instanceof ArrayCreation creation) {
			ArrayInitializer initializer = creation.getInitializer();
			Object array;
			if (initializer != null) {
				array = AstEvaluationEngine.evaluate(context, initializer);
			} else {
				array = evaluateEmpty(context, typeBinding, creation);
			}
			List<Assignment> assignments = ExecutionFlowUtils2.getAssignmentValue0(expression);
			for (Assignment assignment : assignments) {
				ExecutionUtils.runLog(() -> evaluateArrayAssignment(array, context, assignment));
			}
			return array;
		}
		// evaluate ArrayInitializer
		if (expression instanceof ArrayInitializer initializer) {
			return evaluateInitializer(context, typeBinding, initializer);
		}
		// evaluate ArrayAccess
		if (expression instanceof ArrayAccess arrayAccess) {
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

	private static void evaluateArrayAssignment(Object arrayObject, EvaluationContext context, Assignment assignment)
			throws Exception {
		ArrayAccess arrayAccess = (ArrayAccess) assignment.getLeftHandSide();
		Expression indexExpression = arrayAccess.getIndex();
		Expression valueExpression = assignment.getRightHandSide();
		Object indexObject = AstEvaluationEngine.evaluate(context, indexExpression);
		Object valueObject = AstEvaluationEngine.evaluate(context, valueExpression);
		if (indexObject instanceof Integer index) {
			Array.set(arrayObject, index, valueObject);
		}
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
