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

import com.google.common.collect.Maps;

import org.eclipse.wb.internal.core.utils.ui.dialogs.image.ImageInfo;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.browse.AbstractBrowseImagePage;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.browse.model.IImageContainer;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * Implementation of {@link IImageContainer} for jar file in classpath.
 *
 * @author scheglov_ke
 * @coverage core.ui
 */
final class JarImageContainer implements IClasspathImageContainer {
  private final String m_id;
  private final IPackageFragmentRoot m_root;
  private final JarFile m_jarFile;
  private final Map<IPath, JarPackageImageContainer> m_packagePathToEntryMap = Maps.newHashMap();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public JarImageContainer(String id, IPackageFragmentRoot packageFragmentRoot) throws Exception {
    m_id = id;
    m_root = packageFragmentRoot;
    // open jar file
    {
      IPath jarPath = m_root.isExternal() ? m_root.getPath() : m_root.getResource().getLocation();
      m_jarFile = new JarFile(jarPath.toOSString());
    }
    //
    for (Enumeration<JarEntry> E = m_jarFile.entries(); E.hasMoreElements();) {
      JarEntry entry = E.nextElement();
      // prepare entry information
      String entryName = entry.getName();
      IPath entryPath = new Path(entryName);
      // add image
      if (AbstractBrowseImagePage.isImageExtension(entryPath.getFileExtension())) {
        // prepare package container
        JarPackageImageContainer packageEntry;
        {
          IPath packagePath = entryPath.removeLastSegments(1);
          packageEntry = m_packagePathToEntryMap.get(packagePath);
          if (packageEntry == null) {
            packageEntry = new JarPackageImageContainer(this, packagePath);
            m_packagePathToEntryMap.put(packagePath, packageEntry);
          }
        }
        // add resource to package
        packageEntry.addImageEntry(new JarImageResource(this, entryPath));
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IImageElement
  //
  ////////////////////////////////////////////////////////////////////////////
  public Image getImage() {
    return JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_JAR);
  }

  public String getName() {
    return m_root.getElementName();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IImageContainer
  //
  ////////////////////////////////////////////////////////////////////////////
  public IImageContainer[] elements() {
    return m_packagePathToEntryMap.values().toArray(
        new IImageContainer[m_packagePathToEntryMap.size()]);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Internal access
  //
  ////////////////////////////////////////////////////////////////////////////
  public boolean isEmpty() {
    return m_packagePathToEntryMap.isEmpty();
  }

  public void dispose() {
    try {
      m_jarFile.close();
    } catch (Throwable e) {
    }
    disposeJarImages();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Images
  //
  ////////////////////////////////////////////////////////////////////////////
  private Map<String, ImageInfo> m_nameToImageInfo = Maps.newHashMap();

  /**
   * Disposes images loaded from jar.
   */
  private void disposeJarImages() {
    for (ImageInfo imageInfo : m_nameToImageInfo.values()) {
      imageInfo.getImage().dispose();
    }
    m_nameToImageInfo = null;
  }

  /**
   * @return the {@link Image} for {@link ZipEntry} with given name.
   */
  ImageInfo getImage(String entryName) {
    ImageInfo imageInfo = m_nameToImageInfo.get(entryName);
    if (imageInfo == null) {
      // prepare entry in jar
      ZipEntry entry = m_jarFile.getEntry(entryName);
      if (entry == null) {
        return null;
      }
      // load image
      Image image;
      try {
        InputStream inputStream = m_jarFile.getInputStream(entry);
        try {
          image = new Image(Display.getCurrent(), inputStream);
        } finally {
          inputStream.close();
        }
      } catch (Throwable e) {
        return null;
      }
      // add to cache
      imageInfo = new ImageInfo(m_id, entryName, image, entry.getSize());
      m_nameToImageInfo.put(entryName, imageInfo);
    }
    //
    return imageInfo;
  }
}
