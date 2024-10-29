/*******************************************************************************
 * Copyright (c) 2023, 2024 Patrick Ziegler and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Patrick Ziegler - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.swtbot.designer.swing.wizard;

import org.eclipse.wb.tests.swtbot.designer.AbstractWizardTest;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.jupiter.api.Test;

public class SwingNewWizardTest extends AbstractWizardTest {

	@Test
	public void testCreateNewJFrame() throws Exception {
		testTemplateViaProjectExplorer("WindowBuilder", "Swing Designer", "JFrame");
	}

	@Test
	public void testCreateNewJPanel() throws Exception {
		testTemplateViaProjectExplorer("WindowBuilder", "Swing Designer", "JPanel");
	}

	@Test
	public void testCreateNewJDialog() throws Exception {
		testTemplateViaProjectExplorer("WindowBuilder", "Swing Designer", "JDialog");
	}

	@Test
	public void testCreateNewJApplet() throws Exception {
		testTemplateViaProjectExplorer("WindowBuilder", "Swing Designer", "JApplet");
	}

	@Test
	public void testCreateNewJInternalFrame() throws Exception {
		testTemplateViaProjectExplorer("WindowBuilder", "Swing Designer", "JInternalFrame");
	}

	@Test
	public void testCreateNewApplicationWindow() throws Exception {
		testTemplateViaProjectExplorer("WindowBuilder", "Swing Designer", "Application Window");
	}

	@Test
	public void testCreateWithJavaModules() throws Exception {
		bot.setFileContent("module-info.java", """
				module test {
				}""");
		//
		testTemplateViaProjectExplorer("WindowBuilder", "Swing Designer", "JFrame");
		// We can't use code blocks as they don't consider carriage-returns
		assertArrayEquals(bot.getFileContent("module-info.java").split(System.lineSeparator()),
				new String[] {
						"module test {",
						"	requires java.desktop;",
						"}"
				});
	}

	@Test
	public void testCreateNewJFrameNoSelection() throws Exception {
		testTemplateViaMenu("WindowBuilder", "Swing Designer", "JFrame");
	}

	@Test
	public void testCreateNewJPanelNoSelection() throws Exception {
		testTemplateViaMenu("WindowBuilder", "Swing Designer", "JPanel");
	}

	@Test
	public void testCreateNewJDialogNoSelection() throws Exception {
		testTemplateViaMenu("WindowBuilder", "Swing Designer", "JDialog");
	}

	@Test
	public void testCreateNewJAppletNoSelection() throws Exception {
		testTemplateViaMenu("WindowBuilder", "Swing Designer", "JApplet");
	}

	@Test
	public void testCreateNewJInternalFrameNoSelection() throws Exception {
		testTemplateViaMenu("WindowBuilder", "Swing Designer", "JInternalFrame");
	}

	@Test
	public void testCreateNewApplicationWindowNoSelection() throws Exception {
		testTemplateViaMenu("WindowBuilder", "Swing Designer", "Application Window");
	}

	@Test
	public void testCreateWithJavaModulesNoSelection() throws Exception {
		bot.setFileContent("module-info.java", """
				module test {
				}""");
		//
		testTemplateViaMenu("WindowBuilder", "Swing Designer", "JFrame");
		// We can't use code blocks as they don't consider carriage-returns
		assertArrayEquals(bot.getFileContent("module-info.java").split(System.lineSeparator()),
				new String[] {
						"module test {",
						"	requires java.desktop;",
						"}"
				});
	}
}
