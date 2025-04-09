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
package org.eclipse.wb.internal.core.model.variable;

import org.eclipse.wb.core.model.JavaInfo;

/**
 * Implementation of {@link VariableSupport} for cases when there are no variable name, so no
 * implementation of methods that operates this name.
 *
 * @author scheglov_ke
 * @coverage core.model.variable
 */
public abstract class AbstractNoNameVariableSupport extends VariableSupport {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public AbstractNoNameVariableSupport(JavaInfo javaInfo) {
		super(javaInfo);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Name
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public final boolean hasName() {
		return false;
	}

	@Override
	public final String getName() {
		throw new IllegalStateException();
	}

	@Override
	public final void setName(String newName) throws Exception {
		throw new IllegalStateException();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Conversion
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public final boolean canConvertLocalToField() {
		return false;
	}

	@Override
	public final void convertLocalToField() throws Exception {
		throw new IllegalStateException();
	}

	@Override
	public final boolean canConvertFieldToLocal() {
		return false;
	}

	@Override
	public final void convertFieldToLocal() throws Exception {
		throw new IllegalStateException();
	}
}
