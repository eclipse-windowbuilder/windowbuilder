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
package org.eclipse.wb.internal.core.utils.ui;

import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * Utilities for {@link Image} operations.
 *
 * @author scheglov_ke
 * @coverage core.ui
 */
public final class ImageUtils {
  /**
   * @return the PNG bytes of SWT {@link Image}.
   */
  public static byte[] getBytesPNG(Image image) throws IOException {
    ImageLoader imageLoader = new ImageLoader();
    imageLoader.data = new ImageData[]{image.getImageData()};
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    imageLoader.save(baos, SWT.IMAGE_PNG);
    return baos.toByteArray();
  }

  /**
   * @return the SWT {@link Image} for AWT one.
   */
  public static Image convertToSWT(final java.awt.Image awtImage) {
    return ExecutionUtils.runObject(new RunnableObjectEx<Image>() {
      public Image runObject() throws Exception {
        BufferedImage bufferedImage = getBufferedImage(awtImage);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "PNG", os);
        return new Image(null, new ImageData(new ByteArrayInputStream(os.toByteArray())));
      }
    });
  }

  /**
   * @return the {@link BufferedImage} with same content as given {@link java.awt.Image}.
   */
  private static BufferedImage getBufferedImage(java.awt.Image image) {
    BufferedImage bufferedImage;
    if (image instanceof BufferedImage) {
      bufferedImage = (BufferedImage) image;
    } else {
      waitForImage(image);
      // prepare dimensions
      int w = image.getWidth(null);
      int h = image.getHeight(null);
      // draw into BufferedImage
      bufferedImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
      Graphics2D g2 = bufferedImage.createGraphics();
      g2.drawImage(image, 0, 0, null);
      // done
      g2.dispose();
    }
    return bufferedImage;
  }

  /**
   * Waits until image is fully loaded, so ready for drawing.
   */
  private static void waitForImage(java.awt.Image image) {
    BufferedImage bufferedImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = bufferedImage.createGraphics();
    // prepare observer
    final Object done = new Object();
    ImageObserver imageObserver = new ImageObserver() {
      public boolean imageUpdate(java.awt.Image img, int flags, int x, int y, int width, int height) {
        if (flags < ALLBITS) {
          return true;
        } else {
          synchronized (done) {
            done.notify();
          }
          return false;
        }
      }
    };
    // draw Image with wait
    synchronized (done) {
      boolean completelyLoaded = g2.drawImage(image, 0, 0, imageObserver);
      if (!completelyLoaded) {
        while (true) {
          try {
            done.wait(0);
            break;
          } catch (InterruptedException e) {
          }
        }
      }
    }
    // clean up
    g2.dispose();
  }

  /**
   * @return the SWT {@link Image} for AWT icon.
   */
  public static Image convertToSWT(javax.swing.Icon icon) {
    // prepare Swing image from Icon
    BufferedImage awtImage;
    {
      int width = Math.max(1, icon.getIconWidth());
      int height = Math.max(1, icon.getIconHeight());
      awtImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
      Graphics2D graphics2D = awtImage.createGraphics();
      icon.paintIcon(null, graphics2D, 0, 0);
      graphics2D.dispose();
    }
    // convert to SWT Image
    return convertToSWT(awtImage);
  }

  public static BufferedImage convertToAWT(ImageData data) {
    ColorModel colorModel = null;
    PaletteData palette = data.palette;
    if (palette.isDirect) {
      colorModel =
          new DirectColorModel(data.depth,
              palette.redMask,
              palette.greenMask,
              palette.blueMask,
              data.alphaData == null ? 0 : 0xff000000);
      BufferedImage bufferedImage =
          new BufferedImage(colorModel, colorModel.createCompatibleWritableRaster(
              data.width,
              data.height), false, null);
      WritableRaster raster = bufferedImage.getRaster();
      int[] pixelArray = new int[4];
      for (int y = 0; y < data.height; y++) {
        for (int x = 0; x < data.width; x++) {
          int pixel = data.getPixel(x, y);
          RGB rgb = palette.getRGB(pixel);
          pixelArray[0] = rgb.red;
          pixelArray[1] = rgb.green;
          pixelArray[2] = rgb.blue;
          if (data.alphaData != null) {
            pixelArray[3] = data.getAlpha(x, y);
          } else {
            pixelArray[3] = 0xff;
          }
          raster.setPixels(x, y, 1, 1, pixelArray);
        }
      }
      return bufferedImage;
    } else {
      RGB[] rgbs = palette.getRGBs();
      byte[] red = new byte[rgbs.length];
      byte[] green = new byte[rgbs.length];
      byte[] blue = new byte[rgbs.length];
      for (int i = 0; i < rgbs.length; i++) {
        RGB rgb = rgbs[i];
        red[i] = (byte) rgb.red;
        green[i] = (byte) rgb.green;
        blue[i] = (byte) rgb.blue;
      }
      if (data.transparentPixel != -1) {
        colorModel =
            new IndexColorModel(data.depth, rgbs.length, red, green, blue, data.transparentPixel);
      } else {
        colorModel = new IndexColorModel(data.depth, rgbs.length, red, green, blue);
      }
      BufferedImage bufferedImage =
          new BufferedImage(colorModel, colorModel.createCompatibleWritableRaster(
              data.width,
              data.height), false, null);
      WritableRaster raster = bufferedImage.getRaster();
      int[] pixelArray = new int[1];
      for (int y = 0; y < data.height; y++) {
        for (int x = 0; x < data.width; x++) {
          int pixel = data.getPixel(x, y);
          pixelArray[0] = pixel;
          raster.setPixel(x, y, pixelArray);
        }
      }
      return bufferedImage;
    }
  }
}
