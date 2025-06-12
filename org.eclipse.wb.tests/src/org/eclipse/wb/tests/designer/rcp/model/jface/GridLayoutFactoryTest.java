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

import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import org.junit.jupiter.api.Test;

/**
 * Test for {@link GridLayoutFactory}.
 *
 * @author scheglov_ke
 */
public class GridLayoutFactoryTest extends RcpModelTest {
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
	public void test_GridLayoutFactory() throws Exception {
		CompositeInfo composite =
				parseJavaInfo(
						"import org.eclipse.jface.layout.*;",
						"public class Test extends Shell {",
						"  public Test() {",
						"    GridLayoutFactory.swtDefaults().margins(10, 20).applyTo(this);",
						"  }",
						"}");
		assertHierarchy(
				"{this: org.eclipse.swt.widgets.Shell} {this}"
						+ " {/GridLayoutFactory.swtDefaults().margins(10, 20).applyTo(this)/}",
						"  {implicit-layout: absolute} {implicit-layout} {}",
						"  {instance factory container}",
						"    {static factory: org.eclipse.jface.layout.GridLayoutFactory swtDefaults()} {empty}"
								+ " {/GridLayoutFactory.swtDefaults().margins(10, 20)/ /GridLayoutFactory.swtDefaults().margins(10, 20).applyTo(this)/}");
		refresh();
		// check that GridLayout has same values as configured
		Composite compositeObject = composite.getWidget();
		GridLayout layout = (GridLayout) compositeObject.getLayout();
		assertEquals(10, layout.marginWidth);
		assertEquals(20, layout.marginHeight);
	}

	@Test
	public void test_GridDataFactory() throws Exception {
		parseJavaInfo(
				"import org.eclipse.jface.layout.*;",
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new GridLayout());",
				"    {",
				"      Button button = new Button(this, SWT.NONE);",
				"      GridDataFactory.swtDefaults().hint(150, 50).applyTo(button);",
				"    }",
				"  }",
				"}");
		assertHierarchy(
				"{this: org.eclipse.swt.widgets.Shell} {this} {/setLayout(new GridLayout())/ /new Button(this, SWT.NONE)/}",
				"  {new: org.eclipse.swt.layout.GridLayout} {empty} {/setLayout(new GridLayout())/}",
				"  {new: org.eclipse.swt.widgets.Button} {local-unique: button} {/new Button(this, SWT.NONE)/"
						+ " /GridDataFactory.swtDefaults().hint(150, 50).applyTo(button)/}",
						"    {virtual-layout_data: org.eclipse.swt.layout.GridData} {virtual-layout-data} {}",
						"  {instance factory container}",
						"    {static factory: org.eclipse.jface.layout.GridDataFactory swtDefaults()} {empty}"
								+ " {/GridDataFactory.swtDefaults().hint(150, 50)/ /GridDataFactory.swtDefaults().hint(150, 50).applyTo(button)/}");
		refresh();
		ControlInfo button = getJavaInfoByName("button");
		// check that GridData has same values as configured
		GridData gridData = (GridData) button.getWidget().getLayoutData();
		assertEquals(150, gridData.widthHint);
		assertEquals(50, gridData.heightHint);
	}
}