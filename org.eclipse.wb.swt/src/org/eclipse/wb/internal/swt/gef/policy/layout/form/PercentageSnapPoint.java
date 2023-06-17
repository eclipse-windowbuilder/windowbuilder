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
package org.eclipse.wb.internal.swt.gef.policy.layout.form;

import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.IPositionConstants;
import org.eclipse.wb.internal.core.gef.policy.snapping.IFeedbackProxy;
import org.eclipse.wb.internal.core.gef.policy.snapping.PlacementInfo;
import org.eclipse.wb.internal.core.gef.policy.snapping.PlacementUtils;
import org.eclipse.wb.internal.core.gef.policy.snapping.SnapPoint;
import org.eclipse.wb.internal.swt.model.widgets.IControlInfo;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.draw2d.geometry.Transposer;

import java.util.List;

/**
 * A snap point allowing to snap to a percentage value as in FormLayout.
 *
 * @author mitin_aa
 */
final class PercentageSnapPoint<C extends IControlInfo> extends SnapPoint {
  private final int m_percent;
  private Dimension m_containerSize;
  private final boolean m_hasGap;
  private final FormLayoutVisualDataProvider<C> m_visualDataProvider;
  private int m_gap;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public PercentageSnapPoint(FormLayoutVisualDataProvider<C> visualDataProvider,
      int side,
      int percent,
      boolean hasGap) {
    super(visualDataProvider, side, side == IPositionConstants.LEFT
        || side == IPositionConstants.TOP ? PlacementInfo.LEADING : PlacementInfo.TRAILING);
    m_visualDataProvider = visualDataProvider;
    m_percent = percent;
    m_hasGap = hasGap;
  }

  public PercentageSnapPoint(FormLayoutVisualDataProvider<C> visualDataProvider,
      int side,
      int percent) {
    this(visualDataProvider, side, percent, false);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Snapping
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void calculateSnapPoint(List<? extends IAbstractComponentInfo> beingSnappedList) {
    super.calculateSnapPoint(beingSnappedList);
    m_containerSize = m_visualDataProvider.getContainerSize();
    Transposer t = new Transposer(!isHorizontal());
    m_gap = 0;
    if (m_hasGap) {
      m_gap = m_visualDataProvider.getPercentsGap(isHorizontal());
    }
    m_snapPoint = t.t(m_containerSize).width * m_percent / 100 - getSign() * m_gap;
  }

  private int getSign() {
    return PlacementUtils.isLeadingSide(m_side) ? -1 : 1;
  }

  @Override
  public void addFeedback(Rectangle snappedBounds,
      IFeedbackProxy feedbackProxy,
      List<Figure> feedbacks) {
    if (isHorizontal()) {
      if (m_hasGap) {
        feedbacks.add(feedbackProxy.addVerticalFeedbackLine(
            m_snapPoint + getSign() * m_gap,
            0,
            m_containerSize.height));
      }
      feedbacks.add(feedbackProxy.addVerticalFeedbackLine(m_snapPoint, 0, m_containerSize.height));
    } else {
      if (m_hasGap) {
        feedbacks.add(feedbackProxy.addHorizontalFeedbackLine(
            m_snapPoint + getSign() * m_gap,
            0,
            m_containerSize.width));
      }
      feedbacks.add(feedbackProxy.addHorizontalFeedbackLine(m_snapPoint, 0, m_containerSize.width));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  //	Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public int getPercent() {
    return m_percent;
  }

  public int getGap() {
    return m_gap;
  }
}