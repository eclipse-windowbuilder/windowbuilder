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

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.internal.xwt.model.forms.FormInfo;
import org.eclipse.wb.tests.designer.XWT.gef.XwtGefTest;

import org.junit.Test;

/**
 * Test for {@link FormInfo} in GEF.
 *
 * @author scheglov_ke
 */
public class FormGefTest extends XwtGefTest {
	private FormInfo composite;

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
	// Canvas, CREATE
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_canvas_CREATE_headClient() throws Exception {
		prepare_CREATE();
		// use canvas
		canvas.target(composite).in(-10, 10).move();
		canvas.click();
		assertXML(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<!-- Forms API -->",
				"<Form text='My Form'>",
				"  <Form.body/>",
				"  <Form.headClient>",
				"    <Button/>",
				"  </Form.headClient>",
				"</Form>");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Tree
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_tree_CREATE_headClient() throws Exception {
		prepare_CREATE();
		// use tree
		EditPart position = tree.getEditPart(composite).getChildren().get(0);
		tree.moveOn(position).click();
		assertXML(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<!-- Forms API -->",
				"<Form text='My Form'>",
				"  <Form.body/>",
				"  <Form.headClient>",
				"    <Button/>",
				"  </Form.headClient>",
				"</Form>");
	}

	private FormInfo prepare_CREATE() throws Exception {
		composite =
				openEditor(
						"// filler filler filler filler filler",
						"// filler filler filler filler filler",
						"<!-- Forms API -->",
						"<Form text='My Form'/>");
		// create Button
		loadButton();
		canvas.create(0, 0);
		// use this Form_Info
		return composite;
	}
}
