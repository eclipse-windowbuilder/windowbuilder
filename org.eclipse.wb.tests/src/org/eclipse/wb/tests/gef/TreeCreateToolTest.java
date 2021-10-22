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
import org.eclipse.wb.gef.core.requests.ICreationFactory;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.core.tools.CreationTool;
import org.eclipse.wb.gef.tree.TreeEditPart;
import org.eclipse.wb.internal.core.utils.ui.UiUtils;

/**
 * @author lobas_av
 *
 */
public class TreeCreateToolTest extends TreeToolTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // SetUp
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    // create test factory
    ICreationFactory factory = new ICreationFactory() {
      public void activate() {
      }

      public Object getNewObject() {
        return "_NewObject_";
      }

      @Override
      public String toString() {
        return "TestFactory";
      }
    };
    // set CreationTool
    m_domain.setActiveTool(new CreationTool(factory));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_Move_1() throws Exception {
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
    TreeEditPart parent1 = addEditPart(parent, "parent1", actualLogger, ipolicy);
    TreeEditPart parent2 = addEditPart(parent, "parent2", actualLogger, ipolicy);
    //
    refreshTreeParst(parent);
    UiUtils.expandAll(m_viewer.getTree());
    //
    RequestsLogger expectedLogger = new RequestsLogger();
    //
    // move outside of any EditPart
    {
      m_sender.moveTo(470, 370);
      actualLogger.assertEmpty();
    }
    //----------------------------------------------------------------------------------
    {
      Point dropLocation = getBeforeLocation(parent);
      m_sender.moveTo(dropLocation.x, dropLocation.y);
      actualLogger.assertEmpty();
    }
    //
    {
      Point dropLocation = getOnLocation(parent);
      m_sender.moveTo(dropLocation.x, dropLocation.y);
      expectedLogger.log(parent, "getCreateCommand(object=_NewObject_, next=null)");
      assertLoggers(expectedLogger, actualLogger);
    }
    //
    {
      Point dropLocation = getAfterLocation(parent);
      m_sender.moveTo(dropLocation.x, dropLocation.y);
      actualLogger.assertEmpty();
    }
    //----------------------------------------------------------------------------------
    {
      Point dropLocation = getBeforeLocation(child1);
      m_sender.moveTo(dropLocation.x, dropLocation.y);
      expectedLogger.log(parent, "getCreateCommand(object=_NewObject_, next=child1)");
      assertLoggers(expectedLogger, actualLogger);
    }
    //
    {
      Point dropLocation = getOnLocation(child1);
      m_sender.moveTo(dropLocation.x, dropLocation.y);
      expectedLogger.log(child1, "getCreateCommand(object=_NewObject_, next=null)");
      assertLoggers(expectedLogger, actualLogger);
    }
    //
    {
      Point dropLocation = getAfterLocation(child1);
      m_sender.moveTo(dropLocation.x, dropLocation.y);
      expectedLogger.log(parent, "getCreateCommand(object=_NewObject_, next=parent1)");
      assertLoggers(expectedLogger, actualLogger);
    }
    //----------------------------------------------------------------------------------
    {
      Point dropLocation = getBeforeLocation(parent1);
      m_sender.moveTo(dropLocation.x, dropLocation.y);
      expectedLogger.log(parent, "getCreateCommand(object=_NewObject_, next=parent1)");
      assertLoggers(expectedLogger, actualLogger);
    }
    //
    {
      Point dropLocation = getOnLocation(parent1);
      m_sender.moveTo(dropLocation.x, dropLocation.y);
      expectedLogger.log(parent1, "getCreateCommand(object=_NewObject_, next=null)");
      assertLoggers(expectedLogger, actualLogger);
    }
    //
    {
      Point dropLocation = getAfterLocation(parent1);
      m_sender.moveTo(dropLocation.x, dropLocation.y);
      expectedLogger.log(parent, "getCreateCommand(object=_NewObject_, next=parent2)");
      assertLoggers(expectedLogger, actualLogger);
    }
    //----------------------------------------------------------------------------------
    {
      Point dropLocation = getAfterLocation(parent2);
      m_sender.moveTo(dropLocation.x, dropLocation.y);
      expectedLogger.log(parent, "getCreateCommand(object=_NewObject_, next=null)");
      assertLoggers(expectedLogger, actualLogger);
    }
  }

  public void test_Move_2() throws Exception {
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
    addEditPart(parent, "parent1", actualLogger, ipolicy);
    //
    refreshTreeParst(parent);
    UiUtils.expandAll(m_viewer.getTree());
    //
    RequestsLogger expectedLogger = new RequestsLogger();
    //
    // move outside of any EditPart
    {
      m_sender.moveTo(470, 370);
      actualLogger.assertEmpty();
    }
    //
    {
      Point dropLocation = getBeforeLocation(child1);
      m_sender.moveTo(dropLocation.x, dropLocation.y);
      expectedLogger.log(parent, "getCreateCommand(object=_NewObject_, next=child1)");
      assertLoggers(expectedLogger, actualLogger);
    }
    //
    {
      Point dropLocation = getOnLocation(child1);
      m_sender.moveTo(dropLocation.x, dropLocation.y);
      expectedLogger.log(parent, "getCreateCommand(object=_NewObject_, next=parent1)");
      assertLoggers(expectedLogger, actualLogger);
    }
    //
    {
      Point dropLocation = getAfterLocation(child1);
      m_sender.moveTo(dropLocation.x, dropLocation.y);
      expectedLogger.log(parent, "getCreateCommand(object=_NewObject_, next=parent1)");
      assertLoggers(expectedLogger, actualLogger);
    }
  }

  public void test_Move_3() throws Exception {
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
    addEditPart(parent, "parent1", actualLogger, ipolicy);
    //
    refreshTreeParst(parent);
    UiUtils.expandAll(m_viewer.getTree());
    //
    RequestsLogger expectedLogger = new RequestsLogger();
    //
    // move outside of any EditPart
    {
      m_sender.moveTo(470, 370);
      actualLogger.assertEmpty();
    }
    //
    {
      Point dropLocation = getBeforeLocation(child1);
      m_sender.moveTo(dropLocation.x, dropLocation.y);
      actualLogger.assertEmpty();
    }
    //
    {
      Point dropLocation = getOnLocation(child1);
      m_sender.moveTo(dropLocation.x, dropLocation.y);
      expectedLogger.log(child1, "getCreateCommand(object=_NewObject_, next=null)");
      assertLoggers(expectedLogger, actualLogger);
    }
    //
    {
      Point dropLocation = getAfterLocation(child1);
      m_sender.moveTo(dropLocation.x, dropLocation.y);
      actualLogger.assertEmpty();
    }
  }
}