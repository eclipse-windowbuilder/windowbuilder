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
package org.eclipse.wb.internal.swing.FormLayout.gef.header.layout;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.gef.figure.TextFeedback;
import org.eclipse.wb.core.gef.header.AbstractHeaderLayoutEditPolicy;
import org.eclipse.wb.core.gef.policy.layout.grid.AbstractGridLayoutEditPolicy;
import org.eclipse.wb.core.gef.policy.layout.grid.IGridInfo;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.draw2d.Layer;
import org.eclipse.wb.draw2d.geometry.Interval;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.core.requests.IDropRequest;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.swing.FormLayout.gef.FormLayoutEditPolicy;
import org.eclipse.wb.internal.swing.FormLayout.gef.GefMessages;
import org.eclipse.wb.internal.swing.FormLayout.gef.header.edit.ColumnHeaderEditPart;
import org.eclipse.wb.internal.swing.FormLayout.gef.header.selection.ColumnSelectionEditPolicy;
import org.eclipse.wb.internal.swing.FormLayout.model.FormColumnInfo;
import org.eclipse.wb.internal.swing.FormLayout.model.FormLayoutInfo;

import org.eclipse.draw2d.geometry.Point;

import java.text.MessageFormat;

/**
 * Implementation of {@link LayoutEditPolicy} for {@link ColumnHeaderEditPart}.
 *
 * @author scheglov_ke
 * @coverage swing.FormLayout.header
 */
public final class ColumnsLayoutEditPolicy extends AbstractHeaderLayoutEditPolicy {
  private final FormLayoutEditPolicy m_mainPolicy;
  private final FormLayoutInfo m_layout;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ColumnsLayoutEditPolicy(FormLayoutEditPolicy mainPolicy, FormLayoutInfo layout) {
    super(mainPolicy);
    m_mainPolicy = mainPolicy;
    m_layout = layout;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Children
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void decorateChild(EditPart child) {
    child.installEditPolicy(EditPolicy.SELECTION_ROLE, new ColumnSelectionEditPolicy(m_mainPolicy));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Move
  //
  ////////////////////////////////////////////////////////////////////////////
  private final Figure m_insertFeedback = AbstractGridLayoutEditPolicy.createInsertFigure();
  private TextFeedback m_feedback;
  private Command m_moveCommand;

  @Override
  protected Command getMoveCommand(ChangeBoundsRequest request) {
    if (!m_layout.canChangeDimensions()) {
      return null;
    }
    return m_moveCommand;
  }

  @Override
  protected void showLayoutTargetFeedback(Request request) {
    // prepare header
    ColumnHeaderEditPart headerEditPart;
    {
      ChangeBoundsRequest changeBoundsRequest = (ChangeBoundsRequest) request;
      headerEditPart = (ColumnHeaderEditPart) changeBoundsRequest.getEditParts().get(0);
    }
    // prepare location
    Point location;
    {
      IDropRequest dropRequest = (IDropRequest) request;
      location = dropRequest.getLocation().getCopy();
    }
    // prepare target header
    ColumnHeaderEditPart target = null;
    {
      for (EditPart editPart : getHost().getChildren()) {
        ColumnHeaderEditPart columnEditPart = (ColumnHeaderEditPart) editPart;
        Rectangle bounds = columnEditPart.getFigure().getBounds();
        if (location.x < bounds.getCenter().x) {
          target = columnEditPart;
          break;
        }
      }
    }
    // prepare grid information
    IGridInfo gridInfo = m_layout.getGridInfo();
    Interval[] columnIntervals = gridInfo.getColumnIntervals();
    Interval[] rowIntervals = gridInfo.getRowIntervals();
    int y1 = rowIntervals[0].begin - 5;
    int y2 = rowIntervals[rowIntervals.length - 1].end() + 5;
    // prepare index of target column and position for insert feedbacks
    int index;
    int x;
    int size = AbstractGridLayoutEditPolicy.INSERT_COLUMN_SIZE;
    if (target != null) {
      index = target.getIndex();
      // set default
      x = columnIntervals[index].begin - size / 2;
      // check for gap
      FormColumnInfo column = m_layout.getColumns().get(index);
      if (column.isGap()) {
        x = columnIntervals[index].begin;
        size = columnIntervals[index].length + 1;
      } else if (index != 0) {
        FormColumnInfo prevColumn = m_layout.getColumns().get(index - 1);
        if (prevColumn.isGap()) {
          x = columnIntervals[index - 1].begin;
          size = columnIntervals[index - 1].length + 1;
          index--;
        }
      }
    } else {
      index = m_layout.getColumns().size();
      m_mainPolicy.showInsertFeedbacks(null, null);
      //
      x = columnIntervals[columnIntervals.length - 1].end() - size / 2;
    }
    // show insert feedbacks
    {
      // ...on main viewer
      m_mainPolicy.showInsertFeedbacks(new Rectangle(x, y1, size, y2 - y1), null);
      // ...on header viewer
      {
        if (m_insertFeedback.getParent() == null) {
          addFeedback(m_insertFeedback);
        }
        //
        Point offset = headerEditPart.getOffset();
        Rectangle bounds = new Rectangle(x + offset.x, 0, size, getHostFigure().getSize().height);
        m_insertFeedback.setBounds(bounds);
      }
    }
    // show text feedback
    {
      Layer feedbackLayer = getMainLayer(IEditPartViewer.FEEDBACK_LAYER);
      // add feedback
      if (m_feedback == null) {
        m_feedback = new TextFeedback(feedbackLayer);
        m_feedback.add();
      }
      // set feedback bounds
      {
        Point feedbackLocation = new Point(location.x + 30, 10);
        FigureUtils.translateAbsoluteToFigure(feedbackLayer, feedbackLocation);
        m_feedback.setLocation(feedbackLocation);
      }
      // set text
      m_feedback.setText(MessageFormat.format(
          GefMessages.ColumnsLayoutEditPolicy_feedbackPattern,
          1 + index));
    }
    // prepare command
    {
      final FormColumnInfo column = headerEditPart.getDimension();
      final int targetIndex = index;
      m_moveCommand = new EditCommand(m_layout) {
        @Override
        protected void executeEdit() throws Exception {
          m_layout.command_MOVE_COLUMN(m_layout.getColumns().indexOf(column), targetIndex);
        }
      };
    }
  }

  @Override
  protected void eraseLayoutTargetFeedback(Request request) {
    m_mainPolicy.eraseInsertFeedbacks();
    FigureUtils.removeFigure(m_insertFeedback);
    if (m_feedback != null) {
      m_feedback.remove();
      m_feedback = null;
    }
  }
}
