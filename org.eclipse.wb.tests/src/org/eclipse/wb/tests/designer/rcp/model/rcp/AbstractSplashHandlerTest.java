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
package org.eclipse.wb.tests.designer.rcp.model.rcp;

import org.eclipse.wb.internal.rcp.model.rcp.AbstractSplashHandlerInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 * Test for {@link AbstractSplashHandlerInfo}.
 *
 * @author scheglov_ke
 */
public class AbstractSplashHandlerTest extends RcpModelTest {
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
	public void test_0() throws Exception {
		AbstractSplashHandlerInfo splash =
				parseJavaInfo(
						"import org.eclipse.ui.splash.AbstractSplashHandler;",
						"public class Test extends AbstractSplashHandler {",
						"  public Test() {",
						"  }",
						"  public void init(Shell splash) {",
						"    super.init(splash);",
						"    Composite container = new Composite(getSplash(), SWT.NULL);",
						"  }",
						"}");
		assertHierarchy(
				"{this: org.eclipse.ui.splash.AbstractSplashHandler} {this} {/new Composite(getSplash(), SWT.NULL)/}",
				"  {parameter} {splash} {/super.init(splash)/}",
				"    {new: org.eclipse.swt.widgets.Composite} {local-unique: container} {/new Composite(getSplash(), SWT.NULL)/}",
				"      {implicit-layout: absolute} {implicit-layout} {}");
		// refresh()
		splash.refresh();
		assertNoErrors(splash);
	}

	/**
	 * Test for {@link AbstractSplashHandler_TopBoundsSupport}.
	 *
	 * @throws Exception
	 */
	@Test
	public void test_topBoundsSupport() throws Exception {
		AbstractSplashHandlerInfo splash =
				parseJavaInfo(
						"import org.eclipse.ui.splash.AbstractSplashHandler;",
						"public class Test extends AbstractSplashHandler {",
						"  public Test() {",
						"  }",
						"  public void init(Shell splash) {",
						"    super.init(splash);",
						"    Composite container = new Composite(getSplash(), SWT.NULL);",
						"  }",
						"}");
		splash.refresh();
		CompositeInfo container = getJavaInfoByName("container");
		// check bounds
		assertEquals(splash.getBounds().width, 450);
		assertEquals(splash.getBounds().height, 300);
		Assertions.assertThat(container.getBounds().width).isGreaterThan(400);
		Assertions.assertThat(container.getBounds().height).isGreaterThan(230);
		// set bounds
		splash.getTopBoundsSupport().setSize(600, 500);
		splash.refresh();
		assertEquals(splash.getBounds().width, 600);
		assertEquals(splash.getBounds().height, 500);
	}
}