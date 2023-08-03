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
package org.eclipse.wb.tests.designer.rcp.model.jface;

import org.eclipse.wb.core.editor.palette.model.EntryInfo;
import org.eclipse.wb.core.editor.palette.model.entry.ComponentEntryInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.editor.palette.model.entry.StaticFactoryEntryInfo;
import org.eclipse.wb.internal.rcp.palette.TableCompositeEntryInfo;
import org.eclipse.wb.internal.rcp.palette.TableViewerCompositeEntryInfo;
import org.eclipse.wb.internal.rcp.palette.TreeCompositeEntryInfo;
import org.eclipse.wb.internal.rcp.palette.TreeViewerCompositeEntryInfo;
import org.eclipse.wb.tests.designer.core.model.parser.AbstractJavaInfoTest;
import org.eclipse.wb.tests.designer.rcp.BTestUtils;

import org.eclipse.jdt.core.IJavaProject;

import org.junit.Before;
import org.junit.Test;

/**
 * If no JFace in classpath, in {@link IJavaProject}, then corresponding entries should no be
 * active.
 *
 * @author scheglov_ke
 */
public class NoJFaceInClasspathTest extends AbstractJavaInfoTest {
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
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		if (m_testProject == null) {
			do_projectCreate();
			BTestUtils.configureSWT(m_testProject);
		}
	}

	@Override
	protected void configureToolkits() {
		super.configureToolkits();
		configureDefaults(org.eclipse.wb.internal.rcp.ToolkitProvider.DESCRIPTION);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_Button() throws Exception {
		ComponentEntryInfo entry = new ComponentEntryInfo();
		entry.setComponentClassName("org.eclipse.swt.widgets.Button");
		//
		check_entryIsActive(entry);
	}

	@Test
	public void test_TableViewer() throws Exception {
		ComponentEntryInfo entry = new ComponentEntryInfo();
		entry.setComponentClassName("org.eclipse.jface.viewers.TableViewer");
		//
		check_entryIsNotActive(entry);
	}

	@Test
	public void test_TableComposite_EntryInfo() throws Exception {
		TableCompositeEntryInfo entry = new TableCompositeEntryInfo();
		check_entryIsNotActive(entry);
	}

	@Test
	public void test_TableViewerComposite_EntryInfo() throws Exception {
		TableViewerCompositeEntryInfo entry = new TableViewerCompositeEntryInfo();
		check_entryIsNotActive(entry);
	}

	@Test
	public void test_TreeComposite_EntryInfo() throws Exception {
		TreeCompositeEntryInfo entry = new TreeCompositeEntryInfo();
		check_entryIsNotActive(entry);
	}

	@Test
	public void test_TreeViewerComposite_EntryInfo() throws Exception {
		TreeViewerCompositeEntryInfo entry = new TreeViewerCompositeEntryInfo();
		check_entryIsNotActive(entry);
	}

	@Test
	public void test_CheckboxTableViewer() throws Exception {
		StaticFactoryEntryInfo entry = new StaticFactoryEntryInfo();
		entry.setFactoryClassName("org.eclipse.jface.viewers.CheckboxTableViewer");
		entry.setMethodSignature("newCheckList(org.eclipse.swt.widgets.Composite,int)");
		check_entryIsNotActive(entry);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	private void check_entryIsActive(EntryInfo entry) throws Exception {
		check_entryActivation(entry, true);
	}

	private void check_entryIsNotActive(EntryInfo entry) throws Exception {
		check_entryActivation(entry, false);
	}

	private void check_entryActivation(EntryInfo entry, boolean expected) throws Exception {
		JavaInfo shell =
				parseSource(
						"test",
						"Test.java",
						getSource(
								"package test;",
								"import org.eclipse.swt.widgets.*;",
								"public class Test extends Shell {",
								"  public Test() {",
								"  }",
								"}"));
		//
		boolean success = entry.initialize(null, shell);
		assertEquals(expected, success);
	}
}