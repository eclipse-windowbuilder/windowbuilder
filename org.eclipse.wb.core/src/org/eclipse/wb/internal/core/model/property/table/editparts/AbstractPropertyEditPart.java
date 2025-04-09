/*******************************************************************************
 * Copyright (c) 2025 Patrick Ziegler and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Patrick Ziegler - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.model.property.table.editparts;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.property.table.PropertyTable;
import org.eclipse.wb.internal.core.utils.ui.DrawUtils;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public abstract class AbstractPropertyEditPart extends AbstractGraphicalEditPart {
	////////////////////////////////////////////////////////////////////////////
	//
	// Colors
	//
	////////////////////////////////////////////////////////////////////////////
	protected static final Color COLOR_BACKGROUND = ColorConstants.listBackground;
	protected static final Color COLOR_NO_PROPERTIES = ColorConstants.gray;
	protected static final Color COLOR_LINE = ColorConstants.lightGray;
	protected static final Color COLOR_COMPLEX_LINE = DrawUtils.getShiftedColor(ColorConstants.lightGray, -32);
	protected static final Color COLOR_PROPERTY_BG = DrawUtils.getShiftedColor(COLOR_BACKGROUND, -12);
	protected static final Color COLOR_PROPERTY_BG_MODIFIED = COLOR_BACKGROUND;
	protected static final Color COLOR_PROPERTY_FG_TITLE = ColorConstants.listForeground;
	protected static final Color COLOR_PROPERTY_FG_VALUE = DrawUtils.isDarkColor(ColorConstants.listBackground)
			? ColorConstants.lightBlue
			: ColorConstants.darkBlue;
	protected static final Color COLOR_PROPERTY_BG_SELECTED = Display.getCurrent()
			.getSystemColor(SWT.COLOR_LIST_SELECTION);
	protected static final Color COLOR_PROPERTY_FG_SELECTED = Display.getCurrent()
			.getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT);
	protected static final Color COLOR_PROPERTY_FG_ADVANCED = ColorConstants.gray;
	////////////////////////////////////////////////////////////////////////////
	//
	// Sizes
	//
	////////////////////////////////////////////////////////////////////////////
	protected static final int MIN_COLUMN_WIDTH = 75;
	protected static final int MARGIN_LEFT = 2;
	protected static final int MARGIN_RIGHT = 1;
	protected static final int MARGIN_BOTTOM = 1;
	protected static final int STATE_IMAGE_MARGIN_RIGHT = 4;
	////////////////////////////////////////////////////////////////////////////
	//
	// Images
	//
	////////////////////////////////////////////////////////////////////////////
	protected static final Image m_plusImage = DesignerPlugin.getImage("properties/plus.gif");
	protected static final Image m_minusImage = DesignerPlugin.getImage("properties/minus.gif");
	protected static int m_stateWidth = 9;

	@Override
	protected void createEditPolicies() {
		// Nothing to do
	}

	@Override
	public PropertyTable getViewer() {
		return (PropertyTable) super.getViewer();
	}
}
