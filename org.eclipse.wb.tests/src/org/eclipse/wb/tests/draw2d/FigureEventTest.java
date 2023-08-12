/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
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
import org.eclipse.wb.internal.draw2d.FigureCanvas;
import org.eclipse.wb.tests.gef.EventSender;
import org.eclipse.wb.tests.gef.TestLogger;

import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Shell;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author lobas_av
 *
 */
public class FigureEventTest extends Draw2dFigureTestCase {
	private Shell m_shell;
	private EventSender m_sender;
	private FigureCanvas m_canvas;

	////////////////////////////////////////////////////////////////////////////
	//
	// SetUp
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		// configure
		m_shell = new Shell();
		m_shell.setLayout(new FillLayout());
		m_canvas = new FigureCanvas(m_shell, SWT.NONE);
		m_sender = new EventSender(m_canvas);
	}

	@Override
	@After
	public void tearDown() throws Exception {
		m_shell.dispose();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Track tests
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_handleMouseEvents() throws Exception {
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
		//
		TestLogger actualLogger = new TestLogger();
		//
		MouseEventsLogger listener = new MouseEventsLogger(actualLogger);
		listener.addFigure(figure11);
		listener.addFigure(figure12);
		listener.addFigure(figure21);
		listener.addFigure(figure22);
		//
		m_canvas.getRootFigure().addLayer(layer1);
		m_canvas.getRootFigure().addLayer(layer2);
		m_shell.setSize(500, 400);
		//
		TestLogger expectedLogger = new TestLogger();
		//
		// move to point without figures
		{
			m_sender.moveTo(5, 5);
			actualLogger.assertEmpty();
		}
		// move to "figure11"
		{
			m_sender.moveTo(15, 15);
			//
			expectedLogger.log("mouseMove = MouseEvent(5,5) to Figure: figure11");
			actualLogger.assertEquals(expectedLogger);
		}
		// click over "figure11"
		{
			m_sender.click(20, 20, 3);
			//
			expectedLogger.log("mouseDown = MouseEvent(10,10) to Figure: figure11");
			expectedLogger.log("mouseUp = MouseEvent(10,10) to Figure: figure11");
			actualLogger.assertEquals(expectedLogger);
		}
		// double click over "figure21"
		{
			m_sender.doubleClick(60, 60, 2);
			//
			expectedLogger.log("mouseDown = MouseEvent(10,10) to Figure: figure21");
			expectedLogger.log("mouseUp = MouseEvent(10,10) to Figure: figure21");
			expectedLogger.log("mouseDown = MouseEvent(10,10) to Figure: figure21");
			expectedLogger.log("mouseDoubleClick = MouseEvent(10,10) to Figure: figure21");
			expectedLogger.log("mouseUp = MouseEvent(10,10) to Figure: figure21");
			actualLogger.assertEquals(expectedLogger);
		}
		// move to "figure12"
		{
			m_sender.moveTo(420, 330);
			//
			expectedLogger.log("mouseMove = MouseEvent(20,30) to Figure: figure12");
			actualLogger.assertEquals(expectedLogger);
		}
		// click over "figure22"
		{
			m_sender.click(200, 300, 5);
			//
			expectedLogger.log("mouseDown = MouseEvent(50,50) to Figure: figure22");
			expectedLogger.log("mouseUp = MouseEvent(50,50) to Figure: figure22");
			actualLogger.assertEquals(expectedLogger);
		}
		// move to point without figures
		{
			m_sender.moveTo(445, 115);
			actualLogger.assertEmpty();
		}
	}

	private static class MouseEventsLogger implements MouseListener, MouseMotionListener {
		private final TestLogger m_logger;

		////////////////////////////////////////////////////////////////////////////
		//
		// Constructor
		//
		////////////////////////////////////////////////////////////////////////////
		public MouseEventsLogger(TestLogger logger) {
			m_logger = logger;
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Listeners
		//
		////////////////////////////////////////////////////////////////////////////
		public void addFigure(Figure figure) {
			figure.addMouseListener(this);
			figure.addMouseMotionListener(this);
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// IMouseListener
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		public void mousePressed(MouseEvent event) {
			m_logger.log("mouseDown = " + event);
		}

		@Override
		public void mouseReleased(MouseEvent event) {
			m_logger.log("mouseUp = " + event);
		}

		@Override
		public void mouseDoubleClicked(MouseEvent event) {
			m_logger.log("mouseDoubleClick = " + event);
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// IMouseMoveListener
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		public void mouseMoved(MouseEvent event) {
			m_logger.log("mouseMove = " + event);
		}

		@Override
		public void mouseDragged(MouseEvent event) {
			// ignore
		}

		@Override
		public void mouseEntered(MouseEvent event) {
			// ignore
		}

		@Override
		public void mouseExited(MouseEvent event) {
			// ignore
		}

		@Override
		public void mouseHover(MouseEvent event) {
			// ignore
		}
	}
}