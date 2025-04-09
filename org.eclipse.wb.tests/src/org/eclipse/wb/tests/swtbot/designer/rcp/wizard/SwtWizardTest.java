/*******************************************************************************
 * Copyright (c) 2023, 2024 Patrick Ziegler and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Patrick Ziegler - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.swtbot.designer.rcp.wizard;

import org.eclipse.wb.tests.swtbot.designer.AbstractWizardTest;

import org.junit.jupiter.api.Test;

public class SwtWizardTest extends AbstractWizardTest {
	@Test
	public void testCreateNewApplicationWindow() throws Exception {
		testTemplateViaProjectExplorer("WindowBuilder", "SWT Designer", "SWT", "Application Window");
	}

	@Test
	public void testCreateNewComposite() throws Exception {
		testTemplateViaProjectExplorer("WindowBuilder", "SWT Designer", "SWT", "Composite");
	}

	@Test
	public void testCreateNewDialog() throws Exception {
		testTemplateViaProjectExplorer("WindowBuilder", "SWT Designer", "SWT", "Dialog");
	}

	@Test
	public void testCreateNewShell() throws Exception {
		testTemplateViaProjectExplorer("WindowBuilder", "SWT Designer", "SWT", "Shell");
	}

	@Test
	public void testCreateNewApplicationWindowNoSelection() throws Exception {
		testTemplateViaMenu("WindowBuilder", "SWT Designer", "SWT", "Application Window");
	}

	@Test
	public void testCreateNewCompositeNoSelection() throws Exception {
		testTemplateViaMenu("WindowBuilder", "SWT Designer", "SWT", "Composite");
	}

	@Test
	public void testCreateNewDialogNoSelection() throws Exception {
		testTemplateViaMenu("WindowBuilder", "SWT Designer", "SWT", "Dialog");
	}

	@Test
	public void testCreateNewShellNoSelection() throws Exception {
		testTemplateViaMenu("WindowBuilder", "SWT Designer", "SWT", "Shell");
	}
}
