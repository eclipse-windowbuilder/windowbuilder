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
package org.eclipse.wb.internal.swing.databinding.wizards.autobindings;

import org.eclipse.wb.internal.core.databinding.wizards.autobindings.IAutomaticDatabindingProvider;
import org.eclipse.wb.internal.core.model.description.ToolkitDescriptionJava;
import org.eclipse.wb.internal.swing.ToolkitProvider;
import org.eclipse.wb.internal.swing.wizards.SwingWizardPage;

/**
 * Standard "New Java Class" wizard page.
 *
 * @author lobas_av
 * @coverage bindings.swing.wizard.auto
 */
public final class AutomaticDatabindingFirstPage
extends
org.eclipse.wb.internal.core.databinding.wizards.autobindings.AutomaticDatabindingFirstPage {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public AutomaticDatabindingFirstPage(IAutomaticDatabindingProvider databindingProvider,
			String initialBeanClassName) {
		super(databindingProvider, initialBeanClassName);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Substitution support
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected String performSubstitutions(String code, ImportsManager imports) {
		code = super.performSubstitutions(code, imports);
		code = SwingWizardPage.doPerformSubstitutions(this, code, imports);
		return code;
	}

	@Override
	protected ToolkitDescriptionJava getToolkitDescription() {
		return ToolkitProvider.DESCRIPTION;
	}
}