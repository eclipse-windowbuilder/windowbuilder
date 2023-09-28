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
package org.eclipse.wb.internal.rcp;

import org.eclipse.wb.core.branding.BrandingUtils;
import org.eclipse.wb.internal.core.model.description.ToolkitDescription;
import org.eclipse.wb.internal.core.model.description.ToolkitDescriptionJava;
import org.eclipse.wb.internal.core.model.generation.GenerationSettings;
import org.eclipse.wb.internal.core.model.generation.preview.GenerationPreview;
import org.eclipse.wb.internal.core.model.generation.statement.StatementGeneratorDescription;
import org.eclipse.wb.internal.core.model.generation.statement.block.BlockStatementGeneratorDescription;
import org.eclipse.wb.internal.core.model.generation.statement.flat.FlatStatementGeneratorDescription;
import org.eclipse.wb.internal.core.model.variable.NamesManager;
import org.eclipse.wb.internal.core.model.variable.NamesManager.ComponentNameDescription;
import org.eclipse.wb.internal.core.model.variable.description.FieldUniqueVariableDescription;
import org.eclipse.wb.internal.core.model.variable.description.LocalUniqueVariableDescription;
import org.eclipse.wb.internal.rcp.model.ModelMessages;
import org.eclipse.wb.internal.rcp.preferences.IPreferenceConstants;
import org.eclipse.wb.internal.swt.model.generation.preview.GenerationPreviewFieldUniqueBlock;
import org.eclipse.wb.internal.swt.model.generation.preview.GenerationPreviewFieldUniqueFlat;
import org.eclipse.wb.internal.swt.model.generation.preview.GenerationPreviewLocalUniqueBlock;
import org.eclipse.wb.internal.swt.model.generation.preview.GenerationPreviewLocalUniqueFlat;

import org.eclipse.jface.preference.IPreferenceStore;

import org.osgi.framework.Bundle;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link ToolkitDescription} for RCP.
 *
 * @author scheglov_ke
 * @coverage rcp
 */
public final class RcpToolkitDescription extends ToolkitDescriptionJava {
	public static final ToolkitDescriptionJava INSTANCE = new RcpToolkitDescription();
	////////////////////////////////////////////////////////////////////////////
	//
	// Instance fields
	//
	////////////////////////////////////////////////////////////////////////////
	private final IPreferenceStore store = Activator.getDefault().getPreferenceStore();
	private final GenerationSettings settings = new GenerationSettings(store);
	////////////////////////////////////////////////////////////////////////////
	//
	// Initialization
	//
	////////////////////////////////////////////////////////////////////////////
	private boolean m_initialized;

	void initialize() {
		if (!m_initialized) {
			m_initialized = true;
			configureGenerators();
			configureCodeGeneration();
			configureTypeSpecific();
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getId() {
		return IPreferenceConstants.TOOLKIT_ID;
	}

	@Override
	public String getName() {
		return ModelMessages.RcpToolkitDescription_name;
	}

	@Override
	public String getProductName() {
		return BrandingUtils.getBranding().getProductName();
	}

	@Override
	public Bundle getBundle() {
		return Activator.getDefault().getBundle();
	}

	@Override
	public IPreferenceStore getPreferences() {
		return store;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Code generation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public GenerationSettings getGenerationSettings() {
		return settings;
	}

	private void configureGenerators() {
		StatementGeneratorDescription[] usualStatements =
				new StatementGeneratorDescription[]{
						FlatStatementGeneratorDescription.INSTANCE,
						BlockStatementGeneratorDescription.INSTANCE};
		// local unique
		settings.addGenerators(
				LocalUniqueVariableDescription.INSTANCE,
				usualStatements,
				new GenerationPreview[]{
						GenerationPreviewLocalUniqueFlat.INSTANCE,
						GenerationPreviewLocalUniqueBlock.INSTANCE});
		// field unique
		settings.addGenerators(
				FieldUniqueVariableDescription.INSTANCE,
				usualStatements,
				new GenerationPreview[]{
						GenerationPreviewFieldUniqueFlat.INSTANCE,
						GenerationPreviewFieldUniqueBlock.INSTANCE});
	}

	private void configureCodeGeneration() {
		settings.setDefaultDeduceSettings(true);
		settings.setDefaultVariable(LocalUniqueVariableDescription.INSTANCE);
		settings.setDefaultStatement(FlatStatementGeneratorDescription.INSTANCE);
	}

	private void configureTypeSpecific() {
		List<ComponentNameDescription> descriptions = new ArrayList<>();
		descriptions.add(new ComponentNameDescription("org.eclipse.swt.widgets.Text",
				"text",
				"txt",
				true));
		descriptions.add(new ComponentNameDescription("org.eclipse.swt.widgets.Table",
				"table",
				"tbl",
				true));
		NamesManager.setDefaultNameDescriptions(this, descriptions);
	}
}
