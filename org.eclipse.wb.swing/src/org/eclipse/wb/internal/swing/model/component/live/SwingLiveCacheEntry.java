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
package org.eclipse.wb.internal.swing.model.component.live;

import org.eclipse.wb.internal.core.model.util.live.ILiveCacheEntry;

import org.eclipse.swt.graphics.Image;

import java.awt.Component;

/**
 * Live components cache entry for Swing toolkit.
 * 
 * @author mitin_aa
 * @coverage swing.model
 */
public final class SwingLiveCacheEntry implements ILiveCacheEntry {
  private Component m_component;
  private Image m_image;
  private int m_baseline;

  ////////////////////////////////////////////////////////////////////////////
  //
  // IDisposable
  //
  ////////////////////////////////////////////////////////////////////////////
  public void dispose() {
    if (m_image != null && !m_image.isDisposed()) {
      m_image.dispose();
      m_image = null;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Component
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Store component to be cached.
   */
  public void setComponent(Component component) {
    m_component = component;
  }

  /**
   * @return the cached component.
   */
  public Component getComponent() {
    return m_component;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Image
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Store image into cache.
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
