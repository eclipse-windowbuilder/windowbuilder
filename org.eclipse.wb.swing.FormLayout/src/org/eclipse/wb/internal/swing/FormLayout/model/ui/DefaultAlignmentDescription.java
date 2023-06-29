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
package org.eclipse.wb.internal.swing.FormLayout.model.ui;

import com.jgoodies.forms.layout.FormSpec.DefaultAlignment;

/**
 * Description of {@link DefaultAlignment} for {@link DimensionEditDialog}.
 *
 * @author scheglov_ke
 * @coverage swing.FormLayout.ui
 */
public final class DefaultAlignmentDescription {
	private final DefaultAlignment m_alignment;
	private final String m_title;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public DefaultAlignmentDescription(DefaultAlignment alignment, String title) {
		m_alignment = alignment;
		m_title = title;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link DefaultAlignment} value.
	 */
	public DefaultAlignment getAlignment() {
		return m_alignment;
	}

	/**
	 * @return the title to display.
	 */
	public String getTitle() {
		return m_title;
	}
}
