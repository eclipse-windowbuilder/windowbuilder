/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc.
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
package org.eclipse.wb.internal.swing.wizards.applet;

import org.eclipse.wb.internal.core.wizards.AbstractDesignWizardPage;
import org.eclipse.wb.internal.swing.wizards.Messages;
import org.eclipse.wb.internal.swing.wizards.SwingWizard;

import org.eclipse.jface.wizard.Wizard;

import javax.swing.JApplet;

/**
 * {@link Wizard} that creates new Swing {@link JApplet}.
 *
 * @author lobas_av
 * @coverage swing.wizards.ui
 */
public final class NewJAppletWizard extends SwingWizard {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public NewJAppletWizard() {
		setWindowTitle(Messages.NewJAppletWizard_title);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Pages
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected AbstractDesignWizardPage createMainPage() {
		return new NewJAppletWizardPage();
	}
}