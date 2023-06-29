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

import org.eclipse.wb.draw2d.Polyline;
import org.eclipse.wb.tests.gef.TestLogger;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;

/**
 * @author lobas_av
 *
 */
public class PolylineTest extends Draw2dFigureTestCase {
	private TestLogger m_actualLogger;
	private TestCaseRootFigure m_root;
	private TestLogger m_expectedLogger;
	private Polyline m_polyline;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public PolylineTest() {
		super(Polyline.class);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// SetUp
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		// configure
		m_actualLogger = new TestLogger();
		m_root = new TestCaseRootFigure(m_actualLogger);
		m_expectedLogger = new TestLogger();
		m_polyline = new Polyline();
		m_root.add(m_polyline);
		m_actualLogger.clear();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Add/Remove/Get test
	//
	////////////////////////////////////////////////////////////////////////////
	public void test_addPoint_getPoints() throws Exception {
		// check reset state during addPoint()
		m_polyline.addPoint(new Point(10, 20));
		//
		m_expectedLogger.log("repaint(true, 10, 20, 1, 1)");
		m_actualLogger.assertEquals(m_expectedLogger);
		//
		// check reset state during addPoint()
		m_polyline.addPoint(new Point(-90, 0));
		//
		m_expectedLogger.log("repaint(true, -90, 0, 101, 21)");
		m_actualLogger.assertEquals(m_expectedLogger);
		//
		// check reset state during addPoint()
		m_polyline.addPoint(new Point(120, -70));
		//
		m_expectedLogger.log("repaint(true, -90, -70, 211, 91)");
		m_actualLogger.assertEquals(m_expectedLogger);
		//
		// check add null point and not reset state
		try {
			m_polyline.addPoint(null);
			fail();
		} catch (NullPointerException e) {
			m_actualLogger.assertEmpty();
		}
		//
		// check work getPoint(int)
		PointList list = m_polyline.getPoints();
		assertNotNull(list);
		assertEquals(3, list.size());
		assertEquals(new Point(10, 20), list.getPoint(0));
		assertEquals(new Point(-90, 0), list.getPoint(1));
		assertEquals(new Point(120, -70), list.getPoint(2));
	}

	public void test_insertPoint() throws Exception {
		// check insert point with wrong index and not reset state
		try {
			m_polyline.insertPoint(new Point(), 1);
			fail();
		} catch (IndexOutOfBoundsException e) {
			m_actualLogger.assertEmpty();
		}
		//
		// check work insert point and reset state
		m_polyline.insertPoint(new Point(), 0);
		//
		m_expectedLogger.log("repaint(true, 0, 0, 1, 1)");
		m_actualLogger.assertEquals(m_expectedLogger);
		//
		// check work insert point and reset state
		m_polyline.addPoint(new Point(10, 20));
		//
		m_expectedLogger.log("repaint(true, 0, 0, 11, 21)");
		m_actualLogger.assertEquals(m_expectedLogger);
		//
		// check work insert point and reset state
		m_polyline.addPoint(new Point(-90, 0));
		//
		m_expectedLogger.log("repaint(true, -90, 0, 101, 21)");
		m_actualLogger.assertEquals(m_expectedLogger);
		//
		// check work insert point and reset state
		m_polyline.insertPoint(new Point(-1, -1), 1);
		//
		m_expectedLogger.log("repaint(true, -90, -1, 101, 22)");
		m_actualLogger.assertEquals(m_expectedLogger);
		//
		// check points order
		PointList list = m_polyline.getPoints();
		assertEquals(4, list.size());
		assertEquals(new Point(), list.getPoint(0));
		assertEquals(new Point(-1, -1), list.getPoint(1));
		assertEquals(new Point(10, 20), list.getPoint(2));
		assertEquals(new Point(-90, 0), list.getPoint(3));
		//
		// check insert null point index and not reset state
		try {
			m_polyline.insertPoint(null, 0);
			fail();
		} catch (NullPointerException e) {
			m_actualLogger.assertEmpty();
		}
	}

	public void test_removePoint() throws Exception {
		m_polyline.addPoint(new Point(10, -20));
		m_polyline.addPoint(new Point(-90, 0));
		m_polyline.addPoint(new Point(120, 120));
		m_actualLogger.clear();
		//
		PointList list = m_polyline.getPoints();
		assertEquals(3, list.size());
		//
		// check remove point with wrong index and not reset state
		try {
			m_polyline.removePoint(4);
			fail();
		} catch (IndexOutOfBoundsException e) {
			m_actualLogger.assertEmpty();
		}
		//
		// check remove point and reset state
		m_polyline.removePoint(2);
		//
		m_expectedLogger.log("repaint(true, -90, -20, 101, 21)");
		m_actualLogger.assertEquals(m_expectedLogger);
		//
		assertEquals(2, list.size());
		assertEquals(new Point(10, -20), list.getPoint(0));
		assertEquals(new Point(-90, 0), list.getPoint(1));
		//
		// check remove point and reset state
		m_polyline.removePoint(0);
		//
		m_expectedLogger.log("repaint(true, -90, 0, 1, 1)");
		m_actualLogger.assertEquals(m_expectedLogger);
		//
		assertEquals(1, list.size());
		assertEquals(new Point(-90, 0), list.getPoint(0));
		//
		// check remove point and reset state
		m_polyline.removePoint(0);
		//
		m_expectedLogger.log("repaint(true, 0, 0, 0, 0)");
		m_actualLogger.assertEquals(m_expectedLogger);
		//
		assertEquals(0, list.size());
		//
		// check remove point with wrong index and not reset state
		try {
			m_polyline.removePoint(0);
			fail();
		} catch (IndexOutOfBoundsException e) {
			m_actualLogger.assertEmpty();
		}
	}

	public void test_removeAllPoints() throws Exception {
		// check work removeAllPoints() when not children
		m_polyline.removeAllPoints();
		//
		m_expectedLogger.log("repaint(true, 0, 0, 0, 0)");
		m_actualLogger.assertEquals(m_expectedLogger);
		//
		PointList list = m_polyline.getPoints();
		assertEquals(0, list.size());
		//
		m_polyline.addPoint(new Point(10, -20));
		m_polyline.addPoint(new Point(-90, 0));
		m_polyline.addPoint(new Point(120, 120));
		m_actualLogger.clear();
		//
		assertEquals(3, list.size());
		//
		// check work removeAllPoints()
		m_polyline.removeAllPoints();
		//
		m_expectedLogger.log("repaint(true, 0, 0, 0, 0)");
		m_actualLogger.assertEquals(m_expectedLogger);
		//
		assertEquals(0, list.size());
		//
		// check work removeAllPoints() when not children
		m_polyline.removeAllPoints();
		//
		m_expectedLogger.log("repaint(true, 0, 0, 0, 0)");
		m_actualLogger.assertEquals(m_expectedLogger);
		//
		assertEquals(0, list.size());
	}

	public void test_getStart() throws Exception {
		Polyline polyline = new Polyline();
		//
		// check work getStart()
		polyline.addPoint(new Point(10, -20));
		assertEquals(new Point(10, -20), polyline.getStart());
		//
		// check work getStart()
		polyline.addPoint(new Point(-90, 0));
		polyline.addPoint(new Point(120, 120));
		assertEquals(new Point(10, -20), polyline.getStart());
	}

	public void test_getEnd() throws Exception {
		Polyline polyline = new Polyline();
		//
		// check work getEnd()
		polyline.addPoint(new Point(10, -20));
		assertEquals(new Point(10, -20), polyline.getEnd());
		//
		// check work getEnd()
		polyline.addPoint(new Point(-90, 0));
		assertEquals(new Point(-90, 0), polyline.getEnd());
		//
		// check work getEnd()
		polyline.addPoint(new Point(120, 120));
		assertEquals(new Point(120, 120), polyline.getEnd());
	}

	public void test_setPoint_Point_int() throws Exception {
		m_polyline.addPoint(new Point(10, -20));
		m_polyline.addPoint(new Point(40, 40));
		m_actualLogger.clear();
		//
		// check work setPoint() with wrong index
		try {
			m_polyline.setPoint(new Point(), -1);
			fail();
		} catch (IndexOutOfBoundsException e) {
			m_actualLogger.assertEmpty();
		}
		//
		// check work setPoint() with wrong index
		try {
			m_polyline.setPoint(new Point(), 2);
			fail();
		} catch (IndexOutOfBoundsException e) {
			m_actualLogger.assertEmpty();
		}
		//
		// check work setPoint() for null point
		try {
			m_polyline.setPoint(null, 0);
			fail();
		} catch (NullPointerException e) {
			m_actualLogger.assertEmpty();
		}
		//
		// check work setPoint()
		Point point = new Point(3, 4);
		m_polyline.setPoint(point, 1);
		//
		m_expectedLogger.log("repaint(true, 3, -20, 8, 25)");
		m_actualLogger.assertEquals(m_expectedLogger);
		//
		assertEquals(new Point(3, 4), point);
		assertNotSame(point, m_polyline.getPoints().getPoint(1));
		assertEquals(point, m_polyline.getPoints().getPoint(1));
		//
		// check work setPoint()
		point = new Point(-1, 2);
		m_polyline.setPoint(point, 0);
		//
		m_expectedLogger.log("repaint(true, -1, 2, 5, 3)");
		m_actualLogger.assertEquals(m_expectedLogger);
		//
		assertEquals(new Point(-1, 2), point);
		assertNotSame(point, m_polyline.getPoints().getPoint(0));
		assertEquals(point, m_polyline.getPoints().getPoint(0));
	}

	public void test_set_Start() throws Exception {
		assertEquals(0, m_polyline.getPoints().size());
		//
		// check work setStart() and reset state
		m_polyline.setStart(new Point(10, 10));
		//
		m_expectedLogger.log("repaint(true, 10, 10, 1, 1)");
		m_actualLogger.assertEquals(m_expectedLogger);
		//
		assertEquals(1, m_polyline.getPoints().size());
		assertEquals(new Point(10, 10), m_polyline.getStart());
		//
		// check work setStart() and reset state
		m_polyline.setStart(new Point(120, -110));
		//
		m_expectedLogger.log("repaint(true, 120, -110, 1, 1)");
		m_actualLogger.assertEquals(m_expectedLogger);
		//
		assertEquals(1, m_polyline.getPoints().size());
		assertEquals(new Point(120, -110), m_polyline.getStart());
	}

	public void test_set_End() throws Exception {
		assertEquals(0, m_polyline.getPoints().size());
		//
		// check work setEnd() and reset state
		m_polyline.setEnd(new Point(1, 1));
		//
		m_expectedLogger.log("repaint(true, 1, 1, 1, 1)");
		m_actualLogger.assertEquals(m_expectedLogger);
		//
		assertEquals(1, m_polyline.getPoints().size());
		assertEquals(new Point(1, 1), m_polyline.getEnd());
		//
		m_polyline = new Polyline();
		m_root.add(m_polyline);
		//
		m_polyline.addPoint(new Point());
		m_polyline.addPoint(new Point());
		assertEquals(2, m_polyline.getPoints().size());
		m_actualLogger.clear();
		//
		// check work setEnd() and reset state
		m_polyline.setEnd(new Point(10, 10));
		//
		m_expectedLogger.log("repaint(true, 0, 0, 11, 11)");
		m_actualLogger.assertEquals(m_expectedLogger);
		//
		assertEquals(2, m_polyline.getPoints().size());
		assertEquals(new Point(10, 10), m_polyline.getEnd());
		//
		// check work setEnd() and reset state
		m_polyline.setEnd(new Point(120, -110));
		//
		m_expectedLogger.log("repaint(true, 0, -110, 121, 111)");
		m_actualLogger.assertEquals(m_expectedLogger);
		//
		assertEquals(2, m_polyline.getPoints().size());
		assertEquals(new Point(120, -110), m_polyline.getEnd());
	}

	public void test_set_Endpoints() throws Exception {
		m_polyline.addPoint(new Point());
		m_polyline.addPoint(new Point());
		m_actualLogger.clear();
		//
		assertEquals(2, m_polyline.getPoints().size());
		//
		// check work setEndpoints() and reset state
		m_polyline.setEndpoints(new Point(10, 10), new Point(120, -110));
		//
		m_expectedLogger.log("repaint(true, 0, 0, 11, 11)");
		m_expectedLogger.log("repaint(true, 10, -110, 111, 121)");
		m_actualLogger.assertEquals(m_expectedLogger);
		//
		assertEquals(2, m_polyline.getPoints().size());
		assertEquals(new Point(10, 10), m_polyline.getStart());
		assertEquals(new Point(120, -110), m_polyline.getEnd());
		//
		// check work setEndpoints() and reset state
		m_polyline.setEndpoints(new Point(120, -110), new Point(10, 10));
		//
		m_expectedLogger.log("repaint(true, 120, -110, 1, 1)");
		m_expectedLogger.log("repaint(true, 10, -110, 111, 121)");
		m_actualLogger.assertEquals(m_expectedLogger);
		//
		assertEquals(2, m_polyline.getPoints().size());
		assertEquals(new Point(120, -110), m_polyline.getStart());
		assertEquals(new Point(10, 10), m_polyline.getEnd());
	}

	public void test_setPoints() throws Exception {
		m_polyline.addPoint(new Point(1, 2));
		m_polyline.addPoint(new Point(3, 4));
		m_actualLogger.clear();
		//
		// check work setPoints() and reset state
		PointList list1 = m_polyline.getPoints();
		PointList list2 = new PointList();
		m_polyline.setPoints(list2);
		//
		m_expectedLogger.log("repaint(true, 0, 0, 0, 0)");
		m_actualLogger.assertEquals(m_expectedLogger);
		//
		assertNotSame(list1, m_polyline.getPoints());
		assertSame(list2, m_polyline.getPoints());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Bounds tests
	//
	////////////////////////////////////////////////////////////////////////////
	public void test_bounds() throws Exception {
		// check bounds for new empty polyline
		assertEquals(new Rectangle(), m_polyline.getBounds());
		//
		// check not change bounds and reset state
		m_polyline.setBounds(new Rectangle(10, 20, 30, 40));
		m_actualLogger.assertEmpty();
		assertEquals(new Rectangle(), m_polyline.getBounds());
		//
		// check work getBounds()
		m_polyline.addPoint(new Point(10, 10));
		m_polyline.addPoint(new Point(20, 20));
		//
		Rectangle bounds = m_polyline.getBounds();
		assertEquals(
				new Rectangle(10, 10, 11, 11).expand(
						m_polyline.getLineStyle() / 2,
						m_polyline.getLineStyle() / 2),
				bounds);
		assertSame(bounds, m_polyline.getBounds());
		//
		// check work getBounds()
		m_polyline.addPoint(new Point(40, 40));
		Rectangle boundsNew = m_polyline.getBounds();
		assertEquals(
				new Rectangle(10, 10, 31, 31).expand(
						m_polyline.getLineStyle() / 2,
						m_polyline.getLineStyle() / 2),
				boundsNew);
		assertSame(bounds, boundsNew);
		assertSame(boundsNew, m_polyline.getBounds());
	}

	public void test_containsPoint() throws Exception {
		Polyline polyline = new Polyline();
		polyline.addPoint(new Point(10, 10));
		polyline.addPoint(new Point(70, 20));
		polyline.addPoint(new Point(100, 100));
		polyline.addPoint(new Point(10, 100));
		polyline.addPoint(new Point(10, 10));
		//
		assertFalse(polyline.containsPoint(0, 0));
		assertFalse(polyline.containsPoint(30, 40));
		assertTrue(polyline.containsPoint(10, 10));
		assertTrue(polyline.containsPoint(10, 100));
		assertTrue(polyline.containsPoint(100, 100));
		assertTrue(polyline.containsPoint(10, 32));
		assertTrue(polyline.containsPoint(35, 100));
		assertTrue(polyline.containsPoint(22, 12));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Property tests
	//
	////////////////////////////////////////////////////////////////////////////
	public void test_opaque() throws Exception {
		// check opaque for new empty polyline
		assertFalse(m_polyline.isOpaque());
		//
		// check not change opaque and reset state
		m_polyline.setOpaque(true);
		m_actualLogger.assertEmpty();
		assertFalse(m_polyline.isOpaque());
		//
		// check not change opaque and reset state
		m_polyline.setOpaque(true);
		m_actualLogger.assertEmpty();
		assertFalse(m_polyline.isOpaque());
		//
		// check not change opaque and reset state
		m_polyline.setOpaque(false);
		m_actualLogger.assertEmpty();
		assertFalse(m_polyline.isOpaque());
		//
		// check not change opaque and reset state
		m_polyline.setOpaque(false);
		m_actualLogger.assertEmpty();
		assertFalse(m_polyline.isOpaque());
	}

	public void test_lineStyle() throws Exception {
		// check lineStyle for new empty polyline
		assertEquals(SWT.LINE_SOLID, m_polyline.getLineStyle());
		//
		// check change lineStyle
		m_polyline.setLineStyle(SWT.LINE_SOLID);
		m_actualLogger.assertEmpty();
		assertEquals(SWT.LINE_SOLID, m_polyline.getLineStyle());
		//
		// check change lineStyle
		m_polyline.setLineStyle(SWT.LINE_DOT);
		//
		m_expectedLogger.log("repaint(false, 0, 0, 0, 0)");
		m_actualLogger.assertEquals(m_expectedLogger);
		//
		assertEquals(SWT.LINE_DOT, m_polyline.getLineStyle());
	}

	public void test_lineWidth() throws Exception {
		// check lineWidth for new empty polyline
		assertEquals(1, m_polyline.getLineWidth());
		//
		// check change lineWidth and reset state
		m_polyline.setLineWidth(3);
		//
		m_expectedLogger.log("repaint(true, -1, -1, 2, 2)");
		m_actualLogger.assertEquals(m_expectedLogger);
		//
		assertEquals(3, m_polyline.getLineWidth());
		//
		// check change lineWidth and reset state
		m_polyline.setLineWidth(3);
		m_actualLogger.assertEmpty();
		assertEquals(3, m_polyline.getLineWidth());
	}

	public void test_XorMode() throws Exception {
		// check xor mode for new empty polyline
		assertFalse(m_polyline.isXorMode());
		//
		// check change xor mode
		m_polyline.setXorMode(true);
		//
		m_expectedLogger.log("repaint(false, 0, 0, 0, 0)");
		m_actualLogger.assertEquals(m_expectedLogger);
		//
		assertTrue(m_polyline.isXorMode());
		//
		// check work setXorMode() when xor mode not change
		m_polyline.setXorMode(true);
		m_actualLogger.assertEmpty();
		assertTrue(m_polyline.isXorMode());
		//
		// check change xor mode
		m_polyline.setXorMode(false);
		//
		m_expectedLogger.log("repaint(false, 0, 0, 0, 0)");
		m_actualLogger.assertEquals(m_expectedLogger);
		//
		assertFalse(m_polyline.isXorMode());
		//
		// check work setXorMode() when xor mode not change
		m_polyline.setXorMode(false);
		m_actualLogger.assertEmpty();
		assertFalse(m_polyline.isXorMode());
	}
}