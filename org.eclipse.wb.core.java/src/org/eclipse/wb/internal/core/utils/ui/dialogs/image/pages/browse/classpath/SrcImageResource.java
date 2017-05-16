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
package org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.browse.classpath;

import org.eclipse.wb.internal.core.utils.ui.dialogs.image.ImageInfo;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.browse.model.IImageResource;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import java.io.InputStream;

/**
 * implementation of {@link IImageResource} for single {@link IResource} in {@link IPackageFragment}
 * .
 *
 * @author scheglov_ke
 * @coverage core.ui
 */
final class SrcImageResource implements IImageResource {
  private final String m_id;
  private final IPackageFragmentRoot m_packageFragmentRoot;
  private final IFile m_file;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SrcImageResource(String id, IPackageFragmentRoot packageFragmentRoot, IFile file) {
    m_id = id;
    m_packageFragmentRoot = packageFragmentRoot;
    m_file = file;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IImageElement
  //
  ////////////////////////////////////////////////////////////////////////////
  public Image getImage() {
    ImageInfo imageInfo = getImageInfo();
    return imageInfo != null ? imageInfo.getImage() : null;
  }

  public String getName() {
    return m_file.getFullPath().lastSegment();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IImageResource
  //
  ////////////////////////////////////////////////////////////////////////////
  private ImageInfo m_imageInfo;

  public ImageInfo getImageInfo() {
    if (m_imageInfo == null) {
      try {
        // prepare path in class path
        String path;
        {
          int srcSegments = m_packageFragmentRoot.getResource().getFullPath().segmentCount();
          path = m_file.getFullPath().removeFirstSegments(srcSegments).toOSString();
          path = path.replace('\\', '/');
        }
        // load image
        Image image;
        try {
          InputStream inputStream = m_file.getContents();
          try {
            image = new Image(Display.getCurrent(), inputStream);
          } finally {
            inputStream.close();
          }
        } catch (Throwable e) {
          return null;
        }
        // add to cache
        m_imageInfo = new ImageInfo(m_id, path, image, -1);
      } catch (Throwable e) {
      }
    }
    return m_imageInfo;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Internal access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Disposes {@link Image} in {@link ImageInfo}.
   */
  void dispose() {
    if (m_imageInfo != null) {
      m_imageInfo.getImage().dispose();
    }
  }
}
