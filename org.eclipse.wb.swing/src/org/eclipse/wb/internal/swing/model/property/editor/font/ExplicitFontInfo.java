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
package org.eclipse.wb.internal.swing.model.property.editor.font;

import java.awt.Font;
import java.text.MessageFormat;

/**
 * Information object about {@link Font}.
 *
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
public final class ExplicitFontInfo extends FontInfo {
	private final Font m_font;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ExplicitFontInfo(Font font) {
		m_font = font;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Font getFont() {
		return m_font;
	}

	@Override
	public String getText() {
		return getText(m_font);
	}

	@Override
	public String getSource() throws Exception {
		return MessageFormat.format(
				"new java.awt.Font(\"{0}\", {1}, {2})",
				m_font.getFamily(),
				getStyleSource(m_font.getStyle()),
				m_font.getSize());
	}
}
