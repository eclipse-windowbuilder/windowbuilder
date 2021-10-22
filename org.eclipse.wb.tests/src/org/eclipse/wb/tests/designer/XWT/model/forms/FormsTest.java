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
package org.eclipse.wb.tests.designer.XWT.model.forms;

import org.eclipse.wb.internal.xwt.model.widgets.CompositeInfo;
import org.eclipse.wb.tests.designer.XWT.model.XwtModelTest;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

/**
 * Test for basic Forms API features.
 *
 * @author scheglov_ke
 */
public class FormsTest extends XwtModelTest {
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
   * In SWT background is "gray".
   */
  public void test_notForms() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <Composite wbp:name='composite'/>",
        "</Shell>");
    refresh();
    CompositeInfo composite = getObjectByName("composite");
    // background
    {
      Color background = composite.getControl().getBackground();
      assertEquals(new RGB(240, 240, 240), background.getRGB());
    }
  }

  /**
   * In Forms background is "white".
   */
  public void test_isForms() throws Exception {
    parse(
        "// filler filler filler filler filler",
        "<!-- Forms API -->",
        "<Shell>",
        "  <Shell.layout>",
        "    <FillLayout/>",
        "  </Shell.layout>",
        "  <Composite wbp:name='composite'/>",
        "</Shell>");
    refresh();
    CompositeInfo composite = getObjectByName("composite");
    // background
    {
      Color background = composite.getControl().getBackground();
      assertEquals(new RGB(255, 255, 255), background.getRGB());
    }
  }
}
