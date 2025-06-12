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
package org.eclipse.wb.tests.designer.rcp.model.jface;

import org.eclipse.wb.internal.rcp.model.jface.PreferencePageInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link PreferencePageInfo}.
 *
 * @author scheglov_ke
 */
public class PreferencePageTest extends RcpModelTest {
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
		PreferencePageInfo preferencePage =
				parseJavaInfo(
						"import org.eclipse.jface.preference.*;",
						"public class Test extends PreferencePage {",
						"  public Test() {",
						"  }",
						"  public Control createContents(Composite parent) {",
						"    Composite container = new Composite(parent, SWT.NULL);",
						"    return container;",
						"  }",
						"}");
		// check hierarchy
		assertHierarchy(
				"{this: org.eclipse.jface.preference.PreferencePage} {this} {}",
				"  {parameter} {parent} {/new Composite(parent, SWT.NULL)/}",
				"    {new: org.eclipse.swt.widgets.Composite} {local-unique: container} {/new Composite(parent, SWT.NULL)/ /container/}",
				"      {implicit-layout: absolute} {implicit-layout} {}");
		CompositeInfo parentComposite = preferencePage.getChildren(CompositeInfo.class).get(0);
		CompositeInfo container = (CompositeInfo) parentComposite.getChildrenControls().get(0);
		// refresh()
		preferencePage.refresh();
		// check bounds
		assertEquals(preferencePage.getBounds().width, 600);
		assertEquals(preferencePage.getBounds().height, 500);
		Assertions.assertThat(parentComposite.getBounds().width).isGreaterThan(300);
		Assertions.assertThat(parentComposite.getBounds().height).isGreaterThan(30);
		Assertions.assertThat(container.getBounds().width).isGreaterThan(300);
		Assertions.assertThat(container.getBounds().height).isGreaterThan(230);
		// set new bounds
		preferencePage.getTopBoundsSupport().setSize(500, 400);
		preferencePage.refresh();
		assertEquals(preferencePage.getBounds().width, 500);
		assertEquals(preferencePage.getBounds().height, 400);
	}
}