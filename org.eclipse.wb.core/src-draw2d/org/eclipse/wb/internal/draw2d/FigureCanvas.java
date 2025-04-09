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
package org.eclipse.wb.internal.draw2d;

import org.eclipse.wb.draw2d.Figure;

import org.eclipse.draw2d.DeferredUpdateManager;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.SWTGraphics;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

/**
 * A Canvas that contains {@link Figure Figures}.
 *
 * @author lobas_av
 * @coverage gef.draw2d
 */
public class FigureCanvas extends org.eclipse.draw2d.FigureCanvas {
	private RootFigure m_rootFigure;
	private final Dimension m_rootPreferredSize = new Dimension();

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public FigureCanvas(Composite parent, int style) {
		super(parent, style | SWT.NO_BACKGROUND | SWT.NO_REDRAW_RESIZE, createLightweightSystem());
		// create root figure
		createRootFigure();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// FigureCanvas
	//
	////////////////////////////////////////////////////////////////////////////

	private void createRootFigure() {
		m_rootFigure = new RootFigure(this);
		m_rootFigure.setBackgroundColor(getBackground());
		m_rootFigure.setForegroundColor(getForeground());
		m_rootFigure.setFont(getFont());
		setDefaultEventManager();
		setDefaultUpdateManager();
		setContents(m_rootFigure);
	}

	// TODO ptziegler - It should be possible to change the update manager after the
	// figure canvas has been created.
	private static LightweightSystem createLightweightSystem() {
		LightweightSystem lws = new LightweightSystem();
		lws.setUpdateManager(new CachedUpdateManager());
		return lws;
	}

	protected void setDefaultEventManager() {
		m_rootFigure.getFigureCanvas().getLightweightSystem().setEventDispatcher(new EventManager(this));
	}

	protected void setDefaultUpdateManager() {
		getUpdateManager().setControl(this);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////

	/**
	 * Returns figures container.
	 */
	public RootFigure getRootFigure() {
		return m_rootFigure;
	}

	/**
	 * Sets draw cached mode.
	 */
	public void setDrawCached(boolean value) {
		getUpdateManager().m_drawCached = value;
	}

	private CachedUpdateManager getUpdateManager() {
		return (CachedUpdateManager) getLightweightSystem().getUpdateManager();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Control
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Point computeSize(int wHint, int hHint, boolean changed) {
		Dimension size = m_rootFigure.getPreferredSize().getUnioned(wHint, hHint);
		return new Point(size.width, size.height);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Handle events
	//
	////////////////////////////////////////////////////////////////////////////

	/**
	 * Check bounds and reconfigure scroll bar's if needed and repaint client area.
	 */
	public void handleRefresh(int x, int y, int width, int height) {
		if (m_rootPreferredSize.equals(m_rootFigure.getPreferredSize())) {
			// calculate paint area
			Point size = getSize();
			Rectangle paintArea = new Rectangle(0, 0, size.x, size.y);
			paintArea.intersect(new Rectangle(x, y, width, height));
			// set repaint
			redraw(paintArea.x, paintArea.y, paintArea.width, paintArea.height, true);
		} else {
			org.eclipse.swt.graphics.Rectangle clientArea = getClientArea();
			Rectangle bounds = new Rectangle(clientArea).setLocation(0, 0);
			m_rootFigure.setBounds(bounds);
			getViewport().setBounds(bounds);
			getViewport().revalidate();
			// set repaint
			redraw();
		}
	}

	/**
	 * Update manager using double-buffering and allowing the temporary suspension
	 * of any actual paint operations. This mechanism is needed during the creation
	 * of the live images, where the design page is in an inconsistent page.
	 */
	private static class CachedUpdateManager extends DeferredUpdateManager {
		private FigureCanvas m_canvas;
		private Image m_bufferedImage;
		private boolean m_drawCached;

		private void setControl(FigureCanvas canvas) {
			m_canvas = canvas;
			m_canvas.addControlListener(ControlListener.controlResizedAdapter(event -> disposeImage()));
		}

		@Override
		protected void paint(GC paintGC) {
			org.eclipse.swt.graphics.Rectangle bounds = paintGC.getClipping();
			// check draw cached mode
			if (m_drawCached) {
				if (m_bufferedImage == null) {
					paintGC.fillRectangle(bounds);
				} else {
					paintGC.drawImage(m_bufferedImage, 0, 0);
				}
				return;
			}
			// check double buffered image
			if (m_bufferedImage == null) {
				Point size = m_canvas.getSize();
				m_bufferedImage = new Image(null, size.x, size.y);
			}
			// prepare double buffered Graphics
			GC bufferedGC = new GC(m_bufferedImage);
			try {
				bufferedGC.setClipping(bounds);
				bufferedGC.setBackground(paintGC.getBackground());
				bufferedGC.setForeground(paintGC.getForeground());
				bufferedGC.setFont(paintGC.getFont());
				bufferedGC.setLineStyle(paintGC.getLineStyle());
				bufferedGC.setLineWidth(paintGC.getLineWidth());
				bufferedGC.setXORMode(paintGC.getXORMode());
				// draw content
				Graphics graphics = new SWTGraphics(bufferedGC);
				int dx = -m_canvas.getViewport().getHorizontalRangeModel().getValue();
				int dy = -m_canvas.getViewport().getVerticalRangeModel().getValue();
				graphics.translate(dx, dy);
				m_canvas.getRootFigure().paint(graphics);
			} finally {
				bufferedGC.dispose();
			}
			// flush painting
			paintGC.drawImage(m_bufferedImage, 0, 0);
		}

		@Override
		public void dispose() {
			disposeImage();
		}

		private void disposeImage() {
			if (m_bufferedImage != null) {
				m_bufferedImage.dispose();
				m_bufferedImage = null;
			}
		}
	}
}