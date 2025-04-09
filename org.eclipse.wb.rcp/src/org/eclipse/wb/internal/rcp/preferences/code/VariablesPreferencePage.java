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
package org.eclipse.wb.internal.rcp.preferences.code;

import org.eclipse.wb.internal.rcp.ToolkitProvider;

/**
 * Implementation of {@link org.eclipse.wb.internal.core.preferences.code.VariablesPreferencePage}
 * for RCP.
 *
 * @author scheglov_ke
 * @coverage rcp.preferences.ui
 */
public final class VariablesPreferencePage
extends
org.eclipse.wb.internal.core.preferences.code.VariablesPreferencePage {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public VariablesPreferencePage() {
		super(ToolkitProvider.DESCRIPTION);
	}
}
