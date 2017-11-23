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
package org.eclipse.wb.tests.designer.XWT;

import org.eclipse.wb.internal.core.model.description.ToolkitDescription;
import org.eclipse.wb.internal.core.utils.IOUtils2;
import org.eclipse.wb.internal.xwt.Activator;
import org.eclipse.wb.tests.designer.tests.DesignerTestCase;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;

/**
 * Test for {@link Activator}.
 * 
 * @author scheglov_ke
 */
public class ActivatorTest extends DesignerTestCase {
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
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link Activator#getDefault()}.
   */
  public void test_getDefault() throws Exception {
    assertNotNull(Activator.getDefault());
  }

  /**
   * Test for {@link Activator#getToolkit()}.
   */
  public void test_getToolkit() throws Exception {
    ToolkitDescription toolkit = Activator.getToolkit();
    assertEquals("org.eclipse.wb.rcp", toolkit.getId());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getFile()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link Activator#getFile(String)}.
   */
  public void test_getFile() throws Exception {
    InputStream file = Activator.getFile("plugin.xml");
    assertNotNull(file);
    try {
      String s = IOUtils2.readString(file);
      assertThat(s.length()).isGreaterThan(1024);
    } finally {
      IOUtils.closeQuietly(file);
    }
  }

  /**
   * Test for {@link Activator#getFile(String)}.
   */
  public void test_getFile_bad() throws Exception {
    try {
      Activator.getFile("noSuch.file");
    } catch (Throwable e) {
      String msg = e.getMessage();
      assertThat(msg).contains("noSuch.file").contains("org.eclipse.wb.xwt");
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getImage()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link Activator#getImage(String)}.
   */
  public void test_getImage_good() throws Exception {
    Image image = Activator.getImage("editor_xwt.png");
    assertNotNull(image);
  }

  /**
   * Test for {@link Activator#getImage(String)}.
   */
  public void test_getImage_bad() throws Exception {
    try {
      Activator.getImage("noSuch.png");
    } catch (Throwable e) {
      String msg = e.getMessage();
      assertThat(msg).contains("noSuch.png").contains("org.eclipse.wb.xwt");
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getImageDescriptor()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link Activator#getImageDescriptor(String)}.
   */
  public void test_getImageDescription_good() throws Exception {
    ImageDescriptor imageDescriptor = Activator.getImageDescriptor("editor_xwt.png");
    assertNotNull(imageDescriptor);
  }

  /**
   * Test for {@link Activator#getImageDescriptor(String)}.
   */
  public void test_getImageDescription_bad() throws Exception {
    try {
      Activator.getImageDescriptor("noSuch.png");
    } catch (Throwable e) {
      String msg = e.getMessage();
      assertThat(msg).contains("noSuch.png").contains("org.eclipse.wb.xwt");
    }
  }
}