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
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;

import java.util.List;

/**
 * Snap point suitable to snap component to parent container's side.
 *
 * @author mitin_aa
 * @coverage core.gef.policy.snapping
 */
public class ContainerSnapPoint extends SnapPoint {
	protected Dimension m_containerSize;
	private final boolean m_includeGap;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	////////////////////////////////////////////////////////////////////////////
	public ContainerSnapPoint(IVisualDataProvider visualDataProvider, int side) {
		this(visualDataProvider, side, false);
	}

	public ContainerSnapPoint(IVisualDataProvider visualDataProvider, int side, boolean includeGap) {
		super(visualDataProvider, side, side == PositionConstants.LEFT
				|| side == PositionConstants.TOP ? PlacementInfo.LEADING : PlacementInfo.TRAILING);
		m_includeGap = includeGap;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Snap point data update
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void calculateSnapPoint(final List<? extends IAbstractComponentInfo> beingSnappedList) {
		super.calculateSnapPoint(beingSnappedList);
		m_containerSize = m_visualDataProvider.getContainerSize();
		int gapValue = m_visualDataProvider.getContainerGapValue(m_nearestBeingSnapped, m_side);
		switch (m_side) {
		case PositionConstants.TOP :
		case PositionConstants.LEFT :
			m_snapPoint = m_includeGap ? gapValue : 0;
			break;
		case PositionConstants.BOTTOM :
			m_snapPoint = m_includeGap ? m_containerSize.height - gapValue : m_containerSize.height;
			break;
		case PositionConstants.RIGHT :
			m_snapPoint = m_includeGap ? m_containerSize.width - gapValue : m_containerSize.width;
			break;
		default :
			break;
		}
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
		if (isHorizontal()) {
			feedbacks.add(feedbackProxy.addVerticalFeedbackLine(m_snapPoint, 0, m_containerSize.height));
		} else {
			feedbacks.add(feedbackProxy.addHorizontalFeedbackLine(m_snapPoint, 0, m_containerSize.width));
		}
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

	////////////////////////////////////////////////////////////////////////////
	//
	// Object
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected String getObjectInfo() {
		return " gap=" + m_includeGap;
	}
}