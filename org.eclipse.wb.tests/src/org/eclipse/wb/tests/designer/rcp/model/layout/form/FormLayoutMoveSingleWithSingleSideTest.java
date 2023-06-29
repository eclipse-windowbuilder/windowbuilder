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

import org.eclipse.wb.internal.core.gef.policy.snapping.PlacementInfo;
import org.eclipse.wb.internal.swt.model.layout.form.FormLayoutInfo;
import org.eclipse.wb.internal.swt.model.layout.form.FormLayoutInfoImplAutomatic;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.draw2d.geometry.Rectangle;

/**
 * Tests for {@link FormLayoutInfoImplAutomatic}.
 *
 * @author mitin_aa
 */
public class FormLayoutMoveSingleWithSingleSideTest extends RcpModelTest {
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
	 * Freely moving single component in trailing with changing alignment, attached to component.
	 */
	public void test_move_to_trailing_change_alignment_2component() throws Exception {
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
						"        button1.setLayoutData(data1);",
						"      }",
						"    }",
						"    {",
						"      button2 = new Button(this, SWT.NONE);",
						"      {",
						"        FormData data2 = new FormData();",
						"        data2.left = new FormAttachment(0, 200);",
						"        button2.setLayoutData(data2);",
						"      }",
						"    }",
						"  }",
						"}");
		shell.refresh();
		ControlInfo button1 = shell.getChildrenControls().get(0);
		int x = 200 - button1.getModelBounds().width;
		moveTo(shell, button1, x - 30, PlacementInfo.TRAILING);
		assertEditor(
				"public class Test extends Shell {",
				"  private Button button1;",
				"  private Button button2;",
				"  private FormData data1;",
				"  public Test() {",
				"    setLayout(new FormLayout());",
				"    {",
				"      button1 = new Button(this, SWT.NONE);",
				"      {",
				"        data1 = new FormData();",
				"        button1.setLayoutData(data1);",
				"      }",
				"    }",
				"    {",
				"      button2 = new Button(this, SWT.NONE);",
				"      data1.right = new FormAttachment(button2, -30);",
				"      {",
				"        FormData data2 = new FormData();",
				"        data2.left = new FormAttachment(0, 200);",
				"        button2.setLayoutData(data2);",
				"      }",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Freely moving single component in leading with changing alignment, attached to component
	 */
	public void test_move_to_leading_change_alignment_2component() throws Exception {
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
						"        button1.setLayoutData(data1);",
						"      }",
						"    }",
						"    {",
						"      button2 = new Button(this, SWT.NONE);",
						"      {",
						"        FormData data2 = new FormData();",
						"        data2.right = new FormAttachment(100, -20);",
						"        button2.setLayoutData(data2);",
						"      }",
						"    }",
						"  }",
						"}");
		shell.refresh();
		ControlInfo button2 = shell.getChildrenControls().get(1);
		moveTo(shell, button2, 110, PlacementInfo.LEADING);
		assertEditor(
				"public class Test extends Shell {",
				"	private Button button1;",
				"	private Button button2;",
				"	public Test() {",
				"		setLayout(new FormLayout());",
				"		{",
				"			button1 = new Button(this, SWT.NONE);",
				"			{",
				"				FormData data1 = new FormData();",
				"				data1.left = new FormAttachment(0, 20);",
				"				button1.setLayoutData(data1);",
				"			}",
				"		}",
				"		{",
				"			button2 = new Button(this, SWT.NONE);",
				"			{",
				"				FormData data2 = new FormData();",
				"				data2.left = new FormAttachment(button1, 15);",
				"				button2.setLayoutData(data2);",
				"			}",
				"		}",
				"	}",
				"}");
	}

	/**
	 * Freely moving single component in trailing without changing alignment, attached to component.
	 */
	public void test_move_to_trailing_keep_alignment_2component() throws Exception {
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
						"        data1.right = new FormAttachment(100, -320);",
						"        button1.setLayoutData(data1);",
						"      }",
						"    }",
						"    {",
						"      button2 = new Button(this, SWT.NONE);",
						"      {",
						"        FormData data2 = new FormData();",
						"        data2.right = new FormAttachment(100, -20);",
						"        button2.setLayoutData(data2);",
						"      }",
						"    }",
						"  }",
						"}");
		shell.refresh();
		ControlInfo button1 = shell.getChildrenControls().get(0);
		ControlInfo button2 = shell.getChildrenControls().get(1);
		int x = button2.getModelBounds().x - button1.getModelBounds().width;
		moveTo(shell, button1, x - 20, PlacementInfo.TRAILING);
		assertEditor(
				"public class Test extends Shell {",
				"  private Button button1;",
				"  private Button button2;",
				"  private FormData data1;",
				"  public Test() {",
				"    setLayout(new FormLayout());",
				"    {",
				"      button1 = new Button(this, SWT.NONE);",
				"      {",
				"        data1 = new FormData();",
				"        button1.setLayoutData(data1);",
				"      }",
				"    }",
				"    {",
				"      button2 = new Button(this, SWT.NONE);",
				"      data1.right = new FormAttachment(button2, -20);",
				"      {",
				"        FormData data2 = new FormData();",
				"        data2.right = new FormAttachment(100, -20);",
				"        button2.setLayoutData(data2);",
				"      }",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Freely moving single component in leading without changing alignment, attached to component.
	 */
	public void test_move_to_leading_keep_alignment_2component() throws Exception {
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
						"        button1.setLayoutData(data1);",
						"      }",
						"    }",
						"    {",
						"      button2 = new Button(this, SWT.NONE);",
						"      {",
						"        FormData data2 = new FormData();",
						"        data2.left = new FormAttachment(0, 200);",
						"        button2.setLayoutData(data2);",
						"      }",
						"    }",
						"  }",
						"}");
		shell.refresh();
		ControlInfo button2 = shell.getChildrenControls().get(1);
		moveTo(shell, button2, 120, PlacementInfo.LEADING);
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
				"        button1.setLayoutData(data1);",
				"      }",
				"    }",
				"    {",
				"      button2 = new Button(this, SWT.NONE);",
				"      {",
				"        FormData data2 = new FormData();",
				"        data2.left = new FormAttachment(button1, 25);",
				"        button2.setLayoutData(data2);",
				"      }",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Freely moving single component in trailing with no initial constraints, attached to another
	 * component.
	 */
	public void test_move_to_trailing_no_constraints_2component() throws Exception {
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
						"        button1.setLayoutData(data1);",
						"      }",
						"    }",
						"    {",
						"      button2 = new Button(this, SWT.NONE);",
						"      {",
						"        FormData data2 = new FormData();",
						"        data2.left = new FormAttachment(0, 200);",
						"        button2.setLayoutData(data2);",
						"      }",
						"    }",
						"  }",
						"}");
		shell.refresh();
		ControlInfo button1 = shell.getChildrenControls().get(0);
		moveTo(shell, button1, 100, PlacementInfo.TRAILING);
		assertEditor(
				"public class Test extends Shell {",
				"  private Button button1;",
				"  private Button button2;",
				"  private FormData data1;",
				"  public Test() {",
				"    setLayout(new FormLayout());",
				"    {",
				"      button1 = new Button(this, SWT.NONE);",
				"      {",
				"        data1 = new FormData();",
				"        button1.setLayoutData(data1);",
				"      }",
				"    }",
				"    {",
				"      button2 = new Button(this, SWT.NONE);",
				"      data1.right = new FormAttachment(button2, -25);",
				"      {",
				"        FormData data2 = new FormData();",
				"        data2.left = new FormAttachment(0, 200);",
				"        button2.setLayoutData(data2);",
				"      }",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Freely moving single component in trailing with no initial constraints.
	 */
	public void test_move_to_trailing_no_constraints() throws Exception {
		prepareComponent();
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new FormLayout());",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      {",
						"        FormData formData = new FormData();",
						"        button.setLayoutData(formData);",
						"      }",
						"    }",
						"  }",
						"}");
		shell.refresh();
		ControlInfo button = getJavaInfoByName("button");
		//
		moveTo(shell, button, 50, PlacementInfo.TRAILING);
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new FormLayout());",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      {",
				"        FormData formData = new FormData();",
				"        formData.left = new FormAttachment(0, 50);",
				"        button.setLayoutData(formData);",
				"      }",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Freely moving single component in trailing with changing alignment.
	 */
	public void DISABLE_test_move_to_trailing_change_alignment() throws Exception {
		prepareComponent();
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new FormLayout());",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      {",
						"        FormData formData = new FormData();",
						"        formData.left = new FormAttachment(0, 20);",
						"        button.setLayoutData(formData);",
						"      }",
						"    }",
						"  }",
						"}");
		refresh();
		ControlInfo button = getJavaInfoByName("button");
		//
		int shellWidth = shell.getClientArea().width;
		int x = shellWidth - button.getModelBounds().width;
		moveTo(shell, button, x - 30, PlacementInfo.TRAILING);
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new FormLayout());",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      {",
				"        FormData formData = new FormData();",
				"        formData.right = new FormAttachment(100, -30);",
				"        button.setLayoutData(formData);",
				"      }",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Freely moving single component in leading with changing alignment.
	 */
	public void test_move_to_leading_change_alignment() throws Exception {
		prepareComponent();
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new FormLayout());",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      {",
						"        FormData formData = new FormData();",
						"        formData.right = new FormAttachment(100, -10);",
						"        button.setLayoutData(formData);",
						"      }",
						"    }",
						"  }",
						"}");
		shell.refresh();
		ControlInfo button = getJavaInfoByName("button");
		//
		moveTo(shell, button, 10, PlacementInfo.LEADING);
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new FormLayout());",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      {",
				"        FormData formData = new FormData();",
				"        formData.left = new FormAttachment(0, 10);",
				"        button.setLayoutData(formData);",
				"      }",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Freely moving single component in trailing without changing alignment.
	 */
	public void DISABLE_test_move_to_trailing_keep_alignment() throws Exception {
		prepareComponent();
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new FormLayout());",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      {",
						"        FormData formData = new FormData();",
						"        formData.right = new FormAttachment(100, -60);",
						"        button.setLayoutData(formData);",
						"      }",
						"    }",
						"  }",
						"}");
		shell.refresh();
		ControlInfo button = getJavaInfoByName("button");
		//
		int shellWidth = shell.getClientArea().width;
		int x = shellWidth - button.getModelBounds().width;
		moveTo(shell, button, x - 10, PlacementInfo.TRAILING);
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new FormLayout());",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      {",
				"        FormData formData = new FormData();",
				"        formData.right = new FormAttachment(100, -10);",
				"        button.setLayoutData(formData);",
				"      }",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Freely moving single component in leading without changing alignment.
	 */
	public void test_move_to_leading_keep_alignment() throws Exception {
		prepareComponent();
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new FormLayout());",
						"    {",
						"      Button button = new Button(this, SWT.NONE);",
						"      {",
						"        FormData formData = new FormData();",
						"        formData.left = new FormAttachment(0, 60);",
						"        button.setLayoutData(formData);",
						"      }",
						"    }",
						"  }",
						"}");
		shell.refresh();
		ControlInfo button = getJavaInfoByName("button");
		//
		moveTo(shell, button, 10, PlacementInfo.LEADING);
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new FormLayout());",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      {",
				"        FormData formData = new FormData();",
				"        formData.left = new FormAttachment(0, 10);",
				"        button.setLayoutData(formData);",
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