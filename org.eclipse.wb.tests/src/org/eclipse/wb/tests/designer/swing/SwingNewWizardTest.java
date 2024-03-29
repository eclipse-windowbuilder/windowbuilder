/*******************************************************************************
 * Copyright (c) 2023 Patrick Ziegler and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Patrick Ziegler - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.designer.swing;

import org.eclipse.wb.internal.swing.wizards.applet.NewJAppletWizard;
import org.eclipse.wb.internal.swing.wizards.application.NewSwingApplicationWizard;
import org.eclipse.wb.internal.swing.wizards.dialog.NewJDialogWizard;
import org.eclipse.wb.internal.swing.wizards.frame.NewJFrameWizard;
import org.eclipse.wb.internal.swing.wizards.frame.NewJInternalFrameWizard;
import org.eclipse.wb.internal.swing.wizards.panel.NewJPanelWizard;
import org.eclipse.wb.tests.designer.core.annotations.DisposeProjectAfter;
import org.eclipse.wb.tests.designer.editor.DesignerEditorTestCase;

import org.eclipse.jdt.core.IPackageFragment;

import org.junit.Before;
import org.junit.Test;

public class SwingNewWizardTest extends DesignerEditorTestCase {
	private IPackageFragment m_packageFragment;

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();
		if (m_testProject == null) {
			do_projectCreate();
		}
		m_packageFragment = m_testProject.getPackage("test");
	}

	@Test
	@DisposeProjectAfter
	public void testCreateNewJFrame() throws Exception {
		openDesign(new NewJFrameWizard(), m_packageFragment, "MyJFrame");
	}

	@Test
	@DisposeProjectAfter
	public void testCreateNewJPanel() throws Exception {
		openDesign(new NewJPanelWizard(), m_packageFragment, "MyJPanel");
	}

	@Test
	@DisposeProjectAfter
	public void testCreateNewJDialog() throws Exception {
		openDesign(new NewJDialogWizard(), m_packageFragment, "MyJDialog");
	}

	@Test
	@DisposeProjectAfter
	public void testCreateNewJApplet() throws Exception {
		openDesign(new NewJAppletWizard(), m_packageFragment, "MyJApplet");
	}

	@Test
	@DisposeProjectAfter
	public void testCreateNewJInternalFrame() throws Exception {
		openDesign(new NewJInternalFrameWizard(), m_packageFragment, "MyJInternalFrame");
	}

	@Test
	@DisposeProjectAfter
	public void testCreateNewApplicationWindow() throws Exception {
		openDesign(new NewSwingApplicationWizard(), m_packageFragment, "MyApplicationWindow");
	}

	@Test
	@DisposeProjectAfter
	public void testCreateWithJavaModules() throws Exception {
		setFileContentSrc("module-info.java", """
				module test {
				}""");
		//
		openDesign(new NewJFrameWizard(), m_packageFragment, "MyJFrame");
		// We can't use code blocks as they don't consider carriage-returns
		assertArrayEquals(getFileContentSrc("module-info.java").split(System.lineSeparator()),
				new String[] {
						"module test {",
						"	requires java.desktop;",
						"}"
				});
	}
}
