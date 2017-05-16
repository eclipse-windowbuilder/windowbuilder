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

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.browse.model.IImageContainer;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.browse.model.IImageElement;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.swt.graphics.Image;

import java.util.List;

/**
 * Implementation of {@link IImageContainer} for package in jar.
 *
 * @author scheglov_ke
 * @coverage core.ui
 */
final class JarPackageImageContainer extends AbstractJarImageElement implements IImageContainer {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public JarPackageImageContainer(JarImageContainer jarContainer, IPath entryPath) {
    super(jarContainer, entryPath);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IImageContainer
  //
  ////////////////////////////////////////////////////////////////////////////
  public IImageElement[] elements() {
    return m_imageEntryList.toArray(new IImageElement[m_imageEntryList.size()]);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IImageElement
  //
  ////////////////////////////////////////////////////////////////////////////
  public Image getImage() {
    return JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_PACKAGE);
  }

  public String getName() {
    return m_entryPath.toString().replace('/', '.');
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Image resources
  //
  ////////////////////////////////////////////////////////////////////////////
  private final List<JarImageResource> m_imageEntryList = Lists.newArrayList();

  /**
   * Adds given {@link JarImageResource} to this package.
   */
  void addImageEntry(JarImageResource resource) {
    m_imageEntryList.add(resource);
  }
}