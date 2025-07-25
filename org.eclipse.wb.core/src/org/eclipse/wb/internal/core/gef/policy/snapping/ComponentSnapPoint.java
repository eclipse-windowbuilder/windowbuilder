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
 * Snap point allowing to snap component to another component.
 *
 * @author mitin_aa
 * @coverage core.gef.policy.snapping
 */
public class ComponentSnapPoint extends SnapPoint {
	protected final IAbstractComponentInfo m_anchorComponent;
	protected final boolean m_includeGap;
	protected int m_gap;
	//
	protected Rectangle m_anchorChildBounds;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	////////////////////////////////////////////////////////////////////////////
	public ComponentSnapPoint(final IVisualDataProvider visualDataProvider,
			final IAbstractComponentInfo anchorComponent,
			final int side,
			final int snapDirection) {
		this(visualDataProvider, anchorComponent, side, snapDirection, false);
	}

	public ComponentSnapPoint(final IVisualDataProvider visualDataProvider,
			final IAbstractComponentInfo anchorComponent,
			final int side,
			final int snapDirection,
			final boolean includeGap) {
		super(visualDataProvider, side, snapDirection);
		m_anchorComponent = anchorComponent;
		m_includeGap = includeGap;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Snap point data update
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void calculateSnapPoint(List<? extends IAbstractComponentInfo> beingSnappedList) {
		super.calculateSnapPoint(beingSnappedList);
		m_anchorChildBounds =
				PlacementUtils.getTranslatedBounds(m_visualDataProvider, m_anchorComponent);
		m_gap = 0;
		switch (m_side) {
		case PositionConstants.LEFT :
			if (m_includeGap) {
				m_gap =
						m_visualDataProvider.getComponentGapValue(
								m_nearestBeingSnapped,
								m_anchorComponent,
								PositionConstants.LEFT);
			}
			m_snapPoint = m_anchorChildBounds.x - m_gap;
			break;
		case PositionConstants.RIGHT :
			if (m_includeGap) {
				m_gap =
						m_visualDataProvider.getComponentGapValue(
								m_nearestBeingSnapped,
								m_anchorComponent,
								PositionConstants.RIGHT);
			}
			m_snapPoint = m_anchorChildBounds.right() + m_gap;
			break;
		case PositionConstants.TOP :
			if (m_includeGap) {
				m_gap =
						m_visualDataProvider.getComponentGapValue(
								m_nearestBeingSnapped,
								m_anchorComponent,
								PositionConstants.TOP);
			}
			m_snapPoint = m_anchorChildBounds.y - m_gap;
			break;
		case PositionConstants.BOTTOM :
			if (m_includeGap) {
				m_gap =
						m_visualDataProvider.getComponentGapValue(
								m_nearestBeingSnapped,
								m_anchorComponent,
								PositionConstants.BOTTOM);
			}
			m_snapPoint =
					m_includeGap ? m_anchorChildBounds.bottom() + m_gap : m_anchorChildBounds.bottom();
			break;
		default :
			throw new IllegalArgumentException("Invalid side: " + m_side);
		}
	}

	@Override
	protected Interval getXSnapInterval() {
		if (m_includeGap && !isHorizontal()) {
			return new Interval(m_anchorChildBounds.x, m_anchorChildBounds.width);
		}
		return super.getXSnapInterval();
	}

	@Override
	protected Interval getYSnapInterval() {
		if (m_includeGap && isHorizontal()) {
			return new Interval(m_anchorChildBounds.y, m_anchorChildBounds.height);
		}
		return super.getYSnapInterval();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Feedbacks
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void addFeedback(final Rectangle snappedBounds,
			IFeedbackProxy feedbackProxy,
			final List<IFigure> feedbacks) {
		if (isHorizontal()) {
			feedbacks.add(addVerticalFeedback(m_snapPoint, feedbackProxy, snappedBounds));
		} else {
			feedbacks.add(addHorizontalFeedback(m_snapPoint, feedbackProxy, snappedBounds));
		}
	}

	protected IFigure addHorizontalFeedback(final int y,
			IFeedbackProxy feedbackProxy,
			final Rectangle snappedBounds) {
		final Rectangle anchorChildBounds =
				PlacementUtils.getTranslatedBounds(m_visualDataProvider, m_anchorComponent);
		final Rectangle unionBounds = snappedBounds.getUnion(anchorChildBounds);
		unionBounds.expand(8, 0);
		return feedbackProxy.addHorizontalFeedbackLine(y, unionBounds.x, unionBounds.width);
	}

	protected IFigure addVerticalFeedback(final int x,
			IFeedbackProxy feedbackProxy,
			final Rectangle snappedBounds) {
		final Rectangle anchorChildBounds =
				PlacementUtils.getTranslatedBounds(m_visualDataProvider, m_anchorComponent);
		final Rectangle unionBounds = snappedBounds.getUnion(anchorChildBounds);
		unionBounds.expand(0, 8);
		return feedbackProxy.addVerticalFeedbackLine(x, unionBounds.y, unionBounds.height);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Misc
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected boolean snapAllowed(List<? extends IAbstractComponentInfo> beingSnappedList) {
		return !beingSnappedList.contains(m_anchorComponent);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Object
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected String getObjectInfo() {
		return " component=" + m_anchorComponent + " gap=" + m_includeGap;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link IAbstractComponentInfo} instance for which this {@link SnapPoint} would snap
	 *         to ("anchor" component).
	 */
	public final IAbstractComponentInfo getComponent() {
		return m_anchorComponent;
	}

	/**
	 * @return the gap value between "anchor" component and nearest component from being snapped
	 *         components list.
	 */
	public final int getGap() {
		return m_gap;
	}
}