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
package org.eclipse.wb.draw2d;

/**
 * Draws a rectangle whose size is determined by the bounds set to it.
 *
 * @author scheglov_ke
 * @coverage gef.draw2d
 */
public final class RectangleFigure extends Figure {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Paint
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void paintClientArea(Graphics graphics) {
    graphics.drawRectangle(getClientArea().getResized(-1, -1));
  }
}
