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
package org.eclipse.wb.internal.rcp.databinding.model.beans;

/**
 * @author lobas_av
 * @coverage bindings.rcp.model.beans
 */
final class LocalModelCreator extends ModelCreator<BeansObserveTypeContainer> {
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

	public LocalModelCreator(int startIndex0,
			int startIndex1,
			int startIndex2,
			ILocalModelCreator creator) {
		super(startIndex0, startIndex1, startIndex2, creator);
	}

	public LocalModelCreator(boolean isPojo, ILocalModelCreator creator) {
		super(creator);
		this.isPojo = isPojo;
	}

	public LocalModelCreator(int startIndex, boolean isPojo, ILocalModelCreator creator) {
		super(startIndex, creator);
		this.isPojo = isPojo;
	}

	public LocalModelCreator(int startIndex0,
			int startIndex1,
			boolean isPojo,
			ILocalModelCreator creator) {
		super(startIndex0, startIndex1, creator);
		this.isPojo = isPojo;
	}

	public LocalModelCreator(int startIndex0,
			int startIndex1,
			int startIndex2,
			boolean isPojo,
			ILocalModelCreator creator) {
		super(startIndex0, startIndex1, startIndex2, creator);
		this.isPojo = isPojo;
	}
}