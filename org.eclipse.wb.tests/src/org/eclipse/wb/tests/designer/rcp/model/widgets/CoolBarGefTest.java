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

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.rcp.model.widgets.CoolBarInfo;
import org.eclipse.wb.tests.designer.rcp.RcpGefTest;

import org.junit.Test;

/**
 * Test for {@link CoolBarInfo} in GEF.
 *
 * @author scheglov_ke
 */
public class CoolBarGefTest extends RcpGefTest {
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
		CoolBarInfo coolBar =
				openJavaInfo(
						"public class Test extends CoolBar {",
						"  public Test(Composite parent, int style) {",
						"    super(parent, style);",
						"  }",
						"}");
		//
		loadCreationTool("org.eclipse.swt.widgets.CoolItem");
		canvas.moveTo(coolBar, 5, 5);
		canvas.click();
		assertEditor(
				"public class Test extends CoolBar {",
				"  public Test(Composite parent, int style) {",
				"    super(parent, style);",
				"    {",
				"      CoolItem coolItem = new CoolItem(this, SWT.NONE);",
				"    }",
				"  }",
				"}");
	}

	@Test
	public void test_canvas_CREATE_control_good() throws Exception {
		openJavaInfo(
				"public class Test extends CoolBar {",
				"  public Test(Composite parent, int style) {",
				"    super(parent, style);",
				"    {",
				"      CoolItem item = new CoolItem(this, SWT.NONE);",
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
				"public class Test extends CoolBar {",
				"  public Test(Composite parent, int style) {",
				"    super(parent, style);",
				"    {",
				"      CoolItem item = new CoolItem(this, SWT.NONE);",
				"      {",
				"        Button button = new Button(this, SWT.NONE);",
				"        item.setControl(button);",
				"      }",
				"    }",
				"  }",
				"}");
	}

	@Test
	public void test_canvas_CREATE_control_alreadyHasControl() throws Exception {
		openJavaInfo(
				"public class Test extends CoolBar {",
				"  public Test(Composite parent, int style) {",
				"    super(parent, style);",
				"    {",
				"      CoolItem item = new CoolItem(this, SWT.NONE);",
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
		CoolBarInfo coolBar =
				openJavaInfo(
						"public class Test extends CoolBar {",
						"  public Test(Composite parent, int style) {",
						"    super(parent, style);",
						"  }",
						"}");
		//
		loadCreationTool("org.eclipse.swt.widgets.CoolItem");
		tree.moveOn(coolBar);
		tree.click();
		assertEditor(
				"public class Test extends CoolBar {",
				"  public Test(Composite parent, int style) {",
				"    super(parent, style);",
				"    {",
				"      CoolItem coolItem = new CoolItem(this, SWT.NONE);",
				"    }",
				"  }",
				"}");
	}

	@Test
	public void test_tree_CREATE_control_good() throws Exception {
		openJavaInfo(
				"public class Test extends CoolBar {",
				"  public Test(Composite parent, int style) {",
				"    super(parent, style);",
				"    {",
				"      CoolItem item = new CoolItem(this, SWT.NONE);",
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
				"public class Test extends CoolBar {",
				"  public Test(Composite parent, int style) {",
				"    super(parent, style);",
				"    {",
				"      CoolItem item = new CoolItem(this, SWT.NONE);",
				"      {",
				"        Button button = new Button(this, SWT.NONE);",
				"        item.setControl(button);",
				"      }",
				"    }",
				"  }",
				"}");
	}

	@Test
	public void test_tree_CREATE_control_alreadyHasControl() throws Exception {
		openJavaInfo(
				"public class Test extends CoolBar {",
				"  public Test(Composite parent, int style) {",
				"    super(parent, style);",
				"    {",
				"      CoolItem item = new CoolItem(this, SWT.NONE);",
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
