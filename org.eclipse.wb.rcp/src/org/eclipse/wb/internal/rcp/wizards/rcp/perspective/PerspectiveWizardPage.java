/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.rcp.wizards.rcp.perspective;

import org.eclipse.wb.internal.rcp.Activator;
import org.eclipse.wb.internal.rcp.wizards.WizardsMessages;
import org.eclipse.wb.internal.rcp.wizards.rcp.RcpPartWizardPage;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link WizardPage} that creates new RCP Perspective.
 *
 * @author lobas_av
 * @coverage rcp.wizards.ui
 */
public final class PerspectiveWizardPage extends RcpPartWizardPage {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public PerspectiveWizardPage() {
		setTitle(WizardsMessages.PerspectiveWizardPage_title);
		setImageDescriptor(Activator.getImageDescriptor("wizard/Perspective/banner.gif"));
		setDescription(WizardsMessages.PerspectiveWizardPage_description);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// WizardPage
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void createTypeMembers(IType newType, ImportsManager imports, IProgressMonitor monitor)
			throws CoreException {
		super.createTypeMembers(newType, imports, monitor);
		InputStream file = Activator.getFile("templates/rcp/PerspectiveFactory.jvt");
		fillTypeFromTemplate(newType, imports, monitor, file);
		if (m_pdeUtils != null) {
			try {
				m_pdeUtils.createPerspectiveElement(m_newTypeClassName, getNameText(), m_newTypeClassName);
			} catch (Throwable e) {
				throw new CoreException(new Status(IStatus.ERROR,
						Activator.PLUGIN_ID,
						IStatus.OK,
						WizardsMessages.PerspectiveWizardPage_errorPluginXml,
						e));
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// GUI
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void createDesignSuperClassControls(Composite composite, int nColumns) {
	}

	@Override
	protected void initTypePage(IJavaElement elem) {
		super.initTypePage(elem);
		List<String> interfaces = new ArrayList<String>();
		interfaces.add("org.eclipse.ui.IPerspectiveFactory");
		setSuperInterfaces(interfaces, false);
	}

	@Override
	protected void createLocalControls(Composite parent, int columns) {
		createLocalControls(
				parent,
				columns,
				WizardsMessages.PerspectiveWizardPage_nameLabel,
				WizardsMessages.PerspectiveWizardPage_nameDefault);
	}
}