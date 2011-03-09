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
import org.eclipse.wb.draw2d.geometry.Point;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/**
 * @author lobas_av
 * 
 */
public class DimensionTest extends Draw2dTestCase {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DimensionTest() {
    super(Dimension.class);
  }

  @Override
  protected void setUp() throws Exception {
    // check create display for initialize image
    Display.getDefault();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_constructors() throws Exception {
    assertEquals(0, 0, new Dimension());
    //
    assertEquals(-7, 8, new Dimension(-7, 8));
    //
    assertEquals(-7, 8, new Dimension(new Dimension(-7, 8)));
    //
    assertEquals(-7, 8, new Dimension(new org.eclipse.swt.graphics.Point(-7, 8)));
    //
    assertEquals(
        16,
        16,
        new Dimension(new Image(null, getClass().getResourceAsStream("recorder.gif"))));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object test
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_equals_Object() throws Exception {
    Dimension testDimension = new Dimension(-7, 8);
    assertFalse(testDimension.equals(null));
    assertFalse(testDimension.equals(new Object()));
    assertTrue(testDimension.equals(testDimension));
    assertTrue(testDimension.equals(new Dimension(testDimension)));
    assertFalse(testDimension.equals(new Dimension()));
  }

  public void test_toString() throws Exception {
    assertNotNull(new Dimension().toString());
    assertNotNull(new Dimension(1, 2).toString());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Operation tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_contains() throws Exception {
    Dimension template = new Dimension(-7, 8);
    assertTrue(template.contains(template));
    assertTrue(template.contains(new Dimension(-7, 8)));
    assertTrue(template.contains(new Dimension(-8, 7)));
    assertTrue(template.contains(new Dimension(-8, 8)));
    assertFalse(template.contains(new Dimension()));
    assertFalse(template.contains(new Dimension(10, 10)));
  }

  public void test_containsProper() throws Exception {
    Dimension template = new Dimension(-7, 8);
    assertFalse(template.containsProper(template));
    assertTrue(template.containsProper(new Dimension(-8, -8)));
  }

  public void test_getArea() throws Exception {
    assertEquals(6, new Dimension(2, 3).getArea());
  }

  public void test_equals_int_int() throws Exception {
    assertTrue(new Dimension().equals(0, 0));
    assertTrue(new Dimension(1, -2).equals(1, -2));
    assertFalse(new Dimension(1, -2).equals(7, -7));
  }

  public void test_isEmpty() throws Exception {
    assertTrue(new Dimension().isEmpty());
    assertTrue(new Dimension(1, -2).isEmpty());
    assertFalse(new Dimension(3, 3).isEmpty());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Modify operation tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_setSize() throws Exception {
    Dimension template = new Dimension(-7, 8);
    Dimension testDimension = new Dimension();
    testDimension.setSize(template);
    assertEquals(template, testDimension);
  }

  public void test_expand() throws Exception {
    // check work expand(Dimension)
    Dimension template = new Dimension(-1, 1);
    Dimension testDimension = new Dimension(2, 3);
    assertSame(testDimension, testDimension.expand(template));
    assertEquals(1, 4, testDimension);
    assertEquals(-1, 1, template);
    //
    // check work expand(Point)
    Point point = new Point(5, 1);
    assertSame(testDimension, testDimension.expand(point));
    assertEquals(6, 5, testDimension);
    assertEquals(5, 1, point);
    //
    // check work expand(int, int)
    assertSame(testDimension, testDimension.expand(-3, 5));
    assertEquals(3, 10, testDimension);
  }

  public void test_intersect() throws Exception {
    Dimension template = new Dimension(-7, 8);
    Dimension testDimension = new Dimension(0, 5);
    assertSame(testDimension, testDimension.intersect(template));
    assertEquals(-7, 5, testDimension);
    assertEquals(-7, 8, template);
  }

  public void test_negate() throws Exception {
    Dimension testDimension = new Dimension(1, 2);
    assertSame(testDimension, testDimension.negate());
    assertEquals(-1, -2, testDimension);
    assertSame(testDimension, testDimension.negate());
    assertEquals(1, 2, testDimension);
  }

  public void test_scale() throws Exception {
    // check work scale(double)
    Dimension testDimension = new Dimension(10, 20);
    assertSame(testDimension, testDimension.scale(0.5));
    assertEquals(5, 10, testDimension);
    //
    // check work scale(double, double)
    assertSame(testDimension, testDimension.scale(20, 10));
    assertEquals(100, 100, testDimension);
  }

  public void test_shrink() throws Exception {
    Dimension testDimension = new Dimension(3, 5);
    assertSame(testDimension, testDimension.shrink(1, 2));
    assertEquals(2, 3, testDimension);
    assertSame(testDimension, testDimension.shrink(-1, 0));
    assertEquals(3, 3, testDimension);
  }

  public void test_transpose() throws Exception {
    Dimension testDimension = new Dimension(3, 5);
    assertSame(testDimension, testDimension.transpose());
    assertEquals(5, 3, testDimension);
  }

  public void test_union() throws Exception {
    // check work union(Dimension)
    Dimension template = new Dimension(-7, 8);
    Dimension testDimension = new Dimension(3, 5);
    assertSame(testDimension, testDimension.union(template));
    assertEquals(3, 8, testDimension);
    assertEquals(-7, 8, template);
    //
    // check work union(int, int)
    assertSame(testDimension, testDimension.union(5, 0));
    assertEquals(5, 8, testDimension);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Copy operation tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getCopy() throws Exception {
    Dimension template = new Dimension(-7, 8);
    Dimension testDimension = template.getCopy();
    assertNotNull(testDimension);
    assertNotSame(template, testDimension);
    assertEquals(template, testDimension);
  }

  public void test_getDifference() throws Exception {
    Dimension template1 = new Dimension(17, 18);
    Dimension template2 = new Dimension(11, 10);
    Dimension testDimension = template1.getDifference(template2);
    assertNotNull(testDimension);
    assertNotSame(template1, testDimension);
    assertNotSame(template2, testDimension);
    assertEquals(6, 8, testDimension);
    assertEquals(17, 18, template1);
    assertEquals(11, 10, template2);
  }

  public void test_getExpanded() throws Exception {
    // check work getExpanded(Dimension)
    Dimension template1 = new Dimension(17, 18);
    Dimension template2 = new Dimension(11, 10);
    Dimension testDimension = template1.getExpanded(template2);
    assertNotNull(testDimension);
    assertNotSame(template1, testDimension);
    assertNotSame(template2, testDimension);
    assertEquals(28, 28, testDimension);
    assertEquals(17, 18, template1);
    assertEquals(11, 10, template2);
    //
    // check work getExpanded(int, int)
    testDimension = template1.getExpanded(-3, 3);
    assertNotNull(testDimension);
    assertNotSame(template1, testDimension);
    assertEquals(14, 21, testDimension);
    assertEquals(17, 18, template1);
  }

  public void test_getIntersected() throws Exception {
    Dimension template1 = new Dimension(-7, 8);
    Dimension template2 = new Dimension(0, 5);
    Dimension testDimension = template1.getIntersected(template2);
    assertNotNull(testDimension);
    assertNotSame(template1, testDimension);
    assertNotSame(template2, testDimension);
    assertEquals(-7, 5, testDimension);
    assertEquals(-7, 8, template1);
    assertEquals(0, 5, template2);
  }

  public void test_getNegated() throws Exception {
    Dimension template = new Dimension(-7, 8);
    Dimension testDimension = template.getNegated();
    assertNotNull(testDimension);
    assertNotSame(template, testDimension);
    assertEquals(7, -8, testDimension);
    assertEquals(-7, 8, template);
  }

  public void test_getScaled() throws Exception {
    Dimension template = new Dimension(10, 20);
    Dimension testDimension = template.getScaled(0.5);
    assertNotNull(testDimension);
    assertNotSame(template, testDimension);
    assertEquals(5, 10, testDimension);
    assertEquals(10, 20, template);
  }

  public void test_getTransposed() throws Exception {
    Dimension template = new Dimension(3, 5);
    Dimension testDimension = template.getTransposed();
    assertNotNull(testDimension);
    assertNotSame(template, testDimension);
    assertEquals(5, 3, testDimension);
    assertEquals(3, 5, template);
  }

  public void test_getUnioned() throws Exception {
    // check work getUnioned(Dimension)
    Dimension template1 = new Dimension(-7, 8);
    Dimension template2 = new Dimension(0, 5);
    Dimension testDimension = template1.getUnioned(template2);
    assertNotNull(testDimension);
    assertNotSame(template1, testDimension);
    assertNotSame(template2, testDimension);
    assertEquals(0, 8, testDimension);
    assertEquals(-7, 8, template1);
    assertEquals(0, 5, template2);
    //
    // check work getUnioned(int, int)
    testDimension = template1.getUnioned(0, 5);
    assertNotNull(testDimension);
    assertNotSame(template1, testDimension);
    assertEquals(0, 8, testDimension);
    assertEquals(-7, 8, template1);
  }
}