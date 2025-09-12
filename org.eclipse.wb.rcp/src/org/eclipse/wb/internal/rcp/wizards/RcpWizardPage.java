/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
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
package org.eclipse.wb.internal.rcp.wizards;

import org.eclipse.wb.internal.core.model.description.ToolkitDescriptionJava;
import org.eclipse.wb.internal.core.wizards.TemplateDesignWizardPage;
import org.eclipse.wb.internal.rcp.ToolkitProvider;

import org.eclipse.swt.widgets.Composite;

/**
 * General wizard page for RCP wizard's.
 *
 * @author lobas_av
 * @coverage rcp.wizards.ui
 */
public class RcpWizardPage extends TemplateDesignWizardPage {
	////////////////////////////////////////////////////////////////////////////
	//
	// Substitution support
	//
	////////////////////////////////////////////////////////////////////////////
	public static String doPerformSubstitutions(TemplateDesignWizardPage page,
			String code,
			ImportsManager imports) {
		code = code.replace("%CreateMethod%", page.getCreateMethod("createContents"));
		code = code.replace("%SWTLayout%", page.getLayoutCode("", imports));
		code = code.replace("%shell.SWTLayout%", page.getLayoutCode("shell.", imports));
		code =
				code.replace(
						"%container.SWTLayout%",
						page.getLayoutCode("container.", imports));
		code =
				code.replace(
						"%field-prefix-shell.SWTLayout%",
						page.getLayoutCode("%field-prefix%shell.", imports));
		code =
				code.replace(
						"%field-prefix-container.SWTLayout%",
						page.getLayoutCode("%field-prefix%container.", imports));
		code = performFieldPrefixesSubstitutions(code);
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