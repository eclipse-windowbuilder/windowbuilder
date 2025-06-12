/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
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
package org.eclipse.wb.tests.designer.rcp.model.rcp;

import org.eclipse.wb.internal.core.model.menu.IMenuPopupInfo;
import org.eclipse.wb.internal.core.model.menu.MenuObjectInfoUtils;
import org.eclipse.wb.internal.rcp.model.jface.action.MenuManagerInfo;
import org.eclipse.wb.internal.rcp.model.rcp.PageInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link PageInfo}.
 *
 * @author scheglov_ke
 */
public class PageTest extends RcpModelTest {
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
	 * Test for many elements of {@link PageInfo}.
	 */
	@Disabled
	@Test
	public void test_Page() throws Exception {
		PageInfo page =
				parseJavaInfo(
						"import org.eclipse.jface.action.*;",
						"import org.eclipse.ui.*;",
						"import org.eclipse.ui.part.*;",
						"public class Test extends Page {",
						"  private Composite m_container;",
						"  public Test() {",
						"  }",
						"  public void createControl(Composite parent) {",
						"    m_container = new Composite(parent, SWT.NULL);",
						"  }",
						"  public Control getControl() {",
						"    return m_container;",
						"  }",
						"  public void setFocus() {",
						"  }",
						"  public void init(IPageSite site) {",
						"    super.init(site);",
						"    createActions();",
						"    initializeToolBar();",
						"    initializeMenu();",
						"  }",
						"  private void createActions() {",
						"  }",
						"  private void initializeToolBar() {",
						"    IToolBarManager toolbarManager = getSite().getActionBars().getToolBarManager();",
						"  }",
						"  private void initializeMenu() {",
						"    IMenuManager menuManager = getSite().getActionBars().getMenuManager();",
						"  }",
						"}");
		// check hierarchy
		assertHierarchy(
				"{this: org.eclipse.ui.part.Page} {this} {/getSite().getActionBars()/ /getSite().getActionBars()/}",
				"  {invocationChain: getSite().getActionBars().getToolBarManager()} {local-unique: toolbarManager} {/getSite().getActionBars().getToolBarManager()/}",
				"  {invocationChain: getSite().getActionBars().getMenuManager()} {local-unique: menuManager} {/getSite().getActionBars().getMenuManager()/}",
				"  {parameter} {parent} {/new Composite(parent, SWT.NULL)/}",
				"    {new: org.eclipse.swt.widgets.Composite} {field-unique: m_container} {/new Composite(parent, SWT.NULL)/}",
				"      {implicit-layout: absolute} {implicit-layout} {}");
		CompositeInfo parentComposite = page.getChildren(CompositeInfo.class).get(0);
		CompositeInfo container = (CompositeInfo) parentComposite.getChildrenControls().get(0);
		// refresh()
		page.refresh();
		assertNoErrors(page);
		// check bounds
		assertEquals(page.getBounds().width, 600);
		assertEquals(page.getBounds().height, 500);
		Assertions.assertThat(parentComposite.getBounds().width).isGreaterThan(300);
		Assertions.assertThat(parentComposite.getBounds().height).isGreaterThan(30);
		Assertions.assertThat(container.getBounds().width).isGreaterThan(300);
		Assertions.assertThat(container.getBounds().height).isGreaterThan(300);
		// check IMenuPopupInfo for MenuManager
		{
			MenuManagerInfo manager = page.getChildren(MenuManagerInfo.class).get(0);
			IMenuPopupInfo popupObject = page.getMenuImpl(manager);
			assertNotNull(popupObject);
			// model
			assertSame(manager, popupObject.getModel());
			assertSame(manager, popupObject.getToolkitModel());
			// presentation
			assertNull(popupObject.getImageDescriptor());
			Assertions.assertThat(popupObject.getBounds().width).isGreaterThan(10);
			Assertions.assertThat(popupObject.getBounds().height).isGreaterThan(10);
			// menu
			assertSame(MenuObjectInfoUtils.getMenuInfo(manager), popupObject.getMenu());
			assertSame(popupObject.getMenu().getPolicy(), popupObject.getPolicy());
		}
	}

	/**
	 * Test for <code>ContentOutlinePage</code>.
	 */
	@Test
	public void test_ContentOutlinePage() throws Exception {
		PageInfo page =
				parseJavaInfo(
						"import org.eclipse.ui.views.contentoutline.ContentOutlinePage;",
						"public class Test extends ContentOutlinePage {",
						"  public Test() {",
						"  }",
						"  public void createControl(Composite parent) {",
						"  }",
						"}");
		page.refresh();
		assertNoErrors(page);
		// check hierarchy
		assertHierarchy(
				"{this: org.eclipse.ui.views.contentoutline.ContentOutlinePage} {this} {}",
				"  {parameter} {parent} {}");
	}
}