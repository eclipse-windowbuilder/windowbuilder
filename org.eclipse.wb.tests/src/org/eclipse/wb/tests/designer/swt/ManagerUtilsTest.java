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
package org.eclipse.wb.tests.designer.swt;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.description.ToolkitDescription;
import org.eclipse.wb.internal.rcp.RcpToolkitDescription;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.utils.ManagerUtils;
import org.eclipse.wb.tests.designer.core.annotations.DisposeProjectAfter;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.eclipse.jdt.core.IJavaProject;

/**
 * Tests for {@link ManagerUtils}.
 *
 * @author scheglov_ke
 */
public class ManagerUtilsTest extends RcpModelTest {
	private CompositeInfo shell;

	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void tearDown() throws Exception {
		shell = null;
		super.tearDown();
	}

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
	 * Test for {@link ManagerUtils#ensure_SWTResourceManager(IJavaProject, ToolkitDescription)}.
	 */
	@DisposeProjectAfter
	public void test_ensure_SWTResourceManager_usingJavaProject() throws Exception {
		parseShell();
		// no SWTResourceManager initially
		assertTrue(m_javaProject.findType("org.eclipse.wb.swt.SWTResourceManager") == null);
		// add SWTResourceManager
		ManagerUtils.ensure_SWTResourceManager(m_javaProject, RcpToolkitDescription.INSTANCE);
		assertTrue(m_javaProject.findType("org.eclipse.wb.swt.SWTResourceManager") != null);
		// check also that SWTResourceManager already compiled and *.class file can be loaded
		m_lastLoader.loadClass("org.eclipse.wb.swt.SWTResourceManager");
		// second "ensure" does not break anything
		ManagerUtils.ensure_SWTResourceManager(shell);
		assertTrue(m_javaProject.findType("org.eclipse.wb.swt.SWTResourceManager") != null);
	}

	/**
	 * Test for {@link ManagerUtils#ensure_SWTResourceManager(JavaInfo)}.
	 */
	@DisposeProjectAfter
	public void test_ensure_SWTResourceManager_usingJavaInfo() throws Exception {
		parseShell();
		// no SWTResourceManager initially
		assertTrue(m_javaProject.findType("org.eclipse.wb.swt.SWTResourceManager") == null);
		// add SWTResourceManager
		ManagerUtils.ensure_SWTResourceManager(shell);
		assertTrue(m_javaProject.findType("org.eclipse.wb.swt.SWTResourceManager") != null);
		// check also that SWTResourceManager already compiled and *.class file can be loaded
		m_lastLoader.loadClass("org.eclipse.wb.swt.SWTResourceManager");
		// second "ensure" does not break anything
		ManagerUtils.ensure_SWTResourceManager(shell);
		assertTrue(m_javaProject.findType("org.eclipse.wb.swt.SWTResourceManager") != null);
	}

	/**
	 * Test for {@link ManagerUtils#ensure_ResourceManager(IJavaProject, ToolkitDescription)}.
	 */
	@DisposeProjectAfter
	public void test_ensure_ResourceManager_usingJavaProject() throws Exception {
		parseShell();
		// no [SWT]ResourceManager initially
		assertTrue(m_javaProject.findType("org.eclipse.wb.swt.SWTResourceManager") == null);
		assertTrue(m_javaProject.findType("org.eclipse.wb.swt.ResourceManager") == null);
		// add ResourceManager
		ManagerUtils.ensure_ResourceManager(m_javaProject, RcpToolkitDescription.INSTANCE);
		assertTrue(m_javaProject.findType("org.eclipse.wb.swt.SWTResourceManager") != null);
		assertTrue(m_javaProject.findType("org.eclipse.wb.swt.ResourceManager") != null);
		// check also that SWTResourceManager already compiled and *.class file can be loaded
		m_lastLoader.loadClass("org.eclipse.wb.swt.SWTResourceManager");
		m_lastLoader.loadClass("org.eclipse.wb.swt.ResourceManager");
		// second "ensure" does not break anything
		ManagerUtils.ensure_SWTResourceManager(shell);
		assertTrue(m_javaProject.findType("org.eclipse.wb.swt.SWTResourceManager") != null);
		assertTrue(m_javaProject.findType("org.eclipse.wb.swt.ResourceManager") != null);
	}

	/**
	 * Test for {@link ManagerUtils#ensure_ResourceManager(JavaInfo)}.
	 */
	@DisposeProjectAfter
	public void test_ensure_ResourceManager() throws Exception {
		parseShell();
		// no [SWT]ResourceManager initially
		assertTrue(m_javaProject.findType("org.eclipse.wb.swt.SWTResourceManager") == null);
		assertTrue(m_javaProject.findType("org.eclipse.wb.swt.ResourceManager") == null);
		// add ResourceManager
		ManagerUtils.ensure_ResourceManager(shell);
		assertTrue(m_javaProject.findType("org.eclipse.wb.swt.SWTResourceManager") != null);
		assertTrue(m_javaProject.findType("org.eclipse.wb.swt.ResourceManager") != null);
		// check also that SWTResourceManager already compiled and *.class file can be loaded
		m_lastLoader.loadClass("org.eclipse.wb.swt.SWTResourceManager");
		m_lastLoader.loadClass("org.eclipse.wb.swt.ResourceManager");
		// second "ensure" does not break anything
		ManagerUtils.ensure_SWTResourceManager(shell);
		assertTrue(m_javaProject.findType("org.eclipse.wb.swt.SWTResourceManager") != null);
		assertTrue(m_javaProject.findType("org.eclipse.wb.swt.ResourceManager") != null);
	}

	private void parseShell() throws Exception {
		shell =
				parseComposite(
						"// filler filler filler",
						"public class Test extends Shell {",
						"  public Test() {",
						"  }",
						"}");
	}
}
