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

import org.eclipse.wb.internal.core.utils.binding.DataBindManager;
import org.eclipse.wb.internal.core.utils.ui.AbstractBindingComposite;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.swing.ToolkitProvider;
import org.eclipse.wb.internal.swing.model.layout.LayoutNameSupport;
import org.eclipse.wb.internal.swing.preferences.IPreferenceConstants;
import org.eclipse.wb.internal.swing.preferences.Messages;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * Main {@link PreferencePage} for Layout Support.
 * 
 * @author scheglov_ke
 * @author sablin_aa
 * @coverage swing.preferences.ui
 */
public final class LayoutsPreferencePage
    extends
      org.eclipse.wb.internal.core.preferences.LayoutsPreferencePage {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public LayoutsPreferencePage() {
    super(ToolkitProvider.DESCRIPTION);
  }

  @Override
  protected AbstractBindingComposite createBindingComposite(Composite parent) {
    return new ContentsCompositeEx(parent, m_bindManager, m_preferences);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Contents
  //
  ////////////////////////////////////////////////////////////////////////////
  protected class ContentsCompositeEx extends ContentsComposite {
    public ContentsCompositeEx(Composite parent,
        DataBindManager bindManager,
        IPreferenceStore preferences) {
      super(parent, bindManager, preferences);
      // layout variable name template
      {
        new Label(this, SWT.NONE).setText(Messages.LayoutsPreferencePage_layoutNamePattern);
        // control
        Combo templateCombo = new Combo(this, SWT.READ_ONLY);
        GridDataFactory.create(templateCombo).grabH().fillH();
        templateCombo.setItems(LayoutNameSupport.TEMPLATES);
        // bind
        bindString(templateCombo, IPreferenceConstants.P_LAYOUT_NAME_TEMPLATE);
      }
    }
  }
}