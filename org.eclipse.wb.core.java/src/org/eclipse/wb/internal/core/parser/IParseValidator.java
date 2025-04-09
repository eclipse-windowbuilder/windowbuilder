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

import org.eclipse.wb.internal.core.utils.ast.AstEditor;

/**
 * Validator of Java source/AST.
 * <p>
 * Sometimes we know that we don't support some specific pattern of Java source. So, instead of
 * failing with generic message, we validate source/AST and show specific message.
 *
 * @author scheglov_ke
 * @coverage core.model.parser
 */
public interface IParseValidator {
	/**
	 * Validates {@link AstEditor}, does nothing if OK. Throws some exception in case of any problem.
	 */
	void validate(AstEditor editor) throws Exception;
}
