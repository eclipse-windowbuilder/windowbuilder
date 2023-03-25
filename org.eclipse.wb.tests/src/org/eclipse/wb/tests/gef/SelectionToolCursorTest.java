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

import org.eclipse.wb.draw2d.ICursorConstants;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.gef.graphical.tools.SelectionTool;

import org.eclipse.swt.SWT;

/**
 * @author lobas_av
 *
 */
public class SelectionToolCursorTest extends GefCursorTestCase {
  private SelectionTool m_tool;
  private EditPart m_buttonEditPart;
  private EditPart m_shellEditPart;
  private CursorLogger m_expectedLogger;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SelectionToolCursorTest() {
    super(SelectionTool.class);
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
    m_tool = new SelectionTool();
    m_domain.setActiveTool(m_tool);
    //
    m_shellEditPart = createEditPart(m_viewer.getRootEditPart(), 20, 20, 460, 360, null, null);
    m_buttonEditPart = createEditPart(m_shellEditPart, 100, 100, 200, 100, null, null);
    m_viewer.select(m_buttonEditPart);
    m_expectedLogger = new CursorLogger();
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    m_tool = null;
    m_shellEditPart = null;
    m_buttonEditPart = null;
    m_expectedLogger = null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_Move() throws Exception {
    // first update cursor after mouse enter into viewer
    {
      m_actualLogger.assertEmpty();
      //
      m_sender.mouseEnter(0, 0);
      //
      m_actualLogger.assertEmpty();
    }
    // move to "RootEditPart"
    {
      m_sender.moveTo(10, 10);
      //
      m_actualLogger.assertEmpty();
    }
    // move to "ButtonEditPart_NORTH_WEST_ResizeHandle"
    {
      m_sender.moveTo(120, 120);
      //
      m_expectedLogger.setCursor(ICursorConstants.SIZENW);
      m_actualLogger.assertEquals(m_expectedLogger);
    }
    // move to "ShellEditPart"
    {
      m_sender.moveTo(50, 50);
      //
      m_expectedLogger.setCursor(null);
      m_actualLogger.assertEquals(m_expectedLogger);
    }
    // move to "ButtonEditPart_MoveHandle"
    {
      m_sender.moveTo(120, 130);
      //
      m_expectedLogger.setCursor(ICursorConstants.SIZEALL);
      m_actualLogger.assertEquals(m_expectedLogger);
    }
    // move to "RootEditPart"
    {
      m_sender.moveTo(5, 5);
      //
      m_expectedLogger.setCursor(null);
      m_actualLogger.assertEquals(m_expectedLogger);
    }
  }

  public void test_ResizeTracker() throws Exception {
    // first update cursor after mouse enter into viewer
    {
      m_actualLogger.assertEmpty();
      //
      m_sender.mouseEnter(0, 0);
      //
      m_actualLogger.assertEmpty();
    }
    // move to "ButtonEditPart_NORTH_WEST_ResizeHandle"
    {
      m_sender.moveTo(120, 120);
      //
      m_expectedLogger.setCursor(ICursorConstants.SIZENW);
      m_actualLogger.assertEquals(m_expectedLogger);
    }
    // start drag
    {
      m_sender.startDrag(120, 120, 1);
      //
      m_actualLogger.assertEmpty();
    }
    // drag over MoveHandle
    {
      m_sender.dragTo(130, 120);
      //
      m_actualLogger.assertEmpty();
    }
    // end drag
    {
      m_sender.endDrag();
      //
      m_expectedLogger.setCursor(ICursorConstants.SIZEALL);
      m_actualLogger.assertEquals(m_expectedLogger);
    }
    // move to "ShellEditPart"
    {
      m_sender.moveTo(110, 220);
      //
      m_expectedLogger.setCursor(null);
      m_actualLogger.assertEquals(m_expectedLogger);
    }
    // move to "ButtonEditPart_SOUTH_WEST_ResizeHandle"
    {
      m_sender.moveTo(120, 220);
      //
      m_expectedLogger.setCursor(ICursorConstants.SIZESW);
      m_actualLogger.assertEquals(m_expectedLogger);
    }
    // start drag
    {
      m_sender.startDrag(120, 220, 1);
      //
      m_actualLogger.assertEmpty();
    }
    // drag to "ShellEditPart"
    {
      m_sender.dragTo(100, 220);
      //
      m_actualLogger.assertEmpty();
    }
    // end drag
    {
      m_sender.endDrag();
      //
      m_expectedLogger.setCursor(null);
      m_actualLogger.assertEquals(m_expectedLogger);
    }
    // move to "ButtonEditPart_NORTH_ResizeHandle"
    {
      m_sender.moveTo(220, 120);
      //
      m_expectedLogger.setCursor(ICursorConstants.SIZEN);
      m_actualLogger.assertEquals(m_expectedLogger);
    }
    // start drag
    {
      m_sender.startDrag(220, 120, 1);
      //
      m_actualLogger.assertEmpty();
    }
    // drag to "ShellEditPart"
    {
      m_sender.dragTo(220, 100);
      //
      m_actualLogger.assertEmpty();
    }
    // invalid input
    {
      m_sender.startDrag(220, 100, 2);
      //
      m_actualLogger.assertEmpty();
    }
    // drag to "ShellEditPart"
    {
      m_sender.dragTo(220, 90);
      //
      m_actualLogger.assertEmpty();
    }
    // end drag
    {
      m_sender.endDrag();
      //
      //m_expectedLogger.setCursor(ICursorConstants.SIZEN);
      m_expectedLogger.setCursor(null);
      m_actualLogger.assertEquals(m_expectedLogger);
    }
  }

  public void DISABLE_test_ResizeTracker_Resize() throws Exception {
    ResizeCommand command = new ResizeCommand();
    GraphicalEditPart editPart = createEditPart(m_shellEditPart, 10, 10, 50, 60, null, command);
    command.setPart(editPart);
    m_viewer.select(editPart);
    CursorLogger expectedLogger = new CursorLogger();
    //
    // first update cursor after mouse enter into viewer
    {
      m_actualLogger.assertEmpty();
      //
      m_sender.mouseEnter(0, 0);
      //
      m_actualLogger.assertEmpty();
    }
    // move to "EditPart_NORTH_EAST_ResizeHandle"
    {
      m_sender.moveTo(80, 30);
      //
      expectedLogger.setCursor(ICursorConstants.SIZENE);
      m_actualLogger.assertEquals(expectedLogger);
    }
    // start drag
    {
      m_sender.startDrag(80, 30, 1);
      //
      m_actualLogger.assertEmpty();
    }
    // drag
    {
      m_sender.dragTo(90, 30);
      //
      m_actualLogger.assertEmpty();
    }
    // end drag
    {
      m_sender.endDrag();
      //
      m_actualLogger.assertEmpty();
    }
  }

  public void test_ResizeTracker_NoResize() throws Exception {
    EditPart editPart = createEditPart(m_shellEditPart, 10, 10, 50, 60, null, Command.EMPTY);
    m_viewer.select(editPart);
    CursorLogger expectedLogger = new CursorLogger();
    //
    // first update cursor after mouse enter into viewer
    {
      m_actualLogger.assertEmpty();
      //
      m_sender.mouseEnter(0, 0);
      //
      m_actualLogger.assertEmpty();
    }
    // move to "EditPart_NORTH_EAST_ResizeHandle"
    {
      m_sender.moveTo(80, 30);
      //
      expectedLogger.setCursor(ICursorConstants.SIZENE);
      m_actualLogger.assertEquals(expectedLogger);
    }
    // start drag
    {
      m_sender.startDrag(80, 30, 1);
      //
      m_actualLogger.assertEmpty();
    }
    // drag
    {
      m_sender.dragTo(90, 30);
      //
      m_actualLogger.assertEmpty();
    }
    // end drag
    {
      m_sender.endDrag();
      //
      expectedLogger.setCursor(null);
      m_actualLogger.assertEquals(expectedLogger);
    }
  }

  public void test_DragEditPartTracker_Click_NoMove() throws Exception {
    CursorLogger expectedLogger = new CursorLogger();
    // first update cursor after mouse enter into viewer
    {
      m_actualLogger.assertEmpty();
      //
      m_sender.mouseEnter(0, 0);
      //
      m_actualLogger.assertEmpty();
    }
    // move to left top corner "ShellEditPart"
    {
      m_sender.moveTo(16, 16);
      //
      m_actualLogger.assertEmpty();
    }
    // click to left top corner "ShellEditPart"
    {
      m_sender.click(20, 20, 1);
      //
      expectedLogger.setCursor(ICursorConstants.SIZENW);
      m_actualLogger.assertEquals(expectedLogger);
    }
    // move to "ShellEditPart_MoveHandle"
    {
      m_sender.moveTo(20, 30);
      //
      expectedLogger.setCursor(ICursorConstants.SIZEALL);
      m_actualLogger.assertEquals(expectedLogger);
    }
    // start drag "ShellEditPart"
    {
      m_sender.startDrag(20, 30, 1);
      //
      m_actualLogger.assertEmpty();
    }
    // drag
    {
      m_sender.dragTo(10, 30);
      //
      expectedLogger.setCursor(CreationToolCursorTest.CURSOR_NO());
      m_actualLogger.assertEquals(expectedLogger);
    }
    // end drag
    {
      m_sender.endDrag();
      //
      expectedLogger.setCursor(null);
      m_actualLogger.assertEquals(expectedLogger);
    }
  }

  public void test_DragEditPartTracker_Move() throws Exception {
    final GraphicalEditPart editPart = createEditPart(m_shellEditPart, 20, 20, 50, 50, null, null);
    m_shellEditPart.installEditPolicy(EditPolicy.LAYOUT_ROLE, new LayoutEditPolicy() {
      @Override
      protected Command getMoveCommand(ChangeBoundsRequest request) {
        return new Command() {
          @Override
          public void execute() throws Exception {
            editPart.getFigure().setBounds(new Rectangle(10, 10, 50, 50));
          }
        };
      }
    });
    m_viewer.select(editPart);
    CursorLogger expectedLogger = new CursorLogger();
    //
    // first update cursor after mouse enter into viewer
    {
      m_actualLogger.assertEmpty();
      //
      m_sender.mouseEnter(0, 0);
      //
      m_actualLogger.assertEmpty();
    }
    // move to "EditPart_MoveHandle"
    {
      m_sender.moveTo(40, 50);
      //
      expectedLogger.setCursor(ICursorConstants.SIZEALL);
      m_actualLogger.assertEquals(expectedLogger);
    }
    // start drag
    {
      m_sender.startDrag(40, 50, 1);
      //
      m_actualLogger.assertEmpty();
    }
    // drag
    {
      m_sender.dragTo(30, 50);
      //
      m_actualLogger.assertEmpty();
    }
    // end drag
    {
      m_sender.endDrag();
      //
      expectedLogger.setCursor(CreationToolCursorTest.CURSOR_NO());
      expectedLogger.setCursor(ICursorConstants.SIZEALL);
      m_actualLogger.assertEquals(expectedLogger);
    }
  }

  public void test_MarqueeDragTracker() throws Exception {
    // first update cursor after mouse enter into viewer
    {
      m_actualLogger.assertEmpty();
      //
      m_sender.mouseEnter(0, 0);
      //
      m_actualLogger.assertEmpty();
    }
    // move to "ShellEditPart"
    {
      m_sender.moveTo(50, 50);
      //
      m_actualLogger.assertEmpty();
    }
    // use tracker
    {
      m_sender.setStateMask(SWT.ALT);
      m_sender.startDrag(50, 50, 1);
      //
      m_expectedLogger.setCursor(ICursorConstants.CROSS);
      m_actualLogger.assertEquals(m_expectedLogger);
    }
    // drag over "ShellEditPart"
    {
      m_sender.dragTo(100, 100);
      //
      m_actualLogger.assertEmpty();
    }
    // drag over "ButtonEditPart_NORTH_ResizeHandle"
    {
      m_sender.dragTo(220, 120);
      //
      m_actualLogger.assertEmpty();
    }
    // end drag
    {
      m_sender.endDrag();
      m_sender.setStateMask(SWT.NONE);
      //
      m_expectedLogger.setCursor(null);
      m_actualLogger.assertEquals(m_expectedLogger);
    }
    //
    m_viewer.select(m_buttonEditPart);
    //
    // move to "ButtonEditPart_NORTH_ResizeHandle"
    {
      m_sender.moveTo(220, 120);
      //
      m_expectedLogger.setCursor(ICursorConstants.SIZEN);
      m_actualLogger.assertEquals(m_expectedLogger);
    }
    // use tracker
    {
      m_sender.setStateMask(SWT.ALT);
      m_sender.startDrag(220, 120, 1);
      //
      m_expectedLogger.setCursor(ICursorConstants.CROSS);
      m_actualLogger.assertEquals(m_expectedLogger);
    }
    // drag
    {
      m_sender.dragTo(230, 130);
      //
      m_actualLogger.assertEmpty();
    }
    // invalid input
    {
      m_sender.startDrag(230, 130, 2);
      //
      m_expectedLogger.setCursor(ICursorConstants.NO);
      m_actualLogger.assertEquals(m_expectedLogger);
    }
    // drag
    {
      m_sender.dragTo(235, 135);
      //
      m_actualLogger.assertEmpty();
    }
    // end drag
    {
      m_sender.endDrag();
      m_sender.setStateMask(SWT.NONE);
      //
      m_expectedLogger.setCursor(null);
      m_actualLogger.assertEquals(m_expectedLogger);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Test inner classes
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final class ResizeCommand extends Command {
    private GraphicalEditPart m_editPart;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Access
    //
    ////////////////////////////////////////////////////////////////////////////
    public void setPart(GraphicalEditPart editPart) {
      m_editPart = editPart;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Command
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public void execute() throws Exception {
      m_editPart.getFigure().setBounds(new Rectangle(10, 10, 60, 60));
    }
  }
}