/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.swing.model.component.menu;

import org.eclipse.wb.internal.core.model.menu.AbstractMenuObject;
import org.eclipse.wb.internal.core.model.menu.IMenuInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuItemInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuPolicy;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.resource.ImageDescriptor;

import java.util.Optional;

/**
 * Implementation of {@link IMenuItemInfo} for any {@link ComponentInfo}.
 *
 * @author scheglov_ke
 * @coverage swing.model.menu
 */
public class ComponentMenuItemInfo extends AbstractMenuObject implements IMenuItemInfo {
	private final ComponentInfo m_component;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ComponentMenuItemInfo(ComponentInfo component) {
		super(component);
		m_component = component;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Model
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Object getModel() {
		return m_component;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public ImageDescriptor getImageDescriptor() {
		return Optional.ofNullable(m_component.getImage()).map(ImageDescriptor::createFromImage).orElse(null);
	}

	@Override
	public Rectangle getBounds() {
		return m_component.getBounds();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IMenuItemInfo
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public IMenuInfo getMenu() {
		return null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Policy
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public IMenuPolicy getPolicy() {
		return IMenuPolicy.NOOP;
	}
}
