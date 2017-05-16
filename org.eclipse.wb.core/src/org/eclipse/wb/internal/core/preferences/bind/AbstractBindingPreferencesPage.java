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
package org.eclipse.wb.internal.core.preferences.bind;

import org.eclipse.wb.internal.core.model.description.ToolkitDescription;
import org.eclipse.wb.internal.core.utils.binding.DataBindManager;
import org.eclipse.wb.internal.core.utils.ui.AbstractBindingComposite;
import org.eclipse.wb.internal.core.utils.ui.AbstractBindingComposite.IValidationListener;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * {@link PreferencePage} with single {@link AbstractBindingComposite} as contents.
 *
 * @author scheglov_ke
 * @coverage core.preferences.ui
 */
public abstract class AbstractBindingPreferencesPage extends PreferencePage
    implements
      IWorkbenchPreferencePage {
  protected final DataBindManager m_bindManager = new DataBindManager();
  protected final ToolkitDescription m_toolkit;
  protected final IPreferenceStore m_preferences;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractBindingPreferencesPage(ToolkitDescription toolkit) {
    m_toolkit = toolkit;
    m_preferences = m_toolkit.getPreferences();
  }

  public AbstractBindingPreferencesPage(IPreferenceStore preferences) {
    m_toolkit = null;
    m_preferences = preferences;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  private AbstractBindingComposite m_composite;

  @Override
  protected final Control createContents(Composite parent) {
    Composite contents = new Composite(parent, SWT.NONE);
    contents.setLayout(new FillLayout());
    //
    m_composite = createBindingComposite(contents);
    m_composite.setValidationListener(new IValidationListener() {
      public void update(String message) {
        setErrorMessage(message);
        setValid(message == null);
      }
    });
    m_composite.updateValidate();
    //
    return contents;
  }

  /**
   * Creates single {@link AbstractBindingComposite} on given parent.
   */
  protected abstract AbstractBindingComposite createBindingComposite(Composite parent);

  ////////////////////////////////////////////////////////////////////////////
  //
  // State
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final boolean performOk() {
    if (!m_composite.performOk()) {
      return false;
    }
    m_bindManager.performCommit();
    return super.performOk();
  }

  @Override
  protected void performDefaults() {
    m_composite.performDefaults();
    m_bindManager.performDefault();
    super.performDefaults();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IWorkbenchPreferencePage
  //
  ////////////////////////////////////////////////////////////////////////////
  public final void init(IWorkbench workbench) {
  }
}