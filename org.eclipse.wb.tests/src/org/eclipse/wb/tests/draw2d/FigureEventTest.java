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
import org.eclipse.wb.draw2d.events.IMouseListener;
import org.eclipse.wb.draw2d.events.IMouseMoveListener;
import org.eclipse.wb.draw2d.events.MouseEvent;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.draw2d.FigureCanvas;
import org.eclipse.wb.tests.gef.EventSender;
import org.eclipse.wb.tests.gef.TestLogger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Shell;

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
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public FigureEventTest() {
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
    // configure
    m_shell = new Shell();
    m_shell.setLayout(new FillLayout());
    m_canvas = new FigureCanvas(m_shell, SWT.NONE);
    m_sender = new EventSender(m_canvas);
  }

  @Override
  protected void tearDown() throws Exception {
    m_shell.dispose();
    super.tearDown();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Track tests
  //
  ////////////////////////////////////////////////////////////////////////////
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
      expectedLogger.log("mouseMove = MouseEvent{source=figure11 button=0 stateMask=0 x=5 y=5}");
      actualLogger.assertEquals(expectedLogger);
    }
    // click over "figure11"
    {
      m_sender.click(20, 20, 3);
      //
      expectedLogger.log("mouseDown = MouseEvent{source=figure11 button=3 stateMask=0 x=10 y=10}");
      expectedLogger.log("mouseUp = MouseEvent{source=figure11 button=3 stateMask="
          + SWT.BUTTON3
          + " x=10 y=10}");
      actualLogger.assertEquals(expectedLogger);
    }
    // double click over "figure21"
    {
      m_sender.doubleClick(60, 60, 2);
      //
      expectedLogger.log("mouseDown = MouseEvent{source=figure21 button=2 stateMask=0 x=10 y=10}");
      expectedLogger.log("mouseUp = MouseEvent{source=figure21 button=2 stateMask="
          + SWT.BUTTON2
          + " x=10 y=10}");
      expectedLogger.log("mouseDown = MouseEvent{source=figure21 button=2 stateMask=0 x=10 y=10}");
      expectedLogger.log("mouseDoubleClick = MouseEvent{source=figure21 button=2 stateMask=0 x=10 y=10}");
      expectedLogger.log("mouseUp = MouseEvent{source=figure21 button=2 stateMask="
          + SWT.BUTTON2
          + " x=10 y=10}");
      actualLogger.assertEquals(expectedLogger);
    }
    // move to "figure12"
    {
      m_sender.moveTo(420, 330);
      //
      expectedLogger.log("mouseMove = MouseEvent{source=figure12 button=0 stateMask=0 x=20 y=30}");
      actualLogger.assertEquals(expectedLogger);
    }
    // click over "figure22"
    {
      m_sender.click(200, 300, 5);
      //
      expectedLogger.log("mouseDown = MouseEvent{source=figure22 button=5 stateMask=0 x=50 y=50}");
      expectedLogger.log("mouseUp = MouseEvent{source=figure22 button=5 stateMask="
          + SWT.BUTTON5
          + " x=50 y=50}");
      actualLogger.assertEquals(expectedLogger);
    }
    // move to point without figures
    {
      m_sender.moveTo(445, 115);
      actualLogger.assertEmpty();
    }
  }

  private static class MouseEventsLogger implements IMouseListener, IMouseMoveListener {
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
      figure.addMouseMoveListener(this);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // IMouseListener
    //
    ////////////////////////////////////////////////////////////////////////////
    public void mouseDown(MouseEvent event) {
      m_logger.log("mouseDown = " + event);
    }

    public void mouseUp(MouseEvent event) {
      m_logger.log("mouseUp = " + event);
    }

    public void mouseDoubleClick(MouseEvent event) {
      m_logger.log("mouseDoubleClick = " + event);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // IMouseMoveListener
    //
    ////////////////////////////////////////////////////////////////////////////
    public void mouseMove(MouseEvent event) {
      m_logger.log("mouseMove = " + event);
    }
  }
}