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
package org.eclipse.wb.tests.swtbot.designer.rcp.wizard;

import org.eclipse.wb.tests.swtbot.designer.AbstractWizardTest;

import static org.eclipse.swtbot.swt.finder.matchers.WithText.withText;

import org.junit.Test;

public class JFaceWizardTest extends AbstractWizardTest {
	@Test
	public void testCreateNewApplicationWindow() throws Exception {
		testTemplateViaProjectExplorer("WindowBuilder", "SWT Designer", "JFace", "ApplicationWindow");
	}

	@Test
	public void testCreateNewDialog() throws Exception {
		testTemplateViaProjectExplorer("WindowBuilder", "SWT Designer", "JFace", "Dialog");
	}

	@Test
	public void testCreateNewTitleAreaDialog() throws Exception {
		testTemplateViaProjectExplorer("WindowBuilder", "SWT Designer", "JFace", "TitleAreaDialog");
	}

	@Test
	public void testCreateNewWizard() throws Exception {
		testTemplateViaProjectExplorer("WindowBuilder", "SWT Designer", "JFace", "Wizard");
		assertNotNull(editor.widget(withText("Graphical editing is not provided for Wizard classes.")));
	}

	@Test
	public void testCreateNewWizardPage() throws Exception {
		testTemplateViaProjectExplorer("WindowBuilder", "SWT Designer", "JFace", "WizardPage");
	}

	@Test
	public void testCreateNewApplicationWindowNoSelection() throws Exception {
		testTemplateViaMenu("WindowBuilder", "SWT Designer", "JFace", "ApplicationWindow");
	}

	@Test
	public void testCreateNewDialogNoSelection() throws Exception {
		testTemplateViaMenu("WindowBuilder", "SWT Designer", "JFace", "Dialog");
	}

	@Test
	public void testCreateNewTitleAreaDialogNoSelection() throws Exception {
		testTemplateViaMenu("WindowBuilder", "SWT Designer", "JFace", "TitleAreaDialog");
	}

	@Test
	public void testCreateNewWizardNoSelection() throws Exception {
		testTemplateViaMenu("WindowBuilder", "SWT Designer", "JFace", "Wizard");
		assertNotNull(editor.widget(withText("Graphical editing is not provided for Wizard classes.")));
	}

	@Test
	public void testCreateNewWizardPageNoSelection() throws Exception {
		testTemplateViaMenu("WindowBuilder", "SWT Designer", "JFace", "WizardPage");
	}
}
