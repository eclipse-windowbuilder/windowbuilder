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

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;

import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.gef.core.tools.PasteTool;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.internal.swt.model.widgets.menu.MenuInfo;
import org.eclipse.wb.tests.designer.rcp.RcpGefTest;

/**
 * Tests for "bar" and "popup" menu, create/move them.
 *
 * @author scheglov_ke
 */
public class MenuBarPopupTest extends RcpGefTest {
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
	// Popup
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * We can drop new "popup".
	 */
	public void test_popupCreate() throws Exception {
		CompositeInfo shellInfo =
				openComposite(
						"// filler filler filler",
						"public class Test extends Shell {",
						"  public Test() {",
						"  }",
						"}");
		// begin creating "popup" Menu
		MenuInfo popupInfo = loadCreationTool("org.eclipse.swt.widgets.Menu");
		// initially no feedbacks
		canvas.assertNoFeedbacks();
		// move on "shell": target feedback appears
		canvas.moveTo(shellInfo, 0, 0);
		canvas.assertFeedbacks(canvas.getTargetPredicate(shellInfo));
		// click, so drop "popup"
		canvas.click();
		canvas.assertNoFeedbacks();
		assertEditor(
				"// filler filler filler",
				"public class Test extends Shell {",
				"  public Test() {",
				"    {",
				"      Menu menu = new Menu(this);",
				"      setMenu(menu);",
				"    }",
				"  }",
				"}");
		// "popup" is selected and drop-down visible
		{
			GraphicalEditPart popupPart = canvas.getEditPart(popupInfo);
			canvas.assertPrimarySelected(popupInfo);
			canvas.assertChildrenCount(popupPart, 1);
		}
	}

	/**
	 * We can not drop new "popup", if there is already one.
	 */
	public void test_popupCreate_alreadyExists() throws Exception {
		CompositeInfo shellInfo =
				openComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setMenu(new Menu(this));",
						"  }",
						"}");
		String source = m_lastEditor.getSource();
		// begin creating "popup" Menu
		loadCreationTool("org.eclipse.swt.widgets.Menu");
		// move on "shell": target feedback appears
		canvas.moveTo(shellInfo, 0, 0);
		canvas.assertFeedbacks(canvas.getTargetPredicate(shellInfo));
		canvas.assertCommandNull();
		// click, nothing changed
		canvas.click();
		assertEditor(source, m_lastEditor);
	}

	/**
	 * Test for moving "popup" from one control to another.
	 */
	public void test_popupMove_otherControl() throws Exception {
		CompositeInfo shellInfo =
				openComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new RowLayout());",
						"    {",
						"      Button button_1 = new Button(this, SWT.NONE);",
						"      button_1.setText('Button 1');",
						"      {",
						"        Menu menu = new Menu(button_1);",
						"        button_1.setMenu(menu);",
						"      }",
						"    }",
						"    {",
						"      Button button_2 = new Button(this, SWT.NONE);",
						"      button_2.setText('Button 2');",
						"    }",
						"  }",
						"}");
		ControlInfo buttonInfo_1 = shellInfo.getChildrenControls().get(0);
		ControlInfo buttonInfo_2 = shellInfo.getChildrenControls().get(1);
		MenuInfo popupInfo = buttonInfo_1.getChildren(MenuInfo.class).get(0);
		// prepare EditPart's
		// move "popup" on "button_2"
		{
			canvas.beginDrag(popupInfo).dragTo(buttonInfo_2);
			// target on "button_2"
			canvas.assertFeedbacks(canvas.getTargetPredicate(buttonInfo_2));
			// no feedbacks
			canvas.endDrag();
			canvas.assertNoFeedbacks();
		}
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new RowLayout());",
				"    {",
				"      Button button_1 = new Button(this, SWT.NONE);",
				"      button_1.setText('Button 1');",
				"    }",
				"    {",
				"      Button button_2 = new Button(this, SWT.NONE);",
				"      button_2.setText('Button 2');",
				"      {",
				"        Menu menu = new Menu(button_2);",
				"        button_2.setMenu(menu);",
				"      }",
				"    }",
				"  }",
				"}");
	}

	/**
	 * We can not move "popup" inside of same control.
	 */
	public void test_popupMove_sameControl() throws Exception {
		CompositeInfo shellInfo =
				openComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    Menu menu = new Menu(this);",
						"    setMenu(menu);",
						"  }",
						"}");
		String source = m_lastEditor.getSource();
		MenuInfo popupInfo = shellInfo.getChildren(MenuInfo.class).get(0);
		// prepare EditPart's
		// try to move "popup" inside of "shell"
		{
			canvas.beginDrag(popupInfo).dragTo(shellInfo, 100, 100);
			// target on "shell", command is "null"
			canvas.assertFeedbacks(canvas.getTargetPredicate(shellInfo));
			canvas.assertCommandNull();
			// end drag
			canvas.endDrag();
			canvas.assertNoFeedbacks();
		}
		// nothing changed
		assertEditor(source, m_lastEditor);
	}

	/**
	 * Test for copy/paste "popup".
	 */
	public void DISABLE_test_popupPaste() throws Exception {
		CompositeInfo shellInfo =
				openComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setLayout(new RowLayout());",
						"    {",
						"      Button button_1 = new Button(this, SWT.NONE);",
						"      button_1.setText('Button 1');",
						"      {",
						"        Menu menu = new Menu(button_1);",
						"        button_1.setMenu(menu);",
						"        {",
						"          MenuItem item = new MenuItem(menu, SWT.NONE);",
						"          item.setText('My item');",
						"        }",
						"      }",
						"    }",
						"    {",
						"      Button button_2 = new Button(this, SWT.NONE);",
						"      button_2.setText('Button 2');",
						"    }",
						"  }",
						"}");
		ControlInfo buttonInfo_1 = shellInfo.getChildrenControls().get(0);
		ControlInfo buttonInfo_2 = shellInfo.getChildrenControls().get(1);
		MenuInfo popupInfo = buttonInfo_1.getChildren(MenuInfo.class).get(0);
		// load "paste" tool
		{
			JavaInfoMemento memento = JavaInfoMemento.createMemento(popupInfo);
			PasteTool pasteTool = new PasteTool(ImmutableList.of(memento));
			m_viewerCanvas.getEditDomain().setActiveTool(pasteTool);
		}
		// move on "button_2": target feedback
		{
			canvas.moveTo(buttonInfo_2, 0, 0);
			canvas.assertFeedbacks(canvas.getTargetPredicate(buttonInfo_2));
		}
		// do click
		canvas.click();
		assertEditor(
				"public class Test extends Shell {",
				"  public Test() {",
				"    setLayout(new RowLayout());",
				"    {",
				"      Button button_1 = new Button(this, SWT.NONE);",
				"      button_1.setText('Button 1');",
				"      {",
				"        Menu menu = new Menu(button_1);",
				"        button_1.setMenu(menu);",
				"        {",
				"          MenuItem item = new MenuItem(menu, SWT.NONE);",
				"          item.setText('My item');",
				"        }",
				"      }",
				"    }",
				"    {",
				"      Button button_2 = new Button(this, SWT.NONE);",
				"      button_2.setText('Button 2');",
				"      {",
				"        Menu menu = new Menu(button_2);",
				"        button_2.setMenu(menu);",
				"        {",
				"          MenuItem menuItem = new MenuItem(menu, SWT.NONE);",
				"          menuItem.setText('My item');",
				"        }",
				"      }",
				"    }",
				"  }",
				"}");
		// new pasted "popup" should be selected
		{
			MenuInfo newPopup = buttonInfo_2.getChildren(MenuInfo.class).get(0);
			canvas.assertPrimarySelected(newPopup);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Bar
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * We can drop new "bar".
	 */
	public void test_barCreate() throws Exception {
		CompositeInfo shellInfo =
				openComposite(
						"// filler filler filler",
						"public class Test extends Shell {",
						"  public Test() {",
						"  }",
						"}");
		// begin creating "bar" Menu
		MenuInfo barInfo = (MenuInfo) loadCreationTool("org.eclipse.swt.widgets.Menu", "bar");
		// initially no feedbacks
		canvas.assertNoFeedbacks();
		// move on "shell": target feedback appears
		canvas.moveTo(shellInfo, 0, 0);
		canvas.assertFeedbacks(new Predicate<Figure>() {
			@Override
			public boolean apply(Figure t) {
				return t.getSize().width > 200;
			}
		});
		// click, so drop "bar"
		canvas.click();
		canvas.assertNoFeedbacks();
		assertEditor(
				"// filler filler filler",
				"public class Test extends Shell {",
				"  public Test() {",
				"    {",
				"      Menu menu = new Menu(this, SWT.BAR);",
				"      setMenuBar(menu);",
				"    }",
				"  }",
				"}");
		canvas.assertPrimarySelected(barInfo);
	}

	/**
	 * We can not drop new "bar", if there is already one.
	 */
	public void test_barCreate_alreadyExists() throws Exception {
		CompositeInfo shellInfo =
				openComposite(
						"public class Test extends Shell {",
						"  public Test() {",
						"    setMenuBar(new Menu(this, SWT.BAR));",
						"  }",
						"}");
		String source = m_lastEditor.getSource();
		// begin creating "bar" Menu
		loadCreationTool("org.eclipse.swt.widgets.Menu", "bar");
		// move on "shell": target feedback appears
		canvas.moveTo(shellInfo, 0, 0);
		canvas.assertNoFeedbacks();
		canvas.assertCommandNull();
		// click, nothing changed
		canvas.click();
		assertEditor(source, m_lastEditor);
	}
}
