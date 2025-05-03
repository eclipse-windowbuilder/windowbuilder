/*******************************************************************************
 * Copyright (c) 2024, 2025 Patrick Ziegler and others.
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
package org.eclipse.wb.tests.swtbot.designer.bot;

import org.eclipse.wb.tests.designer.core.TestProject;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.intro.IIntroManager;
import org.eclipse.ui.intro.IIntroPart;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 * The SWTBot instance over the entire {@code Eclipse} workbench.
 */
public class WindowBuilderWorkbenchBot extends SWTWorkbenchBot {
	private static final String TEST_PROJECT_NAME = "TestProject";
	private static final Logger LOGGER = Logger.getLogger(WindowBuilderWorkbenchBot.class.getSimpleName());
	private TestProject testProject;
	private List<IFile> createdFiles;
	private boolean init;

	public WindowBuilderWorkbenchBot() {
		testProject = new TestProject(createProject());
		createdFiles = new ArrayList<>();

		if (!init) {
			init = true;
			UIUtil.syncExec(() -> {
				// Close "Welcome" page
				IWorkbench wb = PlatformUI.getWorkbench();
				IIntroManager im = wb.getIntroManager();
				IIntroPart intro = wb.getIntroManager().getIntro();
				if (intro != null) {
					im.closeIntro(intro);
				}
				// Initialize "Resource" perspective
				IWorkbenchWindow ww = wb.getActiveWorkbenchWindow();
				wb.getPerspectiveRegistry().setDefaultPerspective("org.eclipse.ui.resourcePerspective");
				wb.showPerspective("org.eclipse.ui.resourcePerspective", ww);
			});
		}
	}

	private IProject createProject() {
		IProject project = getTestProject();

		if (project.exists()) {
			return project;
		}

		SWTBot wizard = openNewWizard().bot();
		wizard.tree().expandNode("WindowBuilder", "SWT Designer", "SWT/JFace Java Project").click();
		wizard.button("Next >").click();
		wizard.text().setText("TestProject");
		wizard.checkBox("Create module-info.java file").deselect();
		wizard.button("Next >").click();
		wizard.button("Finish").click();
		waitWhile(Conditions.shellIsActive("New SWT/JFace Java Project"));

		return getTestProject();
	}

	private static IProject getTestProject() {
		return UIUtil.syncCall(() -> ResourcesPlugin.getWorkspace().getRoot().getProject(TEST_PROJECT_NAME));
	}

	@Override
	public void resetWorkbench() {
		Iterator<IFile> iterator = createdFiles.iterator();
		while (iterator.hasNext()) {
			UIUtil.syncExec(() -> iterator.next().delete(true, null));
			iterator.remove();
		}
		super.resetWorkbench();
	}

	/**
	 * @return The SWTBot instance over the {@code Project Explorer}.
	 */
	public WindowBuilderProjectExplorerBot getProjectExplorer() {
		LOGGER.fine("Open Project Explorer");
		SWTBotView projectExplorer = viewByPartName("Project Explorer");
		LOGGER.fine("Opened Project Explorer");
		return new WindowBuilderProjectExplorerBot(projectExplorer.getViewReference(), this);
	}

	/**
	 * Opens the {@code New} wizard from the main menu.
	 *
	 * @return the {@code New} wizard shell.
	 */
	public SWTBotShell openNewWizard() {
		LOGGER.fine("Open New wizard");
		shell().menu().menu("File").menu("New", "Other...").click();
		LOGGER.fine("Opened New wizard");
		return activeShell();
	}

	// I/O
	private IFile getFile(String packageName, String fileName) {
		return UIUtil.syncCall(() -> {
			IFolder packageFolder = (IFolder) testProject.getPackage(packageName).getResource();
			return packageFolder.getFile(fileName);
		});
	}

	/**
	 * Registers the given source file to be deleted after the current test has
	 * concluded. The file is located in the root of the source directory.
	 *
	 * <pre>
	 * Example: src/module-info.java
	 * </pre>
	 *
	 * @param fileName The file name (e.g. {@code module-info.java}).
	 */
	public void addFile(String fileName) {
		addFile("", fileName);
	}

	/**
	 * Registers the given source file to be deleted after the current test has
	 * concluded. The file is located in the source directory.
	 *
	 * <pre>
	 * Example: src/test/Test.java
	 * </pre>
	 *
	 * @param packageName The package name (e.g. {@code test}).
	 * @param fileName    The file name (e.g. {@code Test.java}.
	 */
	public void addFile(String packageName, String fileName) {
		IFile sourceFile = getFile(packageName, fileName);
		createdFiles.add(sourceFile);
	}

	/**
	 * Updates the content of the given source file. The file is located in the root
	 * of the source directory and deleted after the test concludes. A new file is
	 * created if necessary.
	 *
	 * <pre>
	 * Example: src/module-info.java
	 * </pre>
	 *
	 * @param fileName The file name (e.g. {@code module-info.java}).
	 * @param content  The file content.
	 */
	public void setFileContent(String fileName, String content) {
		setFileContent("", fileName, content);
	}

	/**
	 * Updates the content of the given source file. The file is deleted after the
	 * test concludes. A new file is created if necessary.
	 *
	 * <pre>
	 * Example: src/test/Test.java
	 * </pre>
	 *
	 * @param packageName The package name (e.g. {@code test})
	 * @param fileName    The file name (e.g. {@code Test.java}).
	 * @param content     The file content.
	 */
	public void setFileContent(String packageName, String fileName, String content) {
		UIUtil.syncExec(() -> {
			try (InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8))) {
				IFile sourceFile = getFile(packageName, fileName);
				sourceFile.setContents(is, true, false, null);
				createdFiles.add(sourceFile);
			}
		});
	}

	/**
	 * Returns the content of the given source file. The file is located in the root
	 * of the source directory.
	 *
	 * <pre>
	 * Example: src/module-info.java
	 * </pre>
	 *
	 * @param fileName The file name (e.g. {@code module-info.java}).
	 * @return The file content.
	 */
	public String getFileContent(String fileName) {
		return getFileContent("", fileName);
	}

	/**
	 * Returns the content of the given source file.
	 *
	 * <pre>
	 * Example: src/test/Test.java
	 * </pre>
	 *
	 * @param packageName The package name (e.g. {@code test})
	 * @param fileName    The file name (e.g. {@code Test.java}).
	 * @return The file content.
	 */
	public String getFileContent(String packageName, String fileName) {
		return UIUtil.syncCall(() -> {
			IFile sourceFile = getFile(packageName, fileName);
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			try (InputStream is = sourceFile.getContents()) {
				is.transferTo(os);
			}
			return new String(os.toByteArray(), StandardCharsets.UTF_8);
		});
	}
}
