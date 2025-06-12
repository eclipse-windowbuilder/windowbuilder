/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
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
package org.eclipse.wb.tests.designer.rcp.model.widgets;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.rcp.model.widgets.ToolBarInfo;
import org.eclipse.wb.tests.designer.rcp.RcpGefTest;

import org.junit.jupiter.api.Test;

/**
 * Test for {@link ToolBarInfo} in GEF.
 *
 * @author scheglov_ke
 */
public class ToolBarGefTest extends RcpGefTest {
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
	// Canvas
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_canvas_CREATE_item() throws Exception {
		ToolBarInfo toolBar =
				openJavaInfo(
						"public class Test extends ToolBar {",
						"  public Test(Composite parent, int style) {",
						"    super(parent, style);",
						"  }",
						"}");
		//
		loadCreationTool("org.eclipse.swt.widgets.ToolItem");
		canvas.moveTo(toolBar, 5, 5);
		canvas.click();
		assertEditor(
				"public class Test extends ToolBar {",
				"  public Test(Composite parent, int style) {",
				"    super(parent, style);",
				"    {",
				"      ToolItem toolItem = new ToolItem(this, SWT.NONE);",
				"      toolItem.setText('New Item');",
				"    }",
				"  }",
				"}");
	}

	@Test
	public void test_canvas_CREATE_control_good() throws Exception {
		openJavaInfo(
				"public class Test extends ToolBar {",
				"  public Test(Composite parent, int style) {",
				"    super(parent, style);",
				"    {",
				"      ToolItem item = new ToolItem(this, SWT.SEPARATOR);",
				"      item.setWidth(100);",
				"    }",
				"  }",
				"}");
		JavaInfo item = getJavaInfoByName("item");
		//
		loadButton();
		canvas.moveTo(item, 5, 5);
		canvas.assertFeedbacks(canvas.getTargetPredicate(item));
		canvas.assertCommandNotNull();
		canvas.click();
		assertEditor(
				"public class Test extends ToolBar {",
				"  public Test(Composite parent, int style) {",
				"    super(parent, style);",
				"    {",
				"      ToolItem item = new ToolItem(this, SWT.SEPARATOR);",
				"      item.setWidth(100);",
				"      {",
				"        Button button = new Button(this, SWT.NONE);",
				"        item.setControl(button);",
				"      }",
				"    }",
				"  }",
				"}");
	}

	@Test
	public void test_canvas_CREATE_control_notSeparator() throws Exception {
		openJavaInfo(
				"public class Test extends ToolBar {",
				"  public Test(Composite parent, int style) {",
				"    super(parent, style);",
				"    {",
				"      ToolItem item = new ToolItem(this, SWT.NONE);",
				"    }",
				"  }",
				"}");
		JavaInfo item = getJavaInfoByName("item");
		//
		loadButton();
		canvas.moveTo(item, 5, 5);
		canvas.assertNoFeedbacks();
		canvas.assertCommandNull();
	}

	@Test
	public void test_canvas_CREATE_control_alreadyHasControl() throws Exception {
		openJavaInfo(
				"public class Test extends ToolBar {",
				"  public Test(Composite parent, int style) {",
				"    super(parent, style);",
				"    {",
				"      ToolItem item = new ToolItem(this, SWT.SEPARATOR);",
				"      item.setWidth(100);",
				"      {",
				"        Button existing = new Button(this, SWT.NONE);",
				"        item.setControl(existing);",
				"      }",
				"    }",
				"  }",
				"}");
		JavaInfo item = getJavaInfoByName("item");
		//
		loadButton();
		canvas.moveTo(item, 5, 5);
		canvas.assertFeedbacks(canvas.getTargetPredicate(item));
		canvas.assertCommandNull();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Tree
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_tree_CREATE_item() throws Exception {
		ToolBarInfo toolBar =
				openJavaInfo(
						"public class Test extends ToolBar {",
						"  public Test(Composite parent, int style) {",
						"    super(parent, style);",
						"  }",
						"}");
		//
		loadCreationTool("org.eclipse.swt.widgets.ToolItem");
		tree.moveOn(toolBar);
		tree.click();
		assertEditor(
				"public class Test extends ToolBar {",
				"  public Test(Composite parent, int style) {",
				"    super(parent, style);",
				"    {",
				"      ToolItem toolItem = new ToolItem(this, SWT.NONE);",
				"      toolItem.setText('New Item');",
				"    }",
				"  }",
				"}");
	}

	@Test
	public void test_tree_CREATE_control_good() throws Exception {
		openJavaInfo(
				"public class Test extends ToolBar {",
				"  public Test(Composite parent, int style) {",
				"    super(parent, style);",
				"    {",
				"      ToolItem item = new ToolItem(this, SWT.SEPARATOR);",
				"      item.setWidth(100);",
				"    }",
				"  }",
				"}");
		JavaInfo item = getJavaInfoByName("item");
		//
		loadButton();
		tree.moveOn(item);
		tree.assertFeedback_on(item);
		tree.assertCommandNotNull();
		tree.click();
		assertEditor(
				"public class Test extends ToolBar {",
				"  public Test(Composite parent, int style) {",
				"    super(parent, style);",
				"    {",
				"      ToolItem item = new ToolItem(this, SWT.SEPARATOR);",
				"      item.setWidth(100);",
				"      {",
				"        Button button = new Button(this, SWT.NONE);",
				"        item.setControl(button);",
				"      }",
				"    }",
				"  }",
				"}");
	}

	@Test
	public void test_tree_CREATE_control_notSeparator() throws Exception {
		openJavaInfo(
				"public class Test extends ToolBar {",
				"  public Test(Composite parent, int style) {",
				"    super(parent, style);",
				"    {",
				"      ToolItem item = new ToolItem(this, SWT.NONE);",
				"    }",
				"  }",
				"}");
		JavaInfo item = getJavaInfoByName("item");
		//
		loadButton();
		tree.moveOn(item);
		tree.assertCommandNull();
	}

	@Test
	public void test_tree_CREATE_control_alreadyHasControl() throws Exception {
		openJavaInfo(
				"public class Test extends ToolBar {",
				"  public Test(Composite parent, int style) {",
				"    super(parent, style);",
				"    {",
				"      ToolItem item = new ToolItem(this, SWT.SEPARATOR);",
				"      item.setWidth(100);",
				"      {",
				"        Button existing = new Button(this, SWT.NONE);",
				"        item.setControl(existing);",
				"      }",
				"    }",
				"  }",
				"}");
		JavaInfo item = getJavaInfoByName("item");
		//
		loadButton();
		tree.moveOn(item);
		tree.assertFeedback_on(item);
		tree.assertCommandNull();
	}
}
