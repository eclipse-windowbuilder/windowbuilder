/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
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
package org.eclipse.wb.tests.designer.core.nls.ui;

import org.eclipse.wb.tests.designer.swing.SwingGefTest;
import org.eclipse.wb.tests.gef.UIRunnable;
import org.eclipse.wb.tests.gef.UiContext;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.swt.widgets.ToolItem;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * Abstract test for NLS UI.
 *
 * @author scheglov_ke
 */
public abstract class AbstractNlsUiTest extends SwingGefTest {
	protected ToolItem m_dialogItem;

	////////////////////////////////////////////////////////////////////////////
	//
	// Design
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void fetchContentFields() {
		super.fetchContentFields();
		{
			UiContext context = new UiContext();
			// NLS dialog item
			m_dialogItem = context.getToolItem("Externalize strings");
			assertNotNull("Can not find NLS dialog item.", m_dialogItem);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Creates compilation unit, opens Design page, opens NLS dialog and then run given
	 * {@link UIRunnable}.
	 */
	protected final void openDialogNLS(String initialSource, UIRunnable runnable) throws Exception {
		openDialogNLS("test", initialSource, runnable);
	}

	/**
	 * Creates compilation unit, opens Design page, opens NLS dialog and then run given
	 * {@link UIRunnable}.
	 */
	protected final void openDialogNLS(String packageName, String initialSource, UIRunnable runnable)
			throws Exception {
		ICompilationUnit unit = createModelCompilationUnit(packageName, "Test.java", initialSource);
		openDesign(unit);
		// click on "Externalize strings" item
		new UiContext().executeAndCheck(new UIRunnable() {
			@Override
			public void run(UiContext context) throws Exception {
				context.click(m_dialogItem);
			}
		}, runnable);
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
		}
	}

	@Override
	@After
	public void tearDown() throws Exception {
		// process UI messages (without this we have exception from Java UI)
		waitEventLoop(1);
		//
		super.tearDown();
		if (m_testProject != null) {
			deleteFiles(m_testProject.getJavaProject().getProject().getFolder("src"));
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Project life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@BeforeClass
	public static void setUpClass() throws Exception {
		do_projectCreate();
	}
}
