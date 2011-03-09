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
package org.eclipse.wb.tests.designer.ercp.model.widgets.mobile;

import org.eclipse.wb.internal.ercp.model.widgets.mobile.CaptionedControlInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.tests.designer.ercp.ErcpModelTest;

/**
 * Tests for {@link CaptionedControlInfo}.
 * 
 * @author scheglov_ke
 */
public class CaptionedControlTest extends ErcpModelTest {
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
  public void test_0() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "class Test extends Shell {",
            "  public Test() {",
            "    CaptionedControl captionedControl = new CaptionedControl(this, SWT.NONE);",
            "  }",
            "}");
    shell.refresh();
    //
    CaptionedControlInfo captionedControl =
        (CaptionedControlInfo) shell.getChildrenControls().get(0);
    assertTrue(captionedControl.shouldDrawDotsBorder());
  }
}