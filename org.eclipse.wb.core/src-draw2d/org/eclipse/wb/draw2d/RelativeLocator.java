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

import org.eclipse.wb.draw2d.geometry.Rectangle;

/**
 * Implementation of {@link AbstractRelativeLocator} that uses some {@link Figure} as reference.
 *
 * @author lobas_av
 * @coverage gef.draw2d
 */
public final class RelativeLocator extends AbstractRelativeLocator {
  private final Figure m_reference;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public RelativeLocator(Figure reference, double relativeX, double relativeY) {
    super(relativeX, relativeY);
    m_reference = reference;
  }

  public RelativeLocator(Figure reference, int location) {
    super(location);
    m_reference = reference;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // AbstractRelativeLocator
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Rectangle getReferenceRectangle() {
    Rectangle bounds = m_reference.getBounds().getCopy();
    FigureUtils.translateFigureToAbsolute(m_reference, bounds);
    return bounds;
  }
}