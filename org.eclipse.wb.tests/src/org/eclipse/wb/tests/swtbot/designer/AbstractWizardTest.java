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
package org.eclipse.wb.tests.swtbot.designer;

import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;

import java.util.Arrays;

/**
 * Abstract base class for all tests that create Java files using the
 * {@code New Wizard}.
 */
public abstract class AbstractWizardTest extends AbstractSWTBotTest {
	private SWTBotShell shell;
	protected SWTBot editor;

	@AfterEach
	public void tearDown() {
		bot.resetWorkbench();
	}

	protected final void testTemplateViaProjectExplorer(String... fullPath) {
		shell = bot.getProjectExplorer().openNewWizard();
		createTemplate(fullPath);
	}

	protected final void testTemplateViaMenu(String... fullPath) {
		shell = bot.openNewWizard();
		createTemplate(fullPath);
	}

	private void createTemplate(String... fullPath) {
		assertTrue(fullPath.length > 1, "path requires at least one argument (template name)");
		String[] path = Arrays.copyOf(fullPath, fullPath.length - 1);
		String name = fullPath[fullPath.length - 1];
		String fileName = name.replaceAll(" ", "_");

		SWTBot newWizard = shell.bot();
		newWizard.tree().expandNode(path).getNode(name).select();
		newWizard.button("Next >").click();
		newWizard.text(1).setText("test");
		newWizard.text(2).setText(fileName);
		newWizard.button("Finish").click();

		// The created Java file needs to be deleted after the test concluded
		bot.addFile("test", fileName + ".java");
		editor = bot.editorByTitle(fileName + ".java").bot();
		editor.cTabItem("Design").activate();
	}
}
