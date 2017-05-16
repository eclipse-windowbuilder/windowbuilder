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

import org.eclipse.wb.core.gef.figure.TextFeedback;
import org.eclipse.wb.core.gef.header.IHeadersProvider;
import org.eclipse.wb.core.gef.policy.PolicyUtils;
import org.eclipse.wb.core.gef.policy.helpers.BroadcastListenerHelper;
import org.eclipse.wb.core.gef.policy.helpers.SelectionListenerHelper;
import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.draw2d.border.LineBorder;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.core.events.IEditPartSelectionListener;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.core.requests.IDropRequest;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.ui.SwtResourceManager;
import org.eclipse.wb.internal.draw2d.SemiTransparentFigure;

import org.eclipse.swt.graphics.Color;

/**
 * Implementation of {@link LayoutEditPolicy} for "grid based" layouts, i.e. layouts where each
 * {@link EditPart} located in some cell - intersection of column and row.
 *
 * @author scheglov_ke
 * @coverage core.gef.policy.grid
 */
public abstract class AbstractGridLayoutEditPolicy extends LayoutEditPolicy
    implements
      IHeadersProvider {
  protected AbstractGridHelper m_gridTargetHelper;
  protected AbstractGridHelper m_gridSelectionHelper;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractGridLayoutEditPolicy(ObjectInfo layout) {
    // add listeners
    new BroadcastListenerHelper(layout, this, new ObjectEventListener() {
      @Override
      public void refreshed2() throws Exception {
        refreshSelectionGrid();
      }
    });
    new SelectionListenerHelper(this, new IEditPartSelectionListener() {
      public void selectionChanged(EditPart editPart) {
        refreshSelectionGrid();
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link IGridInfo} for host container.
   */
  protected abstract IGridInfo getGridInfo();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Selection
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void deactivate() {
    super.deactivate();
    m_gridSelectionHelper.eraseGridFeedback();
  }

  /**
   * Shows/erases the grid on host {@link EditPart} selection.
   */
  private void refreshSelectionGrid() {
    m_gridSelectionHelper.eraseGridFeedback();
    if (getHost().getSelected() != EditPart.SELECTED_NONE) {
      EditPolicy parentLayoutEditPolicy =
          getHost().getParent().getEditPolicy(EditPolicy.LAYOUT_ROLE);
      // show grid only if parent layout is not grid too (to avoid two grids at same time)
      if (!(parentLayoutEditPolicy instanceof AbstractGridLayoutEditPolicy)) {
        m_gridSelectionHelper.showGridFeedback();
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Feedback
  //
  ////////////////////////////////////////////////////////////////////////////
  protected GridTarget m_target;
  private final Figure m_targetFeedback = new SemiTransparentFigure(64);
  private TextFeedback m_textFeedback;
  private static final Color m_goodTargetFillColor = SwtResourceManager.getColor(0, 255, 0);
  private static final Color m_goodTargetBorderColor = SwtResourceManager.getColor(192, 255, 192);
  private static final Color m_badTargetFillColor = SwtResourceManager.getColor(255, 0, 0);
  private static final Color m_badTargetBorderColor = SwtResourceManager.getColor(255, 192, 192);

  @Override
  protected final void showLayoutTargetFeedback(Request request) {
    IGridInfo gridInfo = getGridInfo();
    m_gridTargetHelper.showGridFeedback();
    //
    try {
      Point location = ((IDropRequest) request).getLocation();
      updateGridTarget(location);
      removeTargetFigures();
      // show insert feedbacks
      showInsertFeedbacks(m_target.m_columnInsertBounds, m_target.m_rowInsertBounds);
      // show target feedbacks
      if (!m_target.m_feedbackBounds.isEmpty()) {
        validateTarget(gridInfo);
        // try to show better feedback than "invalid"
        if (!m_target.m_valid) {
          if (showOccupiedLayoutTargetFeedback(request)) {
            return;
          }
        } else {
          eraseOccupiedLayoutTargetFeedback(request);
        }
        // show target rectangle
        {
          if (m_target.m_valid) {
            m_targetFeedback.setBackground(m_goodTargetFillColor);
            m_targetFeedback.setBorder(new LineBorder(m_goodTargetBorderColor));
          } else {
            m_targetFeedback.setBackground(m_badTargetFillColor);
            m_targetFeedback.setBorder(new LineBorder(m_badTargetBorderColor));
          }
          getLayer(IEditPartViewer.HANDLE_LAYER_SUB_1).add(m_targetFeedback);
          //
          Rectangle bounds = m_target.m_feedbackBounds.getCopy();
          bounds.shrink(1, 1);
          PolicyUtils.translateModelToFeedback(this, bounds);
          m_targetFeedback.setBounds(bounds);
        }
        // show text feedback
        {
          if (m_textFeedback == null) {
            m_textFeedback = new TextFeedback(getFeedbackLayer());
            m_textFeedback.add();
          }
          m_textFeedback.setText("column " + m_target.m_column + ", row " + m_target.m_row);
          m_textFeedback.setLocation(location.getTranslated(15, 33));
        }
      }
    } catch (Throwable e) {
      DesignerPlugin.log(e);
    }
  }

  /**
   * Checks if {@link #m_target} points into some filled cell and update "valid" status.
   */
  private void validateTarget(IGridInfo gridInfo) {
    IAbstractComponentInfo targetComponent = null;
    // column insert
    if (targetComponent == null && m_target.m_columnInsert) {
      IAbstractComponentInfo leftTarget =
          gridInfo.getOccupied(m_target.m_column - 1, m_target.m_row);
      IAbstractComponentInfo rightTarget = gridInfo.getOccupied(m_target.m_column, m_target.m_row);
      if (leftTarget != null && leftTarget == rightTarget) {
        targetComponent = leftTarget;
      }
    }
    // row insert
    if (targetComponent == null && m_target.m_rowInsert) {
      IAbstractComponentInfo topTarget =
          gridInfo.getOccupied(m_target.m_column, m_target.m_row - 1);
      IAbstractComponentInfo bottomTarget = gridInfo.getOccupied(m_target.m_column, m_target.m_row);
      if (topTarget != null && topTarget == bottomTarget) {
        targetComponent = topTarget;
      }
    }
    // no insert
    if (targetComponent == null && !(m_target.m_columnInsert || m_target.m_rowInsert)) {
      targetComponent = gridInfo.getOccupied(m_target.m_column, m_target.m_row);
    }
    // update "invalid" target
    if (targetComponent != null) {
      m_target.m_valid = false;
      Rectangle targetCells = gridInfo.getComponentCells(targetComponent);
      if (targetCells != null) {
        m_target.m_feedbackBounds = gridInfo.getCellsRectangle(targetCells);
      }
    }
  }

  @Override
  protected final void eraseLayoutTargetFeedback(Request request) {
    m_gridTargetHelper.eraseGridFeedback();
    removeTargetFigures();
    eraseOccupiedLayoutTargetFeedback(request);
  }

  /**
   * Removes target/insert figures.
   */
  private void removeTargetFigures() {
    eraseInsertFeedbacks();
    FigureUtils.removeFigure(m_targetFeedback);
    if (m_textFeedback != null) {
      m_textFeedback.remove();
      m_textFeedback = null;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Additional feedback
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Allows to show feedback for occupied cells, for example cell split for MigLayout. By default we
   * show "invalid" feedback.
   *
   * @return <code>true</code> if request was processed, so "invalid" feedback should not be
   *         displayed.
   */
  protected boolean showOccupiedLayoutTargetFeedback(Request request) {
    return false;
  }

  /**
   * Erases feedback shown in {@link #showOccupiedLayoutTargetFeedback(Request)}.
   */
  protected void eraseOccupiedLayoutTargetFeedback(Request request) {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Insert feedbacks
  //
  ////////////////////////////////////////////////////////////////////////////
  private final Figure m_insertColumnFeedback = createInsertFigure();
  private final Figure m_insertRowFeedback = createInsertFigure();
  private static final Color m_insertTargetFillColor = new Color(null, 255, 255, 128);
  private static final Color m_insertTargetBorderColor = new Color(null, 255, 235, 30);

  /**
   * @return the {@link Figure} that can be used as "insert feedback".
   */
  public static Figure createInsertFigure() {
    Figure figure = new SemiTransparentFigure(160);
    figure.setBackground(m_insertTargetFillColor);
    figure.setBorder(new LineBorder(m_insertTargetBorderColor));
    return figure;
  }

  /**
   * Shows the column/row insertion feedbacks.
   *
   * @param columnBounds
   *          the column insertion rectangle (in model coordinates), if <code>null</code> no column
   *          feedback will be displayed.
   *
   * @param rowBounds
   *          the row insertion rectangle (in model coordinates), if <code>null</code> no row
   *          feedback will be displayed.
   */
  public void showInsertFeedbacks(Rectangle columnBounds, Rectangle rowBounds) {
    removeTargetFigures();
    // column
    if (columnBounds != null && !columnBounds.isEmpty()) {
      getLayer(IEditPartViewer.HANDLE_LAYER_SUB_1).add(m_insertColumnFeedback);
      //
      Rectangle bounds = columnBounds.getCopy();
      PolicyUtils.translateModelToFeedback(this, bounds);
      m_insertColumnFeedback.setBounds(bounds);
    }
    // row
    if (rowBounds != null && !rowBounds.isEmpty()) {
      getLayer(IEditPartViewer.HANDLE_LAYER_SUB_1).add(m_insertRowFeedback);
      //
      Rectangle bounds = rowBounds.getCopy();
      PolicyUtils.translateModelToFeedback(this, bounds);
      m_insertRowFeedback.setBounds(bounds);
    }
  }

  /**
   * Erases insert feedback figures displayed using
   * {@link #showInsertFeedbacks(Rectangle, Rectangle)}.
   */
  public void eraseInsertFeedbacks() {
    FigureUtils.removeFigure(m_insertColumnFeedback);
    FigureUtils.removeFigure(m_insertRowFeedback);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Grid target
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * The extract size of "insert feedback" on secondary axis.
   */
  protected static final int INSERT_MARGINS = 5;
  /**
   * The width of "insert column" feedback between two columns without gap.
   */
  public static final int INSERT_COLUMN_SIZE = 7;
  /**
   * The width of "insert row" feedback between two rows without gap.
   */
  public static final int INSERT_ROW_SIZE = 5;

  /**
   * Description for target cell.
   *
   * @author scheglov_ke
   */
  protected static class GridTarget {
    public boolean m_valid = true;
    public int m_column = -1;
    public boolean m_columnInsert;
    public int m_row = -1;
    public boolean m_rowInsert;
    public Rectangle m_feedbackBounds = new Rectangle();
    public Rectangle m_columnInsertBounds = new Rectangle();
    public Rectangle m_rowInsertBounds = new Rectangle();

    public GridTarget() {
    }

    @Override
    public String toString() {
      return m_column
          + " "
          + m_columnInsert
          + " "
          + m_row
          + " "
          + m_rowInsert
          + " "
          + m_feedbackBounds;
    }
  }

  /**
   * Updates the current {@link GridTarget}.
   *
   * @param mouseLocation
   *          the location of mouse in absolute coordinates.
   */
  protected abstract void updateGridTarget(Point mouseLocation) throws Exception;
}
