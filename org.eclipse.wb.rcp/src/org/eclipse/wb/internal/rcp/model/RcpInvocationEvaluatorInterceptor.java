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
package org.eclipse.wb.internal.rcp.model;

import org.eclipse.wb.core.eval.AstEvaluationEngine;
import org.eclipse.wb.core.eval.EvaluationContext;
import org.eclipse.wb.core.eval.InvocationEvaluatorInterceptor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * For custom RCP objects.
 *
 * @author scheglov_ke
 * @coverage rcp.model
 */
public final class RcpInvocationEvaluatorInterceptor extends InvocationEvaluatorInterceptor {
	@Override
	public Object evaluateAnonymous(EvaluationContext context,
			ClassInstanceCreation expression,
			ITypeBinding typeBinding,
			ITypeBinding typeBindingConcrete,
			IMethodBinding methodBinding,
			Object[] arguments) throws Exception {
		if (AstNodeUtils.isSuccessorOf(typeBinding, "org.eclipse.jface.action.Action")) {
			return AstEvaluationEngine.createAnonymousInstance(context, methodBinding, arguments);
		}
		return AstEvaluationEngine.UNKNOWN;
	}
}
