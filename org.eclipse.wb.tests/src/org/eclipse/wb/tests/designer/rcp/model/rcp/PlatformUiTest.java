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
package org.eclipse.wb.tests.designer.rcp.model.rcp;

import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.rcp.model.jface.ApplicationWindowInfo;
import org.eclipse.wb.internal.rcp.model.jface.action.ActionContainerInfo;
import org.eclipse.wb.internal.rcp.model.jface.action.ActionInfo;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

/**
 * Tests for things related with {@link PlatformUI}.
 * 
 * @author scheglov_ke
 */
public class PlatformUiTest extends RcpModelTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Exit zone :-) XXX
  //
  ////////////////////////////////////////////////////////////////////////////
  public void _test_exit() throws Exception {
    System.exit(0);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * The <code>HelpContentsAction</code> is internal and uses {@link IWorkbench} that is not
   * available because we don't run separate Eclipse for each Designer editor.
   * <p>
   * We return "fake" implementation of this class from "resources/bytecode".
   */
  public void test_HelpContentsAction() throws Exception {
    ApplicationWindowInfo window =
        parseJavaInfo(
            "import org.eclipse.jface.action.*;",
            "import org.eclipse.jface.window.*;",
            "public class Test extends ApplicationWindow {",
            "  private IAction m_action;",
            "  public Test(Shell parentShell) {",
            "    super(parentShell);",
            "    createActions();",
            "  }",
            "  private void createActions() {",
            "    {",
            "      m_action = new org.eclipse.ui.internal.actions.HelpContentsAction();",
            "    }",
            "  }",
            "}");
    window.refresh();
    assertNoErrors(window);
    // test HelpContentsAction object properties
    ActionInfo action = ActionContainerInfo.getActions(window).get(0);
    Object actionObject = action.getObject();
    assertEquals("&Help Contents", ReflectionUtils.invokeMethod(actionObject, "getText()"));
    assertEquals("Help Contents", ReflectionUtils.invokeMethod(actionObject, "getToolTipText()"));
    assertNotNull(ReflectionUtils.invokeMethod(actionObject, "getImageDescriptor()"));
  }
}