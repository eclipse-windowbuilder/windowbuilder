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
package org.eclipse.wb.internal.core.gef.policy.snapping;

import org.eclipse.wb.core.model.IAbstractComponentInfo;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Interval;
import org.eclipse.draw2d.geometry.Rectangle;

import java.util.List;

/**
 * Indented snap point to snap component under other component with indent
 *
 * +-----------------+ | other component | +-----------------+ | +----------------+ | our component
 * | +----------------+
 *
 * @author mitin_aa
 * @coverage core.gef.policy.snapping
 */
public class IndentedComponentSnapPoint extends ComponentSnapPoint {
	public static final int INDENT = 10;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public IndentedComponentSnapPoint(IVisualDataProvider visualDataProvider,
			IAbstractComponentInfo anchorComponent) {
		super(visualDataProvider, anchorComponent, PositionConstants.LEFT, PlacementInfo.LEADING);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Update snapping data
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void calculateSnapPoint(List<? extends IAbstractComponentInfo> beingSnappedList) {
		super.calculateSnapPoint(beingSnappedList);
		m_gap =
				m_visualDataProvider.getComponentGapValue(
						getNearestComponentToSide(beingSnappedList),
						m_anchorComponent,
						PositionConstants.BOTTOM);
		//
		m_snapPoint = m_anchorChildBounds.x + INDENT;
	}

	@Override
	protected Interval getXSnapInterval() {
		return new Interval(m_anchorChildBounds.x, m_anchorChildBounds.width);
	}

	@Override
	protected Interval getYSnapInterval() {
		return new Interval(m_anchorChildBounds.bottom(), m_gap * 2);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Feedbacks
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void addFeedback(Rectangle snappedBounds,
			IFeedbackProxy feedbackProxy,
			List<IFigure> feedbacks) {
		final Rectangle anchorChildBounds =
				PlacementUtils.getTranslatedBounds(m_visualDataProvider, m_anchorComponent);
		final Rectangle expCandidateBounds = anchorChildBounds.getCopy().expand(0, 8);
		feedbacks.add(feedbackProxy.addVerticalFeedbackLine(
				expCandidateBounds.x,
				expCandidateBounds.y,
				expCandidateBounds.height));
		final Rectangle expBounds = snappedBounds.getCopy().expand(0, 8);
		feedbacks.add(feedbackProxy.addVerticalFeedbackLine(
				snappedBounds.x,
				expBounds.y,
				expBounds.height));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Misc
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected boolean checkDirection() {
		return false;
	}
}