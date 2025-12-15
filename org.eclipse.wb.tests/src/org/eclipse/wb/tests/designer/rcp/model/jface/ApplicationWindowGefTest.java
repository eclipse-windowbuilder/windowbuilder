/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.designer.rcp.model.jface;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.rcp.RcpToolkitDescription;
import org.eclipse.wb.internal.rcp.model.jface.ApplicationWindowInfo;
import org.eclipse.wb.internal.rcp.model.jface.action.ActionInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ShellInfo;
import org.eclipse.wb.internal.swt.model.widgets.menu.MenuInfo;
import org.eclipse.wb.internal.swt.utils.ManagerUtils;
import org.eclipse.wb.tests.designer.TestUtils;
import org.eclipse.wb.tests.designer.core.annotations.DisposeProjectAfter;
import org.eclipse.wb.tests.designer.rcp.RcpGefTest;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ApplicationWindowInfo} in GEF.
 *
 * @author scheglov_ke
 */
public class ApplicationWindowGefTest extends RcpGefTest {
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
	// Bar
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * We can drop new "bar" on empty {@link Shell}.
	 */
	@Test
	public void test_barCreate_Shell() throws Exception {
		CompositeInfo shell =
				openComposite(
						"// filler filler filler",
						"public class Test extends Shell {",
						"  public Test() {",
						"  }",
						"}");
		// begin creating "bar" Menu
		MenuInfo newMenu = (MenuInfo) loadCreationTool("org.eclipse.swt.widgets.Menu", "bar");
		// initially no feedbacks
		canvas.assertNoFeedbacks();
		// move on "shell": target feedback appears
		canvas.moveTo(shell);
		canvas.assertFeedbacks(t -> t.getSize().width > 200);
		// click, so drop "newMenu"
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
		canvas.assertPrimarySelected(newMenu);
	}

	/**
	 * We can not drop new "bar" on {@link Shell} in {@link ApplicationWindowInfo}.
	 */
	@Test
	public void test_barCreate_ApplicationWindow() throws Exception {
		ApplicationWindowInfo window =
				(ApplicationWindowInfo) openJavaInfo(
						"import org.eclipse.jface.window.*;",
						"public class Test extends ApplicationWindow {",
						"  public Test(Shell parentShell) {",
						"    super(parentShell);",
						"  }",
						"  protected void configureShell(Shell newShell) {",
						"    super.configureShell(newShell);",
						"  }",
						"}");
		String source = m_lastEditor.getSource();
		ShellInfo shell = window.getChildren(ShellInfo.class).get(0);
		// canvas
		{
			loadCreationTool("org.eclipse.swt.widgets.Menu", "bar");
			// move on "shell": target feedback appears
			canvas.target(shell).in(100, 100).move();
			canvas.assertNoFeedbacks();
			canvas.assertCommandNull();
			// click, nothing changed
			canvas.click();
			assertEditor(source, m_lastEditor);
		}
		// tree
		{
			loadCreationTool("org.eclipse.swt.widgets.Menu", "bar");
			// move on "shell": target feedback appears
			tree.moveOn(shell);
			tree.assertFeedback_empty();
			tree.assertCommandNull();
			// click, nothing changed
			tree.click();
			assertEditor(source, m_lastEditor);
		}
	}

	/**
	 * When we click on {@link ApplicationWindow}, we hit "parent" parameter for
	 * <code>createContents()</code> , because it is same {@link Shell} as used for control of
	 * {@link ApplicationWindow}. But this is not what user expects - it expects that
	 * {@link ApplicationWindow} itself will be selected and ready for resize.
	 */
	@Test
	public void test_clickOn_createContents_parent() throws Exception {
		ApplicationWindowInfo window =
				(ApplicationWindowInfo) openJavaInfo(
						"import org.eclipse.jface.window.*;",
						"public class Test extends ApplicationWindow {",
						"  public Test(Shell parentShell) {",
						"    super(parentShell);",
						"  }",
						"  protected Control createContents(Composite parent) {",
						"    Composite container = new Composite(parent, SWT.NONE);",
						"    return container;",
						"  }",
						"}");
		assertHierarchy(
				"{this: org.eclipse.jface.window.ApplicationWindow} {this} {}",
				"  {parameter} {parent} {/new Composite(parent, SWT.NONE)/}",
				"    {new: org.eclipse.swt.widgets.Composite} {local-unique: container} {/new Composite(parent, SWT.NONE)/ /container/}",
				"      {implicit-layout: absolute} {implicit-layout} {}");
		// click on "window", ensure that it is really selected
		canvas.target(window).in(100, 1).move().click();
		canvas.assertPrimarySelected(window);
	}

	/**
	 * When we click on {@link Window}, we hit "newShell" parameter for <code>configureShell()</code>
	 * , because it is same {@link Shell} as used for control of {@link Window}. But this is not what
	 * user expects - it expects that {@link Window} itself will be selected and ready for resize.
	 */
	@Test
	public void test_clickOn_configureShell_newShell() throws Exception {
		ApplicationWindowInfo window =
				(ApplicationWindowInfo) openJavaInfo(
						"import org.eclipse.jface.window.*;",
						"public class Test extends ApplicationWindow {",
						"  public Test(Shell parentShell) {",
						"    super(parentShell);",
						"  }",
						"  protected void configureShell(Shell newShell) {",
						"    super.configureShell(newShell);",
						"  }",
						"}");
		assertHierarchy(
				"{this: org.eclipse.jface.window.ApplicationWindow} {this} {}",
				"  {parameter} {newShell} {/super.configureShell(newShell)/}");
		// click on "window", ensure that it is really selected
		canvas.target(window).in(100, 10).move().click();
		canvas.assertPrimarySelected(window);
	}

	/**
	 * When we perform {@link ObjectInfo#refresh()} it is possible, that icon of {@link ObjectInfo}
	 * may become disposed. This should not cause exception.
	 */
	@DisposeProjectAfter
	@Test
	public void test_disposedImageOfAction() throws Exception {
		ManagerUtils.ensure_ResourceManager(m_javaProject, RcpToolkitDescription.INSTANCE);
		TestUtils.createImagePNG(m_testProject, "src/test/images/test.png", 16, 16);
		//
		openJavaInfo(
				"import org.eclipse.jface.action.*;",
				"import org.eclipse.jface.window.*;",
				"public class Test extends ApplicationWindow {",
				"  private IAction m_action;",
				"  public Test(Shell parentShell) {",
				"    super(parentShell);",
				"    createActions();",
				"  }",
				"  private void createActions() {",
				"    {",
				"      m_action = new Action('The text') {",
				"        public void run() {",
				"        }",
				"      };",
				"      m_action.setImageDescriptor(org.eclipse.wb.swt.ResourceManager.getImageDescriptor(Test.class,"
						+ " 'images/test.png'));",
						"    }",
						"  }",
				"}");
		ActionInfo action = getJavaInfoByName("m_action");
		action.getPropertyByTitle("enabled").setValue(false);
		waitEventLoop(0);
	}

	/**
	 * When we perform {@link ObjectInfo#refresh()} it is possible, that icon of {@link ObjectInfo}
	 * may become disposed. This should not cause exception.
	 */
	@DisposeProjectAfter
	@Test
	public void test_usingDisposedImage_inComponentsTree() throws Exception {
		ManagerUtils.ensure_ResourceManager(m_javaProject, RcpToolkitDescription.INSTANCE);
		TestUtils.createImagePNG(m_testProject, "src/test/images/test.png", 16, 16);
		//
		openJavaInfo(
				"import org.eclipse.jface.action.*;",
				"import org.eclipse.jface.window.*;",
				"import org.eclipse.wb.swt.ResourceManager;",
				"public class Test extends ApplicationWindow {",
				"  private IAction m_action;",
				"  private IAction m_action2;",
				"  public Test(Shell parentShell) {",
				"    super(parentShell);",
				"    createActions();",
				"    addMenuBar();",
				"  }",
				"  private void createActions() {",
				"    {",
				"      m_action = new Action('Action 1') {",
				"        public void run() {",
				"        }",
				"      };",
				"      m_action.setImageDescriptor(ResourceManager.getImageDescriptor(Test.class,'images/test.png'));",
				"    }",
				"    {",
				"      m_action2 = new Action('Action 2') {",
				"        public void run() {",
				"        }",
				"      };",
				"    }",
				"  }",
				"  protected MenuManager createMenuManager() {",
				"    MenuManager menuManager = new MenuManager('menu');",
				"    {",
				"      MenuManager menuManager_1 = new MenuManager('New MenuManager');",
				"      menuManager.add(menuManager_1);",
				"      menuManager_1.add(m_action);",
				"      menuManager_1.add(m_action2);",
				"    }",
				"    return menuManager;",
				"  }",
				"}");
		ActionInfo action2 = getJavaInfoByName("m_action2");
		// we need this, because only in this case under Win32 we will able to reproduce problem
		m_viewerTree.getControl().setFocus();
		// delete
		tree.expandAll();
		tree.select(action2);
		action2.delete();
		// to prevent "closed" question from Eclipse
		waitEventLoop(0);
	}
}
