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
package org.eclipse.wb.core.controls.flyout;

/**
 * Implementation of {@link IFlyoutPreferences} for keeping settings in memory.
 *
 * @author scheglov_ke
 * @coverage core.control
 */
public final class MemoryFlyoutPreferences implements IFlyoutPreferences {
	private int m_dockLocation;
	private int m_state;
	private int m_width;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public MemoryFlyoutPreferences(int dockLocation, int state, int width) {
		m_dockLocation = dockLocation;
		m_state = state;
		m_width = width;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IFlyoutPreferences
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public int getDockLocation() {
		return m_dockLocation;
	}

	@Override
	public int getState() {
		return m_state;
	}

	@Override
	public int getWidth() {
		return m_width;
	}

	@Override
	public void setDockLocation(int location) {
		m_dockLocation = location;
	}

	@Override
	public void setState(int state) {
		m_state = state;
	}

	@Override
	public void setWidth(int width) {
		m_width = width;
	}
}