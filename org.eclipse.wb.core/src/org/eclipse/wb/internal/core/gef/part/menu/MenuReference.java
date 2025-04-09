/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
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
package org.eclipse.wb.internal.core.gef.part.menu;

import org.eclipse.wb.internal.core.model.menu.IMenuInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuItemInfo;

/**
 * In Swing same object can be at same time {@link IMenuItemInfo} and {@link IMenuInfo}, so
 * to distinguish them we need to use separate model, this reference.
 *
 * @author scheglov_ke
 * @coverage core.gef.menu
 */
public final class MenuReference {
	private final IMenuInfo m_menu;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public MenuReference(IMenuInfo menu) {
		m_menu = menu;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the underlying {@link IMenuInfo} model.
	 */
	public IMenuInfo getMenu() {
		return m_menu;
	}
}
