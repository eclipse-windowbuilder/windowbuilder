/*******************************************************************************
 * Copyright (c) 2011, 2026 Google, Inc. and others.
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
package org.eclipse.wb.internal.swing.wizards.panel;

import org.eclipse.wb.internal.swing.Activator;
import org.eclipse.wb.internal.swing.wizards.Messages;
import org.eclipse.wb.internal.swing.wizards.SwingWizardPage;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.wizard.WizardPage;

import java.io.InputStream;

import javax.swing.JPanel;

/**
 * {@link WizardPage} that creates new Swing {@link JPanel}.
 *
 * @author lobas_av
 * @coverage swing.wizards.ui
 */
public final class NewJPanelWizardPage extends SwingWizardPage {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public NewJPanelWizardPage() {
		setTitle(Messages.NewJPanelWizardPage_title);
		setImageDescriptor(Activator.getImageDescriptor("wizard/JPanel/banner.gif"));
		setDescription(Messages.NewJPanelWizardPage_description);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// WizardPage
	//
	////////////////////////////////////////////////////////////////////////////

	@Override
	protected void createTypeMembers(IType newType, ImportsManager imports, IProgressMonitor monitor)
			throws CoreException {
		InputStream file = Activator.getFile("templates/JPanel.jvt");
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
		setSuperClass("javax.swing.JPanel", true);
	}
}