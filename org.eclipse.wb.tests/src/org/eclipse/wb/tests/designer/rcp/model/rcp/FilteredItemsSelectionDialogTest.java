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
package org.eclipse.wb.tests.designer.rcp.model.rcp;

import org.eclipse.wb.internal.rcp.model.jface.DialogInfo;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

/**
 * Test for <code>FilteredItemsSelectionDialog</code>.
 *
 * @author scheglov_ke
 */
public class FilteredItemsSelectionDialogTest extends RcpModelTest {
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
	 * Test for parsing.
	 */
	public void test_parse() throws Exception {
		DialogInfo dialog =
				parseJavaInfo(
						"import org.eclipse.jface.dialogs.*;",
						"public abstract class Test extends org.eclipse.ui.dialogs.FilteredItemsSelectionDialog {",
						"  public Test(Shell parentShell) {",
						"    super(parentShell);",
						"  }",
						"  protected Control createDialogArea(Composite parent) {",
						"    Composite container = (Composite) super.createDialogArea(parent);",
						"    return container;",
						"  }",
						"}");
		assertHierarchy(
				"{this: org.eclipse.ui.dialogs.FilteredItemsSelectionDialog} {this} {}",
				"  {parameter} {parent} {/super.createDialogArea(parent)/}",
				"    {casted-superInvocation: (Composite)super.createDialogArea(parent)} {local-unique: container} {/(Composite) super.createDialogArea(parent)/ /container/}",
				"      {implicit-layout: org.eclipse.swt.layout.GridLayout} {implicit-layout} {}");
		dialog.refresh();
		assertNoErrors(dialog);
	}
}