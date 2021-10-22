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
package org.eclipse.wb.internal.swt.model.widgets;

import com.google.common.collect.Maps;

import org.eclipse.wb.internal.core.model.presentation.DefaultJavaInfoPresentation;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.reflect.ClassMap;
import org.eclipse.wb.internal.core.utils.ui.ImageDisposer;
import org.eclipse.wb.internal.swt.model.ModelMessages;
import org.eclipse.wb.internal.swt.support.ControlSupport;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Widget;

import org.osgi.framework.Bundle;

import java.net.URL;
import java.text.MessageFormat;
import java.util.Map;

/**
 * Abstract class maintaining presentation which depends on SWT style set for SWT {@link Widget}.
 *
 * @author mitin_aa
 * @coverage swt.model.widgets.presentation
 */
public abstract class StylePresentation extends DefaultJavaInfoPresentation {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public StylePresentation(WidgetInfo widget) {
    super(widget);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Icon
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Image getIcon() throws Exception {
    // try to get by style
    int style = ControlSupport.getStyle(m_javaInfo.getObject());
    for (Map.Entry<Integer, Image> entry : getImages().entrySet()) {
      int keyStyle = entry.getKey();
      if ((style & keyStyle) == keyStyle) {
        return entry.getValue();
      }
    }
    // use default
    return super.getIcon();
  }

  /**
   * Fills static map of images using {@link #addImage(int, String)}.
   */
  protected abstract void initImages() throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final ClassMap<Map<Integer, Image>> m_images = ClassMap.create();

  /**
   * @return the "style to image" map corresponding to this {@link StylePresentation}.
   */
  private Map<Integer, Image> getImages() throws Exception {
    Map<Integer, Image> images = m_images.get(getClass());
    if (images == null) {
      images = Maps.newHashMap();
      m_images.put(getClass(), images);
      initImages();
    }
    return images;
  }

  /**
   * Add an image into image map representing given <code>style</code> by given
   * <code>imagePath</code>. See {@link StylePresentation#addImage(int, String)}.
   *
   * @param style
   *          the SWT style value.
   * @param imagePath
   *          the path relative to current toolkit support bundle.
   */
  protected final void addImage(int style, String imagePath) throws Exception {
    // load image
    Image image;
    {
      Bundle bundle = m_javaInfo.getDescription().getToolkit().getBundle();
      URL imageURL = bundle.getEntry(imagePath);
      Assert.isNotNull(
          imageURL,
          MessageFormat.format(
              ModelMessages.StylePresentation_canNotFindImage,
              imagePath,
              bundle.getSymbolicName()));
      image = new Image(Display.getDefault(), imageURL.openStream());
    }
    // remember image
    getImages().put(style, image);
    ImageDisposer.add(getClass(), imagePath, image);
  }
}