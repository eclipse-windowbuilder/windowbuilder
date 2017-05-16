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

import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.draw2d.ILocator;
import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.draw2d.geometry.Rectangle;

/**
 * A Locator used to place {@link MoveHandle}s. By default, a MoveHandle's bounds are equal to its
 * owner figure's bounds, expanded by the handle's {@link Insets}.
 *
 * @author lobas_av
 * @coverage gef.graphical
 */
public class MoveHandleLocator implements ILocator {
  private final Figure m_reference;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Creates a new {@link MoveHandleLocator} and sets its reference figure to <code>ref</code>. The
   * reference figure should be the handle's owner figure.
   */
  public MoveHandleLocator(Figure reference) {
    m_reference = reference;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ILocator
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets the handle's bounds to that of its owner figure's bounds, expanded by the handle's
   * {@link Insets}.
   */
  public void relocate(Figure target) {
    Rectangle bounds = m_reference.getBounds().getResized(-1, -1);
    FigureUtils.translateFigureToFigure(m_reference, target, bounds);
    //
    bounds.expand(target.getInsets());
    bounds.resize(1, 1);
    target.setBounds(bounds);
  }
}