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

import org.eclipse.wb.internal.rcp.model.widgets.CBannerInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.junit.Test;

/**
 * Test for {@link CBannerInfo}.
 *
 * @author scheglov_ke
 */
public class CBannerTest extends RcpModelTest {
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
	public void test_parse() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new FillLayout());",
						"    CBanner banner = new CBanner(this, SWT.NONE);",
						"  }",
						"}");
		shell.refresh();
		CBannerInfo banner = (CBannerInfo) shell.getChildrenControls().get(0);
		// no "Style" property
		assertNull(banner.getPropertyByTitle("Style"));
	}
}