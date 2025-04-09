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
package org.eclipse.wb.internal.rcp.wizards.rcp.view;

import org.eclipse.wb.internal.rcp.Activator;
import org.eclipse.wb.internal.rcp.wizards.WizardsMessages;
import org.eclipse.wb.internal.rcp.wizards.rcp.AbstractViewPartWizardPage;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ui.part.ViewPart;

/**
 * {@link WizardPage} that creates new RCP {@link ViewPart}.
 *
 * @author lobas_av
 * @coverage rcp.wizards.ui
 */
public final class ViewPartWizardPage extends AbstractViewPartWizardPage {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ViewPartWizardPage() {
		setTitle(WizardsMessages.ViewPartWizardPage_title);
		setImageDescriptor(Activator.getImageDescriptor("wizard/ViewPart/banner.gif"));
		setDescription(WizardsMessages.ViewPartWizardPage_description);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// WizardPage
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected String getCreateTemplate() {
		return "templates/rcp/ViewPart.jvt";
	}
}