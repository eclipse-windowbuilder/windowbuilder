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
package org.eclipse.wb.internal.swt.model.widgets.live;

import org.eclipse.wb.internal.core.model.util.live.ILiveCacheEntry;

import org.eclipse.swt.graphics.Image;

/**
 * Live components cache entry for SWT toolkit.
 *
 * @author mitin_aa
 * @coverage swt.model.widgets.live
 */
public final class SwtLiveCacheEntry implements ILiveCacheEntry {
  private Image m_image;
  private int m_style;
  private int m_baseline;

  ////////////////////////////////////////////////////////////////////////////
  //
  // IDisposable
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void dispose() {
    if (m_image != null && !m_image.isDisposed()) {
      m_image.dispose();
      m_image = null;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Image
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets the image to be cached.
   */
  public void setImage(Image image) {
    m_image = image;
  }

  /**
   * @return the cached image.
   */
  public Image getImage() {
    return m_image;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Style
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets the style value to be cached.
   */
  public void setStyle(int style) {
    m_style = style;
  }

  /**
   * @return the cached style value.
   */
  public int getStyle() {
    return m_style;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Baseline
  //
  ////////////////////////////////////////////////////////////////////////////
  public void setBaseline(int baseline) {
    m_baseline = baseline;
  }

  public int getBaseline() {
    return m_baseline;
  }
}
