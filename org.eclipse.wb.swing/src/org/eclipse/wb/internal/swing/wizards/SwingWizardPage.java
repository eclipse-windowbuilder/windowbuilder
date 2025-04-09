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
package org.eclipse.wb.internal.swing.wizards;

import org.eclipse.wb.internal.core.model.description.ToolkitDescriptionJava;
import org.eclipse.wb.internal.core.wizards.TemplateDesignWizardPage;
import org.eclipse.wb.internal.swing.ToolkitProvider;

import org.eclipse.swt.widgets.Composite;

import org.apache.commons.lang3.StringUtils;

/**
 * General wizard page for Swing wizard's.
 *
 * @author lobas_av
 * @coverage swing.wizards.ui
 */
public class SwingWizardPage extends TemplateDesignWizardPage {
	////////////////////////////////////////////////////////////////////////////
	//
	// Substitution support
	//
	////////////////////////////////////////////////////////////////////////////
	public static String doPerformSubstitutions(TemplateDesignWizardPage page,
			String code,
			ImportsManager imports) {
		code = StringUtils.replace(code, "%SwingLayout%", page.getLayoutCode("", imports));
		code =
				StringUtils.replace(
						code,
						"%ContentPane.SwingLayout%",
						page.getLayoutCode("getContentPane().", imports));
		return code;
	}

	@Override
	protected String performSubstitutions(String code, ImportsManager imports) {
		code = super.performSubstitutions(code, imports);
		code = doPerformSubstitutions(this, code, imports);
		return code;
	}

	@Override
	protected ToolkitDescriptionJava getToolkitDescription() {
		return ToolkitProvider.DESCRIPTION;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// GUI
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void createDesignSuperClassControls(Composite composite, int nColumns) {
		createSuperClassControls(composite, nColumns);
	}
}