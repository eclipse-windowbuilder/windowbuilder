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
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;

import org.eclipse.draw2d.geometry.Dimension;

/**
 * @author lobas_av
 *
 */
public class RectangleTest extends Draw2dTestCase {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public RectangleTest() {
    super(Rectangle.class);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_constructor() throws Exception {
    assertEquals(0, 0, 0, 0, new Rectangle());
  }

  public void test_constructor_Point_Dimension() throws Exception {
    Point location = new Point(1, 2);
    Dimension size = new Dimension(3, 4);
    assertEquals(1, 2, 3, 4, new Rectangle(location, size));
    assertEquals(1, 2, location);
    assertEquals(3, 4, size);
  }

  public void test_constructor_Rectangle() throws Exception {
    Rectangle template = new Rectangle(1, 2, 3, 4);
    assertEquals(1, 2, 3, 4, new Rectangle(template));
    assertEquals(1, 2, 3, 4, template);
  }

  public void test_constructor_SwtRectangle() throws Exception {
    org.eclipse.swt.graphics.Rectangle template =
        new org.eclipse.swt.graphics.Rectangle(1, 2, 3, 4);
    assertEquals(1, 2, 3, 4, new Rectangle(template));
    assertEquals(1, template.x);
    assertEquals(2, template.y);
    assertEquals(3, template.width);
    assertEquals(4, template.height);
  }

  public void test_constructor_ints() throws Exception {
    assertEquals(1, 2, 3, 4, new Rectangle(1, 2, 3, 4));
  }

  public void test_constructor_Point_Point() throws Exception {
    Point point1 = new Point(10, 5);
    Point point2 = new Point(40, 30);
    Rectangle testRectangle = new Rectangle(point1, point2);
    assertEquals(10, 5, 31, 26, testRectangle);
    assertEquals(10, 5, point1);
    assertEquals(40, 30, point2);
    //
    point1 = new Point(20, 20);
    point2 = new Point(15, 15);
    testRectangle = new Rectangle(point1, point2);
    assertEquals(15, 15, 5, 5, testRectangle);
    assertEquals(20, 20, point1);
    assertEquals(15, 15, point2);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object test
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_equals_Object() throws Exception {
    Rectangle testRectangle = new Rectangle(10, 15, 70, 30);
    assertFalse(testRectangle.equals(null));
    assertFalse(testRectangle.equals(new Object()));
    assertTrue(testRectangle.equals(testRectangle));
    assertTrue(testRectangle.equals(new Rectangle(testRectangle)));
    assertFalse(testRectangle.equals(new Rectangle()));
  }

  public void test_toString() throws Exception {
    assertNotNull(new Rectangle().toString());
    assertNotNull(new Rectangle(1, 2, 3, 4).toString());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Operation tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getLocation() throws Exception {
    Rectangle template = new Rectangle(10, 15, 70, 30);
    assertEquals(10, 15, template.getLocation());
    assertEquals(10, 15, 70, 30, template);
  }

  public void test_getSize() throws Exception {
    Rectangle template = new Rectangle(10, 15, 70, 30);
    assertEquals(70, 30, template.getSize());
    assertEquals(10, 15, 70, 30, template);
  }

  public void test_bottom() throws Exception {
    Rectangle template = new Rectangle(10, 15, 70, 30);
    assertEquals(45, template.bottom());
    assertEquals(10, 15, 70, 30, template);
  }

  public void test_right() throws Exception {
    Rectangle template = new Rectangle(10, 15, 70, 30);
    assertEquals(80, template.right());
    assertEquals(10, 15, 70, 30, template);
  }

  public void test_left() throws Exception {
    Rectangle template = new Rectangle(10, 15, 70, 30);
    assertEquals(10, template.left());
  }

  public void test_top() throws Exception {
    Rectangle template = new Rectangle(10, 15, 70, 30);
    assertEquals(15, template.top());
  }

  public void test_contains_int_int() throws Exception {
    Rectangle template = new Rectangle(10, 15, 70, 30);
    //
    assertTrue(template.contains(10, 15));
    assertEquals(10, 15, 70, 30, template);
    //
    assertFalse(template.contains(-10, 15));
    assertEquals(10, 15, 70, 30, template);
    //
    assertTrue(template.contains(70, 30));
    assertFalse(template.contains(80, 45));
  }

  public void test_contains_Point() throws Exception {
    Rectangle template = new Rectangle(10, 15, 70, 30);
    Point point = new Point(10, 15);
    //
    assertTrue(template.contains(point));
    assertEquals(10, 15, 70, 30, template);
    assertEquals(10, 15, point);
    //
    assertFalse(template.contains(new Point(-10, 15)));
    assertEquals(10, 15, 70, 30, template);
    //
    assertTrue(template.contains(new Point(70, 30)));
    assertFalse(template.contains(new Point(80, 45)));
  }

  public void test_intersects() throws Exception {
    Rectangle rectangle1 = new Rectangle(10, 15, 70, 30);
    //
    assertTrue(rectangle1.intersects(rectangle1));
    assertEquals(10, 15, 70, 30, rectangle1);
    //
    Rectangle rectangle2 = new Rectangle(0, 30, 50, 40);
    assertTrue(rectangle1.intersects(rectangle2));
    assertEquals(0, 30, 50, 40, rectangle2);
    //
    assertFalse(rectangle1.intersects(new Rectangle(10, 15, 0, 0)));
    //
    assertTrue(rectangle1.intersects(new Rectangle(0, 30, 100, 10)));
    //
    assertFalse(rectangle1.intersects(new Rectangle(-100, -100, 10, 10)));
    //
    assertFalse(rectangle1.intersects(new Rectangle(0, 0, 5, 10)));
  }

  public void test_isEmpty() throws Exception {
    assertTrue(new Rectangle().isEmpty());
    assertTrue(new Rectangle(10, 10, -3, 7).isEmpty());
    assertFalse(new Rectangle(-10, -10, 1, 2).isEmpty());
  }

  public void test_touches() throws Exception {
    Rectangle rectangle1 = new Rectangle(10, 15, 70, 30);
    //
    assertTrue(rectangle1.touches(rectangle1));
    assertEquals(10, 15, 70, 30, rectangle1);
    //
    Rectangle rectangle2 = new Rectangle(0, 30, 50, 40);
    assertTrue(rectangle1.touches(rectangle2));
    assertEquals(0, 30, 50, 40, rectangle2);
    //
    assertTrue(rectangle1.touches(new Rectangle(10, 15, 0, 0)));
    //
    assertTrue(rectangle1.touches(new Rectangle(0, 30, 100, 10)));
    //
    assertFalse(rectangle1.touches(new Rectangle(-100, -100, 10, 10)));
    //
    assertFalse(rectangle1.touches(new Rectangle(0, 0, 5, 10)));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Modify operation tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_setBounds_Rectangle() throws Exception {
    Rectangle template = new Rectangle(10, 15, 70, 30);
    Rectangle testRectangle = new Rectangle();
    assertSame(testRectangle, testRectangle.setBounds(template));
    assertEquals(template, testRectangle);
    assertEquals(10, 15, 70, 30, template);
  }

  public void test_setBounds_ints() throws Exception {
    Rectangle testRectangle = new Rectangle(10, 15, 70, 30);
    assertSame(testRectangle, testRectangle.setBounds(-100, 200, 700, 800));
    assertEquals(-100, 200, 700, 800, testRectangle);
  }

  public void test_setLocation() throws Exception {
    // check work setLocation(Point)
    Point location = new Point(10, 30);
    Rectangle testRectangle = new Rectangle(-100, 200, 700, 800);
    assertSame(testRectangle, testRectangle.setLocation(location));
    assertEquals(10, 30, 700, 800, testRectangle);
    assertEquals(10, 30, location);
    //
    // check work setLocation(int, int)
    assertSame(testRectangle, testRectangle.setLocation(-100, 200));
    assertEquals(-100, 200, 700, 800, testRectangle);
  }

  public void test_setSize() throws Exception {
    // check work setSize(Dimension)
    Dimension size = new Dimension(110, 15);
    Rectangle testRectangle = new Rectangle(-100, 200, 700, 800);
    assertSame(testRectangle, testRectangle.setSize(size));
    assertEquals(-100, 200, 110, 15, testRectangle);
    assertEquals(110, 15, size);
    //
    // check work setSize(int, int)
    assertSame(testRectangle, testRectangle.setSize(1024, 768));
    assertEquals(-100, 200, 1024, 768, testRectangle);
  }

  public void test_moveX() throws Exception {
    Rectangle r = new Rectangle(10, 10, 100, 100);
    r.moveX(20);
    assertEquals(30, 10, 80, 100, r);
  }

  public void test_moveY() throws Exception {
    Rectangle r = new Rectangle(10, 10, 100, 100);
    r.moveY(20);
    assertEquals(10, 30, 100, 80, r);
  }

  public void test_crop() throws Exception {
    Rectangle template = new Rectangle(10, 15, 70, 30);
    assertSame(template, template.crop(null));
    assertEquals(10, 15, 70, 30, template);
    //
    Insets insets = new Insets(1, 2, 3, 4);
    assertSame(template, template.crop(insets));
//    assertEquals(12, 16, 64, 26, template);
    assertEquals(1, 2, 3, 4, insets);
  }

  public void test_expand_int_int() throws Exception {
    Rectangle template = new Rectangle(1, 2, 3, 4);
    assertSame(template, template.expand(2, 3));
    assertEquals(-1, -1, 7, 10, template);
  }

  public void test_expand_Insets() throws Exception {
    Rectangle template = new Rectangle(1, 2, 3, 4);
    Insets insets = new Insets(5, 4, 3, 2);
    assertSame(template, template.expand(insets));
    assertEquals(-3, -3, 9, 12, template);
    assertEquals(5, 4, 3, 2, insets);
  }

  public void test_intersect() throws Exception {
    Rectangle rectangle1 = new Rectangle(10, 15, 70, 30);
    //
    assertSame(rectangle1, rectangle1.intersect(rectangle1));
    assertEquals(10, 15, 70, 30, rectangle1);
    //
    Rectangle rectangle2 = new Rectangle(0, 30, 50, 40);
    assertSame(rectangle1, rectangle1.intersect(rectangle2));
    assertEquals(0, 30, 50, 40, rectangle2);
    assertEquals(10, 30, 40, 15, rectangle1);
    //
    rectangle1 = new Rectangle(10, 15, 70, 30);
    assertSame(rectangle1, rectangle1.intersect(new Rectangle(0, 30, 100, 10)));
    assertEquals(10, 30, 70, 10, rectangle1);
    //
    rectangle1 = new Rectangle(10, 15, 70, 30);
    assertSame(rectangle1, rectangle1.intersect(new Rectangle(-100, -100, 10, 10)));
    assertTrue(rectangle1.isEmpty());
    //
    rectangle1 = new Rectangle(10, 15, 70, 30);
    assertSame(rectangle1, rectangle1.intersect(new Rectangle(0, 0, 5, 10)));
    assertTrue(rectangle1.isEmpty());
  }

  public void test_resize() throws Exception {
    // check work resize(Dimension)
    Rectangle template = new Rectangle(1, 2, 3, 4);
    Dimension size = new Dimension(11, -17);
    assertSame(template, template.resize(size));
    assertEquals(1, 2, 14, -13, template);
    assertEquals(11, -17, size);
    //
    // check work resize(int, int)
    assertSame(template, template.resize(-13, 14));
    assertEquals(1, 2, 1, 1, template);
  }

  public void test_scale() throws Exception {
    // check work scale(double)
    Rectangle template = new Rectangle(10, 10, 20, 20);
    assertSame(template, template.scale(0.5));
    assertEquals(5, 5, 10, 10, template);
    //
    // check work scale(double, double)
    template = new Rectangle(10, 10, 20, 20);
    assertSame(template, template.scale(0.5, 0.5));
    assertEquals(5, 5, 10, 10, template);
  }

  public void test_shrink() throws Exception {
    Rectangle template = new Rectangle(1, 2, 30, 40);
    assertSame(template, template.shrink(5, 7));
    assertEquals(6, 9, 20, 26, template);
    //
    template = new Rectangle(10, 20, 3, 4);
    assertSame(template, template.shrink(-5, -7));
    assertEquals(5, 13, 13, 18, template);
  }

  public void test_translate() throws Exception {
    // check work translate(int, int)
    Rectangle template = new Rectangle(1, 2, 3, 4);
    template.translate(15, 17);
    assertEquals(16, 19, 3, 4, template);
    //
    // check work translate(Point)
    template = new Rectangle(1, 2, 3, 4);
    Point point = new Point(-3, -4);
    template.translate(point);
    assertEquals(-2, -2, 3, 4, template);
    assertEquals(-3, -4, point);
    //
    // check work translate(Dimension)
    template = new Rectangle(1, 2, 3, 4);
    Dimension dimension = new Dimension(100, 200);
    template.translate(dimension);
    assertEquals(101, 202, 3, 4, template);
    assertEquals(100, 200, dimension);
    //
    // check work translate(Insets)
    template = new Rectangle(1, 2, 3, 4);
    Insets insets = new Insets(-7);
    template.translate(insets);
    assertEquals(-6, -5, 3, 4, template);
    assertEquals(-7, -7, -7, -7, insets);
  }

  public void test_transpose() throws Exception {
    Rectangle template = new Rectangle(1, 2, 3, 4);
    assertSame(template, template.transpose());
    assertEquals(2, 1, 4, 3, template);
  }

  public void test_union_Dimension() throws Exception {
    Rectangle template = new Rectangle(1, 2, 3, 4);
    Dimension dimension = new Dimension(-1, 7);
    //
    assertSame(template, template.union(dimension));
    assertEquals(1, 2, 3, 7, template);
    assertEquals(-1, 7, dimension);
  }

  public void test_union_int_int() throws Exception {
    Rectangle template = new Rectangle(10, 20, 30, 40);
    //
    assertSame(template, template.union(15, 25));
    assertEquals(10, 20, 30, 40, template);
    //
    assertSame(template, template.union(50, 70));
    assertEquals(10, 20, 41, 51, template);
    //
    assertSame(template, template.union(55, 30));
    assertEquals(10, 20, 46, 51, template);
    //
    assertSame(template, template.union(50, 77));
    assertEquals(10, 20, 46, 58, template);
    //
    assertSame(template, template.union(6, 47));
    assertEquals(6, 20, 50, 58, template);
    //
    assertSame(template, template.union(26, 17));
    assertEquals(6, 17, 50, 61, template);
    //
    assertSame(template, template.union(0, 0));
    assertEquals(0, 0, 56, 78, template);
  }

  public void test_union_Point() throws Exception {
    Rectangle template = new Rectangle(10, 20, 30, 40);
    Point point = new Point(15, 25);
    //
    template.union(point);
    assertEquals(10, 20, 30, 40, template);
    assertEquals(15, 25, point);
    //
    template.union(point.setLocation(50, 70));
    assertEquals(10, 20, 41, 51, template);
    assertEquals(50, 70, point);
    //
    template.union(point.setLocation(55, 30));
    assertEquals(10, 20, 46, 51, template);
    assertEquals(55, 30, point);
    //
    template.union(point.setLocation(50, 77));
    assertEquals(10, 20, 46, 58, template);
    assertEquals(50, 77, point);
    //
    template.union(point.setLocation(6, 47));
    assertEquals(6, 20, 50, 58, template);
    assertEquals(6, 47, point);
    //
    template.union(point.setLocation(26, 17));
    assertEquals(6, 17, 50, 61, template);
    assertEquals(26, 17, point);
    //
    template.union(point.setLocation(0, 0));
    assertEquals(0, 0, 56, 78, template);
    assertEquals(0, 0, point);
  }

  public void test_union_4int() throws Exception {
    Rectangle template = new Rectangle(10, 20, 30, 40);
    //
    assertSame(template, template.union(10, 20, 30, 40));
    assertEquals(10, 20, 30, 40, template);
    //
    assertSame(template, template.union(5, 7, 10, 10));
    assertEquals(5, 7, 35, 53, template);
    //
    assertSame(template, template.union(10, 20, 100, 200));
    assertEquals(5, 7, 105, 213, template);
  }

  public void test_union_Rectangle() throws Exception {
    Rectangle template = new Rectangle(10, 20, 30, 40);
    //
    assertSame(template, template.union(template));
    assertEquals(10, 20, 30, 40, template);
    //
    assertSame(template, template.union((Rectangle) null));
    assertEquals(10, 20, 30, 40, template);
    //
    Rectangle rectangle = new Rectangle(5, 7, 10, 10);
    assertSame(template, template.union(rectangle));
    assertEquals(5, 7, 35, 53, template);
    assertEquals(5, 7, 10, 10, rectangle);
    //
    rectangle = new Rectangle(10, 20, 100, 200);
    assertSame(template, template.union(rectangle));
    assertEquals(5, 7, 105, 213, template);
    assertEquals(10, 20, 100, 200, rectangle);
  }

  public void test_setX() throws Exception {
    Rectangle rectangle = new Rectangle(1, 2, 4, 3);
    rectangle.setX(0);
    assertEquals(0, 2, 5, 3, rectangle);
    rectangle.setX(1);
    assertEquals(1, 2, 4, 3, rectangle);
  }

  public void test_setY() throws Exception {
    Rectangle rectangle = new Rectangle(1, 2, 3, 4);
    rectangle.setY(1);
    assertEquals(1, 1, 3, 5, rectangle);
    rectangle.setY(3);
    assertEquals(1, 3, 3, 3, rectangle);
  }

  public void test_setRight() throws Exception {
    Rectangle rectangle = new Rectangle(1, 2, 3, 4);
    rectangle.setRight(5);
    assertEquals(1, 2, 4, 4, rectangle);
    rectangle.setRight(2);
    assertEquals(1, 2, 1, 4, rectangle);
  }

  public void test_setBottom() throws Exception {
    Rectangle rectangle = new Rectangle(1, 2, 3, 4);
    rectangle.setBottom(5);
    assertEquals(1, 2, 3, 3, rectangle);
    rectangle.setBottom(7);
    assertEquals(1, 2, 3, 5, rectangle);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Copy operation tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getCopy() throws Exception {
    Rectangle template = new Rectangle(1, 2, 3, 4);
    Rectangle testRectangle = template.getCopy();
    assertNotNull(testRectangle);
    assertNotSame(template, testRectangle);
    assertEquals(template, testRectangle);
  }

  public void test_getSwtRectangle() throws Exception {
    org.eclipse.swt.graphics.Rectangle swtRectangle = new Rectangle(1, 2, 3, 4).getSwtRectangle();
    assertNotNull(swtRectangle);
    assertEquals(1, swtRectangle.x);
    assertEquals(2, swtRectangle.y);
    assertEquals(3, swtRectangle.width);
    assertEquals(4, swtRectangle.height);
  }

  public void test_getBottom() throws Exception {
    Rectangle rectangle = new Rectangle(10, 10, 20, 30);
    assertEquals(20, 40, rectangle.getBottom());
    assertEquals(10, 10, 20, 30, rectangle);
  }

  public void test_getBottomLeft() throws Exception {
    Rectangle rectangle = new Rectangle(10, 10, 20, 30);
    assertEquals(10, 40, rectangle.getBottomLeft());
    assertEquals(10, 10, 20, 30, rectangle);
  }

  public void test_getBottomRight() throws Exception {
    Rectangle rectangle = new Rectangle(10, 10, 20, 30);
    assertEquals(30, 40, rectangle.getBottomRight());
    assertEquals(10, 10, 20, 30, rectangle);
  }

  public void test_getCenter() throws Exception {
    Rectangle rectangle = new Rectangle(10, 10, 20, 30);
    assertEquals(20, 25, rectangle.getCenter());
    assertEquals(10, 10, 20, 30, rectangle);
  }

  public void test_getLeft() throws Exception {
    Rectangle rectangle = new Rectangle(10, 10, 20, 30);
    assertEquals(10, 25, rectangle.getLeft());
    assertEquals(10, 10, 20, 30, rectangle);
  }

  public void test_getRight() throws Exception {
    Rectangle rectangle = new Rectangle(10, 10, 20, 30);
    assertEquals(30, 25, rectangle.getRight());
    assertEquals(10, 10, 20, 30, rectangle);
  }

  public void test_getTop() throws Exception {
    Rectangle rectangle = new Rectangle(10, 10, 20, 30);
    assertEquals(20, 10, rectangle.getTop());
    assertEquals(10, 10, 20, 30, rectangle);
  }

  public void test_getTopLeft() throws Exception {
    Rectangle rectangle = new Rectangle(10, 10, 20, 30);
    assertEquals(10, 10, rectangle.getTopLeft());
    assertEquals(10, 10, 20, 30, rectangle);
  }

  public void test_getTopRight() throws Exception {
    Rectangle rectangle = new Rectangle(10, 10, 20, 30);
    assertEquals(30, 10, rectangle.getTopRight());
    assertEquals(10, 10, 20, 30, rectangle);
  }

  public void test_getCropped() throws Exception {
    // check work getCropped() with null Insets
    Rectangle template = new Rectangle(10, 15, 70, 30);
    Rectangle testRectangle = template.getCropped(null);
    assertNotSame(template, testRectangle);
    assertEquals(10, 15, 70, 30, template);
    assertEquals(10, 15, 70, 30, testRectangle);
    //
    // check work getCropped() with Insets
    Insets insets = new Insets(1, 2, 3, 4);
    testRectangle = template.getCropped(insets);
    assertNotSame(template, testRectangle);
//    assertEquals(12, 16, 64, 26, testRectangle);
    assertEquals(10, 15, 70, 30, template);
    assertEquals(1, 2, 3, 4, insets);
  }

  public void test_getExpanded_int_int() throws Exception {
    Rectangle template = new Rectangle(1, 2, 3, 4);
    Rectangle testRectangle = template.getExpanded(2, 3);
    assertNotSame(template, testRectangle);
    assertEquals(1, 2, 3, 4, template);
    assertEquals(-1, -1, 7, 10, testRectangle);
  }

  public void test_getExpanded_Insets() throws Exception {
    Rectangle template = new Rectangle(1, 2, 3, 4);
    Insets insets = new Insets(5, 4, 3, 2);
    Rectangle testRectangle = template.getExpanded(insets);
    assertNotSame(template, testRectangle);
    assertEquals(1, 2, 3, 4, template);
    assertEquals(-3, -3, 9, 12, testRectangle);
    assertEquals(5, 4, 3, 2, insets);
  }

  public void test_getIntersection() throws Exception {
    Rectangle rectangle1 = new Rectangle(10, 15, 70, 30);
    //
    // check work getIntersection(Rectangle) with itself
    Rectangle testRectangle = rectangle1.getIntersection(rectangle1);
    assertNotSame(rectangle1, testRectangle);
    assertEquals(10, 15, 70, 30, testRectangle);
    assertEquals(10, 15, 70, 30, rectangle1);
    //
    // check work getIntersection(Rectangle)
    Rectangle rectangle2 = new Rectangle(0, 30, 50, 40);
    testRectangle = rectangle1.getIntersection(rectangle2);
    assertNotSame(rectangle1, testRectangle);
    assertNotSame(rectangle2, testRectangle);
    assertEquals(10, 30, 40, 15, testRectangle);
    assertEquals(10, 15, 70, 30, rectangle1);
    assertEquals(0, 30, 50, 40, rectangle2);
    //
    // check work getIntersection(Rectangle)
    rectangle1 = new Rectangle(10, 15, 70, 30);
    testRectangle = rectangle1.getIntersection(new Rectangle(0, 30, 100, 10));
    assertEquals(10, 30, 70, 10, testRectangle);
    assertEquals(10, 15, 70, 30, rectangle1);
    //
    // check work getIntersection(Rectangle)
    rectangle1 = new Rectangle(10, 15, 70, 30);
    testRectangle = rectangle1.getIntersection(new Rectangle(-100, -100, 10, 10));
    assertTrue(testRectangle.isEmpty());
    assertEquals(10, 15, 70, 30, rectangle1);
    //
    // check work getIntersection(Rectangle)
    rectangle1 = new Rectangle(10, 15, 70, 30);
    testRectangle = rectangle1.getIntersection(new Rectangle(0, 0, 5, 10));
    assertTrue(testRectangle.isEmpty());
    assertEquals(10, 15, 70, 30, rectangle1);
  }

  public void test_getResized() throws Exception {
    Rectangle template = new Rectangle(1, 2, 3, 4);
    //
    // check work getResized(int, int)
    Rectangle testRectangle = template.getResized(7, 8);
    assertNotNull(testRectangle);
    assertNotSame(template, testRectangle);
    assertEquals(1, 2, 10, 12, testRectangle);
    assertEquals(1, 2, 3, 4, template);
    //
    // check work getResized(Dimension)
    Dimension size = new Dimension(11, 12);
    testRectangle = template.getResized(size);
    assertNotNull(testRectangle);
    assertNotSame(template, testRectangle);
    assertEquals(1, 2, 14, 16, testRectangle);
    assertEquals(1, 2, 3, 4, template);
    assertEquals(11, 12, size);
  }

  public void test_getTranslated() throws Exception {
    Rectangle template = new Rectangle(1, 2, 3, 4);
    //
    // check work getTranslated(int, int)
    Rectangle testRectangle = template.getTranslated(7, 8);
    assertNotNull(testRectangle);
    assertNotSame(template, testRectangle);
    assertEquals(8, 10, 3, 4, testRectangle);
    assertEquals(1, 2, 3, 4, template);
    //
    // check work getTranslated(Point)
    Point point = new Point(3, 2);
    testRectangle = template.getTranslated(point);
    assertNotNull(testRectangle);
    assertNotSame(template, testRectangle);
    assertEquals(4, 4, 3, 4, testRectangle);
    assertEquals(1, 2, 3, 4, template);
    assertEquals(3, 2, point);
  }

  public void test_getTransposed() throws Exception {
    Rectangle template = new Rectangle(1, 2, 3, 4);
    Rectangle testRectangle = template.getTransposed();
    assertNotNull(testRectangle);
    assertNotSame(template, testRectangle);
    assertEquals(2, 1, 4, 3, testRectangle);
    assertEquals(1, 2, 3, 4, template);
  }

  public void test_getUnion() throws Exception {
    Rectangle template = new Rectangle(10, 20, 30, 40);
    //
    // check work getUnion() with itself
    Rectangle testRectangle = template.getUnion(template);
    assertNotNull(testRectangle);
    assertNotSame(template, testRectangle);
    assertEquals(10, 20, 30, 40, testRectangle);
    assertEquals(10, 20, 30, 40, template);
    //
    // check work getUnion() with null
    testRectangle = template.getUnion(null);
    assertNotNull(testRectangle);
    assertNotSame(template, testRectangle);
    assertEquals(10, 20, 30, 40, testRectangle);
    assertEquals(10, 20, 30, 40, template);
    //
    // check work getUnion() with empty rectangle
    testRectangle = template.getUnion(new Rectangle());
    assertNotNull(testRectangle);
    assertNotSame(template, testRectangle);
    assertEquals(10, 20, 30, 40, testRectangle);
    assertEquals(10, 20, 30, 40, template);
    //
    // check work getUnion() with rectangle
    Rectangle rectangle = new Rectangle(5, 7, 10, 10);
    testRectangle = template.getUnion(rectangle);
    assertNotNull(testRectangle);
    assertNotSame(template, testRectangle);
    assertNotSame(rectangle, testRectangle);
    assertEquals(5, 7, 35, 53, testRectangle);
    assertEquals(10, 20, 30, 40, template);
    assertEquals(5, 7, 10, 10, rectangle);
    //
    // check work getUnion() with rectangle
    template = new Rectangle(5, 7, 35, 53);
    rectangle = new Rectangle(10, 20, 100, 200);
    testRectangle = template.getUnion(rectangle);
    assertNotNull(testRectangle);
    assertNotSame(template, testRectangle);
    assertNotSame(rectangle, testRectangle);
    assertEquals(5, 7, 105, 213, testRectangle);
    assertEquals(5, 7, 35, 53, template);
    assertEquals(10, 20, 100, 200, rectangle);
  }
}