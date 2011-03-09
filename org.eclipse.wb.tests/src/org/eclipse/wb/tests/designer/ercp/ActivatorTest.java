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
package org.eclipse.wb.tests.designer.ercp;

import org.eclipse.wb.internal.ercp.Activator;
import org.eclipse.wb.tests.designer.tests.DesignerTestCase;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

import static org.fest.assertions.Assertions.assertThat;

import java.io.InputStream;
import java.net.URL;

/**
 * Tests for {@link Activator}.
 * 
 * @author scheglov_ke
 */
public class ActivatorTest extends DesignerTestCase {
  public void test_getDefault() throws Exception {
    assertNotNull(Activator.getDefault());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getEntry(), etc
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getEntry() throws Exception {
    URL entry = Activator.getEntry("plugin.xml");
    assertNotNull(entry);
    assertEquals("/plugin.xml", entry.getPath());
  }

  public void test_getEntry_notFound() throws Exception {
    URL entry = Activator.getEntry("no-such-file");
    assertNull(entry);
  }

  public void test_getAbsoluteEntry() throws Exception {
    URL entry = Activator.getAbsoluteEntry("plugin.xml");
    assertNotNull(entry);
    assertTrue(entry.getPath().endsWith("/org.eclipse.wb.ercp/plugin.xml"));
  }

  public void test_getAbsolutePath() throws Exception {
    String path = Activator.getAbsolutePath("plugin.xml");
    assertTrue(path.endsWith("/org.eclipse.wb.ercp/plugin.xml"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getFile()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getFile() throws Exception {
    InputStream is = Activator.getFile("plugin.xml");
    assertNotNull(is);
    is.close();
  }

  public void test_getFile_notFound() throws Exception {
    try {
      Activator.getFile("no-such-file");
      fail();
    } catch (Throwable e) {
      assertThat(e.getMessage()).contains("no-such-file");
      assertInstanceOf(NullPointerException.class, e.getCause());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getImage()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getImage() throws Exception {
    Image image = Activator.getImage("preference_transfer.png");
    assertNotNull(image);
    // check that cache is used
    assertSame(image, Activator.getImage("preference_transfer.png"));
  }

  public void test_getImage_notFound() throws Exception {
    try {
      Activator.getImage("no-such-image");
      fail();
    } catch (Throwable e) {
      assertInstanceOf(NullPointerException.class, e.getCause());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getImageDescriptor()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getImageDescriptor() throws Exception {
    ImageDescriptor imageDescriptor = Activator.getImageDescriptor("preference_transfer.png");
    assertNotNull(imageDescriptor);
  }

  public void test_getImageDescriptor_notFound() throws Exception {
    try {
      Activator.getImageDescriptor("no-such-image");
      fail();
    } catch (Throwable e) {
      assertInstanceOf(NullPointerException.class, e.getCause());
    }
  }
}
