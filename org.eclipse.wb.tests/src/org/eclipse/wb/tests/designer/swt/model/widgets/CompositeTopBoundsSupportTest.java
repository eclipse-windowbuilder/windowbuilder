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

import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeTopBoundsSupport;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

/**
 * Test for {@link CompositeTopBoundsSupport}.
 * 
 * @author scheglov_ke
 */
public class CompositeTopBoundsSupportTest extends RcpModelTest {
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
  // setSize()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_setSize_Composite_noSizeInvocations() throws Exception {
    CompositeInfo composite =
        parseComposite(
            "public class Test extends Composite {",
            "  public Test(Composite parent, int style) {",
            "    super(parent, style);",
            "  }",
            "}");
    composite.refresh();
    // default size
    assertEquals(new Dimension(450, 300), composite.getBounds().getSize());
    // set new size
    composite.getTopBoundsSupport().setSize(500, 400);
    composite.refresh();
    assertEquals(new Dimension(500, 400), composite.getBounds().getSize());
    assertEditor(
        "public class Test extends Composite {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "  }",
        "}");
  }

  public void test_setSize_Composite_setSize_Point() throws Exception {
    CompositeInfo composite =
        parseComposite(
            "public class Test extends Composite {",
            "  public Test(Composite parent, int style) {",
            "    super(parent, style);",
            "    setSize(new Point(250, 200));",
            "  }",
            "}");
    composite.refresh();
    // default size
    assertEquals(new Dimension(250, 200), composite.getBounds().getSize());
    // set new size
    composite.getTopBoundsSupport().setSize(500, 400);
    composite.refresh();
    assertEquals(new Dimension(500, 400), composite.getBounds().getSize());
    assertEditor(
        "public class Test extends Composite {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    setSize(new Point(500, 400));",
        "  }",
        "}");
  }

  public void test_setSize_Composite_setSize_ints() throws Exception {
    CompositeInfo composite =
        parseComposite(
            "public class Test extends Composite {",
            "  public Test(Composite parent, int style) {",
            "    super(parent, style);",
            "    setSize(250, 200);",
            "  }",
            "}");
    composite.refresh();
    // default size
    assertEquals(new Dimension(250, 200), composite.getBounds().getSize());
    // set new size
    composite.getTopBoundsSupport().setSize(500, 400);
    composite.refresh();
    assertEquals(new Dimension(500, 400), composite.getBounds().getSize());
    assertEditor(
        "public class Test extends Composite {",
        "  public Test(Composite parent, int style) {",
        "    super(parent, style);",
        "    setSize(500, 400);",
        "  }",
        "}");
  }

  public void test_setSize_Shell_noSizeInvocations() throws Exception {
    CompositeInfo composite =
        parseComposite(
            "// filler filler filler",
            "public class Test extends Shell {",
            "  public Test() {",
            "  }",
            "}");
    composite.refresh();
    // default size
    assertEquals(new Dimension(450, 300), composite.getBounds().getSize());
    // set new size
    composite.getTopBoundsSupport().setSize(500, 400);
    composite.refresh();
    assertEquals(new Dimension(500, 400), composite.getBounds().getSize());
    assertEditor(
        "// filler filler filler",
        "public class Test extends Shell {",
        "  public Test() {",
        "    setSize(500, 400);",
        "  }",
        "}");
  }
}