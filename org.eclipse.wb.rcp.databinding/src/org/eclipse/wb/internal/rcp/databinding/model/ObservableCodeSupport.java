/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
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
package org.eclipse.wb.internal.rcp.databinding.model;

import org.eclipse.wb.internal.core.databinding.model.AstObjectInfo;
import org.eclipse.wb.internal.core.databinding.model.CodeGenerationSupport;

import java.util.List;

/**
 * Source code generator for owner {@link ObservableInfo}. {@link ObservableInfo} can have multiple
 * code generators (f.e. BeanProperties).
 *
 * @author lobas_av
 * @coverage bindings.rcp.model
 */
public abstract class ObservableCodeSupport extends AstObjectInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Code generation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public final void addSourceCode(List<String> lines, CodeGenerationSupport generationSupport)
			throws Exception {
		throw new UnsupportedOperationException();
	}

	/**
	 * Generate source code association with given {@link ObservableInfo} observable and add to
	 * <code>lines</code>.
	 */
	public abstract void addSourceCode(ObservableInfo observable,
			List<String> lines,
			CodeGenerationSupport generationSupport) throws Exception;
}