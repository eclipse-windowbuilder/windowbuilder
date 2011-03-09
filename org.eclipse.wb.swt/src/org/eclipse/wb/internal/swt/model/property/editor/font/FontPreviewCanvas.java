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
package org.eclipse.wb.internal.swt.model.property.editor.font;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.ui.DrawUtils;
import org.eclipse.wb.internal.swt.support.ToolkitSupport;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * Control for displaying {@link Font}.
 * 
 * @author lobas_av
 * @coverage swt.property.editor
 */
public final class FontPreviewCanvas extends Canvas {
  private Image m_image;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public FontPreviewCanvas(Composite parent, int style) {
    super(parent, style);
    addListener(SWT.Paint, new Listener() {
      public void handleEvent(Event event) {
        onPaint(event.gc);
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Widget
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void dispose() {
    if (m_image != null) {
      m_image.dispose();
      m_image = null;
    }
    super.dispose();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Painting
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Point computeSize(int wHint, int hHint, boolean changed) {
    int width = 450;
    int height = 50;
    return new Point(width, height);
  }

  /**
   * Handler for {@link SWT#Paint}.
   */
  protected final void onPaint(GC gc) {
    if (m_image != null) {
      // draw image
      Rectangle clientArea = getClientArea();
      DrawUtils.drawImageCHCV(
          gc,
          m_image,
          clientArea.x,
          clientArea.y,
          clientArea.width,
          clientArea.height);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets the {@link Font} to display.
   */
  public void setFontInfo(JavaInfo javaInfo, final FontInfo fontInfo) {
    if (m_image != null) {
      m_image.dispose();
      m_image = null;
    }
    if (fontInfo != null) {
      ExecutionUtils.runLog(new RunnableEx() {
        public void run() throws Exception {
          m_image = ToolkitSupport.getFontPreview(fontInfo.getFont());
        }
      });
    }
    redraw();
  }
}