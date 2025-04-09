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
package org.eclipse.wb.internal.swing.model.component;

import org.eclipse.draw2d.geometry.Rectangle;

import javax.swing.JTabbedPane;

/**
 * Model for single tab on {@link JTabbedPane}.
 *
 * @author scheglov_ke
 * @coverage swing.model
 */
public final class JTabbedPaneTabInfo {
	private final JTabbedPaneInfo m_pane;
	private final ComponentInfo m_component;
	private final Rectangle m_bounds;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public JTabbedPaneTabInfo(JTabbedPaneInfo pane, ComponentInfo component, Rectangle bounds) {
		m_pane = pane;
		m_component = component;
		m_bounds = bounds;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Object
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public int hashCode() {
		return m_component.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof JTabbedPaneTabInfo tab) {
			return tab.m_component == m_component;
		}
		return false;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link JTabbedPaneInfo} container for this tab.
	 */
	public JTabbedPaneInfo getPane() {
		return m_pane;
	}

	/**
	 * @return the {@link ComponentInfo} of this tab.
	 */
	public ComponentInfo getComponent() {
		return m_component;
	}

	/**
	 * @return the bounds of this tab.
	 */
	public Rectangle getBounds() {
		return m_bounds;
	}
}
