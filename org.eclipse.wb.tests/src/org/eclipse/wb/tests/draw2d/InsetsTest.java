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

import org.eclipse.wb.draw2d.geometry.Insets;

/**
 * @author lobas_av
 *
 */
public class InsetsTest extends Draw2dTestCase {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public InsetsTest() {
    super(Insets.class);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors test
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_constructors() throws Exception {
    // check create object use constructor()
    assertEquals(0, 0, 0, 0, new Insets());
    //
    // check create object use constructor(int)
    assertEquals(7, 7, 7, 7, new Insets(7));
    //
    // check create object use constructor(int, int, int, int)
    assertEquals(1, 2, 3, 4, new Insets(1, 2, 3, 4));
    //
    // check create object use constructor(Insets)
    assertEquals(1, 2, 3, 4, new Insets(new Insets(1, 2, 3, 4)));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object test
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_equals_Object() throws Exception {
    Insets testInsets = new Insets(1, 2, 3, 4);
    assertFalse(testInsets.equals(null));
    assertFalse(testInsets.equals(new Object()));
    assertTrue(testInsets.equals(testInsets));
    assertTrue(testInsets.equals(new Insets(testInsets)));
    assertFalse(testInsets.equals(new Insets()));
  }

  public void test_toString() throws Exception {
    assertNotNull(new Insets().toString());
    assertNotNull(new Insets(3).toString());
    assertNotNull(new Insets(1, 2, 3, 4).toString());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Operation tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getHeight() throws Exception {
    assertEquals(11, new Insets(1, 1, 10, 5).getHeight());
  }

  public void test_getWidth() throws Exception {
    assertEquals(6, new Insets(1, 1, 10, 5).getWidth());
  }

  public void test_isEmpty() throws Exception {
    assertTrue(new Insets().isEmpty());
    assertFalse(new Insets(7).isEmpty());
    assertFalse(new Insets(1, 0, 0, 0).isEmpty());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Modify operation tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_add_Insets() throws Exception {
    Insets testInsets = new Insets(1, 2, 3, 4);
    assertSame(testInsets, testInsets.add(new Insets(4, 3, 2, 1)));
    assertEquals(5, 5, 5, 5, testInsets);
  }

  public void test_transpose() throws Exception {
    int top = 1;
    int left = 2;
    int bottom = 3;
    int right = 4;
    Insets testInsets = new Insets(top, left, bottom, right);
    assertSame(testInsets, testInsets.transpose());
    assertEquals(left, top, right, bottom, testInsets);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Copy operation tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getAdded() throws Exception {
    Insets template = new Insets(1, 2, 3, 4);
    Insets testInsets = template.getAdded(new Insets(4, 3, 2, 1));
    assertNotSame(template, testInsets);
    assertEquals(5, 5, 5, 5, testInsets);
  }

  public void test_getTransposed() throws Exception {
    int top = 1;
    int left = 2;
    int bottom = 3;
    int right = 4;
    Insets template = new Insets(top, left, bottom, right);
    Insets testInsets = template.getTransposed();
    assertNotSame(template, testInsets);
    assertEquals(left, top, right, bottom, testInsets);
  }

  public void test_getNegated() throws Exception {
    Insets template = new Insets(1, 2, 3, 4);
    Insets testInsets = template.getNegated();
    assertNotSame(template, testInsets);
    assertEquals(1, 2, 3, 4, template);
    assertEquals(-1, -2, -3, -4, testInsets);
  }
}