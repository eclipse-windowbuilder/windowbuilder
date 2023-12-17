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
package org.eclipse.wb.core.gef.policy.selection;

import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.draw2d.border.LineBorder;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.graphical.handles.Handle;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;

import org.eclipse.draw2d.AncestorListener;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;

import java.util.Collections;
import java.util.List;

/**
 * {@link SelectionEditPolicy} that has no {@link Handle}'s, just shows line around {@link EditPart}
 * .
 *
 * @author mitin_aa
 * @author scheglov_ke
 * @coverage core.gef.menu
 */
public class LineSelectionEditPolicy extends SelectionEditPolicy {
	private final Color m_lineColor;
	private Figure m_selectionFigure;
	private AncestorListener m_ancestorListener;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public LineSelectionEditPolicy() {
		this(ColorConstants.orange);
	}

	public LineSelectionEditPolicy(Color lineColor) {
		m_lineColor = lineColor;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Handles
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected List<Handle> createSelectionHandles() {
		return Collections.emptyList();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Selection
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void showSelection() {
		hideSelection();
		m_selectionFigure = new Figure();
		m_selectionFigure.setBorder(new LineBorder(m_lineColor));
		updateFeedbackBounds();
		m_ancestorListener = new AncestorListener.Stub() {
			@Override
			public void ancestorMoved(IFigure ancestor) {
				updateFeedbackBounds();
			}
		};
		getHostFigure().addAncestorListener(m_ancestorListener);
		addFeedback(m_selectionFigure);
	}

	@Override
	protected void hideSelection() {
		if (m_selectionFigure != null) {
			getHostFigure().removeAncestorListener(m_ancestorListener);
			removeFeedback(m_selectionFigure);
			m_selectionFigure = null;
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Feedbacks
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Updates feedback figure bounds in ancestor listener.
	 */
	private void updateFeedbackBounds() {
		Rectangle selectionBounds = getHostBounds().getExpanded(1, 1);
		FigureUtils.translateFigureToFigure(getHostFigure(), m_selectionFigure, selectionBounds);
		m_selectionFigure.setBounds(selectionBounds);
	}

	/**
	 * @return the bounds of {@link Figure} of host {@link EditPart}, in "bounds" coordinates.
	 */
	protected Rectangle getHostBounds() {
		return getHostFigure().getBounds();
	}
}
