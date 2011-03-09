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
package org.eclipse.wb.tests.designer.swt.model.property;

import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.utils.ManagerUtils;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.swt.SWT;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;

/**
 * Tests for {@link org.eclipse.wb.swt.SWTResourceManager}.
 * 
 * @author lobas_av
 */
public class SWTResourceManagerTest extends RcpModelTest {
  private Class<?> DisplayClass;
  private Object m_defaultDisplay;
  private Class<?> SWTManagerClass;
  private Class<?> ImageClass;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    CompositeInfo shell =
        parseComposite(
            "// filler filler filler",
            "public class Test extends Shell {",
            "  public Test() {",
            "  }",
            "}");
    // add SWTResourceManager
    ManagerUtils.ensure_SWTResourceManager(shell);
    // load classes
    DisplayClass = m_lastLoader.loadClass("org.eclipse.swt.widgets.Display");
    ImageClass = m_lastLoader.loadClass("org.eclipse.swt.graphics.Image");
    SWTManagerClass = m_lastLoader.loadClass("org.eclipse.wb.swt.SWTResourceManager");
    m_defaultDisplay = ReflectionUtils.invokeMethod(DisplayClass, "getDefault()");
  }

  @Override
  protected void tearDown() throws Exception {
    if (m_testProject != null && SWTManagerClass != null) {
      ReflectionUtils.invokeMethod(SWTManagerClass, "dispose()");
    }
    super.tearDown();
    // clear fields
    DisplayClass = null;
    SWTManagerClass = null;
    ImageClass = null;
    m_defaultDisplay = null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Exit zone :-) XXX
  //
  ////////////////////////////////////////////////////////////////////////////
  public void _test_exit() throws Exception {
    System.exit(0);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Color
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_systemColor() throws Exception {
    // prepare color over Display
    Object expectedColor =
        ReflectionUtils.invokeMethod(m_defaultDisplay, "getSystemColor(int)", SWT.COLOR_RED);
    // prepare color over SWTResourceManager
    Object actualColor =
        ReflectionUtils.invokeMethod(SWTManagerClass, "getColor(int)", SWT.COLOR_RED);
    // check object
    assertEquals(expectedColor, actualColor);
  }

  public void test_getColor_ints() throws Exception {
    // create color over SWTResourceManager
    Object color = ReflectionUtils.invokeMethod(SWTManagerClass, "getColor(int,int,int)", 1, 2, 3);
    // check create
    assertNotNull(color);
    // check RGB
    assertEquals(1, ReflectionUtils.invokeMethod(color, "getRed()"));
    assertEquals(2, ReflectionUtils.invokeMethod(color, "getGreen()"));
    assertEquals(3, ReflectionUtils.invokeMethod(color, "getBlue()"));
    // check state
    assertFalse((Boolean) ReflectionUtils.invokeMethod(color, "isDisposed()"));
    assertSame(
        color,
        ReflectionUtils.invokeMethod(SWTManagerClass, "getColor(int,int,int)", 1, 2, 3));
  }

  public void test_getColor_RGB() throws Exception {
    // prepare RGB class
    Class<?> RGBClass = m_lastLoader.loadClass("org.eclipse.swt.graphics.RGB");
    // create RGB
    Object rgb =
        ReflectionUtils.getConstructorBySignature(RGBClass, "<init>(int,int,int)").newInstance(
            1,
            2,
            3);
    // create color over SWTResourceManager
    Object color =
        ReflectionUtils.invokeMethod(SWTManagerClass, "getColor(org.eclipse.swt.graphics.RGB)", rgb);
    // check create
    assertNotNull(color);
    // check RGB
    assertEquals(1, ReflectionUtils.invokeMethod(color, "getRed()"));
    assertEquals(2, ReflectionUtils.invokeMethod(color, "getGreen()"));
    assertEquals(3, ReflectionUtils.invokeMethod(color, "getBlue()"));
    // check state
    assertFalse((Boolean) ReflectionUtils.invokeMethod(color, "isDisposed()"));
    assertSame(
        color,
        ReflectionUtils.invokeMethod(SWTManagerClass, "getColor(org.eclipse.swt.graphics.RGB)", rgb));
    assertSame(
        color,
        ReflectionUtils.invokeMethod(SWTManagerClass, "getColor(int,int,int)", 1, 2, 3));
  }

  public void test_disposeColors() throws Exception {
    // create color over SWTResourceManager
    Object color = ReflectionUtils.invokeMethod(SWTManagerClass, "getColor(int,int,int)", 1, 2, 3);
    // check create
    assertNotNull(color);
    // check RGB
    assertEquals(1, ReflectionUtils.invokeMethod(color, "getRed()"));
    assertEquals(2, ReflectionUtils.invokeMethod(color, "getGreen()"));
    assertEquals(3, ReflectionUtils.invokeMethod(color, "getBlue()"));
    // check state
    assertFalse((Boolean) ReflectionUtils.invokeMethod(color, "isDisposed()"));
    // check internal state of SWTResourceManager
    Object colorMap = ReflectionUtils.getFieldObject(SWTManagerClass, "m_colorMap");
    assertEquals(1, ReflectionUtils.invokeMethod(colorMap, "size()"));
    // dispose color resource's
    ReflectionUtils.invokeMethod(SWTManagerClass, "disposeColors()");
    // check internal state of SWTResourceManager
    assertEquals(0, ReflectionUtils.invokeMethod(colorMap, "size()"));
    // check new color state
    assertTrue((Boolean) ReflectionUtils.invokeMethod(color, "isDisposed()"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Image
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getImage_absolute() throws Exception {
    // create temp image into filesystem
    File imageFile = ImagePropertyEditorTest.createTempImage();
    try {
      // prepare path
      String path = imageFile.getCanonicalPath();
      // create image over SWTResourceManager
      Object image =
          ReflectionUtils.invokeMethod(SWTManagerClass, "getImage(java.lang.String)", path);
      // check create
      assertNotNull(image);
      // check state
      assertFalse((Boolean) ReflectionUtils.invokeMethod(image, "isDisposed()"));
      assertSame(
          image,
          ReflectionUtils.invokeMethod(SWTManagerClass, "getImage(java.lang.String)", path));
      // load image directly over Image
      Object directImage =
          ReflectionUtils.getConstructorBySignature(
              ImageClass,
              "<init>(org.eclipse.swt.graphics.Device,java.lang.String)").newInstance(null, path);
      // check equals images
      try {
        assertEqualsImage(image, directImage);
      } finally {
        // dispose direct image
        ReflectionUtils.invokeMethod(directImage, "dispose()");
      }
    } finally {
      // delete temp file
      imageFile.delete();
    }
  }

  public void test_getImage_classpath() throws Exception {
    // create image over SWTResourceManager
    Object image =
        ReflectionUtils.invokeMethod(
            SWTManagerClass,
            "getImage(java.lang.Class,java.lang.String)",
            SWTManagerClass,
            "/javax/swing/plaf/basic/icons/JavaCup16.png");
    // check create
    assertNotNull(image);
    // check state
    assertFalse((Boolean) ReflectionUtils.invokeMethod(image, "isDisposed()"));
    assertSame(image, ReflectionUtils.invokeMethod(
        SWTManagerClass,
        "getImage(java.lang.Class,java.lang.String)",
        SWTManagerClass,
        "/javax/swing/plaf/basic/icons/JavaCup16.png"));
    // load image directly over Image 
    Object directImage =
        ReflectionUtils.getConstructorBySignature(
            ImageClass,
            "<init>(org.eclipse.swt.graphics.Device,java.io.InputStream)").newInstance(
            null,
            getClass().getResourceAsStream("/javax/swing/plaf/basic/icons/JavaCup16.png"));
    // check equals images
    try {
      assertEqualsImage(image, directImage);
    } finally {
      ReflectionUtils.invokeMethod(directImage, "dispose()");
    }
    // check work SWTResourceManager with more images
    Object image1 =
        ReflectionUtils.invokeMethod(
            SWTManagerClass,
            "getImage(java.lang.Class,java.lang.String)",
            SWTManagerClass,
            "/javax/swing/plaf/basic/icons/image-failed.png");
    assertNotNull(image1);
    assertFalse((Boolean) ReflectionUtils.invokeMethod(image1, "isDisposed()"));
    assertSame(image1, ReflectionUtils.invokeMethod(
        SWTManagerClass,
        "getImage(java.lang.Class,java.lang.String)",
        SWTManagerClass,
        "/javax/swing/plaf/basic/icons/image-failed.png"));
    assertNotSame(image, image1);
  }

  public void test_getImage_getMissingImage() throws Exception {
    // load first image with bad location
    Object image =
        ReflectionUtils.invokeMethod(SWTManagerClass, "getImage(java.lang.String)", "xxx:xxx:xxx");
    assertNotNull(image);
    assertFalse((Boolean) ReflectionUtils.invokeMethod(image, "isDisposed()"));
    assertSame(
        image,
        ReflectionUtils.invokeMethod(SWTManagerClass, "getImage(java.lang.String)", "xxx:xxx:xxx"));
    // load second image with bad location
    Object image1 =
        ReflectionUtils.invokeMethod(
            SWTManagerClass,
            "getImage(java.lang.Class,java.lang.String)",
            SWTManagerClass,
            "/yyy|yyy|yyy");
    assertNotNull(image1);
    assertSame(image1, ReflectionUtils.invokeMethod(
        SWTManagerClass,
        "getImage(java.lang.Class,java.lang.String)",
        SWTManagerClass,
        "/yyy|yyy|yyy"));
    assertFalse((Boolean) ReflectionUtils.invokeMethod(image1, "isDisposed()"));
    // check equals wrong images
    assertNotSame(image, image1);
    assertEqualsImage(image, image1);
  }

  public void test_disposeImages() throws Exception {
    // create image over SWTResourceManager
    Object image =
        ReflectionUtils.invokeMethod(
            SWTManagerClass,
            "getImage(java.lang.Class,java.lang.String)",
            SWTManagerClass,
            "/javax/swing/plaf/basic/icons/JavaCup16.png");
    // check create
    assertNotNull(image);
    // check state
    assertFalse((Boolean) ReflectionUtils.invokeMethod(image, "isDisposed()"));
    // check internal state of SWTResourceManager
    Object imageMap = ReflectionUtils.getFieldObject(SWTManagerClass, "m_imageMap");
    assertEquals(1, ReflectionUtils.invokeMethod(imageMap, "size()"));
    // dispose image resource's
    ReflectionUtils.invokeMethod(SWTManagerClass, "disposeImages()");
    // check internal state of SWTResourceManager
    assertEquals(0, ReflectionUtils.invokeMethod(imageMap, "size()"));
    // check new image state
    assertTrue((Boolean) ReflectionUtils.invokeMethod(image, "isDisposed()"));
  }

  private static final String[] IMAGE_DATA_FIELDS = {
      "width",
      "height",
      "depth",
      "scanlinePad",
      "bytesPerLine",
      "transparentPixel",
      "maskPad",
      "alpha",
      "type",
      "x",
      "y"};

  private static void assertEqualsImage(Object image1, Object image2) throws Exception {
    Object data1 = ReflectionUtils.invokeMethod(image1, "getImageData()");
    Object data2 = ReflectionUtils.invokeMethod(image2, "getImageData()");
    for (int i = 0; i < IMAGE_DATA_FIELDS.length; i++) {
      String field = IMAGE_DATA_FIELDS[i];
      assertEquals(
          field,
          ReflectionUtils.getFieldInt(data1, field),
          ReflectionUtils.getFieldInt(data2, field));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Decorate Image
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_decorateImage_wrongCorner() throws Exception {
    // check out of range corner values to left
    try {
      ReflectionUtils.invokeMethod(
          SWTManagerClass,
          "decorateImage(org.eclipse.swt.graphics.Image,org.eclipse.swt.graphics.Image,int)",
          null,
          null,
          0);
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Wrong decorate corner", e.getMessage());
    }
    // check out of range corner values to right
    try {
      ReflectionUtils.invokeMethod(
          SWTManagerClass,
          "decorateImage(org.eclipse.swt.graphics.Image,org.eclipse.swt.graphics.Image,int)",
          null,
          null,
          5);
      fail();
    } catch (IllegalArgumentException e) {
      assertEquals("Wrong decorate corner", e.getMessage());
    }
  }

  public void test_decorateImage() throws Exception {
    // prepare default corner for decorateImage()
    int corner = ReflectionUtils.getFieldInt(SWTManagerClass, "BOTTOM_RIGHT");
    // create base and decorator images
    Object[] images = createImages();
    Object base = images[0];
    Object decorator = images[1];
    try {
      // decorate image over decorateImage(Image, Image, corner)
      Object image =
          ReflectionUtils.invokeMethod(
              SWTManagerClass,
              "decorateImage(org.eclipse.swt.graphics.Image,org.eclipse.swt.graphics.Image,int)",
              base,
              decorator,
              corner);
      // check create
      assertNotNull(image);
      // check state
      assertFalse((Boolean) ReflectionUtils.invokeMethod(image, "isDisposed()"));
      // check equals with image created over decorateImage(Image, Image)
      assertSame(image, ReflectionUtils.invokeMethod(
          SWTManagerClass,
          "decorateImage(org.eclipse.swt.graphics.Image,org.eclipse.swt.graphics.Image)",
          base,
          decorator));
    } finally {
      // dispose base and decorator images
      ReflectionUtils.invokeMethod(base, "dispose()");
      ReflectionUtils.invokeMethod(decorator, "dispose()");
    }
  }

  public void test_decorateImage2() throws Exception {
    // prepare tested corner's
    int corner = ReflectionUtils.getFieldInt(SWTManagerClass, "BOTTOM_RIGHT");
    int corner1 = ReflectionUtils.getFieldInt(SWTManagerClass, "TOP_LEFT");
    // create base and decorator images
    Object[] images = createImages();
    Object base = images[0];
    Object decorator = images[1];
    try {
      // decorate BOTTOM_RIGHT image
      Object image =
          ReflectionUtils.invokeMethod(
              SWTManagerClass,
              "decorateImage(org.eclipse.swt.graphics.Image,org.eclipse.swt.graphics.Image,int)",
              base,
              decorator,
              corner);
      // check create
      assertNotNull(image);
      // check state
      assertSame(image, ReflectionUtils.invokeMethod(
          SWTManagerClass,
          "decorateImage(org.eclipse.swt.graphics.Image,org.eclipse.swt.graphics.Image,int)",
          base,
          decorator,
          corner));
      // decorate TOP_LEFT image
      Object image1 =
          ReflectionUtils.invokeMethod(
              SWTManagerClass,
              "decorateImage(org.eclipse.swt.graphics.Image,org.eclipse.swt.graphics.Image,int)",
              base,
              decorator,
              corner1);
      // check create
      assertNotNull(image1);
      assertSame(image1, ReflectionUtils.invokeMethod(
          SWTManagerClass,
          "decorateImage(org.eclipse.swt.graphics.Image,org.eclipse.swt.graphics.Image,int)",
          base,
          decorator,
          corner1));
      // check work SWTResourceManager.decorateImage() with multi images
      assertNotSame(image, image1);
    } finally {
      // dispose base and decorator images
      ReflectionUtils.invokeMethod(base, "dispose()");
      ReflectionUtils.invokeMethod(decorator, "dispose()");
    }
  }

  public void test_decorateImage_TOP_LEFT() throws Exception {
    int corner = ReflectionUtils.getFieldInt(SWTManagerClass, "TOP_LEFT");
    test_decorateImage(corner);
  }

  public void test_decorateImage_TOP_RIGHT() throws Exception {
    int corner = ReflectionUtils.getFieldInt(SWTManagerClass, "TOP_RIGHT");
    test_decorateImage(corner);
  }

  public void test_decorateImage_BOTTOM_LEFT() throws Exception {
    int corner = ReflectionUtils.getFieldInt(SWTManagerClass, "BOTTOM_LEFT");
    test_decorateImage(corner);
  }

  public void test_decorateImage_BOTTOM_RIGHT() throws Exception {
    int corner = ReflectionUtils.getFieldInt(SWTManagerClass, "BOTTOM_RIGHT");
    test_decorateImage(corner);
  }

  private void test_decorateImage(int corner) throws Exception {
    // create base and decorator images
    Object[] images = createImages();
    Object base = images[0];
    Object decorator = images[1];
    try {
      // decorate image with given corner
      Object image =
          ReflectionUtils.invokeMethod(
              SWTManagerClass,
              "decorateImage(org.eclipse.swt.graphics.Image,org.eclipse.swt.graphics.Image,int)",
              base,
              decorator,
              corner);
      // check state
      assertNotNull(image);
      assertFalse((Boolean) ReflectionUtils.invokeMethod(image, "isDisposed()"));
      assertSame(image, ReflectionUtils.invokeMethod(
          SWTManagerClass,
          "decorateImage(org.eclipse.swt.graphics.Image,org.eclipse.swt.graphics.Image,int)",
          base,
          decorator,
          corner));
      // prepare decorator, base and expected pixel's
      int blackPixel = getPixels(decorator)[0][0];
      int[][] result = getPixels(base);
      if (corner == 1) { // TOP_LEFT
        result[0][0] = blackPixel;
      } else if (corner == 2) { // TOP_RIGHT
        result[2][0] = blackPixel;
      } else if (corner == 3) { // BOTTOM_LEFT
        result[0][2] = blackPixel;
      } else if (corner == 4) { // BOTTOM_RIGHT
        result[2][2] = blackPixel;
      }
      // check expected and actual pixel's
      assertEquals(getText(result), getText(getPixels(image)));
    } finally {
      // dispose base and decorator images
      ReflectionUtils.invokeMethod(base, "dispose()");
      ReflectionUtils.invokeMethod(decorator, "dispose()");
    }
  }

  /**
   * @return base(3x3, white) and decorate(1x1, black) images.
   */
  private Object[] createImages() throws Exception {
    // prepare image constructor
    Constructor<?> constructor =
        ReflectionUtils.getConstructorBySignature(
            ImageClass,
            "<init>(org.eclipse.swt.graphics.Device,int,int)");
    // create base image
    Object baseImage = constructor.newInstance(null, 3, 3);
    fillImage(baseImage, SWT.COLOR_WHITE, 3, 3);
    // create decorate image
    Object decoratorImage = constructor.newInstance(null, 1, 1);
    fillImage(decoratorImage, SWT.COLOR_BLACK, 1, 1);
    //
    return new Object[]{baseImage, decoratorImage};
  }

  /**
   * Fill given image for given color.
   */
  private void fillImage(Object image, int color, int width, int height) throws Exception {
    // create GC
    Class<?> GCClass = m_lastLoader.loadClass("org.eclipse.swt.graphics.GC");
    Object gc =
        ReflectionUtils.getConstructorBySignature(
            GCClass,
            "<init>(org.eclipse.swt.graphics.Drawable)").newInstance(image);
    // prepare color
    Object colorObject =
        ReflectionUtils.invokeMethod(m_defaultDisplay, "getSystemColor(int)", color);
    // fill
    ReflectionUtils.invokeMethod(gc, "setBackground(org.eclipse.swt.graphics.Color)", colorObject);
    ReflectionUtils.invokeMethod(gc, "fillRectangle(int,int,int,int)", 0, 0, width, height);
    // release GC
    ReflectionUtils.invokeMethod(gc, "dispose()");
  }

  /**
   * @return 2D int array of pixel's for given image.
   */
  private static int[][] getPixels(Object image) throws Exception {
    // prepare image data
    Object data = ReflectionUtils.invokeMethod(image, "getImageData()");
    // prepare image palette
    Object palette = ReflectionUtils.getFieldObject(data, "palette");
    // prepare image size
    int width = ReflectionUtils.getFieldInt(data, "width");
    int height = ReflectionUtils.getFieldInt(data, "height");
    // create result pixel's
    int[][] pixels = new int[width][height];
    for (int i = 0; i < width; i++) {
      for (int j = 0; j < height; j++) {
        // prepare pixel value
        int pixel = (Integer) ReflectionUtils.invokeMethod(data, "getPixel(int,int)", i, j);
        // convert pixel to color value (red component)
        Object rgb = ReflectionUtils.invokeMethod(palette, "getRGB(int)", pixel);
        pixels[i][j] = ReflectionUtils.getFieldInt(rgb, "red");
      }
    }
    return pixels;
  }

  /**
   * @return string presentation for 2D int array.
   */
  private static String getText(int[][] data) {
    StringBuffer buffer = new StringBuffer();
    for (int i = 0; i < data.length; i++) {
      int[] line = data[i];
      for (int j = 0; j < line.length; j++) {
        buffer.append(line[j]);
        buffer.append(' ');
      }
      buffer.append('\n');
    }
    return buffer.toString();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Font
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getFont() throws Exception {
    // create font over SWTResouceManager
    Object font =
        ReflectionUtils.invokeMethod(
            SWTManagerClass,
            "getFont(java.lang.String,int,int)",
            "Courier New",
            12,
            SWT.ITALIC);
    // check create
    assertNotNull(font);
    // check state
    assertFalse((Boolean) ReflectionUtils.invokeMethod(font, "isDisposed()"));
    // check font metric
    checkEqulas("Courier New", 12, SWT.ITALIC, font);
    assertSame(font, ReflectionUtils.invokeMethod(
        SWTManagerClass,
        "getFont(java.lang.String,int,int)",
        "Courier New",
        12,
        SWT.ITALIC));
    // check work SWTResourceManager with more fonts
    Object font1 =
        ReflectionUtils.invokeMethod(
            SWTManagerClass,
            "getFont(java.lang.String,int,int)",
            "Arial",
            14,
            SWT.NORMAL);
    // check create
    assertNotNull(font1);
    // check state
    assertFalse((Boolean) ReflectionUtils.invokeMethod(font1, "isDisposed()"));
    // check font metric
    checkEqulas("Arial", 14, SWT.NORMAL, font1);
    assertSame(font1, ReflectionUtils.invokeMethod(
        SWTManagerClass,
        "getFont(java.lang.String,int,int)",
        "Arial",
        14,
        SWT.NORMAL));
  }

  public void test_getBoldFont() throws Exception {
    // prepare any font
    Object font = ReflectionUtils.invokeMethod(m_defaultDisplay, "getSystemFont()");
    // create bold version over SWTResourceManager
    Object boldFont =
        ReflectionUtils.invokeMethod(
            SWTManagerClass,
            "getBoldFont(org.eclipse.swt.graphics.Font)",
            font);
    // check create
    assertNotNull(boldFont);
    // check state
    assertFalse((Boolean) ReflectionUtils.invokeMethod(boldFont, "isDisposed()"));
    // check font metric
    Object fontData = Array.get(ReflectionUtils.invokeMethod(font, "getFontData()"), 0);
    checkEqulas(
        (String) ReflectionUtils.invokeMethod(fontData, "getName()"),
        (Integer) ReflectionUtils.invokeMethod(fontData, "getHeight()"),
        SWT.BOLD,
        boldFont);
    assertSame(boldFont, ReflectionUtils.invokeMethod(
        SWTManagerClass,
        "getBoldFont(org.eclipse.swt.graphics.Font)",
        font));
  }

  public void test_disposeFonts() throws Exception {
    // create font over SWTResouceManager
    Object font =
        ReflectionUtils.invokeMethod(
            SWTManagerClass,
            "getFont(java.lang.String,int,int)",
            "Courier New",
            12,
            SWT.ITALIC);
    // check state
    assertFalse((Boolean) ReflectionUtils.invokeMethod(font, "isDisposed()"));
    // create bold version over SWTResourceManager
    Object boldFont =
        ReflectionUtils.invokeMethod(
            SWTManagerClass,
            "getBoldFont(org.eclipse.swt.graphics.Font)",
            font);
    // check state
    assertFalse((Boolean) ReflectionUtils.invokeMethod(boldFont, "isDisposed()"));
    // check internal state of SWTResourceManager
    Object fontMap = ReflectionUtils.getFieldObject(SWTManagerClass, "m_fontMap");
    Object fontToBoldFontMap =
        ReflectionUtils.getFieldObject(SWTManagerClass, "m_fontToBoldFontMap");
    assertEquals(1, ReflectionUtils.invokeMethod(fontMap, "size()"));
    assertEquals(1, ReflectionUtils.invokeMethod(fontToBoldFontMap, "size()"));
    // dispose font resource's
    ReflectionUtils.invokeMethod(SWTManagerClass, "disposeFonts()");
    // check internal state of SWTResourceManager
    assertEquals(0, ReflectionUtils.invokeMethod(fontMap, "size()"));
    assertEquals(0, ReflectionUtils.invokeMethod(fontToBoldFontMap, "size()"));
    // check new font state
    assertTrue((Boolean) ReflectionUtils.invokeMethod(font, "isDisposed()"));
    assertTrue((Boolean) ReflectionUtils.invokeMethod(boldFont, "isDisposed()"));
  }

  private static void checkEqulas(String name, int height, int style, Object font) throws Exception {
    Object fontData = Array.get(ReflectionUtils.invokeMethod(font, "getFontData()"), 0);
    assertEquals(name, ReflectionUtils.invokeMethod(fontData, "getName()"));
    assertEquals(height, ReflectionUtils.invokeMethod(fontData, "getHeight()"));
    assertEquals(style, ReflectionUtils.invokeMethod(fontData, "getStyle()"));
  }
}