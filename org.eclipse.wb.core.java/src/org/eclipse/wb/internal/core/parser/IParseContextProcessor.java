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

import org.eclipse.wb.core.eval.ExecutionFlowDescription;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;

import java.util.List;

/**
 * Processor for {@link ParseRootContext}, used before parsing.
 *
 * @author scheglov_ke
 * @coverage core.model.parser
 */
public interface IParseContextProcessor {
	void process(AstEditor editor, ExecutionFlowDescription flowDescription, List<JavaInfo> components)
			throws Exception;
}
