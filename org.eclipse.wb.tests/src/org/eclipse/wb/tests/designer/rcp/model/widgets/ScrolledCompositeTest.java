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
package org.eclipse.wb.tests.designer.rcp.model.widgets;

import org.eclipse.wb.internal.rcp.model.widgets.ScrolledCompositeInfo;
import org.eclipse.wb.internal.swt.model.layout.FillLayoutInfo;
import org.eclipse.wb.internal.swt.model.layout.RowLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.rcp.BTestUtils;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for {@link ScrolledCompositeInfo}.
 * 
 * @author scheglov_ke
 */
public class ScrolledCompositeTest extends RcpModelTest {
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
  public void test_noContent() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    ScrolledComposite composite = new ScrolledComposite(this, SWT.NONE);",
            "  }",
            "}");
    shell.refresh();
    ScrolledCompositeInfo composite = (ScrolledCompositeInfo) shell.getChildrenControls().get(0);
    // no "setContent()"
    assertNull(composite.getContent());
    // no child Control, so OK
    assertTrue(composite.hasRequired_setContent());
  }

  public void test_withContent() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  private Button button;",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    ScrolledComposite composite = new ScrolledComposite(this, SWT.NONE);",
            "    {",
            "      button = new Button(composite, SWT.NONE);",
            "    }",
            "    composite.setContent(button);",
            "  }",
            "}");
    shell.refresh();
    ScrolledCompositeInfo composite = (ScrolledCompositeInfo) shell.getChildrenControls().get(0);
    ControlInfo button = composite.getChildrenControls().get(0);
    // has "setContent()" with "button"
    assertSame(button, composite.getContent());
    // no child Control and "setContent()", so OK
    assertTrue(composite.hasRequired_setContent());
  }

  public void test_hasChildControl_withContent() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  private Button button;",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    ScrolledComposite composite = new ScrolledComposite(this, SWT.NONE);",
            "    {",
            "      button = new Button(composite, SWT.NONE);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    ScrolledCompositeInfo composite = (ScrolledCompositeInfo) shell.getChildrenControls().get(0);
    assertThat(composite.getChildrenControls()).hasSize(1);
    // no "setContent()"
    assertNull(composite.getContent());
    // no child Control, but no "setContent()", so BAD
    assertFalse(composite.hasRequired_setContent());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ScrolledCompositeInfo#command_CREATE(ControlInfo)}.
   */
  public void test_CREATE() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    ScrolledComposite composite = new ScrolledComposite(this, SWT.NONE);",
            "  }",
            "}");
    shell.refresh();
    ScrolledCompositeInfo composite = (ScrolledCompositeInfo) shell.getChildrenControls().get(0);
    //
    ControlInfo button = BTestUtils.createButton();
    composite.command_CREATE(button);
    assertEditor(
        "public class Test extends Shell {",
        "  private Button button;",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    ScrolledComposite composite = new ScrolledComposite(this, SWT.NONE);",
        "    {",
        "      button = new Button(composite, SWT.NONE);",
        "    }",
        "    composite.setContent(button);",
        "    composite.setMinSize(button.computeSize(SWT.DEFAULT, SWT.DEFAULT));",
        "  }",
        "}");
  }

  /**
   * When we drop {@link ControlInfo} on {@link CompositeInfo} inside of
   * {@link ScrolledCompositeInfo}, it should be added <em>before</em> <code>setContent()</code> or
   * <code>setMinSize()</code> invocations.
   */
  public void test_CREATE2() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  private Group group;",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    ScrolledComposite composite = new ScrolledComposite(this, SWT.NONE);",
            "    {",
            "      group = new Group(composite, SWT.NONE);",
            "      group.setLayout(new RowLayout());",
            "    }",
            "    composite.setContent(group);",
            "    composite.setMinSize(group.computeSize(SWT.DEFAULT, SWT.DEFAULT));",
            "  }",
            "}");
    shell.refresh();
    ScrolledCompositeInfo composite = (ScrolledCompositeInfo) shell.getChildrenControls().get(0);
    CompositeInfo group = (CompositeInfo) composite.getContent();
    //
    ControlInfo button = BTestUtils.createButton();
    ((RowLayoutInfo) group.getLayout()).command_CREATE(button, null);
    assertEditor(
        "public class Test extends Shell {",
        "  private Group group;",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    ScrolledComposite composite = new ScrolledComposite(this, SWT.NONE);",
        "    {",
        "      group = new Group(composite, SWT.NONE);",
        "      group.setLayout(new RowLayout());",
        "      {",
        "        Button button = new Button(group, SWT.NONE);",
        "      }",
        "    }",
        "    composite.setContent(group);",
        "    composite.setMinSize(group.computeSize(SWT.DEFAULT, SWT.DEFAULT));",
        "  }",
        "}");
  }

  /**
   * Test for {@link ScrolledCompositeInfo#command_ADD(ControlInfo)}.
   */
  public void test_ADD() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    ScrolledComposite composite = new ScrolledComposite(this, SWT.NONE);",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    ScrolledCompositeInfo composite = (ScrolledCompositeInfo) shell.getChildrenControls().get(0);
    ControlInfo button = shell.getChildrenControls().get(1);
    //
    composite.command_ADD(button);
    assertEditor(
        "public class Test extends Shell {",
        "  private Button button;",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    ScrolledComposite composite = new ScrolledComposite(this, SWT.NONE);",
        "    {",
        "      button = new Button(composite, SWT.NONE);",
        "    }",
        "    composite.setContent(button);",
        "    composite.setMinSize(button.computeSize(SWT.DEFAULT, SWT.DEFAULT));",
        "  }",
        "}");
  }

  /**
   * Test for moving {@link ControlInfo} from {@link ScrolledCompositeInfo#command_ADD(ControlInfo)}
   * .
   */
  public void test_MOVE_out() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  private Button button;",
            "  public Test() {",
            "    setLayout(new FillLayout());",
            "    ScrolledComposite composite = new ScrolledComposite(this, SWT.NONE);",
            "    {",
            "      button = new Button(composite, SWT.NONE);",
            "    }",
            "    composite.setContent(button);",
            "    composite.setMinSize(button.computeSize(SWT.DEFAULT, SWT.DEFAULT));",
            "  }",
            "}");
    shell.refresh();
    ScrolledCompositeInfo composite = (ScrolledCompositeInfo) shell.getChildrenControls().get(0);
    ControlInfo button = composite.getContent();
    //
    ((FillLayoutInfo) shell.getLayout()).command_MOVE(button, null);
    assertEditor(
        "public class Test extends Shell {",
        "  private Button button;",
        "  public Test() {",
        "    setLayout(new FillLayout());",
        "    ScrolledComposite composite = new ScrolledComposite(this, SWT.NONE);",
        "    {",
        "      button = new Button(this, SWT.NONE);",
        "    }",
        "  }",
        "}");
  }
}