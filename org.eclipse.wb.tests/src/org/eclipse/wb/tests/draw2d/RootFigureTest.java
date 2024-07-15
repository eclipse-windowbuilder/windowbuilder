/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
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
import org.eclipse.wb.internal.draw2d.RootFigure;
import org.eclipse.wb.tests.gef.TestLogger;

import org.eclipse.draw2d.UpdateListener;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;

import org.junit.Test;

import java.util.Map;

/**
 * @author lobas_av
 *
 */
public class RootFigureTest extends Draw2dFigureTestCase {

	////////////////////////////////////////////////////////////////////////////
	//
	// RootFigure tests
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_getPreferredSize_setBounds() throws Exception {
		final TestLogger actualLogger = new TestLogger();
		//
		TestLogger expectedLogger = new TestLogger();
		//
		RootFigure testRoot = new RootFigure(null);
		testRoot.getUpdateManager().addUpdateListener(new UpdateListener() {
			@Override
			@SuppressWarnings("rawtypes")
			public void notifyPainting(Rectangle damage, Map dirtyRegions) {
				actualLogger.log("refreshRequest(" + damage.x + ", " + damage.y + ", " + damage.width + ", " + damage.height + ")");
			}

			@Override
			public void notifyValidating() {
				// Nothing to do
			}
		});
		//
		Layer layer0 = new Layer("Main");
		Figure figure0 = new Figure();
		figure0.setBounds(new Rectangle(10, 10, 100, 200));
		layer0.add(figure0);
		testRoot.addLayer(layer0);
		//
		Layer layer1 = new Layer("Feedback");
		Figure figure1 = new Figure();
		figure1.setBounds(new Rectangle(50, 70, 120, 90));
		layer1.add(figure1);
		testRoot.addLayer(layer1);
		//
		actualLogger.clear();
		//
		// check calc getPreferredSize()
		Dimension preferredSize = testRoot.getPreferredSize();
		assertEquals(new Dimension(170, 210), preferredSize);
		//
		// check work setBounds() and not reset state
		testRoot.setBounds(new Rectangle(0, 0, 180, 150));
		actualLogger.assertEmpty();
		assertEquals(new Rectangle(0, 0, 180, 210), testRoot.getBounds());
		assertEquals(new Rectangle(0, 0, 180, 210), layer0.getBounds());
		assertEquals(new Rectangle(10, 10, 100, 200), figure0.getBounds());
		assertEquals(new Rectangle(0, 0, 180, 210), layer1.getBounds());
		assertEquals(new Rectangle(50, 70, 120, 90), figure1.getBounds());
		//
		// check independent betweeb preffered size and bounds
		assertSame(preferredSize, testRoot.getPreferredSize());
		//
		// check work repaint()
		figure1.repaint();
		waitEventLoop(10);
		//
		expectedLogger.log("refreshRequest(50, 70, 120, 90)");
		actualLogger.assertEquals(expectedLogger);
		//
		assertSame(preferredSize, testRoot.getPreferredSize());
		//
		// check work resetState()
		figure0.revalidate();
		figure0.repaint();
		waitEventLoop(10);
		//
		expectedLogger.log("refreshRequest(10, 10, 100, 200)");
		actualLogger.assertEquals(expectedLogger);
		//
		assertNotSame(preferredSize, testRoot.getPreferredSize());
		assertEquals(preferredSize, testRoot.getPreferredSize());
	}

	@Test
	public void test_findTargetFigure() throws Exception {
		Layer layer1 = new Layer("1");
		Figure figure11 = new Figure() {
			@Override
			public String toString() {
				return "figure11";
			}
		};
		layer1.add(figure11, new Rectangle(10, 10, 200, 150));
		Figure figure12 = new Figure() {
			@Override
			public String toString() {
				return "figure12";
			}
		};
		layer1.add(figure12, new Rectangle(400, 300, 50, 70));
		//
		Layer layer2 = new Layer("2");
		Figure figure21 = new Figure() {
			@Override
			public String toString() {
				return "figure21";
			}
		};
		layer2.add(figure21, new Rectangle(50, 50, 90, 60));
		Figure figure22 = new Figure() {
			@Override
			public String toString() {
				return "figure22";
			}
		};
		layer2.add(figure22, new Rectangle(150, 250, 190, 120));
		Figure figure23 = new Figure() {
			@Override
			public String toString() {
				return "figure23";
			}
		};
		figure22.add(figure23, new Rectangle(15, 25, 19, 12));
		//
		RootFigure testRoot = new RootFigure(null) {
			@Override
			public void repaint(int x, int y, int width, int height) {
			}
		};
		testRoot.addLayer(layer1);
		testRoot.addLayer(layer2);
		testRoot.setBounds(new Rectangle(0, 0, 500, 400));
		//
		// check work findTargetFigure()
		/*		assertNull(testRoot.findTargetFigure(-1, -1));
		assertSame(testRoot, testRoot.findTargetFigure(0, 0));
		assertSame(figure11, testRoot.findTargetFigure(20, 20));
		assertSame(figure21, testRoot.findTargetFigure(70, 70));
		assertSame(figure11, testRoot.findTargetFigure(150, 100));
		assertSame(testRoot, testRoot.findTargetFigure(220, 200));
		assertSame(testRoot, testRoot.findTargetFigure(399, 299));
		assertSame(figure12, testRoot.findTargetFigure(420, 320));
		assertSame(figure23, testRoot.findTargetFigure(170, 280));
		assertSame(figure22, testRoot.findTargetFigure(300, 300));*/
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Layer Test's
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_addLayer_getLayer() throws Exception {
		TestLogger actualLogger = new TestLogger();
		//
		TestCaseRootFigure testRoot = new TestCaseRootFigure(actualLogger);
		//
		TestLogger expectedLogger = new TestLogger();
		//
		// check add null Layer
		try {
			testRoot.addLayer(null);
			fail();
		} catch (NullPointerException e) {
		}
		//
		assertEquals(0, testRoot.getLayers().size());
		//
		// check add Layer
		Layer layer0 = new Layer("Main");
		testRoot.addLayer(layer0);
		//
		expectedLogger.log("invalidate");
		expectedLogger.log("repaint(0, 0, 0, 0)");
		actualLogger.assertEquals(expectedLogger);
		//
		assertSame(testRoot, layer0.getParent());
		assertEquals(1, testRoot.getLayers().size());
		//
		// check add Layer
		Layer layer1 = new Layer("Feedback");
		testRoot.addLayer(layer1);
		//
		expectedLogger.log("invalidate");
		expectedLogger.log("repaint(0, 0, 0, 0)");
		actualLogger.assertEquals(expectedLogger);
		//
		assertSame(testRoot, layer1.getParent());
		assertEquals(2, testRoot.getLayers().size());
		//
		// check child order
		assertSame(layer0, testRoot.getLayers().get(0));
		assertSame(layer1, testRoot.getLayers().get(1));
		//
		// check work getLayer()
		assertSame(layer0, testRoot.getLayer("Main"));
		assertSame(layer1, testRoot.getLayer("Feedback"));
		assertNull(testRoot.getLayer("feedback"));
		assertNull(testRoot.getLayer(null));
	}

	@Test
	public void test_remove() throws Exception {
		TestLogger actualLogger = new TestLogger();
		//
		TestCaseRootFigure testRoot = new TestCaseRootFigure(actualLogger);
		//
		TestLogger expectedLogger = new TestLogger();
		//
		Layer layer0 = new Layer("Feedback");
		testRoot.addLayer(layer0);
		actualLogger.clear();
		//
		// check remove null Layer
		try {
			testRoot.removeLayer((Layer) null);
			fail();
		} catch (NullPointerException e) {
		}
		//
		// check remove Layer with not exist name
		try {
			testRoot.removeLayer("feedback");
			fail();
		} catch (NullPointerException e) {
		}
		//
		Layer layer1 = new Layer("feedback");
		testRoot.addLayer(layer1);
		actualLogger.clear();
		//
		// check work removeLayer(Layer)
		testRoot.removeLayer(layer0);
		//
		expectedLogger.log("repaint(0, 0, 0, 0)");
		expectedLogger.log("invalidate");
		actualLogger.assertEquals(expectedLogger);
		//
		assertNull(layer0.getParent());
		assertEquals(1, testRoot.getLayers().size());
		assertNull(testRoot.getLayer("Feedback"));
		//
		// check work removeLayer(String)
		testRoot.removeLayer("feedback");
		//
		expectedLogger.log("repaint(0, 0, 0, 0)");
		expectedLogger.log("invalidate");
		actualLogger.assertEquals(expectedLogger);
		//
		assertNull(layer0.getParent());
		assertEquals(0, testRoot.getLayers().size());
		assertNull(testRoot.getLayer("feedback"));
	}

	@Test
	public void test_removeAll() throws Exception {
		TestLogger actualLogger = new TestLogger();
		//
		TestCaseRootFigure testRoot = new TestCaseRootFigure(actualLogger);
		//
		TestLogger expectedLogger = new TestLogger();
		//
		// check not reset state if not children
		testRoot.removeAll();
		actualLogger.assertEmpty();
		//
		Layer layer0 = new Layer("Main");
		testRoot.addLayer(layer0);
		Layer layer1 = new Layer("Feedback");
		testRoot.addLayer(layer1);
		actualLogger.clear();
		//
		// check reset state during removeAll()
		testRoot.removeAll();
		//
		expectedLogger.log("repaint(0, 0, 0, 0)");
		expectedLogger.log("invalidate");
		expectedLogger.log("repaint(0, 0, 0, 0)");
		expectedLogger.log("invalidate");
		actualLogger.assertEquals(expectedLogger);
		//
		assertNull(layer0.getParent());
		assertNull(testRoot.getLayer("Main"));
		assertNull(layer1.getParent());
		assertNull(testRoot.getLayer("Feedback"));
		//
		// check not reset state if not children
		testRoot.removeAll();
		actualLogger.assertEmpty();
	}
}