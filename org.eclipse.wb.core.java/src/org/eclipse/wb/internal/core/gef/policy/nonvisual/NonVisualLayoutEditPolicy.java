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
package org.eclipse.wb.internal.core.gef.policy.nonvisual;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.core.requests.CreateRequest;
import org.eclipse.wb.gef.core.requests.PasteRequest;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.gef.GefMessages;
import org.eclipse.wb.internal.core.gef.part.nonvisual.BeanFigure;
import org.eclipse.wb.internal.core.gef.part.nonvisual.NonVisualBeanEditPart;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;
import org.eclipse.wb.internal.core.model.nonvisual.NonVisualBeanContainerInfo;
import org.eclipse.wb.internal.core.model.nonvisual.NonVisualBeanInfo;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;

import org.apache.commons.lang.ClassUtils;

import java.util.Iterator;
import java.util.List;

/**
 * Implementation of {@link LayoutEditPolicy} for placing <i>non-visual beans</i>.
 *
 * @author lobas_av
 * @coverage core.gef.policy.nonvisual
 */
public final class NonVisualLayoutEditPolicy extends LayoutEditPolicy {
  private static final int SNAP_GRID_SIZE = 10;
  private final JavaInfo m_rootInfo;
  private final ILayoutRequestValidator m_validator;
  private Figure m_feedbackFigure;
  private Figure[] m_moveFeedbackFigures;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public NonVisualLayoutEditPolicy(JavaInfo rootInfo) {
    m_rootInfo = rootInfo;
    m_validator = new NonVisualValidator(m_rootInfo);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Validator
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected ILayoutRequestValidator getRequestValidator() {
    return m_validator;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Command getCreateCommand(final CreateRequest request) {
    final Point location = m_feedbackFigure.getLocation();
    return new EditCommand(m_rootInfo) {
      @Override
      protected void executeEdit() throws Exception {
        JavaInfo newInfo = (JavaInfo) request.getNewObject();
        NonVisualBeanContainerInfo.add(m_rootInfo, newInfo, location);
      }
    };
  }

  @Override
  protected Command getPasteCommand(final PasteRequest request) {
    final Point location = m_feedbackFigure.getLocation();
    return new EditCommand(m_rootInfo) {
      @Override
      @SuppressWarnings("unchecked")
      protected void executeEdit() throws Exception {
        List<JavaInfoMemento> mementos = (List<JavaInfoMemento>) request.getMemento();
        JavaInfoMemento memento = mementos.get(0);
        JavaInfo newInfo = memento.create(m_rootInfo);
        NonVisualBeanContainerInfo.add(m_rootInfo, newInfo, location);
        memento.apply();
      }
    };
  }

  @Override
  protected Command getMoveCommand(final ChangeBoundsRequest request) {
    return new EditCommand(m_rootInfo) {
      @Override
      protected void executeEdit() throws Exception {
        Point snapMoveDelta = applyGrid(request.getMoveDelta(), SNAP_GRID_SIZE);
        for (Iterator<?> I = request.getEditParts().iterator(); I.hasNext();) {
          NonVisualBeanEditPart part = (NonVisualBeanEditPart) I.next();
          NonVisualBeanInfo nonVisualBeanInfo = part.getNonVisualInfo();
          nonVisualBeanInfo.moveLocation(snapMoveDelta);
        }
      }
    };
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Feedback
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void showLayoutTargetFeedback(Request request) {
    Object type = request.getType();
    if (Request.REQ_CREATE.equals(type)) {
      showCreationFeedback((CreateRequest) request);
    } else if (Request.REQ_PASTE.equals(type)) {
      showPasteFeedback((PasteRequest) request);
    } else if (Request.REQ_MOVE.equals(type)) {
      showMoveFeedback((ChangeBoundsRequest) request);
    }
  }

  @Override
  protected void eraseLayoutTargetFeedback(Request request) {
    // remove simple feedback
    if (m_feedbackFigure != null) {
      removeFeedback(m_feedbackFigure);
      m_feedbackFigure = null;
    }
    // remove move feedback's
    if (m_moveFeedbackFigures != null) {
      for (Figure figure : m_moveFeedbackFigures) {
        removeFeedback(figure);
      }
      m_moveFeedbackFigures = null;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Create/Paste
  //
  ////////////////////////////////////////////////////////////////////////////
  private void showCreationFeedback(CreateRequest request) {
    JavaInfo newInfo = (JavaInfo) request.getNewObject();
    Point location = request.getLocation();
    showFeedback(GefMessages.NonVisualLayoutEditPolicy_newFeedback, newInfo, location);
  }

  @SuppressWarnings("unchecked")
  private void showPasteFeedback(PasteRequest request) {
    try {
      List<JavaInfoMemento> mementos = (List<JavaInfoMemento>) request.getMemento();
      JavaInfo newInfo = mementos.get(0).create(m_rootInfo);
      Point location = request.getLocation();
      showFeedback(GefMessages.NonVisualLayoutEditPolicy_copyFeedback, newInfo, location);
    } catch (Throwable e) {
      DesignerPlugin.log(e);
    }
  }

  private void showFeedback(String prefix, JavaInfo info, Point location) {
    // check create feedback
    if (m_feedbackFigure == null) {
      BeanFigure figure = new BeanFigure(info.getDescription().getIcon());
      Class<?> componentClass = info.getDescription().getComponentClass();
      figure.update(prefix + ClassUtils.getShortClassName(componentClass), location);
      //
      m_feedbackFigure = figure;
      addFeedback(figure);
    }
    // configure feedback location
    Rectangle bounds = m_feedbackFigure.getBounds();
    Point snapLocation = applyGrid(location, SNAP_GRID_SIZE);
    m_feedbackFigure.setLocation(new Point(snapLocation.x - bounds.width / 2, snapLocation.y
        - bounds.height));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Move
  //
  ////////////////////////////////////////////////////////////////////////////
  private void showMoveFeedback(ChangeBoundsRequest request) {
    List<EditPart> editParts = request.getEditParts();
    // check create feedback's
    if (m_moveFeedbackFigures == null) {
      m_moveFeedbackFigures = new Figure[editParts.size()];
      for (int i = 0; i < m_moveFeedbackFigures.length; i++) {
        NonVisualBeanEditPart part = (NonVisualBeanEditPart) editParts.get(i);
        final JavaInfo info = part.getNonVisualInfo().getJavaInfo();
        BeanFigure figure = new BeanFigure(info.getDescription().getIcon());
        String text = ExecutionUtils.runObjectLog(new RunnableObjectEx<String>() {
          public String runObject() throws Exception {
            return info.getVariableSupport().getTitle();
          }
        }, null);
        figure.update(text, request.getLocation());
        //
        m_moveFeedbackFigures[i] = figure;
        addFeedback(figure);
      }
    }
    // configure feedback's location
    Point snapMoveDelta = applyGrid(request.getMoveDelta(), SNAP_GRID_SIZE);
    for (int i = 0; i < m_moveFeedbackFigures.length; i++) {
      NonVisualBeanEditPart part = (NonVisualBeanEditPart) editParts.get(i);
      Point location = part.getNonVisualInfo().getLocation().getCopy();
      location.translate(snapMoveDelta);
      m_moveFeedbackFigures[i].setLocation(location);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Grid snapping for {@link Point}.
   */
  private static Point applyGrid(Point point, int step) {
    return new Point(applyGrid(point.x, step), applyGrid(point.y, step));
  }

  /**
   * Simple math round to nearest integer, based on grid step.
   */
  private static int applyGrid(int value, int step) {
    return value / step * step;
  }
}