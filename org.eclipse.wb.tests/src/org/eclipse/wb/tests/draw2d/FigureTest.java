/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
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
package org.eclipse.wb.tests.draw2d;

import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.border.Border;
import org.eclipse.wb.draw2d.border.LineBorder;
import org.eclipse.wb.draw2d.border.MarginBorder;
import org.eclipse.wb.tests.gef.TestLogger;

import org.eclipse.draw2d.AncestorListener;
import org.eclipse.draw2d.Cursors;
import org.eclipse.draw2d.FigureListener;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

/**
 * @author lobas_av
 *
 */
public class FigureTest extends Draw2dFigureTestCase {
	private static final String ERROR_MESSAGE_CYCLE = "Figure being added introduces cycle";
	private static final String ERROR_MESSAGE_EMPTY_PARENT = "Figure is not a child";
	private static final String ERROR_MESSAGE_INVALID_INDEX = "Index does not exist";

	////////////////////////////////////////////////////////////////////////////
	//
	// Parent/Children tests
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
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
		 * === assert add already added figure ===
		 */
		parentFigure.add(childFigure1);
		assertEquals(parentFigure, childFigure1.getParent());
		/*
		 * === assert add wrong child's ===
		 */
		Figure wrongChildFigure = new Figure();
		new Figure().add(wrongChildFigure);
		//
		parentFigure.add(wrongChildFigure);
		assertEquals(parentFigure, wrongChildFigure.getParent());
		// assert add itself
		try {
			parentFigure.add(parentFigure);
			fail();
		} catch (IllegalArgumentException e) {
			assertEquals(ERROR_MESSAGE_CYCLE, e.getMessage());
		}
	}

	@Test
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
		 * === assert add already added figure ===
		 */
		parentFigure.add(childFigure2, 1);
		assertEquals(parentFigure, childFigure2.getParent());
		/*
		 * === assert add wrong child's ===
		 */
		Figure wrongChildFigure = new Figure();
		new Figure().add(wrongChildFigure, 0);
		//
		parentFigure.add(wrongChildFigure, 2);
		assertEquals(parentFigure, wrongChildFigure.getParent());
		// assert add itself
		try {
			parentFigure.add(parentFigure, 2);
			fail();
		} catch (IllegalArgumentException e) {
			assertEquals(ERROR_MESSAGE_CYCLE, e.getMessage());
		}
		/*
		 * === assert wrong index ===
		 */
		try {
			parentFigure.add(new Figure(), -2);
			fail();
		} catch (IndexOutOfBoundsException e) {
			assertEquals(ERROR_MESSAGE_INVALID_INDEX, e.getMessage());
		}
		try {
			parentFigure.add(new Figure(), parentFigure.getChildren().size() + 1);
			fail();
		} catch (IndexOutOfBoundsException e) {
			assertEquals(ERROR_MESSAGE_INVALID_INDEX, e.getMessage());
		}
	}

	@Test
	public void test_add_Figure_Rectangle() throws Exception {
		Figure parentFigure = new Figure();
		parentFigure.setLayoutManager(new XYLayout());
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
		 * === assert add already added figure ===
		 */
		parentFigure.add(childFigure1, null);
		assertEquals(parentFigure, childFigure1.getParent());
		/*
		 * === assert add wrong child's ===
		 */
		Figure wrongChildFigure = new Figure();
		new Figure().add(wrongChildFigure, null);
		//
		parentFigure.add(wrongChildFigure, null);
		assertEquals(parentFigure, wrongChildFigure.getParent());

		// assert add itself
		try {
			parentFigure.add(parentFigure, null);
			fail();
		} catch (IllegalArgumentException e) {
			assertEquals(ERROR_MESSAGE_CYCLE, e.getMessage());
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
		assertEquals(bounds, parentFigure.getLayoutManager().getConstraint(childFigure3));
	}

	@Test
	public void test_add_Figure_Rectangle_int() throws Exception {
		Figure parentFigure = new Figure();
		parentFigure.setLayoutManager(new XYLayout());
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
		 * === assert add already added figure ===
		 */
		parentFigure.add(childFigure2, null, 1);
		assertEquals(parentFigure, childFigure2.getParent());
		/*
		 * === assert add wrong child's ===
		 */
		Figure wrongChildFigure = new Figure();
		new Figure().add(wrongChildFigure, null, 0);
		//
		parentFigure.add(wrongChildFigure, null, 2);
		assertEquals(parentFigure, wrongChildFigure.getParent());
		// assert add itself
		try {
			parentFigure.add(parentFigure, null, 2);
			fail();
		} catch (IllegalArgumentException e) {
			assertEquals(ERROR_MESSAGE_CYCLE, e.getMessage());
		}
		/*
		 * === assert wrong index ===
		 */
		try {
			parentFigure.add(new Figure(), null, -2);
			fail();
		} catch (IndexOutOfBoundsException e) {
			assertEquals(ERROR_MESSAGE_INVALID_INDEX, e.getMessage());
		}
		try {
			parentFigure.add(new Figure(), null, parentFigure.getChildren().size() + 1);
			fail();
		} catch (IndexOutOfBoundsException e) {
			assertEquals(ERROR_MESSAGE_INVALID_INDEX, e.getMessage());
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
		assertEquals(bounds, parentFigure.getLayoutManager().getConstraint(childFigure4));
	}

	@Test
	public void test_remove_Figure() throws Exception {
		Figure parentFigure = new Figure();
		/*
		 * === assert remove from empty parent ===
		 */
		try {
			parentFigure.remove(null);
			fail();
		} catch (IllegalArgumentException e) {
			assertEquals(ERROR_MESSAGE_EMPTY_PARENT, e.getMessage());
		}
		//
		try {
			parentFigure.remove(new Figure());
			fail();
		} catch (IllegalArgumentException e) {
			assertEquals(ERROR_MESSAGE_EMPTY_PARENT, e.getMessage());
		}
		// assert remove itself
		try {
			parentFigure.remove(parentFigure);
			fail();
		} catch (IllegalArgumentException e) {
			assertEquals(ERROR_MESSAGE_EMPTY_PARENT, e.getMessage());
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
			assertEquals(ERROR_MESSAGE_EMPTY_PARENT, e.getMessage());
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
			assertEquals(ERROR_MESSAGE_EMPTY_PARENT, e.getMessage());
		}
		// assert remove itself
		try {
			parentFigure.remove(parentFigure);
			fail();
		} catch (IllegalArgumentException e) {
			assertEquals(ERROR_MESSAGE_EMPTY_PARENT, e.getMessage());
		}
	}

	@Test
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
	@Test
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

	@Test
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

	@Test
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

	@Test
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
		testFigure.setLocation(new Point(90, 40));
		assertEquals(new Rectangle(90, 40, 120, 57), testFigure.getBounds());
	}

	@Test
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

	@Test
	public void test_getInsets() throws Exception {
		Figure testFigure = new Figure();
		assertEquals(new Insets(), testFigure.getInsets());
		//
		testFigure.setBorder(new MarginBorder(new Insets(1, 2, 3, 4)));
		assertEquals(new Insets(1, 2, 3, 4), testFigure.getInsets());
	}

	@Test
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

	@Test
	public void test_intersects() throws Exception {
		Figure testFigure = new Figure();
		testFigure.setBounds(new Rectangle(10, -11, 120, 57));
		//
		assertTrue(testFigure.intersects(new Rectangle(30, -20, 50, 100)));
		assertFalse(testFigure.intersects(new Rectangle(0, 0, 5, 100)));
	}

	@Test
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
	@Test
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
		assertNull(testFigure.getBackgroundColor());
		assertNull(testFigure.getForegroundColor());
		assertNull(testFigure.getFont());
		assertNull(testFigure.getCursor());
		assertFalse(testFigure.isOpaque());
		assertTrue(testFigure.isVisible());
		assertNull(testFigure.getToolTip());
	}

	@Test
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

	@Test
	public void test_background() throws Exception {
		Figure testFigure = new Figure();
		//
		// check color for new Figure
		assertNull(testFigure.getBackgroundColor());
		//
		// check set color
		testFigure.setBackgroundColor(black);
		assertSame(black, testFigure.getBackgroundColor());
		//
		// check set other color
		testFigure.setBackgroundColor(red);
		assertSame(red, testFigure.getBackgroundColor());
		//
		// check set 'null' color
		testFigure.setBackgroundColor(null);
		assertNull(testFigure.getBackgroundColor());
	}

	@Test
	public void test_foreground() throws Exception {
		Figure testFigure = new Figure();
		//
		// check color for new Figure
		assertNull(testFigure.getForegroundColor());
		//
		// check set color
		testFigure.setForegroundColor(black);
		assertSame(black, testFigure.getForegroundColor());
		//
		// check set other color
		testFigure.setForegroundColor(red);
		assertSame(red, testFigure.getForegroundColor());
		//
		// check set 'null' color
		testFigure.setForegroundColor(null);
		assertNull(testFigure.getForegroundColor());
	}

	@Test
	public void test_font() throws Exception {
		Figure testFigure = new Figure();
		//
		// check font for new Figure
		assertNull(testFigure.getFont());
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

	@Test
	public void test_cursor() throws Exception {
		Figure testFigure = new Figure();
		// check cursor for new Figure
		assertNull(testFigure.getCursor());
		//
		// check set cursor
		testFigure.setCursor(Cursors.HELP);
		assertSame(Cursors.HELP, testFigure.getCursor());
		//
		// check set other cursor
		testFigure.setCursor(Cursors.CROSS);
		assertSame(Cursors.CROSS, testFigure.getCursor());
		//
		// check set 'null' cursor
		testFigure.setCursor(null);
		assertNull(testFigure.getCursor());
	}

	@Test
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

	@Test
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

	@Test
	public void test_tooltip() throws Exception {
		Figure testFigure = new Figure();
		//
		// check tooltip for new Figure
		assertNull(testFigure.getToolTip());
		//
		// check set tooltip
		testFigure.setToolTip(new Label("JLabel(\"123\")"));
		assertEquals("JLabel(\"123\")", ((Label) testFigure.getToolTip()).getText());
		//
		// check set other tooltip
		testFigure.setToolTip(new Label("new Button()"));
		assertEquals("new Button()", ((Label) testFigure.getToolTip()).getText());
		//
		// check set 'null' tooltip
		testFigure.setToolTip(null);
		assertNull(testFigure.getToolTip());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Event tests
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_add_remove_MouseListener() throws Exception {
		Figure testFigure = new Figure();
		//
		// check init state of listener for new Figure
		assertFalse(testFigure.getListeners(MouseListener.class).hasNext());
		//
		MouseListener listener1 = new MouseListener.Stub();
		testFigure.addMouseListener(listener1);
		//
		// check add MouseListener
		List<MouseListener> list = Lists.newArrayList(testFigure.getListeners(MouseListener.class));
		assertNotNull(list);
		assertEquals(1, list.size());
		assertSame(listener1, list.get(0));
		//
		MouseListener listener2 = new MouseListener.Stub();
		testFigure.addMouseListener(listener2);
		//
		// again check add IMouseListener
		list = Lists.newArrayList(testFigure.getListeners(MouseListener.class));
		assertNotNull(list);
		assertEquals(2, list.size());
		assertSame(listener1, list.get(0));
		assertSame(listener2, list.get(1));
		//
		// check remove IMouseListener
		testFigure.removeMouseListener(listener1);
		list = Lists.newArrayList(testFigure.getListeners(MouseListener.class));
		assertNotNull(list);
		assertEquals(1, list.size());
		assertSame(listener2, list.get(0));
		//
		// again check remove IMouseListener
		testFigure.removeMouseListener(listener2);
		list = Lists.newArrayList(testFigure.getListeners(MouseListener.class));
		assertNotNull(list);
		assertEquals(0, list.size());
	}

	@Test
	public void test_add_remove_MouseMoveListener() throws Exception {
		Figure testFigure = new Figure();
		//
		// check init state of listener for new Figure
		assertFalse(testFigure.getListeners(MouseMotionListener.class).hasNext());
		//
		MouseMotionListener listener1 = new MouseMotionListener.Stub();
		//
		// check add MouseMotionListener
		testFigure.addMouseMotionListener(listener1);
		List<MouseMotionListener> list = Lists.newArrayList(testFigure.getListeners(MouseMotionListener.class));
		assertNotNull(list);
		assertEquals(1, list.size());
		assertSame(listener1, list.get(0));
		//
		MouseMotionListener listener2 = new MouseMotionListener.Stub();
		//
		// again check add MouseMotionListener
		testFigure.addMouseMotionListener(listener2);
		list = Lists.newArrayList(testFigure.getListeners(MouseMotionListener.class));
		assertNotNull(list);
		assertEquals(2, list.size());
		assertSame(listener1, list.get(0));
		assertSame(listener2, list.get(1));
		//
		// check remove MouseMotionListener
		testFigure.removeMouseMotionListener(listener1);
		list = Lists.newArrayList(testFigure.getListeners(MouseMotionListener.class));
		assertNotNull(list);
		assertEquals(1, list.size());
		assertSame(listener2, list.get(0));
		//
		// again check remove MouseMotionListener
		testFigure.removeMouseMotionListener(listener2);
		list = Lists.newArrayList(testFigure.getListeners(MouseMotionListener.class));
		assertNotNull(list);
		assertEquals(0, list.size());
	}

	@Test
	public void test_add_remove_FigureListener() throws Exception {
		Figure testFigure = new Figure();
		//
		// check init state of listener for new Figure
		assertFalse(testFigure.getListeners(FigureListener.class).hasNext());
		//
		FigureListener listener1 = new FigureListener() {
			@Override
			public void figureMoved(IFigure source) {
			}
		};
		testFigure.addFigureListener(listener1);
		//
		// check add IFigureListener
		List<FigureListener> list = Lists.newArrayList(testFigure.getListeners(FigureListener.class));
		assertNotNull(list);
		assertEquals(1, list.size());
		assertSame(listener1, list.get(0));
		//
		FigureListener listener2 = new FigureListener() {
			@Override
			public void figureMoved(IFigure source) {
			}
		};
		testFigure.addFigureListener(listener2);
		//
		// again check add IFigureListener
		list = Lists.newArrayList(testFigure.getListeners(FigureListener.class));
		assertNotNull(list);
		assertEquals(2, list.size());
		assertSame(listener1, list.get(0));
		assertSame(listener2, list.get(1));
		//
		// check remove IFigureListener
		testFigure.removeFigureListener(listener1);
		list = Lists.newArrayList(testFigure.getListeners(FigureListener.class));
		assertNotNull(list);
		assertEquals(1, list.size());
		assertSame(listener2, list.get(0));
		//
		// again check remove IFigureListener
		testFigure.removeFigureListener(listener2);
		list = Lists.newArrayList(testFigure.getListeners(FigureListener.class));
		assertNotNull(list);
		assertEquals(0, list.size());
	}

	@Test
	public void test_invoke_FigureListener() throws Exception {
		final TestLogger actualLogger = new TestLogger();
		//
		FigureListener listener = new FigureListener() {
			@Override
			public void figureMoved(IFigure source) {
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
		PropertyChangeListener listener1 = new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				actualLogger.log("figureReparent(" + event.getOldValue() + ", " + event.getNewValue() + ")");
			}
		};
		//
		// check not invoke during addFigureListener()
		testFigure.addFigureListener(listener);
		testFigure.addPropertyChangeListener("parent", listener1);
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
		testFigure.setLocation(new Point(10, 20));
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
		expectedLogger.log("figureReparent(null, __parent_)");
		actualLogger.assertEquals(expectedLogger);
		//
		// check independent between bounds and parent bounds
		parent.setBounds(new Rectangle(0, 0, 50, 40));
		actualLogger.assertEmpty();
		//
		// check invoke reparent during remove(Figure)
		parent.remove(testFigure);
		expectedLogger.log("figureReparent(__parent_, null)");
		actualLogger.assertEquals(expectedLogger);
	}

	@Test
	public void test_invoke_AncestorListener() throws Exception {
		final TestLogger actualLogger = new TestLogger();
		//
		AncestorListener listener = new AncestorListener.Stub() {
			@Override
			public void ancestorMoved(IFigure ancestor) {
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