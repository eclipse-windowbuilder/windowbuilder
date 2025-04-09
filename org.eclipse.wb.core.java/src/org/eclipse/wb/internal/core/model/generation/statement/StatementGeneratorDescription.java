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
package org.eclipse.wb.internal.core.model.generation.statement;

import org.eclipse.wb.internal.core.model.generation.GenerationDescription;

/**
 * {@link StatementGeneratorDescription} describes some specific {@link StatementGenerator}.
 *
 * @author scheglov_ke
 * @coverage core.model.generation
 */
public abstract class StatementGeneratorDescription extends GenerationDescription {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	protected StatementGeneratorDescription(String id, String name, String description) {
		super(id, name, description);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the instance of {@link StatementGenerator}.
	 */
	public abstract StatementGenerator get();
}
