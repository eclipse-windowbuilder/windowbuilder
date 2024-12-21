/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.designer.swt.support;

import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.internal.swt.model.widgets.ShellInfo;
import org.eclipse.wb.internal.swt.support.CoordinateUtils;
import org.eclipse.wb.tests.designer.Expectations;
import org.eclipse.wb.tests.designer.Expectations.InsValue;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;

import org.assertj.core.api.Assertions;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests for SWT {@link CoordinateUtils}.
 *
 * @author scheglov_ke
 */
public class CoordinateUtilsTest extends RcpModelTest {
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
	// Disabled because of https://github.com/eclipse-windowbuilder/windowbuilder/issues/389
	@Ignore
	@Test
	public void test_1() throws Exception {
		ShellInfo shell = (ShellInfo) parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    Button button = new Button(this, SWT.NONE);",
						"    button.setBounds(10, 20, 50, 30);",
						"  }",
						"}");
		ControlInfo button = shell.getChildrenControls().get(0);
		shell.refresh();
		// On GTK, the bounds are only calculated if the shell is visible
		if (EnvironmentUtils.IS_LINUX) {
			Shell swtShell = shell.getWidget();
			swtShell.setVisible(true);
			waitEventLoop(10);
		}
		// Shell location
		Point shellLocation = CoordinateUtils.getDisplayLocation(shell.getObject());
		// The shell is located at (0, 0), when invisible
		if (EnvironmentUtils.IS_WINDOWS) {
			assertEquals(new Point(-10000, -10000), shellLocation);
		}
		// Shell client area insets
		Insets shellInsets = CoordinateUtils.getClientAreaInsets(shell.getWidget());
		assertTrue(shellInsets.left == shellInsets.right);
		Assertions.assertThat(shellInsets.left).isGreaterThanOrEqualTo(0);
		Assertions.assertThat(shellInsets.top).isGreaterThanOrEqualTo(15);
		// Button location
		{
			Point buttonLocation = CoordinateUtils.getDisplayLocation(button.getObject());
			assertEquals(shellLocation.x + shellInsets.left + 10, buttonLocation.x);
			assertEquals(shellLocation.y + shellInsets.top + 20, buttonLocation.y);
		}
		// getBounds() for Button relative to Shell
		{
			Rectangle bounds = CoordinateUtils.getBounds(shell.getObject(), button.getObject());
			assertEquals(shellInsets.left + 10, bounds.x);
			assertEquals(shellInsets.top + 20, bounds.y);
			assertEquals(50, bounds.width);
			assertEquals(30, bounds.height);
		}
	}

	/**
	 * Test for {@link CoordinateUtils#getClientAreaInsets2(Object)} for {@link Group}.
	 */
	@Ignore
	@Test
	public void test_withGroup() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new FillLayout());",
						"    Group group = new Group(this, SWT.NONE);",
						"  }",
						"}");
		shell.refresh();
		CompositeInfo group = (CompositeInfo) shell.getChildrenControls().get(0);
		//
		Insets insets =
				Expectations.get(new Insets(15, 3, 3, 3), new InsValue[]{
						new InsValue("flanker-windows", new Insets(15, 3, 3, 3)),
						new InsValue("scheglov-win", new Insets(15, 3, 3, 3))});
		assertEquals(insets, CoordinateUtils.getClientAreaInsets2(group.getWidget()));
	}
}