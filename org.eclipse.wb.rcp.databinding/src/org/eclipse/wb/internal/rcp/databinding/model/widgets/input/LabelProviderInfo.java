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
package org.eclipse.wb.internal.rcp.databinding.model.widgets.input;

import org.eclipse.wb.internal.core.databinding.model.CodeGenerationSupport;
import org.eclipse.wb.internal.core.databinding.ui.editor.IUiContentProvider;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassConfiguration;
import org.eclipse.wb.internal.rcp.databinding.DatabindingsProvider;
import org.eclipse.wb.internal.rcp.databinding.Messages;
import org.eclipse.wb.internal.rcp.databinding.ui.contentproviders.SimpleClassUiContentProvider;

import java.util.List;

/**
 * Model for any simple <code>JFace<code> viewer label provider.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.widgets
 */
public class LabelProviderInfo extends AbstractLabelProviderInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public LabelProviderInfo(String className) {
		super(className);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Editing
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Create {@link IUiContentProvider} content providers for edit this model.
	 */
	@Override
	public final void createContentProviders(List<IUiContentProvider> providers,
			DatabindingsProvider provider,
			boolean useClear) {
		ChooseClassConfiguration configuration = new ChooseClassConfiguration();
		configuration.setDialogFieldLabel(Messages.LabelProviderInfo_label);
		configure(configuration, useClear);
		configuration.setEmptyClassErrorMessage(Messages.LabelProviderInfo_errorMessage);
		configuration.setErrorMessagePrefix(Messages.LabelProviderInfo_errorMessagePrefix);
		providers.add(new SimpleClassUiContentProvider(configuration, this));
	}

	/**
	 * Create configuration for edit this label provider.
	 */
	protected void configure(ChooseClassConfiguration configuration, boolean useClear) {
		configuration.setValueScope("org.eclipse.jface.viewers.IBaseLabelProvider");
		configuration.setBaseClassName("org.eclipse.jface.viewers.IBaseLabelProvider");
		configuration.setDefaultValues(new String[]{
				"org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider",
		"org.eclipse.jface.databinding.viewers.ObservableMapCellLabelProvider"});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Code generation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getSourceCode(List<String> lines, CodeGenerationSupport generationSupport)
			throws Exception {
		return "new " + m_className + "()";
	}
}