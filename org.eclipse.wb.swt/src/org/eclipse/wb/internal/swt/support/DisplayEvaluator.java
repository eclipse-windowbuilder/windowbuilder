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
package org.eclipse.wb.internal.swt.support;

import org.eclipse.wb.core.eval.AstEvaluationEngine;
import org.eclipse.wb.core.eval.EvaluationContext;
import org.eclipse.wb.core.eval.IExpressionEvaluator;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.swt.widgets.Display;

/**
 * Implementation of {@link IExpressionEvaluator} for evaluating
 * {@code new Display()} as {@code DesignerPlugin.getStandardDisplay()}. We need
 * this because all SWT forms use same shared, already existing instance of
 * {@link Display}.
 *
 * @author scheglov_ke
 * @coverage swt.support
 */
public class DisplayEvaluator implements IExpressionEvaluator {
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
		if (expression instanceof ClassInstanceCreation
				&& AstNodeUtils.getFullyQualifiedName(typeBinding, false).equals(
						"org.eclipse.swt.widgets.Display")) {
			return DesignerPlugin.getStandardDisplay();
		}
		// we don't understand given expression
		return AstEvaluationEngine.UNKNOWN;
	}
}