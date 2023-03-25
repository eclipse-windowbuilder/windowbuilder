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

import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.gef.core.requests.AbstractCreateRequest;
import org.eclipse.wb.gef.core.tools.AbstractCreationTool;

import org.eclipse.swt.SWT;

/**
 * @author lobas_av
 *
 */
public abstract class AbstractCreationToolTest extends RequestTestCase {
  protected AbstractCreateRequest m_request;
  protected AbstractCreationTool m_tool;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractCreationToolTest(Class<?> _class) {
    super(_class);
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
    configureTestCase();
  }

  protected abstract void configureTestCase();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_Move_DND() throws Exception {
    RequestsLogger actualLogger = new RequestsLogger();
    //
    RequestTestCaseEditPart editPart =
        addEditPart(m_viewer.getRootEditPart(), "parentEditPart", actualLogger, 50, 50, 400, 300);
    RequestTestCaseEditPart childEditPart1 =
        addEditPart(editPart, "childEditPart1", actualLogger, 50, 50, 70, 50);
    RequestTestCaseEditPart childEditPart2 =
        addEditPart(editPart, "childEditPart2", actualLogger, 250, 25, 100, 155);
    //
    RequestsLogger expectedLogger = new RequestsLogger();
    //
    // move outside of any EditPart
    {
      m_sender.moveTo(10, 10);
      actualLogger.assertEmpty();
    }
    // enter in "editPart"
    {
      m_sender.moveTo(60, 60);
      m_request.setLocation(new Point(60, 60));
      //
      expectedLogger.log(editPart, new String[]{
          "getTargetEditPart",
          "getTargetEditPart",
          "showTargetFeedback",
          "showTargetFeedback",
          "getCommand"}, m_request);
      assertLoggers(expectedLogger, actualLogger);
    }
    // exit from "editPart" and enter in "childEditPart1"
    {
      m_sender.moveTo(120, 120);
      m_request.setLocation(new Point(120, 120));
      //
      expectedLogger.log(
          childEditPart1,
          new String[]{"getTargetEditPart", "getTargetEditPart"},
          m_request);
      expectedLogger.log(editPart, "eraseTargetFeedback", m_request);
      expectedLogger.log(childEditPart1, new String[]{
          "showTargetFeedback",
          "showTargetFeedback",
          "getCommand"}, m_request);
      assertLoggers(expectedLogger, actualLogger);
    }
    // move into "childEditPart1"
    {
      m_sender.moveTo(130, 130);
      //
      m_request.setLocation(new Point(130, 130));
      //
      expectedLogger.log(childEditPart1, new String[]{
          "getTargetEditPart",
          "getTargetEditPart",
          "showTargetFeedback",
          "getCommand"}, m_request);
      assertLoggers(expectedLogger, actualLogger);
    }
    // start drag process
    {
      m_sender.startDrag(130, 130, 1);
      actualLogger.assertEmpty();
    }
    // drag
    {
      m_sender.dragTo(200, 200);
      //
      m_request.setStateMask(SWT.BUTTON1);
      m_request.setLocation(new Point(130, 130));
      m_request.setSize(new Dimension(70, 70));
      //
      expectedLogger.log(
          childEditPart1,
          new String[]{"showTargetFeedback", "getCommand"},
          m_request);
      assertLoggers(expectedLogger, actualLogger);
      ////////////////////////////////////////////////////////////////
      m_sender.dragTo(330, 140);
      //
      m_request.setLocation(new Point(130, 130));
      m_request.setSize(new Dimension(200, 10));
      //
      expectedLogger.log(
          childEditPart1,
          new String[]{"showTargetFeedback", "getCommand"},
          m_request);
      assertLoggers(expectedLogger, actualLogger);
    }
    // end drag process
    {
      m_sender.endDrag();
      //
      m_request.setStateMask(SWT.BUTTON1);
      m_request.setLocation(new Point(130, 130));
      m_request.setSize(new Dimension(200, 10));
      //
      expectedLogger.log(childEditPart1, "eraseTargetFeedback", m_request);
      expectedLogger.log(childEditPart2, new String[]{
          "getTargetEditPart",
          "getTargetEditPart",
          "showTargetFeedback",
          "eraseTargetFeedback"}, m_request);
      assertLoggers(expectedLogger, actualLogger);
    }
    // move after deactivate tool
    {
      m_sender.moveTo(300, 150);
      actualLogger.assertEmpty();
    }
    // click after deactivate tool
    {
      m_sender.click(330, 170, 1);
      actualLogger.assertEmpty();
    }
  }

  public void test_Move_DND_InvalidInput() throws Exception {
    RequestsLogger actualLogger = new RequestsLogger();
    //
    RequestTestCaseEditPart editPart =
        addEditPart(m_viewer.getRootEditPart(), "editPart", actualLogger, 50, 50, 400, 300);
    //
    RequestsLogger expectedLogger = new RequestsLogger();
    //
    // move into "editPart"
    {
      m_sender.moveTo(70, 70);
      //
      m_request.setLocation(new Point(70, 70));
      //
      expectedLogger.log(editPart, new String[]{
          "getTargetEditPart",
          "getTargetEditPart",
          "showTargetFeedback",
          "showTargetFeedback",
          "getCommand"}, m_request);
      assertLoggers(expectedLogger, actualLogger);
    }
    // click into "editPart" invalid button
    {
      m_sender.click(70, 70, 2);
      //
      m_request.setLocation(new Point(70, 70));
      //
      expectedLogger.log(editPart, "eraseTargetFeedback", m_request);
      assertLoggers(expectedLogger, actualLogger);
    }
    // move into "editPart" after deactivate tool
    {
      m_sender.moveTo(80, 80);
      actualLogger.assertEmpty();
    }
    // move into "editPart"
    {
      m_domain.setActiveTool(m_tool);
      m_sender.moveTo(90, 90);
      //
      m_request.setLocation(new Point(90, 90));
      //
      expectedLogger.log(editPart, new String[]{
          "getTargetEditPart",
          "getTargetEditPart",
          "showTargetFeedback",
          "showTargetFeedback",
          "getCommand"}, m_request);
      assertLoggers(expectedLogger, actualLogger);
    }
    // start drag process
    {
      m_sender.startDrag(90, 90, 1);
      actualLogger.assertEmpty();
    }
    // drag
    {
      m_sender.dragTo(100, 100);
      //
      m_request.setStateMask(SWT.BUTTON1);
      m_request.setLocation(new Point(90, 90));
      m_request.setSize(new Dimension(10, 10));
      //
      expectedLogger.log(editPart, new String[]{"showTargetFeedback", "getCommand"}, m_request);
      assertLoggers(expectedLogger, actualLogger);
    }
    // click invalid button during drag process
    {
      m_sender.click(100, 100, 3);
      m_request.setStateMask(SWT.BUTTON1);
      m_request.setLocation(new Point(90, 90));
      m_request.setSize(new Dimension(10, 10));
      //
      expectedLogger.log(editPart, "eraseTargetFeedback", m_request);
      assertLoggers(expectedLogger, actualLogger);
    }
    // move into "editPart" after deactivate tool
    {
      m_sender.moveTo(100, 100);
      actualLogger.assertEmpty();
    }
  }
}