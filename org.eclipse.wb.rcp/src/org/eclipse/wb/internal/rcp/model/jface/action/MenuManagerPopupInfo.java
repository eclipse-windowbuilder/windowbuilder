/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
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
package org.eclipse.wb.internal.rcp.model.jface.action;

import org.eclipse.wb.internal.core.model.menu.IMenuInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuPolicy;
import org.eclipse.wb.internal.core.model.menu.IMenuPopupInfo;
import org.eclipse.wb.internal.core.model.menu.JavaMenuMenuObject;
import org.eclipse.wb.internal.core.model.menu.MenuObjectInfoUtils;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Implementation of {@link IMenuPopupInfo} for dropping down some {@link MenuManagerInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.jface
 */
public final class MenuManagerPopupInfo extends JavaMenuMenuObject implements IMenuPopupInfo {
	private final MenuManagerInfo m_manager;
	private Rectangle m_bounds;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public MenuManagerPopupInfo(MenuManagerInfo manager) {
		super(manager);
		m_manager = manager;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Sets the bounds of popup figure.
	 */
	public void setBounds(Rectangle menuToolItemBounds) {
		m_bounds = menuToolItemBounds;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Model
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Object getModel() {
		return m_manager;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	@Override
	public Rectangle getBounds() {
		return m_bounds;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IMenuPopupInfo
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public IMenuInfo getMenu() {
		return MenuObjectInfoUtils.getMenuInfo(m_manager);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Policy
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public IMenuPolicy getPolicy() {
		return getMenu().getPolicy();
	}
}
