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
package org.eclipse.wb.tests.designer.XML.model;

import org.eclipse.wb.internal.core.xml.model.AbstractComponentInfo;
import org.eclipse.wb.tests.designer.XML.model.description.AbstractCoreTest;

import org.eclipse.swt.graphics.Image;

/**
 * Test for {@link AbstractComponentInfo}.
 *
 * @author scheglov_ke
 */
public class AbstractComponentTest extends AbstractCoreTest {
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
   * When we deleted {@link AbstractComponentInfo}, then it will not participate in next "refresh",
   * so will not able to dispose its {@link Image}. We need some solution to clean up
   * {@link AbstractComponentInfo} during delete.
   */
  public void test_disposeImage_whenDeleteModel() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <Button wbp:name='button'/>",
        "</Shell>");
    refresh();
    AbstractComponentInfo button = getObjectByName("button");
    // remember image
    Image image = button.getImage();
    assertFalse(image.isDisposed());
    // do delete
    button.delete();
    assertTrue(image.isDisposed());
  }
}