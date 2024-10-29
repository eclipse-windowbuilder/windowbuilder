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

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class RcpWizardTest extends AbstractWizardTest {
	@Test
	public void testCreateNewActionBarAdvisor() throws Exception {
		testTemplateViaProjectExplorer("WindowBuilder", "SWT Designer", "RCP", "ActionBarAdvisor");
	}

	@Test
	public void testCreateNewEditorPart() throws Exception {
		testTemplateViaProjectExplorer("WindowBuilder", "SWT Designer", "RCP", "EditorPart");
	}

	@Test
	public void testCreateNewMultiPageEditorPart() throws Exception {
		testTemplateViaProjectExplorer("WindowBuilder", "SWT Designer", "RCP", "MultiPageEditorPart");
		assertNotNull(editor.widget(withText("Graphical editing is not provided for MultiPageEditorPart classes.")));
	}

	@Test
	public void testCreateNewPageBookViewPage() throws Exception {
		testTemplateViaProjectExplorer("WindowBuilder", "SWT Designer", "RCP", "PageBookViewPage");
	}

	@Test
	public void testCreateNewPerspective() throws Exception {
		testTemplateViaProjectExplorer("WindowBuilder", "SWT Designer", "RCP", "Perspective");
	}

	@Test
	public void testCreateNewPreferencePage() throws Exception {
		testTemplateViaProjectExplorer("WindowBuilder", "SWT Designer", "RCP", "PreferencePage");
	}

	@Test
	public void testCreateNewPropertyPage() throws Exception {
		testTemplateViaProjectExplorer("WindowBuilder", "SWT Designer", "RCP", "PropertyPage");
	}

	@Test
	public void testCreateNewViewPart() throws Exception {
		testTemplateViaProjectExplorer("WindowBuilder", "SWT Designer", "RCP", "ViewPart");
	}

	@Test
	public void testCreateNewActionBarAdvisorNoSelection() throws Exception {
		testTemplateViaMenu("WindowBuilder", "SWT Designer", "RCP", "ActionBarAdvisor");
	}

	@Test
	public void testCreateNewEditorPartNoSelection() throws Exception {
		testTemplateViaMenu("WindowBuilder", "SWT Designer", "RCP", "EditorPart");
	}

	@Test
	public void testCreateNewMultiPageEditorPartNoSelection() throws Exception {
		testTemplateViaMenu("WindowBuilder", "SWT Designer", "RCP", "MultiPageEditorPart");
		assertNotNull(editor.widget(withText("Graphical editing is not provided for MultiPageEditorPart classes.")));
	}

	@Test
	public void testCreateNewPageBookViewPageNoSelection() throws Exception {
		testTemplateViaMenu("WindowBuilder", "SWT Designer", "RCP", "PageBookViewPage");
	}

	@Test
	public void testCreateNewPerspectiveNoSelection() throws Exception {
		testTemplateViaMenu("WindowBuilder", "SWT Designer", "RCP", "Perspective");
	}

	@Test
	public void testCreateNewPreferencePageNoSelection() throws Exception {
		testTemplateViaMenu("WindowBuilder", "SWT Designer", "RCP", "PreferencePage");
	}

	@Test
	public void testCreateNewPropertyPageNoSelection() throws Exception {
		testTemplateViaMenu("WindowBuilder", "SWT Designer", "RCP", "PropertyPage");
	}

	@Test
	public void testCreateNewViewPartNoSelection() throws Exception {
		testTemplateViaMenu("WindowBuilder", "SWT Designer", "RCP", "ViewPart");
	}
}
