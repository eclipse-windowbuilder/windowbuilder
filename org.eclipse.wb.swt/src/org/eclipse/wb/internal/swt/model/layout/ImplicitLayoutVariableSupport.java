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
package org.eclipse.wb.internal.swt.model.layout;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.variable.AbstractImplicitVariableSupport;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;

/**
 * Implementation of {@link VariableSupport} for implicit {@link LayoutInfo}.
 *
 * @author scheglov_ke
 * @coverage swt.model.layout
 */
public final class ImplicitLayoutVariableSupport extends AbstractImplicitVariableSupport {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ImplicitLayoutVariableSupport(JavaInfo javaInfo) {
		super(javaInfo);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Object
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String toString() {
		return "implicit-layout";
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
		return "(implicit layout)";
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
