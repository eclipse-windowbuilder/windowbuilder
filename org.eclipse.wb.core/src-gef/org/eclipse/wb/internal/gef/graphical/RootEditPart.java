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
package org.eclipse.wb.internal.gef.graphical;

import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.Layer;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.gef.graphical.tools.MarqueeSelectionTool;
import org.eclipse.wb.internal.draw2d.IRootFigure;
import org.eclipse.wb.internal.gef.core.IRootContainer;

/**
 * A {@link RootEditPart} is the <i>root</i> of an {@link IEditPartViewer}. It bridges the gap
 * between the {@link IEditPartViewer} and its contents. It does not correspond to anything in the
 * model, and typically can not be interacted with by the User. The Root provides a homogeneous
 * context for the applications "real" EditParts.
 *
 * @author lobas_av
 * @coverage gef.graphical
 */
class RootEditPart extends GraphicalEditPart implements IRootContainer {
  private final IEditPartViewer m_viewer;
  private final IRootFigure m_rootFigure;
  private EditPart m_contentEditPart;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public RootEditPart(IEditPartViewer viewer, IRootFigure rootFigure) {
    m_viewer = viewer;
    m_rootFigure = rootFigure;
    createLayers();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Layer's
  //
  ////////////////////////////////////////////////////////////////////////////
  private void createLayers() {
    m_rootFigure.addLayer(new Layer(IEditPartViewer.PRIMARY_LAYER_SUB_1));
    m_rootFigure.addLayer(new Layer(IEditPartViewer.PRIMARY_LAYER));
    m_rootFigure.addLayer(new Layer(IEditPartViewer.HANDLE_LAYER_SUB_1));
    m_rootFigure.addLayer(new Layer(IEditPartViewer.HANDLE_LAYER_SUB_2));
    m_rootFigure.addLayer(new Layer(IEditPartViewer.HANDLE_LAYER));
    m_rootFigure.addLayer(new Layer(IEditPartViewer.HANDLE_LAYER_STATIC));
    m_rootFigure.addLayer(new Layer(IEditPartViewer.FEEDBACK_LAYER_SUB_1));
    m_rootFigure.addLayer(new Layer(IEditPartViewer.FEEDBACK_LAYER_SUB_2));
    m_rootFigure.addLayer(new Layer(IEditPartViewer.FEEDBACK_LAYER));
    m_rootFigure.addLayer(new Layer(IEditPartViewer.FEEDBACK_LAYER_ABV_1));
    m_rootFigure.addLayer(new Layer(IEditPartViewer.CLICKABLE_LAYER));
    m_rootFigure.addLayer(new Layer(IEditPartViewer.MENU_PRIMARY_LAYER));
    m_rootFigure.addLayer(new Layer(IEditPartViewer.MENU_HANDLE_LAYER));
    m_rootFigure.addLayer(new Layer(IEditPartViewer.MENU_HANDLE_LAYER_STATIC));
    m_rootFigure.addLayer(new Layer(IEditPartViewer.MENU_FEEDBACK_LAYER));
    m_rootFigure.addLayer(new Layer(IEditPartViewer.TOP_LAYER));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // EditPart
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Returns the root's {@link EditPartViewer}.
   */
  @Override
  public IEditPartViewer getViewer() {
    return m_viewer;
  }

  /**
   * Return root {@link Figure} for all {@link EditPart} {@link Figure}'s.
   */
  @Override
  public Figure getContentPane() {
    return m_rootFigure.getLayer(IEditPartViewer.PRIMARY_LAYER);
  }

  /**
   * This {@link EditPart} not contains itself {@link Figure}.
   */
  @Override
  protected Figure createFigure() {
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IRootEditPart
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Returns the <i>content</i> {@link EditPart}.
   */
  public EditPart getContent() {
    return m_contentEditPart;
  }

  /**
   * Sets the <i>content</i> {@link EditPart}. A RootEditPart only has a single child, called its
   * <i>contents</i>.
   */
  public void setContent(EditPart contentEditPart) {
    if (m_contentEditPart != null) {
      // remove content
      removeChild(m_contentEditPart);
      // clear all layers
      for (Layer layer : m_rootFigure.getLayers()) {
        layer.removeAll();
      }
    }
    //
    m_contentEditPart = contentEditPart;
    //
    if (m_contentEditPart != null) {
      addChild(m_contentEditPart, -1);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // DragTracking
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Tool getDragTrackerTool(Request request) {
    return new MarqueeSelectionTool();
  }
}