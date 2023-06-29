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
package org.eclipse.wb.tests.designer.swt.model.menu;

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.internal.swt.model.widgets.TableColumnInfo;
import org.eclipse.wb.internal.swt.model.widgets.menu.MenuInfo;
import org.eclipse.wb.tests.designer.rcp.RcpGefTest;

/**
 * Tests found bugs with menu.
 *
 * @author scheglov_ke
 */
public class MenuProblemsTest extends RcpGefTest {
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
	 * Drop new "bar" and new "item" on it.<br>
	 * When form has component that requires executing "async" runnables, this may interfere with
	 * menu.
	 */
	public void test_AsyncMessagesSupport_andMenuGEF() throws Exception {
		setFileContentSrc(
				"test/MyButton.java",
				getTestSource2(
						"public class MyButton extends Button {",
						"  public MyButton(Composite parent, int style) {",
						"    super(parent, style);",
						"  }",
						"  protected void checkSubclass() {",
						"  }",
						"}"));
		setFileContentSrc(
				"test/MyButton.wbp-component.xml",
				getSourceDQ(
						"<?xml version='1.0' encoding='UTF-8'?>",
						"<component xmlns='http://www.eclipse.org/wb/WBPComponent'>",
						"  <parameters>",
						"    <parameter name='SWT.runAsyncMessages'>true</parameter>",
						"  </parameters>",
						"</component>"));
		waitForAutoBuild();
		// open editor
		CompositeInfo shellInfo =
				openComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    new MyButton(this, SWT.NONE);",
						"  }",
						"}");
		// prepare models
		GraphicalEditPart shellPart = canvas.getEditPart(shellInfo);
		// drop new "bar"
		GraphicalEditPart barPart;
		{
			MenuInfo barInfo = (MenuInfo) loadCreationTool("org.eclipse.swt.widgets.Menu", "bar");
			canvas.moveTo(shellPart);
			canvas.click();
			assertEditor(
					"public class Test extends Shell {",
					"  public Test() {",
					"    new MyButton(this, SWT.NONE);",
					"    {",
					"      Menu menu = new Menu(this, SWT.BAR);",
					"      setMenuBar(menu);",
					"    }",
					"  }",
					"}");
			barPart = canvas.getEditPart(barInfo);
		}
		// drop new "item"
		{
			loadCreationTool("org.eclipse.swt.widgets.MenuItem");
			canvas.moveTo(barPart);
			canvas.click();
			assertEditor(
					"public class Test extends Shell {",
					"  public Test() {",
					"    new MyButton(this, SWT.NONE);",
					"    {",
					"      Menu menu = new Menu(this, SWT.BAR);",
					"      setMenuBar(menu);",
					"      {",
					"        MenuItem menuItem = new MenuItem(menu, SWT.NONE);",
					"        menuItem.setText('New Item');",
					"      }",
					"    }",
					"  }",
					"}");
		}
	}

	/**
	 * Ensure that in RCP cascade menu also works.
	 *
	 * @throws Exception
	 */
	public void test_cascadeSubMenu_andRCP() throws Exception {
		CompositeInfo shellInfo =
				openComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    {",
						"      Menu menu = new Menu(this, SWT.BAR);",
						"      setMenuBar(menu);",
						"    }",
						"  }",
						"}");
		MenuInfo barInfo = shellInfo.getChildren(MenuInfo.class).get(0);
		GraphicalEditPart barPart = canvas.getEditPart(barInfo);
		// drop new "cascade" item
		{
			loadCreationTool("org.eclipse.swt.widgets.MenuItem", "cascade");
			canvas.moveTo(barPart);
			canvas.click();
			assertEditor(
					"public class Test extends Shell {",
					"  public Test() {",
					"    {",
					"      Menu menu = new Menu(this, SWT.BAR);",
					"      setMenuBar(menu);",
					"      {",
					"        MenuItem menuItem = new MenuItem(menu, SWT.CASCADE);",
					"        menuItem.setText('New SubMenu');",
					"        {",
					"          Menu menu_1 = new Menu(menuItem);",
					"          menuItem.setMenu(menu_1);",
					"        }",
					"      }",
					"    }",
					"  }",
					"}");
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Popup menu on Table and TableColumn at same place
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * This case is not problem, because {@link EditPart} for {@link MenuInfo} is first, so we click
	 * it.
	 */
	public void test_TableColumn_PopupMenu_menuFirst() throws Exception {
		check_TableColumn_PopupMenu(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new FillLayout());",
				"    {",
				"      Table table = new Table(this, SWT.BORDER);",
				"      table.setHeaderVisible(true);",
				"      {",
				"        Menu popup = new Menu(table);",
				"        table.setMenu(popup);",
				"      }",
				"      {",
				"        TableColumn tableColumn = new TableColumn(table, SWT.NONE);",
				"        tableColumn.setWidth(100);",
				"      }",
				"    }",
				"  }",
				"}");
	}

	/**
	 * {@link EditPart} of {@link MenuInfo} is second, so we should place it on menu primary layer to
	 * make it above {@link TableColumnInfo}.
	 */
	public void test_TableColumn_PopupMenu_menuSecond() throws Exception {
		check_TableColumn_PopupMenu(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new FillLayout());",
				"    {",
				"      Table table = new Table(this, SWT.BORDER);",
				"      table.setHeaderVisible(true);",
				"      {",
				"        TableColumn tableColumn = new TableColumn(table, SWT.NONE);",
				"        tableColumn.setWidth(100);",
				"      }",
				"      {",
				"        Menu popup = new Menu(table);",
				"        table.setMenu(popup);",
				"      }",
				"    }",
				"  }",
				"}");
	}

	private void check_TableColumn_PopupMenu(String... lines) throws Exception {
		CompositeInfo shell = openComposite(lines);
		ControlInfo table = shell.getChildrenControls().get(0);
		MenuInfo popup = table.getChildren(MenuInfo.class).get(0);
		// click on "popup"
		canvas.target(popup).in(10, 10).move().click();
		canvas.assertPrimarySelected(popup);
	}
}
