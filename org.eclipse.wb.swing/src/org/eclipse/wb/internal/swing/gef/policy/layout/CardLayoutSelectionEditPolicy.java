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
package org.eclipse.wb.internal.swing.gef.policy.layout;

import com.google.common.collect.Lists;

import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.draw2d.ICursorConstants;
import org.eclipse.wb.draw2d.IPositionConstants;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.graphical.handles.Handle;
import org.eclipse.wb.gef.graphical.handles.MoveHandle;
import org.eclipse.wb.gef.graphical.handles.ResizeHandle;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;
import org.eclipse.wb.gef.graphical.tools.ResizeTracker;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.layout.CardLayoutInfo;

import java.util.List;

/**
 * Implementation of {@link SelectionLayoutEditPolicy} for {@link CardLayoutInfo}.
 * 
 * @author lobas_av
 * @coverage swing.gef.policy
 */
public final class CardLayoutSelectionEditPolicy extends SelectionEditPolicy {
  private final CardLayoutInfo m_layout;
  private CardNavigationFigure m_navigationFigure;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CardLayoutSelectionEditPolicy(CardLayoutInfo layout) {
    m_layout = layout;
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
    handles.add(createHandle(IPositionConstants.SOUTH_EAST));
    handles.add(createHandle(IPositionConstants.SOUTH_WEST));
    handles.add(createHandle(IPositionConstants.NORTH_WEST));
    handles.add(createHandle(IPositionConstants.NORTH_EAST));
    return handles;
  }

  /**
   * @return the {@link ResizeHandle} for given direction.
   */
  private Handle createHandle(int direction) {
    ResizeHandle handle = new ResizeHandle(getHost(), direction);
    ResizeTracker tracker = new ResizeTracker(direction, null);
    tracker.setDefaultCursor(ICursorConstants.SIZEALL);
    handle.setDragTrackerTool(tracker);
    handle.setCursor(ICursorConstants.SIZEALL);
    return handle;
  }

  @Override
  protected void showSelection() {
    super.showSelection();
    // add navigate feedback
    if (m_navigationFigure == null) {
      m_navigationFigure = new CardNavigationFigure(this);
      Figure hostFigure = getHostFigure();
      Rectangle bounds = hostFigure.getBounds().getCopy();
      FigureUtils.translateFigureToAbsolute(hostFigure, bounds);
      m_navigationFigure.setBounds(new Rectangle(bounds.right()
          - CardNavigationFigure.WIDTH
          * 2
          - 3,
          bounds.y - CardNavigationFigure.HEIGHT / 2,
          CardNavigationFigure.WIDTH * 2,
          CardNavigationFigure.HEIGHT));
      addFeedback(m_navigationFigure);
    }
  }

  @Override
  protected void hideSelection() {
    super.hideSelection();
    // remove navigate feedback
    if (m_navigationFigure != null) {
      removeFeedback(m_navigationFigure);
      m_navigationFigure = null;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Selection
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets show previous component relative of current.
   */
  public void showPrevComponent() {
    IEditPartViewer viewer = getHost().getViewer();
    // show previous component
    ComponentInfo component = m_layout.getPrevComponent();
    m_layout.show(component);
    // select EditPart
    EditPart editPart = viewer.getEditPartByModel(component);
    viewer.select(editPart);
  }

  /**
   * Sets show next component relative of current.
   */
  public void showNextComponent() {
    IEditPartViewer viewer = getHost().getViewer();
    // show next component
    ComponentInfo component = m_layout.getNextComponent();
    m_layout.show(component);
    // select EditPart
    EditPart editPart = viewer.getEditPartByModel(component);
    viewer.select(editPart);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Request
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public EditPart getTargetEditPart(Request request) {
    if (Request.REQ_SELECTION.equals(request.getType())) {
      ComponentInfo component = m_layout.getCurrentComponent();
      return getHost().getViewer().getEditPartByModel(component);
    }
    return null;
  }
}