/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
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
package org.eclipse.wb.internal.rcp.wizards.project;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;
import org.eclipse.wb.internal.core.wizards.DesignerJavaProjectWizard;
import org.eclipse.wb.internal.rcp.Activator;
import org.eclipse.wb.internal.rcp.wizards.WizardsMessages;

import org.eclipse.jdt.core.IJavaProject;

/**
 * Wizard that creates new RCP project.
 *
 * @author lobas_av
 * @coverage rcp.wizards
 */
public class NewProjectWizard extends DesignerJavaProjectWizard {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public NewProjectWizard() {
		setDefaultPageImageDescriptor(Activator.getImageDescriptor("wizard/Project/banner.gif"));
		setWindowTitle(WizardsMessages.NewProjectWizard_title);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Wizard
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean performFinish() {
		boolean result = super.performFinish();
		if (result) {
			try {
				addRequiredLibraries(getCreatedElement());
			} catch (Throwable e) {
				DesignerPlugin.log(e);
			}
		}
		return result;
	}

	private static void addRequiredLibraries(IJavaProject javaProject) throws Exception {
		for (String symbolicName : ProjectUtils.getAllPluginLibraries()) {
			ProjectUtils.addPluginLibraries(javaProject, symbolicName);
		}
	}
}