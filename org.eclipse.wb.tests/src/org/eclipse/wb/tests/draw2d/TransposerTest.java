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

import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.draw2d.geometry.Transposer;

/**
 * @author lobas_av
 *
 */
public class TransposerTest extends Draw2dTestCase {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TransposerTest() {
    super(Transposer.class);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // State test
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_state() throws Exception {
    Transposer transposer = new Transposer();
    // check "not enabled" new Transposer
    assertFalse(transposer.isEnabled());
    //
    // check setEnabled()
    transposer.setEnabled(true);
    assertTrue(transposer.isEnabled());
    //
    transposer.setEnabled(false);
    assertFalse(transposer.isEnabled());
    //
    // check enable()
    transposer.enable();
    assertTrue(transposer.isEnabled());
    //
    transposer.enable();
    assertTrue(transposer.isEnabled());
    //
    // check disable()
    transposer.disable();
    assertFalse(transposer.isEnabled());
    //
    transposer.disable();
    assertFalse(transposer.isEnabled());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Transposed test
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_t_Dimension() throws Exception {
    Dimension dimension = new Dimension(100, 200);
    Transposer transposer = new Transposer();
    //
    // check work with Dimension if Transposer is enabled
    transposer.enable();
    Dimension result = transposer.t(dimension);
    assertNotNull(result);
    assertNotSame(result, dimension);
    assertEquals(100, 200, dimension);
    assertEquals(200, 100, result);
    //
    // check work with Dimension if Transposer is disabled
    transposer.disable();
    result = transposer.t(dimension);
    assertSame(result, dimension);
    assertEquals(100, 200, dimension);
  }

  public void test_t_Insets() throws Exception {
    Insets insets = new Insets(1, 2, 3, 4);
    Transposer transposer = new Transposer();
    //
    // check work with Insets if Transposer is enabled
    transposer.enable();
    Insets result = transposer.t(insets);
    assertNotNull(result);
    assertNotSame(result, insets);
    assertEquals(1, 2, 3, 4, insets);
    assertEquals(2, 1, 4, 3, result);
    //
    // check work with Insets if Transposer is disabled
    transposer.disable();
    result = transposer.t(insets);
    assertSame(result, insets);
    assertEquals(1, 2, 3, 4, insets);
  }

  public void test_t_Point() throws Exception {
    Point point = new Point(100, 200);
    Transposer transposer = new Transposer();
    //
    // check work with Point if Transposer is enabled
    transposer.enable();
    Point result = transposer.t(point);
    assertNotNull(result);
    assertNotSame(result, point);
    assertEquals(100, 200, point);
    assertEquals(200, 100, result);
    //
    // check work with Point if Transposer is disabled
    transposer.disable();
    result = transposer.t(point);
    assertSame(result, point);
    assertEquals(100, 200, point);
  }

  public void test_t_Rectangle() throws Exception {
    Rectangle rectangle = new Rectangle(1, 2, 3, 4);
    Transposer transposer = new Transposer();
    //
    // check work with Rectangle if Transposer is enabled
    transposer.enable();
    Rectangle result = transposer.t(rectangle);
    assertNotNull(result);
    assertNotSame(result, rectangle);
    assertEquals(1, 2, 3, 4, rectangle);
    assertEquals(2, 1, 4, 3, result);
    //
    // check work with Rectangle if Transposer is disabled
    transposer.disable();
    result = transposer.t(rectangle);
    assertSame(result, rectangle);
    assertEquals(1, 2, 3, 4, rectangle);
  }
}