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

import org.eclipse.wb.internal.core.utils.IDisposable;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.ImageInfo;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.browse.model.IImageResource;

import org.eclipse.swt.graphics.Image;

/**
 * Common image resource.
 *
 * @author lobas_av
 * @coverage swt.property.editor.plugin
 */
public abstract class ImageResource implements IImageResource, IDisposable {
  ////////////////////////////////////////////////////////////////////////////
  //
  // IImageElement
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final Image getImage() {
    ImageInfo imageInfo = getImageInfo();
    return imageInfo != null ? imageInfo.getImage() : null;
  }
}