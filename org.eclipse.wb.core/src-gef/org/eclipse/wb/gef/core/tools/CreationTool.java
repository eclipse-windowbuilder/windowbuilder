/*******************************************************************************
 * Copyright (c) 2011, 2026 Google, Inc. and others.
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
package org.eclipse.wb.gef.core.tools;

import org.eclipse.wb.gef.core.requests.DesignCreationFactory;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.requests.CreationFactory;

/**
 * The {@link CreationTool} creates new {@link EditPart EditParts} via a {@link CreationFactory}.
 * If the user simply clicks on the viewer, the default sized {@link EditPart} will be created at
 * that point. If the user clicks and drags, the created {@link EditPart} will be sized based on
 * where the user clicked and dragged.
 *
 * @author lobas_av
 * @coverage gef.core
 */
public class CreationTool extends org.eclipse.gef.tools.CreationTool {

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public CreationTool(CreationFactory factory) {
		super(factory);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void activate() {
		super.activate();
		try {
			if (getFactory() instanceof DesignCreationFactory factory) {
				factory.activate();
			}
		} catch (Throwable e) {
			getDomain().loadDefaultTool();
		}
	}

	/**
	 * @return the {@link CreationFactory} used to create the new {@link EditPart}'s.
	 */
	@Override
	public final CreationFactory getFactory() {
		return super.getFactory();
	}
}