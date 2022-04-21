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
package org.eclipse.wb.internal.swt.model.property.editor.image.plugin;

import org.eclipse.wb.internal.core.utils.ui.dialogs.image.ImageInfo;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.browse.model.IImageResource;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import org.apache.commons.lang.StringUtils;
import org.osgi.framework.Bundle;

import java.io.InputStream;
import java.net.URL;

/**
 * Implementation {@link IImageResource} for plugin resource available over {@link Bundle}.
 *
 * @author lobas_av
 * @coverage swt.property.editor.plugin
 */
public final class BundleImageResource extends ImageResource {
  private final URL m_url;
  private final String m_symbolicName;
  private final String m_imagePath;
  private final String m_name;
  private ImageInfo m_imageInfo;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public BundleImageResource(URL url, String symbolicName) {
    m_url = url;
    m_symbolicName = symbolicName;
    m_imagePath = m_url.getFile();
    m_name = StringUtils.substringAfterLast(m_imagePath, "/");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IImageResource
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public ImageInfo getImageInfo() {
    if (m_imageInfo == null) {
      // load image
      Image image;
      try {
        InputStream stream = m_url.openStream();
        try {
          image = new Image(Display.getCurrent(), stream);
        } finally {
          stream.close();
        }
      } catch (Throwable e) {
        return null;
      }
      // add to cache
      m_imageInfo =
          new ImageInfo(PluginFileImagePage.ID,
              new String[]{m_symbolicName, m_imagePath},
              image,
              -1);
    }
    return m_imageInfo;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IImageElement
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getName() {
    return m_name;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Internal
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return resource image path.
   */
  public String getPath() {
    return m_imagePath;
  }

  /**
   * Disposes {@link Image} in {@link ImageInfo}.
   */
  @Override
  public void dispose() {
    if (m_imageInfo != null) {
      m_imageInfo.getImage().dispose();
    }
  }
}