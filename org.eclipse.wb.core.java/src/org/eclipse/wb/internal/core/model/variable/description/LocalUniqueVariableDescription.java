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
package org.eclipse.wb.internal.core.model.variable.description;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.generation.GenerationPropertiesComposite;
import org.eclipse.wb.internal.core.model.variable.LocalUniqueVariableSupport;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.utils.binding.DataBindManager;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Composite;

/**
 * Implementation of {@link IVariableSupportDescription} for {@link LocalUniqueVariableSupport}.
 *
 * @author scheglov_ke
 * @coverage core.model.variable
 */
public class LocalUniqueVariableDescription extends VariableSupportDescription {
  public static final String ID = "org.eclipse.wb.core.model.variable.localUnique";
  public static final VariableSupportDescription INSTANCE = new LocalUniqueVariableDescription();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private LocalUniqueVariableDescription() {
    super(ID, "Local", "declare unique local variable with component, initialize at declaration");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // VariableSupportDescription
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Class<? extends VariableSupport> getType() {
    return LocalUniqueVariableSupport.class;
  }

  @Override
  public VariableSupport createSupport(JavaInfo javaInfo) {
    return new LocalUniqueVariableSupport(javaInfo);
  }

  @Override
  public void configureDefaultPreferences(IPreferenceStore store) {
    store.setDefault(LocalUniqueVariableSupport.P_DECLARE_FINAL, false);
  }

  @Override
  public GenerationPropertiesComposite createPropertiesComposite(Composite parent,
      DataBindManager bindManager,
      IPreferenceStore store) {
    return new PropertiesComposite(parent, bindManager, store);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties composite
  //
  ////////////////////////////////////////////////////////////////////////////
  private static class PropertiesComposite extends GenerationPropertiesComposite {
    public PropertiesComposite(Composite parent,
        DataBindManager bindManager,
        IPreferenceStore preferences) {
      super(parent, bindManager, preferences);
      checkButton(this, "Declare variable as \"final\"", LocalUniqueVariableSupport.P_DECLARE_FINAL);
    }
  }
}
