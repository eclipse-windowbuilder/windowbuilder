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
import org.eclipse.wb.draw2d.geometry.Point;
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
import org.eclipse.wb.internal.swing.FormLayout.gef.header.edit.RowHeaderEditPart;
import org.eclipse.wb.internal.swing.FormLayout.gef.header.selection.RowSelectionEditPolicy;
import org.eclipse.wb.internal.swing.FormLayout.model.FormLayoutInfo;
import org.eclipse.wb.internal.swing.FormLayout.model.FormRowInfo;

import java.text.MessageFormat;

/**
 * Implementation of {@link LayoutEditPolicy} for {@link RowHeaderEditPart}.
 *
 * @author scheglov_ke
 * @coverage swing.FormLayout.header
 */
public final class RowsLayoutEditPolicy extends AbstractHeaderLayoutEditPolicy {
  private final FormLayoutEditPolicy m_mainPolicy;
  private final FormLayoutInfo m_layout;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public RowsLayoutEditPolicy(FormLayoutEditPolicy mainPolicy, FormLayoutInfo layout) {
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
    child.installEditPolicy(EditPolicy.SELECTION_ROLE, new RowSelectionEditPolicy(m_mainPolicy));
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
    RowHeaderEditPart headerEditPart;
    {
      ChangeBoundsRequest changeBoundsRequest = (ChangeBoundsRequest) request;
      headerEditPart = (RowHeaderEditPart) changeBoundsRequest.getEditParts().get(0);
    }
    // prepare location
    Point location;
    {
      IDropRequest dropRequest = (IDropRequest) request;
      location = dropRequest.getLocation().getCopy();
    }
    // prepare target header
    RowHeaderEditPart target = null;
    {
      for (EditPart editPart : getHost().getChildren()) {
        RowHeaderEditPart rowEditPart = (RowHeaderEditPart) editPart;
        Rectangle bounds = rowEditPart.getFigure().getBounds();
        if (location.y < bounds.getCenter().y) {
          target = rowEditPart;
          break;
        }
      }
    }
    // prepare grid information
    IGridInfo gridInfo = m_layout.getGridInfo();
    Interval[] columnIntervals = gridInfo.getColumnIntervals();
    Interval[] rowIntervals = gridInfo.getRowIntervals();
    int x1 = columnIntervals[0].begin - 5;
    int x2 = columnIntervals[columnIntervals.length - 1].end() + 5;
    // prepare index of target column and position for insert feedbacks
    int index;
    int y;
    int size = AbstractGridLayoutEditPolicy.INSERT_ROW_SIZE;
    if (target != null) {
      index = target.getIndex();
      // set default
      y = rowIntervals[index].begin - size / 2;
      // check for gap
      FormRowInfo row = m_layout.getRows().get(index);
      if (row.isGap()) {
        y = rowIntervals[index].begin;
        size = rowIntervals[index].length + 1;
      } else if (index != 0) {
        FormRowInfo prevRow = m_layout.getRows().get(index - 1);
        if (prevRow.isGap()) {
          y = rowIntervals[index - 1].begin;
          size = rowIntervals[index - 1].length + 1;
          index--;
        }
      }
    } else {
      index = m_layout.getRows().size();
      m_mainPolicy.showInsertFeedbacks(null, null);
      //
      y = rowIntervals[rowIntervals.length - 1].end() - size / 2;
    }
    // show insert feedbacks
    {
      // ...on main viewer
      m_mainPolicy.showInsertFeedbacks(new Rectangle(x1, y, x2 - x1, size), null);
      // ...on header viewer
      {
        if (m_insertFeedback.getParent() == null) {
          addFeedback(m_insertFeedback);
        }
        //
        Point offset = headerEditPart.getOffset();
        Rectangle bounds = new Rectangle(0, y + offset.y, getHostFigure().getSize().width, size);
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
        Point feedbackLocation = new Point(10, location.y + 10);
        FigureUtils.translateAbsoluteToFigure(feedbackLayer, feedbackLocation);
        m_feedback.setLocation(feedbackLocation);
      }
      // set text
      m_feedback.setText(MessageFormat.format(
          GefMessages.RowsLayoutEditPolicy_feedbackPattern,
          1 + index));
    }
    // prepare command
    {
      final FormRowInfo row = headerEditPart.getDimension();
      final int targetIndex = index;
      m_moveCommand = new EditCommand(m_layout) {
        @Override
        protected void executeEdit() throws Exception {
          m_layout.command_MOVE_ROW(m_layout.getRows().indexOf(row), targetIndex);
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
