/*******************************************************************************
 * Copyright (c) 2023, 2025 Patrick Ziegler and others.
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
package org.eclipse.wb.tests.designer.rcp;

import org.eclipse.wb.tests.designer.TestUtils;
import org.eclipse.wb.tests.designer.core.model.parser.AbstractJavaInfoRelatedTest;
import org.eclipse.wb.tests.gef.UiContext;

import static org.eclipse.swtbot.swt.finder.waits.Conditions.shellCloses;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.ui.PlatformUI;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.osgi.framework.Version;

import java.util.Arrays;

/**
 * Abstract base class for all tests that create Java files using the
 * {@code New Wizard}.
 */
public abstract class AbstractWizardTest extends RcpModelTest {
	private static Version ECLIPSE_VERSION = Platform.getBundle("org.eclipse.ui.workbench").getVersion();
	private static Version VERSION = new Version(3, 135, 0);
	// https://github.com/eclipse-platform/eclipse.platform/issues/1749
	private static String WIZARD_NAME = ECLIPSE_VERSION.compareTo(VERSION) > 0 ? "New" : "Select a wizard";
	private static boolean m_firstDesignerTest = true;
	private SWTWorkbenchBot workbench;
	private IFile editorFile;
	protected SWTBot editor;

	@Override
	@BeforeEach
	public void setUp() throws Exception {
		super.setUp();
		if (m_firstDesignerTest) {
			m_firstDesignerTest = false;
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().resetPerspective();
		}
		workbench = new SWTWorkbenchBot();
	}

	@Override
	@AfterEach
	public void tearDown() throws Exception {
		if (editorFile != null) {
			forceDeleteResource(editorFile);
		}
		super.tearDown();
	}


	@AfterAll
	public static void tearDownAll() throws Exception {
		AbstractJavaInfoRelatedTest.tearDownAll();
		TestUtils.closeAllViews();
		m_firstDesignerTest = true;
	}

	protected final void testTemplateViaProjectExplorer(String... fullPath) throws Exception {
		new UiContext().executeAndCheck(() -> {
			SWTBot projectExplorer = workbench.viewById("org.eclipse.ui.navigator.ProjectExplorer").bot();
			projectExplorer.tree().getTreeItem("TestProject").contextMenu().menu("New", "Other...").click();
		}, bot -> createTemplate(bot, fullPath));
	}

	protected final void testTemplateViaMenu(String... fullPath) throws Exception {
		new UiContext().executeAndCheck(() -> {
			workbench.shell().menu().menu("File").menu("New", "Other...").click();
		}, bot -> createTemplate(bot, fullPath));
	}

	private void createTemplate(SWTBot activeShell, String... fullPath) throws CoreException {
		assertTrue(fullPath.length > 1, "path requires at least one argument (template name)");
		String[] path = Arrays.copyOf(fullPath, fullPath.length - 1);
		String name = fullPath[fullPath.length - 1];
		String fileName = name.replace(' ', '_').replaceAll("\\(|\\)", "");

		SWTBotShell shell = activeShell.shell(WIZARD_NAME);
		SWTBot bot = shell.bot();
		bot.tree().expandNode(path).getNode(name).select();
		bot.button("Next >").click();
		bot.text(1).setText("test");
		bot.text(2).setText(fileName);
		bot.button("Finish").click();
		// Wait for file creation
		bot.waitUntil(shellCloses(shell));
		// Open design page
		SWTBotEditor activeEditor = new SWTWorkbenchBot().activeEditor();
		editorFile = activeEditor.getReference().getEditorInput().getAdapter(IFile.class);
		editor = activeEditor.bot();
		editor.cTabItem("Design").activate();
	}
}
