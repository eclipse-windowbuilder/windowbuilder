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
package org.eclipse.wb.tests.draw2d;

import org.eclipse.wb.draw2d.border.CompoundBorder;
import org.eclipse.wb.draw2d.border.LineBorder;
import org.eclipse.wb.draw2d.border.MarginBorder;
import org.eclipse.wb.draw2d.geometry.Insets;

/**
 * @author lobas_av
 * 
 */
public class CompoundBorderTest extends Draw2dFigureTestCase {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CompoundBorderTest() {
    super(CompoundBorder.class);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Test's
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_constructor() throws Exception {
    CompoundBorder border = new CompoundBorder();
    //
    // check init state new empty border
    assertNull(border.getInnerBorder());
    assertNull(border.getOuterBorder());
    assertEquals(new Insets(), border.getInsets());
  }

  public void test_constructor_Border_Border() throws Exception {
    //
    // check work when out = LineBorder and inner = MarginBorder
    LineBorder lineBorder = new LineBorder(7);
    MarginBorder marginBorder = new MarginBorder(new Insets(1, 2, 3, 4));
    CompoundBorder border = new CompoundBorder(lineBorder, marginBorder);
    //
    assertSame(lineBorder, border.getOuterBorder());
    assertSame(marginBorder, border.getInnerBorder());
    assertEquals(new Insets(8, 9, 10, 11), border.getInsets());
    //
    // check work when out = LineBorder and inner = null
    border = new CompoundBorder(lineBorder, null);
    assertSame(lineBorder, border.getOuterBorder());
    assertNull(border.getInnerBorder());
    assertEquals(new Insets(7), border.getInsets());
    //
    // check work when out = null and inner = MarginBorder
    border = new CompoundBorder(null, marginBorder);
    assertNull(border.getOuterBorder());
    assertSame(marginBorder, border.getInnerBorder());
    assertEquals(new Insets(1, 2, 3, 4), border.getInsets());
    //
    // check work when out = null and inner = null
    border = new CompoundBorder(null, null);
    assertNull(border.getInnerBorder());
    assertNull(border.getOuterBorder());
    assertEquals(new Insets(), border.getInsets());
  }
}