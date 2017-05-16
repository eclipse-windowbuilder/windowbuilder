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
package org.eclipse.wb.gef.graphical.handles;

import org.eclipse.wb.draw2d.ICursorConstants;
import org.eclipse.wb.draw2d.ILocator;
import org.eclipse.wb.draw2d.RelativeLocator;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;

/**
 * A Handle used to resize a {@link EditPart}s.
 *
 * @author lobas_av
 * @coverage gef.graphical
 */
public class ResizeHandle extends SquareHandle {
  private final int m_direction;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Creates a new ResizeHandle for the given {@link GraphicalEditPart}. The <code>direction</code>
   * is the relative direction from the center of the owner figure.
   */
  public ResizeHandle(GraphicalEditPart owner, int direction) {
    this(owner, direction, new RelativeLocator(owner.getFigure(), direction));
  }

  /**
   * Creates a new ResizeHandle for the given {@link GraphicalEditPart}. The <code>direction</code>
   * is the relative direction from the center of the owner figure.
   */
  public ResizeHandle(GraphicalEditPart owner, int direction, ILocator locator) {
    super(owner, locator);
    m_direction = direction;
    setCursor(ICursorConstants.Directional.getCursor(direction));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public int getDirection() {
    return m_direction;
  }
}