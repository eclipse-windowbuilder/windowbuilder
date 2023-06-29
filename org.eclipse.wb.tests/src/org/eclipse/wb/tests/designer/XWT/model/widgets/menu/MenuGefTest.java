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
package org.eclipse.wb.tests.designer.XWT.model.widgets.menu;

import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.xwt.model.widgets.ShellInfo;
import org.eclipse.wb.internal.xwt.model.widgets.menu.MenuInfo;
import org.eclipse.wb.tests.designer.XWT.gef.XwtGefTest;

/**
 * Test for {@link MenuInfo} in GEF.
 *
 * @author scheglov_ke
 */
public class MenuGefTest extends XwtGefTest {
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
	public void test_CREATE_notMenu() throws Exception {
		ShellInfo shell = openEditor("<Shell/>");
		//
		loadCreationTool("java.lang.Object");
		{
			canvas.moveTo(shell, 0.5, 0.5);
			canvas.assertCommandNull();
		}
		{
			tree.moveOn(shell);
			tree.assertCommandNull();
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// BAR
	//
	////////////////////////////////////////////////////////////////////////////
	public void test_bar_CREATE_canvas() throws Exception {
		ShellInfo shell = openEditor("<Shell/>");
		//
		loadCreationTool("org.eclipse.swt.widgets.Menu", "bar");
		canvas.moveTo(shell, 0.5, 0.5);
		canvas.assertCommandNotNull();
		canvas.click();
		assertXML(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Shell.menuBar>",
				"    <Menu x:Style='BAR'/>",
				"  </Shell.menuBar>",
				"</Shell>");
	}

	public void test_bar_CREATE_tree() throws Exception {
		ShellInfo shell = openEditor("<Shell/>");
		//
		loadCreationTool("org.eclipse.swt.widgets.Menu", "bar");
		tree.moveOn(shell);
		tree.assertCommandNotNull();
		tree.click();
		assertXML(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Shell.menuBar>",
				"    <Menu x:Style='BAR'/>",
				"  </Shell.menuBar>",
				"</Shell>");
	}

	public void test_bar_CREATE_hasBar() throws Exception {
		ShellInfo shell =
				openEditor(
						"// filler filler filler filler filler",
						"// filler filler filler filler filler",
						"<Shell>",
						"  <Shell.menuBar>",
						"    <Menu x:Style='BAR'/>",
						"  </Shell.menuBar>",
						"</Shell>");
		//
		loadCreationTool("org.eclipse.swt.widgets.Menu", "bar");
		{
			canvas.moveTo(shell, 0.5, 0.5);
			canvas.assertCommandNull();
		}
		{
			tree.moveOn(shell);
			tree.assertCommandNull();
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// POP_UP
	//
	////////////////////////////////////////////////////////////////////////////
	public void test_popup_CREATE_canvas() throws Exception {
		ShellInfo shell = openEditor("<Shell/>");
		//
		loadCreationTool("org.eclipse.swt.widgets.Menu");
		canvas.moveTo(shell, 0.5, 0.5);
		canvas.assertFeedbacks(canvas.getTargetPredicate(shell));
		canvas.assertCommandNotNull();
		canvas.click();
		assertXML(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Shell.menu>",
				"    <Menu/>",
				"  </Shell.menu>",
				"</Shell>");
	}

	public void test_popup_CREATE_tree() throws Exception {
		ShellInfo shell = openEditor("<Shell/>");
		//
		loadCreationTool("org.eclipse.swt.widgets.Menu");
		tree.moveOn(shell);
		tree.assertCommandNotNull();
		tree.click();
		assertXML(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Shell.menu>",
				"    <Menu/>",
				"  </Shell.menu>",
				"</Shell>");
	}

	public void test_popup_CREATE_hasPopup() throws Exception {
		ShellInfo shell =
				openEditor(
						"// filler filler filler filler filler",
						"// filler filler filler filler filler",
						"<Shell>",
						"  <Shell.menu>",
						"    <Menu/>",
						"  </Shell.menu>",
						"</Shell>");
		//
		loadCreationTool("org.eclipse.swt.widgets.Menu");
		{
			canvas.moveTo(shell, 0.5, 0.5);
			canvas.assertFeedbacks(canvas.getTargetPredicate(shell));
			canvas.assertCommandNull();
		}
		{
			tree.moveOn(shell);
			tree.assertCommandNull();
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// MenuItem
	//
	////////////////////////////////////////////////////////////////////////////
	public void test_barItem_CREATE_canvas() throws Exception {
		openEditor(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Shell.menuBar>",
				"    <Menu x:Style='BAR'>",
				"      <MenuItem wbp:name='item' text='Existing item'/>",
				"    </Menu>",
				"  </Shell.menuBar>",
				"</Shell>");
		XmlObjectInfo item = getObjectByName("item");
		//
		XmlObjectInfo newItem = loadCreationTool("org.eclipse.swt.widgets.MenuItem");
		canvas.moveTo(item, 0.1, 0.5);
		canvas.assertCommandNotNull();
		canvas.click();
		assertXML(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Shell.menuBar>",
				"    <Menu x:Style='BAR'>",
				"      <MenuItem text='New Item'/>",
				"      <MenuItem wbp:name='item' text='Existing item'/>",
				"    </Menu>",
				"  </Shell.menuBar>",
				"</Shell>");
		canvas.assertPrimarySelected(newItem);
	}

	public void test_barItem_CREATE_tree() throws Exception {
		openEditor(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Shell.menuBar>",
				"    <Menu x:Style='BAR'>",
				"      <MenuItem wbp:name='item' text='Existing item'/>",
				"    </Menu>",
				"  </Shell.menuBar>",
				"</Shell>");
		XmlObjectInfo item = getObjectByName("item");
		//
		XmlObjectInfo newItem = loadCreationTool("org.eclipse.swt.widgets.MenuItem");
		tree.moveBefore(item);
		tree.assertCommandNotNull();
		tree.click();
		assertXML(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Shell.menuBar>",
				"    <Menu x:Style='BAR'>",
				"      <MenuItem text='New Item'/>",
				"      <MenuItem wbp:name='item' text='Existing item'/>",
				"    </Menu>",
				"  </Shell.menuBar>",
				"</Shell>");
		tree.assertPrimarySelected(newItem);
	}
}
