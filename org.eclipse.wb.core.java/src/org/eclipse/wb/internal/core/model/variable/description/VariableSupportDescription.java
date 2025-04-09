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
package org.eclipse.wb.internal.core.model.variable.description;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.generation.GenerationDescription;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;

/**
 * {@link VariableSupportDescription} describes some specific {@link VariableSupport}.
 *
 * @author scheglov_ke
 * @coverage core.model.variable
 */
public abstract class VariableSupportDescription extends GenerationDescription {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	protected VariableSupportDescription(String id, String name, String description) {
		super(id, name, description);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the type of {@link VariableSupport} that will be created by this
	 *         {@link VariableSupportDescription}.
	 */
	public abstract Class<? extends VariableSupport> getType();

	/**
	 * @return the {@link VariableSupport} for given {@link JavaInfo}.
	 */
	public abstract VariableSupport createSupport(JavaInfo javaInfo);
}
