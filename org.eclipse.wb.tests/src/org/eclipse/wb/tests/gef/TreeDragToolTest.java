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

import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.graphical.tools.SelectionTool;
import org.eclipse.wb.gef.tree.TreeEditPart;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.ui.UiUtils;

import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;

/**
 * @author lobas_av
 *
 */
public class TreeDragToolTest extends TreeToolTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // SetUp
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    // set SelectionTool
    m_domain.setActiveTool(new SelectionTool());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Special
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_DoubleClick() throws Exception {
    RequestsLogger actualLogger = new RequestsLogger();
    //
    ILayoutEditPolicy ipolicy = new ILayoutEditPolicy() {
      public boolean isGoodReferenceChild(Request request, EditPart editPart) {
        return true;
      }
    };
    TreeEditPart parent = addEditPart(m_viewer.getRootEditPart(), "parent", actualLogger, ipolicy);
    TreeEditPart child1 = addEditPart(parent, "child1", actualLogger, ipolicy);
    //
    refreshTreeParst(parent);
    UiUtils.expandAll(m_viewer.getTree());
    //
    Point location = getOnLocation(parent);
    m_sender.doubleClick(location.x, location.y, 3);
    actualLogger.assertEmpty();
    //
    RequestsLogger expectedLogger = new RequestsLogger();
    //
    m_sender.doubleClick(location.x, location.y, 1);
    expectedLogger.log(parent, "performRequest[ SelectionRequest(type=open, location=Point("
        + location.x
        + ", "
        + location.y
        + "), stateMask=0, button=0) ]");
    assertLoggers(expectedLogger, actualLogger);
    //
    location = getOnLocation(child1);
    m_sender.doubleClick(location.x, location.y, 1);
    expectedLogger.log(child1, "performRequest[ SelectionRequest(type=open, location=Point("
        + +location.x
        + ", "
        + location.y
        + "), stateMask=0, button=0) ]");
    assertLoggers(expectedLogger, actualLogger);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Test
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_Drag_1() throws Exception {
    RequestsLogger actualLogger = new RequestsLogger();
    //
    ILayoutEditPolicy ipolicy = new ILayoutEditPolicy() {
      public boolean isGoodReferenceChild(Request request, EditPart editPart) {
        return true;
      }
    };
    //
    TreeEditPart parent = addEditPart(m_viewer.getRootEditPart(), "parent", actualLogger, ipolicy);
    TreeEditPart child1 = addEditPart(parent, "child1", actualLogger, ipolicy);
    TreeEditPart child2 = addEditPart(parent, "child2", actualLogger, ipolicy);
    TreeEditPart child3 = addEditPart(parent, "child3", actualLogger, ipolicy);
    //
    refreshTreeParst(parent);
    UiUtils.expandAll(m_viewer.getTree());
    //
    m_viewer.select(child3);
    //
    DropTarget dropTarget = (DropTarget) m_viewer.getTree().getData("DropTarget");
    //
    RequestsLogger expectedLogger = new RequestsLogger();
    //
    {
      Event event = createDNDEvent(child3, getOnLocation(child3));
      dropTarget.notifyListeners(DND.DragEnter, event);
      actualLogger.assertEmpty();
    }
    //------------------------------------------------------------------------------------
    {
      Event event = createDNDEvent(child3, getAfterLocation(child2));
      dropTarget.notifyListeners(DND.DragOver, event);
      expectedLogger.log(parent, "getMoveCommand(parts=[child3], next=child3)");
      assertLoggers(expectedLogger, actualLogger);
    }
    //
    {
      Event event = createDNDEvent(child3, getOnLocation(child2));
      dropTarget.notifyListeners(DND.DragOver, event);
      expectedLogger.log(child2, "getAddCommand(parts=[child3], next=null)");
      assertLoggers(expectedLogger, actualLogger);
    }
    //
    {
      Event event = createDNDEvent(child3, getBeforeLocation(child2));
      dropTarget.notifyListeners(DND.DragOver, event);
      expectedLogger.log(parent, "getMoveCommand(parts=[child3], next=child2)");
      assertLoggers(expectedLogger, actualLogger);
    }
    //------------------------------------------------------------------------------------
    {
      Event event = createDNDEvent(child3, getAfterLocation(child1));
      dropTarget.notifyListeners(DND.DragOver, event);
      expectedLogger.log(parent, "getMoveCommand(parts=[child3], next=child2)");
      assertLoggers(expectedLogger, actualLogger);
    }
    //
    {
      Event event = createDNDEvent(child3, getOnLocation(child1));
      dropTarget.notifyListeners(DND.DragOver, event);
      expectedLogger.log(child1, "getAddCommand(parts=[child3], next=null)");
      assertLoggers(expectedLogger, actualLogger);
    }
    //
    {
      Event event = createDNDEvent(child3, getBeforeLocation(child1));
      dropTarget.notifyListeners(DND.DragOver, event);
      expectedLogger.log(parent, "getMoveCommand(parts=[child3], next=child1)");
      assertLoggers(expectedLogger, actualLogger);
    }
    //------------------------------------------------------------------------------------
    {
      Event event = createDNDEvent(child3, getAfterLocation(parent));
      dropTarget.notifyListeners(DND.DragOver, event);
      actualLogger.assertEmpty();
    }
    //
    {
      Event event = createDNDEvent(child3, getOnLocation(parent));
      dropTarget.notifyListeners(DND.DragOver, event);
      expectedLogger.log(parent, "getMoveCommand(parts=[child3], next=null)");
      assertLoggers(expectedLogger, actualLogger);
    }
    //
    {
      Event event = createDNDEvent(child3, getBeforeLocation(parent));
      dropTarget.notifyListeners(DND.DragOver, event);
      actualLogger.assertEmpty();
    }
    //------------------------------------------------------------------------------------
    {
      Event event = createDNDEvent(child3, getAfterLocation(child3));
      dropTarget.notifyListeners(DND.DragOver, event);
      expectedLogger.log(parent, "getMoveCommand(parts=[child3], next=null)");
      assertLoggers(expectedLogger, actualLogger);
    }
    //
    {
      Event event = createDNDEvent(child3, getBeforeLocation(child3));
      dropTarget.notifyListeners(DND.DragOver, event);
      expectedLogger.log(parent, "getMoveCommand(parts=[child3], next=child3)");
      assertLoggers(expectedLogger, actualLogger);
    }
  }

  public void test_Drag_2() throws Exception {
    RequestsLogger actualLogger = new RequestsLogger();
    //
    ILayoutEditPolicy ipolicy = new ILayoutEditPolicy() {
      public boolean isGoodReferenceChild(Request request, EditPart editPart) {
        return true;
      }
    };
    //
    TreeEditPart parent = addEditPart(m_viewer.getRootEditPart(), "parent", actualLogger, ipolicy);
    TreeEditPart child1 = addEditPart(parent, "child1", actualLogger, null);
    addEditPart(parent, "child2", actualLogger, ipolicy);
    TreeEditPart child3 = addEditPart(parent, "child3", actualLogger, ipolicy);
    //
    refreshTreeParst(parent);
    UiUtils.expandAll(m_viewer.getTree());
    //
    m_viewer.select(child3);
    //
    DropTarget dropTarget = (DropTarget) m_viewer.getTree().getData("DropTarget");
    //
    RequestsLogger expectedLogger = new RequestsLogger();
    //
    {
      Event event = createDNDEvent(child3, getOnLocation(child3));
      dropTarget.notifyListeners(DND.DragEnter, event);
      actualLogger.assertEmpty();
    }
    //
    {
      Event event = createDNDEvent(child3, getAfterLocation(child1));
      dropTarget.notifyListeners(DND.DragOver, event);
      expectedLogger.log(parent, "getMoveCommand(parts=[child3], next=child2)");
      assertLoggers(expectedLogger, actualLogger);
    }
    //
    {
      Event event = createDNDEvent(child3, getOnLocation(child1));
      dropTarget.notifyListeners(DND.DragOver, event);
      expectedLogger.log(parent, "getMoveCommand(parts=[child3], next=child2)");
      assertLoggers(expectedLogger, actualLogger);
    }
    //
    {
      Event event = createDNDEvent(child3, getBeforeLocation(child1));
      dropTarget.notifyListeners(DND.DragOver, event);
      expectedLogger.log(parent, "getMoveCommand(parts=[child3], next=child1)");
      assertLoggers(expectedLogger, actualLogger);
    }
  }

  public void test_Drag_3() throws Exception {
    RequestsLogger actualLogger = new RequestsLogger();
    //
    ILayoutEditPolicy ipolicy = new ILayoutEditPolicy() {
      public boolean isGoodReferenceChild(Request request, EditPart editPart) {
        return true;
      }
    };
    //
    TreeEditPart parent =
        addEditPart(m_viewer.getRootEditPart(), "parent", actualLogger, new ILayoutEditPolicy() {
          public boolean isGoodReferenceChild(Request request, EditPart editPart) {
            return !"child1".equals(editPart.getModel());
          }
        });
    TreeEditPart child1 = addEditPart(parent, "child1", actualLogger, ipolicy);
    addEditPart(parent, "child2", actualLogger, ipolicy);
    TreeEditPart child3 = addEditPart(parent, "child3", actualLogger, ipolicy);
    //
    refreshTreeParst(parent);
    UiUtils.expandAll(m_viewer.getTree());
    //
    m_viewer.select(child3);
    //
    DropTarget dropTarget = (DropTarget) m_viewer.getTree().getData("DropTarget");
    //
    RequestsLogger expectedLogger = new RequestsLogger();
    //
    {
      Event event = createDNDEvent(child3, getOnLocation(child3));
      dropTarget.notifyListeners(DND.DragEnter, event);
      actualLogger.assertEmpty();
    }
    //
    {
      Event event = createDNDEvent(child3, getAfterLocation(child1));
      dropTarget.notifyListeners(DND.DragOver, event);
      actualLogger.assertEmpty();
    }
    //
    {
      Event event = createDNDEvent(child3, getOnLocation(child1));
      dropTarget.notifyListeners(DND.DragOver, event);
      expectedLogger.log(child1, "getAddCommand(parts=[child3], next=null)");
      assertLoggers(expectedLogger, actualLogger);
    }
    //
    {
      Event event = createDNDEvent(child3, getBeforeLocation(child1));
      dropTarget.notifyListeners(DND.DragOver, event);
      actualLogger.assertEmpty();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private Event createDNDEvent(TreeEditPart dragPart, Point dropLocation) throws Exception {
    // create DNDEvent
    Class<?> dndClass =
        ReflectionUtils.getClassByName(getClass().getClassLoader(), "org.eclipse.swt.dnd.DNDEvent");
    Event event =
        (Event) ReflectionUtils.getConstructorBySignature(dndClass, "<init>()").newInstance();
    // configure event
    event.widget = m_viewer.getControl();
    event.item = dragPart.getWidget();
    org.eclipse.swt.graphics.Point p =
        Display.getCurrent().map(m_viewer.getControl(), null, dropLocation.getSwtPoint());
    event.x = p.x;
    event.y = p.y;
    return event;
  }
}