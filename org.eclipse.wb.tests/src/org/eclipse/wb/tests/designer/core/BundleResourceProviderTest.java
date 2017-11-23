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
package org.eclipse.wb.tests.designer.core;

import org.eclipse.wb.internal.core.BundleResourceProvider;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.check.AssertionFailedException;
import org.eclipse.wb.tests.designer.TestUtils;
import org.eclipse.wb.tests.designer.tests.DesignerTestCase;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

import static org.assertj.core.api.Assertions.assertThat;

import org.osgi.framework.Bundle;

import java.io.InputStream;

/**
 * Test for {@link BundleResourceProvider}.
 * 
 * @author scheglov_ke
 */
public class BundleResourceProviderTest extends DesignerTestCase {
  private static final String BUNDLE_ID = DesignerPlugin.PLUGIN_ID;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_noSuchBundle() throws Exception {
    try {
      BundleResourceProvider.get("no.such.bundle");
      fail();
    } catch (AssertionFailedException e) {
    }
  }

  public void test_instanceBundle() throws Exception {
    Bundle bundle = Platform.getBundle(BUNDLE_ID);
    BundleResourceProvider.get(bundle);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getFile()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link BundleResourceProvider#getFile(String)}.
   */
  public void test_getFile() throws Exception {
    BundleResourceProvider provider = BundleResourceProvider.get(BUNDLE_ID);
    // no such file
    try {
      provider.getFile("noSuchFile.txt");
    } catch (Throwable e) {
      assertThat(e.getMessage()).contains(BUNDLE_ID).contains("noSuchFile.txt");
    }
    // good file
    assertGoodFile(provider, "plugin.xml");
    assertGoodFile(provider, "icons/test.png");
    assertGoodFile(provider, "/icons/test.png");
    assertGoodFile(provider, "icons//test.png");
    assertGoodFile(provider, "//icons/test.png");
    assertGoodFile(provider, "//icons//test.png");
  }

  /**
   * Test for {@link BundleResourceProvider#getFileString(String)}.
   */
  public void test_getFileString() throws Exception {
    BundleResourceProvider provider = BundleResourceProvider.get(BUNDLE_ID);
    // no such file
    try {
      provider.getFile("noSuchFile.txt");
    } catch (Throwable e) {
      assertThat(e.getMessage()).contains(BUNDLE_ID).contains("noSuchFile.txt");
    }
    // good file
    {
      String content = provider.getFileString("plugin.xml");
      assertThat(content).contains("<!-- Extension points -->");
    }
  }

  private static void assertGoodFile(BundleResourceProvider provider, String path) throws Exception {
    InputStream stream = provider.getFile(path);
    assertNotNull(stream);
    stream.close();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Images
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link BundleResourceProvider#getImage(String)}.
   */
  public void test_getImage() throws Exception {
    BundleResourceProvider provider = BundleResourceProvider.get(BUNDLE_ID);
    // no such file
    try {
      provider.getImage("noSuchImage.png");
    } catch (Throwable e) {
      assertThat(e.getMessage()).contains(BUNDLE_ID).contains("noSuchImage.png");
    }
    // get image
    Image image = provider.getImage("icons/test.png");
    assertNotNull(image);
    // get cached image
    assertSame(image, provider.getImage("icons/test.png"));
    // path should be normalized, so same image returned
    assertSame(image, provider.getImage("/icons/test.png"));
  }

  /**
   * Test for {@link BundleResourceProvider#getImage(String)}.
   */
  public void test_getImage_disposeWhenUninstall() throws Exception {
    TestBundle testBundle = new TestBundle();
    Image image;
    try {
      testBundle.setFile("icons/test.png", TestUtils.createImagePNG(1, 1));
      testBundle.install();
      // work with Bundle
      {
        BundleResourceProvider provider = BundleResourceProvider.get(testBundle.getId());
        // get Image
        image = provider.getImage("icons/test.png");
        assertNotNull(image);
        assertFalse(image.isDisposed());
      }
    } finally {
      testBundle.dispose();
    }
    // wait for events
    waitEventLoop(0);
    // Bundle uninstalled, so Image is now disposed
    assertTrue(image.isDisposed());
  }

  /**
   * Test for {@link BundleResourceProvider#getImageDescriptor(String)}.
   */
  public void test_getImageDescriptor() throws Exception {
    BundleResourceProvider provider = BundleResourceProvider.get(BUNDLE_ID);
    // no such file
    try {
      provider.getImageDescriptor("noSuchImage.png");
    } catch (Throwable e) {
      assertThat(e.getMessage()).contains(BUNDLE_ID).contains("noSuchImage.png");
    }
    // get image
    ImageDescriptor descriptor = provider.getImageDescriptor("icons/test.png");
    assertNotNull(descriptor);
    // get cached image
    assertSame(descriptor, provider.getImageDescriptor("icons/test.png"));
    // path should be normalized, so same image returned
    assertSame(descriptor, provider.getImageDescriptor("/icons/test.png"));
  }
}
