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
package org.eclipse.wb.tests.draw2d;

import org.eclipse.wb.internal.draw2d.RootFigure;
import org.eclipse.wb.tests.gef.TestLogger;

/**
 * @author lobas_av
 *
 */
public class TestCaseRootFigure extends RootFigure {
  private final TestLogger m_logger;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TestCaseRootFigure(TestLogger logger) {
    super(null, null);
    m_logger = logger;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Figure
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void repaint(boolean reset, int x, int y, int width, int height) {
    if (m_logger != null) {
      m_logger.log("repaint(" + reset + ", " + x + ", " + y + ", " + width + ", " + height + ")");
    }
  }

  @Override
  protected void updateCursor() {
    if (m_logger != null) {
      m_logger.log("updateCursor");
    }
  }
}