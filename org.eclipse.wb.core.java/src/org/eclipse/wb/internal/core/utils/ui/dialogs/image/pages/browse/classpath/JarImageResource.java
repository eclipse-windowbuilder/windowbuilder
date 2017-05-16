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

import org.eclipse.core.runtime.IPath;
import org.eclipse.swt.graphics.Image;

/**
 * implementation of {@link IImageResource} for single file in jar.
 *
 * @author scheglov_ke
 * @coverage core.ui
 */
final class JarImageResource extends AbstractJarImageElement implements IImageResource {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public JarImageResource(JarImageContainer jarContainer, IPath entryPath) {
    super(jarContainer, entryPath);
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
    return m_entryPath.lastSegment();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IImageResource
  //
  ////////////////////////////////////////////////////////////////////////////
  public ImageInfo getImageInfo() {
    return m_jarContainer.getImage(m_entryPath.toOSString().replace('\\', '/'));
  }
}
