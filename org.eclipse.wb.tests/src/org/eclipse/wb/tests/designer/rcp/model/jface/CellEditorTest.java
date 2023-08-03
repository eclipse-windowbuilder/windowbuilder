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

import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.swt.widgets.Control;

import org.junit.Test;

/**
 * Test for {@link CellEditor} support.
 *
 * @author scheglov_ke
 */
public class CellEditorTest extends RcpModelTest {
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
	public void test_TextCellEditor() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    TextCellEditor textCellEditor = new TextCellEditor(this, SWT.BORDER);",
						"  }",
						"}");
		assertHierarchy(
				"{this: org.eclipse.swt.widgets.Shell} {this} {/new TextCellEditor(this, SWT.BORDER)/}",
				"  {implicit-layout: absolute} {implicit-layout} {}",
				"  {viewer: public org.eclipse.swt.widgets.Control org.eclipse.jface.viewers.CellEditor.getControl()} {viewer} {}",
				"    {new: org.eclipse.jface.viewers.TextCellEditor} {local-unique: textCellEditor} {/new TextCellEditor(this, SWT.BORDER)/}");
		// refresh()
		shell.refresh();
		assertNoErrors(shell);
	}

	/**
	 * {@link CheckboxCellEditor} has no {@link Control}, so can not be used as viewer.
	 */
	@Test
	public void test_CheckboxCellEditor() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    CheckboxCellEditor cellEditor = new CheckboxCellEditor(this, SWT.NONE);",
						"  }",
						"}");
		assertHierarchy(
				"{this: org.eclipse.swt.widgets.Shell} {this} {/new CheckboxCellEditor(this, SWT.NONE)/}",
				"  {implicit-layout: absolute} {implicit-layout} {}");
		// refresh()
		shell.refresh();
		assertNoErrors(shell);
	}

	/**
	 * {@link ComboBoxCellEditor} constructor has no items (for #setItems).
	 */
	@Test
	public void test_ComboBoxCellEditor() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    ComboBoxCellEditor cellEditor = new ComboBoxCellEditor(this, null);",
						"  }",
						"}");
		assertHierarchy(
				"{this: org.eclipse.swt.widgets.Shell} {this} {/new ComboBoxCellEditor(this, null)/}",
				"  {implicit-layout: absolute} {implicit-layout} {}",
				"  {viewer: public org.eclipse.swt.widgets.Control org.eclipse.jface.viewers.CellEditor.getControl()} {viewer} {}",
				"    {new: org.eclipse.jface.viewers.ComboBoxCellEditor} {local-unique: cellEditor} {/new ComboBoxCellEditor(this, null)/}");
		// refresh()
		shell.refresh();
		assertNoErrors(shell);
	}
}