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

import org.eclipse.wb.internal.rcp.model.widgets.DialogInfo;
import org.eclipse.wb.internal.rcp.model.widgets.DialogTopBoundsSupport;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.widgets.Dialog;

import org.junit.jupiter.api.Test;

/**
 * Test for {@link DialogTopBoundsSupport}.
 *
 * @author sablin_aa
 */
public class DialogTopBoundsSupportTest extends RcpModelTest {
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
	// apply()
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link DialogTopBoundsSupport#apply()}.
	 * <p>
	 * {@link Dialog} with default source, i.e. no any special sizing method.
	 */
	@Test
	public void test_defaultSize() throws Exception {
		DialogInfo dialog =
				parseJavaInfo(
						"public class Test extends Dialog {",
						"  protected Object result;",
						"  protected Shell shell;",
						"  public Test(Shell parent, int style) {",
						"    super(parent, style);",
						"  }",
						"  public Object open() {",
						"    shell = new Shell(getParent(), getStyle());",
						"    return result;",
						"  }",
						"}");
		dialog.refresh();
		// check size
		assertEquals(new Dimension(450, 300), dialog.getBounds().getSize());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// setSize()
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link DialogTopBoundsSupport#setSize(int, int)}.
	 * <p>
	 * {@link Dialog} without <code>setSize()</code>.
	 */
	@Test
	public void test_setSize_add() throws Exception {
		DialogInfo dialog =
				parseJavaInfo(
						"public class Test extends Dialog {",
						"  protected Object result;",
						"  protected Shell shell;",
						"  public Test(Shell parent, int style) {",
						"    super(parent, style);",
						"  }",
						"  public Object open() {",
						"    shell = new Shell(getParent(), getStyle());",
						"    return result;",
						"  }",
						"}");
		dialog.refresh();
		assertEquals(new Dimension(450, 300), dialog.getBounds().getSize());
		// set size
		dialog.getTopBoundsSupport().setSize(200, 200);
		dialog.refresh();
		assertEquals(new Dimension(200, 200), dialog.getBounds().getSize());
		assertEditor(
				"public class Test extends Dialog {",
				"  protected Object result;",
				"  protected Shell shell;",
				"  public Test(Shell parent, int style) {",
				"    super(parent, style);",
				"  }",
				"  public Object open() {",
				"    shell = new Shell(getParent(), getStyle());",
				"    shell.setSize(200, 200);",
				"    return result;",
				"  }",
				"}");
	}

	/**
	 * Test for {@link DialogTopBoundsSupport#setSize(int, int)}.
	 * <p>
	 * {@link Dialog} with <code>setSize()</code>.
	 */
	@Test
	public void test_setSize_update() throws Exception {
		DialogInfo dialog =
				parseJavaInfo(
						"public class Test extends Dialog {",
						"  protected Object result;",
						"  protected Shell shell;",
						"  public Test(Shell parent, int style) {",
						"    super(parent, style);",
						"  }",
						"  public Object open() {",
						"    shell = new Shell(getParent(), getStyle());",
						"    shell.setSize(200, 200);",
						"    return result;",
						"  }",
						"}");
		dialog.refresh();
		assertEquals(new Dimension(200, 200), dialog.getBounds().getSize());
		// set size
		dialog.getTopBoundsSupport().setSize(300, 300);
		dialog.refresh();
		assertEquals(new Dimension(300, 300), dialog.getBounds().getSize());
		assertEditor(
				"public class Test extends Dialog {",
				"  protected Object result;",
				"  protected Shell shell;",
				"  public Test(Shell parent, int style) {",
				"    super(parent, style);",
				"  }",
				"  public Object open() {",
				"    shell = new Shell(getParent(), getStyle());",
				"    shell.setSize(300, 300);",
				"    return result;",
				"  }",
				"}");
	}
}