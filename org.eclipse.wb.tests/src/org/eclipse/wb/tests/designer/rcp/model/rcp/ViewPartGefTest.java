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

import org.eclipse.wb.internal.rcp.model.rcp.ViewPartInfo;
import org.eclipse.wb.tests.designer.rcp.RcpGefTest;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.part.ViewPart;

/**
 * Test for {@link ViewPartInfo} in GEF.
 *
 * @author scheglov_ke
 */
public class ViewPartGefTest extends RcpGefTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * There was bug that new {@link ViewPart} with {@link IMenuManager} can not be opened. This was
   * caused by adding {@link IMenuManager} as popup menu, and because of other case we decided that
   * popup should be on separate layer. So, this caused problems with count of children on content
   * figure.
   */
  public void DISABLE_test_withMenuManager() throws Exception {
    openJavaInfo(
        "import org.eclipse.jface.action.*;",
        "import org.eclipse.ui.*;",
        "import org.eclipse.ui.part.*;",
        "public class Test extends ViewPart {",
        "  public Test() {",
        "  }",
        "  public void createPartControl(Composite parent) {",
        "    Composite container = new Composite(parent, SWT.NULL);",
        "  }",
        "  public void setFocus() {",
        "  }",
        "  public void init(IViewSite site) throws PartInitException {",
        "    super.init(site);",
        "    createActions();",
        "    initializeMenu();",
        "  }",
        "  private void createActions() {",
        "  }",
        "  private void initializeMenu() {",
        "    IMenuManager menuManager = getViewSite().getActionBars().getMenuManager();",
        "  }",
        "}");
  }
}
