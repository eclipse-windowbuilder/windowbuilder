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
package org.eclipse.wb.internal.core.gef.part.menu;

import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.internal.core.model.menu.IMenuInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuItemInfo;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

/**
 * {@link EditPart} for {@link IMenuItemInfo}.
 *
 * @author mitin_aa
 * @author scheglov_ke
 * @coverage core.gef.menu
 */
public final class MenuItemEditPart extends SubmenuAwareEditPart {
	private final IMenuItemInfo m_item;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public MenuItemEditPart(Object toolkitModel, IMenuItemInfo item) {
		super(toolkitModel, item);
		m_item = item;
	}

	/////////////////////////////////////////////////////////////////////
	//
	// Figure
	//
	/////////////////////////////////////////////////////////////////////
	@Override
	protected IFigure createFigure() {
		return new Figure() {
			@Override
			protected void paintClientArea(Graphics graphics) {
				// draw image
				{
					ImageDescriptor imageDescriptor = m_item.getImageDescriptor();
					if (imageDescriptor != null) {
						Image image = imageDescriptor.createImage();
						graphics.drawImage(image, 0, 0);
						image.dispose();
					}
				}
				// highlight "item" with displayed "menu"
				if (!getModelChildren().isEmpty()) {
					Rectangle area = getFigure().getClientArea();
					graphics.setForegroundColor(ColorConstants.menuBackgroundSelected);
					graphics.setBackgroundColor(ColorConstants.white);
					graphics.setLineWidth(2);
					graphics.drawRectangle(1, 1, area.width - 2, area.height - 2);
				}
			}
		};
	}

	@Override
	protected void refreshVisuals() {
		getFigure().setBounds(m_item.getBounds());
	}

	/////////////////////////////////////////////////////////////////////
	//
	// Children
	//
	/////////////////////////////////////////////////////////////////////
	@Override
	protected Object getChildMenu() {
		IMenuInfo menu = m_item.getMenu();
		return menu != null ? menu.getModel() : null;
	}
}
