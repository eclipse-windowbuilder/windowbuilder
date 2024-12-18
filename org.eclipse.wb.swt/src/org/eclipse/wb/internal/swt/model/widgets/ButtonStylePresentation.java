/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
	protected void initImages() throws Exception {
		addImage(SWT.CHECK, "wbp-meta/org/eclipse/swt/widgets/Button_check.gif");
		addImage(SWT.RADIO, "wbp-meta/org/eclipse/swt/widgets/Button_radio.gif");
	}
}