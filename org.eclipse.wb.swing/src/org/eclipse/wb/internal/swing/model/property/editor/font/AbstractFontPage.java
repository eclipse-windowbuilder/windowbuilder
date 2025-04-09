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

import org.eclipse.swt.widgets.Composite;

/**
 * Abstract page for editing {@link FontInfo} in {@link FontDialog}.
 *
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
public abstract class AbstractFontPage extends Composite {
	protected final FontDialog m_fontDialog;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public AbstractFontPage(Composite parent, int style, FontDialog fontDialog) {
		super(parent, style);
		m_fontDialog = fontDialog;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Sets the {@link FontInfo} selected in {@link FontDialog}.
	 *
	 * @return <code>true</code> if this page thinks that it is suits for given {@link FontInfo}.
	 */
	public abstract boolean setFont(FontInfo fontInfo);
}
