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
package org.eclipse.wb.tests.designer.rcp.model.layout;

import org.eclipse.wb.internal.rcp.gef.policy.layout.StackLayoutNavigationFigure;
import org.eclipse.wb.internal.rcp.model.layout.StackLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.rcp.RcpGefTest;

import org.junit.Test;

/**
 * Test for {@link StackLayoutInfo} in GEF.
 *
 * @author scheglov_ke
 */
public class StackLayoutGefTest extends RcpGefTest {
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
	// CREATE on canvas
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_CREATE_onCanvas_empty() throws Exception {
		CompositeInfo shell =
				openComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new StackLayout());",
						"  }",
						"}");
		//
		loadCreationTool("org.eclipse.swt.widgets.Button", "empty");
		canvas.moveTo(shell, 100, 100).click();
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new StackLayout());",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"    }",
				"  }",
				"}");
	}

	@Test
	public void test_CREATE_onCanvas_beforeExisting() throws Exception {
		CompositeInfo shell =
				openComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new StackLayout());",
						"    {",
						"      Button button_1 = new Button(this, SWT.NONE);",
						"    }",
						"  }",
						"}");
		ControlInfo button_1 = getJavaInfoByName("button_1");
		// select "shell", so "button_1" will be transparent on borders
		canvas.select(shell);
		// create new Button
		loadCreationTool("org.eclipse.swt.widgets.Button", "empty");
		canvas.moveTo(button_1, 2, 100).click();
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new StackLayout());",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"    }",
				"    {",
				"      Button button_1 = new Button(this, SWT.NONE);",
				"    }",
				"  }",
				"}");
	}

	@Test
	public void test_CREATE_onCanvas_afterExisting() throws Exception {
		CompositeInfo shell =
				openComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new StackLayout());",
						"    {",
						"      Button button_1 = new Button(this, SWT.NONE);",
						"    }",
						"  }",
						"}");
		ControlInfo button_1 = getJavaInfoByName("button_1");
		// select "shell", so "button_1" will be transparent on borders
		canvas.select(shell);
		// create new Button
		loadCreationTool("org.eclipse.swt.widgets.Button", "empty");
		canvas.moveTo(button_1, -2, 100).click();
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new StackLayout());",
				"    {",
				"      Button button_1 = new Button(this, SWT.NONE);",
				"    }",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"    }",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// CREATE in tree
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_CREATE_inTree_empty() throws Exception {
		CompositeInfo shell =
				openComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new StackLayout());",
						"  }",
						"}");
		// create new Button
		loadCreationTool("org.eclipse.swt.widgets.Button", "empty");
		tree.moveOn(shell);
		tree.assertCommandNotNull();
		tree.click();
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new StackLayout());",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"    }",
				"  }",
				"}");
	}

	@Test
	public void test_CREATE_inTree_beforeExisting() throws Exception {
		openComposite(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new StackLayout());",
				"    {",
				"      Button button_1 = new Button(this, SWT.NONE);",
				"    }",
				"  }",
				"}");
		ControlInfo button_1 = getJavaInfoByName("button_1");
		// create new Button
		loadCreationTool("org.eclipse.swt.widgets.Button", "empty");
		tree.moveBefore(button_1).click();
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new StackLayout());",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"    }",
				"    {",
				"      Button button_1 = new Button(this, SWT.NONE);",
				"    }",
				"  }",
				"}");
	}

	@Test
	public void test_CREATE_inTree_afterExisting() throws Exception {
		openComposite(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new StackLayout());",
				"    {",
				"      Button button_1 = new Button(this, SWT.NONE);",
				"    }",
				"  }",
				"}");
		ControlInfo button_1 = getJavaInfoByName("button_1");
		// create new Button
		loadCreationTool("org.eclipse.swt.widgets.Button", "empty");
		tree.moveAfter(button_1).click();
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new StackLayout());",
				"    {",
				"      Button button_1 = new Button(this, SWT.NONE);",
				"    }",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"    }",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// MOVE in tree
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_MOVE_inTree() throws Exception {
		openComposite(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new StackLayout());",
				"    {",
				"      Button button_1 = new Button(this, SWT.NONE);",
				"      button_1.setText('Button 1');",
				"    }",
				"    {",
				"      Button button_2 = new Button(this, SWT.NONE);",
				"      button_2.setText('Button 2');",
				"    }",
				"  }",
				"}");
		ControlInfo button_1 = getJavaInfoByName("button_1");
		ControlInfo button_2 = getJavaInfoByName("button_2");
		//
		tree.startDrag(button_2).dragBefore(button_1).endDrag();
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new StackLayout());",
				"    {",
				"      Button button_2 = new Button(this, SWT.NONE);",
				"      button_2.setText('Button 2');",
				"    }",
				"    {",
				"      Button button_1 = new Button(this, SWT.NONE);",
				"      button_1.setText('Button 1');",
				"    }",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Navigation
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_navigation_next() throws Exception {
		openComposite(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new StackLayout());",
				"    {",
				"      Button button_1 = new Button(this, SWT.NONE);",
				"      button_1.setText('Button 1');",
				"    }",
				"    {",
				"      Button button_2 = new Button(this, SWT.NONE);",
				"      button_2.setText('Button 2');",
				"    }",
				"    {",
				"      Button button_3 = new Button(this, SWT.NONE);",
				"      button_3.setText('Button 3');",
				"    }",
				"  }",
				"}");
		ControlInfo button_1 = getJavaInfoByName("button_1");
		ControlInfo button_2 = getJavaInfoByName("button_2");
		ControlInfo button_3 = getJavaInfoByName("button_3");
		// initially "button_1" visible
		canvas.assertNotNullEditPart(button_1);
		canvas.assertNullEditPart(button_2);
		canvas.assertNullEditPart(button_3);
		// click "next", select "button_2"
		canvas.select(button_1);
		navigateNext(button_1);
		canvas.assertNullEditPart(button_1);
		canvas.assertNotNullEditPart(button_2);
		canvas.assertNullEditPart(button_3);
		// click "next", select "button_3"
		navigateNext(button_2);
		canvas.assertNullEditPart(button_1);
		canvas.assertNullEditPart(button_2);
		canvas.assertNotNullEditPart(button_3);
		// click "next", select "button_1"
		navigateNext(button_3);
		canvas.assertNotNullEditPart(button_1);
		canvas.assertNullEditPart(button_2);
		canvas.assertNullEditPart(button_3);
	}

	@Test
	public void test_navigation_prev() throws Exception {
		openComposite(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new StackLayout());",
				"    {",
				"      Button button_1 = new Button(this, SWT.NONE);",
				"      button_1.setText('Button 1');",
				"    }",
				"    {",
				"      Button button_2 = new Button(this, SWT.NONE);",
				"      button_2.setText('Button 2');",
				"    }",
				"    {",
				"      Button button_3 = new Button(this, SWT.NONE);",
				"      button_3.setText('Button 3');",
				"    }",
				"  }",
				"}");
		ControlInfo button_1 = getJavaInfoByName("button_1");
		ControlInfo button_2 = getJavaInfoByName("button_2");
		ControlInfo button_3 = getJavaInfoByName("button_3");
		// initially "button_1" visible
		canvas.assertNotNullEditPart(button_1);
		canvas.assertNullEditPart(button_2);
		canvas.assertNullEditPart(button_3);
		// click "prev", select "button_3"
		canvas.select(button_1);
		navigatePrev(button_1);
		canvas.assertNullEditPart(button_1);
		canvas.assertNullEditPart(button_2);
		canvas.assertNotNullEditPart(button_3);
		// click "prev", select "button_2"
		navigatePrev(button_3);
		canvas.assertNullEditPart(button_1);
		canvas.assertNotNullEditPart(button_2);
		canvas.assertNullEditPart(button_3);
		// click "prev", select "button_1"
		navigatePrev(button_2);
		canvas.assertNotNullEditPart(button_1);
		canvas.assertNullEditPart(button_2);
		canvas.assertNullEditPart(button_3);
	}

	private void navigateNext(ControlInfo component) {
		canvas.moveTo(component, -3 - 1, 0).click();
	}

	private void navigatePrev(ControlInfo component) {
		canvas.moveTo(component, -3 - StackLayoutNavigationFigure.WIDTH - 1, 0).click();
	}
}
