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
package org.eclipse.wb.tests.designer.rcp.model.forms;

import org.eclipse.wb.internal.rcp.model.forms.MasterDetailsBlockInfo;

import org.junit.jupiter.api.Test;

/**
 * Test for {@link MasterDetailsBlockInfo}.
 *
 * @author scheglov_ke
 */
public class MasterDetailsBlockTest extends AbstractFormsTest {
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
		MasterDetailsBlockInfo page =
				parseJavaInfo(
						"public abstract class Test extends MasterDetailsBlock {",
						"  private FormToolkit m_toolkit;",
						"  public Test() {",
						"  }",
						"  protected void createMasterPart(IManagedForm managedForm, Composite parent) {",
						"    m_toolkit = managedForm.getToolkit();",
						"    Composite composite = m_toolkit.createComposite(parent, SWT.NONE);",
						"  }",
						"  protected void registerPages(DetailsPart part) {",
						"  }",
						"  protected void createToolBarActions(IManagedForm managedForm) {",
						"  }",
						"}");
		assertHierarchy(
				"{this: org.eclipse.ui.forms.MasterDetailsBlock} {this} {}",
				"  {parameter} {parent} {/m_toolkit.createComposite(parent, SWT.NONE)/}",
				"    {instance factory: {toolkitAccess} createComposite(org.eclipse.swt.widgets.Composite,int)} {local-unique: composite} {/m_toolkit.createComposite(parent, SWT.NONE)/}",
				"      {implicit-layout: absolute} {implicit-layout} {}",
				"  {instance factory container}",
				"    {toolkitAccess: m_toolkit} {toolkitAccess} {/m_toolkit.createComposite(parent, SWT.NONE)/ /managedForm.getToolkit()/}");
		// refresh
		page.refresh();
		assertNoErrors(page);
		assertEquals(page.getBounds().width, 600);
		assertEquals(page.getBounds().height, 500);
		// Set new size.
		// This test intentionally commented.
		// ScrolledForm performs re-flow in async, so requires running messages loop,
		// but we can not do this, because GEF canvas will start painting, and we don't have images, etc.
		/*{
    	page.getTopBoundsSupport().setSize(450, 300);
    	page.refresh();
    	assertEquals(page.getBounds().width, 450);
    	assertEquals(page.getBounds().height, 300);
    }*/
	}
}