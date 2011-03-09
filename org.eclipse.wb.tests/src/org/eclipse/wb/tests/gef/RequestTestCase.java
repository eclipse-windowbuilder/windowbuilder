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
import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.internal.gef.core.EditDomain;
import org.eclipse.wb.internal.gef.graphical.GraphicalViewer;

import org.eclipse.swt.widgets.Shell;

/**
 * @author lobas_av
 * 
 */
public class RequestTestCase extends GefTestCase {
  protected Shell m_shell;
  protected EditDomain m_domain;
  protected GraphicalViewer m_viewer;
  protected EventSender m_sender;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public RequestTestCase(Class<?> _class) {
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
    //
    m_shell = new Shell();
    // create domain
    m_domain = new EditDomain() {
      @Override
      public Tool getDefaultTool() {
        return null;
      }
    };
    // create viewer
    m_viewer = new GraphicalViewer(m_shell);
    m_viewer.getControl().setSize(500, 400);
    m_viewer.setEditDomain(m_domain);
    // create sender
    m_sender = new EventSender(m_viewer.getControl());
  }

  @Override
  protected void tearDown() throws Exception {
    m_shell.dispose();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return new {@link EditPart} added as child of given <code>parentEditPart</code>.
   */
  protected static final RequestTestCaseEditPart addEditPart(EditPart parentEditPart,
      String name,
      RequestsLogger actualLogger,
      int x,
      int y,
      int w,
      int h) throws Exception {
    RequestTestCaseEditPart editPart = new RequestTestCaseEditPart(name, actualLogger);
    editPart.getFigure().setBounds(new Rectangle(x, y, w, h));
    addChildEditPart(parentEditPart, editPart);
    return editPart;
  }

  /**
   * Asserts that given {@link RequestsLogger}'s contain same sequence of events.
   */
  protected static final void assertLoggers(RequestsLogger expectedLogger,
      RequestsLogger actualLogger) {
    actualLogger.assertEquals(expectedLogger);
    actualLogger.clear();
    expectedLogger.clear();
  }
}