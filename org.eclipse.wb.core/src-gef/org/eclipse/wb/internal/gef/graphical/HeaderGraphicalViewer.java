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
package org.eclipse.wb.internal.gef.graphical;

import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.RangeModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * @author lobas_av
 * @coverage gef.graphical
 */
public class HeaderGraphicalViewer extends GraphicalViewer {
	private GraphicalViewer m_mainViewer;
	private final boolean m_horizontal;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	////////////////////////////////////////////////////////////////////////////
	public HeaderGraphicalViewer(Composite parent, boolean horizontal) {
		this(parent, SWT.NONE, horizontal);
	}

	public HeaderGraphicalViewer(Composite parent, int style, boolean horizontal) {
		super(parent, style);
		getControl().setScrollBarVisibility(FigureCanvas.NEVER);
		m_horizontal = horizontal;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public void setMainViewer(GraphicalViewer mainViewer) {
		// set main viewer
		m_mainViewer = mainViewer;
		// set EditDomain
		setEditDomain(m_mainViewer.getEditDomain());
		// configure canvas
		if (m_horizontal) {
			setHorizontallHook();
		} else {
			setVerticalHook();
		}
	}

	private void setHorizontallHook() {
		// configure scrolling
		m_mainViewer.m_canvas.getViewport().getHorizontalRangeModel().addPropertyChangeListener(event -> {
			if (RangeModel.PROPERTY_VALUE.equals(event.getPropertyName())) {
				m_canvas.getViewport().getHorizontalRangeModel().setValue((int) event.getNewValue());
				getRootFigureInternal().repaint();
			}
		});
	}

	private void setVerticalHook() {
		// configure scrolling
		m_mainViewer.m_canvas.getViewport().getVerticalRangeModel().addPropertyChangeListener(event -> {
			if (RangeModel.PROPERTY_VALUE.equals(event.getPropertyName())) {
				m_canvas.getViewport().getVerticalRangeModel().setValue((int) event.getNewValue());
				getRootFigureInternal().repaint();
			}
		});
	}
}