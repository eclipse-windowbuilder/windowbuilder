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
package org.eclipse.wb.tests.designer.swt.support;

import org.eclipse.wb.internal.swt.support.ImageSupport;

/**
 * Test for {@link ImageSupport}.
 * 
 * @author lobas_av
 */
public class ImageSupportTest extends AbstractSupportTest {
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
  public void test_createImage() throws Exception {
    Object image =
        ImageSupport.createImage(getClass().getResourceAsStream(
            "/javax/swing/plaf/basic/icons/JavaCup16.png"));
    assertNotNull(image);
    assertSame(m_lastLoader.loadClass("org.eclipse.swt.graphics.Image"), image.getClass());
    ImageSupport.dispose(image);
  }

  public void test_getImageClass() throws Exception {
    assertSame(
        m_lastLoader.loadClass("org.eclipse.swt.graphics.Image"),
        ImageSupport.getImageClass());
  }
}