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
package org.eclipse.wb.internal.swing.MigLayout.model.ui;

/**
 * Description for size unit.
 *
 * @author scheglov_ke
 * @coverage swing.MigLayout.ui
 */
public final class UnitDescription {
	private final String m_unit;
	private final String m_title;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public UnitDescription(String unit, String title) {
		m_unit = unit;
		m_title = title;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the unit name.
	 */
	public String getUnit() {
		return m_unit;
	}

	/**
	 * @return the title to display.
	 */
	public String getTitle() {
		return m_title;
	}
}
