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
package org.eclipse.wb.internal.swing.model.property.editor.font;

import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.ui.DrawUtils;
import org.eclipse.wb.internal.swing.utils.SwingImageUtils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JLabel;

/**
 * Control for displaying {@link Font}.
 * 
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
public final class FontPreviewCanvas extends Canvas {
  private FontInfo m_fontInfo;

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
  private void onPaint(final GC gc) {
    if (m_fontInfo != null) {
      ExecutionUtils.runLog(new RunnableEx() {
        public void run() throws Exception {
          // prepare label
          JLabel label;
          {
            label = new JLabel();
            label.setFont(m_fontInfo.getFont());
            label.setText(m_fontInfo.getText());
            {
              org.eclipse.swt.graphics.Color swtColor = gc.getBackground();
              label.setBackground(new Color(swtColor.getRed(),
                  swtColor.getGreen(),
                  swtColor.getBlue()));
              label.setOpaque(true);
            }
          }
          // prepare image
          Image image;
          {
            label.setSize(label.getPreferredSize());
            image = SwingImageUtils.createComponentShot(label);
          }
          // draw image
          try {
            Rectangle clientArea = getClientArea();
            DrawUtils.drawImageCHCV(
                gc,
                image,
                clientArea.x,
                clientArea.y,
                clientArea.width,
                clientArea.height);
          } finally {
            image.dispose();
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
  /**
   * Sets the {@link Font} to display.
   */
  public void setFontInfo(FontInfo font) {
    m_fontInfo = font;
    redraw();
  }
}
