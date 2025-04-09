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
package org.eclipse.wb.internal.rcp.model.forms;

import org.eclipse.wb.core.eval.ExecutionFlowDescription;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.parser.IParseContextProcessor;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;

import org.eclipse.jdt.core.dom.MethodDeclaration;

import java.util.List;

/**
 * {@link IParseContextProcessor} for Forms API.
 *
 * @author scheglov_ke
 * @coverage rcp.model.forms
 */
public final class FormToolkitParseContextProcessor implements IParseContextProcessor {
	////////////////////////////////////////////////////////////////////////////
	//
	// Instance
	//
	////////////////////////////////////////////////////////////////////////////
	public static final Object INSTANCE = new FormToolkitParseContextProcessor();

	private FormToolkitParseContextProcessor() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IParseContextProcessor
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void process(AstEditor editor,
			ExecutionFlowDescription flowDescription,
			List<JavaInfo> components) throws Exception {
		List<MethodDeclaration> methods = flowDescription.getStartMethods();
		if (!methods.isEmpty()) {
			MethodDeclaration method = methods.get(0);
			FormToolkitAccessUtils.createFormToolkit_asMethodParameter(editor, method);
		}
	}
}
