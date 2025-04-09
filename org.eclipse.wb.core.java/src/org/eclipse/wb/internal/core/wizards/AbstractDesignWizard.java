/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
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
package org.eclipse.wb.internal.core.wizards;

import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IModuleDescription;
import org.eclipse.jface.wizard.WizardPage;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Base class for wizard responsible to create Java elements.
 *
 * @author lobas_av
 * @coverage core.wizards.ui
 */
public abstract class AbstractDesignWizard extends DesignerNewElementWizard {
	// TODO(scheglov) move to shared place
	private static final String EDITOR_ID = "org.eclipse.wb.core.guiEditor";
	protected AbstractDesignWizardPage m_mainPage;

	////////////////////////////////////////////////////////////////////////////
	//
	// Pages
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void addPages() {
		if (!validateSelection()) {
			return;
		}
		m_mainPage = createMainPage();
		addPage(m_mainPage);
		m_mainPage.setInitialSelection(getSelection());
	}

	/**
	 * Create main wizard page for this wizard.
	 */
	protected abstract AbstractDesignWizardPage createMainPage();

	/**
	 * @return <code>true</code> if current selection is valid, or adds error {@link WizardPage} and
	 *         return <code>false</code>.
	 */
	protected boolean validateSelection() {
		return true;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Finish
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean performFinish() {
		boolean canFinish = super.performFinish();
		if (canFinish) {
			openEditor();
		}
		return canFinish;
	}

	@Override
	protected void finishPage(IProgressMonitor monitor) throws Exception {
		if (getJavaProject() != null && getJavaProject().getModuleDescription() != null) {
			updateModuleInfo(getJavaProject().getModuleDescription());
		}
		m_mainPage.createType(monitor);
	}

	public final IJavaElement getCreatedElement() {
		return m_mainPage.getCreatedType();
	}

	private void updateModuleInfo(IModuleDescription moduleDescription) throws Exception {
		Set<String> moduleNames = Set.of(moduleDescription.getRequiredModuleNames());
		if (moduleNames.containsAll(getRequiredModuleNames())) {
			return;
		}

		IFile moduleInfo = (IFile) moduleDescription.getResource();
		List<String> lines = new ArrayList<>(Files.readAllLines(moduleInfo.getLocation().toPath()));
		// Calculate the content of the new module-info.java file
		boolean moduleHeaderFound = false;
		boolean moduleBodyFound = false;
		for (int i = 0; i < lines.size(); ++i) {
			if (lines.get(i).contains("module")) {
				moduleHeaderFound = true;
			}
			if (lines.get(i).contains("{")) {
				moduleBodyFound = true;
			}
			if (moduleHeaderFound && moduleBodyFound) {
				for (String moduleName : getRequiredModuleNames()) {
					if (!moduleNames.contains(moduleName)) {
						lines.add(i + 1, String.format("\trequires %s;", moduleName));
					}
				}
				break;
			}
		}
		// Store the updated module-info.java to the file system
		String sourceCode = lines.stream().reduce((u, v) -> u + System.lineSeparator() + v).orElse(null);
		if (sourceCode != null) {
			try (InputStream is = new ByteArrayInputStream(sourceCode.getBytes(StandardCharsets.UTF_8))) {
				moduleInfo.setContents(is, IResource.FORCE, null);
			}
		}
		//
		ProjectUtils.requireModuleAttribute(getJavaProject());
	}

	/**
	 * This method is called just before a new type is created, to ensure that all
	 * dependencies are available, before the type is compiled.
	 *
	 * @return A set of all modules names that need to be added to
	 *         {@code module-info.java}.
	 */
	protected abstract Set<String> getRequiredModuleNames();

	/**
	 * Opens creates UI in editor.
	 */
	protected void openEditor() {
		IFile file = (IFile) m_mainPage.getModifiedResource();
		openEditor(file);
	}

	/**
	 * Opens created {@link IFile} in editor.
	 */
	protected void openEditor(IFile file) {
		openResource(file, EDITOR_ID);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link IJavaProject} of main page, may be <code>null</code>.
	 */
	protected final IJavaProject getJavaProject() {
		return m_mainPage.getJavaProject();
	}
}