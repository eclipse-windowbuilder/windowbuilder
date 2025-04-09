/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
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
package org.eclipse.wb.internal.swt.model.widgets.menu;

import org.eclipse.wb.internal.swt.model.widgets.StylePresentation;

import org.eclipse.swt.SWT;

/**
 * Presentation for SWT menu with style: <code>SWT.POP_UP</code>, <code>SWT.DROP_DOWN</code>,
 * <code>SWT.BAR</code>.
 *
 * @author mitin_aa
 * @author lobas_av
 * @coverage swt.model.widgets.menu
 */
public final class MenuStylePresentation extends StylePresentation {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public MenuStylePresentation(MenuInfo menu) {
		super(menu);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// StylePresentation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void initImages() throws Exception {
		addImage(SWT.BAR, "wbp-meta/org/eclipse/swt/widgets/Menu_bar.gif");
		addImage(SWT.POP_UP, "wbp-meta/org/eclipse/swt/widgets/Menu.gif");
		addImage(SWT.DROP_DOWN, "wbp-meta/org/eclipse/swt/widgets/Menu_dropdown.gif");
	}
}
