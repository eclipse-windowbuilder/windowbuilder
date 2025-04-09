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
package org.eclipse.wb.internal.rcp.databinding.model.widgets.input;

import org.eclipse.wb.internal.core.databinding.model.AstObjectInfo;
import org.eclipse.wb.internal.core.databinding.model.CodeGenerationSupport;

import java.util.List;

/**
 * Source code generator for owner {@link AbstractViewerInputBindingInfo}.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.widgets
 */
public abstract class CodeSupport extends AstObjectInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Code generation
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Generate source code association with this object and add to <code>lines</code>.
	 */
	@Override
	public abstract void addSourceCode(List<String> lines, CodeGenerationSupport generationSupport)
			throws Exception;
}