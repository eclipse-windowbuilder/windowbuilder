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
package org.eclipse.wb.internal.rcp.wizards.jface.application;

import org.eclipse.wb.internal.core.wizards.AbstractDesignWizardPage;
import org.eclipse.wb.internal.rcp.wizards.RcpWizard;
import org.eclipse.wb.internal.rcp.wizards.WizardsMessages;

import org.eclipse.jface.wizard.Wizard;

/**
 * {@link Wizard} that creates new JFace application.
 *
 * @author lobas_av
 * @coverage rcp.wizards.ui
 */
public final class JFaceApplicationWizard extends RcpWizard {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public JFaceApplicationWizard() {
		setWindowTitle(WizardsMessages.JFaceApplicationWizard_title);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Wizard
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected AbstractDesignWizardPage createMainPage() {
		return new JFaceApplicationWizardPage();
	}
}