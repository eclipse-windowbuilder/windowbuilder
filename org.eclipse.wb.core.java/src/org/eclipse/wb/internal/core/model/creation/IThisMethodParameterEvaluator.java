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
package org.eclipse.wb.internal.core.model.creation;

import org.eclipse.wb.core.eval.AstEvaluationEngine;
import org.eclipse.wb.core.eval.EvaluationContext;
import org.eclipse.wb.core.model.JavaInfo;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

/**
 * This interface allows {@link JavaInfo} participate in evaluation of {@link MethodDeclaration}
 * parameter value.
 *
 * @author scheglov_ke
 * @coverage core.model.creation
 */
public interface IThisMethodParameterEvaluator {
	/**
	 * @param context
	 *          the {@link EvaluationContext}
	 * @param methodDeclaration
	 *          the {@link MethodDeclaration} to evaluate parameter.
	 * @param methodSignature
	 *          the signature of {@link MethodDeclaration}.
	 * @param parameter
	 *          the parameter in {@link MethodDeclaration}.
	 * @param index
	 *          the index of parameter.
	 *
	 * @return the value of given parameter or {@link AstEvaluationEngine#UNKNOWN}.
	 */
	Object evaluateParameter(EvaluationContext context,
			MethodDeclaration methodDeclaration,
			String methodSignature,
			SingleVariableDeclaration parameter,
			int index) throws Exception;
}
