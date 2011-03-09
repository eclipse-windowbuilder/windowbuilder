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
package org.eclipse.wb.tests.designer.rcp.model.layout.form;

import org.eclipse.wb.internal.core.gef.policy.snapping.PlacementInfo;
import org.eclipse.wb.internal.swt.model.layout.form.FormLayoutInfo;
import org.eclipse.wb.internal.swt.model.layout.form.FormLayoutInfoImplAutomatic;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

/**
 * Tests for {@link FormLayoutInfoImplAutomatic}.
 * 
 * @author mitin_aa
 */
public class FormLayoutAlignmentDetectionTest extends RcpModelTest {
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
  // Alignment
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getAlignment_single_noConstraints() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FormLayout());",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      FormData data = new FormData();",
            "      button.setLayoutData(data);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    ControlInfo button = shell.getChildrenControls().get(0);
    assertEquals(PlacementInfo.LEADING, getImpl(shell).getAlignment(button, true).alignment);
  }

  public void test_getAlignment_single_left() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FormLayout());",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      FormData data = new FormData();",
            "      data.left = new FormAttachment(0, 50);",
            "      button.setLayoutData(data);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    ControlInfo button = shell.getChildrenControls().get(0);
    assertEquals(PlacementInfo.LEADING, getImpl(shell).getAlignment(button, true).alignment);
  }

  public void test_getAlignment_single_right() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FormLayout());",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      FormData data = new FormData();",
            "      data.right = new FormAttachment(100, -50);",
            "      button.setLayoutData(data);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    ControlInfo button = shell.getChildrenControls().get(0);
    assertEquals(PlacementInfo.TRAILING, getImpl(shell).getAlignment(button, true).alignment);
  }

  public void test_getAlignment_single_left_as_trailing() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FormLayout());",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      FormData data = new FormData();",
            "      data.left = new FormAttachment(100, -50);",
            "      button.setLayoutData(data);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    ControlInfo button = shell.getChildrenControls().get(0);
    assertEquals(PlacementInfo.TRAILING, getImpl(shell).getAlignment(button, true).alignment);
  }

  public void test_getAlignment_single_forBothSidesAttached_right() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FormLayout());",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      FormData data = new FormData();",
            "      data.right = new FormAttachment(100, -50);",
            "      data.left = new FormAttachment(100, -150);",
            "      button.setLayoutData(data);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    ControlInfo button = shell.getChildrenControls().get(0);
    assertEquals(PlacementInfo.TRAILING, getImpl(shell).getAlignment(button, true).alignment);
  }

  public void test_getAlignment_single_forBothSidesAttached_left() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FormLayout());",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      FormData data = new FormData();",
            "      data.left = new FormAttachment(0, 50);",
            "      data.right = new FormAttachment(0, 150);",
            "      button.setLayoutData(data);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    ControlInfo button = shell.getChildrenControls().get(0);
    assertEquals(PlacementInfo.LEADING, getImpl(shell).getAlignment(button, true).alignment);
  }

  public void test_getAlignment_single_forBothSidesAttached_left_and_right() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FormLayout());",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      FormData data = new FormData();",
            "      data.left = new FormAttachment(0, 50);",
            "      data.right = new FormAttachment(100, -150);",
            "      button.setLayoutData(data);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    ControlInfo button = shell.getChildrenControls().get(0);
    assertTrue(getImpl(shell).getAlignment(button, true).resizable);
  }

  public void test_getAlignment_single_forBothSidesAttached_left_and_right_numerators()
      throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FormLayout());",
            "    {",
            "      Button button = new Button(this, SWT.NONE);",
            "      FormData data = new FormData();",
            "      data.left = new FormAttachment(50, 0);",
            "      data.right = new FormAttachment(100, -150);",
            "      button.setLayoutData(data);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    ControlInfo button = shell.getChildrenControls().get(0);
    assertTrue(getImpl(shell).getAlignment(button, true).resizable);
  }

  public void test_getAlignment_complex_forBothSidesAttached_left_and_right() throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FormLayout());",
            "    Button button1 = new Button(this, SWT.NONE);",
            "    button1.setText(\"Button 1\");",
            "    Button button2 = new Button(this, SWT.NONE);",
            "    button2.setText(\"Button 2\");",
            "    {",
            "      FormData data = new FormData();",
            "      data.left = new FormAttachment(0, 40);",
            "      button1.setLayoutData(data);",
            "    }",
            "    {",
            "      FormData data = new FormData();",
            "      data.left = new FormAttachment(button1, 20);",
            "      data.right = new FormAttachment(100, -120);",
            "      button2.setLayoutData(data);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    ControlInfo button = shell.getChildrenControls().get(1);
    assertTrue(getImpl(shell).getAlignment(button, true).resizable);
  }

  public void test_getAlignment_complex_forBothSidesAttached_left_and_right_indirect()
      throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FormLayout());",
            "    Button button1 = new Button(this, SWT.NONE);",
            "    button1.setText(\"Button 1\");",
            "    Button button2 = new Button(this, SWT.NONE);",
            "    button2.setText(\"Button 2\");",
            "    {",
            "      FormData data = new FormData();",
            "      data.left = new FormAttachment(0, 50);",
            "      data.right = new FormAttachment(100, -150);",
            "      button1.setLayoutData(data);",
            "    }",
            "    {",
            "      FormData data = new FormData();",
            "      data.left = new FormAttachment(button1, 10, SWT.LEFT);",
            "      data.right = new FormAttachment(button1, 0, SWT.RIGHT);",
            "      button2.setLayoutData(data);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    ControlInfo button1 = shell.getChildrenControls().get(1);
    ControlInfo button2 = shell.getChildrenControls().get(1);
    assertTrue(getImpl(shell).getAlignment(button1, true).resizable);
    assertTrue(getImpl(shell).getAlignment(button2, true).resizable);
  }

  public void test_getAlignment_complex_forBothSidesAttached_notResizeable_indirect()
      throws Exception {
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  public Test() {",
            "    setLayout(new FormLayout());",
            "    Button button1 = new Button(this, SWT.NONE);",
            "    button1.setText(\"Button 1\");",
            "    Button button2 = new Button(this, SWT.NONE);",
            "    button2.setText(\"Button 2\");",
            "    {",
            "      FormData data = new FormData();",
            "      data.left = new FormAttachment(0, 50);",
            "      data.right = new FormAttachment(0, 150);",
            "      button1.setLayoutData(data);",
            "    }",
            "    {",
            "      FormData data = new FormData();",
            "      data.left = new FormAttachment(button1, 10, SWT.LEFT);",
            "      data.right = new FormAttachment(button1, 0, SWT.RIGHT);",
            "      button2.setLayoutData(data);",
            "    }",
            "  }",
            "}");
    shell.refresh();
    ControlInfo button1 = shell.getChildrenControls().get(1);
    ControlInfo button2 = shell.getChildrenControls().get(1);
    assertEquals(PlacementInfo.LEADING, getImpl(shell).getAlignment(button1, true).alignment);
    assertEquals(PlacementInfo.LEADING, getImpl(shell).getAlignment(button2, true).alignment);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Helpers
  //
  ////////////////////////////////////////////////////////////////////////////	
  private FormLayoutInfoImplAutomatic<ControlInfo> getImpl(CompositeInfo shell) {
    FormLayoutInfo layout = (FormLayoutInfo) shell.getLayout();
    return (FormLayoutInfoImplAutomatic<ControlInfo>) layout.getImpl();
  }
}