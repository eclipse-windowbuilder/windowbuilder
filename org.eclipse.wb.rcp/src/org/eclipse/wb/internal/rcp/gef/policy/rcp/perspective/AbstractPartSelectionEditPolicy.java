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
package org.eclipse.wb.internal.rcp.gef.policy.rcp.perspective;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.draw2d.IColorConstants;
import org.eclipse.wb.draw2d.ICursorConstants;
import org.eclipse.wb.draw2d.ILocator;
import org.eclipse.wb.draw2d.RectangleFigure;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.graphical.handles.Handle;
import org.eclipse.wb.gef.graphical.handles.MoveHandle;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;
import org.eclipse.wb.gef.graphical.tools.ResizeTracker;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.AbstractPartInfo;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.SashLineInfo;

import java.util.List;

/**
 * {@link SelectionEditPolicy} for {@link AbstractPartInfo}, that shows simple rectangle selection
 * around {@link EditPart} and "static" resize {@link Handle}.
 * 
 * @author scheglov_ke
 * @coverage rcp.gef.policy
 */
public final class AbstractPartSelectionEditPolicy extends SelectionEditPolicy {
  private static final String REQ_RESIZE = "resize";
  private final AbstractPartInfo m_part;
  private final SashLineInfo m_line;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractPartSelectionEditPolicy(AbstractPartInfo part) {
    m_part = part;
    m_line = part.getSashLine();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Handles
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected List<Handle> createSelectionHandles() {
    List<Handle> handles = Lists.newArrayList();
    // create move column handle
    MoveHandle moveHandle = new MoveHandle(getHost());
    moveHandle.setForeground(IColorConstants.red);
    handles.add(moveHandle);
    //
    return handles;
  }

  @Override
  protected List<Handle> createStaticHandles() {
    if (m_line == null) {
      return ImmutableList.of();
    }
    // prepare handle
    Handle resizeHandle = new Handle(getHost(), new ILocator() {
      public void relocate(Figure target) {
        // prepare bounds (relative to page)
        Rectangle bounds = m_line.getBounds().getCopy();
        if (m_line.isHorizontal()) {
          bounds.expand(6, 0);
        } else {
          bounds.expand(0, 6);
        }
        // set bounds relative to layer
        Figure pageFigure = getHostFigure().getParent();
        FigureUtils.translateFigureToAbsolute2(pageFigure, bounds);
        target.setBounds(bounds);
      }
    }) {
    };
    // set cursor
    if (m_line.isHorizontal()) {
      resizeHandle.setCursor(ICursorConstants.SIZEE);
    } else {
      resizeHandle.setCursor(ICursorConstants.SIZEN);
    }
    // single static handle
    resizeHandle.setDragTrackerTool(new ResizeTracker(getHost(), m_line.getPosition(), REQ_RESIZE));
    return ImmutableList.of(resizeHandle);
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
  public Command getCommand(final Request request) {
    return getResizeCommand((ChangeBoundsRequest) request);
  }

  @Override
  public void showSourceFeedback(Request request) {
    showResizeFeedback((ChangeBoundsRequest) request);
  }

  @Override
  public void eraseSourceFeedback(Request request) {
    eraseResizeFeedback((ChangeBoundsRequest) request);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Resize
  //
  ////////////////////////////////////////////////////////////////////////////
  private Figure m_resizeFeedback;

  private Command getResizeCommand(final ChangeBoundsRequest request) {
    return new EditCommand(m_part) {
      @Override
      protected void executeEdit() throws Exception {
        int delta;
        if (m_line.isHorizontal()) {
          delta = request.getSizeDelta().width;
        } else {
          delta = request.getSizeDelta().height;
        }
        // do resize
        m_part.resize(delta);
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
    }
    // prepare bounds XXX
    Rectangle bounds;
    {
      Figure hostFigure = getHostFigure();
      bounds = m_line.getPartBounds().getCopy();
      bounds = request.getTransformedRectangle(bounds);
      FigureUtils.translateFigureToAbsolute(hostFigure, bounds.shrink(-1, -1));
    }
    // update selection feedback
    m_resizeFeedback.setBounds(bounds);
  }

  private void eraseResizeFeedback(ChangeBoundsRequest request) {
    // erase selection feedback
    removeFeedback(m_resizeFeedback);
    m_resizeFeedback = null;
  }
}
