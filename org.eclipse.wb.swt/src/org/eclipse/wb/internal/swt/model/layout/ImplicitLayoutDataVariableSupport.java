/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.swt.model.layout;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.variable.AbstractImplicitVariableSupport;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.swt.widgets.Control;

/**
 * Implementation of {@link VariableSupport} for implicit {@link LayoutDataInfo}.
 *
 * "Virtual" is state when there are no layout data at all, i.e. {@link Control#getLayoutData()}
 * returns <code>null</code>.
 *
 * "Implicit" is state when {@link Control#getLayoutData()} returns some not <code>null</code>
 * value, but layout data was not created it this {@link CompilationUnit}.
 *
 * @author lobas_av
 * @coverage swt.model.layout
 */
public final class ImplicitLayoutDataVariableSupport extends AbstractImplicitVariableSupport {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ImplicitLayoutDataVariableSupport(JavaInfo javaInfo) {
		super(javaInfo);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Object
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String toString() {
		return "implicit-layout-data";
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean isDefault() {
		return true;
	}

	@Override
	public String getTitle() throws Exception {
		return "(implicit layout data)";
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Materializing
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected JavaInfo getParent() {
		return m_javaInfo.getParentJava();
	}
}