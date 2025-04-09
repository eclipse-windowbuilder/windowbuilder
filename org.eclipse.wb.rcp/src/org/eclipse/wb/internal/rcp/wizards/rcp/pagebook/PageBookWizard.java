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
package org.eclipse.wb.internal.rcp.wizards.rcp.pagebook;

import org.eclipse.wb.internal.core.wizards.AbstractDesignWizardPage;
import org.eclipse.wb.internal.rcp.wizards.RcpWizard;
import org.eclipse.wb.internal.rcp.wizards.WizardsMessages;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.part.PageBook;

/**
 * {@link Wizard} that creates new RCP {@link PageBook}.
 *
 * @author lobas_av
 * @coverage rcp.wizards.ui
 */
public final class PageBookWizard extends RcpWizard {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public PageBookWizard() {
		setWindowTitle(WizardsMessages.PageBookWizard_title);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Wizard
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected AbstractDesignWizardPage createMainPage() {
		return new PageBookWizardPage();
	}
}