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
package org.eclipse.wb.tests.designer.swing.jsr296;

import org.eclipse.wb.internal.core.utils.IOUtils2;
import org.eclipse.wb.internal.swing.jsr296.Activator;
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
      assertThat(s.length()).isGreaterThan(512);
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
      assertThat(msg).contains("noSuch.file").contains("org.eclipse.wb.swing.jsr296");
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
    Image image = Activator.getImage("wizards/banner.png");
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
      assertThat(msg).contains("noSuch.png").contains("org.eclipse.wb.swing.jsr296");
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
    ImageDescriptor imageDescriptor = Activator.getImageDescriptor("wizards/banner.png");
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
      assertThat(msg).contains("noSuch.png").contains("org.eclipse.wb.swing.jsr296");
    }
  }
}