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
package org.eclipse.wb.core.gef.policy.selection;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.core.model.ITopBoundsSupport;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.draw2d.IPositionConstants;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.graphical.handles.Handle;
import org.eclipse.wb.gef.graphical.handles.MoveHandle;
import org.eclipse.wb.gef.graphical.handles.ResizeHandle;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;
import org.eclipse.wb.gef.graphical.tools.ResizeTracker;

import java.util.List;

/**
 * {@link SelectionEditPolicy} for top level {@link AbstractComponentInfo}.
 *
 * @author scheglov_ke
 * @coverage core.gef.policy
 */
public final class TopSelectionEditPolicy extends SelectionEditPolicy {
  private static final String REQ_RESIZE = "resize";
  private final IAbstractComponentInfo m_component;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TopSelectionEditPolicy(IAbstractComponentInfo component) {
    m_component = component;
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
    handles.add(createResizeHandle(IPositionConstants.EAST));
    handles.add(createResizeHandle(IPositionConstants.SOUTH_EAST));
    handles.add(createResizeHandle(IPositionConstants.SOUTH));
    return handles;
  }

  private Handle createResizeHandle(int direction) {
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
  //@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "BC_UNCONFIRMED_CAST")
  public Command getCommand(Request request) {
    return getResizeCommand((ChangeBoundsRequest) request);
  }

  @Override
  //@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "BC_UNCONFIRMED_CAST")
  public void showSourceFeedback(Request request) {
    showResizeFeedback((ChangeBoundsRequest) request);
  }

  @Override
  //@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "BC_UNCONFIRMED_CAST")
  public void eraseSourceFeedback(Request request) {
    eraseResizeFeedback((ChangeBoundsRequest) request);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Resize
  //
  ////////////////////////////////////////////////////////////////////////////
  private TopResizeFigure m_resizeFeedback;

  /**
   * @return the {@link Command} for resize.
   */
  private Command getResizeCommand(ChangeBoundsRequest request) {
    final Rectangle oldBounds = getHost().getFigure().getBounds();
    final Rectangle newBounds = request.getTransformedRectangle(oldBounds);
    sanitizeBounds(newBounds);
    return new EditCommand(m_component) {
      @Override
      protected void executeEdit() throws Exception {
        ITopBoundsSupport topBoundsSupport = m_component.getTopBoundsSupport();
        topBoundsSupport.setSize(newBounds.width, newBounds.height);
      }
    };
  }

  /**
   * Shows resize feedback.
   */
  protected void showResizeFeedback(ChangeBoundsRequest request) {
    if (m_resizeFeedback == null) {
      // create feedback
      {
        m_resizeFeedback = new TopResizeFigure();
        addFeedback(m_resizeFeedback);
      }
    }
    // update feedback
    {
      // prepare feedback bounds
      Rectangle bounds;
      {
        Figure hostFigure = getHostFigure();
        bounds = request.getTransformedRectangle(hostFigure.getBounds());
        sanitizeBounds(bounds);
        FigureUtils.translateFigureToAbsolute(hostFigure, bounds);
      }
      String sizeText =
          "[" + Integer.toString(bounds.width) + " x " + Integer.toString(bounds.height) + "]";
      m_resizeFeedback.setSizeText(sizeText);
      // set bounds for feedback
      m_resizeFeedback.setBounds(bounds);
    }
  }

  /**
   * Erases resize feedback.
   */
  private void eraseResizeFeedback(ChangeBoundsRequest request) {
    if (m_resizeFeedback != null) {
      removeFeedback(m_resizeFeedback);
      m_resizeFeedback = null;
    }
  }

  /**
   * Ensure that given {@link Rectangle} has reasonable width/height.
   */
  private static void sanitizeBounds(Rectangle bounds) {
    bounds.width = Math.max(bounds.width, 10);
    bounds.height = Math.max(bounds.height, 10);
  }
}