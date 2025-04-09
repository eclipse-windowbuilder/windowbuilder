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
package org.eclipse.wb.internal.rcp.databinding.model.beans;

import org.eclipse.wb.internal.core.databinding.model.AstObjectInfo;
import org.eclipse.wb.internal.core.databinding.parser.IModelResolver;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;

import org.eclipse.jdt.core.dom.Expression;

/**
 * Factory for create {@link AstObjectInfo} models for AST <code>MethodInvocation</code> and
 * <code>ClassInstanceCreation</code>.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.beans
 */
public interface IModelCreator<T> {
	/**
	 * @return {@link AstObjectInfo} for AST <code>MethodInvocation</code> or
	 *         <code>ClassInstanceCreation</code> with {@link Expression} arument's.
	 */
	AstObjectInfo create(T container,
			AstEditor editor,
			Expression[] arguments,
			IModelResolver resolver,
			ModelCreator<T> modelCreator) throws Exception;
}