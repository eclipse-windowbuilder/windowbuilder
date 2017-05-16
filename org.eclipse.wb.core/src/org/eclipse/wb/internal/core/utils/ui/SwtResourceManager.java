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

import com.google.common.collect.Maps;

import org.eclipse.wb.internal.core.DesignerPlugin;

import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Utility class for managing OS resources associated with SWT controls such as colors, fonts,
 * images, etc.
 * <p>
 * !!! IMPORTANT !!! Application code must explicitly invoke the <code>dispose()</code> method to
 * release the operating system resources managed by cached objects when those objects and OS
 * resources are no longer needed (e.g. on application shutdown)
 * <p>
 * This class may be freely distributed as part of any application or plugin.
 * <p>
 *
 * @author scheglov_ke
 * @author Dan Rubel
 * @coverage core.ui
 */
public class SwtResourceManager {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Color
  //
  ////////////////////////////////////////////////////////////////////////////
  private static Map<RGB, Color> m_colorMap = Maps.newHashMap();

  /**
   * Returns the system {@link Color} matching the specific ID.
   *
   * @param systemColorID
   *          the ID value for the color
   * @return the system {@link Color} matching the specific ID
   */
  public static Color getColor(int systemColorID) {
    Display display = Display.getCurrent();
    return display.getSystemColor(systemColorID);
  }

  /**
   * Returns a {@link Color} given its red, green and blue component values.
   *
   * @param r
   *          the red component of the color
   * @param g
   *          the green component of the color
   * @param b
   *          the blue component of the color
   * @return the {@link Color} matching the given red, green and blue component values
   */
  public static Color getColor(int r, int g, int b) {
    return getColor(new RGB(r, g, b));
  }

  /**
   * Returns a {@link Color} given its RGB value.
   *
   * @param rgb
   *          the {@link RGB} value of the color
   * @return the {@link Color} matching the RGB value
   */
  public static Color getColor(RGB rgb) {
    Color color = m_colorMap.get(rgb);
    if (color == null) {
      Display display = Display.getCurrent();
      color = new Color(display, rgb);
      m_colorMap.put(rgb, color);
    }
    return color;
  }

  /**
   * Dispose of all the cached {@link Color}'s.
   */
  public static void disposeColors() {
    for (Color color : m_colorMap.values()) {
      color.dispose();
    }
    m_colorMap.clear();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Image
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Maps image paths to images.
   */
  private static Map<String, Image> m_imageMap = Maps.newHashMap();
  private static Map<Integer, Image> m_systemImageMap = Maps.newHashMap();

  /**
   * Get an <code>Image</code> from the provide SWT image constant.
   *
   * @param imageID
   *          the SWT image constant.
   * @return image the image.
   */
  public static Image getImage(final int imageID) {
    final Image[] image = new Image[1];
    image[0] = m_systemImageMap.get(imageID);
    if (image[0] == null) {
      final Display display = DesignerPlugin.getStandardDisplay();
      display.syncExec(new Runnable() {
        public void run() {
          image[0] = display.getSystemImage(imageID);
        }
      });
      m_systemImageMap.put(imageID, image[0]);
    }
    return image[0];
  }

  /**
   * Returns an {@link Image} encoded by the specified {@link InputStream}.
   *
   * @param stream
   *          the {@link InputStream} encoding the image data
   * @return the {@link Image} encoded by the specified input stream
   */
  protected static Image getImage(InputStream stream) throws IOException {
    try {
      Display display = Display.getCurrent();
      ImageData data = new ImageData(stream);
      if (data.transparentPixel > 0) {
        return new Image(display, data, data.getTransparencyMask());
      }
      return new Image(display, data);
    } finally {
      stream.close();
    }
  }

  /**
   * Returns an {@link Image} stored in the file at the specified path.
   *
   * @param path
   *          the path to the image file
   * @return the {@link Image} stored in the file at the specified path
   */
  public static Image getImage(String path) {
    Image image = m_imageMap.get(path);
    if (image == null) {
      try {
        image = getImage(new FileInputStream(path));
        m_imageMap.put(path, image);
      } catch (Exception e) {
        image = getMissingImage();
        m_imageMap.put(path, image);
      }
    }
    return image;
  }

  /**
   * Returns an {@link Image} stored in the file at the specified path relative to the specified
   * class.
   *
   * @param clazz
   *          the {@link Class} relative to which to find the image
   * @param path
   *          the path to the image file, if starts with <code>'/'</code>
   * @return the {@link Image} stored in the file at the specified path
   */
  public static Image getImage(Class<?> clazz, String path) {
    String key = clazz.getName() + '|' + path;
    Image image = m_imageMap.get(key);
    if (image == null) {
      try {
        image = getImage(clazz.getResourceAsStream(path));
        m_imageMap.put(key, image);
      } catch (Exception e) {
        image = getMissingImage();
        m_imageMap.put(key, image);
      }
    }
    return image;
  }

  private static final int MISSING_IMAGE_SIZE = 10;

  /**
   * @return the small {@link Image} that can be used as placeholder for missing image.
   */
  private static Image getMissingImage() {
    Image image = new Image(Display.getCurrent(), MISSING_IMAGE_SIZE, MISSING_IMAGE_SIZE);
    //
    GC gc = new GC(image);
    gc.setBackground(getColor(SWT.COLOR_RED));
    gc.fillRectangle(0, 0, MISSING_IMAGE_SIZE, MISSING_IMAGE_SIZE);
    gc.dispose();
    //
    return image;
  }

  /**
   * Style constant for placing decorator image in top left corner of base image.
   */
  public static final int TOP_LEFT = 1;
  /**
   * Style constant for placing decorator image in top right corner of base image.
   */
  public static final int TOP_RIGHT = 2;
  /**
   * Style constant for placing decorator image in bottom left corner of base image.
   */
  public static final int BOTTOM_LEFT = 3;
  /**
   * Style constant for placing decorator image in bottom right corner of base image.
   */
  public static final int BOTTOM_RIGHT = 4;
  /**
   * Internal value.
   */
  protected static final int LAST_CORNER_KEY = 5;
  /**
   * Maps images to decorated images.
   */
  @SuppressWarnings("unchecked")
  private static Map<Image, Map<Image, Image>>[] m_decoratedImageMap = new Map[LAST_CORNER_KEY];

  /**
   * Returns an {@link Image} composed of a base image decorated by another image.
   *
   * @param baseImage
   *          the base {@link Image} that should be decorated
   * @param decorator
   *          the {@link Image} to decorate the base image
   * @param corner
   *          the corner to place decorator image
   * @return the resulting decorated {@link Image}
   */
  public static Image decorateImage(final Image baseImage, final Image decorator, final int corner) {
    if (corner <= 0 || corner >= LAST_CORNER_KEY) {
      throw new IllegalArgumentException("Wrong decorate corner");
    }
    Map<Image, Map<Image, Image>> cornerDecoratedImageMap = m_decoratedImageMap[corner];
    if (cornerDecoratedImageMap == null) {
      cornerDecoratedImageMap = Maps.newHashMap();
      m_decoratedImageMap[corner] = cornerDecoratedImageMap;
    }
    Map<Image, Image> decoratedMap = cornerDecoratedImageMap.get(baseImage);
    if (decoratedMap == null) {
      decoratedMap = Maps.newHashMap();
      cornerDecoratedImageMap.put(baseImage, decoratedMap);
    }
    //
    Image result = decoratedMap.get(decorator);
    if (result == null) {
      final Rectangle bib = baseImage.getBounds();
      final Rectangle dib = decorator.getBounds();
      final Point baseImageSize = new Point(bib.width, bib.height);
      CompositeImageDescriptor compositImageDesc = new CompositeImageDescriptor() {
        @Override
        protected void drawCompositeImage(int width, int height) {
          drawImage(baseImage.getImageData(), 0, 0);
          if (corner == TOP_LEFT) {
            drawImage(decorator.getImageData(), 0, 0);
          } else if (corner == TOP_RIGHT) {
            drawImage(decorator.getImageData(), bib.width - dib.width, 0);
          } else if (corner == BOTTOM_LEFT) {
            drawImage(decorator.getImageData(), 0, bib.height - dib.height);
          } else if (corner == BOTTOM_RIGHT) {
            drawImage(decorator.getImageData(), bib.width - dib.width, bib.height - dib.height);
          }
        }

        @Override
        protected Point getSize() {
          return baseImageSize;
        }
      };
      //
      result = compositImageDesc.createImage();
      decoratedMap.put(decorator, result);
    }
    return result;
  }

  /**
   * Dispose all of the cached {@link Image}'s.
   */
  public static void disposeImages() {
    // dispose loaded images
    {
      for (Image image : m_imageMap.values()) {
        image.dispose();
      }
      m_imageMap.clear();
    }
    // dispose decorated images
    for (int i = 0; i < m_decoratedImageMap.length; i++) {
      Map<Image, Map<Image, Image>> cornerDecoratedImageMap = m_decoratedImageMap[i];
      if (cornerDecoratedImageMap != null) {
        for (Map<Image, Image> decoratedMap : cornerDecoratedImageMap.values()) {
          for (Image decoratedImage : decoratedMap.values()) {
            decoratedImage.dispose();
          }
          decoratedMap.clear();
        }
        cornerDecoratedImageMap.clear();
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Font
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Maps font keys (constructed from font name, height, style, etc) to fonts.
   */
  private static Map<String, Font> m_fontMap = Maps.newHashMap();
  /**
   * Maps {@link FontData} to {@link Font}.
   */
  private static Map<FontData, Font> m_fontDataMap = Maps.newHashMap();
  /**
   * Maps fonts to their bold versions.
   */
  private static Map<Font, Font> m_fontToBoldFontMap = Maps.newHashMap();

  /**
   * @return the {@link Font} for given {@link FontData} array.
   */
  public static Font getFont(FontData[] fontDataArray) {
    FontData fontData = fontDataArray[0];
    Font font = m_fontDataMap.get(fontData);
    if (font == null) {
      font = new Font(null, fontDataArray);
      m_fontDataMap.put(fontData, font);
    }
    return font;
  }

  /**
   * Returns a {@link Font} based on its name, height and style.
   *
   * @param name
   *          the name of the font
   * @param height
   *          the height of the font
   * @param style
   *          the style of the font
   * @return {@link Font} The font matching the name, height and style
   */
  public static Font getFont(String name, int height, int style) {
    return getFont(name, height, style, false, false);
  }

  /**
   * Returns a {@link Font} based on its name, height and style. Windows-specific strikeout and
   * underline flags are also supported.
   *
   * @param name
   *          the name of the font
   * @param size
   *          the size of the font
   * @param style
   *          the style of the font
   * @param strikeout
   *          the strikeout flag (warning: Windows only)
   * @param underline
   *          the underline flag (warning: Windows only)
   * @return {@link Font} The font matching the name, height, style, strikeout and underline
   */
  public static Font getFont(String name, int size, int style, boolean strikeout, boolean underline) {
    String fontName = name + '|' + size + '|' + style + '|' + strikeout + '|' + underline;
    Font font = m_fontMap.get(fontName);
    if (font == null) {
      FontData fontData = new FontData(name, size, style);
      if (strikeout || underline) {
        try {
          Class<?> logFontClass = Class.forName("org.eclipse.swt.internal.win32.LOGFONT"); //$NON-NLS-1$
          Object logFont = FontData.class.getField("data").get(fontData); //$NON-NLS-1$
          if (logFont != null && logFontClass != null) {
            if (strikeout) {
              logFontClass.getField("lfStrikeOut").set(logFont, (byte) 1); //$NON-NLS-1$
            }
            if (underline) {
              logFontClass.getField("lfUnderline").set(logFont, (byte) 1); //$NON-NLS-1$
            }
          }
        } catch (Throwable e) {
        }
      }
      font = new Font(Display.getCurrent(), fontData);
      m_fontMap.put(fontName, font);
    }
    return font;
  }

  /**
   * Returns a bold version of the given {@link Font}.
   *
   * @param baseFont
   *          the {@link Font} for which a bold version is desired
   * @return the bold version of the given {@link Font}
   */
  public static Font getBoldFont(Font baseFont) {
    Font font = m_fontToBoldFontMap.get(baseFont);
    if (font == null) {
      FontData fontDatas[] = baseFont.getFontData();
      FontData data = fontDatas[0];
      font = new Font(Display.getCurrent(), data.getName(), data.getHeight(), SWT.BOLD);
      m_fontToBoldFontMap.put(baseFont, font);
    }
    return font;
  }

  /**
   * Dispose all of the cached {@link Font}'s.
   */
  public static void disposeFonts() {
    // clear map: key -> font
    for (Font font : m_fontMap.values()) {
      font.dispose();
    }
    m_fontMap.clear();
    // clear map: FontData -> font
    for (Font font : m_fontDataMap.values()) {
      font.dispose();
    }
    m_fontDataMap.clear();
    // clear bold fonts
    for (Font font : m_fontToBoldFontMap.values()) {
      font.dispose();
    }
    m_fontToBoldFontMap.clear();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // General
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Dispose of cached objects and their underlying OS resources. This should only be called when
   * the cached objects are no longer needed (e.g. on application shutdown).
   */
  public static void dispose() {
    disposeColors();
    disposeImages();
    disposeFonts();
  }
}