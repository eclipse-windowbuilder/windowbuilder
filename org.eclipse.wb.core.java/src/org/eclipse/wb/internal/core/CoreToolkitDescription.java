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
package org.eclipse.wb.internal.core;

import org.eclipse.wb.core.branding.BrandingUtils;
import org.eclipse.wb.internal.core.model.description.ToolkitDescription;
import org.eclipse.wb.internal.core.model.description.ToolkitDescriptionJava;
import org.eclipse.wb.internal.core.model.generation.GenerationSettings;
import org.eclipse.wb.internal.core.model.generation.preview.GenerationPreview;
import org.eclipse.wb.internal.core.model.generation.preview.GenerationPreviewFieldInitializerBlock;
import org.eclipse.wb.internal.core.model.generation.preview.GenerationPreviewFieldInitializerFlat;
import org.eclipse.wb.internal.core.model.generation.preview.GenerationPreviewFieldUniqueFlat;
import org.eclipse.wb.internal.core.model.generation.preview.GenerationPreviewLazy;
import org.eclipse.wb.internal.core.model.generation.preview.GenerationPreviewLocalUniqueBlock;
import org.eclipse.wb.internal.core.model.generation.preview.GenerationPreviewLocalUniqueFlat;
import org.eclipse.wb.internal.core.model.generation.statement.StatementGeneratorDescription;
import org.eclipse.wb.internal.core.model.generation.statement.block.BlockStatementGeneratorDescription;
import org.eclipse.wb.internal.core.model.generation.statement.flat.FlatStatementGeneratorDescription;
import org.eclipse.wb.internal.core.model.generation.statement.lazy.LazyStatementGeneratorDescription;
import org.eclipse.wb.internal.core.model.variable.description.FieldInitializerVariableDescription;
import org.eclipse.wb.internal.core.model.variable.description.FieldUniqueVariableDescription;
import org.eclipse.wb.internal.core.model.variable.description.LazyVariableDescription;
import org.eclipse.wb.internal.core.model.variable.description.LocalUniqueVariableDescription;
import org.eclipse.wb.internal.core.preferences.IPreferenceConstants;

import org.eclipse.jface.preference.IPreferenceStore;

import org.osgi.framework.Bundle;

/**
 * {@link ToolkitDescription} for generic Java object's.
 *
 * @author scheglov_ke
 * @coverage code
 */
public final class CoreToolkitDescription extends ToolkitDescriptionJava {
  public static final ToolkitDescription INSTANCE = new CoreToolkitDescription();
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance fields
  //
  ////////////////////////////////////////////////////////////////////////////
  private final IPreferenceStore store = DesignerPlugin.getDefault().getPreferenceStore();
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
    return "Core Java toolkit";
  }

  @Override
  public String getProductName() {
    return BrandingUtils.getBranding().getProductName();
  }

  @Override
  public Bundle getBundle() {
    return DesignerPlugin.getDefault().getBundle();
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
        new GenerationPreview[]{GenerationPreviewFieldUniqueFlat.INSTANCE, null});
    // field with initializer
    settings.addGenerators(
        FieldInitializerVariableDescription.INSTANCE,
        usualStatements,
        new GenerationPreview[]{
            GenerationPreviewFieldInitializerFlat.INSTANCE,
            GenerationPreviewFieldInitializerBlock.INSTANCE});
    // lazy
    settings.addGenerators(
        LazyVariableDescription.INSTANCE,
        new StatementGeneratorDescription[]{LazyStatementGeneratorDescription.INSTANCE},
        new GenerationPreview[]{GenerationPreviewLazy.INSTANCE});
  }

  private void configureCodeGeneration() {
    settings.setDefaultDeduceSettings(true);
    settings.setDefaultVariable(FieldInitializerVariableDescription.INSTANCE);
    settings.setDefaultStatement(BlockStatementGeneratorDescription.INSTANCE);
  }

  private void configureTypeSpecific() {
  }
}
