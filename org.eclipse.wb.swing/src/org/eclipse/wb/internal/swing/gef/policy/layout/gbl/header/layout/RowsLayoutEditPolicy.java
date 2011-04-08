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
package org.eclipse.wb.internal.swing.gef.policy.layout.gbl.header.layout;

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
import org.eclipse.wb.internal.swing.gef.GefMessages;
import org.eclipse.wb.internal.swing.gef.policy.layout.gbl.GridBagLayoutEditPolicy;
import org.eclipse.wb.internal.swing.gef.policy.layout.gbl.header.edit.RowHeaderEditPart;
import org.eclipse.wb.internal.swing.gef.policy.layout.gbl.header.selection.RowSelectionEditPolicy;
import org.eclipse.wb.internal.swing.model.layout.gbl.AbstractGridBagLayoutInfo;
import org.eclipse.wb.internal.swing.model.layout.gbl.RowInfo;

import java.text.MessageFormat;
import java.util.List;

/**
 * {@link LayoutEditPolicy} for {@link RowHeaderEditPart}'s.
 * 
 * @author scheglov_ke
 * @coverage swing.gef.policy
 */
public final class RowsLayoutEditPolicy extends AbstractHeaderLayoutEditPolicy {
  private final GridBagLayoutEditPolicy m_mainPolicy;
  private final AbstractGridBagLayoutInfo m_layout;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public RowsLayoutEditPolicy(GridBagLayoutEditPolicy mainPolicy, AbstractGridBagLayoutInfo layout) {
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
      List<EditPart> children = getHost().getChildren();
      for (EditPart child : children) {
        RowHeaderEditPart rowEditPart = (RowHeaderEditPart) child;
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
    // prepare index of target row and position for insert feedbacks
    final int targetIndex;
    int y;
    int size;
    if (target != null) {
      targetIndex = target.getDimension().getIndex();
      // prepare previous interval
      Interval prevInterval;
      if (targetIndex == 0) {
        prevInterval = new Interval(0, 0);
      } else {
        prevInterval = rowIntervals[targetIndex - 1];
      }
      // prepare parameters
      int[] parameters =
          GridBagLayoutEditPolicy.getInsertFeedbackParameters(
              prevInterval,
              rowIntervals[targetIndex],
              AbstractGridLayoutEditPolicy.INSERT_ROW_SIZE);
      y = parameters[1];
      size = parameters[2] - parameters[1];
    } else {
      targetIndex = m_layout.getRows().size();
      m_mainPolicy.showInsertFeedbacks(null, null);
      // prepare parameters
      y = rowIntervals[rowIntervals.length - 1].end() + 1;
      size = AbstractGridLayoutEditPolicy.INSERT_ROW_SIZE;
    }
    // show insert feedbacks
    {
      // ...on main viewer
      m_mainPolicy.showInsertFeedbacks(null, new Rectangle(x1, y, x2 - x1, size));
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
        Point feedbackLocation = new Point(30, location.y + 10);
        FigureUtils.translateAbsoluteToFigure(feedbackLayer, feedbackLocation);
        m_feedback.setLocation(feedbackLocation);
      }
      // set text
      m_feedback.setText(MessageFormat.format(
          GefMessages.RowsLayoutEditPolicy_feedbackPattern,
          targetIndex));
    }
    // prepare command
    {
      RowInfo row = headerEditPart.getDimension();
      final int sourceIndex = row.getIndex();
      if (targetIndex == sourceIndex || targetIndex == sourceIndex + 1) {
        m_moveCommand = Command.EMPTY;
      } else {
        m_moveCommand = new EditCommand(m_layout) {
          @Override
          protected void executeEdit() throws Exception {
            m_layout.getRowOperations().move(sourceIndex, targetIndex);
          }
        };
      }
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
