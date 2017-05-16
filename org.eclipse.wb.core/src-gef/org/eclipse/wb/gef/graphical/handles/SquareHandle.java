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

import org.eclipse.wb.draw2d.Graphics;
import org.eclipse.wb.draw2d.IColorConstants;
import org.eclipse.wb.draw2d.ILocator;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;

import org.eclipse.swt.graphics.Color;

/**
 * A small square handle approximately 7x7 pixels in size, that is either black or white.
 *
 * @author lobas_av
 * @coverage gef.graphical
 */
public abstract class SquareHandle extends Handle {
  /**
   * The default size for square handles.
   */
  protected static final int DEFAULT_HANDLE_SIZE = 7;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Creates a handle for the given <code>{@link GraphicalEditPart}</code> using the given
   * <code>{@link ILocator}</code>.
   */
  public SquareHandle(GraphicalEditPart owner, ILocator locator) {
    super(owner, locator);
    setSize(DEFAULT_HANDLE_SIZE, DEFAULT_HANDLE_SIZE);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // SquareHandle
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Returns <code>true</code> if the handle's owner is the primary selection.
   */
  protected boolean isPrimary() {
    return getOwner().getSelected() == EditPart.SELECTED_PRIMARY;
  }

  /**
   * Returns the color for the outside of the handle.
   */
  protected Color getBorderColor() {
    return isPrimary() ? IColorConstants.white : IColorConstants.black;
  }

  /**
   * Returns the color for the inside of the handle.
   */
  protected Color getFillColor() {
    return isPrimary() ? IColorConstants.black : IColorConstants.white;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Figure
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void paintClientArea(Graphics graphics) {
    Rectangle area = getClientArea();
    area.shrink(1, 1);
    //
    graphics.setBackgroundColor(getFillColor());
    graphics.fillRectangle(area);
    //
    graphics.setForegroundColor(getBorderColor());
    graphics.drawRectangle(area);
  }
}