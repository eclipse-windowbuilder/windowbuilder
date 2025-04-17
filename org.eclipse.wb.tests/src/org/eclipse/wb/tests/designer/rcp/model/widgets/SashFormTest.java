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

import org.eclipse.wb.internal.rcp.model.widgets.SashFormInfo;
import org.eclipse.wb.internal.swt.model.layout.FillLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.Expectations;
import org.eclipse.wb.tests.designer.Expectations.IntValue;
import org.eclipse.wb.tests.designer.rcp.BTestUtils;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.swt.custom.SashForm;

import org.junit.Ignore;
import org.junit.Test;

/**
 * Test for {@link SashFormInfo}.
 *
 * @author scheglov_ke
 */
public class SashFormTest extends RcpModelTest {
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
	 * Test for parsing {@link SashForm} with <code>setLayout()</code> invocation.
	 */
	@Test
	public void test_parseWith_setLayout() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setSize(500, 300);",
						"    setLayout(new FillLayout());",
						"    SashForm sashForm = new SashForm(this, SWT.NONE);",
						"    sashForm.setLayout(new FillLayout());",
						"  }",
						"}");
		assertHierarchy(
				"{this: org.eclipse.swt.widgets.Shell} {this} {/setSize(500, 300)/ /setLayout(new FillLayout())/ /new SashForm(this, SWT.NONE)/}",
				"  {new: org.eclipse.swt.layout.FillLayout} {empty} {/setLayout(new FillLayout())/}",
				"  {new: org.eclipse.swt.custom.SashForm} {local-unique: sashForm} {/new SashForm(this, SWT.NONE)/ /sashForm.setLayout(new FillLayout())/}");
		//
		SashFormInfo sashForm = (SashFormInfo) shell.getChildrenControls().get(0);
		assertFalse(sashForm.hasLayout());
	}

	/**
	 * Test for parsing {@link SashForm} without <code>setLayout()</code> invocation.
	 */
	@Test
	public void test_parseWithout_setLayout() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setSize(500, 300);",
						"    setLayout(new FillLayout());",
						"    SashForm sashForm = new SashForm(this, SWT.NONE);",
						"  }",
						"}");
		assertHierarchy(
				"{this: org.eclipse.swt.widgets.Shell} {this} {/setSize(500, 300)/ /setLayout(new FillLayout())/ /new SashForm(this, SWT.NONE)/}",
				"  {new: org.eclipse.swt.layout.FillLayout} {empty} {/setLayout(new FillLayout())/}",
				"  {new: org.eclipse.swt.custom.SashForm} {local-unique: sashForm} {/new SashForm(this, SWT.NONE)/}");
		//
		SashFormInfo sashForm = (SashFormInfo) shell.getChildrenControls().get(0);
		assertFalse(sashForm.hasLayout());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// isHorizontal()
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link SashFormInfo#isHorizontal()}.
	 */
	@Test
	public void test_isHorizontal_true() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setSize(500, 300);",
						"    setLayout(new FillLayout());",
						"    SashForm sashForm = new SashForm(this, SWT.NONE);",
						"  }",
						"}");
		shell.refresh();
		SashFormInfo sashForm = (SashFormInfo) shell.getChildrenControls().get(0);
		assertTrue(sashForm.isHorizontal());
	}

	/**
	 * Test for {@link SashFormInfo#isHorizontal()}.
	 */
	@Test
	public void test_isHorizontal_false() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setSize(500, 300);",
						"    setLayout(new FillLayout());",
						"    SashForm sashForm = new SashForm(this, SWT.VERTICAL);",
						"  }",
						"}");
		shell.refresh();
		SashFormInfo sashForm = (SashFormInfo) shell.getChildrenControls().get(0);
		assertFalse(sashForm.isHorizontal());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Commands
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link SashFormInfo#command_CREATE(ControlInfo, ControlInfo)}.<br>
	 * No existing children yet.
	 */
	@Test
	public void test_CREATE_0() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setSize(500, 300);",
						"    setLayout(new FillLayout());",
						"    SashForm sashForm = new SashForm(this, SWT.NONE);",
						"  }",
						"}");
		shell.refresh();
		SashFormInfo sashForm = (SashFormInfo) shell.getChildrenControls().get(0);
		//
		ControlInfo button = BTestUtils.createButton();
		sashForm.command_CREATE(button, null);
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setSize(500, 300);",
				"    setLayout(new FillLayout());",
				"    SashForm sashForm = new SashForm(this, SWT.NONE);",
				"    {",
				"      Button button = new Button(sashForm, SWT.NONE);",
				"    }",
				"    sashForm.setWeights(new int[] {1});",
				"  }",
				"}");
	}

	/**
	 * Test for {@link SashFormInfo#command_CREATE(ControlInfo, ControlInfo)}.<br>
	 * Two existing children with weights.
	 */
	@Test
	public void test_CREATE_2() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setSize(500, 300);",
						"    setLayout(new FillLayout());",
						"    SashForm sashForm = new SashForm(this, SWT.NONE);",
						"    {",
						"      Button button_1 = new Button(sashForm, SWT.NONE);",
						"    }",
						"    {",
						"      Button button_2 = new Button(sashForm, SWT.NONE);",
						"    }",
						"    sashForm.setWeights(new int[] {2, 4});",
						"  }",
						"}");
		shell.refresh();
		SashFormInfo sashForm = (SashFormInfo) shell.getChildrenControls().get(0);
		//
		ControlInfo button = BTestUtils.createButton();
		sashForm.command_CREATE(button, null);
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setSize(500, 300);",
				"    setLayout(new FillLayout());",
				"    SashForm sashForm = new SashForm(this, SWT.NONE);",
				"    {",
				"      Button button_1 = new Button(sashForm, SWT.NONE);",
				"    }",
				"    {",
				"      Button button_2 = new Button(sashForm, SWT.NONE);",
				"    }",
				"    {",
				"      Button button = new Button(sashForm, SWT.NONE);",
				"    }",
				"    sashForm.setWeights(new int[] {2, 4, 3});",
				"  }",
				"}");
	}

	/**
	 * Test for {@link SashFormInfo#command_MOVE(ControlInfo, ControlInfo)}.<br>
	 * Two existing children with weights.
	 */
	@Test
	public void test_MOVE_inner() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setSize(500, 300);",
						"    setLayout(new FillLayout());",
						"    SashForm sashForm = new SashForm(this, SWT.NONE);",
						"    {",
						"      Button button_1 = new Button(sashForm, SWT.NONE);",
						"    }",
						"    {",
						"      Button button_2 = new Button(sashForm, SWT.NONE);",
						"    }",
						"    sashForm.setWeights(new int[] {2, 4});",
						"  }",
						"}");
		shell.refresh();
		SashFormInfo sashForm = (SashFormInfo) shell.getChildrenControls().get(0);
		ControlInfo button_1 = sashForm.getChildrenControls().get(0);
		ControlInfo button_2 = sashForm.getChildrenControls().get(1);
		//
		sashForm.command_MOVE(button_2, button_1);
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setSize(500, 300);",
				"    setLayout(new FillLayout());",
				"    SashForm sashForm = new SashForm(this, SWT.NONE);",
				"    {",
				"      Button button_2 = new Button(sashForm, SWT.NONE);",
				"    }",
				"    {",
				"      Button button_1 = new Button(sashForm, SWT.NONE);",
				"    }",
				"    sashForm.setWeights(new int[] {4, 2});",
				"  }",
				"}");
	}

	/**
	 * Test for {@link SashFormInfo#command_MOVE(ControlInfo, ControlInfo)}.<br>
	 * Move {@link ControlInfo} in.
	 */
	@Test
	public void test_MOVE_in() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setSize(500, 300);",
						"    setLayout(new FillLayout());",
						"    SashForm sashForm = new SashForm(this, SWT.NONE);",
						"    {",
						"      Button button_1 = new Button(sashForm, SWT.NONE);",
						"    }",
						"    {",
						"      Button button_2 = new Button(this, SWT.NONE);",
						"    }",
						"  }",
						"}");
		shell.refresh();
		SashFormInfo sashForm = (SashFormInfo) shell.getChildrenControls().get(0);
		ControlInfo button_2 = shell.getChildrenControls().get(1);
		//
		sashForm.command_MOVE(button_2, null);
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setSize(500, 300);",
				"    setLayout(new FillLayout());",
				"    SashForm sashForm = new SashForm(this, SWT.NONE);",
				"    {",
				"      Button button_1 = new Button(sashForm, SWT.NONE);",
				"    }",
				"    {",
				"      Button button_2 = new Button(sashForm, SWT.NONE);",
				"    }",
				"    sashForm.setWeights(new int[] {1, 1});",
				"  }",
				"}");
	}

	/**
	 * Test for {@link SashFormInfo#command_MOVE(ControlInfo, ControlInfo)}.<br>
	 * Move {@link ControlInfo} out.
	 */
	@Test
	public void test_MOVE_out() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setSize(500, 300);",
						"    setLayout(new FillLayout());",
						"    SashForm sashForm = new SashForm(this, SWT.NONE);",
						"    {",
						"      Button button_1 = new Button(sashForm, SWT.NONE);",
						"    }",
						"    {",
						"      Button button_2 = new Button(sashForm, SWT.NONE);",
						"    }",
						"    sashForm.setWeights(new int[] {2, 3});",
						"  }",
						"}");
		shell.refresh();
		FillLayoutInfo fillLayout = (FillLayoutInfo) shell.getLayout();
		SashFormInfo sashForm = (SashFormInfo) shell.getChildrenControls().get(0);
		ControlInfo button_2 = sashForm.getChildrenControls().get(1);
		//
		fillLayout.command_MOVE(button_2, null);
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setSize(500, 300);",
				"    setLayout(new FillLayout());",
				"    SashForm sashForm = new SashForm(this, SWT.NONE);",
				"    {",
				"      Button button_1 = new Button(sashForm, SWT.NONE);",
				"    }",
				"    sashForm.setWeights(new int[] {2});",
				"    {",
				"      Button button_2 = new Button(this, SWT.NONE);",
				"    }",
				"  }",
				"}");
	}

	/**
	 * Test for {@link SashFormInfo#command_MOVE(ControlInfo, ControlInfo)}.<br>
	 * Delete child {@link ControlInfo}.
	 */
	@Test
	public void test_DELETE() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setSize(500, 300);",
						"    setLayout(new FillLayout());",
						"    SashForm sashForm = new SashForm(this, SWT.NONE);",
						"    {",
						"      Button button_1 = new Button(sashForm, SWT.NONE);",
						"    }",
						"    {",
						"      Button button_2 = new Button(sashForm, SWT.NONE);",
						"    }",
						"    sashForm.setWeights(new int[] {1, 2});",
						"  }",
						"}");
		shell.refresh();
		SashFormInfo sashForm = (SashFormInfo) shell.getChildrenControls().get(0);
		ControlInfo button_2 = sashForm.getChildrenControls().get(1);
		//
		button_2.delete();
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setSize(500, 300);",
				"    setLayout(new FillLayout());",
				"    SashForm sashForm = new SashForm(this, SWT.NONE);",
				"    {",
				"      Button button_1 = new Button(sashForm, SWT.NONE);",
				"    }",
				"    sashForm.setWeights(new int[] {1});",
				"  }",
				"}");
	}

	/**
	 * Test for {@link SashFormInfo#command_RESIZE(ControlInfo, int)}.
	 */
	@Ignore
	@Test
	public void test_RESIZE() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setSize(500, 300);",
						"    setLayout(new FillLayout());",
						"    SashForm sashForm = new SashForm(this, SWT.NONE);",
						"    {",
						"      Button button_1 = new Button(sashForm, SWT.NONE);",
						"    }",
						"    {",
						"      Button button_2 = new Button(sashForm, SWT.NONE);",
						"    }",
						"  }",
						"}");
		shell.refresh();
		SashFormInfo sashForm = (SashFormInfo) shell.getChildrenControls().get(0);
		ControlInfo button_1 = sashForm.getChildrenControls().get(0);
		//
		sashForm.command_RESIZE(button_1, 150);
		int expectedRightWeight =
				Expectations.get(331, new IntValue[]{
						new IntValue("flanker-win", 339),
						new IntValue("kosta-home", 339),
						new IntValue("scheglov-win", 331)});
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setSize(500, 300);",
				"    setLayout(new FillLayout());",
				"    SashForm sashForm = new SashForm(this, SWT.NONE);",
				"    {",
				"      Button button_1 = new Button(sashForm, SWT.NONE);",
				"    }",
				"    {",
				"      Button button_2 = new Button(sashForm, SWT.NONE);",
				"    }",
				"    sashForm.setWeights(new int[] {150, " + expectedRightWeight + "});",
				"  }",
				"}");
	}
}