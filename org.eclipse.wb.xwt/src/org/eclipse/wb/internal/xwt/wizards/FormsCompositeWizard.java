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
package org.eclipse.wb.internal.xwt.wizards;

import org.eclipse.jface.wizard.Wizard;

/**
 * {@link Wizard} for new XWT Composite, with Forms API support.
 *
 * @author scheglov_ke
 * @coverage XWT.wizards
 */
public final class FormsCompositeWizard extends XwtWizard {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public FormsCompositeWizard() {
		setWindowTitle("New XWT Composite");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Wizard
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected XwtWizardPage createMainPage() {
		return new FormsCompositeWizardPage();
	}
}