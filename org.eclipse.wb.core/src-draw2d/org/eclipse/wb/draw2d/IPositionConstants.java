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
package org.eclipse.wb.draw2d;

/**
 * @author lobas_av
 * @coverage gef.draw2d
 */
public interface IPositionConstants {
  /**
   * Left.
   */
  public static final int LEFT = 1;
  /**
   * Center (Horizontal).
   */
  public static final int CENTER = 2;
  /**
   * Right.
   */
  public static final int RIGHT = 4;
  /**
   * Bit-wise OR of LEFT, CENTER, and RIGHT.
   */
  public static final int LEFT_CENTER_RIGHT = LEFT | CENTER | RIGHT;
  /**
   * Top.
   */
  public static final int TOP = 8;
  /**
   * Middle (Vertical).
   */
  public static final int MIDDLE = 16;
  /**
   * Bottom.
   */
  public static final int BOTTOM = 32;
  /**
   * Baseline.
   */
  public static final int BASELINE = 64;
  /**
   * Bit-wise OR of TOP, MIDDLE, and BOTTOM.
   */
  public static final int TOP_MIDDLE_BOTTOM = TOP | MIDDLE | BOTTOM;
  /**
   * None.
   */
  int NONE = 0;
  /**
   * North.
   */
  int NORTH = 1 << 0;
  /**
   * South.
   */
  int SOUTH = 1 << 2;
  /**
   * West.
   */
  int WEST = 1 << 3;
  /**
   * East.
   */
  int EAST = 1 << 4;
  /**
   * North-East: a bit-wise OR of {@link #NORTH} and {@link #EAST}
   */
  int NORTH_EAST = NORTH | EAST;
  /**
   * North-West: a bit-wise OR of {@link #NORTH} and {@link #WEST}
   */
  int NORTH_WEST = NORTH | WEST;
  /**
   * South-East: a bit-wise OR of {@link #SOUTH} and {@link #EAST}
   */
  int SOUTH_EAST = SOUTH | EAST;
  /**
   * South-West: a bit-wise OR of {@link #SOUTH} and {@link #WEST}
   */
  int SOUTH_WEST = SOUTH | WEST;
  /**
   * North-South: a bit-wise OR of {@link #NORTH} and {@link #SOUTH}
   */
  int NORTH_SOUTH = NORTH | SOUTH;
  /**
   * East-West: a bit-wise OR of {@link #EAST} and {@link #WEST}
   */
  int EAST_WEST = EAST | WEST;
}