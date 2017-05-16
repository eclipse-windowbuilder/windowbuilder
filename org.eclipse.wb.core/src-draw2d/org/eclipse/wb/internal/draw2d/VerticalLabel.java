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
package org.eclipse.wb.internal.draw2d;

import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.draw2d.Graphics;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Insets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

/**
 * A Label which draws its text vertically.
 *
 * @author mitin_aa
 */
public final class VerticalLabel extends Label {
  private Dimension m_preferredSize;

  @Override
  protected void paintClientArea(Graphics graphics) {
    drawVerticalText(getText(), 0, 0, graphics, false);
  }

  /**
   * Returns the desirable size for this label's text.
   */
  @Override
  public Dimension getPreferredSize() {
    if (m_preferredSize == null) {
      m_preferredSize = FigureUtils.calculateTextSize(getText(), getFont());
      Insets insets = getInsets();
      m_preferredSize.expand(insets.getWidth(), insets.getHeight());
      m_preferredSize.transpose();
    }
    return m_preferredSize;
  }

  @Override
  protected void repaint(boolean reset, int x, int y, int width, int height) {
    if (reset) {
      m_preferredSize = null;
    }
    super.repaint(reset, x, y, width, height);
  }

  /**
   * Draws text vertically (rotates plus or minus 90 degrees). Uses the current font, color, and
   * background.
   *
   * @param string
   *          the text to draw
   * @param x
   *          the x coordinate of the top left corner of the drawing rectangle
   * @param y
   *          the y coordinate of the top left corner of the drawing rectangle
   * @param graphics
   *          the GC on which to draw the text
   */
  private static void drawVerticalText(String string, int x, int y, Graphics graphics, boolean isUp) {
    // Get the current display
    Display display = Display.getCurrent();
    if (display == null) {
      SWT.error(SWT.ERROR_THREAD_INVALID_ACCESS);
    }
    // Determine string's dimensions
    Point pt = graphics.gc.textExtent(string);
    // Create an image the same size as the string
    Image stringImage = new Image(display, pt.x, pt.y);
    // Create a GC so we can draw the image
    GC stringGc = new GC(stringImage);
    // Set attributes from the original GC to the new GC
    stringGc.setForeground(graphics.getForegroundColor());
    stringGc.setBackground(graphics.getBackgroundColor());
    stringGc.setFont(graphics.getFont());
    // Draw the text onto the image
    stringGc.drawText(string, 0, 0);
    // Draw the image vertically onto the original GC
    Image rotatedImage = rotateImage(stringImage, isUp);
    // Draw the vertical image onto the original GC
    graphics.drawImage(rotatedImage, x, y);
    // Dispose
    rotatedImage.dispose();
    stringGc.dispose();
    stringImage.dispose();
  }

  /**
   * Rotates an image vertically (rotates plus or minus 90 degrees)
   */
  private static Image rotateImage(Image image, boolean up) {
    // Get the current display
    Display display = Display.getCurrent();
    if (display == null) {
      SWT.error(SWT.ERROR_THREAD_INVALID_ACCESS);
    }
    // Use the image's data to create a rotated image's data
    ImageData sd = image.getImageData();
    ImageData dd = new ImageData(sd.height, sd.width, sd.depth, sd.palette);
    // Determine which way to rotate, depending on up or down
    // Run through the horizontal pixels
    for (int sx = 0; sx < sd.width; sx++) {
      // Run through the vertical pixels
      for (int sy = 0; sy < sd.height; sy++) {
        // Determine where to move pixel to in destination image data
        int dx = up ? sy : sd.height - sy - 1;
        int dy = up ? sd.width - sx - 1 : sx;
        // Swap the x, y source data to y, x in the destination
        dd.setPixel(dx, dy, sd.getPixel(sx, sy));
      }
    }
    // Create the vertical image
    return new Image(display, dd);
  }
}