/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.designer.swt.model.widgets;

import org.eclipse.wb.internal.swt.model.layout.FillLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.ButtonInfo;
import org.eclipse.wb.internal.swt.model.widgets.ButtonStylePresentation;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.swt.widgets.Button;

import org.junit.Test;

/**
 * Tests for {@link ButtonInfo}.
 *
 * @author lobas_av
 */
public class ButtonsTest extends RcpModelTest {
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
	// Creation with different "creationId"
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_addButton_1() throws Exception {
		check_addButton(new String[]{
				"class Test {",
				"  public static void main(String[] args) {",
				"    Shell shell = new Shell();",
				"    shell.setSize(320, 240);",
				"    shell.setLayout(new FillLayout());",
				"  }",
		"}"}, new String[]{
				"class Test {",
				"  public static void main(String[] args) {",
				"    Shell shell = new Shell();",
				"    shell.setSize(320, 240);",
				"    shell.setLayout(new FillLayout());",
				"    {",
				"      Button button = new Button(shell, SWT.NONE);",
				"      button.setText(\"New Button\");",
				"    }",
				"  }",
		"}"}, null);
	}

	@Test
	public void test_addButton_2() throws Exception {
		check_addButton(new String[]{
				"class Test {",
				"  public static void main(String[] args) {",
				"    Shell shell = new Shell();",
				"    shell.setSize(320, 240);",
				"    shell.setLayout(new FillLayout());",
				"  }",
		"}"}, new String[]{
				"class Test {",
				"  public static void main(String[] args) {",
				"    Shell shell = new Shell();",
				"    shell.setSize(320, 240);",
				"    shell.setLayout(new FillLayout());",
				"    {",
				"      Button button = new Button(shell, SWT.NONE);",
				"    }",
				"  }",
		"}"}, "no-such-creationId");
	}

	@Test
	public void test_addCheckButton() throws Exception {
		check_addButton(new String[]{
				"class Test {",
				"  public static void main(String[] args) {",
				"    Shell shell = new Shell();",
				"    shell.setSize(320, 240);",
				"    shell.setLayout(new FillLayout());",
				"  }",
		"}"}, new String[]{
				"class Test {",
				"  public static void main(String[] args) {",
				"    Shell shell = new Shell();",
				"    shell.setSize(320, 240);",
				"    shell.setLayout(new FillLayout());",
				"    {",
				"      Button button = new Button(shell, SWT.CHECK);",
				"      button.setText(\"Check Button\");",
				"    }",
				"  }",
		"}"}, "check");
	}

	@Test
	public void test_addRadioButton() throws Exception {
		check_addButton(new String[]{
				"class Test {",
				"  public static void main(String[] args) {",
				"    Shell shell = new Shell();",
				"    shell.setSize(320, 240);",
				"    shell.setLayout(new FillLayout());",
				"  }",
		"}"}, new String[]{
				"class Test {",
				"  public static void main(String[] args) {",
				"    Shell shell = new Shell();",
				"    shell.setSize(320, 240);",
				"    shell.setLayout(new FillLayout());",
				"    {",
				"      Button button = new Button(shell, SWT.RADIO);",
				"      button.setText(\"Radio Button\");",
				"    }",
				"  }",
		"}"}, "radio");
	}

	private void check_addButton(String[] lines, String[] expected, String creationId)
			throws Exception {
		CompositeInfo shell = parseComposite(lines);
		//
		FillLayoutInfo layout = (FillLayoutInfo) shell.getLayout();
		ControlInfo button = createJavaInfo("org.eclipse.swt.widgets.Button", creationId);
		assertEquals(
				"org.eclipse.swt.widgets.Button",
				button.getDescription().getComponentClass().getName());
		//
		layout.command_CREATE(button, null);
		assertEditor(expected);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ButtonStylePresentation
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test that {@link ButtonStylePresentation} returns different icons for buttons with different
	 * styles.
	 */
	@Test
	public void test_ButtonStylePresentation() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    new Button(this, SWT.NONE);",
						"    new Button(this, SWT.PUSH);",
						"    new Button(this, SWT.CHECK);",
						"    new Button(this, SWT.RADIO);",
						"  }",
						"}");
		shell.refresh();
		// prepare Button's
		ButtonInfo buttonDefault = (ButtonInfo) shell.getChildrenControls().get(0);
		ButtonInfo buttonPush = (ButtonInfo) shell.getChildrenControls().get(1);
		ButtonInfo buttonCheck = (ButtonInfo) shell.getChildrenControls().get(2);
		ButtonInfo buttonRadio = (ButtonInfo) shell.getChildrenControls().get(3);
		// check icons
		assertSame(buttonDefault.getPresentation().getIcon(), buttonPush.getPresentation().getIcon());
		assertNotSame(buttonPush.getPresentation().getIcon(), buttonRadio.getPresentation().getIcon());
		assertNotSame(buttonPush.getPresentation().getIcon(), buttonCheck.getPresentation().getIcon());
		assertNotSame(buttonRadio.getPresentation().getIcon(), buttonCheck.getPresentation().getIcon());
	}

	/**
	 * Test that {@link ButtonStylePresentation} returns same icons for different {@link Button}
	 * instances, for same style.
	 */
	@Test
	public void test_ButtonStylePresentation_cacheIcons() throws Exception {
		parseComposite(
				"public class Test extends Shell {",
				"  public Test() {",
				"    Button button_1 = new Button(this, SWT.CHECK);",
				"    Button button_2 = new Button(this, SWT.CHECK);",
				"  }",
				"}");
		refresh();
		ButtonInfo button_1 = getJavaInfoByName("button_1");
		ButtonInfo button_2 = getJavaInfoByName("button_2");
		// icons should be same
		assertSame(button_1.getPresentation().getIcon(), button_2.getPresentation().getIcon());
	}
}