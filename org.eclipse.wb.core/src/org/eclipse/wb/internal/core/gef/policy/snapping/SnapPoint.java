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
import org.eclipse.wb.draw2d.geometry.Transposer;
import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;

import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * This class used to provide snap point (horizontal or vertical).
 *
 * @author mitin_aa
 * @coverage core.gef.policy.snapping
 */
public abstract class SnapPoint {
  public static final int SNAP_DISTANCE = 10;
  //
  private Interval m_xSnapInterval;
  private Interval m_ySnapInterval;
  //
  private final int m_snapDirection;
  protected int m_snapDistance = SNAP_DISTANCE;
  protected int m_snapPoint;
  protected Interval m_snapInterval;
  protected final int m_side;
  protected IAbstractComponentInfo m_nearestBeingSnapped;
  //
  protected IVisualDataProvider m_visualDataProvider;
  protected boolean m_isResizing;
  //
  private SnapPointCommand m_command;
  private List<? extends IAbstractComponentInfo> m_workingSet;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @param visualDataProvider
   * @param snapDirection
   *          A direction in which snap point would be applied, e.g. if direction is
   *          {@link PlacementInfo#TRAILING} then snapping rectangle's right side would be set. Also
   *          this parameter taken into account when checking for moving direction.
   */
  public SnapPoint(IVisualDataProvider visualDataProvider, int side, int snapDirection) {
    m_visualDataProvider = visualDataProvider;
    m_snapDirection = snapDirection;
    m_side = side;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Snapping
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @param beingSnappedList
   *          A component models list which is about to snap.
   * @param beingSnappedBounds
   *          A bounds we want to snap, would be modified. For multiple components snapping. it is a
   *          bounding rectangle of all components bounds.
   * @param moveDirection
   *          A mouse moving direction.
   * @param resizeDirection
   *          Resize direction as it passed from {@link ChangeBoundsRequest}.
   * @return <code>true</code> if snapping occurred.
   */
  public boolean snap(List<? extends IAbstractComponentInfo> beingSnappedList,
      Rectangle beingSnappedBounds,
      int moveDirection,
      int resizeDirection) {
    m_isResizing = resizeDirection != 0;
    if (!snapAllowed(beingSnappedList)) {
      return false;
    }
    // make snapping data up-to-date
    updateSnapData(beingSnappedList);
    // check moving direction
    if (checkDirection() && !validDirection(moveDirection)) {
      return false;
    }
    // check whether we able to snap at this area
    Interval xSnapInterval = new Interval(beingSnappedBounds.x, beingSnappedBounds.width);
    Interval ySnapInterval = new Interval(beingSnappedBounds.y, beingSnappedBounds.height);
    if (!m_xSnapInterval.intersects(xSnapInterval) || !m_ySnapInterval.intersects(ySnapInterval)) {
      return false;
    }
    // do snapping
    Transposer t = new Transposer();
    t.setEnabled(!isHorizontal());
    Rectangle transposedBounds = t.t(beingSnappedBounds);
    int beingSnappedPoint =
        m_snapDirection == PlacementInfo.LEADING ? transposedBounds.x : transposedBounds.right();
    if (m_isResizing) {
      if (isValidResizeDirection(resizeDirection)) {
        if (m_snapInterval.contains(beingSnappedPoint)) {
          if (m_snapDirection == PlacementInfo.LEADING) {
            transposedBounds.width += transposedBounds.x - m_snapPoint;
            transposedBounds.x = m_snapPoint;
          } else {
            transposedBounds.width = m_snapPoint - transposedBounds.x;
          }
          beingSnappedBounds.setBounds(t.t(transposedBounds));
          return true;
        }
      }
    } else {
      if (m_snapInterval.contains(beingSnappedPoint)) {
        if (m_snapDirection == PlacementInfo.LEADING) {
          transposedBounds.x = m_snapPoint;
        } else {
          transposedBounds.x = m_snapPoint - transposedBounds.width;
        }
        beingSnappedBounds.setBounds(t.t(transposedBounds));
        return true;
      }
    }
    return false;
  }

  /**
   * Determines if resize direction is suitable for snapping.
   *
   * @param resizeDirection
   *          .
   */
  protected boolean isValidResizeDirection(int resizeDirection) {
    if (isHorizontal()) {
      return m_snapDirection == PlacementInfo.LEADING
          && (resizeDirection & IPositionConstants.WEST) != 0
          || m_snapDirection == PlacementInfo.TRAILING
          && (resizeDirection & IPositionConstants.EAST) != 0;
    } else {
      return m_snapDirection == PlacementInfo.LEADING
          && (resizeDirection & IPositionConstants.NORTH) != 0
          || m_snapDirection == PlacementInfo.TRAILING
          && (resizeDirection & IPositionConstants.SOUTH) != 0;
    }
  }

  /**
   * Shows a feedback for this point. Called when snapping for this point is occurred.
   *
   * @param snappedBounds
   *          the {@link Rectangle} of snapped bounds to make feedback drawing hint.
   * @param feedbackProxy
   *          a instance of object responsible to create visual feedbacks.
   * @param feedbacks
   *          a {@link List} of added feedbacks. Descendants should add every created feedback to
   *          this list for further remove.
   */
  public void addFeedback(Rectangle snappedBounds,
      IFeedbackProxy feedbackProxy,
      List<Figure> feedbacks) {
  }

  /**
   * Determines should we pay attention on mouse moving direction or not.
   *
   * @return true if mouse moving direction should be used otherwise return false.
   */
  protected boolean checkDirection() {
    return true;
  }

  /**
   * Is current mouse move direction is valid for this snap point. When mouse move direction is not
   * valid then no snapping is occurred.
   *
   * @param direction
   *          A mouse move direction as it passed from <code>snap()</code>.
   */
  protected boolean validDirection(int direction) {
    return m_snapDirection == direction;
  }

  /**
   * During process of snapping some snap points may disallow snapping for some reasons, for example
   * component snapping should not allow to snap to itself
   *
   * @param beingSnappedList
   *          A {@link List} of components' model being snapped.
   * @return <code>true</code> if snapping to this point is allowed.
   */
  protected boolean snapAllowed(List<? extends IAbstractComponentInfo> beingSnappedList) {
    return true;
  }

  /**
   * @return true if this snap point is for horizontal dimension.
   */
  protected boolean isHorizontal() {
    return m_side == IPositionConstants.LEFT || m_side == IPositionConstants.RIGHT;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Update
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Makes snap interval (interval where snapping is valid) up to date. Updating is needed because
   * non-static component list is used, so its need to calculate actual snap point position (and
   * snapping intervals) every time.
   */
  private void updateSnapData(List<? extends IAbstractComponentInfo> beingSnappedList) {
    m_workingSet = beingSnappedList;
    calculateSnapPoint(beingSnappedList);
    m_xSnapInterval = getXSnapInterval();
    m_ySnapInterval = getYSnapInterval();
    m_snapInterval = new Interval(m_snapPoint - m_snapDistance, m_snapDistance * 2);
  }

  /**
   * Calculate snap point position and setup additional required info.
   *
   * @param beingSnappedList
   *          .
   */
  protected void calculateSnapPoint(List<? extends IAbstractComponentInfo> beingSnappedList) {
    m_nearestBeingSnapped = getNearestComponentToSide(beingSnappedList);
  }

  /**
   * @return A horizontal interval in which this snap point would work, e.g. if snapping rectangle
   *         is not intersects this interval then no snapping would be occurred.
   */
  protected Interval getXSnapInterval() {
    return Interval.INFINITE;
  }

  /**
   * @return A vertical interval in which this snap point would work.
   */
  protected Interval getYSnapInterval() {
    return Interval.INFINITE;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  protected IAbstractComponentInfo getNearestComponentToSide(List<? extends IAbstractComponentInfo> beingSnappedList) {
    return getNearestComponentToSide(beingSnappedList, m_side, isHorizontal(), m_visualDataProvider);
  }

  public static IAbstractComponentInfo getNearestComponentToSide(List<? extends IAbstractComponentInfo> beingSnappedList,
      int side,
      boolean isHorizontal,
      IVisualDataProvider provider) {
    if (beingSnappedList.size() == 1) {
      return beingSnappedList.get(0);
    }
    boolean isLeading = PlacementUtils.isLeadingSide(side);
    Transposer t = new Transposer();
    t.setEnabled(!isHorizontal);
    int offset = isLeading ? Integer.MAX_VALUE : Integer.MIN_VALUE;
    IAbstractComponentInfo result = null;
    for (IAbstractComponentInfo component : beingSnappedList) {
      Rectangle bounds = t.t(PlacementUtils.getTranslatedBounds(provider, component));
      int newOffset = isLeading ? Math.min(offset, bounds.x) : Math.max(offset, bounds.right());
      if (newOffset == (isLeading ? bounds.x : bounds.right())) {
        result = component;
        offset = newOffset;
      }
    }
    return result;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    String name = StringUtils.substringAfterLast(getClass().getName(), ".");
    return name
        + "(side="
        + sideToString(m_side)
        + " snapDir="
        + (m_snapDirection == PlacementInfo.LEADING ? "LEADING" : "TRAILING")
        + getObjectInfo()
        + ")";
  }

  private static String sideToString(int side) {
    switch (side) {
      case IPositionConstants.TOP :
        return "TOP";
      case IPositionConstants.BOTTOM :
        return "BOTTOM";
      case IPositionConstants.LEFT :
        return "LEFT";
      case IPositionConstants.RIGHT :
        return "RIGHT";
      default :
        return Integer.toString(side);
    }
  }

  protected String getObjectInfo() {
    return "";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the current value of this {@link SnapPoint}.
   */
  public final int getValue() {
    return m_snapPoint;
  }

  /**
   * @return the list of components which are dragged.
   */
  public final List<? extends IAbstractComponentInfo> getWorkingSet() {
    return m_workingSet;
  }

  /**
   * @return the component which actually used for snapping.
   */
  public final IAbstractComponentInfo getNearestBeingSnapped() {
    return m_nearestBeingSnapped;
  }

  /**
   * @return the side of this {@link SnapPoint}. The possible values are
   *         {@link IPositionConstants#TOP}, {@link IPositionConstants#BOTTOM},
   *         {@link IPositionConstants#LEFT}, {@link IPositionConstants#RIGHT}.
   */
  public final int getSide() {
    return m_side;
  }

  public final int getSnapDirection() {
    return m_snapDirection;
  }

  public final Command getCommand() {
    return m_command;
  }

  public final void setCommand(SnapPointCommand command) {
    m_command = command;
  }
}