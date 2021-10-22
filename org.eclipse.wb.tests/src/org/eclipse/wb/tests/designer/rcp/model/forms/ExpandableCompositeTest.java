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
package org.eclipse.wb.tests.designer.rcp.model.forms;

import org.eclipse.wb.internal.rcp.model.forms.ExpandableCompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.rcp.BTestUtils;

/**
 * Test for {@link ExpandableCompositeInfo}.
 *
 * @author scheglov_ke
 */
public class ExpandableCompositeTest extends AbstractFormsTest {
  public void test_properties() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    ExpandableComposite composite = new ExpandableComposite(this, SWT.BORDER, ExpandableComposite.TREE_NODE);",
            "  }",
            "}");
    shell.refresh();
    ExpandableCompositeInfo composite =
        (ExpandableCompositeInfo) shell.getChildrenControls().get(0);
    assertNotNull(composite.getPropertyByTitle("Style"));
    assertNotNull(composite.getPropertyByTitle("ExpansionStyle"));
  }

  /**
   * Test for {@link ExpandableComposite_Info#command_CREATE(ControlInfo, String), String)}.<br>
   * When drop using <code>setTextClient()</code>, just create happens, "expanded" property is not
   * changed.
   */
  public void test_CREATE_setTextClient() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    ExpandableComposite composite = new ExpandableComposite(this, SWT.BORDER);",
            "  }",
            "}");
    shell.refresh();
    ExpandableCompositeInfo composite =
        (ExpandableCompositeInfo) shell.getChildrenControls().get(0);
    // when
    ControlInfo button = BTestUtils.createButton();
    composite.command_CREATE(button, "setTextClient");
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new RowLayout());",
        "    ExpandableComposite composite = new ExpandableComposite(this, SWT.BORDER);",
        "    {",
        "      Button button = new Button(composite, SWT.NONE);",
        "      composite.setTextClient(button);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link ExpandableCompositeInfo#command_CREATE(ControlInfo, String)}.<br>
   * When drop using <code>setClient()</code>, "expanded" property should be set to
   * <code>true</code>.
   */
  public void test_CREATE_setClient() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    ExpandableComposite composite = new ExpandableComposite(this, SWT.BORDER);",
            "  }",
            "}");
    shell.refresh();
    ExpandableCompositeInfo composite =
        (ExpandableCompositeInfo) shell.getChildrenControls().get(0);
    // when
    ControlInfo button = BTestUtils.createButton();
    composite.command_CREATE(button, "setClient");
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new RowLayout());",
        "    ExpandableComposite composite = new ExpandableComposite(this, SWT.BORDER);",
        "    composite.setExpanded(true);",
        "    {",
        "      Button button = new Button(composite, SWT.NONE);",
        "      composite.setClient(button);",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for {@link ExpandableCompositeInfo#command_MOVE(ControlInfo, String)}.<br>
   * When drop using <code>setClient()</code>, "expanded" property should be set to
   * <code>true</code>.
   */
  public void test_MOVE_setClient() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new RowLayout());",
            "    ExpandableComposite composite = new ExpandableComposite(this, SWT.BORDER);",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    ExpandableCompositeInfo composite =
        (ExpandableCompositeInfo) shell.getChildrenControls().get(0);
    ControlInfo button = shell.getChildrenControls().get(1);
    // when
    composite.command_MOVE(button, "setClient");
    assertEditor(
        "public class Test extends Shell {",
        "  public Test() {",
        "    setLayout(new RowLayout());",
        "    ExpandableComposite composite = new ExpandableComposite(this, SWT.BORDER);",
        "    composite.setExpanded(true);",
        "    {",
        "      Button button = new Button(composite, SWT.NONE);",
        "      composite.setClient(button);",
        "    }",
        "  }",
        "}");
  }
}