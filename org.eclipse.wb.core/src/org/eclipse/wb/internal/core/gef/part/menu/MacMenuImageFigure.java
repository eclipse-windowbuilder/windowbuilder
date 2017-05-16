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
package org.eclipse.wb.internal.core.gef.part.menu;

import org.eclipse.wb.draw2d.Graphics;
import org.eclipse.wb.draw2d.IColorConstants;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.menu.IMenuInfo;

import org.eclipse.swt.SWT;

/**
 * Figure to represent menu on MacOSX.
 *
 * @author mitin_aa
 * @coverage core.gef.menu
 */
public final class MacMenuImageFigure extends MenuImageFigure {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public MacMenuImageFigure(IMenuInfo menu) {
    super(menu);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Paint
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void paintClientArea(Graphics graphics) {
    super.paintClientArea(graphics);
    // draw border on MacOSX because the fill color of menu is the same as fill color of window client area
    {
      Rectangle clientArea = getClientArea();
      graphics.setForegroundColor(IColorConstants.buttonLightest);
      graphics.setLineStyle(SWT.LINE_DASH);
      graphics.drawRectangle(0, 0, clientArea.width - 1, clientArea.height - 1);
    }
  }
}
