/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
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
package org.eclipse.wb.internal.rcp.wizards.jface.wizard;

import org.eclipse.wb.internal.rcp.Activator;
import org.eclipse.wb.internal.rcp.wizards.RcpWizardPage;
import org.eclipse.wb.internal.rcp.wizards.WizardsMessages;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.wizard.WizardPage;

import java.io.InputStream;

/**
 * {@link WizardPage} that creates new JFace {@link WizardPage}.
 *
 * @author lobas_av
 * @coverage rcp.wizards.ui
 */
public final class WizardPageWizardPage extends RcpWizardPage {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public WizardPageWizardPage() {
		setTitle(WizardsMessages.WizardPageWizardPage_title);
		setImageDescriptor(Activator.getImageDescriptor("wizard/JFace/WizardPage/banner.gif"));
		setDescription(WizardsMessages.WizardPageWizardPage_description);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// WizardPage
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void createTypeMembers(IType newType, ImportsManager imports, IProgressMonitor monitor)
			throws CoreException {
		InputStream file = Activator.getFile("templates/jface/WizardPage.jvt");
		fillTypeFromTemplate(newType, imports, monitor, file);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// GUI
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void initTypePage(IJavaElement elem) {
		super.initTypePage(elem);
		setSuperClass("org.eclipse.jface.wizard.WizardPage", true);
	}
}