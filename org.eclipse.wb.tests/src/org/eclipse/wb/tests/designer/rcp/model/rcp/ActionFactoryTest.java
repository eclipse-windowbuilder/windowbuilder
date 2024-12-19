/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.designer.rcp.model.rcp;

import org.eclipse.wb.core.model.association.EmptyAssociation;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.utils.ui.UiUtils;
import org.eclipse.wb.internal.rcp.Activator;
import org.eclipse.wb.internal.rcp.model.jface.action.ActionContainerInfo;
import org.eclipse.wb.internal.rcp.model.jface.action.ActionInfo;
import org.eclipse.wb.internal.rcp.model.jface.action.MenuManagerInfo;
import org.eclipse.wb.internal.rcp.model.rcp.ActionBarAdvisorInfo;
import org.eclipse.wb.internal.rcp.model.rcp.ActionFactoryCreationSupport;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.actions.ActionFactory;

import org.junit.Test;

/**
 * Test for {@link Action}'s from {@link ActionFactory}.
 *
 * @author scheglov_ke
 */
public class ActionFactoryTest extends RcpModelTest {
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
	// ActionFactory
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for using {@link ActionFactory#SAVE}.
	 */
	@Test
	public void test_ActionFactory_1() throws Exception {
		ActionBarAdvisorInfo advisor =
				parseJavaInfo(
						"public class Test extends ActionBarAdvisor {",
						"  private IAction m_saveAction;",
						"  public Test(IActionBarConfigurer configurer) {",
						"    super(configurer);",
						"  }",
						"  protected void fillMenuBar(IMenuManager menuBar) {",
						"    menuBar.add(m_saveAction);",
						"  }",
						"  protected void makeActions(IWorkbenchWindow window) {",
						"    {",
						"      m_saveAction = ActionFactory.SAVE.create(window);",
						"      register(m_saveAction);",
						"    }",
						"  }",
						"}");
		advisor.refresh();
		// check hierarchy
		assertHierarchy(
				"{this: org.eclipse.ui.application.ActionBarAdvisor} {this} {/register(m_saveAction)/}",
				"  {parameter} {menuBar} {/menuBar.add(m_saveAction)/}",
				"    {void} {void} {/menuBar.add(m_saveAction)/}",
				"  {org.eclipse.wb.internal.rcp.model.jface.action.ActionContainerInfo}",
				"    {ActionFactory.SAVE} {field-unique: m_saveAction} {/ActionFactory.SAVE.create(window)/ /register(m_saveAction)/ /menuBar.add(m_saveAction)/}");
		// check Action properties
		ActionInfo action = ActionContainerInfo.getActions(advisor).get(0);
		assertEquals("&Save", action.getAction().getText());
		assertEquals("Save", action.getAction().getDescription());
		assertEquals("Save", action.getAction().getToolTipText());
		assertNotNull(action.getAction().getImageDescriptor());
	}

	/**
	 * Test for {@link ActionFactoryCreationSupport}.
	 */
	@Test
	public void test_ActionFactory_2() throws Exception {
		ActionBarAdvisorInfo advisor =
				parseJavaInfo(
						"public class Test extends ActionBarAdvisor {",
						"  private IAction m_saveAction;",
						"  public Test(IActionBarConfigurer configurer) {",
						"    super(configurer);",
						"  }",
						"  protected void makeActions(IWorkbenchWindow window) {",
						"    {",
						"      m_saveAction = ActionFactory.SAVE.create(window);",
						"      register(m_saveAction);",
						"    }",
						"  }",
						"}");
		advisor.refresh();
		// check Action
		ActionInfo action = ActionContainerInfo.getActions(advisor).get(0);
		assertInstanceOf(EmptyAssociation.class, action.getAssociation());
		// ActionFactory_CreationSupport
		{
			ActionFactoryCreationSupport creationSupport =
					(ActionFactoryCreationSupport) action.getCreationSupport();
			{
				MethodInvocation invocation = (MethodInvocation) creationSupport.getNode();
				assertEquals("ActionFactory.SAVE.create(window)", m_lastEditor.getSource(invocation));
			}
			assertTrue(creationSupport.canReorder());
			assertTrue(creationSupport.canReparent());
			assertTrue(creationSupport.canDelete());
		}
		// delete
		{
			action.delete();
			assertEditor(
					"public class Test extends ActionBarAdvisor {",
					"  public Test(IActionBarConfigurer configurer) {",
					"    super(configurer);",
					"  }",
					"  protected void makeActions(IWorkbenchWindow window) {",
					"  }",
					"}");
		}
	}

	/**
	 * Test for using {@link ActionFactory#QUIT}.
	 */
	@Test
	public void test_ActionFactory_defaultIcon() throws Exception {
		ActionBarAdvisorInfo advisor =
				parseJavaInfo(
						"public class Test extends ActionBarAdvisor {",
						"  private IAction m_quitAction;",
						"  public Test(IActionBarConfigurer configurer) {",
						"    super(configurer);",
						"  }",
						"  protected void makeActions(IWorkbenchWindow window) {",
						"    {",
						"      m_quitAction = ActionFactory.QUIT.create(window);",
						"      register(m_quitAction);",
						"    }",
						"  }",
						"}");
		advisor.refresh();
		// check Action properties
		ActionInfo action = ActionContainerInfo.getActions(advisor).get(0);
		assertEquals("E&xit", action.getAction().getText());
		assertEquals("Exit Workbench", action.getAction().getDescription());
		assertEquals("Exit Workbench", action.getAction().getToolTipText());
		// no "imageDescriptor" property for Action, but presentation still has icon
		assertNull(action.getAction().getImageDescriptor());
		{
			ImageDescriptor icon = action.getPresentation().getIcon();
			assertNotNull(icon);
			assertTrue(UiUtils.equals(Activator.getImageDescriptor("info/Action/workbench_action.gif"), icon));
		}
	}

	/**
	 * Test for adding new {@link ActionFactory#SAVE_ALL}.
	 */
	@Test
	public void test_ActionFactory_CREATE() throws Exception {
		ActionBarAdvisorInfo advisor =
				parseJavaInfo(
						"public class Test extends ActionBarAdvisor {",
						"  public Test(IActionBarConfigurer configurer) {",
						"    super(configurer);",
						"  }",
						"  protected void fillMenuBar(IMenuManager menuBar) {",
						"  }",
						"  protected void makeActions(IWorkbenchWindow window) {",
						"  }",
						"}");
		advisor.refresh();
		MenuManagerInfo menuManager = advisor.getChildren(MenuManagerInfo.class).get(0);
		// create Action
		ActionInfo action = ActionFactoryCreationSupport.createNew(advisor, "SAVE_ALL");
		menuManager.command_CREATE(action, null);
		assertEditor(
				"public class Test extends ActionBarAdvisor {",
				"  private IAction saveAllAction;",
				"  public Test(IActionBarConfigurer configurer) {",
				"    super(configurer);",
				"  }",
				"  protected void fillMenuBar(IMenuManager menuBar) {",
				"    menuBar.add(saveAllAction);",
				"  }",
				"  protected void makeActions(IWorkbenchWindow window) {",
				"    {",
				"      saveAllAction = ActionFactory.SAVE_ALL.create(window);",
				"      register(saveAllAction);",
				"    }",
				"  }",
				"}");
		// refresh
		advisor.refresh();
		assertNoErrors(advisor);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Source
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected String[] getTestSource_decorate(String... lines) {
		lines =
				CodeUtils.join(new String[]{
						"package test;",
						"import org.eclipse.jface.action.*;",
						"import org.eclipse.ui.*;",
						"import org.eclipse.ui.actions.*;",
				"import org.eclipse.ui.application.*;"}, lines);
		return lines;
	}
}