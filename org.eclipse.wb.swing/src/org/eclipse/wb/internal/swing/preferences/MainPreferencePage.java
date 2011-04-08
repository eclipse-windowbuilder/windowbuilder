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
package org.eclipse.wb.internal.swing.preferences;

import org.eclipse.wb.internal.core.preferences.IPreferenceConstants;
import org.eclipse.wb.internal.core.preferences.bind.AbstractBindingPreferencesPage;
import org.eclipse.wb.internal.core.utils.binding.DataBindManager;
import org.eclipse.wb.internal.core.utils.ui.AbstractBindingComposite;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.swing.ToolkitProvider;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * {@link PreferencePage} with general Swing preferences.
 * 
 * @author scheglov_ke
 * @coverage swing.preferences.ui
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
  private static class ContentsComposite extends AbstractBindingComposite {
    public ContentsComposite(Composite parent,
        DataBindManager bindManager,
        IPreferenceStore preferences) {
      super(parent, bindManager, preferences);
      GridLayoutFactory.create(this).noMargins().columns(2);
      // default size
      {
        {
          new Label(this, SWT.NONE).setText(Messages.MainPreferencePage_defaultWidth);
          Text text = new Text(this, SWT.BORDER | SWT.RIGHT);
          GridDataFactory.create(text).grabH().fillH();
          bindInteger(text, IPreferenceConstants.P_GENERAL_DEFAULT_TOP_WIDTH);
        }
        {
          new Label(this, SWT.NONE).setText(Messages.MainPreferencePage_defaultHeight);
          Text text = new Text(this, SWT.BORDER | SWT.RIGHT);
          GridDataFactory.create(text).grabH().fillH();
          bindInteger(text, IPreferenceConstants.P_GENERAL_DEFAULT_TOP_HEIGHT);
        }
      }
      // other, boolean preferences
      checkButton(
          this,
          2,
          Messages.MainPreferencePage_highlightBorders,
          IPreferenceConstants.P_GENERAL_HIGHLIGHT_CONTAINERS);
      checkButton(
          this,
          2,
          Messages.MainPreferencePage_showTextInTree,
          IPreferenceConstants.P_GENERAL_TEXT_SUFFIX);
      checkButton(
          this,
          2,
          Messages.MainPreferencePage_showImportProperties,
          IPreferenceConstants.P_GENERAL_IMPORTANT_PROPERTIES_AFTER_ADD);
      checkButton(
          this,
          2,
          Messages.MainPreferencePage_autoDirectEdit,
          IPreferenceConstants.P_GENERAL_DIRECT_EDIT_AFTER_ADD);
    }
  }
}
