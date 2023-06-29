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
package org.eclipse.wb.internal.rcp.databinding.wizards.autobindings;

import org.eclipse.wb.internal.core.databinding.wizards.autobindings.AutomaticDatabindingSecondPage;
import org.eclipse.wb.internal.core.databinding.wizards.autobindings.IAutomaticDatabindingProvider;
import org.eclipse.wb.internal.rcp.databinding.Messages;

/**
 * RCP Automatic bindings wizard.
 *
 * @author lobas_av
 * @coverage bindings.rcp.wizard.auto
 */
public final class AutomaticDatabindingWizard
extends
org.eclipse.wb.internal.core.databinding.wizards.autobindings.AutomaticDatabindingWizard {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public AutomaticDatabindingWizard() {
		setWindowTitle(Messages.AutomaticDatabindingWizard_windowTitle);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Pages
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void addPages() {
		IAutomaticDatabindingProvider databindingProvider = SwtDatabindingProvider.create();
		// prepare selection
		String beanClassName = getSelectionBeanClass(getSelection());
		// create first page: via standard "New Java Wizard"
		AutomaticDatabindingFirstPage firstPage =
				new AutomaticDatabindingFirstPage(databindingProvider, beanClassName);
		firstPage.setTitle(Messages.AutomaticDatabindingWizard_firstPageTitle);
		firstPage.setDescription(Messages.AutomaticDatabindingWizard_firstPageDescription);
		m_mainPage = firstPage;
		addPage(firstPage);
		firstPage.setInitialSelection(getSelection());
		// create second page: databindings
		AutomaticDatabindingSecondPage secondPage =
				new AutomaticDatabindingSecondPage(firstPage, databindingProvider, beanClassName);
		secondPage.setTitle(Messages.AutomaticDatabindingWizard_secondPageTitle);
		secondPage.setDescription(Messages.AutomaticDatabindingWizard_secondPageDescription);
		addPage(secondPage);
	}
}