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
package org.eclipse.wb.internal.rcp.preferences;

import org.eclipse.wb.internal.core.preferences.IPreferenceConstants;
import org.eclipse.wb.internal.core.preferences.bind.AbstractBindingPreferencesPage;
import org.eclipse.wb.internal.core.utils.binding.DataBindManager;
import org.eclipse.wb.internal.core.utils.ui.AbstractBindingComposite;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.rcp.ToolkitProvider;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Main {@link PreferencePage} for RCP.
 *
 * @author scheglov_ke
 * @coverage rcp.preferences.ui
 */
public final class MainPreferencePage extends AbstractBindingPreferencesPage {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public MainPreferencePage() {
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
  private class ContentsComposite extends AbstractBindingComposite {
    public ContentsComposite(Composite parent,
        DataBindManager bindManager,
        IPreferenceStore preferences) {
      super(parent, bindManager, preferences);
      GridLayoutFactory.create(this).noMargins().columns(2);
      // default size
      {
        {
          new Label(this, SWT.NONE).setText(PreferencesMessages.MainPreferencePage_defaultFormWidth);
          Text text = new Text(this, SWT.BORDER | SWT.RIGHT);
          GridDataFactory.create(text).grabH().fillH();
          bindInteger(text, IPreferenceConstants.P_GENERAL_DEFAULT_TOP_WIDTH);
        }
        {
          new Label(this, SWT.NONE).setText(PreferencesMessages.MainPreferencePage_defaultFormHeight);
          Text text = new Text(this, SWT.BORDER | SWT.RIGHT);
          GridDataFactory.create(text).grabH().fillH();
          bindInteger(text, IPreferenceConstants.P_GENERAL_DEFAULT_TOP_HEIGHT);
        }
      }
      // other, boolean preferences
      checkButton(
          this,
          2,
          PreferencesMessages.MainPreferencePage_highlightBorders,
          IPreferenceConstants.P_GENERAL_HIGHLIGHT_CONTAINERS);
      checkButton(
          this,
          2,
          PreferencesMessages.MainPreferencePage_showTextInComponentsTree,
          IPreferenceConstants.P_GENERAL_TEXT_SUFFIX);
      checkButton(
          this,
          2,
          PreferencesMessages.MainPreferencePage_showImportantProperties,
          IPreferenceConstants.P_GENERAL_IMPORTANT_PROPERTIES_AFTER_ADD);
      checkButton(
          this,
          2,
          PreferencesMessages.MainPreferencePage_autoDirectEdit,
          IPreferenceConstants.P_GENERAL_DIRECT_EDIT_AFTER_ADD);
      // SWT specific preferences
      checkButton(
          this,
          2,
          PreferencesMessages.MainPreferencePage_useResourceManager,
          org.eclipse.wb.internal.swt.preferences.IPreferenceConstants.P_USE_RESOURCE_MANAGER);
      checkButton(
          this,
          2,
          PreferencesMessages.MainPreferencePage_useCascadedStyleMenu,
          IPreferenceConstants.P_STYLE_PROPERTY_CASCADE_POPUP);
    }
  }
}