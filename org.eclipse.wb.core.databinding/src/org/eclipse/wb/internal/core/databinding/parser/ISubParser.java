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
package org.eclipse.wb.internal.core.databinding.parser;

import org.eclipse.wb.internal.core.databinding.model.AstObjectInfo;
import org.eclipse.wb.internal.core.databinding.model.IDatabindingsProvider;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

/**
 * Abstract parser for create {@link AstObjectInfo}'s for any {@link ClassInstanceCreation} and
 * {@link MethodInvocation}.
 *
 * @author lobas_av
 * @coverage bindings.parser
 */
public interface ISubParser {
	/**
	 * Informs {@link ISubParser} about {@link ClassInstanceCreation}.
	 *
	 * @return {@link AstObjectInfo} model corresponding to given {@link ClassInstanceCreation}.
	 */
	AstObjectInfo parseExpression(AstEditor editor,
			String signature,
			ClassInstanceCreation creation,
			Expression[] arguments,
			IModelResolver resolver,
			IDatabindingsProvider provider) throws Exception;

	/**
	 * Informs {@link ISubParser} about {@link MethodInvocation}.
	 *
	 * @return {@link AstObjectInfo} model corresponding to given {@link MethodInvocation}.
	 */
	AstObjectInfo parseExpression(AstEditor editor,
			String signature,
			MethodInvocation invocation,
			Expression[] arguments,
			IModelResolver resolver) throws Exception;
}