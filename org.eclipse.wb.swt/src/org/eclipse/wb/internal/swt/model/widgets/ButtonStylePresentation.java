/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
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
package org.eclipse.wb.internal.swt.model.widgets;

import org.eclipse.swt.SWT;

/**
 * Presentation for button with style: <code>CHECK</code> or <code>RADIO</code>.
 *
 * @author lobas_av
 * @author mitin_aa
 * @coverage swt.model.presentation
 */
public final class ButtonStylePresentation extends StylePresentation {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ButtonStylePresentation(ButtonInfo button) {
		super(button);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// StylePresentation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void initImages() {
		addImage(SWT.CHECK, "wbp-meta/org/eclipse/swt/widgets/Button_check.gif");
		addImage(SWT.RADIO, "wbp-meta/org/eclipse/swt/widgets/Button_radio.gif");
	}
}