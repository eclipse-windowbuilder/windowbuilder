/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
