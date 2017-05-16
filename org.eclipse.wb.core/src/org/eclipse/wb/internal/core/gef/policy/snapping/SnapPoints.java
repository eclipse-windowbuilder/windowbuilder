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

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.draw2d.IPositionConstants;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.draw2d.geometry.Transposer;

import java.util.List;

/**
 * This class provides bounds snapping and drawing feedbacks based on defined SnapPoint list. See
 * also {@link IVisualDataProvider}.
 *
 * @author mitin_aa
 * @coverage core.gef.policy.snapping
 */
public class SnapPoints {
  protected final IVisualDataProvider m_visualDataProvider;
  protected final List<IAbstractComponentInfo> m_allWidgets;
  //
  protected Point m_lastMouseLocation;
  protected int m_horizontalMouseMoveDirection;
  protected int m_verticalMouseMoveDirection;
  // feedbacks
  protected List<Figure> m_feedbacks = Lists.newArrayList();
  private final IFeedbackProxy m_feedbackProxy;
  private SnapPoint m_horizontalSnappedPoint;
  private SnapPoint m_verticalSnappedPoint;
  private final ISnapPointsProvider m_snapPoints;
  private final ISnapPointsListener m_listener;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @param listener
   * @param allWidgets
   *          A list of all components in host container.
   */
  public SnapPoints(IVisualDataProvider visualDataProvider,
      IFeedbackProxy feedbackProxy,
      ISnapPointsProvider snapPoints,
      ISnapPointsListener listener,
      List<? extends IAbstractComponentInfo> allWidgets) {
    m_visualDataProvider = visualDataProvider;
    m_feedbackProxy = feedbackProxy;
    m_snapPoints = snapPoints;
    m_listener = listener;
    m_allWidgets = Lists.newArrayList(allWidgets);
  }

  /**
   * @param allWidgets
   *          A list of all components in host container.
   */
  public SnapPoints(IVisualDataProvider visualDataProvider,
      IFeedbackProxy feedbackProxy,
      List<? extends IAbstractComponentInfo> allWidgets) {
    this(visualDataProvider,
        feedbackProxy,
        new DefaultSnapPoints(visualDataProvider, allWidgets),
        null,
        allWidgets);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Snapping magic
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Main snapping magic. This method processes added (by
   * {@link SnapPoints#createHorizontalPoints(List)} and by other similar methods) snap points and
   * modifies <code>snappedBounds</code> according to snap point which this bounds moves/resizes
   * around. If no snap point engaged then grid snapping applied to <code>snappedBounds</code>. Any
   * snapping may be disabled by {@link IVisualDataProvider#isSuppressingSnapping()}.
   *
   * @param location
   *          Current mouse pointer location. Used to calc mouse move direction.
   * @param beingSnappedList
   *          A component model list that we want to be snapped.
   * @param resizeDirection
   *          Resize direction as it passed from ChangeBoundsRequest. Zero means that is no resizing
   *          in progress.
   */
  public void processBounds(Point location,
      List<? extends IAbstractComponentInfo> beingSnappedList,
      Rectangle snappedBounds,
      int resizeDirection) {
    //
    calculateMouseMoveDirections(location);
    removeFeedbacks();
    m_horizontalSnappedPoint = null;
    m_verticalSnappedPoint = null;
    // don't do any snapping if disabled
    if (!m_visualDataProvider.isSuppressingSnapping()) {
      if (m_visualDataProvider.useFreeSnapping()) {
        // iterate thru vertical points to find one is snapped
        for (SnapPoint snapPoint : getSnapPoints(false)) {
          if (snapPoint.snap(
              beingSnappedList,
              snappedBounds,
              m_verticalMouseMoveDirection,
              resizeDirection)) {
            m_verticalSnappedPoint = snapPoint;
            break;
          }
        }
        // iterate thru horizontal points to find one is snapped
        for (SnapPoint snapPoint : getSnapPoints(true)) {
          if (snapPoint.snap(
              beingSnappedList,
              snappedBounds,
              m_horizontalMouseMoveDirection,
              resizeDirection)) {
            m_horizontalSnappedPoint = snapPoint;
            break;
          }
        }
      }
      // if not snapped on this axis then apply grid snapping if enabled by visual data provider
      if (m_verticalSnappedPoint == null) {
        // apply grid. don't apply grid while resizing to every axis
        if (resizeDirection == 0
            || (resizeDirection & IPositionConstants.WEST) != 0
            || (resizeDirection & IPositionConstants.EAST) != 0) {
          applyGrid(snappedBounds, resizeDirection, true);
        }
      }
      if (m_horizontalSnappedPoint == null) {
        // apply grid. don't apply grid while resizing to every axis
        if (resizeDirection == 0
            || (resizeDirection & IPositionConstants.NORTH) != 0
            || (resizeDirection & IPositionConstants.SOUTH) != 0) {
          applyGrid(snappedBounds, resizeDirection, false);
        }
      }
      // TODO: remove feedbacks drawing from here, leave just preparations
      // draw feedback for snapped point (if any). do this after all possible
      // snapping done to avoid improper annoying line feedback drawing
      if (m_verticalSnappedPoint != null) {
        m_verticalSnappedPoint.addFeedback(snappedBounds, m_feedbackProxy, m_feedbacks);
      }
      if (m_horizontalSnappedPoint != null) {
        m_horizontalSnappedPoint.addFeedback(snappedBounds, m_feedbackProxy, m_feedbacks);
      }
    }
    if (m_listener != null) {
      m_listener.boundsChanged(snappedBounds, beingSnappedList, new SnapPoint[]{
          m_horizontalSnappedPoint,
          m_verticalSnappedPoint}, new int[]{
          m_horizontalMouseMoveDirection,
          m_verticalMouseMoveDirection});
    }
  }

  void processBounds(PlacementsSupport placements,
      Point location,
      List<? extends IAbstractComponentInfo> beingSnappedList,
      int resizeDirection) {
    Rectangle snappedBounds = placements.getInternalBounds();
    processBounds(location, beingSnappedList, snappedBounds, resizeDirection);
    // do drag
    placements.doDrag(
        new int[]{m_horizontalMouseMoveDirection, m_verticalMouseMoveDirection},
        new SnapPoint[]{m_horizontalSnappedPoint, m_verticalSnappedPoint});
  }

  /**
   * Common method for grid snapping. Does some transposing and passes transposed values into grid
   * snapping main method.
   */
  private void applyGrid(Rectangle snappedBounds, int resizeDirection, boolean isHorizontal) {
    if (!m_visualDataProvider.useGridSnapping()) {
      return;
    }
    Transposer t = new Transposer(!isHorizontal);
    Rectangle transposed = t.t(snappedBounds);
    applyGrid(transposed, resizeDirection, isHorizontal
        ? m_horizontalMouseMoveDirection
        : m_verticalMouseMoveDirection, isHorizontal
        ? m_visualDataProvider.getGridStepX()
        : m_visualDataProvider.getGridStepY());
    snappedBounds.setBounds(t.t(transposed));
  }

  /**
   * Grid snapping magic.
   *
   * Widget move snapping depends on mouse move direction: when direction is leading then widget
   * left coordinate snapped to nearest to left grid-based coordinate, for trailing move direction
   * widget's right side snapped to nearest to right grid-based coordinate.
   *
   * Widget resizing snaps as usual, depending on value passed from request.
   *
   * @param bounds
   *          the transposed bounds to snap to grid.
   * @param resizeDirection
   *          the resize direction value passed from request.
   * @param moveDirection
   *          the transposed mouse move direction.
   * @param gridStep
   *          the "transposed" grid step.
   */
  private void applyGrid(Rectangle bounds, int resizeDirection, int moveDirection, int gridStep) {
    if (resizeDirection == 0) { // moving
      if (moveDirection == PlacementInfo.LEADING) {
        int snapX = applyGrid(bounds.x, gridStep);
        bounds.x = snapX;
      } else {
        int snapRight = applyGrid(bounds.right(), gridStep);
        bounds.x = snapRight - bounds.width;
      }
    } else { // resizing
      if ((resizeDirection & IPositionConstants.WEST) != 0
          || (resizeDirection & IPositionConstants.NORTH) != 0) {
        int snapX = applyGrid(bounds.x, gridStep);
        bounds.width = bounds.right() - snapX;
        bounds.x = snapX;
      } else if ((resizeDirection & IPositionConstants.EAST) != 0
          || (resizeDirection & IPositionConstants.SOUTH) != 0) {
        int snapRight = applyGrid(bounds.right(), gridStep);
        bounds.width = snapRight - bounds.x;
      }
    }
  }

  /**
   * Simple math round to nearest integer, based on grid step.
   */
  private int applyGrid(int value, int step) {
    return value / step * step;
  }

  /**
   * Helper method to calculate mouse move direction.
   */
  protected void calculateMouseMoveDirections(Point mouseLocation) {
    if (m_lastMouseLocation == null) {
      m_lastMouseLocation = mouseLocation;
      return;
    }
    int deltaX = mouseLocation.x - m_lastMouseLocation.x;
    int deltaY = mouseLocation.y - m_lastMouseLocation.y;
    if (deltaX != 0) {
      m_horizontalMouseMoveDirection = deltaX > 0 ? PlacementInfo.TRAILING : PlacementInfo.LEADING;
    }
    if (deltaY != 0) {
      m_verticalMouseMoveDirection = deltaY > 0 ? PlacementInfo.TRAILING : PlacementInfo.LEADING;
    }
    m_lastMouseLocation = mouseLocation;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Snap points creation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Used to create snap points at whole.
   */
  private List<SnapPoint> getSnapPoints(boolean isHorizontal) {
    List<SnapPoint> pts = Lists.newArrayList();
    for (IAbstractComponentInfo child : m_allWidgets) {
      pts.addAll(m_snapPoints.forComponent(child, isHorizontal));
    }
    pts.addAll(m_snapPoints.forContainer(isHorizontal));
    return pts;
  }

  public static class DefaultSnapPoints implements ISnapPointsProvider {
    private final IVisualDataProvider m_visualDataProvider;
    private final List<? extends IAbstractComponentInfo> m_allWidgets;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public DefaultSnapPoints(IVisualDataProvider visualDataProvider,
        List<? extends IAbstractComponentInfo> allWidgets) {
      m_visualDataProvider = visualDataProvider;
      m_allWidgets = allWidgets;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // ISnapPointsProvider
    //
    ////////////////////////////////////////////////////////////////////////////
    public List<SnapPoint> forContainer(boolean isHorizontal) {
      List<SnapPoint> pts = Lists.newArrayList();
      int leadingSide = PlacementUtils.getSide(isHorizontal, true);
      int trailingSide = PlacementUtils.getSide(isHorizontal, false);
      // snap to parent at left side with gap
      pts.add(new ContainerSnapPoint(m_visualDataProvider, leadingSide, true));
      // snap to parent at left side
      pts.add(new ContainerSnapPoint(m_visualDataProvider, leadingSide));
      // snap to parent at right side with gap
      pts.add(new ContainerSnapPoint(m_visualDataProvider, trailingSide, true));
      // snap to parent at right side
      pts.add(new ContainerSnapPoint(m_visualDataProvider, trailingSide));
      //
      pts.add(new SameSizeSnapPoint(m_visualDataProvider, m_allWidgets, leadingSide));
      pts.add(new SameSizeSnapPoint(m_visualDataProvider, m_allWidgets, trailingSide));
      return pts;
    }

    public List<SnapPoint> forComponent(IAbstractComponentInfo target, boolean isHorizontal) {
      List<SnapPoint> pts = Lists.newArrayList();
      int leadingSide = PlacementUtils.getSide(isHorizontal, true);
      int trailingSide = PlacementUtils.getSide(isHorizontal, false);
      if (isHorizontal) {
        // snap to child on left side with indent
        pts.add(new IndentedComponentSnapPoint(m_visualDataProvider, target));
      } else {
        // baseline snap
        pts.add(new BaselineComponentSnapPoint(m_visualDataProvider, target));
      }
      // snap to child on left side with gap
      pts.add(new ComponentSnapPoint(m_visualDataProvider,
          target,
          leadingSide,
          PlacementInfo.TRAILING,
          true));
      // snap to child on left side
      pts.add(new ComponentSnapPoint(m_visualDataProvider,
          target,
          leadingSide,
          PlacementInfo.LEADING));
      // snap to child on right side with gap
      pts.add(new ComponentSnapPoint(m_visualDataProvider,
          target,
          trailingSide,
          PlacementInfo.LEADING,
          true));
      // snap to child on right side
      pts.add(new ComponentSnapPoint(m_visualDataProvider,
          target,
          trailingSide,
          PlacementInfo.TRAILING));
      return pts;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Feedbacks
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Removes all feedbacks.
   */
  public void removeFeedbacks() {
    for (Figure figure : m_feedbacks) {
      FigureUtils.removeFigure(figure);
    }
    m_feedbacks = Lists.newArrayList();
  }
}