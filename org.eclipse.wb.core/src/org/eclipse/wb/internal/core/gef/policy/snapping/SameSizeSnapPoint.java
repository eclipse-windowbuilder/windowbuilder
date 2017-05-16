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
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Interval;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.draw2d.geometry.Transposer;

import java.util.List;

/**
 * Snapping size of component to same size as any of component on the same parent.
 *
 * @author mitin_aa
 * @coverage core.gef.policy.snapping
 */
public class SameSizeSnapPoint extends SnapPoint {
  private final List<? extends IAbstractComponentInfo> m_allComponents;
  private IAbstractComponentInfo m_sameSizedComponent;

  //
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SameSizeSnapPoint(IVisualDataProvider visualDataProvider,
      List<? extends IAbstractComponentInfo> components,
      int side) {
    super(visualDataProvider, side, side == IPositionConstants.LEFT
        || side == IPositionConstants.TOP ? PlacementInfo.LEADING : PlacementInfo.TRAILING);
    m_allComponents = components;
    m_snapDistance = 5;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Snapping
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean snap(List<? extends IAbstractComponentInfo> beingSnappedList,
      Rectangle beingSnappedBounds,
      int moveDirection,
      int resizeDirection) {
    // don't snap if not is in resizing state or processing multiple components
    if (resizeDirection == 0 || beingSnappedList.size() != 1) {
      return false;
    }
    // prepare
    Transposer t = new Transposer();
    t.setEnabled(!isHorizontal());
    Rectangle transposedBounds = t.t(beingSnappedBounds);
    m_sameSizedComponent = null;
    IAbstractComponentInfo beingSnapped = beingSnappedList.get(0);
    // check for resize direction to be valid to this snap point
    if (isValidResizeDirection(resizeDirection)) {
      // lookup all components for equal size
      for (IAbstractComponentInfo component : m_allComponents) {
        if (component == beingSnapped) {
          // do not snap to itself
          continue;
        }
        Rectangle modelBounds =
            t.t(PlacementUtils.getTranslatedBounds(m_visualDataProvider, component));
        Dimension size = modelBounds.getSize();
        if (doSnapping(transposedBounds, size)) {
          m_sameSizedComponent = component;
          beingSnappedBounds.setBounds(t.t(transposedBounds));
          return true;
        }
      }
      // snap to preferred size
      Dimension preferredSize = m_visualDataProvider.getComponentPreferredSize(beingSnapped);
      if (preferredSize == null) {
        return false;
      }
      if (doSnapping(transposedBounds, t.t(preferredSize))) {
        beingSnappedBounds.setBounds(t.t(transposedBounds));
        return true;
      }
    }
    return false;
  }

  private boolean doSnapping(Rectangle transposedBounds, Dimension size) {
    int beingSnappedPoint =
        getSnapDirection() == PlacementInfo.LEADING ? transposedBounds.x : transposedBounds.right();
    if (m_side == IPositionConstants.LEFT || m_side == IPositionConstants.TOP) {
      m_snapPoint = transposedBounds.right() - size.width;
    } else {
      m_snapPoint = transposedBounds.x + size.width;
    }
    m_snapInterval = new Interval(m_snapPoint - m_snapDistance, m_snapDistance * 2);
    if (m_snapInterval.contains(beingSnappedPoint)) {
      if (getSnapDirection() == PlacementInfo.LEADING) {
        transposedBounds.width += transposedBounds.x - m_snapPoint;
        transposedBounds.x = m_snapPoint;
      } else {
        transposedBounds.width = m_snapPoint - transposedBounds.x;
      }
      return true;
    }
    return false;
  }

  @Override
  protected void calculateSnapPoint(List<? extends IAbstractComponentInfo> beingSnappedList) {
    // actually there is no snap point position needed
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Feedbacks
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void addFeedback(Rectangle snappedBounds,
      IFeedbackProxy feedbackProxy,
      List<Figure> feedbacks) {
    // snapped to some component
    if (m_sameSizedComponent != null) {
      Rectangle bounds =
          PlacementUtils.getTranslatedBounds(m_visualDataProvider, m_sameSizedComponent);
      feedbacks.add(feedbackProxy.addOutlineFeedback(bounds));
      addMiddleLineFeedback(bounds, feedbackProxy, feedbacks);
    }
    // draw preferred size feedback or same-sized feedback
    addMiddleLineFeedback(snappedBounds, feedbackProxy, feedbacks);
  }

  private void addMiddleLineFeedback(Rectangle bounds,
      IFeedbackProxy feedbackProxy,
      List<Figure> feedbacks) {
    Figure figure =
        isHorizontal() ? feedbackProxy.addHorizontalMiddleLineFeedback(
            bounds.y + bounds.height / 2,
            bounds.x,
            bounds.width) : feedbackProxy.addVerticalMiddleLineFeedback(
            bounds.x + bounds.width / 2,
            bounds.y,
            bounds.height);
    feedbacks.add(figure);
  }
}