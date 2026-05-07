/*******************************************************************************
 * Copyright (c) 2011, 2026 Google, Inc. and others.
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
package org.eclipse.wb.internal.core.gef.part.menu;

import org.eclipse.wb.internal.core.model.menu.IMenuInfo;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.swt.SWT;

/**
 * Figure to represent menu on MacOSX.
 *
 * @author mitin_aa
 * @coverage core.gef.menu
 */
public final class MacMenuImageFigure extends MenuImageFigure {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public MacMenuImageFigure(IMenuInfo menu) {
		super(menu);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Paint
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void paintFigure(Graphics graphics) {
		super.paintFigure(graphics);
		// draw border on MacOSX because the fill color of menu is the same as fill color of window client area
		{
			graphics.setForegroundColor(ColorConstants.buttonLightest);
			graphics.setLineStyle(SWT.LINE_DASH);
			graphics.drawRectangle(bounds.x, bounds.y, bounds.width - 1, bounds.height - 1);
		}
	}
}
