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

import com.google.common.collect.ImmutableList;

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.gef.policy.snapping.PlacementInfo;
import org.eclipse.wb.internal.swt.model.layout.form.FormLayoutInfo;
import org.eclipse.wb.internal.swt.model.layout.form.FormLayoutInfoImplAutomatic;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.Expectations;
import org.eclipse.wb.tests.designer.Expectations.IntValue;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

/**
 * Tests for {@link FormLayoutInfoImplAutomatic}.
 *
 * @author mitin_aa
 */
public class FormLayoutMoveSingleResizableTest extends RcpModelTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Exit zone :-) XXX
  //
  ////////////////////////////////////////////////////////////////////////////
  public void _test_exit() throws Exception {
    System.exit(0);
  }

  /**
   * Freely moving single resizable component with attachment into different target sides, attached
   * to component.
   */
  public void DISABLE_test_move_to_leading_1_2component() throws Exception {
    prepareComponent();
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  private Button button1;",
            "  private Button button2;",
            "  public Test() {",
            "    setLayout(new FormLayout());",
            "    {",
            "      button1 = new Button(this, SWT.NONE);",
            "      {",
            "        FormData data1 = new FormData();",
            "        data1.left = new FormAttachment(0, 20);",
            "        data1.right = new FormAttachment(0, 70);",
            "        button1.setLayoutData(data1);",
            "      }",
            "    }",
            "    {",
            "      button2 = new Button(this, SWT.NONE);",
            "      {",
            "        FormData data2 = new FormData();",
            "        data2.left = new FormAttachment(0, 100);",
            "        data2.right = new FormAttachment(100, -250);",
            "        button2.setLayoutData(data2);",
            "      }",
            "    }",
            "  }",
            "}");
    shell.refresh();
    ControlInfo button2 = shell.getChildrenControls().get(1);
    moveTo(shell, button2, 90, PlacementInfo.LEADING);
    assertEditor(
        "public class Test extends Shell {",
        "  private Button button1;",
        "  private Button button2;",
        "  public Test() {",
        "    setLayout(new FormLayout());",
        "    {",
        "      button1 = new Button(this, SWT.NONE);",
        "      {",
        "        FormData data1 = new FormData();",
        "        data1.left = new FormAttachment(0, 20);",
        "        data1.right = new FormAttachment(0, 70);",
        "        button1.setLayoutData(data1);",
        "      }",
        "    }",
        "    {",
        "      button2 = new Button(this, SWT.NONE);",
        "      {",
        "        FormData data2 = new FormData();",
        "        data2.left = new FormAttachment(button1, 20);",
        "        data2.right = new FormAttachment(100, -260);",
        "        button2.setLayoutData(data2);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  /**
   * Freely moving single resizable component with attachment into reverse-different target sides,
   * attached to compoennt.
   */
  public void test_move_to_leading_2_2component() throws Exception {
    prepareComponent();
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  private Button button1;",
            "  private Button button2;",
            "  public Test() {",
            "    setLayout(new FormLayout());",
            "    {",
            "      button1 = new Button(this, SWT.NONE);",
            "      {",
            "        FormData data1 = new FormData();",
            "        data1.left = new FormAttachment(0, 20);",
            "        data1.right = new FormAttachment(0, 70);",
            "        button1.setLayoutData(data1);",
            "      }",
            "    }",
            "    {",
            "      button2 = new Button(this, SWT.NONE);",
            "      {",
            "        FormData data2 = new FormData();",
            "        data2.left = new FormAttachment(0, 100);",
            "        data2.right = new FormAttachment(0, 150);",
            "        button2.setLayoutData(data2);",
            "      }",
            "    }",
            "  }",
            "}");
    shell.refresh();
    ControlInfo button1 = shell.getChildrenControls().get(0);
    moveTo(shell, button1, 30, PlacementInfo.TRAILING);
    assertEditor(
        "public class Test extends Shell {",
        "	private Button button1;",
        "	private Button button2;",
        "	private FormData data1;",
        "	public Test() {",
        "		setLayout(new FormLayout());",
        "		{",
        "			button1 = new Button(this, SWT.NONE);",
        "			{",
        "				data1 = new FormData();",
        "				button1.setLayoutData(data1);",
        "			}",
        "		}",
        "		{",
        "			button2 = new Button(this, SWT.NONE);",
        "			data1.right = new FormAttachment(button2, -20);",
        "			data1.left = new FormAttachment(button2, -70, SWT.LEFT);",
        "			{",
        "				FormData data2 = new FormData();",
        "				data2.left = new FormAttachment(0, 100);",
        "				data2.right = new FormAttachment(0, 150);",
        "				button2.setLayoutData(data2);",
        "			}",
        "		}",
        "	}",
        "}");
  }

  /**
   * Freely moving single resizable component with attachment into different target sides.
   */
  public void DISABLE_test_move_to_leading_1() throws Exception {
    prepareComponent();
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  private Button button1;",
            "  public Test() {",
            "    setLayout(new FormLayout());",
            "    {",
            "      button1 = new Button(this, SWT.NONE);",
            "      {",
            "        FormData data1 = new FormData();",
            "        data1.left = new FormAttachment(0, 120);",
            "        data1.right = new FormAttachment(100, -70);",
            "        button1.setLayoutData(data1);",
            "      }",
            "    }",
            "  }",
            "}");
    shell.refresh();
    ControlInfo button1 = shell.getChildrenControls().get(0);
    moveTo(shell, button1, 100, PlacementInfo.LEADING);
    assertEditor(
        "public class Test extends Shell {",
        "  private Button button1;",
        "  public Test() {",
        "    setLayout(new FormLayout());",
        "    {",
        "      button1 = new Button(this, SWT.NONE);",
        "      {",
        "        FormData data1 = new FormData();",
        "        data1.left = new FormAttachment(0, 100);",
        "        data1.right = new FormAttachment(100, -90);",
        "        button1.setLayoutData(data1);",
        "      }",
        "    }",
        "  }",
        "}");
  }

  /**
   * Freely moving single resizable component with attachment into reverse-different target sides.
   */
  public void DISABLE_test_move_to_leading_2() throws Exception {
    prepareComponent();
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  private Button button1;",
            "  public Test() {",
            "    setLayout(new FormLayout());",
            "    {",
            "      button1 = new Button(this, SWT.NONE);",
            "      {",
            "        FormData data1 = new FormData();",
            "        data1.left = new FormAttachment(100, -120);",
            "        data1.right = new FormAttachment(0, 370);",
            "        button1.setLayoutData(data1);",
            "      }",
            "    }",
            "  }",
            "}");
    shell.refresh();
    ControlInfo button1 = shell.getChildrenControls().get(0);
    moveTo(shell, button1, 100, PlacementInfo.LEADING);
    int expectedLeft =
        Expectations.get(0, new IntValue[]{
            new IntValue("scheglov-win", -334),
            new IntValue("mitin-aa-mac", -350),
            new IntValue("flanker-windows", -350),});
    int expectedRight =
        Expectations.get(0, new IntValue[]{
            new IntValue("scheglov-win", 156),
            new IntValue("mitin-aa-mac", 140),
            new IntValue("flanker-windows", 140),});
    assertEditor(
        "public class Test extends Shell {",
        "  private Button button1;",
        "  public Test() {",
        "    setLayout(new FormLayout());",
        "    {",
        "      button1 = new Button(this, SWT.NONE);",
        "      {",
        "        FormData data1 = new FormData();",
        "        data1.left = new FormAttachment(100, " + expectedLeft + ");",
        "        data1.right = new FormAttachment(0, " + expectedRight + ");",
        "        button1.setLayoutData(data1);",
        "      }",
        "    }",
        "  }",
        "}");
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

  private void moveTo(CompositeInfo shell, ControlInfo control, int x, int direction)
      throws Exception {
    FormLayoutInfoImplAutomatic<ControlInfo> impl = getImpl(shell);
    Rectangle controlBounds = control.getModelBounds();
    impl.command_moveFreely(
        new Rectangle(x, 0, controlBounds.width, controlBounds.height),
        ImmutableList.of(control),
        control,
        direction,
        true);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Custom Button
  //
  ////////////////////////////////////////////////////////////////////////////
  private void prepareComponent() throws Exception {
    prepareComponent(75, 25);
  }

  private void prepareComponent(int width, int height) throws Exception {
    setFileContentSrc(
        "test/Button.java",
        getTestSource(
            "public class Button extends org.eclipse.swt.widgets.Button {",
            "  public Button(Composite parent, int style) {",
            "    super(parent, style);",
            "  }",
            "  protected void checkSubclass () {",
            "  }",
            "  public Point computeSize (int wHint, int hHint, boolean changed) {",
            "    return new Point(" + width + ", " + height + ");",
            "  }",
            "}"));
    waitForAutoBuild();
  }
}