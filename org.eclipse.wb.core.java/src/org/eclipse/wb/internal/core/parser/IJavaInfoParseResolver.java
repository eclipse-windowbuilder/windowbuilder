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
package org.eclipse.wb.internal.core.parser;

import org.eclipse.wb.core.model.JavaInfo;

import org.eclipse.jdt.core.dom.Expression;

/**
 * Interface for resolving {@link Expressions}'s into {@link JavaInfo} objects.
 *
 * @author scheglov_ke
 */
public interface IJavaInfoParseResolver {
	/**
	 * @return the {@link JavaInfo} for given {@link Expression} or <code>null</code> if given
	 *         {@link Expression} does not represent {@link JavaInfo}.
	 */
	JavaInfo getJavaInfo(Expression expression) throws Exception;
}
