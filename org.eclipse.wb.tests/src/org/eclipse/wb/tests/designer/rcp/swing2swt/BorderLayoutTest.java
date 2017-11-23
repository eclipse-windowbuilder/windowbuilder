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
package org.eclipse.wb.tests.designer.rcp.swing2swt;

import org.eclipse.wb.internal.core.model.generic.FlowContainerFactory;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.rcp.swing2swt.layout.BorderLayoutInfo;
import org.eclipse.wb.internal.swt.model.layout.LayoutDataInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.rcp.BTestUtils;

import org.eclipse.swt.widgets.Control;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test {@link BorderLayoutInfo}.
 * 
 * @author scheglov_ke
 */
public class BorderLayoutTest extends AbstractSwing2SwtTest {
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
  public void test_parse() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "import swing2swt.layout.BorderLayout;",
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new BorderLayout(0, 0));",
            "  }",
            "}");
    shell.refresh();
    assertHierarchy(
        "{this: org.eclipse.swt.widgets.Shell} {this} {/setLayout(new BorderLayout(0, 0))/}",
        "  {new: swing2swt.layout.BorderLayout} {empty} {/setLayout(new BorderLayout(0, 0))/}");
    BorderLayoutInfo layout = (BorderLayoutInfo) shell.getLayout();
    // BorderLayout is "flow container" only for tree
    assertThat(new FlowContainerFactory(layout, true).get()).isEmpty();
    assertThat(new FlowContainerFactory(layout, false).get()).isNotEmpty();
  }

  /**
   * Test for {@link BorderLayoutInfo#getControl(String)}.
   */
  public void test_getControl() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "import swing2swt.layout.BorderLayout;",
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new BorderLayout(0, 0));",
            "    {",
            "      Button button_1 = new Button(this, SWT.NONE);",
            "      button_1.setLayoutData(BorderLayout.NORTH);",
            "    }",
            "    {",
            "      Button button_2 = new Button(this, SWT.NONE);",
            "      button_2.setLayoutData(BorderLayout.WEST);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    BorderLayoutInfo layout = (BorderLayoutInfo) shell.getLayout();
    ControlInfo button_1 = shell.getChildrenControls().get(0);
    ControlInfo button_2 = shell.getChildrenControls().get(1);
    //
    assertSame(button_1, layout.getControl("NORTH"));
    assertSame(button_2, layout.getControl("WEST"));
    assertSame(null, layout.getControl("SOUTH"));
    assertSame(null, layout.getControl("EAST"));
    assertSame(null, layout.getControl("CENTER"));
  }

  /**
   * When we delete {@link BorderLayoutInfo} we should also remove manually
   * {@link Control#setLayoutData(Object)} invocations, because there are no real
   * {@link LayoutDataInfo} to remove (which does this automatically).
   */
  public void test_whenLayoutDelete_setLayoutData() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "import swing2swt.layout.BorderLayout;",
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new BorderLayout(0, 0));",
            "    {",
            "      Button button_1 = new Button(this, SWT.NONE);",
            "      button_1.setLayoutData(BorderLayout.NORTH);",
            "    }",
            "    {",
            "      Button button_2 = new Button(this, SWT.NONE);",
            "      button_2.setLayoutData(BorderLayout.WEST);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    BorderLayoutInfo layout = (BorderLayoutInfo) shell.getLayout();
    //
    layout.delete();
    assertEditor(
        "import swing2swt.layout.BorderLayout;",
        "public class Test extends Shell {",
        "  public Test() {",
        "    {",
        "      Button button_1 = new Button(this, SWT.NONE);",
        "    }",
        "    {",
        "      Button button_2 = new Button(this, SWT.NONE);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CREATE
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link BorderLayoutInfo#command_CREATE(ControlInfo, String)}.
   */
  public void test_CREATE() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "import swing2swt.layout.BorderLayout;",
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new BorderLayout(0, 0));",
            "  }",
            "}");
    shell.refresh();
    BorderLayoutInfo layout = (BorderLayoutInfo) shell.getLayout();
    //
    ControlInfo newButton = BTestUtils.createButton();
    layout.command_CREATE(newButton, "NORTH");
    assertEditor(
        "import swing2swt.layout.BorderLayout;",
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new BorderLayout(0, 0));",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setLayoutData(BorderLayout.NORTH);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // MOVE
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link BorderLayoutInfo#command_MOVE(ControlInfo, String)}.
   */
  public void test_MOVE_setRegion() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "import swing2swt.layout.BorderLayout;",
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new BorderLayout(0, 0));",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setLayoutData(BorderLayout.NORTH);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    BorderLayoutInfo layout = (BorderLayoutInfo) shell.getLayout();
    ControlInfo button = shell.getChildrenControls().get(0);
    //
    layout.command_MOVE(button, "WEST");
    assertEditor(
        "import swing2swt.layout.BorderLayout;",
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new BorderLayout(0, 0));",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setLayoutData(BorderLayout.WEST);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link BorderLayoutInfo#command_MOVE(ControlInfo, String)}.
   */
  public void test_MOVE_withReparent() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "import swing2swt.layout.BorderLayout;",
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new BorderLayout(0, 0));",
            "    {",
            "      Button button_1 = new Button(this, SWT.NONE);",
            "      button_1.setLayoutData(BorderLayout.NORTH);",
            "    }",
            "    {",
            "      Composite composite = new Composite(this, SWT.NONE);",
            "      {",
            "        Button button_2 = new Button(composite, SWT.NONE);",
            "      }",
            "    }",
            "  }",
            "}");
    shell.refresh();
    BorderLayoutInfo layout = (BorderLayoutInfo) shell.getLayout();
    CompositeInfo composite = (CompositeInfo) shell.getChildrenControls().get(1);
    ControlInfo button_2 = composite.getChildrenControls().get(0);
    //
    layout.command_MOVE(button_2, "WEST");
    assertEditor(
        "import swing2swt.layout.BorderLayout;",
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new BorderLayout(0, 0));",
        "    {",
        "      Button button_1 = new Button(this, SWT.NONE);",
        "      button_1.setLayoutData(BorderLayout.NORTH);",
        "    }",
        "    {",
        "      Composite composite = new Composite(this, SWT.NONE);",
        "    }",
        "    {",
        "      Button button_2 = new Button(this, SWT.NONE);",
        "      button_2.setLayoutData(BorderLayout.WEST);",
        "    }",
        "  }",
        "}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // "Region" property
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_RegionProperty() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "import swing2swt.layout.BorderLayout;",
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new BorderLayout(0, 0));",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      button.setLayoutData(BorderLayout.NORTH);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    BorderLayoutInfo layout = (BorderLayoutInfo) shell.getLayout();
    ControlInfo button = shell.getChildrenControls().get(0);
    Property property = button.getPropertyByTitle("Region");
    // same property each time
    assertSame(property, button.getPropertyByTitle("Region"));
    // initial value
    assertTrue(property.isModified());
    assertEquals("North", property.getValue());
    // set "West"
    property.setValue("West");
    layout.command_MOVE(button, "WEST");
    assertEditor(
        "import swing2swt.layout.BorderLayout;",
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new BorderLayout(0, 0));",
        "    {",
        "      Button button = new Button(this, SWT.NONE);",
        "      button.setLayoutData(BorderLayout.WEST);",
        "    }",
        "  }",
        "}");
  }

  public void test_RegionProperty_noValue() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "import swing2swt.layout.BorderLayout;",
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new BorderLayout(0, 0));",
            "    {",
            "      Button button_1 = new Button(this, SWT.NONE);",
            "    }",
            "    {",
            "      Button button_2 = new Button(this, SWT.NONE);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    ControlInfo button_1 = shell.getChildrenControls().get(0);
    ControlInfo button_2 = shell.getChildrenControls().get(1);
    // "button_2" has value for "Region"
    {
      Property property = button_2.getPropertyByTitle("Region");
      assertTrue(property.isModified());
      assertEquals("Center", property.getValue());
    }
    // "button_1" has no value for "Region"
    {
      Property property = button_1.getPropertyByTitle("Region");
      assertTrue(property.isModified());
      assertEquals("", property.getValue());
    }
  }
}