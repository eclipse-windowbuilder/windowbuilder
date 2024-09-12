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
package org.eclipse.wb.tests.swtbot.designer;

import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.ui.PlatformUI;

import org.junit.After;
import org.junit.Before;

import java.util.Arrays;

/**
 * Abstract base class for all tests that create Java files using the
 * {@code New Wizard}.
 */
public abstract class AbstractWizardTest extends AbstractSWTBotTest {
	private SWTBotShell shell;
	protected SWTBot editor;

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();
		// Initialize "Resource" perspective
		bot.perspectiveByLabel("Resource").activate();
	}

	@After
	@Override
	public void tearDown() throws Exception {
		// Clear "Resource" perspective and close all open editors
		bot.perspectiveByLabel("Resource").close();
		if (shell != null) {
			PlatformUI.getWorkbench().getDisplay().syncExec(shell.widget::dispose);
		}
		super.tearDown();
	}

	protected final SWTBotShell openNewWizardViaProjectExplorer() {
		String project = m_javaProject.getElementName();
		SWTBot packageExplorer = bot.viewByPartName("Project Explorer").bot();
		packageExplorer.tree().getTreeItem(project).contextMenu().menu("New", "Other...").click();
		return bot.shell("Select a wizard");
	}

	protected final SWTBotShell openNewWizardViaMenu() {
		bot.menu("File").menu("New", "Other...").click();
		return bot.shell("Select a wizard");
	}

	protected final void testTemplateViaProjectExplorer(String... fullPath) {
		shell = openNewWizardViaProjectExplorer();
		createTemplate(fullPath);
	}

	protected final void testTemplateViaMenu(String... fullPath) {
		shell = openNewWizardViaMenu();
		createTemplate(fullPath);
	}

	private void createTemplate(String... fullPath) {
		assertTrue("path requires at least one argument (template name)", fullPath.length > 1);
		String[] path = Arrays.copyOf(fullPath, fullPath.length - 1);
		String name = fullPath[fullPath.length - 1];
		String fileName = name.replaceAll(" ", "_");

		SWTBot newWizard = shell.bot();
		newWizard.tree().expandNode(path).getNode(name).select();
		newWizard.button("Next >").click();
		newWizard.text(1).setText("test");
		newWizard.text(2).setText(fileName);
		newWizard.button("Finish").click();

		editor = bot.editorByTitle(fileName + ".java").bot();
		editor.cTabItem("Design").activate();
	}
}
