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
package org.eclipse.wb.gef.graphical.policies;

import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.Layer;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;

/**
 * @author lobas_av
 * @coverage gef.graphical
 */
public class GraphicalEditPolicy extends EditPolicy {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the <i>host</i> {@link GraphicalEditPart} on which this policy is installed.
   */
  @Override
  public GraphicalEditPart getHost() {
    return (GraphicalEditPart) super.getHost();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Figure
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Convenience method to return the host's {@link Figure}.
   */
  protected final Figure getHostFigure() {
    return getHost().getFigure();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Layer's
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Obtains the specified layer.
   */
  //@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "ES_COMPARING_PARAMETER_STRING_WITH_EQ")
  protected final Layer getLayer(String name) {
    if (isOnMenuLayer()) {
      if (name == IEditPartViewer.HANDLE_LAYER) {
        name = IEditPartViewer.MENU_HANDLE_LAYER;
      } else if (name == IEditPartViewer.HANDLE_LAYER_STATIC) {
        name = IEditPartViewer.MENU_HANDLE_LAYER_STATIC;
      } else if (name == IEditPartViewer.FEEDBACK_LAYER) {
        name = IEditPartViewer.MENU_FEEDBACK_LAYER;
      }
    }
    return getHost().getViewer().getLayer(name);
  }

  /**
   * @return the {@link Layer} for {@link IEditPartViewer#FEEDBACK_LAYER}.
   */
  protected Layer getFeedbackLayer() {
    return getLayer(IEditPartViewer.FEEDBACK_LAYER);
  }

  /**
   * Adds the specified <code>{@link Figure}</code> to the {@link IEditPartViewer#FEEDBACK_LAYER}.
   */
  protected final void addFeedback(Figure figure) {
    getFeedbackLayer().add(figure);
  }

  /**
   * Removes the specified <code>{@link Figure}</code> to the {@link IEditPartViewer#FEEDBACK_LAYER}
   * .
   */
  protected final void removeFeedback(Figure figure) {
    getFeedbackLayer().remove(figure);
  }

  /**
   * @return <code>true</code> if host {@link EditPart} is located on
   *         {@link IEditPartViewer#MENU_PRIMARY_LAYER}.
   */
  private boolean isOnMenuLayer() {
    Layer menuPrimaryLayer = getHost().getViewer().getLayer(IEditPartViewer.MENU_PRIMARY_LAYER);
    for (Figure figure = getHostFigure(); figure != null; figure = figure.getParent()) {
      if (figure == menuPrimaryLayer) {
        return true;
      }
    }
    // no, probably normal PRIMARY_LAYER
    return false;
  }
}