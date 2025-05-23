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
import org.eclipse.wb.internal.core.model.creation.IMethodParameterEvaluator;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * {@link IMethodParameterEvaluator} for RCP objects.
 *
 * @author scheglov_ke
 * @coverage rcp.model
 */
public final class RcpMethodParameterEvaluator implements IMethodParameterEvaluator {
	public static final IMethodParameterEvaluator INSTANCE = new RcpMethodParameterEvaluator();
	public static final FormToolkit FORM_TOOLKIT = new FormToolkit(Display.getDefault());

	////////////////////////////////////////////////////////////////////////////
	//
	// IMethodParameterEvaluator
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Object evaluateParameter(EvaluationContext context,
			MethodDeclaration methodDeclaration,
			String methodSignature,
			SingleVariableDeclaration parameter,
			int index) throws Exception {
		// FormToolkit parameter may be used before creating "this", passed in "super", so provide it
		if (AstNodeUtils.isSuccessorOf(parameter, "org.eclipse.ui.forms.widgets.FormToolkit")) {
			return FORM_TOOLKIT;
		}
		// provide empty, but not null, value for ISelection
		if (AstNodeUtils.isSuccessorOf(parameter, "org.eclipse.jface.viewers.ISelection")) {
			return new StructuredSelection();
		}
		// unknown parameter
		return AstEvaluationEngine.UNKNOWN;
	}
}