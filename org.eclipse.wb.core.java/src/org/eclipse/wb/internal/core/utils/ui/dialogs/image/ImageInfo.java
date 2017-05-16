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
package org.eclipse.wb.internal.core.utils.ui.dialogs.image;

import org.eclipse.swt.graphics.Image;

/**
 * Information about image.
 *
 * @author scheglov_ke
 * @coverage core.ui
 */
public final class ImageInfo {
  private final String m_pageId;
  private final Object m_data;
  private final Image m_image;
  private final long m_size;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ImageInfo(String pageId, Object data, Image image, long size) {
    m_pageId = pageId;
    m_data = data;
    m_image = image;
    m_size = size;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the id of page that provided this {@link ImageInfo}.
   */
  public String getPageId() {
    return m_pageId;
  }

  /**
   * @return the page specific data abound image, usually string with path.
   */
  public Object getData() {
    return m_data;
  }

  /**
   * @return the SWT {@link Image} of this {@link ImageInfo}.
   */
  public Image getImage() {
    return m_image;
  }

  /**
   * @return the size of image in bytes.
   */
  public long getSize() {
    return m_size;
  }
}
