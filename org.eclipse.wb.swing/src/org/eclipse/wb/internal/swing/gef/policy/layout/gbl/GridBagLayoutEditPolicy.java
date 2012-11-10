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
package org.eclipse.wb.internal.swing.gef.policy.layout.gbl;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.gef.policy.PolicyUtils;
import org.eclipse.wb.core.gef.policy.layout.LayoutPolicyUtils2;
import org.eclipse.wb.core.gef.policy.layout.LayoutPolicyUtils2.IPasteProcessor;
import org.eclipse.wb.core.gef.policy.layout.grid.AbstractGridLayoutEditPolicy;
import org.eclipse.wb.core.gef.policy.layout.grid.IGridInfo;
import org.eclipse.wb.draw2d.geometry.Interval;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.core.requests.CreateRequest;
import org.eclipse.wb.gef.core.requests.PasteRequest;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;
import org.eclipse.wb.internal.core.model.util.ObjectInfoAction;
import org.eclipse.wb.internal.swing.gef.ComponentsLayoutRequestValidator;
import org.eclipse.wb.internal.swing.gef.GefMessages;
import org.eclipse.wb.internal.swing.gef.policy.layout.gbl.header.edit.ColumnHeaderEditPart;
import org.eclipse.wb.internal.swing.gef.policy.layout.gbl.header.edit.RowHeaderEditPart;
import org.eclipse.wb.internal.swing.gef.policy.layout.gbl.header.layout.ColumnsLayoutEditPolicy;
import org.eclipse.wb.internal.swing.gef.policy.layout.gbl.header.layout.RowsLayoutEditPolicy;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.layout.gbl.AbstractGridBagLayoutInfo;
import org.eclipse.wb.internal.swing.model.layout.gbl.ColumnInfo;
import org.eclipse.wb.internal.swing.model.layout.gbl.RowInfo;

import org.eclipse.jface.action.IMenuManager;

import java.util.List;

/**
 * Implementation of {@link LayoutEditPolicy} for {@link FormLayoutInfo}.
 * 
 * @author scheglov_ke
 * @coverage swing.gef.policy
 */
public final class GridBagLayoutEditPolicy extends AbstractGridLayoutEditPolicy {
  private final AbstractGridBagLayoutInfo m_layout;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public GridBagLayoutEditPolicy(AbstractGridBagLayoutInfo layout) {
    super(layout);
    m_layout = layout;
    m_gridTargetHelper = new GridHelper(this, true);
    m_gridSelectionHelper = new GridHelper(this, false);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Requests
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected ILayoutRequestValidator getRequestValidator() {
    return ComponentsLayoutRequestValidator.INSTANCE_EXT;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected IGridInfo getGridInfo() {
    return m_layout.getGridInfo();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Selection
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void decorateChild(EditPart child) {
    Object model = child.getModel();
    if (m_layout.isManagedObject(model)) {
      ComponentInfo component = (ComponentInfo) model;
      EditPolicy selectionPolicy = new GridBagSelectionEditPolicy(m_layout, component);
      child.installEditPolicy(EditPolicy.SELECTION_ROLE, selectionPolicy);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Command getCreateCommand(CreateRequest request) {
    if (m_target.m_valid) {
      final ComponentInfo component = (ComponentInfo) request.getNewObject();
      return new EditCommand(m_layout) {
        @Override
        protected void executeEdit() throws Exception {
          m_layout.command_CREATE(
              component,
              m_target.m_column,
              m_target.m_columnInsert,
              m_target.m_row,
              m_target.m_rowInsert);
        }
      };
    }
    return null;
  }

  @Override
  @SuppressWarnings("unchecked")
  protected Command getPasteCommand(PasteRequest request) {
    List<JavaInfoMemento> mementos = (List<JavaInfoMemento>) request.getMemento();
    if (m_target.m_valid && mementos.size() == 1) {
      return LayoutPolicyUtils2.getPasteCommand(
          m_layout,
          request,
          ComponentInfo.class,
          new IPasteProcessor<ComponentInfo>() {
            public void process(ComponentInfo component) throws Exception {
              m_layout.command_CREATE(
                  component,
                  m_target.m_column,
                  m_target.m_columnInsert,
                  m_target.m_row,
                  m_target.m_rowInsert);
            }
          });
    }
    return null;
  }

  @Override
  protected Command getMoveCommand(ChangeBoundsRequest request) {
    if (m_target.m_valid && request.getEditParts().size() == 1) {
      EditPart editPart = request.getEditParts().get(0);
      final ComponentInfo component = (ComponentInfo) editPart.getModel();
      return new EditCommand(m_layout) {
        @Override
        protected void executeEdit() throws Exception {
          m_layout.command_MOVE(
              component,
              m_target.m_column,
              m_target.m_columnInsert,
              m_target.m_row,
              m_target.m_rowInsert);
        }
      };
    }
    return null;
  }

  @Override
  protected Command getAddCommand(ChangeBoundsRequest request) {
    return getMoveCommand(request);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Grid target
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void updateGridTarget(Point mouseLocation) throws Exception {
    m_target = new GridTarget();
    // prepare location in model
    Point location = mouseLocation.getCopy();
    PolicyUtils.translateAbsoluteToModel(this, location);
    // prepare grid information
    IGridInfo gridInfo = m_layout.getGridInfo();
    Interval[] columnIntervals = gridInfo.getColumnIntervals();
    Interval[] rowIntervals = gridInfo.getRowIntervals();
    int lastX =
        columnIntervals.length != 0
            ? columnIntervals[columnIntervals.length - 1].end()
            : gridInfo.getInsets().left;
    int lastY =
        rowIntervals.length != 0
            ? rowIntervals[rowIntervals.length - 1].end()
            : gridInfo.getInsets().top;
    // prepare insert bounds
    {
      if (columnIntervals.length != 0) {
        m_target.m_rowInsertBounds.x = columnIntervals[0].begin - INSERT_MARGINS;
        m_target.m_rowInsertBounds.setRight(lastX + INSERT_MARGINS);
      } else {
        m_target.m_rowInsertBounds.x = 0;
        m_target.m_rowInsertBounds.setRight(getHostFigure().getSize().width);
      }
      if (rowIntervals.length != 0) {
        m_target.m_columnInsertBounds.y = rowIntervals[0].begin - INSERT_MARGINS;
        m_target.m_columnInsertBounds.setBottom(lastY + INSERT_MARGINS);
      } else {
        m_target.m_columnInsertBounds.y = 0;
        m_target.m_columnInsertBounds.setBottom(getHostFigure().getSize().height);
      }
    }
    // find existing column
    for (int columnIndex = 0; columnIndex < columnIntervals.length; columnIndex++) {
      boolean isLast = columnIndex == columnIntervals.length - 1;
      Interval interval = columnIntervals[columnIndex];
      Interval nextInterval = !isLast ? columnIntervals[columnIndex + 1] : null;
      // before first
      if (location.x < Math.max(interval.begin, INSERT_COLUMN_SIZE)) {
        m_target.m_column = 0;
        m_target.m_columnInsert = true;
        // prepare parameters
        int[] parameters =
            getInsertFeedbackParameters(new Interval(0, 0), interval, INSERT_COLUMN_SIZE);
        // feedback
        m_target.m_feedbackBounds.x = parameters[3];
        m_target.m_feedbackBounds.width = parameters[4] - parameters[3];
        // insert
        m_target.m_columnInsertBounds.x = parameters[1];
        m_target.m_columnInsertBounds.width = parameters[2] - parameters[1];
        // stop
        break;
      }
      // gap or near to end of interval
      if (nextInterval != null) {
        int gap = nextInterval.begin - interval.end();
        boolean directGap = interval.end() <= location.x && location.x < nextInterval.begin;
        boolean narrowGap = gap < 2 * INSERT_COLUMN_SIZE;
        boolean nearEnd = Math.abs(location.x - interval.end()) < INSERT_COLUMN_SIZE;
        boolean nearBegin = Math.abs(location.x - nextInterval.begin) < INSERT_COLUMN_SIZE;
        if (directGap || narrowGap && (nearEnd || nearBegin)) {
          m_target.m_column = columnIndex + 1;
          m_target.m_columnInsert = true;
          // prepare parameters
          int[] parameters =
              getInsertFeedbackParameters(interval, nextInterval, INSERT_COLUMN_SIZE);
          // feedback
          m_target.m_feedbackBounds.x = parameters[3];
          m_target.m_feedbackBounds.width = parameters[4] - parameters[3];
          // insert
          m_target.m_columnInsertBounds.x = parameters[1];
          m_target.m_columnInsertBounds.width = parameters[2] - parameters[1];
          // stop
          break;
        }
      }
      // column
      if (interval.contains(location.x)) {
        m_target.m_column = columnIndex;
        // feedback
        m_target.m_feedbackBounds.x = interval.begin;
        m_target.m_feedbackBounds.width = interval.length + 1;
        // stop
        break;
      }
    }
    // find virtual column
    if (m_target.m_column == -1) {
      int columnGap = gridInfo.getVirtualColumnGap();
      int columnSize = gridInfo.getVirtualColumnSize();
      //
      int newWidth = columnSize + columnGap;
      int newDelta = Math.max(location.x - lastX - columnGap / 2, 0) / newWidth;
      //
      m_target.m_column = columnIntervals.length + newDelta;
      m_target.m_feedbackBounds.x = lastX + columnGap + newWidth * newDelta;
      m_target.m_feedbackBounds.width = columnSize + 1;
    }
    // find existing row
    for (int rowIndex = 0; rowIndex < rowIntervals.length; rowIndex++) {
      boolean isLast = rowIndex == rowIntervals.length - 1;
      Interval interval = rowIntervals[rowIndex];
      Interval nextInterval = !isLast ? rowIntervals[rowIndex + 1] : null;
      // before first
      if (location.y < Math.max(interval.begin, INSERT_ROW_SIZE)) {
        m_target.m_row = 0;
        m_target.m_rowInsert = true;
        // prepare parameters
        int[] parameters =
            getInsertFeedbackParameters(new Interval(0, 0), interval, INSERT_ROW_SIZE);
        // feedback
        m_target.m_feedbackBounds.y = parameters[3];
        m_target.m_feedbackBounds.height = parameters[4] - parameters[3];
        // insert
        m_target.m_rowInsertBounds.y = parameters[1];
        m_target.m_rowInsertBounds.height = parameters[2] - parameters[1];
        // stop
        break;
      }
      // gap or near to end of interval
      if (nextInterval != null) {
        int gap = nextInterval.begin - interval.end();
        boolean directGap = interval.end() <= location.y && location.y < nextInterval.begin;
        boolean narrowGap = gap < 2 * INSERT_ROW_SIZE;
        boolean nearEnd = Math.abs(location.y - interval.end()) < INSERT_ROW_SIZE;
        boolean nearBegin = Math.abs(location.y - nextInterval.begin) < INSERT_ROW_SIZE;
        if (directGap || narrowGap && (nearEnd || nearBegin)) {
          m_target.m_row = rowIndex + 1;
          m_target.m_rowInsert = true;
          // prepare parameters
          int[] parameters = getInsertFeedbackParameters(interval, nextInterval, INSERT_ROW_SIZE);
          // feedback
          m_target.m_feedbackBounds.y = parameters[3];
          m_target.m_feedbackBounds.height = parameters[4] - parameters[3];
          // insert
          m_target.m_rowInsertBounds.y = parameters[1];
          m_target.m_rowInsertBounds.height = parameters[2] - parameters[1];
          // stop
          break;
        }
      }
      // row
      if (interval.contains(location.y)) {
        m_target.m_row = rowIndex;
        // feedback
        m_target.m_feedbackBounds.y = interval.begin;
        m_target.m_feedbackBounds.height = interval.length + 1;
        // stop
        break;
      }
    }
    // find virtual row
    if (m_target.m_row == -1) {
      int rowGap = gridInfo.getVirtualRowGap();
      int rowSize = gridInfo.getVirtualRowSize();
      //
      int newHeight = rowSize + rowGap;
      int newDelta = Math.max(location.y - lastY - rowGap / 2, 0) / newHeight;
      //
      m_target.m_row = rowIntervals.length + newDelta;
      m_target.m_feedbackBounds.y = lastY + rowGap + newHeight * newDelta;
      m_target.m_feedbackBounds.height = rowSize + 1;
    }
  }

  /**
   * Determines parameters of insert feedback.
   * 
   * @return the array of: visual gap, begin/end of insert feedback, begin/end of target feedback.
   */
  public static int[] getInsertFeedbackParameters(Interval interval,
      Interval nextInterval,
      int minGap) {
    int gap = nextInterval.begin - interval.end();
    int visualGap = Math.max(gap, minGap);
    // determine x1/x2
    int x1, x2;
    {
      int a = interval.end();
      int b = nextInterval.begin;
      int x1_2 = a + b - visualGap;
      x1 = x1_2 % 2 == 0 ? x1_2 / 2 : x1_2 / 2 - 1;
      x2 = a + b - x1;
      // we don't want to have insert feedback be same as intervals
      if (x1 == a - 1) {
        x1--;
        x2++;
      }
    }
    //
    return new int[]{visualGap, x1, x2, x1 - minGap, x2 + minGap};
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IHeadersProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  public LayoutEditPolicy getContainerLayoutPolicy(boolean horizontal) {
    if (horizontal) {
      return new ColumnsLayoutEditPolicy(this, m_layout);
    } else {
      return new RowsLayoutEditPolicy(this, m_layout);
    }
  }

  public List<?> getHeaders(boolean horizontal) {
    return horizontal ? m_layout.getColumns() : m_layout.getRows();
  }

  public EditPart createHeaderEditPart(boolean horizontal, Object model) {
    if (horizontal) {
      return new ColumnHeaderEditPart(m_layout, (ColumnInfo) model, getHostFigure());
    } else {
      return new RowHeaderEditPart(m_layout, (RowInfo) model, getHostFigure());
    }
  }

  public void buildContextMenu(IMenuManager manager, boolean horizontal) {
    if (horizontal) {
      manager.add(new ObjectInfoAction(m_layout, GefMessages.GridBagLayoutEditPolicy_appendColumn) {
        @Override
        protected void runEx() throws Exception {
          m_layout.getColumnOperations().insert(m_layout.getColumns().size());
        }
      });
    } else {
      manager.add(new ObjectInfoAction(m_layout, GefMessages.GridBagLayoutEditPolicy_appendRow) {
        @Override
        protected void runEx() throws Exception {
          m_layout.getRowOperations().insert(m_layout.getRows().size());
        }
      });
    }
  }

  public void handleDoubleClick(boolean horizontal) {
    // we don't have columns/rows dialogs for GridBagLayout
  }
}
