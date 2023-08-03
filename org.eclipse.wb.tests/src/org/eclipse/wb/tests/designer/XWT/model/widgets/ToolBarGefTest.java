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
import org.eclipse.wb.internal.xwt.model.widgets.ToolBarInfo;
import org.eclipse.wb.tests.designer.XWT.gef.XwtGefTest;

import org.junit.Test;

/**
 * Test for {@link ToolBarInfo} in GEF.
 *
 * @author scheglov_ke
 */
public class ToolBarGefTest extends XwtGefTest {
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
	@Test
	public void test_canvas_CREATE_item() throws Exception {
		ToolBarInfo toolBar = openEditor("<ToolBar/>");
		//
		loadCreationTool("org.eclipse.swt.widgets.ToolItem");
		canvas.moveTo(toolBar, 5, 5);
		canvas.click();
		assertXML(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<ToolBar>",
				"  <ToolItem text='New Item'/>",
				"</ToolBar>");
	}

	@Test
	public void test_canvas_CREATE_control_good() throws Exception {
		openEditor(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<ToolBar>",
				"  <ToolItem wbp:name='item' x:Style='SEPARATOR' width='100'/>",
				"</ToolBar>");
		XmlObjectInfo item = getObjectByName("item");
		//
		loadButton();
		canvas.moveTo(item, 5, 5);
		canvas.assertFeedbacks(canvas.getTargetPredicate(item));
		canvas.assertCommandNotNull();
		canvas.click();
		assertXML(
				"// filler filler filler filler filler",
				"<ToolBar>",
				"  <ToolItem wbp:name='item' x:Style='SEPARATOR' width='100'>",
				"    <ToolItem.control>",
				"      <Button/>",
				"    </ToolItem.control>",
				"  </ToolItem>",
				"</ToolBar>");
	}

	@Test
	public void test_canvas_CREATE_control_notSeparator() throws Exception {
		openEditor(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<ToolBar>",
				"  <ToolItem wbp:name='item' width='100'/>",
				"</ToolBar>");
		XmlObjectInfo item = getObjectByName("item");
		//
		loadButton();
		canvas.moveTo(item, 5, 5);
		canvas.assertNoFeedbacks();
		canvas.assertCommandNull();
	}

	@Test
	public void test_canvas_CREATE_control_alreadyHasControl() throws Exception {
		openEditor(
				"// filler filler filler filler filler",
				"<ToolBar>",
				"  <ToolItem wbp:name='item' x:Style='SEPARATOR' width='100'>",
				"    <ToolItem.control>",
				"      <Button/>",
				"    </ToolItem.control>",
				"  </ToolItem>",
				"</ToolBar>");
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
	@Test
	public void test_tree_CREATE_item() throws Exception {
		ToolBarInfo toolBar = openEditor("<ToolBar/>");
		//
		loadCreationTool("org.eclipse.swt.widgets.ToolItem");
		tree.moveOn(toolBar);
		tree.click();
		assertXML(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<ToolBar>",
				"  <ToolItem text='New Item'/>",
				"</ToolBar>");
	}

	@Test
	public void test_tree_CREATE_control_good() throws Exception {
		openEditor(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<ToolBar>",
				"  <ToolItem wbp:name='item' x:Style='SEPARATOR' width='100'/>",
				"</ToolBar>");
		XmlObjectInfo item = getObjectByName("item");
		//
		loadButton();
		tree.moveOn(item);
		tree.assertFeedback_on(item);
		tree.assertCommandNotNull();
		tree.click();
		assertXML(
				"// filler filler filler filler filler",
				"<ToolBar>",
				"  <ToolItem wbp:name='item' x:Style='SEPARATOR' width='100'>",
				"    <ToolItem.control>",
				"      <Button/>",
				"    </ToolItem.control>",
				"  </ToolItem>",
				"</ToolBar>");
	}

	@Test
	public void test_tree_CREATE_control_notSeparator() throws Exception {
		openEditor(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<ToolBar>",
				"  <ToolItem wbp:name='item' width='100'/>",
				"</ToolBar>");
		XmlObjectInfo item = getObjectByName("item");
		//
		loadButton();
		tree.moveOn(item);
		tree.assertCommandNull();
	}

	@Test
	public void test_tree_CREATE_control_alreadyHasControl() throws Exception {
		openEditor(
				"// filler filler filler filler filler",
				"<ToolBar>",
				"  <ToolItem wbp:name='item' x:Style='SEPARATOR' width='100'>",
				"    <ToolItem.control>",
				"      <Button/>",
				"    </ToolItem.control>",
				"  </ToolItem>",
				"</ToolBar>");
		XmlObjectInfo item = getObjectByName("item");
		//
		loadButton();
		tree.moveOn(item);
		tree.assertFeedback_on(item);
		tree.assertCommandNull();
	}
}
