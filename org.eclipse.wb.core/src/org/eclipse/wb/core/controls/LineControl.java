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
package org.eclipse.wb.core.controls;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

/**
 * Horizontal or vertical line.
 *
 * @author scheglov_ke
 * @coverage core.control
 */
public final class LineControl extends Canvas {
  private final boolean m_horizontal;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public LineControl(Composite parent, int style) {
    super(parent, style);
    m_horizontal = (style & SWT.HORIZONTAL) != 0;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Size
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Point computeSize(int hint, int hint2, boolean changed) {
    return new Point(m_horizontal ? hint : 1, m_horizontal ? 1 : hint2);
  }
}
