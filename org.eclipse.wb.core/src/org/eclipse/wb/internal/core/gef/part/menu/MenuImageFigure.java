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

import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.Graphics;
import org.eclipse.wb.internal.core.model.menu.IMenuInfo;

import org.eclipse.swt.graphics.Image;

/**
 * Figure to display menu image.
 *
 * @author mitin_aa
 * @coverage core.gef.figure
 */
public class MenuImageFigure extends Figure {
  private final IMenuInfo m_menu;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public MenuImageFigure(IMenuInfo menu) {
    m_menu = menu;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Paint
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void paintClientArea(Graphics graphics) {
    Image image = m_menu.getImage();
    graphics.drawImage(image, 0, 0);
  }
}
