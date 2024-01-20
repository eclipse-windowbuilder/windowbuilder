/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.core.controls.palette;

import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.internal.core.model.property.table.HtmlTooltipHelper;
import org.eclipse.wb.internal.draw2d.ICustomTooltipProvider;
import org.eclipse.wb.internal.draw2d.ICustomTooltipSite;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import org.apache.commons.text.StringEscapeUtils;

/**
 * Standard palette tooltip: bold header and multi line details.
 *
 * @author scheglov_ke
 * @coverage core.control.palette
 */
public final class HtmlPaletteTooltipProvider implements ICustomTooltipProvider {
	private final String m_header;
	private final String m_details;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public HtmlPaletteTooltipProvider(String header, String details) {
		m_header = StringEscapeUtils.escapeHtml4(header);
		m_details = details;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ICustomTooltipProvider
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Control createTooltipControl(Composite parent, ICustomTooltipSite site, Figure figure) {
		return HtmlTooltipHelper.createTooltipControl(parent, m_header, m_details);
	}

	@Override
	public void show(Shell shell) {
		// do nothing, show Shell when Browser ready
	}
}