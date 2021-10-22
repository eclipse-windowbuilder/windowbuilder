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
package org.eclipse.wb.tests.gef;

import org.eclipse.wb.draw2d.IPositionConstants;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.graphical.tools.ResizeTracker;

import org.eclipse.swt.SWT;

/**
 * @author lobas_av
 *
 */
public class ResizeTrackerTest extends RequestTestCase {
  private int m_direction;
  private Object m_type;
  private RequestsLogger m_actualLogger;
  private RequestTestCaseEditPart m_editPart;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ResizeTrackerTest() {
    super(ResizeTracker.class);
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
    m_actualLogger = new RequestsLogger();
    //
    m_editPart = new RequestTestCaseEditPart("editPart", m_actualLogger);
    m_editPart.activate();
    m_viewer.select(m_editPart);
  }

  private void setUp(int direction, Object type) {
    m_direction = direction;
    m_type = type;
    m_domain.setActiveTool(new ResizeTracker(direction, type));
  }

  private ChangeBoundsRequest createEmptyRequest() {
    ChangeBoundsRequest request = new ChangeBoundsRequest(m_type);
    request.setResizeDirection(m_direction);
    return request;
  }

  private ChangeBoundsRequest createRequest() {
    ChangeBoundsRequest request = createEmptyRequest();
    request.addEditPart(m_editPart);
    return request;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Request tests
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Check not work after deactivate tool.
   */
  private void commonEndTest() throws Exception {
    m_sender.startDrag(10, 10, 1);
    m_sender.dragTo(20, 20);
    m_sender.endDrag();
    m_actualLogger.assertEmpty();
    //
    m_domain.loadDefaultTool();
    //
    m_sender.startDrag(10, 10, 2);
    m_sender.dragTo(20, 20);
    m_sender.endDrag();
    m_actualLogger.assertEmpty();
  }

  public void test_Request_from_NORTH() throws Exception {
    setUp(IPositionConstants.NORTH, "__Resize_N_");
    //
    RequestsLogger expectedLogger = new RequestsLogger();
    // start drag process of handle
    {
      m_sender.startDrag(10, 18, 1);
      m_actualLogger.assertEmpty();
    }
    // drag handle
    {
      m_sender.dragTo(10, 11);
      //
      ChangeBoundsRequest request = createEmptyRequest();
      //
      expectedLogger.log(m_editPart, "getTargetEditPart", request);
      //
      request.addEditPart(m_editPart);
      request.setStateMask(SWT.BUTTON1);
      request.setLocation(new Point(10, 11));
      request.setSizeDelta(new Dimension(0, 7));
      request.setMoveDelta(new Point(0, -7));
      //
      expectedLogger.log(m_editPart, new String[]{"showSourceFeedback", "getCommand"}, request);
      assertLoggers(expectedLogger, m_actualLogger);
    }
    // drag handle
    {
      m_sender.dragTo(10, 10);
      //
      ChangeBoundsRequest request = createRequest();
      request.setStateMask(SWT.BUTTON1);
      request.setLocation(new Point(10, 10));
      request.setSizeDelta(new Dimension(0, 8));
      request.setMoveDelta(new Point(0, -8));
      //
      expectedLogger.log(m_editPart, new String[]{"showSourceFeedback", "getCommand"}, request);
      assertLoggers(expectedLogger, m_actualLogger);
    }
    // drag handle
    {
      m_sender.dragTo(11, 11);
      //
      ChangeBoundsRequest request = createRequest();
      request.setStateMask(SWT.BUTTON1);
      request.setLocation(new Point(11, 11));
      request.setSizeDelta(new Dimension(0, 7));
      request.setMoveDelta(new Point(0, -7));
      //
      expectedLogger.log(m_editPart, new String[]{"showSourceFeedback", "getCommand"}, request);
      assertLoggers(expectedLogger, m_actualLogger);
    }
    // end drag process of handle
    {
      m_sender.endDrag();
      //
      ChangeBoundsRequest request = createRequest();
      request.setStateMask(SWT.BUTTON1);
      request.setLocation(new Point(11, 11));
      request.setSizeDelta(new Dimension(0, 7));
      request.setMoveDelta(new Point(0, -7));
      //
      expectedLogger.log(m_editPart, "eraseSourceFeedback", request);
      assertLoggers(expectedLogger, m_actualLogger);
    }
    //
    commonEndTest();
  }

  public void test_Request_from_SOUTH() throws Exception {
    setUp(IPositionConstants.SOUTH, "__Resize_S_");
    //
    RequestsLogger expectedLogger = new RequestsLogger();
    // start drag process of handle
    {
      m_sender.startDrag(10, 10, 1);
      m_actualLogger.assertEmpty();
    }
    // drag handle
    {
      m_sender.dragTo(10, 17);
      //
      ChangeBoundsRequest request = createEmptyRequest();
      //
      expectedLogger.log(m_editPart, "getTargetEditPart", request);
      //
      request.setStateMask(SWT.BUTTON1);
      request.addEditPart(m_editPart);
      request.setLocation(new Point(10, 17));
      request.setSizeDelta(new Dimension(0, 7));
      //
      expectedLogger.log(m_editPart, new String[]{"showSourceFeedback", "getCommand"}, request);
      assertLoggers(expectedLogger, m_actualLogger);
    }
    // drag handle end drag process of handle
    {
      m_sender.dragTo(10, 18);
      //
      ChangeBoundsRequest request = createRequest();
      request.setStateMask(SWT.BUTTON1);
      request.setLocation(new Point(10, 18));
      request.setSizeDelta(new Dimension(0, 8));
      //
      expectedLogger.log(m_editPart, new String[]{"showSourceFeedback", "getCommand"}, request);
      assertLoggers(expectedLogger, m_actualLogger);
    }
    // drag handle
    {
      m_sender.dragTo(11, 17);
      //
      ChangeBoundsRequest request = createRequest();
      request.setStateMask(SWT.BUTTON1);
      request.setLocation(new Point(11, 17));
      request.setSizeDelta(new Dimension(0, 7));
      //
      expectedLogger.log(m_editPart, new String[]{"showSourceFeedback", "getCommand"}, request);
      assertLoggers(expectedLogger, m_actualLogger);
    }
    // drag handle end drag process of handle
    {
      m_sender.endDrag();
      //
      ChangeBoundsRequest request = createRequest();
      request.setStateMask(SWT.BUTTON1);
      request.setLocation(new Point(11, 17));
      request.setSizeDelta(new Dimension(0, 7));
      //
      expectedLogger.log(m_editPart, "eraseSourceFeedback", request);
      assertLoggers(expectedLogger, m_actualLogger);
    }
    //
    commonEndTest();
  }

  public void test_Request_from_WEST() throws Exception {
    setUp(IPositionConstants.WEST, "__Resize_E_");
    //
    RequestsLogger expectedLogger = new RequestsLogger();
    // start drag process of handle
    {
      m_sender.startDrag(18, 10, 1);
      m_actualLogger.assertEmpty();
    }
    // drag handle
    {
      m_sender.dragTo(11, 10);
      //
      ChangeBoundsRequest request = createEmptyRequest();
      //
      expectedLogger.log(m_editPart, "getTargetEditPart", request);
      //
      request.addEditPart(m_editPart);
      request.setStateMask(SWT.BUTTON1);
      request.setLocation(new Point(11, 10));
      request.setSizeDelta(new Dimension(7, 0));
      request.setMoveDelta(new Point(-7, 0));
      //
      expectedLogger.log(m_editPart, new String[]{"showSourceFeedback", "getCommand"}, request);
      assertLoggers(expectedLogger, m_actualLogger);
    }
    // drag handle
    {
      m_sender.dragTo(10, 10);
      //
      ChangeBoundsRequest request = createRequest();
      request.setStateMask(SWT.BUTTON1);
      request.setLocation(new Point(10, 10));
      request.setSizeDelta(new Dimension(8, 0));
      request.setMoveDelta(new Point(-8, 0));
      //
      expectedLogger.log(m_editPart, new String[]{"showSourceFeedback", "getCommand"}, request);
      assertLoggers(expectedLogger, m_actualLogger);
    }
    // drag handle
    {
      m_sender.dragTo(11, 11);
      //
      ChangeBoundsRequest request = createRequest();
      request.setStateMask(SWT.BUTTON1);
      request.setLocation(new Point(11, 11));
      request.setSizeDelta(new Dimension(7, 0));
      request.setMoveDelta(new Point(-7, 0));
      //
      expectedLogger.log(m_editPart, new String[]{"showSourceFeedback", "getCommand"}, request);
      assertLoggers(expectedLogger, m_actualLogger);
    }
    // end drag process of handle
    {
      m_sender.endDrag();
      //
      ChangeBoundsRequest request = createRequest();
      request.setStateMask(SWT.BUTTON1);
      request.setLocation(new Point(11, 11));
      request.setSizeDelta(new Dimension(7, 0));
      request.setMoveDelta(new Point(-7, 0));
      //
      expectedLogger.log(m_editPart, "eraseSourceFeedback", request);
      assertLoggers(expectedLogger, m_actualLogger);
    }
    //
    commonEndTest();
  }

  public void test_Request_from_EAST() throws Exception {
    setUp(IPositionConstants.EAST, "__Resize_E_");
    //
    RequestsLogger expectedLogger = new RequestsLogger();
    // start drag process of handle
    {
      m_sender.startDrag(10, 10, 1);
      m_actualLogger.assertEmpty();
    }
    // drag handle
    {
      m_sender.dragTo(17, 10);
      //
      ChangeBoundsRequest request = createEmptyRequest();
      //
      expectedLogger.log(m_editPart, "getTargetEditPart", request);
      //
      request.setStateMask(SWT.BUTTON1);
      request.addEditPart(m_editPart);
      request.setLocation(new Point(17, 10));
      request.setSizeDelta(new Dimension(7, 0));
      //
      expectedLogger.log(m_editPart, new String[]{"showSourceFeedback", "getCommand"}, request);
      assertLoggers(expectedLogger, m_actualLogger);
    }
    // drag handle
    {
      m_sender.dragTo(18, 10);
      //
      ChangeBoundsRequest request = createRequest();
      request.setStateMask(SWT.BUTTON1);
      request.setLocation(new Point(18, 10));
      request.setSizeDelta(new Dimension(8, 0));
      //
      expectedLogger.log(m_editPart, new String[]{"showSourceFeedback", "getCommand"}, request);
      assertLoggers(expectedLogger, m_actualLogger);
    }
    // drag handle
    {
      m_sender.dragTo(17, 11);
      //
      ChangeBoundsRequest request = createRequest();
      request.setStateMask(SWT.BUTTON1);
      request.setLocation(new Point(17, 11));
      request.setSizeDelta(new Dimension(7, 0));
      //
      expectedLogger.log(m_editPart, new String[]{"showSourceFeedback", "getCommand"}, request);
      assertLoggers(expectedLogger, m_actualLogger);
    }
    // end drag process of handle
    {
      m_sender.endDrag();
      //
      ChangeBoundsRequest request = createRequest();
      request.setStateMask(SWT.BUTTON1);
      request.setLocation(new Point(17, 11));
      request.setSizeDelta(new Dimension(7, 0));
      //
      expectedLogger.log(m_editPart, "eraseSourceFeedback", request);
      assertLoggers(expectedLogger, m_actualLogger);
    }
    //
    commonEndTest();
  }

  public void test_Request_from_NORTH_EAST() throws Exception {
    setUp(IPositionConstants.NORTH_EAST, "__Resize_NE_");
    //
    RequestsLogger expectedLogger = new RequestsLogger();
    // start drag process of handle
    {
      m_sender.startDrag(18, 18, 1);
      m_actualLogger.assertEmpty();
    }
    // drag handle
    {
      m_sender.dragTo(25, 11);
      //
      ChangeBoundsRequest request = createEmptyRequest();
      //
      expectedLogger.log(m_editPart, "getTargetEditPart", request);
      //
      request.addEditPart(m_editPart);
      request.setStateMask(SWT.BUTTON1);
      request.setLocation(new Point(25, 11));
      request.setSizeDelta(new Dimension(7, 7));
      request.setMoveDelta(new Point(0, -7));
      //
      expectedLogger.log(m_editPart, new String[]{"showSourceFeedback", "getCommand"}, request);
      assertLoggers(expectedLogger, m_actualLogger);
    }
    // end drag process of handle
    {
      m_sender.endDrag();
      //
      ChangeBoundsRequest request = createRequest();
      request.setStateMask(SWT.BUTTON1);
      request.setLocation(new Point(25, 11));
      request.setSizeDelta(new Dimension(7, 7));
      request.setMoveDelta(new Point(0, -7));
      //
      expectedLogger.log(m_editPart, "eraseSourceFeedback", request);
      assertLoggers(expectedLogger, m_actualLogger);
    }
    //
    commonEndTest();
  }

  public void test_Request_from_NORTH_WEST() throws Exception {
    setUp(IPositionConstants.NORTH_WEST, "__Resize_NW_");
    //
    RequestsLogger expectedLogger = new RequestsLogger();
    // start drag process of handle
    {
      m_sender.startDrag(18, 18, 1);
      m_actualLogger.assertEmpty();
    }
    // drag handle
    {
      m_sender.dragTo(11, 11);
      //
      ChangeBoundsRequest request = createEmptyRequest();
      //
      expectedLogger.log(m_editPart, "getTargetEditPart", request);
      //
      request.addEditPart(m_editPart);
      request.setStateMask(SWT.BUTTON1);
      request.setLocation(new Point(11, 11));
      request.setSizeDelta(new Dimension(7, 7));
      request.setMoveDelta(new Point(-7, -7));
      //
      expectedLogger.log(m_editPart, new String[]{"showSourceFeedback", "getCommand"}, request);
      assertLoggers(expectedLogger, m_actualLogger);
    }
    // end drag process of handle
    {
      m_sender.endDrag();
      //
      ChangeBoundsRequest request = createRequest();
      request.setStateMask(SWT.BUTTON1);
      request.setLocation(new Point(11, 11));
      request.setSizeDelta(new Dimension(7, 7));
      request.setMoveDelta(new Point(-7, -7));
      //
      expectedLogger.log(m_editPart, "eraseSourceFeedback", request);
      assertLoggers(expectedLogger, m_actualLogger);
    }
    //
    commonEndTest();
  }

  public void test_Request_from_SOUTH_EAST() throws Exception {
    setUp(IPositionConstants.SOUTH_EAST, "__Resize_SE_");
    //
    RequestsLogger expectedLogger = new RequestsLogger();
    // start drag process of handle
    {
      m_sender.startDrag(11, 11, 1);
      m_actualLogger.assertEmpty();
    }
    // drag handle
    {
      m_sender.dragTo(18, 18);
      //
      ChangeBoundsRequest request = createEmptyRequest();
      //
      expectedLogger.log(m_editPart, "getTargetEditPart", request);
      //
      request.addEditPart(m_editPart);
      request.setStateMask(SWT.BUTTON1);
      request.setLocation(new Point(18, 18));
      request.setSizeDelta(new Dimension(7, 7));
      //
      expectedLogger.log(m_editPart, new String[]{"showSourceFeedback", "getCommand"}, request);
      assertLoggers(expectedLogger, m_actualLogger);
    }
    // end drag process of handle
    {
      m_sender.endDrag();
      //
      ChangeBoundsRequest request = createRequest();
      request.setStateMask(SWT.BUTTON1);
      request.setLocation(new Point(18, 18));
      request.setSizeDelta(new Dimension(7, 7));
      //
      expectedLogger.log(m_editPart, "eraseSourceFeedback", request);
      assertLoggers(expectedLogger, m_actualLogger);
    }
    //
    commonEndTest();
  }

  public void test_Request_from_SOUTH_WEST() throws Exception {
    setUp(IPositionConstants.SOUTH_WEST, "__Resize_SW_");
    //
    RequestsLogger expectedLogger = new RequestsLogger();
    // start drag process of handle
    {
      m_sender.startDrag(18, 18, 1);
      m_actualLogger.assertEmpty();
    }
    // drag handle
    {
      m_sender.dragTo(11, 25);
      //
      ChangeBoundsRequest request = createEmptyRequest();
      //
      expectedLogger.log(m_editPart, "getTargetEditPart", request);
      //
      request.addEditPart(m_editPart);
      request.setStateMask(SWT.BUTTON1);
      request.setLocation(new Point(11, 25));
      request.setSizeDelta(new Dimension(7, 7));
      request.setMoveDelta(new Point(-7, 0));
      //
      expectedLogger.log(m_editPart, new String[]{"showSourceFeedback", "getCommand"}, request);
      assertLoggers(expectedLogger, m_actualLogger);
    }
    // end drag process of handle
    {
      m_sender.endDrag();
      //
      ChangeBoundsRequest request = createRequest();
      request.setStateMask(SWT.BUTTON1);
      request.setLocation(new Point(11, 25));
      request.setSizeDelta(new Dimension(7, 7));
      request.setMoveDelta(new Point(-7, 0));
      //
      expectedLogger.log(m_editPart, "eraseSourceFeedback", request);
      assertLoggers(expectedLogger, m_actualLogger);
    }
    //
    commonEndTest();
  }

  public void test_not_Request() throws Exception {
    setUp(IPositionConstants.NORTH, "__Resize_N_");
    // start drag process use invalid button
    m_sender.startDrag(10, 18, 3);
    m_actualLogger.assertEmpty();
    // drag
    m_sender.dragTo(10, 11);
    m_actualLogger.assertEmpty();
    // drag
    m_sender.dragTo(10, 10);
    m_actualLogger.assertEmpty();
    // drag
    m_sender.dragTo(11, 11);
    m_actualLogger.assertEmpty();
    // end drag process use invalid button
    m_sender.endDrag();
    m_actualLogger.assertEmpty();
  }

  public void test_break_Request() throws Exception {
    setUp(IPositionConstants.NORTH, "__Resize_N_");
    //
    RequestsLogger expectedLogger = new RequestsLogger();
    // start drag process of handle
    {
      m_sender.startDrag(10, 18, 1);
      m_actualLogger.assertEmpty();
    }
    // drag handle
    {
      m_sender.dragTo(10, 11);
      //
      ChangeBoundsRequest request = createEmptyRequest();
      //
      expectedLogger.log(m_editPart, "getTargetEditPart", request);
      //
      request.addEditPart(m_editPart);
      request.setStateMask(SWT.BUTTON1);
      request.setLocation(new Point(10, 11));
      request.setSizeDelta(new Dimension(0, 7));
      request.setMoveDelta(new Point(0, -7));
      //
      expectedLogger.log(m_editPart, new String[]{"showSourceFeedback", "getCommand"}, request);
      assertLoggers(expectedLogger, m_actualLogger);
    }
    // invalid click during drag process
    {
      m_sender.click(10, 11, 3);
      //
      ChangeBoundsRequest request = createRequest();
      request.setStateMask(SWT.BUTTON1);
      request.setLocation(new Point(10, 11));
      request.setSizeDelta(new Dimension(0, 7));
      request.setMoveDelta(new Point(0, -7));
      //
      expectedLogger.log(m_editPart, "eraseSourceFeedback", request);
      assertLoggers(expectedLogger, m_actualLogger);
    }
    // drag after deactivate tool
    {
      m_sender.dragTo(8, 9);
      m_actualLogger.assertEmpty();
    }
    // drag after deactivate tool
    {
      m_sender.dragTo(11, 11);
      m_actualLogger.assertEmpty();
    }
    // end drag process after deactivate tool
    {
      m_sender.endDrag();
      m_actualLogger.assertEmpty();
    }
  }
}