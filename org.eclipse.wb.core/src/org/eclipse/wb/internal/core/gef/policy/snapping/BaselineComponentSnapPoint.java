/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.gef.policy.snapping;

import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.IPositionConstants;
import org.eclipse.wb.draw2d.geometry.Interval;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.laf.IBaselineSupport;

import java.util.List;

/**
 * Supports snapping to baseline point (if any).
 *
 * @author mitin_aa
 * @coverage core.gef.policy.snapping
 */
public final class BaselineComponentSnapPoint extends ComponentSnapPoint {
  private int m_baseline;
  private int m_anchorBaseline;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public BaselineComponentSnapPoint(final IVisualDataProvider visualDataProvider,
      final IAbstractComponentInfo anchorComponent) {
    super(visualDataProvider, anchorComponent, IPositionConstants.TOP, PlacementInfo.LEADING);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Snap data update
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void calculateSnapPoint(final List<? extends IAbstractComponentInfo> beingSnappedList) {
    m_anchorChildBounds =
        PlacementUtils.getTranslatedBounds(m_visualDataProvider, m_anchorComponent);
    m_snapPoint = m_anchorChildBounds.y + m_anchorBaseline - m_baseline;
  }

  @Override
  protected Interval getYSnapInterval() {
    return new Interval(m_anchorChildBounds.y, m_anchorChildBounds.height);
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

  @Override
  protected boolean snapAllowed(List<? extends IAbstractComponentInfo> beingSnappedList) {
    if (super.snapAllowed(beingSnappedList) && !m_isResizing) {
      // don't snap if multiple components dragged
      if (beingSnappedList.size() > 1) {
        return false;
      }
      IAbstractComponentInfo beingSnapped = beingSnappedList.get(0);
      m_baseline = m_visualDataProvider.getBaseline(beingSnapped);
      // don't allow snap to this point for components that doesn't have baseline value
      if (m_baseline == IBaselineSupport.NO_BASELINE) {
        return false;
      }
      m_anchorBaseline = m_visualDataProvider.getBaseline(m_anchorComponent);
      if (m_anchorBaseline == IBaselineSupport.NO_BASELINE) {
        return false;
      }
      return true;
    }
    return false;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Feedback
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void addFeedback(final Rectangle snappedBounds,
      IFeedbackProxy feedbackProxy,
      final List<Figure> feedbacks) {
    final Rectangle anchorChildBounds =
        PlacementUtils.getTranslatedBounds(m_visualDataProvider, m_anchorComponent);
    final Rectangle unionBounds = snappedBounds.getUnion(anchorChildBounds);
    unionBounds.expand(8, 0);
    feedbacks.add(feedbackProxy.addHorizontalFeedbackLine(
        snappedBounds.y + m_baseline,
        unionBounds.x,
        unionBounds.width));
  }
}