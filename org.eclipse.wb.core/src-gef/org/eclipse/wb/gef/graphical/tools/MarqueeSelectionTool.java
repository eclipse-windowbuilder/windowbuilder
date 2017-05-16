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
package org.eclipse.wb.gef.graphical.tools;

import com.google.common.collect.Lists;

import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.draw2d.Graphics;
import org.eclipse.wb.draw2d.IColorConstants;
import org.eclipse.wb.draw2d.ICursorConstants;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;

import java.util.ArrayList;
import java.util.List;

/**
 * A Tool which selects multiple objects inside a rectangular area of a Graphical Viewer. If the
 * SHIFT key is pressed at the beginning of the drag, the enclosed items will be appended to the
 * current selection. If the CONTROL key is pressed at the beginning of the drag, the enclosed items
 * will have their selection state inverted.
 * <P>
 * By default, only {@link EditPart}'s whose figure's are on the primary layer will be considered
 * within the enclosed rectangle.
 *
 * @author lobas_av
 * @coverage gef.graphical
 */
public class MarqueeSelectionTool extends Tool {
  private static final int TOGGLE_MODE = 1;
  private static final int APPEND_MODE = 2;
  private static final Request REQUEST = new Request(Request.REQ_SELECTION);
  //
  private int m_selectionMode;
  private List<EditPart> m_allChildren;
  private List<EditPart> m_selectedEditParts;
  private Figure m_marqueeFeedbackFigure;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public MarqueeSelectionTool() {
    setDefaultCursor(ICursorConstants.CROSS);
    setDisabledCursor(ICursorConstants.NO);
    setUnloadWhenFinished(false);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tool
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void deactivate() {
    if (m_state == STATE_DRAG_IN_PROGRESS) {
      eraseMarqueeFeedback();
      eraseTargetFeedback();
    }
    super.deactivate();
    m_allChildren = null;
    m_selectedEditParts = null;
    m_state = STATE_NONE;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Cursor
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Cursor calculateCursor() {
    switch (m_state) {
      case STATE_INIT :
      case STATE_DRAG :
      case STATE_DRAG_IN_PROGRESS :
        return getDefaultCursor();
      case STATE_INVALID :
        return getDisabledCursor();
      default :
        return super.calculateCursor();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // High-Level handle MouseEvent
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void handleButtonDown(int button) {
    if (button == 1) {
      if (m_state == STATE_INIT) {
        m_state = STATE_DRAG_IN_PROGRESS;
        if ((m_stateMask & SWT.CONTROL) != 0) {
          m_selectionMode = TOGGLE_MODE;
        } else if ((m_stateMask & SWT.SHIFT) != 0) {
          m_selectionMode = APPEND_MODE;
        }
      }
    } else {
      m_state = STATE_INVALID;
      eraseTargetFeedback();
      eraseMarqueeFeedback();
    }
    refreshCursor();
  }

  @Override
  protected void handleButtonUp(int button) {
    if (m_state == STATE_DRAG_IN_PROGRESS) {
      m_state = STATE_NONE;
      eraseTargetFeedback();
      eraseMarqueeFeedback();
      performMarqueeSelect();
    }
    //
    //setUnloadWhenFinished(false);
    handleFinished();
  }

  @Override
  protected void handleDragInProgress() {
    if (m_state == STATE_DRAG || m_state == STATE_DRAG_IN_PROGRESS) {
      showMarqueeFeedback();
      eraseTargetFeedback();
      m_selectedEditParts = calculateNewSelection();
      showTargetFeedback();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Selection
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * If in multi select mode, add the new selections to the already selected group; otherwise, clear
   * the selection and select the new group
   */
  private void performMarqueeSelect() {
    IEditPartViewer viewer = getViewer();
    List<EditPart> newSelections = calculateNewSelection();
    //
    if (m_selectionMode == APPEND_MODE) {
      // append all new selection elements
      for (EditPart editPart : newSelections) {
        viewer.appendSelection(editPart);
      }
    } else if (m_selectionMode == TOGGLE_MODE) {
      List<EditPart> selected = new ArrayList<EditPart>(viewer.getSelectedEditParts());
      //
      for (EditPart editPart : newSelections) {
        if (editPart.getSelected() == EditPart.SELECTED_NONE) {
          selected.add(editPart);
        } else {
          selected.remove(editPart);
        }
      }
      //
      viewer.setSelection(selected);
    } else {
      // replace selection to all new selection elements
      viewer.setSelection(newSelections);
    }
  }

  /**
   * Calculate new selections based on which children fall inside the marquee selection rectangle.
   * Do not select children who are not visible.
   */
  private List<EditPart> calculateNewSelection() {
    List<EditPart> newSelections = Lists.newArrayList();
    // loop of all editparts
    for (EditPart editPart : getAllChildren()) {
      if (!editPart.isSelectable()) {
        continue;
      }
      // prepare figure info
      GraphicalEditPart graphicalPart = (GraphicalEditPart) editPart;
      Figure figure = graphicalPart.getFigure();
      Rectangle r = figure.getBounds().getCopy();
      FigureUtils.translateFigureToAbsolute(figure, r);
      // compare bounds and selection bounds
      Rectangle marqueeSelectionRectangle = getMarqueeSelectionRectangle();
      if (marqueeSelectionRectangle.contains(r.getTopLeft())
          && marqueeSelectionRectangle.contains(r.getBottomRight())
          && figure.isVisible()
          && editPart.getTargetEditPart(REQUEST) == editPart) {
        newSelections.add(editPart);
      }
    }
    return newSelections;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Target Feedback
  //
  ////////////////////////////////////////////////////////////////////////////
  private void showTargetFeedback() {
    for (EditPart editPart : m_selectedEditParts) {
      editPart.showTargetFeedback(REQUEST);
    }
  }

  private void eraseTargetFeedback() {
    if (m_selectedEditParts != null) {
      for (EditPart editPart : m_selectedEditParts) {
        editPart.eraseTargetFeedback(REQUEST);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Marquee Feedback
  //
  ////////////////////////////////////////////////////////////////////////////
  private void showMarqueeFeedback() {
    // check create feedback figure
    if (m_marqueeFeedbackFigure == null) {
      m_marqueeFeedbackFigure = new Figure() {
        @Override
        protected void paintClientArea(Graphics graphics) {
          graphics.setLineStyle(SWT.LINE_DASHDOT);
          graphics.setXORMode(true);
          Rectangle r = getClientArea();
          graphics.drawRectangle(0, 0, r.width - 1, r.height - 1);
        }
      };
      m_marqueeFeedbackFigure.setForeground(IColorConstants.white);
      m_marqueeFeedbackFigure.setBackground(IColorConstants.black);
      getFeedbackPane().add(m_marqueeFeedbackFigure);
    }
    // update feedback figure
    Rectangle bounds = getMarqueeSelectionRectangle();
    FigureUtils.translateAbsoluteToFigure(m_marqueeFeedbackFigure, bounds);
    m_marqueeFeedbackFigure.setBounds(bounds);
  }

  private void eraseMarqueeFeedback() {
    if (m_marqueeFeedbackFigure != null) {
      getFeedbackPane().remove(m_marqueeFeedbackFigure);
      m_marqueeFeedbackFigure = null;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Returns feedback layer.
   */
  private Figure getFeedbackPane() {
    return getViewer().getLayer(IEditPartViewer.FEEDBACK_LAYER);
  }

  /**
   * Returns rectangular area between left-top corner <code>(m_startX, m_startY)</code> and
   * bottom-right corner <code>(m_currentX, m_currentY)</code>.
   */
  private Rectangle getMarqueeSelectionRectangle() {
    return new Rectangle(getStartLocation(), getLocation());
  }

  /**
   * Return a {@link List} including all of the children of the root {@link EditPart}.
   */
  private List<EditPart> getAllChildren() {
    if (m_allChildren == null || m_allChildren.isEmpty()) {
      m_allChildren = Lists.newArrayList();
      getAllChildren(m_allChildren, getViewer().getRootEditPart());
    }
    return m_allChildren;
  }

  /**
   * Returns a {@link List} including all of the children of the {@link EditPart} passed in.
   */
  private static void getAllChildren(List<EditPart> children, EditPart editPart) {
    for (EditPart childEditPart : editPart.getChildren()) {
      children.add(childEditPart);
      getAllChildren(children, childEditPart);
    }
  }
}