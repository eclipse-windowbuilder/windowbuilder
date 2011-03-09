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
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassConfiguration;

import java.util.List;

/**
 * Model for {@link org.eclipse.jface.databinding.viewers.ObservableSetTreeContentProvider}.
 * 
 * @author lobas_av
 * @coverage bindings.rcp.model.widgets
 */
public final class ObservableSetTreeContentProviderInfo
    extends
      ObservableCollectionTreeContentProviderInfo {
  private static final String PROVIDER_CLASS =
      "org.eclipse.jface.databinding.viewers.ObservableSetTreeContentProvider";

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public ObservableSetTreeContentProviderInfo(String className,
      ObservableFactoryInfo factoryInfo,
      TreeStructureAdvisorInfo advisorInfo) {
    super(className, factoryInfo, advisorInfo);
  }

  public ObservableSetTreeContentProviderInfo(ObservableFactoryInfo factoryInfo,
      TreeStructureAdvisorInfo advisorInfo) {
    this(PROVIDER_CLASS, factoryInfo, advisorInfo);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void configure(ChooseClassConfiguration configuration, boolean useClear) {
    configuration.setValueScope(PROVIDER_CLASS);
    if (useClear) {
      configuration.setClearValue(PROVIDER_CLASS);
    }
    configuration.setBaseClassName(PROVIDER_CLASS);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Code generation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void addSourceCode(List<String> lines, CodeGenerationSupport generationSupport)
      throws Exception {
    super.addSourceCode(lines, generationSupport);
    // add code
    lines.add("org.eclipse.jface.databinding.viewers.ObservableSetTreeContentProvider "
        + getVariableIdentifier()
        + " = new "
        + m_className
        + "("
        + m_factoryInfo.getVariableIdentifier()
        + ", "
        + m_advisorInfo.getVariableIdentifier()
        + ");");
  }
}