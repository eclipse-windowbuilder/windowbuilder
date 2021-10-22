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

import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.Layer;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.tests.gef.TestLogger;

/**
 * @author lobas_av
 *
 */
public class LayerTest extends Draw2dFigureTestCase {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public LayerTest() {
    super(Layer.class);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Test's
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_name() throws Exception {
    // check name for new Layer
    assertNull(new Layer(null).getName());
    //
    // check name for new Layer
    assertEquals("", new Layer("").getName());
    //
    // check name for new Layer
    assertEquals("Feedback", new Layer("Feedback").getName());
  }

  public void test_bounds() throws Exception {
    TestLogger actualLogger = new TestLogger();
    //
    TestCaseRootFigure parentFigure = new TestCaseRootFigure(actualLogger);
    //
    Layer layer = new Layer("test");
    parentFigure.add(layer);
    actualLogger.clear();
    //
    // check work setBounds() and not reset state
    layer.setBounds(new Rectangle(1, 2, 3, 4));
    actualLogger.assertEmpty();
    assertEquals(new Rectangle(1, 2, 3, 4), layer.getBounds());
    //
    // check work setLocation(int, int) and not reset state
    layer.setLocation(5, 5);
    actualLogger.assertEmpty();
    assertEquals(new Rectangle(5, 5, 3, 4), layer.getBounds());
    //
    // check work setLocation(Point) and not reset state
    layer.setLocation(new Point(7, 8));
    actualLogger.assertEmpty();
    assertEquals(new Rectangle(7, 8, 3, 4), layer.getBounds());
    //
    // check work setSize(int, int) and not reset state
    layer.setSize(12, 13);
    actualLogger.assertEmpty();
    assertEquals(new Rectangle(7, 8, 12, 13), layer.getBounds());
    //
    // check work setSize(Dimension) and not reset state
    layer.setSize(new Dimension(2, 1));
    actualLogger.assertEmpty();
    assertEquals(new Rectangle(7, 8, 2, 1), layer.getBounds());
  }

  public void test_opaque() throws Exception {
    TestLogger actualLogger = new TestLogger();
    //
    TestCaseRootFigure parentFigure = new TestCaseRootFigure(actualLogger);
    //
    Layer layer = new Layer("test");
    parentFigure.add(layer);
    actualLogger.clear();
    //
    assertFalse(layer.isOpaque());
    //
    // check not change opaque
    layer.setOpaque(true);
    actualLogger.assertEmpty();
    assertFalse(layer.isOpaque());
    //
    // check not change opaque
    layer.setOpaque(false);
    actualLogger.assertEmpty();
    assertFalse(layer.isOpaque());
  }

  public void test_containsPoint() throws Exception {
    Layer layer = new Layer("test");
    Figure figure = new Figure();
    layer.add(figure, new Rectangle(10, 10, 100, 100));
    layer.setBounds(new Rectangle(0, 0, 200, 200));
    //
    // check contains point (0, 0)
    assertFalse(layer.containsPoint(0, 0));
    //
    // check contains point (50, 50)
    assertTrue(layer.containsPoint(50, 50));
  }
}