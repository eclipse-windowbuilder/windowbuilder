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
import org.eclipse.wb.draw2d.border.LineBorder;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.tests.gef.TestLogger;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;

/**
 * @author lobas_av
 *
 */
public class FigurePaintingTest extends Draw2dFigureTestCase {
  private TestLogger m_actualLogger;
  private TestCaseRootFigure m_root;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public FigurePaintingTest() {
    super(Figure.class);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // SetUp
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    m_actualLogger = new TestLogger();
    m_root = new TestCaseRootFigure(m_actualLogger);
  }

  private Figure addFigure() {
    Figure figure = new Figure();
    m_root.add(figure);
    m_actualLogger.clear();
    return figure;
  }

  private Figure addFigure(int x, int y, int width, int height) {
    Figure figure = new Figure();
    m_root.add(figure, new Rectangle(x, y, width, height));
    m_actualLogger.clear();
    return figure;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Figure notify tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_reset() throws Exception {
    Figure testFigure = addFigure(10, 11, 50, 78);
    TestLogger expectedLogger = new TestLogger();
    //
    // check reset state from figure fully
    testFigure.resetState();
    expectedLogger.log("repaint(true, 10, 11, 50, 78)");
    m_actualLogger.assertEquals(expectedLogger);
    //
    // check reset state from part figure
    testFigure.resetState(new Rectangle(1, 2, 3, 4));
    expectedLogger.log("repaint(true, 1, 2, 3, 4)");
    m_actualLogger.assertEquals(expectedLogger);
    //
    // check no reset state from invisible figure
    testFigure.setVisible(false);
    m_actualLogger.clear();
    //
    testFigure.resetState();
    m_actualLogger.assertEmpty();
    //
    testFigure.resetState(new Rectangle(1, 2, 3, 4));
    m_actualLogger.assertEmpty();
  }

  public void test_repaint() throws Exception {
    Figure testFigure = addFigure(10, 11, 50, 78);
    TestLogger expectedLogger = new TestLogger();
    //
    // check repaint from figure fully
    testFigure.repaint();
    expectedLogger.log("repaint(false, 10, 11, 50, 78)");
    m_actualLogger.assertEquals(expectedLogger);
    //
    // check no repaint from invisible figure
    testFigure.setVisible(false);
    m_actualLogger.clear();
    //
    testFigure.repaint();
    m_actualLogger.assertEmpty();
  }

  public void test_add() throws Exception {
    Figure testFigure = addFigure(10, 11, 50, 78);
    TestLogger expectedLogger = new TestLogger();
    //
    // check reset state during add child figure with empty bounds
    testFigure.add(new Figure());
    expectedLogger.log("repaint(true, 10, 11, 0, 0)");
    m_actualLogger.assertEquals(expectedLogger);
    //
    // check reset state during add(Figure) child figure with not empty bounds
    Figure testChildFigure = new Figure();
    testChildFigure.setBounds(new Rectangle(1, 2, 3, 4));
    testFigure.add(testChildFigure);
    expectedLogger.log("repaint(true, 11, 13, 3, 4)");
    m_actualLogger.assertEquals(expectedLogger);
    //
    // check reset state during add(Figure, Rectangle) child figure with not empty bounds
    testFigure.add(new Figure(), new Rectangle(1, 2, 3, 4));
    expectedLogger.log("repaint(true, 10, 11, 4, 6)");
    expectedLogger.log("repaint(true, 11, 13, 3, 4)");
    m_actualLogger.assertEquals(expectedLogger);
    //
    // check reset state during add(Figure, Rectangle, int) child figure with not empty bounds
    testFigure.add(new Figure(), new Rectangle(1, 2, 3, 4), -1);
    expectedLogger.log("repaint(true, 10, 11, 4, 6)");
    expectedLogger.log("repaint(true, 11, 13, 3, 4)");
    m_actualLogger.assertEquals(expectedLogger);
  }

  public void test_remove() throws Exception {
    Figure testFigure = addFigure(10, 11, 50, 78);
    TestLogger expectedLogger = new TestLogger();
    //
    Figure testChildFigure = new Figure();
    testFigure.add(testChildFigure, new Rectangle(21, 17, 25, 24));
    testFigure.add(new Figure());
    m_actualLogger.clear();
    //
    // check reset state during remove child figure
    testFigure.remove(testChildFigure);
    expectedLogger.log("repaint(true, 31, 28, 25, 24)");
    m_actualLogger.assertEquals(expectedLogger);
    //
    // check reset state during remove all children figures
    testFigure.removeAll();
    expectedLogger.log("repaint(true, 10, 11, 50, 78)");
    m_actualLogger.assertEquals(expectedLogger);
    //
    // check no reset state during remove if not childrens
    testFigure.removeAll();
    m_actualLogger.assertEmpty();
  }

  public void test_bounds() throws Exception {
    Figure testFigure = addFigure();
    TestLogger expectedLogger = new TestLogger();
    //
    // check reset state during setBounds()
    testFigure.setBounds(new Rectangle(1, 2, 3, 4));
    expectedLogger.log("repaint(true, 0, 0, 4, 6)");
    m_actualLogger.assertEquals(expectedLogger);
    //
    // check no reset state during setBounds() if bounds not change
    testFigure.setBounds(new Rectangle(1, 2, 3, 4));
    m_actualLogger.assertEmpty();
    //
    // check no reset state during setSize(int, int) if bounds not change
    testFigure.setSize(3, 4);
    m_actualLogger.assertEmpty();
    //
    // check reset state during setSize(int, int)
    testFigure.setSize(1, 5);
    expectedLogger.log("repaint(true, 1, 2, 3, 5)");
    m_actualLogger.assertEquals(expectedLogger);
    //
    // check reset state during setSize(Dimension)
    testFigure.setSize(new Dimension(11, 12));
    expectedLogger.log("repaint(true, 1, 2, 11, 12)");
    m_actualLogger.assertEquals(expectedLogger);
    //
    // check no reset state during setSize(Dimension) if bounds not change
    testFigure.setSize(new Dimension(11, 12));
    m_actualLogger.assertEmpty();
    //
    // check no reset state during setLocation(int, int) if bounds not change
    testFigure.setLocation(1, 2);
    m_actualLogger.assertEmpty();
    //
    // check reset state during setLocation(int, int)
    testFigure.setLocation(3, 7);
    expectedLogger.log("repaint(true, 1, 2, 13, 17)");
    m_actualLogger.assertEquals(expectedLogger);
    //
    // check reset state during setLocation(Point)
    testFigure.setLocation(new Point());
    expectedLogger.log("repaint(true, 0, 0, 14, 19)");
    m_actualLogger.assertEquals(expectedLogger);
    //
    // check no reset state during setLocation(Point) if bounds not change
    testFigure.setLocation(new Point());
    m_actualLogger.assertEmpty();
  }

  public void test_border() throws Exception {
    Figure testFigure = addFigure();
    TestLogger expectedLogger = new TestLogger();
    //
    // check repaint during setBorder()
    LineBorder border = new LineBorder();
    testFigure.setBorder(border);
    expectedLogger.log("repaint(true, 0, 0, 0, 0)");
    m_actualLogger.assertEquals(expectedLogger);
    //
    // check no repaint during setBorder() if border not change
    testFigure.setBorder(border);
    m_actualLogger.assertEmpty();
    //
    // check repaint during setBorder()
    testFigure.setBorder(new LineBorder(7));
    expectedLogger.log("repaint(true, 0, 0, 0, 0)");
    m_actualLogger.assertEquals(expectedLogger);
    //
    // check repaint during setBorder()
    testFigure.setBorder(null);
    expectedLogger.log("repaint(true, 0, 0, 0, 0)");
    m_actualLogger.assertEquals(expectedLogger);
    //
    // check no repaint during setBorder() if border not change
    testFigure.setBorder(null);
    m_actualLogger.assertEmpty();
  }

  public void test_background() throws Exception {
    Figure testFigure = addFigure();
    TestLogger expectedLogger = new TestLogger();
    //
    // check repaint during setBackground()
    testFigure.setBackground(red);
    expectedLogger.log("repaint(false, 0, 0, 0, 0)");
    m_actualLogger.assertEquals(expectedLogger);
    //
    // check no repaint during setBackground() if color not change
    testFigure.setBackground(red);
    m_actualLogger.assertEmpty();
    //
    // check repaint during setBackground()
    testFigure.setBackground(green);
    expectedLogger.log("repaint(false, 0, 0, 0, 0)");
    m_actualLogger.assertEquals(expectedLogger);
    //
    // check repaint during setBackground()
    testFigure.setBackground(null);
    expectedLogger.log("repaint(false, 0, 0, 0, 0)");
    m_actualLogger.assertEquals(expectedLogger);
    //
    // check no repaint during setBackground() if color not change
    testFigure.setBackground(null);
    m_actualLogger.assertEmpty();
  }

  public void test_foreground() throws Exception {
    Figure testFigure = addFigure();
    TestLogger expectedLogger = new TestLogger();
    //
    // check repaint during setForeground()
    testFigure.setForeground(red);
    expectedLogger.log("repaint(false, 0, 0, 0, 0)");
    m_actualLogger.assertEquals(expectedLogger);
    //
    // check no repaint during setForeground() if color not change
    testFigure.setForeground(red);
    m_actualLogger.assertEmpty();
    //
    // check repaint during setForeground()
    testFigure.setForeground(green);
    expectedLogger.log("repaint(false, 0, 0, 0, 0)");
    m_actualLogger.assertEquals(expectedLogger);
    //
    // check repaint during setForeground()
    testFigure.setForeground(null);
    expectedLogger.log("repaint(false, 0, 0, 0, 0)");
    m_actualLogger.assertEquals(expectedLogger);
    //
    // check no repaint during setForeground() if color not change
    testFigure.setForeground(null);
    m_actualLogger.assertEmpty();
  }

  public void test_font() throws Exception {
    Figure testFigure = addFigure();
    TestLogger expectedLogger = new TestLogger();
    //
    // check reset state during setFont()
    testFigure.setFont(new Font(null, "Courier New", 12, SWT.BOLD));
    expectedLogger.log("repaint(true, 0, 0, 0, 0)");
    m_actualLogger.assertEquals(expectedLogger);
    //
    // check reset state during setFont()
    testFigure.setFont(Display.getCurrent().getSystemFont());
    expectedLogger.log("repaint(true, 0, 0, 0, 0)");
    m_actualLogger.assertEquals(expectedLogger);
    //
    // check no reset state during setFont() if font not change
    testFigure.setFont(Display.getCurrent().getSystemFont());
    m_actualLogger.assertEmpty();
    //
    // check reset state during setFont()
    testFigure.setFont(null);
    expectedLogger.log("repaint(true, 0, 0, 0, 0)");
    m_actualLogger.assertEquals(expectedLogger);
    //
    // check no reset state during setFont() if font not change
    testFigure.setFont(null);
    m_actualLogger.assertEmpty();
  }

  public void test_cursor() throws Exception {
    Figure testFigure = addFigure();
    TestLogger expectedLogger = new TestLogger();
    //
    // check ivoke updateCursor() during setCursor()
    testFigure.setCursor(HELP);
    expectedLogger.log("updateCursor");
    m_actualLogger.assertEquals(expectedLogger);
    //
    // check ivoke updateCursor() during setCursor()
    testFigure.setCursor(CROSS);
    expectedLogger.log("updateCursor");
    m_actualLogger.assertEquals(expectedLogger);
    //
    // check not ivoke updateCursor() during setCursor() if cursor not change
    testFigure.setCursor(CROSS);
    m_actualLogger.assertEmpty();
    //
    // check ivoke updateCursor() during setCursor()
    testFigure.setCursor(null);
    expectedLogger.log("updateCursor");
    m_actualLogger.assertEquals(expectedLogger);
    //
    // check not ivoke updateCursor() during setCursor() if cursor not change
    testFigure.setCursor(null);
    m_actualLogger.assertEmpty();
  }

  public void test_opaque() throws Exception {
    Figure testFigure = addFigure();
    TestLogger expectedLogger = new TestLogger();
    //
    // check repaint during setOpaque()
    testFigure.setOpaque(true);
    expectedLogger.log("repaint(false, 0, 0, 0, 0)");
    m_actualLogger.assertEquals(expectedLogger);
    //
    // check no repaint during setOpaque() if opaque not change
    testFigure.setOpaque(true);
    m_actualLogger.assertEmpty();
    //
    // check repaint during setOpaque()
    testFigure.setOpaque(false);
    expectedLogger.log("repaint(false, 0, 0, 0, 0)");
    m_actualLogger.assertEquals(expectedLogger);
    //
    // check no repaint during setOpaque() if opaque not change
    testFigure.setOpaque(false);
    m_actualLogger.assertEmpty();
  }

  public void test_visible() throws Exception {
    Figure testFigure = addFigure();
    TestLogger expectedLogger = new TestLogger();
    //
    // check reset state during setVisible()
    testFigure.setVisible(false);
    expectedLogger.log("repaint(true, 0, 0, 0, 0)");
    m_actualLogger.assertEquals(expectedLogger);
    //
    // check no reset state during setVisible() if visible not change
    testFigure.setVisible(false);
    m_actualLogger.assertEmpty();
    //
    // check reset state during setVisible()
    testFigure.setVisible(true);
    expectedLogger.log("repaint(true, 0, 0, 0, 0)");
    m_actualLogger.assertEquals(expectedLogger);
    //
    // check no reset state during setVisible() if visible not change
    testFigure.setVisible(true);
    m_actualLogger.assertEmpty();
  }
}