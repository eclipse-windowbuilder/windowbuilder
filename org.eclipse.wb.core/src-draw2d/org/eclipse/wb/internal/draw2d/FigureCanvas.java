/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.draw2d;

import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.SWTGraphics;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * A Canvas that contains {@link Figure Figures}.
 *
 * @author lobas_av
 * @coverage gef.draw2d
 */
public class FigureCanvas extends org.eclipse.draw2d.FigureCanvas {
	private RootFigure m_rootFigure;
	private final Dimension m_rootPreferredSize = new Dimension();
	// TODO ptziegler: Painting the figures on the canvas is the responsibility of
	// the UpdateManager, not the FigureCanvas.
	@Deprecated
	private Image m_bufferedImage;
	private boolean m_drawCached;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public FigureCanvas(Composite parent, int style) {
		super(parent, style | SWT.NO_BACKGROUND | SWT.NO_REDRAW_RESIZE, createLightweightSystem());
		// add all listeners
		hookControlEvents();
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
		setContents(m_rootFigure);
	}

	private static LightweightSystem createLightweightSystem() {
		return new LightweightSystem() {
			private FigureCanvas getFigureCanvas() {
				return (FigureCanvas) ReflectionUtils.getFieldObject(this, "canvas");
			}

			@Override
			protected void controlResized() {
				getFigureCanvas().disposeBufferedImage();
				super.controlResized();
			}

			@Override
			public void paint(GC gc) {
				org.eclipse.swt.graphics.Rectangle bounds = gc.getClipping();
				getFigureCanvas().handlePaint(gc, bounds.x, bounds.y, bounds.width, bounds.height);
			}
		};
	}

	protected void setDefaultEventManager() {
		m_rootFigure.setEventManager(new EventManager(this));
	}

	private void disposeBufferedImage() {
		if (m_bufferedImage != null) {
			m_bufferedImage.dispose();
			m_bufferedImage = null;
		}
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
		m_drawCached = value;
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
	private void hookControlEvents() {
		addListener(SWT.Dispose, new Listener() {
			@Override
			public void handleEvent(Event event) {
				disposeBufferedImage();
			}
		});
	}

	private void handlePaint(GC paintGC, int x, int y, int width, int height) {
		// check draw cached mode
		if (m_drawCached) {
			if (m_bufferedImage == null) {
				paintGC.fillRectangle(x, y, width, height);
			} else {
				paintGC.drawImage(m_bufferedImage, 0, 0);
			}
			return;
		}
		// check double buffered image
		if (m_bufferedImage == null) {
			Point size = getSize();
			m_bufferedImage = new Image(null, size.x, size.y);
		}
		// prepare double buffered Graphics
		GC bufferedGC = new GC(m_bufferedImage);
		try {
			bufferedGC.setClipping(x, y, width, height);
			bufferedGC.setBackground(paintGC.getBackground());
			bufferedGC.setForeground(paintGC.getForeground());
			bufferedGC.setFont(paintGC.getFont());
			bufferedGC.setLineStyle(paintGC.getLineStyle());
			bufferedGC.setLineWidth(paintGC.getLineWidth());
			bufferedGC.setXORMode(paintGC.getXORMode());
			// draw content
			Graphics graphics = new SWTGraphics(bufferedGC);
			int dx = -getViewport().getHorizontalRangeModel().getValue();
			int dy = -getViewport().getVerticalRangeModel().getValue();
			graphics.translate(dx, dy);
			m_rootFigure.paint(graphics);
		} finally {
			bufferedGC.dispose();
		}
		// flush painting
		paintGC.drawImage(m_bufferedImage, 0, 0);
	}

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
}