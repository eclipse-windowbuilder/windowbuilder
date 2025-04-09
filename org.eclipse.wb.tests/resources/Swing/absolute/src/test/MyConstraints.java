/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package test;

/**
 * Constraint class for an X-Y layout for custom panels.
 * 
 * @author desa
 * @version 0.0 2001-04-26 desa initial version
 */
public class MyConstraints implements Cloneable, java.io.Serializable
{

  /**
   * 
   */
  private static final long serialVersionUID = 0L;
  int x;
  int y;
  int width; // <= 0 means use the components's preferred size
  int height; // <= 0 means use the components's preferred size


  /**
   * Constructor.
   */
  public MyConstraints()
  {
    this(0, 0, 0, 0);
  }


  /**
   * Constructor.
   */
  public MyConstraints(final int x, final int y, final int width,
      final int height)
  {
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
  }


  public int getX()
  {
    return x;
  }


  public void setX(final int x)
  {
    this.x = x;
  }


  public int getY()
  {
    return y;
  }


  public void setY(final int y)
  {
    this.y = y;
  }


  public int getWidth()
  {
    return width;
  }


  public void setWidth(final int width)
  {
    this.width = width;
  }


  public int getHeight()
  {
    return height;
  }


  public void setHeight(final int height)
  {
    this.height = height;
  }


  /**
   * Returns the hashcode for this MyConstraints.
   */
  public int hashCode()
  {
    return x ^ (y * 37) ^ (width * 43) ^ (height * 47);
  }


  /**
   * Checks whether two MyConstraints are equal.
   */
  public boolean equals(final Object that)
  {
    if (that instanceof MyConstraints)
    {
      final MyConstraints other = (MyConstraints) that;
      return other.x == x && other.y == y && other.width == width
          && other.height == height;
    }
    return false;
  }


  /**
   * clone()
   */
  public Object clone()
  {
    return new MyConstraints(x, y, width, height);
  }


  /**
   * toString()
   */
  public String toString()
  {
    return "MyConstraints[" + x + "," + y + "," + width + "," + height
        + "]";
  }
}