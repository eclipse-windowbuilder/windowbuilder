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
package org.eclipse.wb.tests.designer.XWT.model.forms;

import org.eclipse.wb.internal.xwt.model.forms.ScrolledFormInfo;
import org.eclipse.wb.internal.xwt.model.widgets.CompositeInfo;
import org.eclipse.wb.tests.designer.XWT.model.XwtModelTest;

import org.junit.Test;

/**
 * Test for {@link ScrolledFormInfo}.
 *
 * @author scheglov_ke
 */
public class ScrolledFormTest extends XwtModelTest {
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
		parse(
				"<!-- Forms API -->",
				"<Shell>",
				"  <Shell.layout>",
				"    <FillLayout/>",
				"  </Shell.layout>",
				"  <ScrolledForm wbp:name='form'/>",
				"</Shell>");
		assertHierarchy(
				"// filler filler filler filler filler",
				"<Shell>",
				"  <FillLayout>",
				"  <ScrolledForm wbp:name='form'>",
				"    <ScrolledForm.form?>",
				"    <ScrolledForm.body?>",
				"      implicit-layout: absolute");
		refresh();
	}

	@Test
	public void test_create() throws Exception {
		CompositeInfo shell =
				parse(
						"<!-- Forms API -->",
						"<Shell>",
						"  <Shell.layout>",
						"    <FillLayout/>",
						"  </Shell.layout>",
						"</Shell>");
		refresh();
		//
		ScrolledFormInfo newForm = createObject("org.eclipse.ui.forms.widgets.ScrolledForm");
		shell.getLayout().command_CREATE(newForm, null);
		assertXML(
				"<!-- Forms API -->",
				"<Shell>",
				"  <Shell.layout>",
				"    <FillLayout/>",
				"  </Shell.layout>",
				"  <ScrolledForm>",
				"    <ScrolledForm.form text='New ScrolledForm'/>",
				"  </ScrolledForm>",
				"</Shell>");
		assertHierarchy(
				"// filler filler filler filler filler",
				"<Shell>",
				"  <FillLayout>",
				"  <ScrolledForm>",
				"    <ScrolledForm.form text='New ScrolledForm'>",
				"    <ScrolledForm.body?>",
				"      implicit-layout: absolute");
	}
}
