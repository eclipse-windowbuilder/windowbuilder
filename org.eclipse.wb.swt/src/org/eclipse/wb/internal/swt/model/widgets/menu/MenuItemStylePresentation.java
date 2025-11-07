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
package org.eclipse.wb.internal.swt.model.widgets.menu;

import org.eclipse.wb.internal.swt.model.widgets.StylePresentation;
import org.eclipse.wb.internal.swt.model.widgets.WidgetInfo;

import org.eclipse.swt.SWT;

/**
 * Presentation for SWT menu item with style: <code>SWT.CHECK</code>, <code>SWT.RADIO</code>,
 * <code>SWT.SEPARATOR</code>.
 *
 * @author mitin_aa
 * @coverage swt.model.widgets.menu.presentation
 */
public final class MenuItemStylePresentation extends StylePresentation {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public MenuItemStylePresentation(MenuItemInfo item) {
		super(item);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Custom text for separator
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getText() throws Exception {
		if ((((WidgetInfo) m_javaInfo).getWidget().getStyle() & SWT.SEPARATOR) != 0) {
			return "<separator>";
		}
		return super.getText();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// StylePresentation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void initImages() {
		addImage(SWT.CHECK, "wbp-meta/org/eclipse/swt/widgets/MenuItem_check.gif");
		addImage(SWT.RADIO, "wbp-meta/org/eclipse/swt/widgets/MenuItem_radio.gif");
		addImage(SWT.SEPARATOR, "wbp-meta/org/eclipse/swt/widgets/MenuItem_separator.gif");
	}
}
