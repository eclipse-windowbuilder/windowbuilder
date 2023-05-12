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
package org.eclipse.wb.internal.swing.MigLayout.gef.header.layout;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.gef.figure.TextFeedback;
import org.eclipse.wb.core.gef.header.AbstractHeaderLayoutEditPolicy;
import org.eclipse.wb.core.gef.policy.layout.grid.AbstractGridLayoutEditPolicy;
import org.eclipse.wb.core.gef.policy.layout.grid.IGridInfo;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.draw2d.Layer;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.core.requests.IDropRequest;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.swing.MigLayout.gef.GefMessages;
import org.eclipse.wb.internal.swing.MigLayout.gef.MigLayoutEditPolicy;
import org.eclipse.wb.internal.swing.MigLayout.gef.header.edit.RowHeaderEditPart;
import org.eclipse.wb.internal.swing.MigLayout.gef.header.selection.RowSelectionEditPolicy;
import org.eclipse.wb.internal.swing.MigLayout.model.MigLayoutInfo;

import org.eclipse.draw2d.geometry.Interval;
import org.eclipse.draw2d.geometry.Point;

/**
 * Implementation of {@link LayoutEditPolicy} for {@link RowHeaderEditPart}.
 *
 * @author scheglov_ke
 * @coverage swing.MigLayout.header
 */
public final class RowsLayoutEditPolicy extends AbstractHeaderLayoutEditPolicy {
  private final MigLayoutEditPolicy m_mainPolicy;
  private final MigLayoutInfo m_layout;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public RowsLayoutEditPolicy(MigLayoutEditPolicy mainPolicy, MigLayoutInfo layout) {
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
    int x1 = columnIntervals[0].begin() - 5;
    int x2 = columnIntervals[columnIntervals.length - 1].end() + 5;
    // prepare index of target column and position for insert feedbacks
    final int targetIndex;
    int y;
    int size = AbstractGridLayoutEditPolicy.INSERT_ROW_SIZE;
    if (target != null) {
      targetIndex = target.getIndex();
      y = rowIntervals[targetIndex].begin() - size / 2;
      if (targetIndex != 0) {
        y -= (rowIntervals[targetIndex].begin() - rowIntervals[targetIndex - 1].end()) / 2;
      }
    } else {
      targetIndex = m_layout.getRows().size();
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
        // set bounds
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
      m_feedback.setText(GefMessages.RowsLayoutEditPolicy_rowPrefix + targetIndex);
    }
    // prepare command
    {
      final int sourceIndex = headerEditPart.getIndex();
      m_moveCommand = new EditCommand(m_layout) {
        @Override
        protected void executeEdit() throws Exception {
          m_layout.moveRow(sourceIndex, targetIndex);
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
