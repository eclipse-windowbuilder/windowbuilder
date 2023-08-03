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
package org.eclipse.wb.tests.designer.rcp.model.forms;

import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;

import org.junit.Test;

/**
 * Test for {@link FormText}.
 *
 * @author scheglov_ke
 */
public class FormTextTest extends AbstractFormsTest {
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
	 * Test for using {@link FormToolkit#createFormText(Composite, boolean)}.
	 * <p>
	 * Problem is that it requires "com.ibm.icu" plugin, that is not included into list.
	 */
	@Test
	public void test_create() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  private final FormToolkit m_toolkit = new FormToolkit(Display.getDefault());",
						"  public Test() {",
						"    setLayout(new RowLayout());",
						"    FormText formText = m_toolkit.createFormText(this, true);",
						"    formText.setText('abc', false, false);",
						"  }",
						"}");
		shell.refresh();
	}

	/**
	 * {@link FormText} creates {@link Image} with its size, so throws exception when it is zero.
	 */
	@Test
	public void test_zeroSize() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    FormText formText = new FormText(this, SWT.NONE);",
						"  }",
						"}");
		shell.refresh();
		assertNoErrors(shell);
	}
}