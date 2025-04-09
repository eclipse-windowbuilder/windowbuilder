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
package org.eclipse.wb.internal.swt.model.property.editor.font;

import org.eclipse.swt.widgets.Composite;

/**
 * Abstract page for editing {@link FontInfo} in {@link FontDialog}.
 *
 * @author lobas_av
 * @coverage swt.property.editor
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
	 */
	public abstract void setFont(FontInfo fontInfo);
}