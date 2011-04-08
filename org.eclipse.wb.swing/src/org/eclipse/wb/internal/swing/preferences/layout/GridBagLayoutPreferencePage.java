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
package org.eclipse.wb.internal.swing.preferences.layout;

import org.eclipse.wb.internal.core.preferences.bind.AbstractBindingPreferencesPage;
import org.eclipse.wb.internal.core.utils.binding.DataBindManager;
import org.eclipse.wb.internal.core.utils.ui.AbstractBindingComposite;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.swing.ToolkitProvider;
import org.eclipse.wb.internal.swing.model.layout.gbl.AbstractGridBagLayoutInfo;
import org.eclipse.wb.internal.swing.model.layout.gbl.GridBagConstraintsNameSupport;
import org.eclipse.wb.internal.swing.model.layout.gbl.IPreferenceConstants;
import org.eclipse.wb.internal.swing.preferences.Messages;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * {@link PreferencePage} for {@link AbstractGridBagLayoutInfo}.
 * 
 * @author scheglov_ke
 * @coverage swing.preferences.ui
 */
public final class GridBagLayoutPreferencePage extends AbstractBindingPreferencesPage {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public GridBagLayoutPreferencePage() {
    super(ToolkitProvider.DESCRIPTION);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected AbstractBindingComposite createBindingComposite(Composite parent) {
    return new ContentsComposite(parent, m_bindManager, m_preferences);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Contents
  //
  ////////////////////////////////////////////////////////////////////////////
  private static class ContentsComposite extends AbstractBindingComposite {
    public ContentsComposite(Composite parent,
        DataBindManager bindManager,
        IPreferenceStore preferences) {
      super(parent, bindManager, preferences);
      GridLayoutFactory.create(this).noMargins().columns(2);
      // boolean preferences
      checkButton(
          this,
          2,
          Messages.GridBagLayoutPreferencePage_useLong,
          IPreferenceConstants.P_GBC_LONG);
      checkButton(
          this,
          2,
          Messages.GridBagLayoutPreferencePage_autoGrab,
          IPreferenceConstants.P_ENABLE_GRAB);
      checkButton(
          this,
          2,
          Messages.GridBagLayoutPreferencePage_rightAlign,
          IPreferenceConstants.P_ENABLE_RIGHT_ALIGNMENT);
      checkButton(
          this,
          2,
          Messages.GridBagLayoutPreferencePage_changeInsets,
          IPreferenceConstants.P_CHANGE_INSETS_FOR_GAPS);
      integerField(
          this,
          2,
          Messages.GridBagLayoutPreferencePage_columnGap,
          IPreferenceConstants.P_GAP_COLUMN);
      integerField(
          this,
          2,
          Messages.GridBagLayoutPreferencePage_rowGap,
          IPreferenceConstants.P_GAP_ROW);
      // gbl-constraints variable name template
      {
        new Label(this, SWT.NONE).setText(Messages.GridBagLayoutPreferencePage_constraintsNamePattern);
        // control
        Combo templateCombo = new Combo(this, SWT.READ_ONLY);
        GridDataFactory.create(templateCombo).grabH().fillH();
        templateCombo.setItems(GridBagConstraintsNameSupport.TEMPLATES);
        // bind
        bindString(templateCombo, IPreferenceConstants.P_CONSTRAINTS_NAME_TEMPLATE);
      }
    }
  }
}