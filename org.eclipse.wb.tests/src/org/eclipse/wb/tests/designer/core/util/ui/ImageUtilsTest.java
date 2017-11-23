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
package org.eclipse.wb.tests.designer.core.util.ui;

import org.eclipse.wb.internal.core.utils.ui.ImageUtils;
import org.eclipse.wb.tests.designer.TestUtils;
import org.eclipse.wb.tests.designer.tests.DesignerTestCase;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

/**
 * Test for {@link ImageUtils}.
 * 
 * @author scheglov_ke
 */
public class ImageUtilsTest extends DesignerTestCase {
  ////////////////////////////////////////////////////////////////////////////
  //
  // getBytesPNG()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getBytesPNG() throws Exception {
    Image image = new Image(null, 32, 16);
    try {
      byte[] bytes = ImageUtils.getBytesPNG(image);
      assertThat(bytes.length).isGreaterThan(50);
      // try to use these bytes to load Image
      {
        Image loadedImage = new Image(null, new ByteArrayInputStream(bytes));
        try {
          Rectangle bounds = loadedImage.getBounds();
          assertEquals(32, bounds.width);
          assertEquals(16, bounds.height);
        } finally {
          loadedImage.dispose();
        }
      }
    } finally {
      image.dispose();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // convertToSWT()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ImageUtils#convertToSWT(java.awt.Image)}.
   */
  public void test_convertToSWT_BufferedImage() throws Exception {
    java.awt.Image awtImage = new BufferedImage(10, 20, BufferedImage.TYPE_INT_ARGB);
    // do convert
    Image swtImage = ImageUtils.convertToSWT(awtImage);
    // has same size
    {
      Rectangle bounds = swtImage.getBounds();
      assertEquals(10, bounds.width);
      assertEquals(20, bounds.height);
    }
  }

  /**
   * Test for {@link ImageUtils#convertToSWT(java.awt.Image)}.
   */
  public void test_convertToSWT_ToolkitImage() throws Exception {
    java.awt.Image awtImage;
    {
      byte[] bytes = TestUtils.createImagePNG(10, 20);
      java.awt.Toolkit tk = java.awt.Toolkit.getDefaultToolkit();
      awtImage = tk.createImage(bytes);
    }
    // do convert
    Image swtImage = ImageUtils.convertToSWT(awtImage);
    // has same size
    {
      Rectangle bounds = swtImage.getBounds();
      assertEquals(10, bounds.width);
      assertEquals(20, bounds.height);
    }
  }
}