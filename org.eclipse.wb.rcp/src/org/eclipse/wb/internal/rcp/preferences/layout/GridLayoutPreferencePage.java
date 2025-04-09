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
package org.eclipse.wb.internal.rcp.preferences.layout;

import org.eclipse.wb.internal.rcp.ToolkitProvider;
import org.eclipse.wb.internal.swt.model.layout.grid.GridLayoutInfo;

import org.eclipse.jface.preference.PreferencePage;

/**
 * {@link PreferencePage} for {@link GridLayoutInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.preferences.ui
 */
public final class GridLayoutPreferencePage
extends
org.eclipse.wb.internal.swt.preferences.layout.GridLayoutPreferencePage {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public GridLayoutPreferencePage() {
		super(ToolkitProvider.DESCRIPTION);
	}
}
