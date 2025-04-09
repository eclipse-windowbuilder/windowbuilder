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
package org.eclipse.wb.internal.rcp.databinding.model.context.strategies;

import org.eclipse.wb.internal.core.utils.ast.AstEditor;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;

/**
 * Model for <code>org.eclipse.core.databinding.conversion.IConverter</code> objects.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.context
 */
public final class ConverterInfo extends StrategyPropertyInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	////////////////////////////////////////////////////////////////////////////
	public ConverterInfo(String className) {
		super(className);
	}

	public ConverterInfo(AstEditor editor, ClassInstanceCreation creation) {
		super(editor, creation);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Code generation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected String getBaseClassName() {
		return "org.eclipse.core.databinding.conversion.IConverter";
	}
}