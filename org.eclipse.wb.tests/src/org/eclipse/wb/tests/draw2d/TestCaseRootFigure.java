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
package org.eclipse.wb.tests.draw2d;

import org.eclipse.wb.internal.draw2d.RootFigure;
import org.eclipse.wb.tests.gef.TestLogger;

import org.eclipse.draw2d.GraphicsSource;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.UpdateManager;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * @author lobas_av
 *
 */
public class TestCaseRootFigure extends RootFigure {
	private final UpdateManager m_testManager;
	private final TestLogger m_logger;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public TestCaseRootFigure(TestLogger logger) {
		super(null);
		m_logger = logger;
		m_testManager = new UpdateManager() {
			@Override
			public void addDirtyRegion(IFigure figure, int x, int y, int w, int h) {
				if (m_logger != null) {
					m_logger.log("repaint(" + x + ", " + y + ", " + w + ", " + h + ")");
				}
			}

			@Override
			public void addInvalidFigure(IFigure figure) {
				// Not relevant for testing...
			}

			@Override
			public void performUpdate() {
				// Not relevant for testing...
			}

			@Override
			public void performUpdate(Rectangle exposed) {
				// Not relevant for testing...
			}

			@Override
			public void setGraphicsSource(GraphicsSource gs) {
				// Not relevant for testing...
			}

			@Override
			public void setRoot(IFigure figure) {
				// Not relevant for testing...
			}
		};
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Figure
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void repaint(int x, int y, int width, int height) {
		if (m_logger != null) {
			m_logger.log("repaint(" + x + ", " + y + ", " + width + ", " + height + ")");
		}
	}

	@Override
	public void invalidate() {
		if (m_logger != null) {
			m_logger.log("invalidate");
		}
	}

	@Override
	protected void updateCursor() {
		if (m_logger != null) {
			m_logger.log("updateCursor");
		}
	}

	@Override
	public UpdateManager getUpdateManager() {
		return m_testManager;
	}
}