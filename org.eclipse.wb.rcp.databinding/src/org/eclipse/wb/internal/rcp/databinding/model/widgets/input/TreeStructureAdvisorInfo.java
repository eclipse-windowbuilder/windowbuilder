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
import org.eclipse.wb.internal.rcp.databinding.model.SimpleClassObjectInfo;
import org.eclipse.wb.internal.rcp.databinding.ui.contentproviders.SimpleClassUiContentProvider;

import org.apache.commons.lang.ArrayUtils;

import java.util.List;

/**
 * Model for {@link org.eclipse.jface.databinding.viewers.TreeStructureAdvisor}.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.widgets
 */
public class TreeStructureAdvisorInfo extends SimpleClassObjectInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TreeStructureAdvisorInfo(String className) {
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
  public final void createContentProviders(List<IUiContentProvider> providers,
      DatabindingsProvider provider) {
    ChooseClassConfiguration configuration = new ChooseClassConfiguration();
    configuration.setDialogFieldLabel(Messages.TreeStructureAdvisorInfo_label);
    configure(configuration);
    configuration.setEmptyClassErrorMessage(Messages.TreeStructureAdvisorInfo_errorMessage);
    configuration.setErrorMessagePrefix(Messages.TreeStructureAdvisorInfo_errorMessagePrefix);
    providers.add(new SimpleClassUiContentProvider(configuration, this));
  }

  /**
   * Create configuration for edit this advisor.
   */
  protected void configure(ChooseClassConfiguration configuration) {
    configuration.setValueScope("org.eclipse.jface.databinding.viewers.TreeStructureAdvisor");
    configuration.setBaseClassName("org.eclipse.jface.databinding.viewers.TreeStructureAdvisor");
    configuration.setConstructorParameters(ArrayUtils.EMPTY_CLASS_ARRAY);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Code generation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final void addSourceCode(List<String> lines, CodeGenerationSupport generationSupport)
      throws Exception {
    if (getVariableIdentifier() == null) {
      setVariableIdentifier(generationSupport.generateLocalName("treeAdvisor"));
    }
    addSourceCode(lines);
  }

  /**
   * Generate source code association with this object.
   */
  protected void addSourceCode(List<String> lines) throws Exception {
    lines.add("org.eclipse.jface.databinding.viewers.TreeStructureAdvisor "
        + getVariableIdentifier()
        + " = new "
        + m_className
        + "();");
  }
}