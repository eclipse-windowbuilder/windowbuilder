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
