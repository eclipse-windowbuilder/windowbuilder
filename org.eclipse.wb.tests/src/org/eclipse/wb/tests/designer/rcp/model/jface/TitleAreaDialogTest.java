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
package org.eclipse.wb.tests.designer.rcp.model.jface;

import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.variable.ThisVariableSupport;
import org.eclipse.wb.internal.rcp.model.jface.TitleAreaDialogInfo;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.widgets.Composite;

import org.junit.Test;

/**
 * Test for {@link TitleAreaDialogInfo}.
 *
 * @author scheglov_ke
 */
public class TitleAreaDialogTest extends RcpModelTest {
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
	 * {@link TitleAreaDialog} with {@link TitleAreaDialog#createDialogArea(Composite)} method.<br>
	 * Parameter "parent" in <code>createDialogArea()</code> should not have layout.
	 */
	@Test
	public void test_0() throws Exception {
		parseJavaInfo(
				"import org.eclipse.jface.dialogs.*;",
				"public class Test extends org.eclipse.jface.dialogs.TitleAreaDialog {",
				"  public Test(Shell parentShell) {",
				"    super(parentShell);",
				"  }",
				"  protected Control createDialogArea(Composite parent) {",
				"    Composite container = (Composite) super.createDialogArea(parent);",
				"    Button button = new Button(container, SWT.NONE);",
				"    return container;",
				"  }",
				"}");
		assertHierarchy(
				"{this: org.eclipse.jface.dialogs.TitleAreaDialog} {this} {}",
				"  {parameter} {parent} {/super.createDialogArea(parent)/}",
				"    {casted-superInvocation: (Composite)super.createDialogArea(parent)} {local-unique: container} {/(Composite) super.createDialogArea(parent)/ /new Button(container, SWT.NONE)/ /container/}",
				"      {implicit-layout: org.eclipse.swt.layout.GridLayout} {implicit-layout} {}",
				"      {new: org.eclipse.swt.widgets.Button} {local-unique: button} {/new Button(container, SWT.NONE)/}",
				"        {virtual-layout_data: org.eclipse.swt.layout.GridData} {virtual-layout-data} {}");
	}

	/**
	 * In {@link TitleAreaDialog} title/message/titleImage properties should be set in
	 * {@link TitleAreaDialog#createDialogArea(Composite)}, because at this point "title area" is
	 * already created. Bad news is that {@link ThisVariableSupport} thinks that constructor is good
	 * place for any "this" properties.
	 */
	@Test
	public void test_titleAreaProperties() throws Exception {
		TitleAreaDialogInfo dialog =
				parseJavaInfo(
						"import org.eclipse.jface.dialogs.*;",
						"public class Test extends org.eclipse.jface.dialogs.TitleAreaDialog {",
						"  public Test(Shell parentShell) {",
						"    super(parentShell);",
						"  }",
						"  protected Control createDialogArea(Composite parent) {",
						"    Composite container = (Composite) super.createDialogArea(parent);",
						"    return container;",
						"  }",
						"}");
		// set properties
		dialog.getPropertyByTitle("title").setValue("The title.");
		dialog.getPropertyByTitle("message").setValue("The message.");
		((GenericProperty) dialog.getPropertyByTitle("titleImage")).setExpression(
				"null",
				Property.UNKNOWN_VALUE);
		assertEditor(
				"import org.eclipse.jface.dialogs.*;",
				"public class Test extends org.eclipse.jface.dialogs.TitleAreaDialog {",
				"  public Test(Shell parentShell) {",
				"    super(parentShell);",
				"  }",
				"  protected Control createDialogArea(Composite parent) {",
				"    setTitleImage(null);",
				"    setMessage('The message.');",
				"    setTitle('The title.');",
				"    Composite container = (Composite) super.createDialogArea(parent);",
				"    return container;",
				"  }",
				"}");
	}
}