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
package org.eclipse.wb.core.gef.policy.layout.grid;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.gef.policy.PolicyUtils;
import org.eclipse.wb.core.gef.policy.helpers.BroadcastListenerHelper;
import org.eclipse.wb.core.gef.policy.layout.LayoutPolicyUtils;
import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.draw2d.AbstractRelativeLocator;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.IColorConstants;
import org.eclipse.wb.draw2d.ICursorConstants;
import org.eclipse.wb.draw2d.ILocator;
import org.eclipse.wb.draw2d.IPositionConstants;
import org.eclipse.wb.draw2d.RectangleFigure;
import org.eclipse.wb.draw2d.RelativeLocator;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Interval;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.graphical.handles.Handle;
import org.eclipse.wb.gef.graphical.handles.MoveHandle;
import org.eclipse.wb.gef.graphical.handles.SquareHandle;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;
import org.eclipse.wb.gef.graphical.tools.ResizeTracker;
import org.eclipse.wb.internal.core.DesignerPlugin;

import org.eclipse.swt.graphics.Color;

import java.util.Collection;
import java.util.List;

/**
 * Implementation of {@link SelectionEditPolicy} for "grid based" layouts.
 *
 * @author scheglov_ke
 * @coverage core.gef.policy.grid
 */
public abstract class AbstractGridSelectionEditPolicy extends SelectionEditPolicy {
  private final IAbstractComponentInfo m_component;
  protected final ObjectInfo m_object;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractGridSelectionEditPolicy(IAbstractComponentInfo component) {
    m_component = component;
    m_object = m_component.getUnderlyingModel();
    // add listeners
    new BroadcastListenerHelper(m_object, this, new ObjectEventListener() {
      @Override
      public void refreshed() throws Exception {
        boolean activePolicy = isActive();
        boolean activeLayout = isActiveLayout();
        boolean isSelected = getHost().getSelected() == EditPart.SELECTED_PRIMARY;
        boolean isDeleted = m_component.isDeleted();
        if (activePolicy && activeLayout && isSelected && !isDeleted) {
          hideSelection();
          showSelection();
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if this layout manager is active on this components parent.
   */
  protected abstract boolean isActiveLayout();

  /**
   * @return the {@link IGridInfo} for host container.
   */
  protected abstract IGridInfo getGridInfo();

  /**
   * @return the cells {@link Rectangle} occupied by host component.
   */
  private Rectangle getComponentCells() throws Exception {
    return getGridInfo().getComponentCells(m_component);
  }

  /**
   * @return the pixels {@link Rectangle} of host component.
   */
  private Rectangle getComponentCellBounds() throws Exception {
    Rectangle cells = getComponentCells();
    return getGridInfo().getCellsRectangle(cells);
  }

  /**
   * @return the pixels {@link Rectangle} of host component in feedback.
   */
  private Rectangle getComponentCellBounds_atFeedback() throws Exception {
    Rectangle bounds = getComponentCellBounds();
    translateModelToFeedback(bounds);
    return bounds;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Handles
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link MoveHandle} for host component.
   */
  protected final MoveHandle createMoveHandle() {
    MoveHandle moveHandle = new MoveHandle(getHost(), new ILocator() {
      public void relocate(Figure target) {
        try {
          Rectangle bounds = getComponentCellBounds_atFeedback();
          target.setBounds(bounds);
        } catch (Throwable e) {
          DesignerPlugin.log(e);
        }
      }
    });
    moveHandle.setForeground(IColorConstants.red);
    return moveHandle;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Selection
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void showSelection() {
    super.showSelection();
    if (getHost().getSelected() == EditPart.SELECTED_PRIMARY) {
      showPrimarySelection();
      showAlignmentFigures();
    }
  }

  @Override
  protected void hideSelection() {
    super.hideSelection();
    hideAlignmentFigures();
  }

  /**
   * Shows the selection for primary selected {@link EditPart}.
   */
  protected void showPrimarySelection() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Cell figures XXX
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final int MIN_LEFT_SPACE = 10;
  private static final int INITIAL_RIGHT_SPACE = 10;
  private static final int FIGURES_SPACE = 10;
  private List<Figure> m_alignmentFigures;

  /**
   * @return the alignment figure for given component and axis.
   */
  protected abstract Figure createAlignmentFigure(IAbstractComponentInfo component,
      boolean horizontal);

  /**
   * Shows alignment figures for host {@link EditPart} and its siblings.
   */
  public final void showAlignmentFigures() {
    if (m_alignmentFigures == null) {
      m_alignmentFigures = Lists.newArrayList();
      // show cell figures for all children of host's parent
      {
        Collection<EditPart> editParts = getHost().getParent().getChildren();
        for (EditPart editPart : editParts) {
          showCellFigures(editPart);
        }
      }
    }
  }

  /**
   * Hides alignment figures for this host and its siblings.
   */
  public final void hideAlignmentFigures() {
    if (m_alignmentFigures != null) {
      for (Figure figure : m_alignmentFigures) {
        figure.getParent().remove(figure);
      }
      m_alignmentFigures = null;
    }
  }

  /**
   * Shows all possible cell figures for given edit part.
   */
  private void showCellFigures(EditPart editPart) {
    // check model
    IAbstractComponentInfo component;
    {
      Object model = editPart.getModel();
      if (!(model instanceof IAbstractComponentInfo)) {
        return;
      }
      component = (IAbstractComponentInfo) model;
    }
    // check, if we can show alignment figures for this control
    {
      // FIXME
      //String showFiguresString = DesignerPlugin.getGridLayoutAlignmentFigures();
      String showFiguresString = null;
      if (!LayoutPolicyUtils.shouldShowSideFigures(showFiguresString, editPart)) {
        return;
      }
    }
    // ok, we can show alignment figures
    {
      int offset = INITIAL_RIGHT_SPACE;
      {
        Figure horizontalFigure = createAlignmentFigure(component, true);
        if (horizontalFigure != null) {
          offset += horizontalFigure.getSize().width;
          addAlignmentFigure(component, horizontalFigure, offset);
          offset += FIGURES_SPACE;
        }
      }
      {
        Figure verticalFigure = createAlignmentFigure(component, false);
        if (verticalFigure != null) {
          offset += verticalFigure.getSize().width;
          addAlignmentFigure(component, verticalFigure, offset);
          offset += FIGURES_SPACE;
        }
      }
    }
  }

  /**
   * Adds alignment figure at given offset from right side of component's cells.
   */
  private void addAlignmentFigure(IAbstractComponentInfo component, Figure figure, int offset) {
    Figure layer = getLayer(IEditPartViewer.CLICKABLE_LAYER);
    // prepare rectangle for cells used by component (in layer coordinates)
    Rectangle cellRect;
    {
      IGridInfo gridInfo = getGridInfo();
      Rectangle cells = gridInfo.getComponentCells(component);
      cellRect = gridInfo.getCellsRectangle(cells).getCopy();
      translateModelToFeedback(cellRect);
    }
    // prepare location and size
    Point figureLocation;
    {
      Dimension figureSize = figure.getSize();
      figureLocation = new Point(cellRect.right() - offset, cellRect.y - figureSize.height / 2);
      if (figureLocation.x < cellRect.x + MIN_LEFT_SPACE) {
        return;
      }
    }
    // add alignment figure
    layer.add(figure);
    figure.setLocation(figureLocation);
    m_alignmentFigures.add(figure);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Requests routing
  //
  ////////////////////////////////////////////////////////////////////////////
  protected RectangleFigure m_lineFeedback;

  @Override
  public boolean understandsRequest(Request request) {
    return super.understandsRequest(request)
        || request.getType() == REQ_RESIZE_SPAN
        || request.getType() == REQ_RESIZE_SIZE;
  }

  @Override
  public void showSourceFeedback(Request request) {
    try {
      if (request instanceof ChangeBoundsRequest) {
        ChangeBoundsRequest cbRequest = (ChangeBoundsRequest) request;
        if (REQ_RESIZE_SIZE.equals(request.getType())) {
          showSizeFeedback(cbRequest);
        }
        if (REQ_RESIZE_SPAN.equals(request.getType())) {
          showSpanFeedback(cbRequest);
        }
      }
    } catch (Throwable e) {
      DesignerPlugin.log(e);
    }
  }

  @Override
  public void eraseSourceFeedback(Request request) {
    if (m_lineFeedback != null) {
      removeFeedback(m_lineFeedback);
      m_lineFeedback = null;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Command getCommand(Request request) {
    Object type = request.getType();
    if (REQ_RESIZE_SIZE.equals(type)) {
      return m_sizeCommand;
    }
    if (REQ_RESIZE_SPAN.equals(type)) {
      return m_spanCommand;
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // XXX Resize support
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final String REQ_RESIZE_SIZE = "resize_size";
  protected Command m_sizeCommand;

  /**
   * @return the new resize {@link Handle} for given direction.
   */
  protected final Handle createSizeHandle(int direction, double percent) {
    return createSizeHandle(direction, createComponentLocator(direction, percent));
  }

  /**
   * @return the new resize {@link Handle} with given direction.
   */
  protected final Handle createSizeHandle(int direction, ILocator locator) {
    return new SizeHandle(direction, locator);
  }

  /**
   * Shows feedback and creates command for resize.
   */
  private void showSizeFeedback(ChangeBoundsRequest request) throws Exception {
    // prepare new bounds
    Rectangle bounds = m_component.getModelBounds().getCopy();
    bounds.translate(request.getMoveDelta());
    bounds.resize(request.getSizeDelta());
    // create command
    boolean isHorizontal =
        request.getResizeDirection() == IPositionConstants.WEST
            || request.getResizeDirection() == IPositionConstants.EAST;
    m_sizeCommand = createSizeCommand(isHorizontal, bounds.getSize());
    // show feedback
    {
      // add feedback figure
      if (m_lineFeedback == null) {
        m_lineFeedback = new RectangleFigure();
        m_lineFeedback.setForeground(IColorConstants.green);
        addFeedback(m_lineFeedback);
      }
      // set bounds
      {
        translateModelToFeedback(bounds);
        m_lineFeedback.setBounds(bounds);
      }
    }
  }

  /**
   * @return the {@link Command} for changing size of host component.
   */
  protected Command createSizeCommand(boolean horizontal, Dimension size) {
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Resize: handle
  //
  ////////////////////////////////////////////////////////////////////////////
  private class SizeHandle extends SquareHandle {
    public SizeHandle(int direction, ILocator locator) {
      super(getHost(), locator);
      setCursor(ICursorConstants.Directional.getCursor(direction));
      setDragTrackerTool(new ResizeTracker(direction, REQ_RESIZE_SIZE));
    }

    @Override
    protected Color getFillColor() {
      return isPrimary() ? IColorConstants.black : IColorConstants.white;
    }

    @Override
    protected Color getBorderColor() {
      return IColorConstants.white;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Spanning support
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final String REQ_RESIZE_SPAN = "resize_span";
  protected Command m_spanCommand;

  /**
   * @return the new span {@link Handle} for given direction.
   */
  protected final Handle createSpanHandle(int direction, double percent) {
    return createSpanHandle(direction, createCellLocator(direction, percent));
  }

  /**
   * @return the new span {@link Handle} with given direction.
   */
  protected final Handle createSpanHandle(int direction, ILocator locator) {
    return new SpanHandle(direction, locator);
  }

  /**
   * Shows feedback and creates command for span.
   */
  private void showSpanFeedback(ChangeBoundsRequest request) throws Exception {
    IGridInfo gridInfo = getGridInfo();
    // prepare cells
    Rectangle cells = getComponentCells().getCopy();
    {
      // prepare cells rectangle
      Rectangle bounds = getGridInfo().getCellsRectangle(cells).getCopy();
      bounds.translate(request.getMoveDelta());
      bounds.resize(request.getSizeDelta());
      // prepare direction
      int direction = request.getResizeDirection();
      boolean isWest = direction == IPositionConstants.WEST;
      boolean isEast = direction == IPositionConstants.EAST;
      boolean isNorth = direction == IPositionConstants.NORTH;
      boolean isSouth = direction == IPositionConstants.SOUTH;
      // update cells
      if (isWest || isEast) {
        Interval[] columnIntervals = gridInfo.getColumnIntervals();
        if (isWest) {
          int begin = 1 + Interval.getRightmostIntervalIndex(columnIntervals, bounds.x);
          begin = Math.max(begin, 0);
          begin = Math.min(begin, cells.right() - 1);
          begin = fixSpanLocation(true, begin, -1, cells, gridInfo);
          //
          cells.setX(begin);
        } else if (isEast) {
          int end = Interval.getRightmostIntervalIndex(columnIntervals, bounds.right());
          end = Math.min(end, columnIntervals.length - 1);
          end = Math.max(end, cells.x);
          end = fixSpanLocation(true, end, +1, cells, gridInfo);
          //
          cells.setRight(1 + end);
        }
        // create command
        m_spanCommand = createSpanCommand(true, cells);
      } else if (isNorth || isSouth) {
        Interval[] rowIntervals = gridInfo.getRowIntervals();
        if (isNorth) {
          int begin = 1 + Interval.getRightmostIntervalIndex(rowIntervals, bounds.y);
          begin = Math.max(begin, 0);
          begin = Math.min(begin, cells.bottom() - 1);
          begin = fixSpanLocation(false, begin, -1, cells, gridInfo);
          //
          cells.setY(begin);
        } else if (isSouth) {
          int end = Interval.getRightmostIntervalIndex(rowIntervals, bounds.bottom());
          end = Math.min(end, rowIntervals.length - 1);
          end = Math.max(end, cells.y);
          end = fixSpanLocation(false, end, +1, cells, gridInfo);
          //
          cells.setBottom(1 + end);
        }
        // create command
        m_spanCommand = createSpanCommand(false, cells);
      }
    }
    // show feedback
    {
      // add feedback figure
      if (m_lineFeedback == null) {
        m_lineFeedback = new RectangleFigure();
        m_lineFeedback.setForeground(IColorConstants.green);
        addFeedback(m_lineFeedback);
      }
      // set bounds
      {
        Rectangle bounds = gridInfo.getCellsRectangle(cells);
        translateModelToFeedback(bounds);
        m_lineFeedback.setBounds(bounds);
      }
    }
  }

  /**
   * @return the fixed location for given axis, component cells and grid information, so that we
   *         don't intersect other components.
   *
   * @param locationStep
   *          the step for changing location, for example it is +1 for end and -1 for begin
   *          location.
   */
  private int fixSpanLocation(boolean horizontal,
      int location,
      int locationStep,
      Rectangle cells,
      IGridInfo gridInfo) {
    if (horizontal) {
      int column = locationStep == 1 ? cells.right() : cells.x;
      for (; locationStep == 1 ? column <= location : column >= location; column += locationStep) {
        for (int row = cells.y; row < cells.bottom(); row++) {
          IAbstractComponentInfo occupied = gridInfo.getOccupied(column, row);
          if (occupied != null && occupied != m_component) {
            return column - locationStep;
          }
        }
      }
      return location;
    } else {
      int row = locationStep == 1 ? cells.bottom() : cells.y;
      for (; locationStep == 1 ? row <= location : row >= location; row += locationStep) {
        for (int column = cells.x; column < cells.right(); column++) {
          IAbstractComponentInfo occupied = gridInfo.getOccupied(column, row);
          if (occupied != null && occupied != m_component) {
            return row - locationStep;
          }
        }
      }
      return location;
    }
  }

  /**
   * @return the {@link Command} for changing span of host component.
   */
  protected abstract Command createSpanCommand(boolean horizontal, Rectangle cells);

  ////////////////////////////////////////////////////////////////////////////
  //
  // Spanning: handle
  //
  ////////////////////////////////////////////////////////////////////////////
  private class SpanHandle extends SquareHandle {
    public SpanHandle(int direction, ILocator locator) {
      super(getHost(), locator);
      setCursor(ICursorConstants.Directional.getCursor(direction));
      setDragTrackerTool(new ResizeTracker(direction, REQ_RESIZE_SPAN));
    }

    @Override
    protected Color getFillColor() {
      return isPrimary() ? IColorConstants.green : IColorConstants.white;
    }

    @Override
    protected Color getBorderColor() {
      return IColorConstants.black;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Locators
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return {@link ILocator} that positions handles on component side.
   */
  protected final ILocator createComponentLocator(int direction, double percent) {
    Figure reference = getHostFigure();
    if (direction == IPositionConstants.WEST) {
      return new RelativeLocator(reference, 0, percent);
    } else if (direction == IPositionConstants.EAST) {
      return new RelativeLocator(reference, 1, percent);
    } else if (direction == IPositionConstants.NORTH) {
      return new RelativeLocator(reference, percent, percent);
    } else if (direction == IPositionConstants.SOUTH) {
      return new RelativeLocator(reference, percent, 1);
    }
    throw new IllegalArgumentException("Unknown direction: " + direction);
  }

  /**
   * @return {@link ILocator} that positions handles on component's cells side.
   */
  protected final ILocator createCellLocator(int direction, double percent) {
    class SideLocator extends AbstractRelativeLocator {
      public SideLocator(double relativeX, double relativeY) {
        super(relativeX, relativeY);
      }

      @Override
      protected Rectangle getReferenceRectangle() {
        try {
          Rectangle bounds = getComponentCellBounds_atFeedback();
          return bounds;
        } catch (Throwable e) {
          return new Rectangle();
        }
      }
    }
    //
    if (direction == IPositionConstants.WEST) {
      return new SideLocator(0, percent);
    } else if (direction == IPositionConstants.EAST) {
      return new SideLocator(1, percent);
    } else if (direction == IPositionConstants.NORTH) {
      return new SideLocator(percent, 0);
    } else if (direction == IPositionConstants.SOUTH) {
      return new SideLocator(percent, 1);
    }
    throw new IllegalArgumentException("Unknown direction: " + direction);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Policy Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  protected void translateModelToFeedback(Rectangle bounds) {
    PolicyUtils.translateModelToFeedback(this, bounds);
  }
}