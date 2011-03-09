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
package org.eclipse.wb.internal.xwt.model.widgets;

import com.google.common.collect.Maps;

import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.xml.model.XmlObjectPresentation;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Widget;

import org.osgi.framework.Bundle;

import java.net.URL;
import java.util.Map;

/**
 * Abstract class maintaining presentation which depends on SWT style set for SWT {@link Widget}.
 * 
 * @author mitin_aa
 * @coverage XWT.model.widgets
 */
public abstract class StylePresentation extends XmlObjectPresentation {
  protected final WidgetInfo m_widget;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public StylePresentation(WidgetInfo widget) {
    super(widget);
    m_widget = widget;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Icon
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Image getIcon() throws Exception {
    // try to get by style
    int style = m_widget.getStyle();
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
  private static final Map<Class<?>, Map<Integer, Image>> m_images = Maps.newHashMap();

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
      Bundle bundle = m_widget.getDescription().getToolkit().getBundle();
      URL imageURL = bundle.getEntry(imagePath);
      Assert.isNotNull(
          imageURL,
          "Can't find image: " + imagePath + " in " + bundle.getSymbolicName());
      image = new Image(Display.getDefault(), imageURL.openStream());
    }
    // remember image
    getImages().put(style, image);
  }
}