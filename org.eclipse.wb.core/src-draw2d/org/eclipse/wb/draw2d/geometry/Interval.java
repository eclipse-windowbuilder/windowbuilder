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
package org.eclipse.wb.draw2d.geometry;

import java.io.Serializable;

/**
 * Represents interval in 1-dimensional space.
 *
 * @author mitin_aa
 * @author lobas_av
 * @author scheglov_ke
 * @coverage gef.draw2d
 */
public final class Interval implements Serializable {
  private static final long serialVersionUID = 0L;
  public static final Interval INFINITE = new Interval(0, Integer.MAX_VALUE);
  public int begin;
  public int length;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public Interval() {
  }

  public Interval(int begin, int length) {
    this.begin = begin;
    this.length = length;
  }

  public Interval(Interval interval) {
    begin = interval.begin;
    length = interval.length;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean equals(Object o) {
    if (o instanceof Interval) {
      Interval interval = (Interval) o;
      return interval.begin == begin && interval.length == length;
    }
    return false;
  }

  @Override
  public int hashCode() {
    return begin ^ length << 10;
  }

  @Override
  public String toString() {
    return "Interval(" + begin + ", " + length + ")";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access/Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return begin of this Interval. Provided for convenience.
   */
  public int begin() {
    return begin;
  }

  /**
   * @return length of this Interval. Provided for convenience.
   */
  public int length() {
    return length;
  }

  /**
   * @return end of this Interval
   */
  public int end() {
    return begin + length;
  }

  /**
   * @return the center of this {@link Interval}.
   */
  public int center() {
    return begin + length / 2;
  }

  /**
   * @return <code>true</code> if this {@link Interval} is empty.
   */
  public boolean isEmpty() {
    return length == 0;
  }

  /**
   * Returns whether the given value is within [begin, begin + length).
   */
  public boolean contains(int value) {
    return begin <= value && value < end();
  }

  /**
   * Returns <code>true</code> if the input {@link Interval} intersects this {@link Interval}.
   */
  public boolean intersects(Interval interval) {
    return interval.begin < end() && begin < interval.end();
  }

  /**
   * @return <code>true</code> if this interval leads the given <code>interval</code>, i.e. for
   *         horizontal interval <code>isLeadingOf()</code> returns <code>true</code> if this
   *         interval begins at the left of the given interval.
   */
  public boolean isLeadingOf(Interval interval) {
    return begin < interval.begin;
  }

  /**
   * @return <code>true</code> if this interval trails the given <code>interval</code>, i.e. for
   *         horizontal interval <code>isTrailingOf()</code> returns <code>true</code> if this
   *         interval ends at the right of the given interval.
   */
  public boolean isTrailingOf(Interval interval) {
    return end() > interval.end();
  }

  /**
   * Calculates the distance to given <code>point</code>.
   *
   * Example:
   *
   * <pre>
	 *  Let x=[10, 100]:
	 *  	distance for point 3 is 7.
	 *  	distance for point 10 is 0.
	 *  	distance for point 50 is 0.
	 *  	distance for point 110 is 0.
	 *  	distance for point 150 is 40.
	 *  </pre>
   *
   * @param point
   *          the point to calculate the distance to.
   * @return the calculated distance.
   */
  public int distance(int point) {
    if (point < begin) {
      return begin - point;
    } else if (point > end()) {
      return point - end();
    }
    return 0;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Operations
  //
  ////////////////////////////////////////////////////////////////////////////
  public Interval getIntersection(Interval interval) {
    int x1 = Math.max(begin, interval.begin);
    int x2 = Math.min(end(), interval.end());
    if (x2 - x1 < 0) {
      // no intersection
      return new Interval();
    }
    return new Interval(x1, x2 - x1);
  }

  /**
   * @return copy of this Interval
   */
  public Interval getCopy() {
    return new Interval(begin, length);
  }

  /**
   * Changes begin, but does not change end position of interval, so also changes length.
   */
  public void setBegin(int newBegin) {
    length += begin - newBegin;
    begin = newBegin;
  }

  /**
   * Increase the interval into leading direction (ex., grow left for horizontal interval).
   *
   * @param delta
   *          the value to increase the interval to.
   */
  public void growLeading(int delta) {
    begin += delta;
    length -= delta;
  }

  /**
   * Increase the interval into trailing direction (ex., grow right for horizontal interval).
   *
   * @param delta
   *          the value to increase the interval to.
   */
  public void growTrailing(int delta) {
    length += delta;
  }

  /**
   * Changes begin, but does not change end position of interval, so also changes length.
   *
   * @deprecated use {@link #setBegin(int)}.
   */
  @Deprecated
  public void setBeginKeepEnd(int newBegin) {
    setBegin(newBegin);
  }

  /**
   * Moves begin with given delta. End point of interval does not move, so we change also length.
   *
   * @deprecated use {@link #growLeading(int)}.
   */
  @Deprecated
  public void moveBeginKeepEnd(int delta) {
    growLeading(delta);
  }

  /**
   * Moves end with given delta. Begin point of interval does not move, so practically we just
   * change length.
   *
   * @deprecated use {@link #growTrailing(int)}.
   */
  @Deprecated
  public void moveEndKeepBegin(int delta) {
    growTrailing(delta);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return copy of given array of Interval's
   */
  public static Interval[] getCopy(Interval[] intervals) {
    Interval[] intervalsCopy = new Interval[intervals.length];
    for (int i = 0; i < intervals.length; i++) {
      intervalsCopy[i] = intervals[i].getCopy();
    }
    return intervalsCopy;
  }

  /**
   * @return index of rightmost interval that contains given value in its right half or
   *         <code>-1</code> if there is not such interval.
   *
   *         We can use this method for example for span support.
   *
   * @param intervals
   *          sorted, disjoint array of intervals
   */
  public static int getRightmostIntervalIndex(Interval[] intervals, int value) {
    int index = -1;
    for (int i = 0; i < intervals.length; i++) {
      Interval interval = intervals[i];
      if (interval.begin + interval.length / 2 < value) {
        index = i;
      }
    }
    return index;
  }
}
