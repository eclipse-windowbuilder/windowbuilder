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
package org.eclipse.wb.internal.swing.MigLayout.model.ui;

import org.eclipse.wb.internal.swing.model.layout.gbl.DimensionInfo;

/**
 * Description for {@link DimensionInfo} alignment.
 *
 * @author scheglov_ke
 * @coverage swing.MigLayout.ui
 */
public final class AlignmentDescription<A extends Enum<?>> {
	private final A m_alignment;
	private final String m_title;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public AlignmentDescription(A alignment, String title) {
		m_alignment = alignment;
		m_title = title;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the alignment value.
	 */
	public A getAlignment() {
		return m_alignment;
	}

	/**
	 * @return the title for alignment.
	 */
	public String getTitle() {
		return m_title;
	}
}
