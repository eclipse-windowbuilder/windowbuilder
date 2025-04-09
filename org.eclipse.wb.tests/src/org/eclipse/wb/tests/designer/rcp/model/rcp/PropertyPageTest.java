/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
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
package org.eclipse.wb.tests.designer.rcp.model.rcp;

import org.eclipse.wb.internal.rcp.model.rcp.PropertyPageInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 * Test for {@link PropertyPageInfo}.
 *
 * @author scheglov_ke
 */
public class PropertyPageTest extends RcpModelTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_0() throws Exception {
		PropertyPageInfo page =
				parseJavaInfo(
						"import org.eclipse.ui.dialogs.*;",
						"public class Test extends PropertyPage {",
						"  public Test() {",
						"  }",
						"  public Control createContents(Composite parent) {",
						"    Composite container = new Composite(parent, SWT.NULL);",
						"    return container;",
						"  }",
						"}");
		// check hierarchy
		assertHierarchy(
				"{this: org.eclipse.ui.dialogs.PropertyPage} {this} {}",
				"  {parameter} {parent} {/new Composite(parent, SWT.NULL)/}",
				"    {new: org.eclipse.swt.widgets.Composite} {local-unique: container} {/new Composite(parent, SWT.NULL)/ /container/}",
				"      {implicit-layout: absolute} {implicit-layout} {}");
		CompositeInfo parentComposite = page.getChildren(CompositeInfo.class).get(0);
		CompositeInfo container = (CompositeInfo) parentComposite.getChildrenControls().get(0);
		// refresh()
		page.refresh();
		assertNoErrors(page);
		// check bounds
		assertEquals(page.getBounds().width, 600);
		assertEquals(page.getBounds().height, 500);
		Assertions.assertThat(parentComposite.getBounds().width).isGreaterThan(300);
		Assertions.assertThat(parentComposite.getBounds().height).isGreaterThan(30);
		Assertions.assertThat(container.getBounds().width).isGreaterThan(300);
		Assertions.assertThat(container.getBounds().height).isGreaterThan(230);
	}
}