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
import org.eclipse.wb.internal.xwt.model.forms.ExpandableCompositeInfo;
import org.eclipse.wb.tests.designer.XWT.gef.XwtGefTest;

import org.junit.Test;

/**
 * Test for {@link ExpandableCompositeInfo} in GEF.
 *
 * @author scheglov_ke
 */
public class ExpandableCompositeGefTest extends XwtGefTest {
	private ExpandableCompositeInfo composite;

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
	public void test_canvas_CREATE_textClient() throws Exception {
		prepare_canvas_CREATE();
		// use canvas
		canvas.target(composite).in(-0.1, 0.1).move();
		canvas.click();
		assertXML(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<!-- Forms API -->",
				"<ExpandableComposite>",
				"  <ExpandableComposite.textClient>",
				"    <Button/>",
				"  </ExpandableComposite.textClient>",
				"</ExpandableComposite>");
	}

	@Test
	public void test_canvas_CREATE_client() throws Exception {
		prepare_canvas_CREATE();
		// use canvas
		canvas.target(composite).in(0.5, 0.5).move();
		canvas.click();
		assertXML(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<!-- Forms API -->",
				"<ExpandableComposite>",
				"  <ExpandableComposite.client>",
				"    <Button/>",
				"  </ExpandableComposite.client>",
				"</ExpandableComposite>");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// CREATE
	//
	////////////////////////////////////////////////////////////////////////////
	private ExpandableCompositeInfo prepare_canvas_CREATE() throws Exception {
		composite =
				openEditor(
						"// filler filler filler filler filler",
						"// filler filler filler filler filler",
						"<!-- Forms API -->",
						"<ExpandableComposite/>");
		// create Button
		loadButton();
		canvas.create(0, 0);
		// use this ExpandableComposite_Info
		return composite;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Tree
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_tree_CREATE_left() throws Exception {
		prepare_canvas_CREATE();
		// use tree
		EditPart position = tree.getEditPart(composite).getChildren().get(1);
		tree.moveOn(position).click();
		assertXML(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<!-- Forms API -->",
				"<ExpandableComposite>",
				"  <ExpandableComposite.client>",
				"    <Button/>",
				"  </ExpandableComposite.client>",
				"</ExpandableComposite>");
	}
}
