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
package org.eclipse.wb.internal.rcp.model.rcp;

import org.eclipse.wb.core.eval.AstEvaluationEngine;
import org.eclipse.wb.core.eval.EvaluationContext;
import org.eclipse.wb.core.eval.IExpressionEvaluator;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.actions.ActionFactory;

/**
 * Implementation of {@link IExpressionEvaluator} for evaluating {@link Action}'s from
 * {@link ActionFactory}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.rcp
 */
public class ActionFactoryExpressionEvaluator implements IExpressionEvaluator {
	@Override
	public Object evaluate(EvaluationContext context,
			Expression expression,
			ITypeBinding typeBinding,
			String typeQualifiedName) throws Exception {
		if (expression instanceof QualifiedName qualifiedName) {
			if (AstNodeUtils.getFullyQualifiedName(qualifiedName.getQualifier(), false).equals(
					"org.eclipse.ui.actions.ActionFactory")) {
			}
		}
		// we don't understand given expression
		return AstEvaluationEngine.UNKNOWN;
	}
}
