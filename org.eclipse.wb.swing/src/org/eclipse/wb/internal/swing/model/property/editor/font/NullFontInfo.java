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

/**
 * Information object about {@link Font}.
 *
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
public final class NullFontInfo extends FontInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Font getFont() {
		return null;
	}

	@Override
	public String getText() {
		return null;
	}

	@Override
	public String getSource() throws Exception {
		return "null";
	}
}
