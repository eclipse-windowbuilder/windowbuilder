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

import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.xwt.Activator;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;

/**
 * {@link WizardPage} for {@link ApplicationWizard}, with Forms API support.
 *
 * @author scheglov_ke
 * @coverage XWT.wizards
 */
public final class FormsCompositeWizardPage extends XwtWizardPage {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public FormsCompositeWizardPage() {
		setTitle("Create XWT Composite");
		setImageDescriptor(Activator.getImageDescriptor("wizard/Composite/banner.png"));
		setDescription("Create a XWT Composite.");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Configuration
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void initTypePage(IJavaElement elem) {
		super.initTypePage(elem);
		setSuperClass("org.eclipse.swt.widgets.Composite", false);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Create
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected String getTemplatePath_Java() {
		return "templates/FormsComposite.jvt";
	}

	@Override
	protected String getTemplatePath_XWT() {
		return "templates/FormsComposite.xwt";
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// GUI
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void createLocalControls(Composite parent, int columns) {
		// I always use same names during tests
		if (EnvironmentUtils.DEVELOPER_HOST) {
			setTypeName("Composite_1", true);
		}
	}
}