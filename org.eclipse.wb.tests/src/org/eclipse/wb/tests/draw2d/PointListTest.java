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

import org.eclipse.wb.draw2d.geometry.PointList;
import org.eclipse.wb.draw2d.geometry.Rectangle;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;

/**
 * @author lobas_av
 *
 */
public class PointListTest extends Draw2dTestCase {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public PointListTest() {
    super(PointList.class);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Test's
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_addPoint_getPoint_int() throws Exception {
    PointList list = new PointList();
    list.addPoint(10, 20);
    list.addPoint(new Point(-90, 0));
    list.addPoint(120, -70);
    //
    // check add null point
    try {
      list.addPoint(null);
      fail();
    } catch (NullPointerException e) {
    }
    //
    // check work getPoint()
    assertEquals(3, list.size());
    assertEquals(10, 20, list.getPoint(0));
    assertEquals(-90, 0, list.getPoint(1));
    assertEquals(120, -70, list.getPoint(2));
    //
    // check work getPoint() with wrong index
    try {
      list.getPoint(-1);
      fail();
    } catch (IndexOutOfBoundsException e) {
    }
    //
    // check work getPoint() with wrong index
    try {
      list.getPoint(3);
      fail();
    } catch (IndexOutOfBoundsException e) {
    }
  }

  public void test_insertPoint() throws Exception {
    PointList list = new PointList();
    //
    // check insert point use wrong index
    try {
      list.insertPoint(new Point(), 1);
      fail();
    } catch (IndexOutOfBoundsException e) {
    }
    //
    // check work insertPoint()
    list.insertPoint(new Point(), 0);
    list.addPoint(10, 20);
    list.addPoint(new Point(-90, 0));
    list.insertPoint(new Point(-1, -1), 1);
    //
    assertEquals(4, list.size());
    assertEquals(0, 0, list.getPoint(0));
    assertEquals(-1, -1, list.getPoint(1));
    assertEquals(10, 20, list.getPoint(2));
    assertEquals(-90, 0, list.getPoint(3));
    //
    // check insert null point
    try {
      list.insertPoint(null, 0);
      fail();
    } catch (NullPointerException e) {
    }
  }

  public void test_removePoint() throws Exception {
    PointList list = new PointList();
    //
    list.addPoint(10, -20);
    list.addPoint(-90, 0);
    list.addPoint(120, 120);
    assertEquals(3, list.size());
    //
    // check remove point use wrong index
    try {
      list.removePoint(4);
      fail();
    } catch (IndexOutOfBoundsException e) {
    }
    //
    // check work removePoint()
    list.removePoint(2);
    assertEquals(2, list.size());
    assertEquals(10, -20, list.getPoint(0));
    assertEquals(-90, 0, list.getPoint(1));
    //
    // check work removePoint()
    list.removePoint(0);
    assertEquals(1, list.size());
    assertEquals(-90, 0, list.getPoint(0));
    //
    // check work removePoint()
    list.removePoint(0);
    assertEquals(0, list.size());
    //
    // check remove point use wrong index
    try {
      list.removePoint(0);
      fail();
    } catch (IndexOutOfBoundsException e) {
    }
  }

  public void test_removeAllPoints() throws Exception {
    PointList list = new PointList();
    //
    // check work removeAllPoints() when not childrens
    list.removeAllPoints();
    assertEquals(0, list.size());
    //
    list.addPoint(10, -20);
    list.addPoint(-90, 0);
    list.addPoint(120, 120);
    assertEquals(3, list.size());
    //
    // check work removeAllPoints()
    list.removeAllPoints();
    assertEquals(0, list.size());
    //
    // check work removeAllPoints() when not childrens
    list.removeAllPoints();
    assertEquals(0, list.size());
  }

  public void test_size_setSize() throws Exception {
    assertEquals(0, new PointList().size());
    assertEquals(0, new PointList(7).size());
    //
    PointList list = new PointList();
    //
    list.addPoint(10, -20);
    list.addPoint(-90, 0);
    list.addPoint(120, 120);
    assertEquals(3, list.size());
    //
    // check work setSize()
    list.setSize(5);
    assertEquals(5, list.size());
    assertEquals(120, 120, list.getPoint(2));
    assertEquals(0, 0, list.getPoint(3));
    assertEquals(0, 0, list.getPoint(4));
    //
    // check work setSize()
    list.setSize(1);
    assertEquals(1, list.size());
    assertEquals(10, -20, list.getPoint(0));
    //
    // check work setSize()
    list.setSize(0);
    assertEquals(0, list.size());
  }

  public void test_toIntArray() throws Exception {
    PointList list = new PointList(5);
    list.addPoint(10, -20);
    list.addPoint(-90, 0);
    list.addPoint(120, 120);
    //
    int[] points = list.toIntArray();
    assertNotNull(points);
    assertEquals(6, points.length);
    //
    for (int i = 0; i < points.length; i += 2) {
      assertEquals(points[i], points[i + 1], list.getPoint(i / 2));
    }
  }

  public void test_getCopy() throws Exception {
    PointList list = new PointList();
    list.addPoint(10, -20);
    list.addPoint(-90, 0);
    list.addPoint(120, 120);
    //
    PointList copy = list.getCopy();
    assertNotNull(copy);
    assertNotSame(list, copy);
    assertEquals(list.size(), copy.size());
    //
    for (int i = 0; i < list.size(); i++) {
      assertEquals(copy.getPoint(i), copy.getPoint(i));
    }
  }

  public void test_getFirstPoint() throws Exception {
    PointList list = new PointList();
    //
    list.addPoint(10, -20);
    assertEquals(10, -20, list.getFirstPoint());
    //
    list.addPoint(-90, 0);
    list.addPoint(120, 120);
    assertEquals(10, -20, list.getFirstPoint());
  }

  public void test_getLastPoint() throws Exception {
    PointList list = new PointList();
    //
    list.addPoint(10, -20);
    assertEquals(10, -20, list.getLastPoint());
    //
    list.addPoint(-90, 0);
    assertEquals(-90, 0, list.getLastPoint());
    //
    list.addPoint(120, 120);
    assertEquals(120, 120, list.getLastPoint());
  }

  public void test_getMidpoint() throws Exception {
    PointList list = new PointList();
    list.addPoint(10, -20);
    list.addPoint(-90, 0);
    list.addPoint(120, 120);
    assertEquals(-90, 0, list.getMidpoint());
    //
    list.removeAllPoints();
    list.addPoint(10, 10);
    list.addPoint(20, 20);
    list.addPoint(40, 40);
    list.addPoint(50, 50);
    assertEquals(30, 30, list.getMidpoint());
  }

  public void test_getPoint_Point_int() throws Exception {
    PointList list = new PointList();
    list.addPoint(10, -20);
    list.addPoint(40, 40);
    //
    // check work getPoint() use wrong index
    try {
      list.getPoint(new Point(), -1);
      fail();
    } catch (IndexOutOfBoundsException e) {
    }
    //
    // check work getPoint() use wrong index
    try {
      list.getPoint(new Point(), 2);
      fail();
    } catch (IndexOutOfBoundsException e) {
    }
    //
    // check work getPoint() for null point argument
    try {
      list.getPoint(null, 0);
      fail();
    } catch (NullPointerException e) {
    }
    //
    // check work getPoint()
    Point point = new Point();
    //
    list.getPoint(point, 1);
    assertEquals(40, 40, point);
    //
    list.getPoint(point, 0);
    assertEquals(10, -20, point);
  }

  public void test_setPoint_Point_int() throws Exception {
    PointList list = new PointList();
    list.addPoint(10, -20);
    list.addPoint(40, 40);
    //
    // check work setPoint() use wrong index
    try {
      list.setPoint(new Point(), -1);
      fail();
    } catch (IndexOutOfBoundsException e) {
    }
    //
    // check work setPoint() use wrong index
    try {
      list.setPoint(new Point(), 2);
      fail();
    } catch (IndexOutOfBoundsException e) {
    }
    //
    // check work setPoint() for null point argument
    try {
      list.setPoint(null, 0);
      fail();
    } catch (NullPointerException e) {
    }
    //
    // check work setPoint()
    Point point = new Point(3, 4);
    list.setPoint(point, 1);
    assertEquals(3, 4, point);
    assertNotSame(point, list.getPoint(1));
    assertEquals(point, list.getPoint(1));
    //
    // check work setPoint()
    point = new Point(-1, 2);
    list.setPoint(point, 0);
    assertEquals(-1, 2, point);
    assertNotSame(point, list.getPoint(0));
    assertEquals(point, list.getPoint(0));
  }

  public void test_getBounds() throws Exception {
    PointList list = new PointList();
    //
    list.addPoint(10, 10);
    list.addPoint(20, 20);
    Rectangle bounds = list.getBounds();
    assertEquals(10, 10, 11, 11, bounds);
    assertSame(bounds, list.getBounds());
    //
    list.addPoint(40, 40);
    Rectangle boundsNew = list.getBounds();
    assertEquals(10, 10, 31, 31, boundsNew);
    assertNotSame(bounds, boundsNew);
    assertSame(boundsNew, list.getBounds());
  }

  public void test_translate() throws Exception {
    PointList list = new PointList();
    for (int i = 0; i < 5; i++) {
      list.addPoint(i, -i);
    }
    /*
     * ====== int, int ======
     */
    list.performTranslate(0, 0);
    assertEquals(0, -4, 5, 5, list.getBounds());
    //
    for (int i = 0; i < list.size(); i++) {
      assertEquals(i, -i, list.getPoint(i));
    }
    /*
     * ====== int, int ======
     */
    list.performTranslate(10, -10);
    assertEquals(10, -14, 5, 5, list.getBounds());
    //
    for (int i = 0; i < list.size(); i++) {
      assertEquals(10 + i, -10 - i, list.getPoint(i));
    }
    /*
     * ====== Point ======
     */
    Point point = new Point(-5, 5);
    list.performTranslate(point);
    assertEquals(-5, 5, point);
    assertEquals(5, -9, 5, 5, list.getBounds());
    //
    for (int i = 0; i < list.size(); i++) {
      assertEquals(5 + i, -5 - i, list.getPoint(i));
    }
    /*
     * ====== Dimension ======
     */
    Dimension dimension = new Dimension(1, 2);
    list.performTranslate(dimension);
    assertEquals(1, 2, dimension);
    assertEquals(6, -7, 5, 5, list.getBounds());
    //
    for (int i = 0; i < list.size(); i++) {
      assertEquals(6 + i, -3 - i, list.getPoint(i));
    }
    /*
     * ====== Insets ======
     */
    Insets insets = new Insets(1, -2, 3, 4);
    list.performTranslate(insets);
    assertEquals(1, -2, 3, 4, insets);
    assertEquals(4, -6, 5, 5, list.getBounds());
    //
    for (int i = 0; i < list.size(); i++) {
      assertEquals(4 + i, -2 - i, list.getPoint(i));
    }
  }
}