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
package org.eclipse.wb.internal.core.utils.ui.dialogs.color;

import org.eclipse.swt.widgets.Composite;

/**
 * Abstract {@link Composite} for color selection.
 *
 * @author scheglov_ke
 * @coverage core.ui
 */
public abstract class AbstractColorsComposite extends Composite {
	protected final AbstractColorDialog m_colorDialog;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public AbstractColorsComposite(Composite parent, int style, AbstractColorDialog colorDialog) {
		super(parent, style);
		m_colorDialog = colorDialog;
	}
}
