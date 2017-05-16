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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.draw2d.IPositionConstants;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Interval;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.draw2d.geometry.Transposer;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.gef.policy.snapping.PlacementInfo.AttachmentTypes;
import org.eclipse.wb.internal.core.utils.Debug;
import org.eclipse.wb.internal.core.utils.Pair;
import org.eclipse.wb.internal.core.utils.check.Assert;

import org.apache.commons.collections.CollectionUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Intelligent component placement preparations.
 *
 * @author mitin_aa
 * @coverage core.gef.policy.snapping
 */
public final class PlacementsSupport {
  private final IVisualDataProvider m_visualDataProvider;
  private final IFeedbackProxy m_feedbackProxy;
  private final List<IAbstractComponentInfo> m_allWidgets;
  private List<IAbstractComponentInfo> m_operatingWidgets;
  private SnapPoints m_snapPoints;
  //
  private final PlacementInfo m_x = new PlacementInfo();
  private final PlacementInfo m_y = new PlacementInfo();
  private Rectangle m_bounds;
  private final Map<IAbstractComponentInfo, Rectangle> m_newModelBounds = Maps.newHashMap();
  private final Map<IAbstractComponentInfo, Rectangle> m_oldModelBounds = Maps.newHashMap();
  private final Map<IAbstractComponentInfo, Integer[]> m_effectiveAlignments =
      new HashMap<IAbstractComponentInfo, Integer[]>();
  private final IAbsoluteLayoutCommands m_layoutCommands;
  private int m_resizeDirection;
  private boolean m_isCreating;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @param visualDataProvider
   *          the instance of {@link IVisualDataProvider}.
   * @param allWidgets
   *          the list of the components which are not processed now (rest of the components).
   */
  public PlacementsSupport(IVisualDataProvider visualDataProvider,
      IFeedbackProxy feedbackProxy,
      IAbsoluteLayoutCommands layout,
      List<? extends IAbstractComponentInfo> allWidgets) {
    m_visualDataProvider = visualDataProvider;
    m_feedbackProxy = feedbackProxy;
    m_layoutCommands = layout;
    m_snapPoints = new SnapPoints(visualDataProvider, feedbackProxy, allWidgets);
    m_allWidgets = Lists.newArrayList(allWidgets);
  }

  /**
   * Private constructor for internal use only.
   */
  private PlacementsSupport(IAbstractComponentInfo widget,
      IVisualDataProvider visualDataProvider,
      IAbsoluteLayoutCommands layoutCommands,
      List<? extends IAbstractComponentInfo> remainingWidgets) {
    this(visualDataProvider, null, layoutCommands, remainingWidgets);
    m_bounds = PlacementUtils.getTranslatedBounds(visualDataProvider, widget);
    m_operatingWidgets = ImmutableList.of(widget);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // API
  //
  ////////////////////////////////////////////////////////////////////////////
  public Rectangle getBounds() {
    return m_bounds.getCopy();
  }

  public void drag(Point location,
      IAbstractComponentInfo widget,
      Rectangle widgetBounds,
      int resizeDirection) {
    m_resizeDirection = resizeDirection;
    m_isCreating = widget.getModelBounds() == null;
    m_bounds = widgetBounds.getCopy();
    m_operatingWidgets = ImmutableList.of(widget);
    m_snapPoints.processBounds(this, location, m_operatingWidgets, resizeDirection);
    m_newModelBounds.put(widget, m_bounds.getCopy());
  }

  public void drag(Point location,
      List<IAbstractComponentInfo> widgets,
      Rectangle widgetsUnionBounds,
      List<Rectangle> relativeBounds) {
    drag(location, widgets, widgetsUnionBounds, relativeBounds, 0);
  }

  public void drag(Point location,
      List<IAbstractComponentInfo> widgets,
      Rectangle widgetsUnionBounds,
      List<Rectangle> relativeBounds,
      int resizeDirection) {
    m_resizeDirection = resizeDirection;
    setOperatingWidgets(widgets);
    Assert.isTrue(widgets.size() == relativeBounds.size());
    m_bounds = widgetsUnionBounds.getCopy();
    m_snapPoints.processBounds(this, location, m_operatingWidgets, resizeDirection);
    for (int i = 0; i < widgets.size(); ++i) {
      IAbstractComponentInfo widget = widgets.get(i);
      Rectangle relativeRect = relativeBounds.get(i);
      Rectangle modelBounds = m_bounds.getTranslated(relativeRect.x, relativeRect.y);
      modelBounds.setSize(relativeRect.getSize());
      if (isResizing()) {
        if (PlacementUtils.hasHorizontalResizeSide(resizeDirection)) {
          modelBounds.setRight(m_bounds.right());
        }
        if (PlacementUtils.hasVerticalResizeSide(resizeDirection)) {
          modelBounds.setBottom(m_bounds.bottom());
        }
      }
      m_newModelBounds.put(widget, modelBounds);
    }
  }

  public void commit() throws Exception {
    doCommit();
    // all widgets processed
    cleanup();
  }

  public void commitAdd() throws Exception {
    doCommit();
    addWidgets();
    cleanup();
  }

  public void delete(List<IAbstractComponentInfo> widgets) throws Exception {
    setOperatingWidgets(widgets);
    preprocess();
    // remove any reference to deleted widgets
    postprocess();
    // forget deleted widgets
    removeWidgets(widgets);
    cleanup();
  }

  public void clearFeedbacks() {
    m_snapPoints.removeFeedbacks();
  }

  public void cleanup() {
    m_operatingWidgets = null;
    m_resizeDirection = 0;
    m_x.cleanup();
    m_y.cleanup();
    m_newModelBounds.clear();
    m_oldModelBounds.clear();
    m_effectiveAlignments.clear();
    clearFeedbacks();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Positioning
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Prepares information for component placement.
   *
   * @param mouseMoveDirection
   *          the array containing mouse move directions for both dimensions
   * @param snappedPoints
   *          the array with snapped points for both dimensions, values of the array can be
   *          <code>null</code>.
   */
  void doDrag(int mouseMoveDirection[], SnapPoint[] snappedPoints) {
    {
      PlacementInfo xPlacement = getPlacementInfoX();
      xPlacement.setDirection(mouseMoveDirection[0]);
      setAttachmentType(xPlacement, snappedPoints[0]);
      checkAttachedToWidget(xPlacement, snappedPoints[0]);
      findNeighbors(xPlacement, true);
      findOverlappings(xPlacement, true);
    }
    {
      PlacementInfo yPlacement = getPlacementInfoY();
      yPlacement.setDirection(mouseMoveDirection[1]);
      setAttachmentType(yPlacement, snappedPoints[1]);
      checkAttachedToWidget(yPlacement, snappedPoints[1]);
      findNeighbors(yPlacement, false);
      findOverlappings(yPlacement, false);
    }
  }

  private void findNeighbors() {
    findNeighbors(getPlacementInfoX(), true);
    findNeighbors(getPlacementInfoY(), false);
  }

  private void checkAttachedToWidget(PlacementInfo placement, SnapPoint snapPoint) {
    if (placement.getAttachmentType() == AttachmentTypes.Component) {
      ComponentSnapPoint componentSnapPoint = (ComponentSnapPoint) snapPoint;
      placement.setAttachedToWidget(componentSnapPoint.getComponent());
      int direction = placement.getDirection();
      // early fill the neighbor
      if (componentSnapPoint.getGap() != 0) {
        boolean isNeighbor =
            !PlacementUtils.isLeadingSide(componentSnapPoint.getSide()) == (direction == PlacementInfo.LEADING);
        if (isNeighbor) {
          placement.getNeighbors()[direction] = componentSnapPoint.getComponent();
          placement.getDistances()[direction] = componentSnapPoint.getGap();
        }
      }
    } else if (placement.getAttachmentType() == AttachmentTypes.ComponentWithOffset) {
      int direction = placement.getDirection();
      IndentedComponentSnapPoint componentSnapPoint = (IndentedComponentSnapPoint) snapPoint;
      placement.setAttachedToWidget(componentSnapPoint.getComponent());
      placement.getDistances()[direction] = IndentedComponentSnapPoint.INDENT;
    } else if (placement.getAttachmentType() == AttachmentTypes.Baseline) {
      placement.setAttachedToWidget(((ComponentSnapPoint) snapPoint).getComponent());
    }
  }

  private void findOverlappings(PlacementInfo placement, boolean isHorizontal) {
    Transposer t = new Transposer(!isHorizontal);
    Rectangle componentsBounds = t.t(m_bounds.getCopy());
    Interval componentsWidth = new Interval(componentsBounds.x, componentsBounds.width);
    int componentsWidthCenter = componentsWidth.center();
    Interval componentsHeight = new Interval(componentsBounds.y, componentsBounds.height);
    List<IAbstractComponentInfo>[] overlappings = placement.getOverlappings();
    int[] distances = placement.getDistances();
    // traverse children
    List<IAbstractComponentInfo> remainingComponents = getRemainingWidgets();
    for (IAbstractComponentInfo component : remainingComponents) {
      // test where is the component located: leading or trailing
      Rectangle childComponentBounds =
          t.t(PlacementUtils.getTranslatedBounds(m_visualDataProvider, component));
      // component should intersect in opposite dimension.
      Interval childComponentHeight =
          new Interval(childComponentBounds.y, childComponentBounds.height);
      if (componentsHeight.intersects(childComponentHeight)) {
        Interval childComponentWidth =
            new Interval(childComponentBounds.x, childComponentBounds.width);
        if (childComponentWidth.intersects(componentsWidth)) {
          // overlapping goes there, see how it overlaps
          Interval intersection = componentsWidth.getIntersection(childComponentWidth);
          int distance = -intersection.length();
          int childComponentCenter = childComponentWidth.center();
          int direction;
          if (childComponentCenter > componentsWidthCenter) {
            direction = PlacementInfo.TRAILING;
          } else {
            direction = PlacementInfo.LEADING;
          }
          overlappings[direction].add(component);
          if (distances[direction] > distance) {
            distances[direction] = distance;
          }
        }
      }
    }
  }

  private void findNeighbors(PlacementInfo placement, boolean isHorizontal) {
    findNeighbor(PlacementInfo.LEADING, placement, isHorizontal);
    findNeighbor(PlacementInfo.TRAILING, placement, isHorizontal);
  }

  private void findNeighbor(int direction, PlacementInfo placement, boolean isHorizontal) {
    IAbstractComponentInfo[] neighbors = placement.getNeighbors();
    int[] distances = placement.getDistances();
    // the exact neighbor can already be found using ComponentSnapPoint
    if (neighbors[direction] == null) {
      Transposer t = new Transposer(!isHorizontal);
      Rectangle widgetsBounds = t.t(m_bounds.getCopy());
      Interval widgetsWidth = new Interval(widgetsBounds.x, widgetsBounds.width);
      Interval widgetsHeight = new Interval(widgetsBounds.y, widgetsBounds.height);
      List<IAbstractComponentInfo> remainingWidgets = getRemainingWidgets();
      for (IAbstractComponentInfo widget : remainingWidgets) {
        // test where is the neighbor component located: leading or trailing
        Rectangle possibleNeighborBounds =
            t.t(PlacementUtils.getTranslatedBounds(m_visualDataProvider, widget));
        // neighbor should intersect in opposite dimension.
        Interval possibleNeighborHeight =
            new Interval(possibleNeighborBounds.y, possibleNeighborBounds.height);
        if (widgetsHeight.intersects(possibleNeighborHeight)) {
          // this is possible neighbor, get the most nearest depending on side: leading or trailing
          Interval possibleNeighborWidth =
              new Interval(possibleNeighborBounds.x, possibleNeighborBounds.width);
          if (!possibleNeighborWidth.intersects(widgetsWidth)) {
            // no overlapping, check the distances
            if (direction == PlacementInfo.LEADING
                && possibleNeighborWidth.isLeadingOf(widgetsWidth)) {
              // leading
              int distance = widgetsWidth.distance(possibleNeighborWidth.end());
              if (distances[direction] > distance) {
                distances[direction] = distance;
                neighbors[direction] = widget;
                continue;
              }
            } else if (direction == PlacementInfo.TRAILING
                && possibleNeighborWidth.isTrailingOf(widgetsWidth)) {
              // trailing
              int distance = widgetsWidth.distance(possibleNeighborWidth.begin());
              if (distances[direction] > distance) {
                distances[direction] = distance;
                neighbors[direction] = widget;
                continue;
              }
            }
          }
        }
      }
      if (neighbors[direction] == null
          && placement.getAttachmentType() != AttachmentTypes.ComponentWithOffset) {
        // if no one wants to be our neighbor ;-) so get the distance to the container's boundary
        Dimension containerSize = t.t(m_visualDataProvider.getContainerSize());
        distances[direction] =
            direction == PlacementInfo.LEADING ? widgetsWidth.begin() : containerSize.width
                - widgetsWidth.end();
      }
    }
  }

  private void setAttachmentType(PlacementInfo placement, SnapPoint snapPoint) {
    if (snapPoint != null) {
      if (snapPoint instanceof BaselineComponentSnapPoint) {
        placement.setAttachmentType(AttachmentTypes.Baseline);
        return;
      } else if (snapPoint instanceof IndentedComponentSnapPoint) {
        placement.setAttachmentType(AttachmentTypes.ComponentWithOffset);
        return;
      } else if (snapPoint instanceof ComponentSnapPoint) {
        placement.setAttachmentType(AttachmentTypes.Component);
        return;
      } else if (snapPoint instanceof ContainerSnapPoint) {
        placement.setAttachmentType(AttachmentTypes.Container);
        return;
      }
    }
    placement.setAttachmentType(AttachmentTypes.Free);
  }

  /**
   * Commits the changes into layout. <br>
   * After the commit the {@link IAbstractComponentInfo#getModelBounds()} are set to real bounds
   * that it should be in layout.
   */
  private void doCommit() throws Exception {
    preprocess();
    if (isResizing()) {
      if (isCreating()) {
        place();
      }
      IAbstractComponentInfo widget = m_operatingWidgets.get(0);
      resize(widget);
    } else {
      place();
    }
    // keep positioning of other widgets
    postprocess();
  }

  private void place() throws Exception {
    if (m_operatingWidgets.size() > 1) {
      placeMultipleWidgets();
    } else {
      placeSingleWidget();
    }
  }

  private void placeMultipleWidgets() throws Exception {
    for (IAbstractComponentInfo widget : m_operatingWidgets) {
      placeSingleWidget(widget, true);
      placeSingleWidget(widget, false);
    }
  }

  private boolean isAttachedWithinOperatingWidgets(IAbstractComponentInfo widget, int side)
      throws Exception {
    if (m_adjustingAttached) {
      return false;
    }
    IAbstractComponentInfo attachedToWidget = m_layoutCommands.getAttachedToWidget(widget, side);
    return attachedToWidget != null && m_operatingWidgets.contains(attachedToWidget);
  }

  private void placeSingleWidget() throws Exception {
    IAbstractComponentInfo widget = m_operatingWidgets.get(0);
    placeSingleWidget(widget, true);
    placeSingleWidget(widget, false);
  }

  private void placeSingleWidget(IAbstractComponentInfo widget, boolean isHorizontal)
      throws Exception {
    PlacementInfo placementInfo = getPlacementInfo(isHorizontal);
    if (!isOverlapped(isHorizontal)) {
      // no overlapping at all
      if (placementInfo.getAttachedToWidget() == null) {
        placeFreely(widget, placementInfo, isHorizontal);
      } else {
        placeAttachedToWidget(widget, placementInfo, isHorizontal);
      }
    } else {
      // overlapping
      Debug.println("move overlapping");
    }
  }

  private void placeAttachedToWidget(IAbstractComponentInfo widget,
      PlacementInfo placementInfo,
      boolean isHorizontal) throws Exception {
    IAbstractComponentInfo attachedToWidget = placementInfo.getAttachedToWidget();
    int direction = placementInfo.getDirection();
    int side = PlacementUtils.getSide(isHorizontal, direction == PlacementInfo.LEADING);
    int oppositeSide = PlacementUtils.getOppositeSide(side);
    boolean isAttachedOpposite =
        m_layoutCommands.isAttached(widget, side)
            && m_layoutCommands.isAttached(widget, oppositeSide);
    m_layoutCommands.detach(widget, side);
    if (attachedToWidget == placementInfo.getNeighbors()[direction]) {
      // sequential attachment
      m_layoutCommands.attachWidgetSequientially(
          widget,
          attachedToWidget,
          side,
          placementInfo.getDistances()[direction]);
    } else {
      // parallel attachment
      if (AttachmentTypes.Baseline == placementInfo.getAttachmentType()) {
        m_layoutCommands.attachWidgetBaseline(widget, attachedToWidget);
      } else {
        int distance =
            placementInfo.getAttachmentType() == AttachmentTypes.ComponentWithOffset
                ? placementInfo.getDistances()[direction]
                : 0;
        m_layoutCommands.attachWidgetParallelly(widget, attachedToWidget, side, distance);
      }
    }
    if (isAttachedOpposite) {
      m_layoutCommands.adjustAttachmentOffset(
          widget,
          oppositeSide,
          getMoveDelta(widget, isHorizontal));
    } else {
      if (AttachmentTypes.Baseline == placementInfo.getAttachmentType()) {
        m_layoutCommands.detach(widget, IPositionConstants.BOTTOM);
      } else {
        m_layoutCommands.detach(widget, oppositeSide);
      }
    }
  }

  private void placeFreely(IAbstractComponentInfo widget,
      PlacementInfo placementInfo,
      boolean isHorizontal) throws Exception {
    int[] distances = placementInfo.getDistances();
    int alignment =
        distances[PlacementInfo.TRAILING] < distances[PlacementInfo.LEADING]
            ? PlacementInfo.TRAILING
            : PlacementInfo.LEADING;
    placeFreelyUsingAlignment(widget, placementInfo, isHorizontal, alignment);
  }

  private void placeFreelyUsingAlignment(IAbstractComponentInfo widget,
      PlacementInfo placementInfo,
      boolean isHorizontal,
      int alignment) throws Exception {
    int[] distances = placementInfo.getDistances();
    int side = PlacementUtils.getSide(isHorizontal, alignment == PlacementInfo.LEADING);
    int oppositeSide = PlacementUtils.getOppositeSide(side);
    boolean isAttachedBothSides =
        m_layoutCommands.isAttached(widget, side)
            && m_layoutCommands.isAttached(widget, oppositeSide);
    if (!isAttachedWithinOperatingWidgets(widget, side)) {
      m_layoutCommands.detach(widget, side);
      IAbstractComponentInfo[] neighbors = placementInfo.getNeighbors();
      int distance =
          distances[alignment]
              + getWidgetRelativeDistance(widget, isHorizontal, alignment == PlacementInfo.LEADING);
      if (neighbors[alignment] == null) {
        m_layoutCommands.attachAbsolute(widget, side, distance);
      } else {
        m_layoutCommands.attachWidgetSequientially(widget, neighbors[alignment], side, distance);
      }
    }
    if (m_adjustingAttached && isResizing()) {
      // don't process other side: this widget didn't moved
      return;
    }
    if (isAttachedBothSides) {
      m_layoutCommands.adjustAttachmentOffset(
          widget,
          oppositeSide,
          getMoveDelta(widget, isHorizontal));
    } else {
      // do not touch attaches within operating widgets list
      if (!isAttachedWithinOperatingWidgets(widget, oppositeSide)) {
        m_layoutCommands.detach(widget, oppositeSide);
      }
    }
  }

  private int getMoveDelta(IAbstractComponentInfo widget, boolean isHorizontal) {
    Rectangle movedBounds = m_newModelBounds.get(widget);
    Rectangle originalBounds = PlacementUtils.getTranslatedBounds(m_visualDataProvider, widget);
    return isHorizontal ? movedBounds.x - originalBounds.x : movedBounds.y - originalBounds.y;
  }

  private int getWidgetRelativeDistance(IAbstractComponentInfo widget,
      boolean isHorizontal,
      boolean isLeading) {
    // no relative distance for non-operating widgets or moving single widget
    if (m_operatingWidgets.size() > 1 && !m_adjustingAttached) {
      if (isHorizontal) {
        if (isLeading) {
          return m_newModelBounds.get(widget).x - m_bounds.x;
        } else {
          return m_bounds.right() - m_newModelBounds.get(widget).right();
        }
      } else {
        if (isLeading) {
          return m_newModelBounds.get(widget).y - m_bounds.y;
        } else {
          return m_bounds.bottom() - m_newModelBounds.get(widget).bottom();
        }
      }
    } else {
      return 0;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Related widgets
  //
  ////////////////////////////////////////////////////////////////////////////
  private boolean m_adjustingAttached;

  private PlacementInfo findNeighborsOfWidget(IAbstractComponentInfo widget, boolean isHorizontal) {
    PlacementsSupport placementsSupport =
        new PlacementsSupport(widget,
            m_visualDataProvider,
            m_layoutCommands,
            getNonDeletedWidgets());
    placementsSupport.findNeighbors();
    return placementsSupport.getPlacementInfo(isHorizontal);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Resize
  //
  ////////////////////////////////////////////////////////////////////////////
  private void resize(IAbstractComponentInfo widget) throws Exception {
    if (PlacementUtils.hasHorizontalResizeSide(m_resizeDirection)) {
      resizeWidget(widget, true);
    }
    if (PlacementUtils.hasVerticalResizeSide(m_resizeDirection)) {
      resizeWidget(widget, false);
    }
  }

  private void resizeWidget(IAbstractComponentInfo widget, boolean isHorizontal) throws Exception {
    PlacementInfo placementInfo = getPlacementInfo(isHorizontal);
    int side = PlacementUtils.extractResizingSide(isHorizontal, m_resizeDirection);
    int resizeDelta = getResizeDelta(side, widget, isHorizontal);
    int oppositeSide = PlacementUtils.getOppositeSide(side);
    boolean isAttachedResizingSide = m_layoutCommands.isAttached(widget, side);
    boolean isAttachedOppositeSide = m_layoutCommands.isAttached(widget, oppositeSide);
    // proceed
    if (!isOverlapped(isHorizontal)) {
      // no overlapping at all
      if (placementInfo.getAttachedToWidget() == null) {
        // TODO: check if overlapping may occur (parent or another widget).
        if (isAttachedResizingSide && isAttachedOppositeSide) {
          // both attached, adjust existing attachment of the resizing side
          adjustAttachmentOffsetOnResize(widget, side, resizeDelta);
        } else if (placementInfo.getAttachmentType() == AttachmentTypes.Container
            && !isCreating()
            && !isAttachedResizingSide) {
          // resize and attach to container
          m_layoutCommands.attachAbsolute(
              widget,
              side,
              placementInfo.getDistances()[placementInfo.getDirection()]);
        } else {
          m_layoutCommands.setExplicitSize(
              widget,
              isAttachedResizingSide ? side : oppositeSide,
              side,
              resizeDelta);
          if (isAttachedResizingSide && !isCreating()) {
            adjustAttachmentOffsetOnResize(widget, side, resizeDelta);
          }
        }
      } else {
        IAbstractComponentInfo attachedToWidget = placementInfo.getAttachedToWidget();
        int direction = placementInfo.getDirection();
        int distance = placementInfo.getDistances()[direction];
        if (attachedToWidget == placementInfo.getNeighbors()[direction]) {
          // sequential attachment
          m_layoutCommands.attachWidgetSequientially(widget, attachedToWidget, side, distance);
        } else {
          // parallel attachment
          distance =
              placementInfo.getAttachmentType() == AttachmentTypes.ComponentWithOffset
                  ? placementInfo.getDistances()[direction]
                  : 0;
          m_layoutCommands.attachWidgetParallelly(widget, attachedToWidget, side, distance);
        }
        if (!isAttachedOppositeSide) {
          m_layoutCommands.setExplicitSize(
              widget,
              isAttachedResizingSide ? side : oppositeSide,
              side,
              resizeDelta);
        }
      }
    } else {
      // overlapping
      Debug.println("resize overlapping");
    }
  }

  private void adjustAttachmentOffsetOnResize(IAbstractComponentInfo widget,
      int side,
      int resizeDelta) throws Exception {
    if (!PlacementUtils.isTrailingSide(side)) {
      // see adjustAttachmentOffset() comment
      resizeDelta = -resizeDelta;
    }
    m_layoutCommands.adjustAttachmentOffset(widget, side, resizeDelta);
  }

  private int getResizeDelta(int side, IAbstractComponentInfo widget, boolean isHorizontal) {
    Rectangle changedBounds = m_newModelBounds.get(widget);
    Rectangle originalBounds = m_oldModelBounds.get(widget);//widget.getModelBounds();
    Dimension originalSize =
        originalBounds != null ? originalBounds.getSize() : widget.getPreferredSize();
    int resizeDelta =
        isHorizontal ? changedBounds.width - originalSize.width : changedBounds.height
            - originalSize.height;
    return resizeDelta;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Effective alignment
  //
  ////////////////////////////////////////////////////////////////////////////
  private int getEffectiveAlignment(IAbstractComponentInfo component, boolean isHorizontal) {
    return m_effectiveAlignments.get(component)[isHorizontal ? 0 : 1];
  }

  /**
   * Before any operation.<br>
   * 1. Finds effective alignment.<br>
   * 2. Stores old bounds.<br>
   */
  private void preprocess() throws Exception {
    for (IAbstractComponentInfo widget : m_allWidgets) {
      // find effective alignments
      int horizontal = findEffectiveAlignment(widget, true);
      int vertical = findEffectiveAlignment(widget, false);
      m_effectiveAlignments.put(widget, new Integer[]{horizontal, vertical});
      // store model bounds (translated)
      m_oldModelBounds.put(widget, PlacementUtils.getTranslatedBounds(m_visualDataProvider, widget));
    }
  }

  /**
   * Effective alignment shows with which side the widget would move if parent is resizing. If
   * widget moves with both sides (ex., attached to both sides), then effective alignment determines
   * by the nearest component or parent boundary.
   */
  private int findEffectiveAlignment(IAbstractComponentInfo widget, boolean isHorizontal)
      throws Exception {
    boolean isLeading =
        isAlignedToSide(widget, isHorizontal ? IPositionConstants.LEFT : IPositionConstants.TOP);
    boolean isTrailing =
        isAlignedToSide(widget, isHorizontal ? IPositionConstants.RIGHT : IPositionConstants.BOTTOM);
    if (!(isLeading ^ isTrailing)) {
      // return direction to the nearest widget or parent boundary
      PlacementInfo placementInfo = findNeighborsOfWidget(widget, isHorizontal);
      int[] distances = placementInfo.getDistances();
      return distances[PlacementInfo.LEADING] < distances[PlacementInfo.TRAILING]
          ? PlacementInfo.LEADING
          : PlacementInfo.TRAILING;
    } else {
      return isLeading ? PlacementInfo.LEADING : PlacementInfo.TRAILING;
    }
  }

  private boolean isAlignedToSide(IAbstractComponentInfo widget, int side) throws Exception {
    // go by given side until parent or not attached.
    while (true) {
      IAbstractComponentInfo attachedTo = m_layoutCommands.getAttachedToWidget(widget, side);
      if (attachedTo != null) {
        widget = attachedTo;
      } else {
        return m_layoutCommands.isAttached(widget, side);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access/Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the list of the components which should not be affected by operation.
   */
  @SuppressWarnings("unchecked")
  private List<IAbstractComponentInfo> getRemainingWidgets() {
    return (List<IAbstractComponentInfo>) CollectionUtils.subtract(m_allWidgets, m_operatingWidgets);
  }

  /**
   * @return the widgets which are not deleted (even within operating widgets). In move/resize
   *         operations it returns the whole list of widgets.
   */
  private List<IAbstractComponentInfo> getNonDeletedWidgets() {
    if (!CollectionUtils.isEmpty(m_operatingWidgets) && m_operatingWidgets.get(0).isDeleting()) {
      return getRemainingWidgets();
    }
    return Lists.newArrayList(m_allWidgets);
  }

  private PlacementInfo getPlacementInfoX() {
    return m_x;
  }

  private PlacementInfo getPlacementInfoY() {
    return m_y;
  }

  private PlacementInfo getPlacementInfo(boolean isHorizontal) {
    return isHorizontal ? m_x : m_y;
  }

  private boolean isResizing() {
    return m_resizeDirection != 0;
  }

  private boolean isCreating() {
    return m_isCreating;
  }

  private boolean isOverlapped(boolean isHorizontal) {
    PlacementInfo placementInfo = getPlacementInfo(isHorizontal);
    int[] distances = placementInfo.getDistances();
    return distances[PlacementInfo.LEADING] < 0 || distances[PlacementInfo.TRAILING] < 0;
  }

  private void setOperatingWidgets(List<? extends IAbstractComponentInfo> widgets) {
    m_operatingWidgets = ImmutableList.copyOf(widgets);
  }

  private void addWidgets() {
    for (IAbstractComponentInfo widget : m_operatingWidgets) {
      if (!m_allWidgets.contains(widget)) {
        m_allWidgets.add(widget);
      }
    }
    m_snapPoints = new SnapPoints(m_visualDataProvider, m_feedbackProxy, m_allWidgets);
  }

  private void removeWidgets(List<IAbstractComponentInfo> widgets) {
    for (IAbstractComponentInfo widget : widgets) {
      m_allWidgets.remove(widget);
    }
    m_snapPoints = new SnapPoints(m_visualDataProvider, m_feedbackProxy, m_allWidgets);
  }

  Rectangle getInternalBounds() {
    return m_bounds;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Alignment
  //
  ////////////////////////////////////////////////////////////////////////////
  public void align(List<? extends IAbstractComponentInfo> widgets, boolean isHorizontal, int side)
      throws Exception {
    setOperatingWidgets(widgets);
    preprocess();
    IAbstractComponentInfo sampleWidget = widgets.get(0);
    for (int i = 1; i < widgets.size(); i++) {
      IAbstractComponentInfo aligningWidget = widgets.get(i);
      PlacementInfo placementInfo =
          preparePlacementInfoAligning(sampleWidget, aligningWidget, isHorizontal, side);
      placeAttachedToWidget(aligningWidget, placementInfo, isHorizontal);
    }
    postprocess();
    cleanup();
  }

  private PlacementInfo preparePlacementInfoAligning(IAbstractComponentInfo sampleWidget,
      IAbstractComponentInfo widget,
      boolean isHorizontal,
      int side) {
    PlacementInfo placementInfo = new PlacementInfo();
    placementInfo.setAttachedToWidget(sampleWidget);
    // for CENTER alignment the direction would always be leading
    int direction = PlacementUtils.getSidePosition(side);
    placementInfo.setAttachmentType(AttachmentTypes.Component);
    placementInfo.setDirection(direction);
    Transposer t = new Transposer(!isHorizontal);
    Rectangle sampleBounds =
        t.t(PlacementUtils.getTranslatedBounds(m_visualDataProvider, sampleWidget));
    Rectangle aligningBounds =
        t.t(PlacementUtils.getTranslatedBounds(m_visualDataProvider, widget));
    switch (side) {
      case IPositionConstants.CENTER : {
        placementInfo.setAttachmentType(AttachmentTypes.ComponentWithOffset);
        int distance = sampleBounds.width / 2 - aligningBounds.width / 2;
        placementInfo.getDistances()[direction] = distance;
        aligningBounds.x += distance;
      }
        break;
      case IPositionConstants.LEFT :
      case IPositionConstants.TOP :
        aligningBounds.x = sampleBounds.x;
        break;
      case IPositionConstants.RIGHT :
      case IPositionConstants.BOTTOM :
        aligningBounds.x = sampleBounds.right() - aligningBounds.width;
        break;
      default :
        break;
    }
    m_newModelBounds.put(widget, t.t(aligningBounds));
    return placementInfo;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Replicate size
  //
  ////////////////////////////////////////////////////////////////////////////
  public void replicateSize(List<? extends IAbstractComponentInfo> widgets, boolean isHorizontal)
      throws Exception {
    setOperatingWidgets(widgets);
    preprocess();
    IAbstractComponentInfo sampleWidget = widgets.get(0);
    int sampleSize = PlacementUtils.getSize(sampleWidget, isHorizontal);
    for (int i = 1; i < widgets.size(); i++) {
      IAbstractComponentInfo widget = widgets.get(i);
      int widgetSize = PlacementUtils.getSize(widget, isHorizontal);
      int resizeDelta = sampleSize - widgetSize;
      //
      int leadingSide = PlacementUtils.getSide(isHorizontal, true);
      int trailingSide = PlacementUtils.getSide(isHorizontal, false);
      boolean isAttachedLeadingSide = m_layoutCommands.isAttached(widget, leadingSide);
      boolean isAttachedTrailingSide = m_layoutCommands.isAttached(widget, trailingSide);
      if (isAttachedLeadingSide && isAttachedTrailingSide) {
        m_layoutCommands.adjustAttachmentOffset(widget, trailingSide, resizeDelta);
      } else if (isAttachedLeadingSide && !isAttachedTrailingSide) {
        m_layoutCommands.setExplicitSize(widget, leadingSide, trailingSide, resizeDelta);
      } else if (!isAttachedLeadingSide && isAttachedTrailingSide) {
        m_layoutCommands.setExplicitSize(widget, trailingSide, leadingSide, resizeDelta);
      } else {
        m_layoutCommands.attachAbsolute(widget, leadingSide, 0);
        m_layoutCommands.setExplicitSize(widget, leadingSide, trailingSide, resizeDelta);
      }
    }
    for (int i = 1; i < widgets.size(); i++) {
      IAbstractComponentInfo widget = widgets.get(i);
      m_operatingWidgets = ImmutableList.of(widget);
      postprocess();
    }
    cleanup();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Center in parent
  //
  ////////////////////////////////////////////////////////////////////////////
  public void center(List<? extends IAbstractComponentInfo> widgets, boolean isHorizontal)
      throws Exception {
    setOperatingWidgets(widgets);
    preprocess();
    for (IAbstractComponentInfo widget : widgets) {
      centerWidget(widget, isHorizontal);
    }
    cleanup();
  }

  private void centerWidget(IAbstractComponentInfo widget, boolean isHorizontal) throws Exception {
    Transposer t = new Transposer(!isHorizontal);
    Rectangle widgetBounds = t.t(PlacementUtils.getTranslatedBounds(m_visualDataProvider, widget));
    Dimension containerSize = t.t(m_visualDataProvider.getContainerSize());
    int position = (containerSize.width - widgetBounds.width) / 2;
    Point newPosition = new Point(position, widgetBounds.y);
    //
    m_operatingWidgets = ImmutableList.of(widget);
    moveTo(widget, t.t(newPosition));
  }

  private int calcDirection(int p1, int p2) {
    return p1 >= p2 ? PlacementInfo.LEADING : PlacementInfo.TRAILING;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Distribute space
  //
  ////////////////////////////////////////////////////////////////////////////
  public void distributeSpace(List<? extends IAbstractComponentInfo> widgets, boolean isHorizontal)
      throws Exception {
    final Transposer t = new Transposer(!isHorizontal);
    Dimension clientArea = t.t(m_visualDataProvider.getContainerSize());
    // calculate sum width of objects
    int widgetsWidth = 0;
    for (IAbstractComponentInfo widget : widgets) {
      widgetsWidth += t.t(PlacementUtils.getTranslatedBounds(m_visualDataProvider, widget)).width;
    }
    // sort objects by their left positions
    Collections.sort(widgets, new Comparator<IAbstractComponentInfo>() {
      public int compare(IAbstractComponentInfo widget1, IAbstractComponentInfo widget2) {
        return t.t(PlacementUtils.getTranslatedBounds(m_visualDataProvider, widget1)).x
            - t.t(PlacementUtils.getTranslatedBounds(m_visualDataProvider, widget2)).x;
      }
    });
    // distribute objects between:
    // 1. left-most and right-most objects (if Ctrl pressed);
    // 2. or in parents client area
    int space;
    int x;
    int widgetsLength = widgets.size();
    if (DesignerPlugin.isCtrlPressed() && widgetsLength > 2) {
      // calculate space and start location (x)
      Rectangle leftBounds =
          t.t(PlacementUtils.getTranslatedBounds(m_visualDataProvider, widgets.get(0)));
      Rectangle rightBounds =
          t.t(PlacementUtils.getTranslatedBounds(
              m_visualDataProvider,
              widgets.get(widgetsLength - 1)));
      int totalWidth = rightBounds.right() - leftBounds.x;
      space = (totalWidth - widgetsWidth) / (widgetsLength - 1);
      x = leftBounds.x;
    } else {
      // calculate space and start location (x)
      space = (clientArea.width - widgetsWidth) / (widgetsLength + 1);
      x = space;
    }
    // change positions for objects from left to right
    for (IAbstractComponentInfo widget : widgets) {
      Rectangle widgetBounds =
          t.t(PlacementUtils.getTranslatedBounds(m_visualDataProvider, widget));
      //
      m_operatingWidgets = ImmutableList.of(widget);
      moveTo(widget, t.t(new Point(x, widgetBounds.y)));
      x += widgetBounds.width;
      x += space;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Internal move
  //
  ////////////////////////////////////////////////////////////////////////////
  private void moveTo(IAbstractComponentInfo widget, Point position) throws Exception {
    Rectangle widgetBounds = PlacementUtils.getTranslatedBounds(m_visualDataProvider, widget);
    m_bounds = new Rectangle(position, widgetBounds.getSize());
    doDrag(
        new int[]{
            calcDirection(widgetBounds.x, position.x),
            calcDirection(widgetBounds.y, position.y)},
        new SnapPoint[]{null, null});
    m_newModelBounds.put(widget, m_bounds);
    commit();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Layout Actions
  //
  ////////////////////////////////////////////////////////////////////////////
  public void setAlignment(IAbstractComponentInfo widget, int side) throws Exception {
    setOperatingWidgets(ImmutableList.of(widget));
    preprocess();
    // proceed
    boolean isHorizontal = PlacementUtils.isHorizontalSide(side);
    int oppositeSide = PlacementUtils.getOppositeSide(side);
    boolean attachedSide = m_layoutCommands.isAttached(widget, side);
    boolean attachedOppositeSide = m_layoutCommands.isAttached(widget, oppositeSide);
    if (!attachedSide) {
      PlacementInfo placementInfo = findNeighborsOfWidget(widget, isHorizontal);
      placeFreelyUsingAlignment(
          widget,
          placementInfo,
          isHorizontal,
          PlacementUtils.getSidePosition(side));
    }
    if (attachedOppositeSide) {
      // detach if both attached
      m_layoutCommands.detach(widget, oppositeSide);
      // keep size
      m_layoutCommands.setExplicitSize(widget, side, oppositeSide, 0);
    }
    // finish
    postprocess();
    cleanup();
  }

  public void setResizeable(IAbstractComponentInfo widget, boolean isHorizontal) throws Exception {
    setOperatingWidgets(ImmutableList.of(widget));
    preprocess();
    // proceed
    int leadingSide = PlacementUtils.getSide(isHorizontal, true);
    int trailingSide = PlacementUtils.getSide(isHorizontal, false);
    boolean attachedLeading = m_layoutCommands.isAttached(widget, leadingSide);
    boolean attachedTrailing = m_layoutCommands.isAttached(widget, trailingSide);
    PlacementInfo placementInfo = findNeighborsOfWidget(widget, isHorizontal);
    if (!attachedLeading) {
      attachSide(widget, placementInfo, leadingSide);
    }
    if (!attachedTrailing) {
      attachSide(widget, placementInfo, trailingSide);
    }
    // finish
    postprocess();
    cleanup();
  }

  private void attachSide(IAbstractComponentInfo widget, PlacementInfo placementInfo, int side)
      throws Exception {
    IAbstractComponentInfo[] neighbors = placementInfo.getNeighbors();
    int sidePosition = PlacementUtils.getSidePosition(side);
    int distance = placementInfo.getDistances()[sidePosition];
    // TODO: attach parallel also.
    if (neighbors[sidePosition] == null) {
      m_layoutCommands.attachAbsolute(widget, side, distance);
    } else {
      m_layoutCommands.attachWidgetSequientially(widget, neighbors[sidePosition], side, distance);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Adjusting and optimizing layout
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Storing model bounds in {@link IAbstractComponentInfo} allows to keep widgets positions as they
   * should be after operation. And can easier working with set of widgets also.
   */
  private void applyNewBounds() {
    for (IAbstractComponentInfo widget : m_operatingWidgets) {
      Rectangle newBounds = m_newModelBounds.get(widget);
      if (newBounds != null) {
        // bounds changed, apply
        widget.setModelBounds(newBounds.getTranslated(m_visualDataProvider.getClientAreaOffset()));
      }
    }
  }

  /**
   * Works with operating widgets. <br>
   * 1. Applies new bounds if available. <br>
   * 2. Keeps positioning of other widgets. <br>
   * 3. Resolves cycling attachments. <br>
   */
  private void postprocess() throws Exception {
    applyNewBounds();
    keepWidgetsPositions();
    resolveCyclicReferences();
  }

  /**
   * 1. Find widgets which can be affected by an operation (except operation widgets). Operations
   * are: <br>
   * 2. Delete. For deleted widgets just to re-attach affected widgets keeping existing effective
   * alignment.<br>
   * 3. Move. Adjust the attachment if the operating (moved) widget is still a neighbor otherwise
   * re-attach keeping effective alignment. <br>
   * 4. Resize. Adjust the attachment.
   */
  private void keepWidgetsPositions() throws Exception {
    List<ComponentAttachmentInfo> affectedWidgets = findAffectedWidgets();
    for (ComponentAttachmentInfo attachmentInfo : affectedWidgets) {
      IAbstractComponentInfo source = attachmentInfo.getSource();
      int side = attachmentInfo.getAlignment();
      boolean isHorizontal = PlacementUtils.isHorizontalSide(side);
      PlacementInfo placementInfo = findNeighborsOfWidget(source, isHorizontal);
      // re-attach the affected widget
      placeFreelyUsingAlignment2(
          source,
          placementInfo,
          isHorizontal,
          getEffectiveAlignment(source, isHorizontal));
    }
  }

  /**
   * For widgets, which was affected by operation on operating widgets.
   */
  private void placeFreelyUsingAlignment2(IAbstractComponentInfo widget,
      PlacementInfo placementInfo,
      boolean isHorizontal,
      int alignment) throws Exception {
    int side = PlacementUtils.getSide(isHorizontal, alignment == PlacementInfo.LEADING);
    int oppositeSide = PlacementUtils.getOppositeSide(side);
    boolean isResizeable = isResizeable(widget, isHorizontal);
    m_layoutCommands.detach(widget, side);
    attachSide(widget, placementInfo, side);
    m_layoutCommands.detach(widget, oppositeSide);
    if (isResizeable) {
      attachSide(widget, placementInfo, oppositeSide);
    }
  }

  private boolean isResizeable(IAbstractComponentInfo widget, boolean isHorizontal)
      throws Exception {
    // TODO: use effective alignment
    int side = PlacementUtils.getSide(isHorizontal, true);
    int oppositeSide = PlacementUtils.getOppositeSide(side);
    return m_layoutCommands.isAttached(widget, side)
        && m_layoutCommands.isAttached(widget, oppositeSide);
  }

  /**
   * @return the list of widgets which attached to given <code>widget</code> by any side.
   */
  private List<ComponentAttachmentInfo> findAffectedWidgets() throws Exception {
    List<ComponentAttachmentInfo> attached = Lists.newArrayList();
    // traverse through non-operating widgets.
    List<IAbstractComponentInfo> remainingWidgets = getRemainingWidgets();
    for (IAbstractComponentInfo remainingWidget : remainingWidgets) {
      // check every remaining widget for to be attached in list of operating widget
      for (IAbstractComponentInfo operatingWidget : m_operatingWidgets) {
        // check every side
        checkAttached(remainingWidget, operatingWidget, attached, IPositionConstants.LEFT);
        checkAttached(remainingWidget, operatingWidget, attached, IPositionConstants.RIGHT);
        checkAttached(remainingWidget, operatingWidget, attached, IPositionConstants.TOP);
        checkAttached(remainingWidget, operatingWidget, attached, IPositionConstants.BOTTOM);
      }
    }
    return attached;
  }

  private boolean checkAttached(IAbstractComponentInfo widget,
      IAbstractComponentInfo targetWidget,
      List<ComponentAttachmentInfo> attachedList,
      int side) throws Exception {
    if (isAttachedToWidget(widget, targetWidget, side)) {
      attachedList.add(new ComponentAttachmentInfo(widget, targetWidget, side));
      return true;
    }
    return false;
  }

  private boolean isAttachedToWidget(IAbstractComponentInfo widget,
      IAbstractComponentInfo targetWidget,
      int side) throws Exception {
    return m_layoutCommands.getAttachedToWidget(widget, side) == targetWidget;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Resolve cyclic references
  //
  ////////////////////////////////////////////////////////////////////////////
  private void resolveCyclicReferences() throws Exception {
    resolveCyclicReferences(true);
    resolveCyclicReferences(false);
  }

  private void resolveCyclicReferences(boolean isHorizontal) throws Exception {
    List<ComponentAttachmentInfo> cyclicList = detectCyclicReferences(isHorizontal);
    while (!cyclicList.isEmpty()) {
      ComponentAttachmentInfo attachment =
          findReferenceToResolve(cyclicList, getCyclicPair(cyclicList));
      if (attachment != null) {
        resolveReference(attachment);
      }
    }
  }

  private void resolveReference(ComponentAttachmentInfo attachment) throws Exception {
    int side = attachment.getAlignment();
    IAbstractComponentInfo widget = attachment.getSource();
    boolean isLeading = PlacementUtils.isLeadingSide(side);
    boolean isHorizontal = PlacementUtils.isHorizontalSide(side);
    Transposer t = new Transposer(!isHorizontal);
    Dimension containerSize = t.t(m_visualDataProvider.getContainerSize());
    Interval widgetSize =
        PlacementUtils.getTranslatedBounds(m_visualDataProvider, widget).getInterval(isHorizontal);
    int distance = isLeading ? widgetSize.begin() : containerSize.width - widgetSize.end();
    m_layoutCommands.attachAbsolute(widget, side, distance);
  }

  private ComponentAttachmentInfo findReferenceToResolve(List<ComponentAttachmentInfo> cyclicList,
      Pair<ComponentAttachmentInfo, ComponentAttachmentInfo> pair) {
    if (!m_operatingWidgets.contains(pair.getLeft().getSource())) {
      return pair.getLeft();
    }
    return pair.getRight();
  }

  private Pair<ComponentAttachmentInfo, ComponentAttachmentInfo> getCyclicPair(List<ComponentAttachmentInfo> cyclicList) {
    ComponentAttachmentInfo first = cyclicList.get(0);
    ComponentAttachmentInfo second = null;
    for (int i = 1; i < cyclicList.size(); i++) {
      ComponentAttachmentInfo next = cyclicList.get(i);
      if (first.getSource() == next.getTarget() && next.getSource() == first.getTarget()) {
        second = next;
        break;
      }
    }
    cyclicList.remove(first);
    cyclicList.remove(second);
    return new Pair<ComponentAttachmentInfo, ComponentAttachmentInfo>(first, second);
  }

  private List<ComponentAttachmentInfo> detectCyclicReferences(boolean isHorizontal)
      throws Exception {
    List<ComponentAttachmentInfo> cyclicList = Lists.newArrayList();
    List<IAbstractComponentInfo> widgets = getNonDeletedWidgets();
    for (IAbstractComponentInfo widget : widgets) {
      IAbstractComponentInfo attachedLeading =
          m_layoutCommands.getAttachedToWidget(widget, PlacementUtils.getSide(isHorizontal, true));
      IAbstractComponentInfo attachedTrailing =
          m_layoutCommands.getAttachedToWidget(widget, PlacementUtils.getSide(isHorizontal, false));
      traverseAttachedWidgets(
          cyclicList,
          Sets.<IAbstractComponentInfo>newHashSet(),
          attachedLeading,
          widget,
          isHorizontal);
      traverseAttachedWidgets(
          cyclicList,
          Sets.<IAbstractComponentInfo>newHashSet(),
          attachedTrailing,
          widget,
          isHorizontal);
    }
    return cyclicList;
  }

  private void traverseAttachedWidgets(List<ComponentAttachmentInfo> cyclicList,
      Set<IAbstractComponentInfo> visitedWidgets,
      IAbstractComponentInfo widget,
      IAbstractComponentInfo targetWidget,
      boolean isHorizontal) throws Exception {
    if (widget == null) {
      return;
    }
    // prevent cycling here
    if (visitedWidgets.contains(widget)) {
      return;
    }
    visitedWidgets.add(widget);
    //
    int trailingSide = PlacementUtils.getSide(isHorizontal, false);
    int leadingSide = PlacementUtils.getSide(isHorizontal, true);
    IAbstractComponentInfo attachedLeading =
        m_layoutCommands.getAttachedToWidget(widget, leadingSide);
    IAbstractComponentInfo attachedTrailing =
        m_layoutCommands.getAttachedToWidget(widget, trailingSide);
    if (attachedLeading == targetWidget) {
      cyclicList.add(new ComponentAttachmentInfo(widget, targetWidget, leadingSide));
      return;
    }
    if (attachedTrailing == targetWidget) {
      cyclicList.add(new ComponentAttachmentInfo(widget, targetWidget, trailingSide));
      return;
    }
    traverseAttachedWidgets(cyclicList, visitedWidgets, attachedLeading, targetWidget, isHorizontal);
    traverseAttachedWidgets(
        cyclicList,
        visitedWidgets,
        attachedTrailing,
        targetWidget,
        isHorizontal);
  }
}
