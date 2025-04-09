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
package org.eclipse.wb.internal.rcp.databinding.emf.model;

import org.eclipse.wb.internal.rcp.databinding.model.beans.ModelCreator;

/**
 * @author lobas_av
 * @coverage bindings.rcp.emf.model
 */
final class LocalModelCreator extends ModelCreator<EmfObserveTypeContainer> {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	////////////////////////////////////////////////////////////////////////////
	public LocalModelCreator(ILocalModelCreator creator) {
		super(creator);
	}

	public LocalModelCreator(int startIndex, ILocalModelCreator creator) {
		super(startIndex, creator);
	}

	public LocalModelCreator(int startIndex0, int startIndex1, ILocalModelCreator creator) {
		super(startIndex0, startIndex1, creator);
	}
}