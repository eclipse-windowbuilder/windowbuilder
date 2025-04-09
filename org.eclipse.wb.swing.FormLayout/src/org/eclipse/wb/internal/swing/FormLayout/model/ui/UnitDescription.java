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
package org.eclipse.wb.internal.swing.FormLayout.model.ui;

import com.jgoodies.forms.layout.ConstantSize.Unit;

/**
 * Description for {@link Unit}.
 *
 * @author scheglov_ke
 * @coverage swing.FormLayout.ui
 */
public final class UnitDescription {
	private final Unit m_unit;
	private final String m_title;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public UnitDescription(Unit unit, String title) {
		m_unit = unit;
		m_title = title;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link Unit} value.
	 */
	public Unit getUnit() {
		return m_unit;
	}

	/**
	 * @return the title to display.
	 */
	public String getTitle() {
		return m_title;
	}
}
