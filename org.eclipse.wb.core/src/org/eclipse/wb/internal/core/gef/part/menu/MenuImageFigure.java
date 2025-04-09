/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
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

import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.internal.core.model.menu.IMenuInfo;

import org.eclipse.draw2d.Graphics;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

/**
 * Figure to display menu image.
 *
 * @author mitin_aa
 * @coverage core.gef.figure
 */
public class MenuImageFigure extends Figure {
	private final IMenuInfo m_menu;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public MenuImageFigure(IMenuInfo menu) {
		m_menu = menu;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Paint
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void paintClientArea(Graphics graphics) {
		ImageDescriptor imageDescriptor = m_menu.getImageDescriptor();
		Image image = imageDescriptor.createImage();
		graphics.drawImage(image, 0, 0);
		image.dispose();
	}
}
