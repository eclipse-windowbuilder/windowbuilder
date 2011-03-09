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
package org.eclipse.wb.tests.designer.XML.editor;

import org.eclipse.wb.internal.core.xml.editor.actions.TestAction;
import org.eclipse.wb.tests.designer.XWT.gef.XwtGefTest;
import org.eclipse.wb.tests.gef.EventSender;
import org.eclipse.wb.tests.gef.UIRunnable;
import org.eclipse.wb.tests.gef.UiContext;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

/**
 * Test for {@link TestAction}.
 * 
 * @author scheglov_ke
 */
public class TestActionTest extends XwtGefTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test that {@link TestAction} shows {@link Shell} with valid bounds.
   */
  public void test_run() throws Exception {
    openEditor("<Shell text='My Shell'/>");
    //
    new UiContext().executeAndCheck(new UIRunnable() {
      public void run(UiContext context) throws Exception {
        m_designPageActions.getTestAction().run();
      }
    }, new UIRunnable() {
      public void run(UiContext context) throws Exception {
        Shell shell = context.useShell("My Shell");
        {
          assertEquals(450, shell.getSize().x);
          assertEquals(300, shell.getSize().y);
        }
        new EventSender(shell).keyDown(SWT.ESC);
      }
    });
  }
}
