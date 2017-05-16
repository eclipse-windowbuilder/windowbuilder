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
package org.eclipse.wb.core.gef.policy.layout.position;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.gef.figure.AbstractPositionFeedback;
import org.eclipse.wb.core.gef.figure.GhostPositionFeedback;
import org.eclipse.wb.core.gef.figure.TextFeedback;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.draw2d.Layer;
import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.core.requests.CreateRequest;
import org.eclipse.wb.gef.core.requests.IDropRequest;
import org.eclipse.wb.gef.core.requests.PasteRequest;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.core.DesignerPlugin;

import java.util.List;

/**
 * Implementation of {@link LayoutEditPolicy} for placing children on fixed areas on parent.
 *
 * @author scheglov_ke
 * @coverage core.gef.policy
 */
public abstract class AbstractPositionLayoutEditPolicy extends LayoutEditPolicy {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Feedbacks
  //
  ////////////////////////////////////////////////////////////////////////////
  private TextFeedback m_hintFeedback;
  private List<AbstractPositionFeedback> m_feedbacks;
  private AbstractPositionFeedback m_activeFeedback;

  @Override
  protected final void showLayoutTargetFeedback(Request request) {
    super.showLayoutTargetFeedback(request);
    // create feedbacks
    if (m_feedbacks == null) {
      // create positions
      m_feedbacks = Lists.newArrayList();
      try {
        addFeedbacks();
      } catch (Throwable e) {
        DesignerPlugin.log(e);
      }
      // create hint
      m_hintFeedback = new TextFeedback(getFeedbackLayer());
      m_hintFeedback.add();
    }
    // highlight feedback
    m_activeFeedback = null;
    Point location = ((IDropRequest) request).getLocation();
    for (AbstractPositionFeedback feedback : m_feedbacks) {
      if (feedback.update(location)) {
        m_activeFeedback = feedback;
      }
    }
    // update hint
    {
      if (m_activeFeedback != null) {
        m_hintFeedback.setText(m_activeFeedback.getHint());
      } else {
        m_hintFeedback.setText("<Unknown position>");
      }
      Rectangle target = getHostFigure().getBounds().getCopy();
      FigureUtils.translateFigureToFigure(getHostFigure(), getFeedbackLayer(), target);
      m_hintFeedback.centerHorizontallyAbove(target, 10);
    }
  }

  @Override
  protected final void eraseLayoutTargetFeedback(Request request) {
    super.eraseLayoutTargetFeedback(request);
    if (m_feedbacks != null) {
      // remove positions
      for (AbstractPositionFeedback feedback : m_feedbacks) {
        feedback.remove();
      }
      m_feedbacks = null;
      // remove hint
      m_hintFeedback.remove();
      m_hintFeedback = null;
    }
  }

  /**
   * Adds single feedback with given parameters.
   *
   * @param bounds
   *          the host relative bounds of feedback, this method automatically will convert it into
   *          feedback layer coordinates.
   * @param data
   *          the {@link Object} associated with this feedback, it will be returned back in
   *          {@link Command} related methods.
   */
  protected final void addFeedback(Rectangle bounds, String hint, Object data) {
    Layer layer = getFeedbackLayer();
    // convert bounds
    {
      bounds = bounds.getCopy();
      FigureUtils.translateFigureToFigure2(getHostFigure(), layer, bounds);
    }
    // add feedback
    {
      AbstractPositionFeedback feedback = new GhostPositionFeedback(layer, bounds, hint);
      feedback.setData(data);
      m_feedbacks.add(feedback);
    }
  }

  /**
   * Adds feedback that start at <code>(px1, py1)</code> and ends at <code>(px2, py2)</code> portion
   * of host size.
   */
  protected final void addFeedback(double px1,
      double py1,
      double px2,
      double py2,
      Insets insets,
      String hint,
      Object data) {
    Rectangle area = getHostFigure().getClientArea();
    double offset_x1 = getOffset(area.width, px1);
    double offset_x2 = getOffset(area.width, px2);
    double offset_y1 = getOffset(area.height, py1);
    double offset_y2 = getOffset(area.height, py2);
    int x1 = (int) (area.x + offset_x1) + insets.left;
    int y1 = (int) (area.y + offset_y1) + insets.top;
    int x2 = (int) (area.x + offset_x2) - insets.right;
    int y2 = (int) (area.y + offset_y2) - insets.bottom;
    addFeedback(new Rectangle(x1, y1, x2 - x1, y2 - y1), hint, data);
  }

  /**
   * @return offset as percent of size (<= 1.0) or absolute value.
   */
  private static double getOffset(int size, double offsetPercent) {
    if (offsetPercent > 1) {
      return offsetPercent;
    } else {
      return size * offsetPercent;
    }
  }

  /**
   * Adds feedbacks using {@link #addFeedback(Rectangle, String, Object)}.
   */
  protected abstract void addFeedbacks() throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Command getCommand(Request request) {
    if (m_activeFeedback != null) {
      Command command = getCommand(request, m_activeFeedback.getData());
      if (command != null) {
        return command;
      }
    }
    return super.getCommand(request);
  }

  /**
   * @return the {@link Command} for given {@link Request}.
   */
  protected Command getCommand(Request request, Object data) {
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands: create
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected final Command getCreateCommand(CreateRequest request) {
    if (m_activeFeedback != null) {
      return getCreateCommand(request.getNewObject(), m_activeFeedback.getData());
    }
    return null;
  }

  /**
   * @return the {@link Command} for {@link Request#REQ_CREATE}.
   */
  protected abstract Command getCreateCommand(Object newObject, Object data);

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands: paste
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected final Command getPasteCommand(PasteRequest request) {
    if (m_activeFeedback != null) {
      return getPasteCommand(request, m_activeFeedback.getData());
    }
    return null;
  }

  /**
   * @return the {@link Command} for {@link Request#REQ_PASTE}.
   */
  protected abstract Command getPasteCommand(PasteRequest request, Object data);

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands: move
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected final Command getMoveCommand(ChangeBoundsRequest request) {
    if (m_activeFeedback != null && request.getEditParts().size() == 1) {
      EditPart editPart = request.getEditParts().get(0);
      return getMoveCommand(editPart.getModel(), m_activeFeedback.getData());
    }
    return null;
  }

  /**
   * @return the {@link Command} for {@link Request#REQ_MOVE}.
   */
  protected abstract Command getMoveCommand(Object moveObject, Object data);

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands: add
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected final Command getAddCommand(ChangeBoundsRequest request) {
    if (m_activeFeedback != null && request.getEditParts().size() == 1) {
      EditPart editPart = request.getEditParts().get(0);
      return getAddCommand(editPart.getModel(), m_activeFeedback.getData());
    }
    return null;
  }

  /**
   * @return the {@link Command} for {@link Request#REQ_ADD}.
   */
  protected abstract Command getAddCommand(Object addObject, Object data);
}
