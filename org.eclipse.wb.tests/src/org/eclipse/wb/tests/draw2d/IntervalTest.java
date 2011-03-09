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

import org.eclipse.wb.draw2d.geometry.Interval;

/**
 * Tests for {@link Interval} class.
 * 
 * @author mitin_aa
 * @author lobas_av
 */
public class IntervalTest extends Draw2dTestCase {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public IntervalTest() {
    super(Interval.class);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_constructor() throws Exception {
    assertEquals(0, 0, new Interval());
  }

  public void test_constructor_int_int() throws Exception {
    assertEquals(-1, 2, new Interval(-1, 2));
  }

  public void test_constructor_Interval() throws Exception {
    Interval template = new Interval(-1, 2);
    assertEquals(-1, 2, new Interval(template));
    assertEquals(-1, 2, template); // assert read only argument interval
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object test
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_equals_Object() throws Exception {
    Interval testInterval = new Interval(-1, 2);
    assertFalse(testInterval.equals(null));
    assertFalse(testInterval.equals(new Object()));
    assertTrue(testInterval.equals(testInterval));
    assertTrue(testInterval.equals(new Interval(testInterval)));
    assertFalse(testInterval.equals(new Interval()));
  }

  public void test_hashCode_toString() throws Exception {
    assertEquals(0, new Interval().hashCode());
    assertEquals(1 ^ 2 << 10, new Interval(1, 2).hashCode());
    //
    assertNotNull(new Interval().toString());
    assertNotNull(new Interval(1, 2).toString());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Copy tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getCopy() throws Exception {
    Interval template = new Interval(-1, 2);
    Interval testInterval = template.getCopy();
    assertNotNull(testInterval);
    assertNotSame(template, testInterval);
    assertEquals(template, testInterval);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Operations tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_begin() throws Exception {
    Interval interval = new Interval(1, 2);
    assertEquals(1, interval.begin());
  }

  public void test_length() throws Exception {
    Interval interval = new Interval(1, 2);
    assertEquals(2, interval.length());
  }

  public void test_end() throws Exception {
    Interval interval = new Interval(1, 2);
    assertEquals(3, interval.end());
    assertEquals(1, 2, interval);
  }

  public void test_isEmpty() throws Exception {
    {
      Interval interval = new Interval();
      assertTrue(interval.isEmpty());
    }
    {
      Interval interval = new Interval(1, 2);
      assertFalse(interval.isEmpty());
    }
  }

  public void test_center() throws Exception {
    {
      Interval interval = new Interval(1, 2);
      assertEquals(2, interval.center());
    }
    {
      Interval interval = new Interval(1, 3);
      assertEquals(2, interval.center());
    }
    {
      Interval interval = new Interval(1, 4);
      assertEquals(3, interval.center());
    }
    {
      Interval interval = new Interval();
      assertEquals(0, interval.center());
    }
  }

  public void test_distance() throws Exception {
    Interval interval = new Interval(10, 100);
    assertEquals(7, interval.distance(3));
    assertEquals(0, interval.distance(10));
    assertEquals(0, interval.distance(50));
    assertEquals(0, interval.distance(110));
    assertEquals(40, interval.distance(150));
  }

  public void test_contains() throws Exception {
    Interval interval = new Interval(1, 2);
    assertFalse(interval.contains(0));
    assertTrue(interval.contains(1));
    assertTrue(interval.contains(2));
    assertFalse(interval.contains(3));
    assertFalse(interval.contains(4));
  }

  public void test_intersects() throws Exception {
    {
      // not intersects
      Interval interval1 = new Interval(10, 20);
      Interval interval2 = new Interval(2, 6);
      assertFalse(interval1.intersects(interval2));
      assertFalse(interval2.intersects(interval1));
    }
    {
      // fully overlaps
      Interval interval1 = new Interval(10, 20);
      Interval interval2 = new Interval(15, 3);
      assertTrue(interval1.intersects(interval2));
      assertTrue(interval2.intersects(interval1));
    }
    {
      // partly intersects
      Interval interval1 = new Interval(10, 20);
      Interval interval2 = new Interval(15, 20);
      assertTrue(interval1.intersects(interval2));
      assertTrue(interval2.intersects(interval1));
    }
  }

  public void test_isLeadingOf() throws Exception {
    Interval interval1 = new Interval(10, 20);
    Interval interval2 = new Interval(15, 20);
    assertTrue(interval1.isLeadingOf(interval2));
    assertFalse(interval2.isLeadingOf(interval1));
  }

  public void test_isTrailingOf() throws Exception {
    Interval interval1 = new Interval(10, 20);
    Interval interval2 = new Interval(15, 20);
    assertFalse(interval1.isTrailingOf(interval2));
    assertTrue(interval2.isTrailingOf(interval1));
  }

  public void test_getIntersection() throws Exception {
    // intervals intersect
    {
      Interval interval1 = new Interval(10, 20);
      Interval interval2 = new Interval(15, 20);
      Interval intersection = interval2.getIntersection(interval1);
      assertTrue(intersection.equals(new Interval(15, 15)));
    }
    {
      Interval interval1 = new Interval(10, 20);
      Interval interval2 = new Interval(15, 20);
      Interval intersection = interval1.getIntersection(interval2);
      assertTrue(intersection.equals(new Interval(15, 15)));
    }
    // no intersection
    {
      Interval interval1 = new Interval(10, 5);
      Interval interval2 = new Interval(20, 10);
      Interval intersection = interval1.getIntersection(interval2);
      assertTrue(intersection.length() == 0);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Modify operations tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_setBeginKeepEnd() throws Exception {
    // move to left
    Interval interval = new Interval(1, 2);
    interval.setBeginKeepEnd(0);
    assertEquals(0, 3, interval);
    // move to right
    interval = new Interval(-1, 2);
    interval.setBeginKeepEnd(0);
    assertEquals(0, 1, interval);
  }

  public void test_moveBeginKeepEnd() throws Exception {
    // to right
    Interval interval = new Interval(1, 2);
    interval.moveBeginKeepEnd(2);
    assertEquals(3, 0, interval);
    // to left
    interval = new Interval(1, 2);
    interval.moveBeginKeepEnd(-1);
    assertEquals(0, 3, interval);
  }

  public void test_moveEndKeepBegin() throws Exception {
    // to right
    {
      Interval interval = new Interval(1, 2);
      interval.moveEndKeepBegin(2);
      assertEquals(1, 4, interval);
    }
    // to left
    {
      Interval interval = new Interval(1, 2);
      interval.moveEndKeepBegin(-1);
      assertEquals(1, 1, interval);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Static Utils tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_static_getCopy_Array() throws Exception {
    Interval[] intervals = {new Interval(1, 2), new Interval(8, 1)};
    Interval[] copy = Interval.getCopy(intervals);
    assertNotNull(copy);
    assertEquals(2, copy.length);
    assertNotNull(intervals[0]);
    assertNotNull(intervals[1]);
    assertNotNull(copy[0]);
    assertNotNull(copy[1]);
    assertNotSame(intervals[0], copy[0]);
    assertEquals(intervals[0], copy[0]);
    assertNotSame(intervals[1], copy[1]);
    assertEquals(intervals[1], copy[1]);
  }

  public void test_getRightmostIntervalIndex() throws Exception {
    Interval[] intervals = {new Interval(1, 5), new Interval(8, 1)};
    assertEquals(-1, Interval.getRightmostIntervalIndex(intervals, 3));
    assertEquals(0, Interval.getRightmostIntervalIndex(intervals, 7));
    assertEquals(1, Interval.getRightmostIntervalIndex(intervals, 9));
  }
}