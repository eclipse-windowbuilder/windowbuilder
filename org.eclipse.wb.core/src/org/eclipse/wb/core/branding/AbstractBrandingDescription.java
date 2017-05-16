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
package org.eclipse.wb.core.branding;

import org.eclipse.wb.draw2d.Graphics;
import org.eclipse.wb.draw2d.IColorConstants;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.utils.platform.PluginUtilities;

/**
 * A generic {@link IBrandingDescription} that stores the product name on its creation, and returns
 * the value with {@link #getProductName()}.
 *
 * @see BrandingUtils
 *
 * @author Jaime Wren
 * @coverage core.util
 */
public abstract class AbstractBrandingDescription implements IBrandingDescription {
  private final String m_productName;
  private final IBrandingSupportInfo m_supportInfo;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  protected AbstractBrandingDescription(String productName, IBrandingSupportInfo supportInfo) {
    m_productName = productName;
    m_supportInfo = supportInfo;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IBrandingDescription
  //
  ////////////////////////////////////////////////////////////////////////////
  public String getProductName() {
    return m_productName;
  }

  public IBrandingSupportInfo getSupportInfo() {
    return m_supportInfo;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Painting
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * If not overridden, then this is the default behavior.
   */
  public void paintBrandingOnCanvas(Rectangle clientArea, Graphics graphics) {
    //String text = getProductName();
    String version = PluginUtilities.getVersionString("org.eclipse.wb.core");
    paintBrandingOnCanvas_grayText(clientArea, graphics, /*text + "\n" +*/version);
  }

  /**
   * Utility method for subclasses that just want the paintBrandingOnCanvas override to just paint
   * gray text.
   */
  protected final void paintBrandingOnCanvas_grayText(Rectangle clientArea,
      Graphics graphics,
      String text) {
    Dimension extent = graphics.getTextExtent(text);
    graphics.setForegroundColor(IColorConstants.lightGray);
    int x = clientArea.right() - extent.width - 2;
    int y = clientArea.bottom() - extent.height - 0;
    graphics.drawText(text, x, y);
  }
}
