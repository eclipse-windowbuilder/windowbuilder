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
package org.eclipse.wb.tests.designer.rcp.model.jface;

import org.eclipse.wb.internal.rcp.model.jface.WindowInfo;
import org.eclipse.wb.internal.rcp.model.jface.WindowTopBoundsSupport;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.jface.window.Window;

import org.junit.jupiter.api.Test;

/**
 * Test for {@link WindowTopBoundsSupport}.
 *
 * @author scheglov_ke
 */
public class WindowTopBoundsSupportTest extends RcpModelTest {
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
	 * Test for {@link WindowTopBoundsSupport#apply()}.
	 * <p>
	 * {@link Window} with default source, i.e. no any special method.
	 */
	@Test
	public void test_apply_defaultSize() throws Exception {
		WindowInfo window =
				parseJavaInfo(
						"import org.eclipse.jface.window.*;",
						"public class Test extends Window {",
						"  public Test(Shell parentShell) {",
						"    super(parentShell);",
						"  }",
						"}");
		window.refresh();
		// check size
		assertEquals(new Dimension(450, 300), window.getBounds().getSize());
	}

	/**
	 * Test for {@link WindowTopBoundsSupport#apply()}.
	 * <p>
	 * {@link Window} with <code>getInitialSize()</code>.
	 */
	@Test
	public void test_apply_getInitialSize() throws Exception {
		WindowInfo window =
				parseJavaInfo(
						"import org.eclipse.jface.window.*;",
						"public class Test extends Window {",
						"  public Test(Shell parentShell) {",
						"    super(parentShell);",
						"  }",
						"  protected Point getInitialSize() {",
						"    return new Point(500, 300);",
						"  }",
						"}");
		window.refresh();
		// check size
		assertEquals(new Dimension(500, 300), window.getBounds().getSize());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// setSize()
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link WindowTopBoundsSupport#setSize(int, int)}.
	 * <p>
	 * {@link Window} with default source, i.e. no any special method.
	 */
	@Test
	public void test_setSize_defaultSize() throws Exception {
		WindowInfo window =
				parseJavaInfo(
						"import org.eclipse.jface.window.*;",
						"public class Test extends Window {",
						"  public Test(Shell parentShell) {",
						"    super(parentShell);",
						"  }",
						"}");
		window.refresh();
		assertEquals(new Dimension(450, 300), window.getBounds().getSize());
		// set size
		window.getTopBoundsSupport().setSize(200, 200);
		window.refresh();
		assertEquals(new Dimension(200, 200), window.getBounds().getSize());
	}

	/**
	 * Test for {@link WindowTopBoundsSupport#setSize(int, int)}.
	 * <p>
	 * {@link Window} with <code>getInitialSize()</code>.
	 */
	@Test
	public void test_setSize_getInitialSize() throws Exception {
		WindowInfo window =
				parseJavaInfo(
						"import org.eclipse.jface.window.*;",
						"public class Test extends Window {",
						"  public Test(Shell parentShell) {",
						"    super(parentShell);",
						"  }",
						"  protected Point getInitialSize() {",
						"    return new Point(500, 300);",
						"  }",
						"}");
		window.refresh();
		assertEquals(new Dimension(500, 300), window.getBounds().getSize());
		// set size
		window.getTopBoundsSupport().setSize(200, 200);
		window.refresh();
		assertEquals(new Dimension(200, 200), window.getBounds().getSize());
		assertEditor(
				"import org.eclipse.jface.window.*;",
				"public class Test extends Window {",
				"  public Test(Shell parentShell) {",
				"    super(parentShell);",
				"  }",
				"  protected Point getInitialSize() {",
				"    return new Point(200, 200);",
				"  }",
				"}");
	}
}