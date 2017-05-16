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
package org.eclipse.wb.internal.core.model.generation.statement.flat;

import org.eclipse.wb.internal.core.model.generation.GenerationPropertiesComposite;
import org.eclipse.wb.internal.core.model.generation.statement.StatementGenerator;
import org.eclipse.wb.internal.core.model.generation.statement.StatementGeneratorDescription;
import org.eclipse.wb.internal.core.utils.binding.DataBindManager;
import org.eclipse.wb.internal.core.utils.binding.editors.controls.CheckButtonEditor;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/**
 * Implementation of {@link StatementGeneratorDescription} for {@link FlatStatementGenerator}.
 *
 * @author scheglov_ke
 * @coverage core.model.generation
 */
public final class FlatStatementGeneratorDescription extends StatementGeneratorDescription {
  public static final StatementGeneratorDescription INSTANCE =
      new FlatStatementGeneratorDescription();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private FlatStatementGeneratorDescription() {
    super("org.eclipse.wb.core.model.statement.flat", "Flat", "all components in same block");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // StatementGeneratorDescription
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public StatementGenerator get() {
    return FlatStatementGenerator.INSTANCE;
  }

  @Override
  public void configureDefaultPreferences(IPreferenceStore store) {
    store.setDefault(FlatStatementGenerator.P_USE_PREFIX, true);
    store.setDefault(FlatStatementGenerator.P_PREFIX_TEXT, "");
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
    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public PropertiesComposite(Composite parent,
        DataBindManager bindManager,
        IPreferenceStore preferences) {
      super(parent, bindManager, preferences);
      // prefix flag
      Button prefixFlagButton = new Button(this, SWT.CHECK);
      prefixFlagButton.setText("Prefix component creation code:");
      // prefix text
      Text prefixText = new Text(this, SWT.BORDER);
      GridDataFactory.create(prefixText).indentHC(3).grabH().fill();
      // bind
      {
        CheckButtonEditor prefixFlagEditor =
            bindBoolean(prefixFlagButton, FlatStatementGenerator.P_USE_PREFIX);
        prefixFlagEditor.addEnableControl(prefixText);
        bindString(prefixText, FlatStatementGenerator.P_PREFIX_TEXT);
      }
    }
  }
}
