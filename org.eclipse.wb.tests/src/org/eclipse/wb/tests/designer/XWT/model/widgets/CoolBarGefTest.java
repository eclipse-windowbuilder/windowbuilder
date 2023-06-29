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
package org.eclipse.wb.tests.designer.XWT.model.widgets;

import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.xwt.model.widgets.CoolBarInfo;
import org.eclipse.wb.tests.designer.XWT.gef.XwtGefTest;

/**
 * Test for {@link CoolBarInfo} in GEF.
 *
 * @author scheglov_ke
 */
public class CoolBarGefTest extends XwtGefTest {
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
	// Canvas
	//
	////////////////////////////////////////////////////////////////////////////
	public void test_canvas_CREATE_item() throws Exception {
		CoolBarInfo toolBar = openEditor("<CoolBar/>");
		//
		loadCreationTool("org.eclipse.swt.widgets.CoolItem");
		canvas.moveTo(toolBar, 5, 5);
		canvas.click();
		assertXML(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<CoolBar>",
				"  <CoolItem/>",
				"</CoolBar>");
	}

	public void test_canvas_CREATE_control_good() throws Exception {
		openEditor(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<CoolBar>",
				"  <CoolItem wbp:name='item' size='100, 50'/>",
				"</CoolBar>");
		XmlObjectInfo item = getObjectByName("item");
		//
		loadButton();
		canvas.moveTo(item, 5, 5);
		canvas.assertFeedbacks(canvas.getTargetPredicate(item));
		canvas.assertCommandNotNull();
		canvas.click();
		assertXML(
				"// filler filler filler filler filler",
				"<CoolBar>",
				"  <CoolItem wbp:name='item' size='100, 50'>",
				"    <CoolItem.control>",
				"      <Button/>",
				"    </CoolItem.control>",
				"  </CoolItem>",
				"</CoolBar>");
	}

	public void test_canvas_CREATE_control_alreadyHasControl() throws Exception {
		openEditor(
				"// filler filler filler filler filler",
				"<CoolBar>",
				"  <CoolItem wbp:name='item' size='100, 50'>",
				"    <CoolItem.control>",
				"      <Button/>",
				"    </CoolItem.control>",
				"  </CoolItem>",
				"</CoolBar>");
		XmlObjectInfo item = getObjectByName("item");
		//
		loadButton();
		canvas.moveTo(item, 5, 5);
		canvas.assertFeedbacks(canvas.getTargetPredicate(item));
		canvas.assertCommandNull();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Tree
	//
	////////////////////////////////////////////////////////////////////////////
	public void test_tree_CREATE_item() throws Exception {
		CoolBarInfo toolBar = openEditor("<CoolBar/>");
		//
		loadCreationTool("org.eclipse.swt.widgets.CoolItem");
		tree.moveOn(toolBar);
		tree.click();
		assertXML(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<CoolBar>",
				"  <CoolItem/>",
				"</CoolBar>");
	}

	public void test_tree_CREATE_control_good() throws Exception {
		openEditor(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<CoolBar>",
				"  <CoolItem wbp:name='item' size='100, 50'/>",
				"</CoolBar>");
		XmlObjectInfo item = getObjectByName("item");
		//
		loadButton();
		tree.moveOn(item);
		tree.assertFeedback_on(item);
		tree.assertCommandNotNull();
		tree.click();
		assertXML(
				"// filler filler filler filler filler",
				"<CoolBar>",
				"  <CoolItem wbp:name='item' size='100, 50'>",
				"    <CoolItem.control>",
				"      <Button/>",
				"    </CoolItem.control>",
				"  </CoolItem>",
				"</CoolBar>");
	}

	public void test_tree_CREATE_control_alreadyHasControl() throws Exception {
		openEditor(
				"// filler filler filler filler filler",
				"<CoolBar>",
				"  <CoolItem wbp:name='item' size='100, 50'>",
				"    <CoolItem.control>",
				"      <Button/>",
				"    </CoolItem.control>",
				"  </CoolItem>",
				"</CoolBar>");
		XmlObjectInfo item = getObjectByName("item");
		//
		loadButton();
		tree.moveOn(item);
		tree.assertFeedback_on(item);
		tree.assertCommandNull();
	}
}
