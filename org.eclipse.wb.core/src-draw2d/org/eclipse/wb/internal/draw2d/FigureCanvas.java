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
package org.eclipse.wb.internal.draw2d;

import org.eclipse.wb.draw2d.Figure;

import org.eclipse.draw2d.DeferredUpdateManager;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
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

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public FigureCanvas(Composite parent, int style) {
		super(parent, style | SWT.DOUBLE_BUFFERED, createLightweightSystem());
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
		getLightweightSystem().setEventDispatcher(new EventManager(this));
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
	 * Update manager allowing the temporary suspension of any actual paint
	 * operations. This mechanism is needed during the creation of the live images,
	 * where the design page is in an inconsistent page.
	 */
	private static class CachedUpdateManager extends DeferredUpdateManager {
		private FigureCanvas m_canvas;
		private boolean m_drawCached;

		private void setControl(FigureCanvas canvas) {
			m_canvas = canvas;
		}

		@Override
		public synchronized void performUpdate() {
			if (m_drawCached) {
				queueWork();
				return;
			}
			super.performUpdate();
		}

		@Override
		protected void paint(GC gc) {
			if (m_drawCached) {
				addDirtyRegion(m_canvas.getRootFigure(), new Rectangle(gc.getClipping()));
				return;
			}
			super.paint(gc);
		}
	}
}