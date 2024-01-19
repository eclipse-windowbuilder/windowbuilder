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
package org.eclipse.wb.internal.rcp.wizards.rcp;

import org.eclipse.wb.internal.rcp.Activator;
import org.eclipse.wb.internal.rcp.wizards.WizardsMessages;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;

/**
 * {@link WizardPage} that creates new {@link ViewPart}.
 *
 * @author lobas_av
 * @coverage rcp.wizards.ui
 */
public abstract class AbstractViewPartWizardPage extends RcpPartWizardPage {
	////////////////////////////////////////////////////////////////////////////
	//
	// WizardPage
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void createTypeMembers(IType newType, ImportsManager imports, IProgressMonitor monitor)
			throws CoreException {
		super.createTypeMembers(newType, imports, monitor);
		InputStream file = Activator.getFile(getCreateTemplate());
		fillTypeFromTemplate(newType, imports, monitor, file);
		if (m_pdeUtils != null) {
			try {
				m_pdeUtils.createViewElement(m_newTypeClassName, getNameText(), m_newTypeClassName);
			} catch (Throwable e) {
				throw new CoreException(new Status(IStatus.ERROR,
						Activator.PLUGIN_ID,
						IStatus.OK,
						WizardsMessages.AbstractViewPartWizardPage_errorPluginXml,
						e));
			}
		}
	}

	@Override
	protected String performSubstitutions(String code, ImportsManager imports) {
		code = super.performSubstitutions(code, imports);
		code = StringUtils.replace(code, "%VIEW_ID%", m_newTypeClassName);
		return code;
	}

	/**
	 * @return {@link String} path to template file for create new type.
	 */
	protected abstract String getCreateTemplate();

	////////////////////////////////////////////////////////////////////////////
	//
	// GUI
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void initTypePage(IJavaElement elem) {
		super.initTypePage(elem);
		setSuperClass("org.eclipse.ui.part.ViewPart", true);
	}

	@Override
	protected void createLocalControls(Composite parent, int columns) {
		createLocalControls(
				parent,
				columns,
				WizardsMessages.AbstractViewPartWizardPage_viewName,
				WizardsMessages.AbstractViewPartWizardPage_newViewPart);
	}
}