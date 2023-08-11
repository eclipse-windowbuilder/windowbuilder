/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.xwt.wizards;

import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;
import org.eclipse.wb.internal.rcp.model.rcp.PdeUtils;
import org.eclipse.wb.internal.rcp.wizards.RcpWizard;
import org.eclipse.wb.internal.xwt.Activator;
import org.eclipse.wb.internal.xwt.editor.XwtEditor;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.wizard.Wizard;

import org.osgi.framework.Bundle;

/**
 * Abstract {@link Wizard} for XWT.
 *
 * @author scheglov_ke
 * @coverage XWT.wizards
 */
public abstract class XwtWizard extends RcpWizard {
	private XwtWizardPage m_page;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public XwtWizard() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Pages
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void addPages() {
		super.addPages();
		m_page = (XwtWizardPage) m_mainPage;
	}

	@Override
	protected abstract XwtWizardPage createMainPage();

	////////////////////////////////////////////////////////////////////////////
	//
	// Finish
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void finishPage(IProgressMonitor monitor) throws Exception {
		ensureXWTLibraries();
		super.finishPage(monitor);
		m_page.createXWT();
	}

	@Override
	protected void openEditor() {
		openResource(m_page.getFileJava(), JavaUI.ID_CU_EDITOR);
		openResource(m_page.getFileXWT(), XwtEditor.ID);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Classpath
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Ensures that {@link IJavaProject} has required XWT libraries.
	 */
	private void ensureXWTLibraries() throws Exception {
		IJavaProject javaProject = m_page.getJavaProject();
		// PushingPixels
		if (!ProjectUtils.hasType(javaProject, "org.pushingpixels.trident.Timeline")) {
			addPlugin(javaProject, "org.pushingpixels.trident.Timeline", "org.pushingpixels.trident");
		}
		// XWT itself
		if (!ProjectUtils.hasType(javaProject, "org.eclipse.xwt.XWT")) {
			addPlugin(javaProject, "org.eclipse.xwt.XWT", "org.eclipse.xwt");
		}
		// XWTForms
		if (!ProjectUtils.hasType(javaProject, "org.eclipse.xwt.forms.XWTForms")) {
			addPlugin(javaProject, "org.eclipse.xwt.forms.XWTForms", "org.eclipse.xwt.forms");
		}
		// bindings
		addPlugin(javaProject, "org.eclipse.core.databinding.Binding", "org.eclipse.core.databinding");
		addPlugin(
				javaProject,
				"org.eclipse.core.databinding.observable.IObservable",
				"org.eclipse.core.databinding.observable");
		addPlugin(
				javaProject,
				"org.eclipse.jface.databinding.swt.SWTObservables",
				"org.eclipse.jface.databinding");
	}

	/**
	 * Ensures that plugin (or its libraries) are imported into given {@link IJavaProject}.
	 */
	private void addPlugin(IJavaProject javaProject, String typeName, String pluginId)
			throws Exception {
		if (!ProjectUtils.hasType(javaProject, typeName)) {
			IProject project = javaProject.getProject();
			if (PdeUtils.hasPDENature(project)) {
				PdeUtils.get(project).addPluginImport(pluginId);
			} else {
				ProjectUtils.addPluginLibraries(javaProject, pluginId);
			}
		}
	}

	/**
	 * Copies file from "lib" folder to {@link IJavaProject}.
	 */
	private static String copyJar(IJavaProject javaProject, String name) throws Exception {
		Bundle bundle = Activator.getDefault().getBundle();
		return ProjectUtils.copyFile(javaProject, bundle, "lib/" + name);
	}

	/**
	 * Adds jar/src files as library, in both classpath and PDE manifest.
	 */
	private static void addLibrary(IJavaProject javaProject, String jarName, String srcName)
			throws Exception {
		ProjectUtils.addClasspathEntry(javaProject, jarName, srcName);
	}
}