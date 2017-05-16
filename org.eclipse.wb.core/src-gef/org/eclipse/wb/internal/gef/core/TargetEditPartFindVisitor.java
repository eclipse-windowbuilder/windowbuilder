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
package org.eclipse.wb.internal.gef.core;

import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.internal.draw2d.FigureCanvas;
import org.eclipse.wb.internal.draw2d.TargetFigureFindVisitor;

/**
 * This class use to find {@link EditPart} under mouse.
 *
 * @author lobas_av
 * @coverage gef.core
 */
public class TargetEditPartFindVisitor extends TargetFigureFindVisitor {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TargetEditPartFindVisitor(FigureCanvas canvas, int x, int y) {
    super(canvas, x, y);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Return mouse target {@link EditPart}.
   */
  public EditPart getTargetEditPart() {
    return extractEditPart(getTargetFigure());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Extract {@link EditPart} from given {@link Figure}.
   */
  protected static EditPart extractEditPart(Figure figure) {
    EditPart editPart = null;
    while (editPart == null && figure != null) {
      editPart = (EditPart) figure.getData();
      figure = figure.getParent();
    }
    return editPart;
  }
}