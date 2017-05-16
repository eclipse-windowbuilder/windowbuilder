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

import org.eclipse.wb.internal.core.utils.Messages;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.core.utils.ui.UiUtils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import java.text.MessageFormat;

/**
 * {@link Composite} for displaying {@link ImageInfo}.
 *
 * @author scheglov_ke
 * @coverage core.ui
 */
public class ImagePreviewComposite extends Composite {
  private final Label m_dimensionLabel;
  private final Label m_sizeLabel;
  private final Canvas m_imageCanvas;
  private ImageInfo m_imageInfo;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ImagePreviewComposite(Composite parent, int style) {
    super(parent, style);
    GridLayoutFactory.create(this);
    // dimension label
    {
      m_dimensionLabel = new Label(this, SWT.NONE);
      GridDataFactory.create(m_dimensionLabel).grabH().fillH();
    }
    // size label
    {
      m_sizeLabel = new Label(this, SWT.NONE);
      GridDataFactory.create(m_sizeLabel).grabH().fillH();
    }
    // image canvas
    {
      m_imageCanvas = new Canvas(this, SWT.NONE);
      GridDataFactory.create(m_imageCanvas).grab().fill();
      // paint listener
      m_imageCanvas.addListener(SWT.Paint, new Listener() {
        public void handleEvent(Event event) {
          Rectangle clientArea = m_imageCanvas.getClientArea();
          GC gc = event.gc;
          if (m_imageInfo != null && m_imageInfo.getImage() != null) {
            Image image = m_imageInfo.getImage();
            UiUtils.drawScaledImage(gc, image, clientArea);
          } else {
            String text = Messages.ImagePreviewComposite_noPreview;
            Point extent = gc.textExtent(text);
            int x = clientArea.x + (clientArea.width - extent.x) / 2;
            int y = clientArea.y + (clientArea.height - extent.y) / 2;
            gc.setForeground(m_imageCanvas.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
            gc.drawText(text, x, y);
          }
        }
      });
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public void setImageInfo(ImageInfo imageInfo) {
    m_imageInfo = imageInfo;
    if (m_imageInfo != null && m_imageInfo.getImage() != null) {
      // update dimension
      {
        Rectangle bounds = m_imageInfo.getImage().getBounds();
        m_dimensionLabel.setText(MessageFormat.format(
            Messages.ImagePreviewComposite_dimension,
            bounds.width,
            bounds.height));
      }
      // update size
      {
        long size = m_imageInfo.getSize();
        if (size != -1) {
          m_sizeLabel.setText(MessageFormat.format(Messages.ImagePreviewComposite_size, size));
        } else {
          m_sizeLabel.setText(Messages.ImagePreviewComposite_sizeNA);
        }
      }
    } else {
      m_dimensionLabel.setText("");
      m_sizeLabel.setText("");
    }
    // update image
    m_imageCanvas.redraw();
  }
}
