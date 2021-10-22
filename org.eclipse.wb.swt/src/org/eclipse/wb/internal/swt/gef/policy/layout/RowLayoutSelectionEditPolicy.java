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
package org.eclipse.wb.internal.swt.gef.policy.layout;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.gef.figure.TextFeedback;
import org.eclipse.wb.core.gef.policy.PolicyUtils;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.draw2d.IColorConstants;
import org.eclipse.wb.draw2d.IPositionConstants;
import org.eclipse.wb.draw2d.RectangleFigure;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.graphical.handles.Handle;
import org.eclipse.wb.gef.graphical.handles.MoveHandle;
import org.eclipse.wb.gef.graphical.handles.ResizeHandle;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;
import org.eclipse.wb.gef.graphical.tools.ResizeTracker;
import org.eclipse.wb.internal.swt.model.layout.IRowDataInfo;
import org.eclipse.wb.internal.swt.model.layout.IRowLayoutInfo;
import org.eclipse.wb.internal.swt.model.layout.RowLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.IControlInfo;

import java.util.List;

/**
 * {@link SelectionEditPolicy} for {@link RowLayoutInfo}.
 *
 * @author lobas_av
 * @coverage swt.gef.policy
 */
public final class RowLayoutSelectionEditPolicy<C extends IControlInfo> extends SelectionEditPolicy {
  private static final String REQ_RESIZE = "resize";
  private final IRowLayoutInfo<C> m_layout;
  private final C m_control;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public RowLayoutSelectionEditPolicy(IRowLayoutInfo<C> layout, C control) {
    m_layout = layout;
    m_control = control;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Handles
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected List<Handle> createSelectionHandles() {
    List<Handle> handles = Lists.newArrayList();
    handles.add(new MoveHandle(getHost()));
    handles.add(createHandle(IPositionConstants.EAST));
    handles.add(createHandle(IPositionConstants.SOUTH));
    handles.add(createHandle(IPositionConstants.SOUTH_EAST));
    return handles;
  }

  /**
   * @return the {@link ResizeHandle} for given direction.
   */
  private Handle createHandle(int direction) {
    ResizeHandle handle = new ResizeHandle(getHost(), direction);
    handle.setDragTrackerTool(new ResizeTracker(direction, REQ_RESIZE));
    return handle;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Routing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean understandsRequest(Request request) {
    return super.understandsRequest(request) || request.getType() == REQ_RESIZE;
  }

  @Override
  public Command getCommand(Request request) {
    if (request instanceof ChangeBoundsRequest) {
      return getResizeCommand((ChangeBoundsRequest) request);
    }
    return null;
  }

  @Override
  public void showSourceFeedback(Request request) {
    if (request instanceof ChangeBoundsRequest) {
      showResizeFeedback((ChangeBoundsRequest) request);
    }
  }

  @Override
  public void eraseSourceFeedback(Request request) {
    if (request instanceof ChangeBoundsRequest) {
      eraseResizeFeedback((ChangeBoundsRequest) request);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Resize
  //
  ////////////////////////////////////////////////////////////////////////////
  private Figure m_resizeFeedback;
  private TextFeedback m_textFeedback;

  private Command getResizeCommand(ChangeBoundsRequest request) {
    final int resizeDirection = request.getResizeDirection();
    final Rectangle newBounds = request.getTransformedRectangle(getHost().getFigure().getBounds());
    return new EditCommand(m_control) {
      @Override
      protected void executeEdit() throws Exception {
        IRowDataInfo rowData = m_layout.getRowData2(m_control);
        if (PolicyUtils.hasDirection(resizeDirection, IPositionConstants.EAST)) {
          rowData.setWidth(newBounds.width);
        }
        if (PolicyUtils.hasDirection(resizeDirection, IPositionConstants.SOUTH)) {
          rowData.setHeight(newBounds.height);
        }
      }
    };
  }

  private void showResizeFeedback(ChangeBoundsRequest request) {
    if (m_resizeFeedback == null) {
      // create selection feedback
      {
        m_resizeFeedback = new RectangleFigure();
        m_resizeFeedback.setForeground(IColorConstants.red);
        addFeedback(m_resizeFeedback);
      }
      // create text feedback
      {
        m_textFeedback = new TextFeedback(getFeedbackLayer());
        m_textFeedback.add();
      }
    }
    // prepare bounds
    Rectangle bounds;
    {
      Figure hostFigure = getHostFigure();
      bounds = request.getTransformedRectangle(hostFigure.getBounds());
      FigureUtils.translateFigureToAbsolute(hostFigure, bounds);
    }
    // update selection feedback
    m_resizeFeedback.setBounds(bounds);
    // update text feedback
    m_textFeedback.setText(bounds.width + " x " + bounds.height);
    m_textFeedback.setLocation(request.getLocation().getTranslated(10, 10));
  }

  private void eraseResizeFeedback(ChangeBoundsRequest request) {
    // erase selection feedback
    removeFeedback(m_resizeFeedback);
    m_resizeFeedback = null;
    // erase text feedback
    m_textFeedback.remove();
    m_textFeedback = null;
  }
}