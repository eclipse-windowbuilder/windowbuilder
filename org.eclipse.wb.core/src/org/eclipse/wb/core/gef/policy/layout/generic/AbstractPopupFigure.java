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
package org.eclipse.wb.core.gef.policy.layout.generic;

import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.internal.core.utils.ui.DrawUtils;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Cursors;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

/**
 * Abstract {@link IFigure} that shows images of fixed size and open popup menu on click.
 *
 * @author scheglov_ke
 * @coverage core.gef.policy.generic
 */
public abstract class AbstractPopupFigure extends Figure {
	private static final int MARGIN = 6;
	private static final Color COLOR_BACKGROUND = DrawUtils.getShiftedColor(
			ColorConstants.white,
			-32);
	private static final Color COLOR_FOREGROUND = DrawUtils.getShiftedColor(
			ColorConstants.white,
			-64);
	private final IEditPartViewer m_viewer;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Creates new {@link AbstractPopupFigure}.
	 *
	 * @param width
	 *          the width of the image.
	 * @param height
	 *          the height of the image.
	 */
	public AbstractPopupFigure(IEditPartViewer viewer, int width, int height) {
		m_viewer = viewer;
		// configure figure
		setSize(width + MARGIN, height + MARGIN);
		setBackgroundColor(COLOR_BACKGROUND);
		setForegroundColor(COLOR_FOREGROUND);
		setCursor(Cursors.HAND);
		// add mouse listener
		addMouseListener(new MouseListener.Stub() {
			@Override
			public void mousePressed(MouseEvent event) {
				event.consume();
				// prepare IMenuManager
				MenuManager manager = new MenuManager();
				fillMenu(manager);
				// open context menu
				Control control = m_viewer.getControl();
				Menu menu = manager.createContextMenu(control);
				menu.setVisible(true);
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Paint
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void paintClientArea(Graphics graphics) {
		Rectangle clientArea = getClientArea();
		// draw filled rectangle
		graphics.fillRectangle(clientArea);
		graphics.drawRectangle(clientArea.getResized(-1, -1));
		// draw image
		{
			ImageDescriptor imageDescriptor = getImageDescriptor();
			if (imageDescriptor != null) {
				Image image = imageDescriptor.createImage();
				org.eclipse.swt.graphics.Rectangle imageBounds = image.getBounds();
				int x = (clientArea.width - imageBounds.width) / 2;
				int y = (clientArea.height - imageBounds.height) / 2;
				graphics.drawImage(image, x, y);
				image.dispose();
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Abstract methods
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the image to display.
	 */
	protected abstract ImageDescriptor getImageDescriptor();

	/**
	 * Creates the actions on given {@link IMenuManager}.
	 */
	protected abstract void fillMenu(IMenuManager manager);
}
