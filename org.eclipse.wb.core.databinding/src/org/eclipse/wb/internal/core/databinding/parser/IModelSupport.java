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

import org.eclipse.jdt.core.dom.Expression;

/**
 * Helper class that know as {@link AstObjectInfo} represented on AST.
 *
 * @author lobas_av
 * @coverage bindings.parser
 */
public interface IModelSupport {
	/**
	 * @return {@link AstObjectInfo} host model.
	 */
	AstObjectInfo getModel();

	/**
	 * @return <code>true</code> if given {@link Expression} represented host model.
	 */
	boolean isRepresentedBy(Expression expression) throws Exception;
}