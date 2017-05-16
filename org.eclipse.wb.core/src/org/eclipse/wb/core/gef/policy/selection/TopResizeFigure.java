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
package org.eclipse.wb.core.gef.policy.selection;

import org.eclipse.wb.draw2d.Graphics;
import org.eclipse.wb.draw2d.IColorConstants;
import org.eclipse.wb.draw2d.border.LineBorder;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.utils.ui.SwtResourceManager;
import org.eclipse.wb.internal.draw2d.SemiTransparentFigure;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;

import org.apache.commons.lang.StringUtils;

/**
 * Figure used as feedback while resizing top-level edit parts.
 *
 * @author mitin_aa
 */
public class TopResizeFigure extends SemiTransparentFigure {
  private String m_sizeText;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TopResizeFigure() {
    super(64);
    setBackground(IColorConstants.lightGreen);
    setForeground(IColorConstants.darkGray);
    setBorder(new LineBorder(IColorConstants.darkBlue, 1));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public void setSizeText(String sizeText) {
    m_sizeText = sizeText;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Paint
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void paintClientArea(Graphics graphics) {
    super.paintClientArea(graphics);
    if (!StringUtils.isEmpty(m_sizeText)) {
      Rectangle area = getClientArea();
      Font font = SwtResourceManager.getFont("Arial", 16, SWT.NONE);
      graphics.setFont(font);
      Dimension textExtent = graphics.getTextExtent(m_sizeText);
      int x = area.x + (area.width - textExtent.width) / 2;
      int y = area.y + (area.height - textExtent.height) / 2;
      graphics.drawString(m_sizeText, x, y);
    }
  }
}
