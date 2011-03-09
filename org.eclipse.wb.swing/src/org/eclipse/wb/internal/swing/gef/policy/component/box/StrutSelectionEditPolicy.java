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
package org.eclipse.wb.internal.swing.gef.policy.component.box;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.gef.figure.TextFeedback;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.draw2d.IColorConstants;
import org.eclipse.wb.draw2d.RectangleFigure;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.graphical.handles.Handle;
import org.eclipse.wb.gef.graphical.handles.MoveHandle;
import org.eclipse.wb.gef.graphical.handles.SideResizeHandle;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;
import org.eclipse.wb.gef.graphical.tools.ResizeTracker;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.layout.BoxSupport;

import java.util.List;

import javax.swing.Box;

/**
 * Abstract {@link SelectionEditPolicy} for any strut or rigid area from {@link Box}.
 * 
 * @author scheglov_ke
 * @coverage swing.gef.policy
 */
abstract class StrutSelectionEditPolicy extends SelectionEditPolicy {
  private static final String REQ_RESIZE = "resize";
  private final ComponentInfo m_strut;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public StrutSelectionEditPolicy(ComponentInfo strut) {
    m_strut = strut;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Handles
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected List<Handle> createSelectionHandles() {
    List<Handle> handles = Lists.newArrayList();
    // create move handle
    MoveHandle moveHandle = new MoveHandle(getHost());
    moveHandle.setForeground(IColorConstants.red);
    handles.add(moveHandle);
    //
    return handles;
  }

  /**
   * @return the resize {@link Handle} located on given side.
   */
  protected Handle createResizeHandle(int handleSide, int resizeDirection) {
    SideResizeHandle resizeHandle = new SideResizeHandle(getHost(), handleSide, 5, true);
    resizeHandle.setDragTrackerTool(new ResizeTracker(getHost(), resizeDirection, REQ_RESIZE));
    return resizeHandle;
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
  private Figure m_sizeFeedback;
  private TextFeedback m_textFeedback;

  /**
   * @return the {@link Command} that resizes strut.
   */
  private Command getResizeCommand(ChangeBoundsRequest request) {
    final Rectangle newBounds = request.getTransformedRectangle(getHostFigure().getBounds());
    return new EditCommand(m_strut) {
      @Override
      protected void executeEdit() throws Exception {
        String source = getSource(m_strut, newBounds.width, newBounds.height);
        BoxSupport.setStrutSize(m_strut, source);
      }
    };
  }

  /**
   * Shows resize feedback.
   */
  private void showResizeFeedback(ChangeBoundsRequest request) {
    if (m_sizeFeedback == null) {
      // create size feedback
      {
        m_sizeFeedback = new RectangleFigure();
        m_sizeFeedback.setForeground(IColorConstants.red);
        addFeedback(m_sizeFeedback);
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
      FigureUtils.translateFigureToAbsolute(hostFigure, bounds.shrink(-1, -1));
    }
    // update size feedback
    m_sizeFeedback.setBounds(bounds);
    // update text feedback
    {
      String tooltip = getTooltip(bounds.width - 2, bounds.height - 2);
      m_textFeedback.setText(tooltip);
      m_textFeedback.setLocation(request.getLocation().getTranslated(10, 10));
    }
  }

  /**
   * Erases resize feedback.
   */
  private void eraseResizeFeedback(ChangeBoundsRequest request) {
    // erase size feedback
    removeFeedback(m_sizeFeedback);
    m_sizeFeedback = null;
    // erase text feedback
    m_textFeedback.remove();
    m_textFeedback = null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Abstract
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the text to display new size for user during resize.
   */
  protected abstract String getTooltip(int width, int height);

  /**
   * @return the source to set as factory argument, such that size of strut is same as in given.
   */
  protected abstract String getSource(ComponentInfo strut, int width, int height) throws Exception;
}