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

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.awt.Rectangle;
import java.util.Hashtable;

/**
 * Class for an X-Y layout for custom panels.
 * 
 * @author desa
 * @version 0.0 2001-04-26 desa initial version
 */
public class MyLayout implements LayoutManager2, java.io.Serializable
{

  private static final long serialVersionUID = 0L;

  int width; // <= 0 means use the container's preferred size
  int height; // <= 0 means use the container's preferred size


  /**
   * Constructor.
   */
  public MyLayout()
  {
  }


  /**
   * Constructor.
   */
  public MyLayout(final int width, final int height)
  {
    this.width = width;
    this.height = height;
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


  public String toString()
  {
    return "MyLayout" + "[width=" + width + ",height=" + height + "]"; //NORES
  }


  // LayoutManager interface

  public void addLayoutComponent(final String name, final Component component)
  {
    //System.err.println("MyLayout.addLayoutComponent(" + name + "," +
    // component + ")");
  }


  public void removeLayoutComponent(final Component component)
  {
    //System.err.println("MyLayout.removeLayoutComponent(" + component +
    // ")");
    info.remove(component);
  }


  public Dimension preferredLayoutSize(final Container target)
  {
    return getLayoutSize(target, true);
  }


  public Dimension minimumLayoutSize(final Container target)
  {
    return getLayoutSize(target, false);
  }


  public void layoutContainer(final Container target)
  {
    final Insets insets = target.getInsets();

    final int count = target.getComponentCount();

    //System.err.println("MyLayout.layoutContainer(" + target + ")
    // insets=" + insets + " count=" + count);

    for (int i = 0; i < count; i++)
    {
      final Component component = target.getComponent(i);
      if (component.isVisible())
      {
        final Rectangle r = getComponentBounds(component, true);
        component.setBounds(insets.left + r.x, insets.top + r.y, r.width,
            r.height);
      }
    }
  }


  // LayoutManager2 interface

  public void addLayoutComponent(final Component component,
      final Object constraints)
  {
    //System.err.println("MyLayout.addLayoutComponent(" + component + ","
    // + constraints + ")");
    if (constraints instanceof MyConstraints)
    {
      info.put(component, constraints);
    }
  }


  public Dimension maximumLayoutSize(final Container target)
  {
    return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
  }


  public float getLayoutAlignmentX(final Container target)
  {
    return 0.5f;
  }


  public float getLayoutAlignmentY(final Container target)
  {
    return 0.5f;
  }


  public void invalidateLayout(final Container target)
  {
  }

  // internal

  Hashtable info = new Hashtable(); // leave this as non-transient
  static final MyConstraints defaultConstraints = new MyConstraints();


  private Rectangle getComponentBounds(final Component component,
      final boolean doPreferred)
  {
    MyConstraints constraints = (MyConstraints) info.get(component);

    //System.err.println("MyLayout.getComponentBounds(" + component + ","
    // + doPreferred + ") constraints=" + constraints + " width=" + width + "
    // height=" + height);

    if (constraints == null)
    {
      constraints = defaultConstraints;
    }

    final Rectangle r = new Rectangle(constraints.x, constraints.y,
        constraints.width, constraints.height);

    if (r.width <= 0 || r.height <= 0)
    {
      final Dimension d = doPreferred ? component.getPreferredSize()
          : component.getMinimumSize();
      if (r.width <= 0)
      {
        r.width = d.width;
      }
      if (r.height <= 0)
      {
        r.height = d.height;
      }
    }
    return r;
  }


  private Dimension getLayoutSize(final Container target,
      final boolean doPreferred)
  {
    final Dimension dim = new Dimension(0, 0);

    if (width <= 0 || height <= 0)
    {
      final int count = target.getComponentCount();
      for (int i = 0; i < count; i++)
      {
        final Component component = target.getComponent(i);
        if (component.isVisible())
        {
          final Rectangle r = getComponentBounds(component, doPreferred);
          dim.width = Math.max(dim.width, r.x + r.width);
          dim.height = Math.max(dim.height, r.y + r.height);
        }
      }
    }
    if (width > 0)
    {
      dim.width = width;
    }
    if (height > 0)
    {
      dim.height = height;
    }
    final Insets insets = target.getInsets();
    dim.width += insets.left + insets.right;
    dim.height += insets.top + insets.bottom;

    return dim;
  }
}