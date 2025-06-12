/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc.
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
package org.eclipse.wb.tests.designer.rcp.model.layout.form.gef;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.rcp.RcpGefTest;

import org.eclipse.jface.action.IAction;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Shell;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * GEF tests for {@link FormLayout} alignment support.
 *
 * @author mitin_aa
 * @author scheglov_ke
 */
public class FormLayoutAlignmentTest extends RcpGefTest {
	private CompositeInfo shell;
	private ControlInfo button_1;
	private ControlInfo button_2;

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
	@Test
	public void test_alignLeft() throws Exception {
		String[] constraints_1 =
				new String[]{"left = new FormAttachment(0, 50);", "top = new FormAttachment(0, 100);"};
		parse_twoButtons_typical(constraints_1, new String[]{
				"left = new FormAttachment(0, 150);",
		"top = new FormAttachment(0, 200);"});
		// do alignment
		runAlignmentAction_twoButtons("Align left edges");
		assertEditor_twoButtons_typical(constraints_1, new String[]{
				"left = new FormAttachment(button_1, 0, SWT.LEFT);",
		"top = new FormAttachment(0, 200);"});
	}

	@Test
	public void test_replicateWidth_independentControls() throws Exception {
		String[] constraints_1 =
				new String[]{
						"left = new FormAttachment(0, 50);",
						"top = new FormAttachment(0, 100);",
		"right = new FormAttachment(0, 250);"};
		parse_twoButtons_typical(constraints_1, new String[]{
				"left = new FormAttachment(0, 150);",
		"top = new FormAttachment(0, 200);"});
		// do alignment
		runAlignmentAction_twoButtons("Replicate width");
		assertEditor_twoButtons_typical(constraints_1, new String[]{
				"right = new FormAttachment(0, 350);",
				"left = new FormAttachment(0, 150);",
		"top = new FormAttachment(0, 200);"});
	}

	@Test
	public void test_replicateWidth_leftParent() throws Exception {
		String[] constraints_1 =
				new String[]{
						"left = new FormAttachment(0, 50);",
						"top = new FormAttachment(0, 100);",
		"right = new FormAttachment(0, 250);"};
		parse_twoButtons_typical(constraints_1, new String[]{
				"left = new FormAttachment(0, 10);",
		"top = new FormAttachment(0, 200);"});
		// do alignment
		runAlignmentAction_twoButtons("Replicate width");
		assertEditor_twoButtons_typical(constraints_1, new String[]{
				"right = new FormAttachment(0, 210);",
				"left = new FormAttachment(0, 10);",
		"top = new FormAttachment(0, 200);"});
	}

	@Test
	public void test_replicateWidth_rightParent() throws Exception {
		String[] constraints_1 =
				new String[]{
						"left = new FormAttachment(0, 50);",
						"top = new FormAttachment(0, 100);",
		"right = new FormAttachment(0, 250);"};
		parse_twoButtons_typical(constraints_1, new String[]{
				"right = new FormAttachment(100, -10);",
		"top = new FormAttachment(0, 200);"});
		// do alignment
		runAlignmentAction_twoButtons("Replicate width");
		assertEditor_twoButtons_typical(constraints_1, new String[]{
				"left = new FormAttachment(100, -210);",
				"right = new FormAttachment(100, -10);",
		"top = new FormAttachment(0, 200);"});
	}

	@Test
	public void test_replicateWidth_leftAttached() throws Exception {
		String[] constraints_1 =
				new String[]{
						"left = new FormAttachment(0, 50);",
						"top = new FormAttachment(0, 100);",
		"right = new FormAttachment(0, 250);"};
		parse_twoButtons_typical(constraints_1, new String[]{
				"left = new FormAttachment(button_1, 0, SWT.LEFT);",
		"top = new FormAttachment(0, 200);"});
		// do alignment
		runAlignmentAction_twoButtons("Replicate width");
		assertEditor_twoButtons_typical(constraints_1, new String[]{
				"right = new FormAttachment(button_1, 200);",
				"left = new FormAttachment(button_1, 0, SWT.LEFT);",
		"top = new FormAttachment(0, 200);"});
	}

	@Test
	public void test_replicateWidth_rightAttached() throws Exception {
		String[] constraints_1 =
				new String[]{
						"left = new FormAttachment(0, 50);",
						"top = new FormAttachment(0, 100);",
		"right = new FormAttachment(0, 250);"};
		parse_twoButtons_typical(constraints_1, new String[]{
				"right = new FormAttachment(button_1, 0, SWT.RIGHT);",
		"top = new FormAttachment(0, 200);"});
		// do alignment
		runAlignmentAction_twoButtons("Replicate width");
		assertEditor_twoButtons_typical(constraints_1, new String[]{
				"left = new FormAttachment(button_1, -200);",
				"right = new FormAttachment(button_1, 0, SWT.RIGHT);",
		"top = new FormAttachment(0, 200);"});
	}

	@Test
	public void test_replicateWidth_notAttached() throws Exception {
		String[] constraints_1 =
				new String[]{
						"left = new FormAttachment(0, 50);",
						"top = new FormAttachment(0, 100);",
		"right = new FormAttachment(0, 250);"};
		parse_twoButtons_typical(constraints_1, new String[]{"top = new FormAttachment(0, 200);"});
		// do alignment
		runAlignmentAction_twoButtons("Replicate width");
		assertEditor_twoButtons_typical(constraints_1, new String[]{
				"right = new FormAttachment(0, 200);",
				"left = new FormAttachment(0);",
		"top = new FormAttachment(0, 200);"});
	}

	@Test
	public void test_replicateWidth_leftRightAttached() throws Exception {
		String[] constraints_1 =
				new String[]{
						"left = new FormAttachment(0, 50);",
						"top = new FormAttachment(0, 100);",
		"right = new FormAttachment(0, 150);"};
		parse_twoButtons_typical(constraints_1, new String[]{
				"left = new FormAttachment(button_1, 0, SWT.LEFT);",
				"top = new FormAttachment(0, 200);",
		"right = new FormAttachment(button_1, 200, SWT.LEFT);"});
		// do alignment
		runAlignmentAction_twoButtons("Replicate width");
		assertEditor_twoButtons_typical(constraints_1, new String[]{
				"right = new FormAttachment(button_1, 100);",
				"left = new FormAttachment(button_1, 0, SWT.LEFT);",
		"top = new FormAttachment(0, 200);"});
	}

	@Test
	@Disabled
	public void test_replicateWidth_leftRightAttached_reverse() throws Exception {
		parse_twoButtons_typical(new String[]{
				"left = new FormAttachment(0, 50);",
				"top = new FormAttachment(0, 100);",
		"right = new FormAttachment(0, 150);"}, new String[]{
				"left = new FormAttachment(button_1, 0, SWT.LEFT);",
				"top = new FormAttachment(0, 200);",
		"right = new FormAttachment(button_1, 200, SWT.LEFT);"});
		// do alignment
		runAlignmentAction("Replicate width", button_2, button_1);
		assertEditor_twoButtons_typical(new String[]{
				"left = new FormAttachment(0, 50);",
				"top = new FormAttachment(0, 100);",
		"right = new FormAttachment(0, 250);"}, new String[]{
				"right = new FormAttachment(100, " + (50 + 200 - shell.getClientArea().width) + ");",
				"left = new FormAttachment(0, 50);",
		"top = new FormAttachment(0, 200);"});
	}

	@Test
	public void test_replicateWidth_leftAttachedToRight_rightNotAttached() throws Exception {
		parse_twoButtons(
				"public class Test extends Shell {",
				"  private org.eclipse.swt.widgets.Button button_1;",
				"  private Control button_2;",
				"  public Test() {",
				"    setLayout(new FormLayout());",
				"    {",
				"      button_1 = new org.eclipse.swt.widgets.Button(this, SWT.NONE);",
				"      button_1.setText('Some long text');",
				"      {",
				"        FormData data_1 = new FormData();",
				"        data_1.left = new FormAttachment(0, 50);",
				"        data_1.top = new FormAttachment(0, 100);",
				"        button_1.setLayoutData(data_1);",
				"      }",
				"    }",
				"    {",
				"      button_2 = new Button(this, SWT.NONE);",
				"      {",
				"        FormData data_2 = new FormData();",
				"        data_2.left = new FormAttachment(button_1, 5);",
				"        data_2.top = new FormAttachment(0, 200);",
				"        button_2.setLayoutData(data_2);",
				"      }",
				"    }",
				"  }",
				"}");
		// do alignment
		runAlignmentAction_twoButtons("Replicate width");
		assertEditor(
				"public class Test extends Shell {",
				"  private org.eclipse.swt.widgets.Button button_1;",
				"  private Control button_2;",
				"  public Test() {",
				"    setLayout(new FormLayout());",
				"    {",
				"      button_1 = new org.eclipse.swt.widgets.Button(this, SWT.NONE);",
				"      button_1.setText('Some long text');",
				"      {",
				"        FormData data_1 = new FormData();",
				"        data_1.left = new FormAttachment(0, 50);",
				"        data_1.top = new FormAttachment(0, 100);",
				"        button_1.setLayoutData(data_1);",
				"      }",
				"    }",
				"    {",
				"      button_2 = new Button(this, SWT.NONE);",
				"      {",
				"        FormData data_2 = new FormData();",
				"        data_2.right = new FormAttachment(button_1, "
						+ (5 + button_1.getModelBounds().width)
						+ ", SWT.RIGHT);",
						"        data_2.left = new FormAttachment(button_1, 5);",
						"        data_2.top = new FormAttachment(0, 200);",
						"        button_2.setLayoutData(data_2);",
						"      }",
						"    }",
						"  }",
				"}");
	}

	@Test
	public void test_replicateWidth_leftAttachedToRight_rightNotAttached_createButton2()
			throws Exception {
		parse(
				"public class Test extends Shell {",
				"  private org.eclipse.swt.widgets.Button button_1;",
				"  public Test() {",
				"    setLayout(new FormLayout());",
				"    {",
				"      button_1 = new org.eclipse.swt.widgets.Button(this, SWT.NONE);",
				"      button_1.setText('Some long text');",
				"      {",
				"        FormData data_1 = new FormData();",
				"        data_1.left = new FormAttachment(0, 50);",
				"        data_1.top = new FormAttachment(0, 100);",
				"        button_1.setLayoutData(data_1);",
				"      }",
				"    }",
				"  }",
				"}");
		button_1 = shell.getChildrenControls().get(0);
		// create "button_2"
		button_2 = loadCreationTool("test.Button");
		canvas.create(75, 25).sideMode();
		canvas.moveTo(shell, -100, -100);
		canvas.target(button_1).outX(5).inY(0).move();
		canvas.click();
		assertEditor(
				"public class Test extends Shell {",
				"  private org.eclipse.swt.widgets.Button button_1;",
				"  public Test() {",
				"    setLayout(new FormLayout());",
				"    {",
				"      button_1 = new org.eclipse.swt.widgets.Button(this, SWT.NONE);",
				"      button_1.setText('Some long text');",
				"      {",
				"        FormData data_1 = new FormData();",
				"        data_1.left = new FormAttachment(0, 50);",
				"        data_1.top = new FormAttachment(0, 100);",
				"        button_1.setLayoutData(data_1);",
				"      }",
				"    }",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      {",
				"        FormData fd_button = new FormData();",
				"        fd_button.top = new FormAttachment(button_1, 0, SWT.TOP);",
				"        fd_button.left = new FormAttachment(button_1, 6);",
				"        button.setLayoutData(fd_button);",
				"      }",
				"    }",
				"  }",
				"}");
		// do alignment
		runAlignmentAction_twoButtons("Replicate width");
		assertEditor(
				"public class Test extends Shell {",
				"  private org.eclipse.swt.widgets.Button button_1;",
				"  public Test() {",
				"    setLayout(new FormLayout());",
				"    {",
				"      button_1 = new org.eclipse.swt.widgets.Button(this, SWT.NONE);",
				"      button_1.setText('Some long text');",
				"      {",
				"        FormData data_1 = new FormData();",
				"        data_1.left = new FormAttachment(0, 50);",
				"        data_1.top = new FormAttachment(0, 100);",
				"        button_1.setLayoutData(data_1);",
				"      }",
				"    }",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      {",
				"        FormData fd_button = new FormData();",
				"        fd_button.right = new FormAttachment(button_1, "
						+ (6 + button_1.getModelBounds().width)
						+ ", SWT.RIGHT);",
						"        fd_button.top = new FormAttachment(button_1, 0, SWT.TOP);",
						"        fd_button.left = new FormAttachment(button_1, 6);",
						"        button.setLayoutData(fd_button);",
						"      }",
						"    }",
						"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Parses typical RCP code with {@link Shell}.
	 */
	private void parse(String... lines) throws Exception {
		prepareComponent(75, 25);
		// Shell is root
		shell = openComposite(lines);
	}

	/**
	 * Parses typical RCP code with {@link Shell} and two {@link Button}-s.
	 */
	private void parse_twoButtons_typical(String[] constraints_1, String[] constraints_2)
			throws Exception {
		String[] lines = getLines_twoButtons_typical(constraints_1, constraints_2);
		parse_twoButtons(lines);
	}

	private void assertEditor_twoButtons_typical(String[] constraints_1, String[] constraints_2)
			throws Exception {
		String[] lines = getLines_twoButtons_typical(constraints_1, constraints_2);
		assertEditor(lines);
	}

	private static String[] getLines_twoButtons_typical(String[] constraints_1, String[] constraints_2) {
		constraints_1 = ArrayUtils.clone(constraints_1);
		constraints_2 = ArrayUtils.clone(constraints_2);
		for (int i = 0; i < constraints_1.length; i++) {
			constraints_1[i] = "        data_1." + constraints_1[i];
		}
		for (int i = 0; i < constraints_2.length; i++) {
			constraints_2[i] = "        data_2." + constraints_2[i];
		}
		String[] lines =
				new String[]{
						"public class Test extends Shell {",
						"  private Button button_1;",
						"  private Button button_2;",
						"  public Test() {",
						"    setLayout(new FormLayout());",
						"    {",
						"      button_1 = new Button(this, SWT.NONE);",
						"      {",
		"        FormData data_1 = new FormData();"};
		lines = ArrayUtils.addAll(lines, constraints_1);
		lines =
				ArrayUtils.addAll(lines, new String[]{
						"        button_1.setLayoutData(data_1);",
						"      }",
						"    }",
						"    {",
						"      button_2 = new Button(this, SWT.NONE);",
						"      {",
				"        FormData data_2 = new FormData();"});
		lines = ArrayUtils.addAll(lines, constraints_2);
		lines =
				ArrayUtils.addAll(lines, new String[]{
						"        button_2.setLayoutData(data_2);",
						"      }",
						"    }",
						"  }",
				"}"});
		return lines;
	}

	/**
	 * Parses typical RCP code with {@link Shell} and two {@link Button}-s.
	 */
	private void parse_twoButtons(String... lines) throws Exception {
		parse(lines);
		// prepare Button-s
		button_1 = shell.getChildrenControls().get(0);
		button_2 = shell.getChildrenControls().get(1);
	}

	private void runAlignmentAction_twoButtons(String actionText) throws Exception {
		runAlignmentAction(actionText, button_1, button_2);
	}

	private void runAlignmentAction(String actionText, ControlInfo... controls) throws Exception {
		// prepare actions
		List<Object> actions;
		{
			actions = new ArrayList<>();
			List<ObjectInfo> selectedObjects = List.of(controls);
			shell.getBroadcastObject().addSelectionActions(selectedObjects, actions);
		}
		// run action
		{
			IAction action = findAction(actions, actionText);
			assertNotNull(action);
			action.run();
		}
	}
}
