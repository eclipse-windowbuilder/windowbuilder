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

import com.google.common.collect.Lists;

import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.border.Border;
import org.eclipse.wb.draw2d.border.LineBorder;
import org.eclipse.wb.draw2d.border.MarginBorder;
import org.eclipse.wb.draw2d.events.IAncestorListener;
import org.eclipse.wb.draw2d.events.IFigureListener;
import org.eclipse.wb.draw2d.events.IMouseListener;
import org.eclipse.wb.draw2d.events.IMouseMoveListener;
import org.eclipse.wb.draw2d.events.MouseEvent;
import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.draw2d.FigureVisitor;
import org.eclipse.wb.tests.gef.TestLogger;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;

import java.util.List;

/**
 * @author lobas_av
 *
 */
public class FigureTest extends Draw2dFigureTestCase {
  private static final String ERROR_MESSAGE_CYCLE =
      "IWAG0002E Figure.add(...) Cycle created in figure heirarchy";
  private static final String ERROR_MESSAGE_EXIST_PARENT =
      "Figure.add(...) Figure already added to parent";
  private static final String ERROR_MESSAGE_EMPTY_PARENT = "This parent is empty";
  private static final String ERROR_MESSAGE_WRONG_PARENT =
      "IWAG0003E Figure is not a child of this parent";
  private static final String ERROR_MESSAGE_INVALID_INDEX =
      "IWAG0001E Figure.add(...) invalid index";

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public FigureTest() {
    super(Figure.class);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Parent/Children tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_add_Figure() throws Exception {
    Figure parentFigure = new Figure();
    /*
     * === assert add figure's ===
     */
    Figure childFigure0 = new Figure();
    parentFigure.add(childFigure0);
    assertSame(parentFigure, childFigure0.getParent());
    assertEquals(1, parentFigure.getChildren().size());
    assertTrue(parentFigure.getChildren().contains(childFigure0));
    //
    Figure childFigure1 = new Figure();
    parentFigure.add(childFigure1);
    assertSame(parentFigure, childFigure1.getParent());
    assertEquals(2, parentFigure.getChildren().size());
    assertTrue(parentFigure.getChildren().contains(childFigure1));
    // assert order
    assertSame(childFigure0, parentFigure.getChildren().get(0));
    assertSame(childFigure1, parentFigure.getChildren().get(1));
    /*
     * === assert add alredy added figure ===
     */
    try {
      parentFigure.add(childFigure1);
      fail();
    } catch (IllegalArgumentException e) {
      if (!ERROR_MESSAGE_EXIST_PARENT.equals(e.getMessage())) {
        fail();
      }
    }
    /*
     * === assert add wrong child's ===
     */
    Figure wrongChildFigure = new Figure();
    new Figure().add(wrongChildFigure);
    //
    try {
      parentFigure.add(wrongChildFigure);
      fail();
    } catch (IllegalArgumentException e) {
      if (!ERROR_MESSAGE_EXIST_PARENT.equals(e.getMessage())) {
        fail();
      }
    }
    // assert add itself
    try {
      parentFigure.add(parentFigure);
      fail();
    } catch (IllegalArgumentException e) {
      if (!ERROR_MESSAGE_CYCLE.equals(e.getMessage())) {
        fail();
      }
    }
  }

  public void test_add_Figure_int() throws Exception {
    Figure parentFigure = new Figure();
    /*
     * === assert add figure's ===
     */
    Figure childFigure0 = new Figure();
    parentFigure.add(childFigure0, 0);
    assertSame(parentFigure, childFigure0.getParent());
    assertEquals(1, parentFigure.getChildren().size());
    assertTrue(parentFigure.getChildren().contains(childFigure0));
    //
    Figure childFigure2 = new Figure();
    parentFigure.add(childFigure2, -1);
    assertSame(parentFigure, childFigure2.getParent());
    assertEquals(2, parentFigure.getChildren().size());
    assertTrue(parentFigure.getChildren().contains(childFigure2));
    //
    Figure childFigure1 = new Figure();
    parentFigure.add(childFigure1, 1);
    assertSame(parentFigure, childFigure1.getParent());
    assertTrue(parentFigure.getChildren().contains(childFigure1));
    assertEquals(3, parentFigure.getChildren().size());
    // assert order
    assertSame(childFigure0, parentFigure.getChildren().get(0));
    assertSame(childFigure1, parentFigure.getChildren().get(1));
    assertSame(childFigure2, parentFigure.getChildren().get(2));
    /*
     * === assert add alredy added figure ===
     */
    try {
      parentFigure.add(childFigure2, 1);
      fail();
    } catch (IllegalArgumentException e) {
      if (!ERROR_MESSAGE_EXIST_PARENT.equals(e.getMessage())) {
        fail();
      }
    }
    /*
     * === assert add wrong child's ===
     */
    Figure wrongChildFigure = new Figure();
    new Figure().add(wrongChildFigure, 0);
    //
    try {
      parentFigure.add(wrongChildFigure, 2);
      fail();
    } catch (IllegalArgumentException e) {
      if (!ERROR_MESSAGE_EXIST_PARENT.equals(e.getMessage())) {
        fail();
      }
    }
    // assert add itself
    try {
      parentFigure.add(parentFigure, 2);
      fail();
    } catch (IllegalArgumentException e) {
      if (!ERROR_MESSAGE_CYCLE.equals(e.getMessage())) {
        fail();
      }
    }
    /*
     * === assert wrong index ===
     */
    try {
      parentFigure.add(new Figure(), -2);
      fail();
    } catch (IndexOutOfBoundsException e) {
      if (!ERROR_MESSAGE_INVALID_INDEX.equals(e.getMessage())) {
        fail();
      }
    }
    try {
      parentFigure.add(new Figure(), parentFigure.getChildren().size() + 1);
      fail();
    } catch (IndexOutOfBoundsException e) {
      if (!ERROR_MESSAGE_INVALID_INDEX.equals(e.getMessage())) {
        fail();
      }
    }
  }

  public void test_add_Figure_Rectangle() throws Exception {
    Figure parentFigure = new Figure();
    /*
     * === assert add figure's ===
     */
    Figure childFigure0 = new Figure();
    parentFigure.add(childFigure0, null);
    assertSame(parentFigure, childFigure0.getParent());
    assertEquals(1, parentFigure.getChildren().size());
    assertTrue(parentFigure.getChildren().contains(childFigure0));
    //
    Figure childFigure1 = new Figure();
    parentFigure.add(childFigure1, null);
    assertSame(parentFigure, childFigure1.getParent());
    assertTrue(parentFigure.getChildren().contains(childFigure1));
    assertEquals(2, parentFigure.getChildren().size());
    // assert order
    assertSame(childFigure0, parentFigure.getChildren().get(0));
    assertSame(childFigure1, parentFigure.getChildren().get(1));
    /*
     * === assert add alredy added figure ===
     */
    try {
      parentFigure.add(childFigure1, null);
      fail();
    } catch (IllegalArgumentException e) {
      if (!ERROR_MESSAGE_EXIST_PARENT.equals(e.getMessage())) {
        fail();
      }
    }
    /*
     * === assert add wrong child's ===
     */
    Figure wrongChildFigure = new Figure();
    new Figure().add(wrongChildFigure, null);
    //
    try {
      parentFigure.add(wrongChildFigure, null);
      fail();
    } catch (IllegalArgumentException e) {
      if (!ERROR_MESSAGE_EXIST_PARENT.equals(e.getMessage())) {
        fail();
      }
    }
    // assert add itself
    try {
      parentFigure.add(parentFigure, null);
      fail();
    } catch (IllegalArgumentException e) {
      if (!ERROR_MESSAGE_CYCLE.equals(e.getMessage())) {
        fail();
      }
    }
    /*
     * === assert add with bounds ===
     */
    Rectangle bounds = new Rectangle(10, 11, 120, 200);
    // assert predefined bounds
    Figure childFigure2 = new Figure();
    childFigure2.setBounds(bounds);
    parentFigure.add(childFigure2, null);
    assertEquals(bounds, childFigure2.getBounds());
    // assert set bounds during add(...)
    Figure childFigure3 = new Figure();
    parentFigure.add(childFigure3, bounds);
    assertEquals(bounds, childFigure3.getBounds());
  }

  public void test_add_Figure_Rectangle_int() throws Exception {
    Figure parentFigure = new Figure();
    /*
     * === assert add figure's ===
     */
    Figure childFigure0 = new Figure();
    parentFigure.add(childFigure0, null, 0);
    assertSame(parentFigure, childFigure0.getParent());
    assertEquals(1, parentFigure.getChildren().size());
    assertTrue(parentFigure.getChildren().contains(childFigure0));
    //
    Figure childFigure2 = new Figure();
    parentFigure.add(childFigure2, null, -1);
    assertSame(parentFigure, childFigure2.getParent());
    assertTrue(parentFigure.getChildren().contains(childFigure2));
    assertEquals(2, parentFigure.getChildren().size());
    //
    Figure childFigure1 = new Figure();
    parentFigure.add(childFigure1, null, 1);
    assertSame(parentFigure, childFigure1.getParent());
    assertTrue(parentFigure.getChildren().contains(childFigure1));
    assertEquals(3, parentFigure.getChildren().size());
    // assert order
    assertSame(childFigure0, parentFigure.getChildren().get(0));
    assertSame(childFigure1, parentFigure.getChildren().get(1));
    assertSame(childFigure2, parentFigure.getChildren().get(2));
    /*
     * === assert add alredy added figure ===
     */
    try {
      parentFigure.add(childFigure2, null, 1);
      fail();
    } catch (IllegalArgumentException e) {
      if (!ERROR_MESSAGE_EXIST_PARENT.equals(e.getMessage())) {
        fail();
      }
    }
    /*
     * === assert add wrong child's ===
     */
    Figure wrongChildFigure = new Figure();
    new Figure().add(wrongChildFigure, null, 0);
    //
    try {
      parentFigure.add(wrongChildFigure, null, 2);
      fail();
    } catch (IllegalArgumentException e) {
      if (!ERROR_MESSAGE_EXIST_PARENT.equals(e.getMessage())) {
        fail();
      }
    }
    // assert add itself
    try {
      parentFigure.add(parentFigure, null, 2);
      fail();
    } catch (IllegalArgumentException e) {
      if (!ERROR_MESSAGE_CYCLE.equals(e.getMessage())) {
        fail();
      }
    }
    /*
     * === assert wrong index ===
     */
    try {
      parentFigure.add(new Figure(), null, -2);
      fail();
    } catch (IndexOutOfBoundsException e) {
      if (!ERROR_MESSAGE_INVALID_INDEX.equals(e.getMessage())) {
        fail();
      }
    }
    try {
      parentFigure.add(new Figure(), null, parentFigure.getChildren().size() + 1);
      fail();
    } catch (IndexOutOfBoundsException e) {
      if (!ERROR_MESSAGE_INVALID_INDEX.equals(e.getMessage())) {
        fail();
      }
    }
    /*
     * === assert add with bounds ===
     */
    Rectangle bounds = new Rectangle(10, 11, 120, 200);
    // assert predefined bounds
    Figure childFigure3 = new Figure();
    childFigure3.setBounds(bounds);
    parentFigure.add(childFigure3, null, parentFigure.getChildren().size());
    assertEquals(bounds, childFigure3.getBounds());
    // assert set bounds during add(...)
    Figure childFigure4 = new Figure();
    parentFigure.add(childFigure4, bounds, 0);
    assertEquals(bounds, childFigure4.getBounds());
  }

  public void test_remove_Figure() throws Exception {
    Figure parentFigure = new Figure();
    /*
     * === assert remove from empty parent ===
     */
    try {
      parentFigure.remove(null);
      fail();
    } catch (IllegalArgumentException e) {
      if (!ERROR_MESSAGE_EMPTY_PARENT.equals(e.getMessage())) {
        fail();
      }
    }
    //
    try {
      parentFigure.remove(new Figure());
      fail();
    } catch (IllegalArgumentException e) {
      if (!ERROR_MESSAGE_EMPTY_PARENT.equals(e.getMessage())) {
        fail();
      }
    }
    // assert remove itself
    try {
      parentFigure.remove(parentFigure);
      fail();
    } catch (IllegalArgumentException e) {
      if (!ERROR_MESSAGE_EMPTY_PARENT.equals(e.getMessage())) {
        fail();
      }
    }
    /*
     * === assert remove figure ===
     */
    Figure childFigure = new Figure();
    parentFigure.add(childFigure);
    parentFigure.add(new Figure());
    //
    parentFigure.remove(childFigure);
    assertNull(childFigure.getParent());
    assertFalse(parentFigure.getChildren().contains(childFigure));
    /*
     * === assert remove alredy removed figure ===
     */
    try {
      parentFigure.remove(childFigure);
      fail();
    } catch (IllegalArgumentException e) {
      if (!ERROR_MESSAGE_WRONG_PARENT.equals(e.getMessage())) {
        fail();
      }
    }
    /*
     * === assert remove wrong child ===
     */
    Figure wrongChildFigure = new Figure();
    new Figure().add(wrongChildFigure);
    try {
      parentFigure.remove(wrongChildFigure);
      fail();
    } catch (IllegalArgumentException e) {
      if (!ERROR_MESSAGE_WRONG_PARENT.equals(e.getMessage())) {
        fail();
      }
    }
    // assert remove itself
    try {
      parentFigure.remove(parentFigure);
      fail();
    } catch (IllegalArgumentException e) {
      if (!ERROR_MESSAGE_WRONG_PARENT.equals(e.getMessage())) {
        fail();
      }
    }
  }

  public void test_removeAll() throws Exception {
    Figure parentFigure = new Figure();
    // check always work removeAll()
    parentFigure.removeAll();
    // check full removing
    Figure childFigure1 = new Figure();
    parentFigure.add(childFigure1);
    Figure childFigure2 = new Figure();
    parentFigure.add(childFigure2);
    Figure childFigure3 = new Figure();
    parentFigure.add(childFigure3);
    parentFigure.removeAll();
    assertNull(childFigure1.getParent());
    assertNull(childFigure2.getParent());
    assertNull(childFigure3.getParent());
    assertEquals(0, parentFigure.getChildren().size());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Bounds tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_get_set_Bounds() throws Exception {
    Figure testFigure = new Figure();
    //
    // check new Figure bounds
    assertEquals(new Rectangle(), testFigure.getBounds());
    //
    // check set bounds over Rectangle
    testFigure.setBounds(new Rectangle(10, -11, 120, 57));
    assertEquals(new Rectangle(10, -11, 120, 57), testFigure.getBounds());
    //
    testFigure.setBounds(new Rectangle(0, 5, 60, 30));
    assertEquals(new Rectangle(0, 5, 60, 30), testFigure.getBounds());
    //
    // check set bounds over (int, int, int, int)
    Rectangle bounds = new Rectangle(1, 2, 3, 4);
    testFigure.setBounds(bounds);
    bounds.setSize(5, 6);
    assertEquals(new Rectangle(1, 2, 3, 4), testFigure.getBounds());
  }

  public void test_getLocation() throws Exception {
    Figure testFigure = new Figure();
    testFigure.setBounds(new Rectangle(10, -11, 120, 57));
    //
    // check work getLocation()
    assertEquals(new Point(10, -11), testFigure.getLocation());
    //
    // check "readOnly" work getLocation()
    testFigure.getLocation().scale(120);
    assertEquals(new Rectangle(10, -11, 120, 57), testFigure.getBounds());
  }

  public void test_getSize() throws Exception {
    Figure testFigure = new Figure();
    testFigure.setBounds(new Rectangle(10, -11, 120, 57));
    //
    // check work getSize()
    assertEquals(new Dimension(120, 57), testFigure.getSize());
    //
    // check "readOnly" work getSize()
    testFigure.getSize().scale(-120);
    assertEquals(new Rectangle(10, -11, 120, 57), testFigure.getBounds());
  }

  public void test_setLocation() throws Exception {
    Figure testFigure = new Figure();
    testFigure.setBounds(new Rectangle(10, -11, 120, 57));
    Point location = new Point(20, 40);
    //
    // check work setLocation(Point)
    testFigure.setLocation(location);
    assertEquals(new Rectangle(20, 40, 120, 57), testFigure.getBounds());
    //
    // check independent between setLocation(Point) and parameter
    location.scale(10);
    assertEquals(new Rectangle(20, 40, 120, 57), testFigure.getBounds());
    //
    // check work setLocation(int, int)
    testFigure.setLocation(90, 40);
    assertEquals(new Rectangle(90, 40, 120, 57), testFigure.getBounds());
  }

  public void test_setSize() throws Exception {
    Figure testFigure = new Figure();
    testFigure.setBounds(new Rectangle(10, -11, 120, 57));
    Dimension size = new Dimension(60, 30);
    //
    // check work setSize(Dimension)
    testFigure.setSize(size);
    assertEquals(new Rectangle(10, -11, 60, 30), testFigure.getBounds());
    //
    // check independent between setSize(Dimension) and parameter
    size.scale(-10);
    assertEquals(new Rectangle(10, -11, 60, 30), testFigure.getBounds());
    //
    // check work setSize(int, int)
    testFigure.setSize(60, 120);
    assertEquals(new Rectangle(10, -11, 60, 120), testFigure.getBounds());
  }

  public void test_getInsets() throws Exception {
    Figure testFigure = new Figure();
    assertEquals(new Insets(), testFigure.getInsets());
    //
    testFigure.setBorder(new MarginBorder(new Insets(1, 2, 3, 4)));
    assertEquals(new Insets(1, 2, 3, 4), testFigure.getInsets());
  }

  public void test_getClientArea() throws Exception {
    Figure testFigure = new Figure();
    testFigure.setBounds(new Rectangle(10, -11, 120, 57));
    testFigure.setBorder(new MarginBorder(new Insets(1, 2, 3, 4)));
    //
    Rectangle clientArea1 = testFigure.getClientArea();
//    assertEquals(new Rectangle(0, 0, 114, 53), clientArea1);
    //
    Rectangle clientArea2 = testFigure.getClientArea();
    assertSame(clientArea2, testFigure.getClientArea(clientArea2));
    assertEquals(clientArea1, clientArea2);
    //
    testFigure.getClientArea().setLocation(50, 50);
    assertEquals(new Rectangle(10, -11, 120, 57), testFigure.getBounds());
  }

  public void test_intersects() throws Exception {
    Figure testFigure = new Figure();
    testFigure.setBounds(new Rectangle(10, -11, 120, 57));
    //
    assertTrue(testFigure.intersects(new Rectangle(30, -20, 50, 100)));
    assertFalse(testFigure.intersects(new Rectangle(0, 0, 5, 100)));
  }

  public void test_containsPoint() {
    Figure testFigure = new Figure();
    testFigure.setBounds(new Rectangle(10, 11, 120, 130));
    //
    assertFalse(testFigure.containsPoint(0, 0));
    assertTrue(testFigure.containsPoint(10, 11));
    assertTrue(testFigure.containsPoint(40, 51));
    assertFalse(testFigure.containsPoint(10 + 120, 11 + 130));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Property tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_init_properties() throws Exception {
    Figure testFigure = new Figure();
    // check initial state all properties for new empty figure
    assertNull(testFigure.getParent());
    assertEquals(0, testFigure.getChildren().size());
    assertEquals(new Rectangle(), testFigure.getBounds());
    assertEquals(new Rectangle(), testFigure.getClientArea());
    assertEquals(new Rectangle(), testFigure.getClientArea(new Rectangle()));
    assertEquals(new Insets(), testFigure.getInsets());
    assertNull(testFigure.getBorder());
    assertNull(testFigure.getBackground());
    assertNull(testFigure.getForeground());
    assertEquals(Display.getCurrent().getSystemFont(), testFigure.getFont());
    assertNull(testFigure.getCursor());
    assertFalse(testFigure.isOpaque());
    assertTrue(testFigure.isVisible());
    assertNull(testFigure.getToolTipText());
    assertNull(testFigure.getData());
  }

  public void test_border() throws Exception {
    Figure testFigure = new Figure();
    //
    // check border for new Figure
    assertNull(testFigure.getBorder());
    //
    // check set border
    Border border = new LineBorder();
    testFigure.setBorder(border);
    assertSame(border, testFigure.getBorder());
    //
    // check set other border
    border = new MarginBorder(4);
    testFigure.setBorder(border);
    assertSame(border, testFigure.getBorder());
    //
    // check set 'null' border
    testFigure.setBorder(null);
    assertNull(testFigure.getBorder());
  }

  public void test_background() throws Exception {
    Figure testFigure = new Figure();
    //
    // check color for new Figure
    assertNull(testFigure.getBackground());
    //
    // check set color
    testFigure.setBackground(black);
    assertSame(black, testFigure.getBackground());
    //
    // check set other color
    testFigure.setBackground(red);
    assertSame(red, testFigure.getBackground());
    //
    // check set 'null' color
    testFigure.setBackground(null);
    assertNull(testFigure.getBackground());
  }

  public void test_foreground() throws Exception {
    Figure testFigure = new Figure();
    //
    // check color for new Figure
    assertNull(testFigure.getForeground());
    //
    // check set color
    testFigure.setForeground(black);
    assertSame(black, testFigure.getForeground());
    //
    // check set other color
    testFigure.setForeground(red);
    assertSame(red, testFigure.getForeground());
    //
    // check set 'null' color
    testFigure.setForeground(null);
    assertNull(testFigure.getForeground());
  }

  public void test_font() throws Exception {
    Figure testFigure = new Figure();
    //
    // check font for new Figure
    assertEquals(Display.getCurrent().getSystemFont(), testFigure.getFont());
    //
    // check set font
    Font font = new Font(null, "Courier New", 12, SWT.BOLD);
    testFigure.setFont(font);
    assertSame(font, testFigure.getFont());
    //
    // check set 'null' font
    testFigure.setFont(null);
    assertNull(testFigure.getFont());
  }

  public void test_cursor() throws Exception {
    Figure testFigure = new Figure();
    // check cursor for new Figure
    assertNull(testFigure.getCursor());
    //
    // check set cursor
    testFigure.setCursor(HELP);
    assertSame(HELP, testFigure.getCursor());
    //
    // check set other cursor
    testFigure.setCursor(CROSS);
    assertSame(CROSS, testFigure.getCursor());
    //
    // check set 'null' cursor
    testFigure.setCursor(null);
    assertNull(testFigure.getCursor());
  }

  public void test_opaque() throws Exception {
    Figure testFigure = new Figure();
    //
    // check opaque for new Figure
    assertFalse(testFigure.isOpaque());
    //
    // check set opaque
    testFigure.setOpaque(true);
    assertTrue(testFigure.isOpaque());
    //
    // check unset opaque
    testFigure.setOpaque(false);
    assertFalse(testFigure.isOpaque());
  }

  public void test_visible() throws Exception {
    Figure testFigure = new Figure();
    //
    // check visible for new Figure
    assertTrue(testFigure.isVisible());
    //
    // check set visible
    testFigure.setVisible(false);
    assertFalse(testFigure.isVisible());
    //
    // check unset visible
    testFigure.setVisible(true);
    assertTrue(testFigure.isVisible());
  }

  public void test_tooltip() throws Exception {
    Figure testFigure = new Figure();
    //
    // check tooltip for new Figure
    assertNull(testFigure.getToolTipText());
    //
    // check set tooltip
    testFigure.setToolTipText("JLabel(\"123\")");
    assertEquals("JLabel(\"123\")", testFigure.getToolTipText());
    //
    // check set other tooltip
    testFigure.setToolTipText("new Button()");
    assertEquals("new Button()", testFigure.getToolTipText());
    //
    // check set 'null' tooltip
    testFigure.setToolTipText(null);
    assertNull(testFigure.getToolTipText());
  }

  public void test_data() throws Exception {
    Figure testFigure = new Figure();
    //
    // check user data for new Figure
    assertNull(testFigure.getData());
    //
    // check set user data
    testFigure.setData("zzz");
    assertEquals("zzz", testFigure.getData());
    //
    // check set other user data
    testFigure.setData(3);
    assertEquals(3, testFigure.getData());
    //
    // check set user data itself
    testFigure.setData(testFigure);
    assertSame(testFigure, testFigure.getData());
    //
    // check set 'null' user data
    testFigure.setData(null);
    assertNull(testFigure.getData());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Visiting test
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_visit() throws Exception {
    //
    Figure testFigure1 = new Figure();
    Figure testFigure2 = new Figure();
    Figure testFigure3 = new Figure();
    testFigure1.add(testFigure2);
    testFigure1.add(testFigure3);
    //
    final List<Figure> track = Lists.newArrayList();
    FigureVisitor visitor = new FigureVisitor() {
      @Override
      public boolean visit(Figure figure) {
        track.add(figure);
        return super.visit(figure);
      }

      @Override
      public void endVisit(Figure figure) {
        track.add(figure);
      }
    };
    //
    // check work forward visiting
    testFigure1.accept(visitor, true);
    assertEquals(6, track.size());
    assertSame(testFigure1, track.get(0));
    assertSame(testFigure2, track.get(1));
    assertSame(testFigure2, track.get(2));
    assertSame(testFigure3, track.get(3));
    assertSame(testFigure3, track.get(4));
    assertSame(testFigure1, track.get(5));
    //
    // check work backward visiting
    track.clear();
    testFigure1.accept(visitor, false);
    assertEquals(6, track.size());
    assertSame(testFigure1, track.get(0));
    assertSame(testFigure3, track.get(1));
    assertSame(testFigure3, track.get(2));
    assertSame(testFigure2, track.get(3));
    assertSame(testFigure2, track.get(4));
    assertSame(testFigure1, track.get(5));
    //
    // check work visiting when FigureVisitor.visit() return false
    visitor = new FigureVisitor() {
      @Override
      public boolean visit(Figure figure) {
        track.add(figure);
        return false;
      }

      @Override
      public void endVisit(Figure figure) {
        track.add(figure);
      }
    };
    //
    track.clear();
    testFigure1.accept(visitor, true);
    assertEquals(1, track.size());
    assertSame(testFigure1, track.get(0));
    //
    track.clear();
    testFigure1.accept(visitor, false);
    assertEquals(1, track.size());
    assertSame(testFigure1, track.get(0));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Event tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_add_remove_MouseListener() throws Exception {
    Figure testFigure = new Figure();
    //
    // check init state of listener for new Figure
    assertNull(testFigure.getListeners(IMouseListener.class));
    //
    IMouseListener listener1 = new IMouseListener() {
      @Override
      public void mouseUp(MouseEvent event) {
      }

      @Override
      public void mouseDown(MouseEvent event) {
      }

      @Override
      public void mouseDoubleClick(MouseEvent event) {
      }
    };
    testFigure.addMouseListener(listener1);
    //
    // check add IMouseListener
    List<IMouseListener> list = testFigure.getListeners(IMouseListener.class);
    assertNotNull(list);
    assertEquals(1, list.size());
    assertSame(listener1, list.get(0));
    //
    IMouseListener listener2 = new IMouseListener() {
      @Override
      public void mouseUp(MouseEvent event) {
      }

      @Override
      public void mouseDown(MouseEvent event) {
      }

      @Override
      public void mouseDoubleClick(MouseEvent event) {
      }
    };
    testFigure.addMouseListener(listener2);
    //
    // again check add IMouseListener
    list = testFigure.getListeners(IMouseListener.class);
    assertNotNull(list);
    assertEquals(2, list.size());
    assertSame(listener1, list.get(0));
    assertSame(listener2, list.get(1));
    //
    // check remove IMouseListener
    testFigure.removeMouseListener(listener1);
    list = testFigure.getListeners(IMouseListener.class);
    assertNotNull(list);
    assertEquals(1, list.size());
    assertSame(listener2, list.get(0));
    //
    // again check remove IMouseListener
    testFigure.removeMouseListener(listener2);
    list = testFigure.getListeners(IMouseListener.class);
    assertNotNull(list);
    assertEquals(0, list.size());
  }

  public void test_add_remove_MouseMoveListener() throws Exception {
    Figure testFigure = new Figure();
    //
    // check init state of listener for new Figure
    assertNull(testFigure.getListeners(IMouseMoveListener.class));
    //
    IMouseMoveListener listener1 = new IMouseMoveListener() {
      @Override
      public void mouseMove(MouseEvent event) {
      }
    };
    //
    // check add IMouseMoveListener
    testFigure.addMouseMoveListener(listener1);
    List<IMouseMoveListener> list = testFigure.getListeners(IMouseMoveListener.class);
    assertNotNull(list);
    assertEquals(1, list.size());
    assertSame(listener1, list.get(0));
    //
    IMouseMoveListener listener2 = new IMouseMoveListener() {
      @Override
      public void mouseMove(MouseEvent event) {
      }
    };
    //
    // again check add IMouseMoveListener
    testFigure.addMouseMoveListener(listener2);
    list = testFigure.getListeners(IMouseMoveListener.class);
    assertNotNull(list);
    assertEquals(2, list.size());
    assertSame(listener1, list.get(0));
    assertSame(listener2, list.get(1));
    //
    // check remove IMouseMoveListener
    testFigure.removeMouseMoveListener(listener1);
    list = testFigure.getListeners(IMouseMoveListener.class);
    assertNotNull(list);
    assertEquals(1, list.size());
    assertSame(listener2, list.get(0));
    //
    // again check remove IMouseMoveListener
    testFigure.removeMouseMoveListener(listener2);
    list = testFigure.getListeners(IMouseMoveListener.class);
    assertNotNull(list);
    assertEquals(0, list.size());
  }

  public void test_add_remove_FigureListener() throws Exception {
    Figure testFigure = new Figure();
    //
    // check init state of listener for new Figure
    assertNull(testFigure.getListeners(IFigureListener.class));
    //
    IFigureListener listener1 = new IFigureListener() {
      @Override
      public void figureReparent(Figure source, Figure oldParent, Figure newParent) {
      }

      @Override
      public void figureMoved(Figure source) {
      }
    };
    testFigure.addFigureListener(listener1);
    //
    // check add IFigureListener
    List<IFigureListener> list = testFigure.getListeners(IFigureListener.class);
    assertNotNull(list);
    assertEquals(1, list.size());
    assertSame(listener1, list.get(0));
    //
    IFigureListener listener2 = new IFigureListener() {
      @Override
      public void figureReparent(Figure source, Figure oldParent, Figure newParent) {
      }

      @Override
      public void figureMoved(Figure source) {
      }
    };
    testFigure.addFigureListener(listener2);
    //
    // again check add IFigureListener
    list = testFigure.getListeners(IFigureListener.class);
    assertNotNull(list);
    assertEquals(2, list.size());
    assertSame(listener1, list.get(0));
    assertSame(listener2, list.get(1));
    //
    // check remove IFigureListener
    testFigure.removeFigureListener(listener1);
    list = testFigure.getListeners(IFigureListener.class);
    assertNotNull(list);
    assertEquals(1, list.size());
    assertSame(listener2, list.get(0));
    //
    // again check remove IFigureListener
    testFigure.removeFigureListener(listener2);
    list = testFigure.getListeners(IFigureListener.class);
    assertNotNull(list);
    assertEquals(0, list.size());
  }

  public void test_invoke_FigureListener() throws Exception {
    final TestLogger actualLogger = new TestLogger();
    //
    IFigureListener listener = new IFigureListener() {
      @Override
      public void figureReparent(Figure source, Figure oldParent, Figure newParent) {
        actualLogger.log("figureReparent(" + source + ", " + oldParent + ", " + newParent + ")");
      }

      @Override
      public void figureMoved(Figure source) {
        actualLogger.log("figureMoved(" + source + ")");
      }
    };
    //
    TestLogger expectedLogger = new TestLogger();
    //
    Figure testFigure = new Figure() {
      @Override
      public String toString() {
        return "__testFigure_";
      }
    };
    //
    // check not invoke during addFigureListener()
    testFigure.addFigureListener(listener);
    //
    actualLogger.assertEmpty();
    //
    // check invoke figure move during setSize()
    testFigure.setSize(10, 20);
    //
    expectedLogger.log("figureMoved(__testFigure_)");
    actualLogger.assertEquals(expectedLogger);
    //
    // check invoke figure move during setLocation()
    testFigure.setLocation(10, 20);
    //
    expectedLogger.log("figureMoved(__testFigure_)");
    actualLogger.assertEquals(expectedLogger);
    //
    // check not invoke figure move when bounds not change
    testFigure.setBounds(new Rectangle(10, 20, 10, 20));
    //
    actualLogger.assertEmpty();
    //
    // check invoke figure move during setBounds()
    testFigure.setBounds(new Rectangle(-11, -11, 17, 120));
    //
    expectedLogger.log("figureMoved(__testFigure_)");
    actualLogger.assertEquals(expectedLogger);
    //
    // check invoke reparent during add(Figure)
    Figure parent = new Figure() {
      @Override
      public String toString() {
        return "__parent_";
      }
    };
    parent.add(testFigure);
    //
    expectedLogger.log("figureReparent(__testFigure_, null, __parent_)");
    actualLogger.assertEquals(expectedLogger);
    //
    // check independent between bounds and parent bounds
    parent.setBounds(new Rectangle(0, 0, 50, 40));
    actualLogger.assertEmpty();
    //
    // check invoke reparent during remove(Figure)
    parent.remove(testFigure);
    expectedLogger.log("figureReparent(__testFigure_, __parent_, null)");
    actualLogger.assertEquals(expectedLogger);
  }

  public void test_invoke_AncestorListener() throws Exception {
    final TestLogger actualLogger = new TestLogger();
    //
    IAncestorListener listener = new IAncestorListener() {
      @Override
      public void ancestorMoved(Figure ancestor) {
        actualLogger.log("ancestorMoved(" + ancestor + ")");
      }
    };
    //
    TestLogger expectedLogger = new TestLogger();
    //
    Figure parent = new Figure() {
      @Override
      public String toString() {
        return "__parent_";
      }
    };
    Figure testFigure = new Figure() {
      @Override
      public String toString() {
        return "__testFigure_";
      }
    };
    parent.add(testFigure);
    //
    // check not invoke during addAncestorListener()
    testFigure.addAncestorListener(listener);
    //
    actualLogger.assertEmpty();
    //
    // check invoke during setBounds()
    testFigure.setBounds(new Rectangle(1, 2, 3, 4));
    //
    expectedLogger.log("ancestorMoved(__testFigure_)");
    actualLogger.assertEquals(expectedLogger);
    //
    // check not invoke when bounds not changed
    testFigure.setSize(3, 4);
    actualLogger.assertEmpty();
    //
    // check invoke during setSize()
    parent.setSize(3, 4);
    //
    expectedLogger.log("ancestorMoved(__parent_)");
    actualLogger.assertEquals(expectedLogger);
  }
}