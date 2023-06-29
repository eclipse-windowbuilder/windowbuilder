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
package org.eclipse.wb.internal.rcp.model.jface;

import org.eclipse.wb.internal.core.model.TopBoundsSupport;

/**
 * Implementation of {@link TopBoundsSupport} for {@link PopupDialogInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.jface
 */
public final class PopupDialogTopBoundsSupport extends WindowTopBoundsSupport {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public PopupDialogTopBoundsSupport(PopupDialogInfo window) {
		super(window);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Show
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean show() throws Exception {
		return false;
	}
}