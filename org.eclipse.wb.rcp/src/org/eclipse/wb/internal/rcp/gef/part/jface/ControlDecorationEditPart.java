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
package org.eclipse.wb.internal.rcp.gef.part.jface;

import org.eclipse.wb.core.gef.part.AbstractComponentEditPart;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.internal.rcp.model.jface.ControlDecorationInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

/**
 * {@link EditPart} for {@link ControlDecorationInfo}.
 * 
 * @author scheglov_ke
 * @coverage rcp.gef.part
 */
public final class ControlDecorationEditPart extends AbstractComponentEditPart {
  private final ControlDecorationInfo m_decoration;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ControlDecorationEditPart(ControlDecorationInfo decoration) {
    super(decoration);
    m_decoration = decoration;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Figure
  //
  ////////////////////////////////////////////////////////////////////////////
  private Figure m_originalControlFigure;

  @Override
  protected void refreshVisuals() {
    Figure figure = getFigure();
    Figure controlFigure = getControlFigure();
    Figure controlParentFigure = controlFigure.getParent();
    // ensure that decoration is located on _parent_ of Control
    if (figure.getParent() != controlParentFigure) {
      m_originalControlFigure = controlFigure;
      FigureUtils.removeFigure(figure);
      int controlIndex = controlParentFigure.getChildren().indexOf(controlFigure);
      controlParentFigure.add(figure, controlIndex + 1);
    }
    // set decoration bounds
    {
      Point controlLocation = controlFigure.getLocation();
      Rectangle boundsInParent = m_decoration.getModelBounds().getTranslated(controlLocation);
      boundsInParent.translate(1, 1); // not sure why, but required to center selection visually
      figure.setBounds(boundsInParent);
    }
  }

  @Override
  public void removeNotify() {
    // move decoration figure back to Control figure
    {
      Figure figure = getFigure();
      FigureUtils.removeFigure(figure);
      m_originalControlFigure.add(figure);
    }
    // continue
    super.removeNotify();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link Figure} of decorated {@link ControlInfo}.
   */
  private Figure getControlFigure() {
    EditPart controlEditPart = getViewer().getEditPartByModel(m_decoration.getControl());
    return ((GraphicalEditPart) controlEditPart).getFigure();
  }
}
