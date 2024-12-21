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
	protected void initImages() throws Exception {
		addImage(SWT.CHECK, "wbp-meta/org/eclipse/swt/widgets/MenuItem_check.gif");
		addImage(SWT.RADIO, "wbp-meta/org/eclipse/swt/widgets/MenuItem_radio.gif");
		addImage(SWT.SEPARATOR, "wbp-meta/org/eclipse/swt/widgets/MenuItem_separator.gif");
	}
}
