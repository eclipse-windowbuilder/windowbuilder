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

import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.rcp.model.jface.DialogPageInfo;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.swt.layout.FillLayout;

import org.junit.Test;

/**
 * Test for {@link DialogPageInfo}.
 *
 * @author scheglov_ke
 */
public class DialogPageTest extends RcpModelTest {
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
	/**
	 * {@link DialogPage} is not {@link AbstractComponentInfo}, so there was problem that we don't
	 * have "active" {@link JavaInfo} during parsing.
	 */
	@Test
	public void test_empty() throws Exception {
		DialogPageInfo dialog =
				parseJavaInfo(
						"import org.eclipse.jface.dialogs.*;",
						"public class Test extends org.eclipse.jface.dialogs.DialogPage {",
						"  public Test() {",
						"    setTitle('My title');",
						"  }",
						"  public void createControl(Composite parent) {",
						"  }",
						"}");
		assertHierarchy(
				"{this: org.eclipse.jface.dialogs.DialogPage} {this} {/setTitle('My title')/}",
				"  {parameter} {parent} {}");
		// refresh() also should be successful
		dialog.refresh();
		assertNotNull(dialog.getImage());
		assertEquals(600, dialog.getBounds().width);
		assertEquals(500, dialog.getBounds().height);
	}

	@Test
	public void test_emptyContainer() throws Exception {
		DialogPageInfo dialog =
				parseJavaInfo(
						"import org.eclipse.jface.dialogs.*;",
						"public class Test extends org.eclipse.jface.dialogs.DialogPage {",
						"  public Test() {",
						"    setTitle('My title');",
						"  }",
						"  public void createControl(Composite parent) {",
						"    Composite container = new Composite(parent, SWT.NONE);",
						"    setControl(container);",
						"  }",
						"}");
		assertHierarchy(
				"{this: org.eclipse.jface.dialogs.DialogPage} {this} {/setTitle('My title')/ /setControl(container)/}",
				"  {parameter} {parent} {/new Composite(parent, SWT.NONE)/}",
				"    {new: org.eclipse.swt.widgets.Composite} {local-unique: container} {/new Composite(parent, SWT.NONE)/ /setControl(container)/}",
				"      {implicit-layout: absolute} {implicit-layout} {}");
		// refresh() also should be successful
		dialog.refresh();
		assertNotNull(dialog.getImage());
		assertEquals(600, dialog.getBounds().width);
		assertEquals(500, dialog.getBounds().height);
	}

	/**
	 * We can not know what set of "Layout" and "LayoutData" user wants to use for this
	 * {@link DialogPage} and its parent. We use {@link FillLayout} for "parent", so we should ensure
	 * that "container" does not have incompatible "LayoutData". Easiest way - just clear
	 * "LayoutData".
	 */
	@Test
	public void test_containerLayoutData() throws Exception {
		parseJavaInfo(
				"import org.eclipse.jface.dialogs.*;",
				"public class Test extends org.eclipse.jface.dialogs.DialogPage {",
				"  public Test() {",
				"    setTitle('My title');",
				"  }",
				"  public void createControl(Composite parent) {",
				"    Composite container = new Composite(parent, SWT.NONE);",
				"    container.setLayoutData(new GridData());",
				"    setControl(container);",
				"  }",
				"}");
		refresh();
	}
}