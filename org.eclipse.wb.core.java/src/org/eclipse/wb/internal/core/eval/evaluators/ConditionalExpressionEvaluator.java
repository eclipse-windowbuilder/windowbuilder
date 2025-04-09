/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
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
	@Override
	public Object evaluate(EvaluationContext context,
			Expression expression,
			ITypeBinding typeBinding,
			String typeQualifiedName) throws Exception {
		if (expression instanceof ConditionalExpression conditionalExpression) {
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
