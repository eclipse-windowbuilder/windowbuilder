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

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.graphical.tools.MarqueeSelectionTool;

import org.eclipse.swt.SWT;

/**
 * @author lobas_av
 *
 */
public class MarqueeSelectionToolTest extends RequestTestCase {
  private MarqueeSelectionTool m_tool;
  private Request m_request;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public MarqueeSelectionToolTest() {
    super(MarqueeSelectionTool.class);
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
    m_tool = new MarqueeSelectionTool();
    m_domain.setActiveTool(m_tool);
    //
    m_request = new Request(Request.REQ_SELECTION);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_Selection() throws Exception {
    RequestsLogger actualLogger = new RequestsLogger();
    //
    RequestTestCaseEditPart editPart =
        addEditPart(m_viewer.getRootEditPart(), "ParentEditPart", actualLogger, 50, 50, 400, 300);
    //
    RequestTestCaseEditPart childEditPart1 =
        addEditPart(editPart, "ChildEditPart1", actualLogger, 50, 50, 70, 50);
    //
    RequestTestCaseEditPart childEditPart2 =
        addEditPart(editPart, "ChildEditPart2", actualLogger, 250, 25, 100, 155);
    //
    RequestsLogger expectedLogger = new RequestsLogger();
    //
    // start drag process of selection "ChildEditPart1"
    {
      m_sender.startDrag(95, 95, 1);
      //
      assertTrue(m_viewer.getSelectedEditParts().isEmpty());
      actualLogger.assertEmpty();
    }
    // drag
    {
      m_sender.dragTo(175, 175);
      //
      assertTrue(m_viewer.getSelectedEditParts().isEmpty());
      //
      expectedLogger.log(
          childEditPart1,
          new String[]{"getTargetEditPart", "showTargetFeedback"},
          m_request);
      assertLoggers(expectedLogger, actualLogger);
    }
    // drag
    {
      m_sender.dragTo(177, 177);
      //
      assertTrue(m_viewer.getSelectedEditParts().isEmpty());
      //
      expectedLogger.log(childEditPart1, new String[]{
          "eraseTargetFeedback",
          "getTargetEditPart",
          "showTargetFeedback"}, m_request);
      assertLoggers(expectedLogger, actualLogger);
    }
    // end drag process
    {
      m_sender.endDrag();
      //
      assertEquals(1, m_viewer.getSelectedEditParts().size());
      assertSame(childEditPart1, m_viewer.getSelectedEditParts().get(0));
      assertEquals(EditPart.SELECTED_PRIMARY, childEditPart1.getSelected());
      //
      expectedLogger.log(
          childEditPart1,
          new String[]{"eraseTargetFeedback", "getTargetEditPart"},
          m_request);
      assertLoggers(expectedLogger, actualLogger);
    }
    // start drag process of append selection "ChildEditPart2"
    {
      m_domain.setActiveTool(m_tool);
      m_sender.setStateMask(SWT.SHIFT);
      m_sender.startDrag(295, 60, 1);
      //
      actualLogger.assertEmpty();
    }
    // drag
    {
      m_sender.dragTo(405, 235);
      //
      expectedLogger.log(
          childEditPart2,
          new String[]{"getTargetEditPart", "showTargetFeedback"},
          m_request);
      assertLoggers(expectedLogger, actualLogger);
    }
    // end drag process
    {
      m_sender.endDrag();
      m_sender.setStateMask(0);
      //
      assertEquals(2, m_viewer.getSelectedEditParts().size());
      assertSame(childEditPart1, m_viewer.getSelectedEditParts().get(0));
      assertSame(childEditPart2, m_viewer.getSelectedEditParts().get(1));
      assertEquals(EditPart.SELECTED, childEditPart1.getSelected());
      assertEquals(EditPart.SELECTED_PRIMARY, childEditPart2.getSelected());
      //
      expectedLogger.log(
          childEditPart2,
          new String[]{"eraseTargetFeedback", "getTargetEditPart"},
          m_request);
      assertLoggers(expectedLogger, actualLogger);
    }
    // start drag process of all selection
    {
      m_domain.setActiveTool(m_tool);
      m_sender.startDrag(10, 10, 1);
      //
      actualLogger.assertEmpty();
    }
    // drag
    {
      m_sender.dragTo(460, 360);
      //
      expectedLogger.log(editPart, "getTargetEditPart", m_request);
      expectedLogger.log(childEditPart1, "getTargetEditPart", m_request);
      expectedLogger.log(childEditPart2, "getTargetEditPart", m_request);
      expectedLogger.log(editPart, "showTargetFeedback", m_request);
      expectedLogger.log(childEditPart1, "showTargetFeedback", m_request);
      expectedLogger.log(childEditPart2, "showTargetFeedback", m_request);
      assertLoggers(expectedLogger, actualLogger);
    }
    // end drag process
    {
      m_sender.endDrag();
      //
      assertEquals(3, m_viewer.getSelectedEditParts().size());
      assertSame(editPart, m_viewer.getSelectedEditParts().get(0));
      assertSame(childEditPart1, m_viewer.getSelectedEditParts().get(1));
      assertSame(childEditPart2, m_viewer.getSelectedEditParts().get(2));
      assertEquals(EditPart.SELECTED, editPart.getSelected());
      assertEquals(EditPart.SELECTED, childEditPart1.getSelected());
      assertEquals(EditPart.SELECTED_PRIMARY, childEditPart2.getSelected());
      //
      expectedLogger.log(editPart, "eraseTargetFeedback", m_request);
      expectedLogger.log(childEditPart1, "eraseTargetFeedback", m_request);
      expectedLogger.log(childEditPart2, "eraseTargetFeedback", m_request);
      expectedLogger.log(editPart, "getTargetEditPart", m_request);
      expectedLogger.log(childEditPart1, "getTargetEditPart", m_request);
      expectedLogger.log(childEditPart2, "getTargetEditPart", m_request);
      assertLoggers(expectedLogger, actualLogger);
    }
    // start drag process of deselection "ChildEditPart1" and "ChildEditPart2"
    {
      m_domain.setActiveTool(m_tool);
      m_sender.setStateMask(SWT.CONTROL);
      m_sender.startDrag(95, 60, 1);
      //
      actualLogger.assertEmpty();
    }
    // drag
    {
      m_sender.dragTo(405, 235);
      //
      expectedLogger.log(childEditPart1, "getTargetEditPart", m_request);
      expectedLogger.log(childEditPart2, "getTargetEditPart", m_request);
      expectedLogger.log(childEditPart1, "showTargetFeedback", m_request);
      expectedLogger.log(childEditPart2, "showTargetFeedback", m_request);
      assertLoggers(expectedLogger, actualLogger);
    }
    // end drag process
    {
      m_sender.endDrag();
      m_sender.setStateMask(0);
      //
      assertEquals(1, m_viewer.getSelectedEditParts().size());
      assertSame(editPart, m_viewer.getSelectedEditParts().get(0));
      assertEquals(EditPart.SELECTED_PRIMARY, editPart.getSelected());
      assertEquals(EditPart.SELECTED_NONE, childEditPart1.getSelected());
      assertEquals(EditPart.SELECTED_NONE, childEditPart2.getSelected());
      //
      expectedLogger.log(childEditPart1, "eraseTargetFeedback", m_request);
      expectedLogger.log(childEditPart2, "eraseTargetFeedback", m_request);
      expectedLogger.log(childEditPart1, "getTargetEditPart", m_request);
      expectedLogger.log(childEditPart2, "getTargetEditPart", m_request);
      assertLoggers(expectedLogger, actualLogger);
    }
    // start drag process of selection "ChildEditPart1" and deselection "ChildEditPart2"
    m_viewer.appendSelection(childEditPart2);
    assertEquals(EditPart.SELECTED_PRIMARY, childEditPart2.getSelected());
    //
    {
      m_domain.setActiveTool(m_tool);
      m_sender.setStateMask(SWT.CONTROL);
      m_sender.startDrag(95, 60, 1);
      //
      actualLogger.assertEmpty();
    }
    // drag
    {
      m_sender.dragTo(405, 235);
      //
      expectedLogger.log(childEditPart1, "getTargetEditPart", m_request);
      expectedLogger.log(childEditPart2, "getTargetEditPart", m_request);
      expectedLogger.log(childEditPart1, "showTargetFeedback", m_request);
      expectedLogger.log(childEditPart2, "showTargetFeedback", m_request);
      assertLoggers(expectedLogger, actualLogger);
    }
    // end drag process
    {
      m_sender.endDrag();
      m_sender.setStateMask(0);
      //
      assertEquals(2, m_viewer.getSelectedEditParts().size());
      assertSame(editPart, m_viewer.getSelectedEditParts().get(0));
      assertSame(childEditPart1, m_viewer.getSelectedEditParts().get(1));
      assertEquals(EditPart.SELECTED_PRIMARY, childEditPart1.getSelected());
      assertEquals(EditPart.SELECTED, editPart.getSelected());
      assertEquals(EditPart.SELECTED_NONE, childEditPart2.getSelected());
      //
      expectedLogger.log(childEditPart1, "eraseTargetFeedback", m_request);
      expectedLogger.log(childEditPart2, "eraseTargetFeedback", m_request);
      expectedLogger.log(childEditPart1, "getTargetEditPart", m_request);
      expectedLogger.log(childEditPart2, "getTargetEditPart", m_request);
      assertLoggers(expectedLogger, actualLogger);
    }
    // check not work after deactivate tool
    {
      m_sender.startDrag(10, 10, 1);
      m_sender.dragTo(100, 100);
      m_sender.endDrag();
      //
      assertEquals(2, m_viewer.getSelectedEditParts().size());
      assertSame(editPart, m_viewer.getSelectedEditParts().get(0));
      assertSame(childEditPart1, m_viewer.getSelectedEditParts().get(1));
      assertEquals(EditPart.SELECTED_PRIMARY, childEditPart1.getSelected());
      assertEquals(EditPart.SELECTED, editPart.getSelected());
      assertEquals(EditPart.SELECTED_NONE, childEditPart2.getSelected());
      //
      actualLogger.assertEmpty();
    }
  }

  public void test_Selection_isSelectable() throws Exception {
    RequestsLogger actualLogger = new RequestsLogger();
    //
    RequestTestCaseEditPart editPart =
        addEditPart(m_viewer.getRootEditPart(), "ParentEditPart", actualLogger, 50, 50, 400, 300);
    //
    RequestTestCaseEditPart childEditPart1 =
        addEditPart(editPart, "ChildEditPart1", actualLogger, 50, 50, 70, 50);
    //
    RequestTestCaseEditPart childEditPart2 =
        new RequestTestCaseEditPart("ChildEditPart2", actualLogger) {
          @Override
          public boolean isSelectable() {
            return false;
          }
        };
    childEditPart2.getFigure().setBounds(new Rectangle(250, 25, 100, 155));
    addChildEditPart(editPart, childEditPart2);
    //
    RequestsLogger expectedLogger = new RequestsLogger();
    //
    // start drag process over "ChildEditPart1" and "ChildEditPart2"
    {
      m_sender.startDrag(95, 60, 1);
      //
      assertTrue(m_viewer.getSelectedEditParts().isEmpty());
      actualLogger.assertEmpty();
    }
    // drag
    {
      m_sender.dragTo(405, 235);
      //
      expectedLogger.log(childEditPart1, "getTargetEditPart", m_request);
      expectedLogger.log(childEditPart1, "showTargetFeedback", m_request);
      assertLoggers(expectedLogger, actualLogger);
    }
    // end drag process
    {
      m_sender.endDrag();
      //
      assertEquals(1, m_viewer.getSelectedEditParts().size());
      assertSame(childEditPart1, m_viewer.getSelectedEditParts().get(0));
      assertEquals(EditPart.SELECTED_PRIMARY, childEditPart1.getSelected());
      assertEquals(EditPart.SELECTED_NONE, editPart.getSelected());
      assertEquals(EditPart.SELECTED_NONE, childEditPart2.getSelected());
      //
      expectedLogger.log(childEditPart1, "eraseTargetFeedback", m_request);
      expectedLogger.log(childEditPart1, "getTargetEditPart", m_request);
      assertLoggers(expectedLogger, actualLogger);
    }
  }

  public void test_Selection_InvalidInput() throws Exception {
    RequestsLogger actualLogger = new RequestsLogger();
    //
    RequestTestCaseEditPart editPart =
        addEditPart(m_viewer.getRootEditPart(), "ParentEditPart", actualLogger, 50, 50, 400, 300);
    //
    RequestTestCaseEditPart childEditPart =
        addEditPart(editPart, "ChildEditPart", actualLogger, 50, 50, 70, 50);
    //
    RequestsLogger expectedLogger = new RequestsLogger();
    //
    // start drag process of selection "ChildEditPart1"
    {
      assertTrue(m_viewer.getSelectedEditParts().isEmpty());
      //
      m_sender.startDrag(95, 95, 1);
      //
      actualLogger.assertEmpty();
    }
    // drag
    {
      m_sender.dragTo(175, 175);
      //
      expectedLogger.log(
          childEditPart,
          new String[]{"getTargetEditPart", "showTargetFeedback"},
          m_request);
      assertLoggers(expectedLogger, actualLogger);
    }
    // invalid input
    {
      m_sender.click(175, 175, 3);
      //
      assertTrue(m_viewer.getSelectedEditParts().isEmpty());
      //
      expectedLogger.log(childEditPart, "eraseTargetFeedback", m_request);
      assertLoggers(expectedLogger, actualLogger);
    }
    // deactivate
    {
      m_domain.setActiveTool(m_tool);
      expectedLogger.assertEmpty();
      //
      m_sender.startDrag(95, 95, 1);
      m_sender.dragTo(175, 175);
      //
      expectedLogger.log(
          childEditPart,
          new String[]{"getTargetEditPart", "showTargetFeedback"},
          m_request);
      assertLoggers(expectedLogger, actualLogger);
      //
      m_domain.loadDefaultTool();
      //
      expectedLogger.log(childEditPart, "eraseTargetFeedback", m_request);
      assertLoggers(expectedLogger, actualLogger);
    }
  }
}