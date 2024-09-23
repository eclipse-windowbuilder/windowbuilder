/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.gef.graphical;

import org.eclipse.wb.internal.draw2d.IPreferredSizeProvider;

import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.RangeModel;
import org.eclipse.draw2d.geometry.Dimension;
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
		// configure root preferred size
		getRootFigureInternal().setPreferredSizeProvider(new IPreferredSizeProvider() {
			@Override
			public Dimension getPreferredSize(Dimension originalPreferredSize) {
				return new Dimension(m_mainViewer.getRootFigureInternal().getPreferredSize().width
						+ m_mainViewer.m_canvas.getVerticalBar().getSize().x, originalPreferredSize.height);
			}
		});
		// configure scrolling
		m_mainViewer.m_canvas.getViewport().getHorizontalRangeModel().addPropertyChangeListener(event -> {
			if (RangeModel.PROPERTY_VALUE.equals(event.getPropertyName())) {
				m_canvas.getViewport().getHorizontalRangeModel().setValue((int) event.getNewValue());
				getRootFigureInternal().repaint();
			}
		});
	}

	private void setVerticalHook() {
		// configure root preferred size
		getRootFigureInternal().setPreferredSizeProvider(new IPreferredSizeProvider() {
			@Override
			public Dimension getPreferredSize(Dimension originalPreferredSize) {
				return new Dimension(originalPreferredSize.width,
						m_mainViewer.getRootFigureInternal().getPreferredSize().height
						+ m_mainViewer.m_canvas.getHorizontalBar().getSize().y);
			}
		});
		// configure scrolling
		m_mainViewer.m_canvas.getViewport().getVerticalRangeModel().addPropertyChangeListener(event -> {
			if (RangeModel.PROPERTY_VALUE.equals(event.getPropertyName())) {
				m_canvas.getViewport().getVerticalRangeModel().setValue((int) event.getNewValue());
				getRootFigureInternal().repaint();
			}
		});
	}
}