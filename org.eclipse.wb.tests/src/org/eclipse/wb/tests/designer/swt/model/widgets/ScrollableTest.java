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
package org.eclipse.wb.tests.designer.swt.model.widgets;

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ScrollableInfo;
import org.eclipse.wb.tests.designer.Expectations;
import org.eclipse.wb.tests.designer.Expectations.IntValue;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

/**
 * Test for {@link ScrollableInfo}.
 * 
 * @author scheglov_ke
 */
public class ScrollableTest extends RcpModelTest {
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
  public void test_getClientArea_Composite() throws Exception {
    CompositeInfo composite =
        parseComposite(
            "public class Test extends Composite {",
            "  public Test(Composite parent, int style) {",
            "    super(parent, style);",
            "  }",
            "}");
    composite.refresh();
    //
    assertEquals(new Rectangle(0, 0, 450, 300), composite.getClientArea());
  }

  public void test_getClientArea_Shell() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "// filler filler filler",
            "public class Test extends Shell {",
            "  public Test() {",
            "  }",
            "}");
    shell.refresh();
    //
    int shellBorder =
        Expectations.get(8, new IntValue[]{
            new IntValue("Windows Vista", 8),
            new IntValue("Windows XP", 4),
            new IntValue("Linux", 1)});
    int shellTitle =
        Expectations.get(30, new IntValue[]{
            new IntValue("flanker-linux", 31),
            new IntValue("flanker-windows", 30),
            new IntValue("scheglov-win", 30)});
    assertEquals(new Rectangle(0, 0, 450 - shellBorder - shellBorder, 300
        - shellTitle
        - shellBorder), shell.getClientArea());
  }
}