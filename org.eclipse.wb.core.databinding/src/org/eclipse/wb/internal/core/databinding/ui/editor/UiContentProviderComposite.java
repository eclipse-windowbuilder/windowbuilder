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
package org.eclipse.wb.internal.core.databinding.ui.editor;

import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;

import org.eclipse.swt.widgets.Composite;

import java.util.List;

/**
 * {@link Composite} container for all content providers.
 *
 * @author lobas_av
 * @coverage bindings.ui
 */
public class UiContentProviderComposite extends Composite implements ICompleteListener {
  private final IPageListener m_listener;
  private final List<IUiContentProvider> m_providers;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public UiContentProviderComposite(IPageListener listener,
      List<IUiContentProvider> providers,
      Composite parent,
      int style) {
    super(parent, style);
    m_listener = listener;
    m_providers = providers;
    // calculate columns
    int columns = 0;
    for (IUiContentProvider provider : m_providers) {
      columns = Math.max(columns, provider.getNumberOfControls());
    }
    // create controls and fill composite
    GridLayoutFactory.create(this).columns(columns);
    for (IUiContentProvider provider : m_providers) {
      provider.setCompleteListener(this);
      provider.createContent(this, columns);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ICompleteListener
  //
  ////////////////////////////////////////////////////////////////////////////
  public void calculateFinish() {
    for (IUiContentProvider provider : m_providers) {
      String errorMessage = provider.getErrorMessage();
      // handle provider error
      if (errorMessage != null) {
        m_listener.setErrorMessage(errorMessage);
        m_listener.setPageComplete(false);
        return;
      }
    }
    // no errors
    m_listener.setErrorMessage(null);
    m_listener.setPageComplete(true);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Initialize/Finish
  //
  ////////////////////////////////////////////////////////////////////////////
  public void performInitialize() throws Exception {
    for (IUiContentProvider provider : m_providers) {
      provider.updateFromObject();
    }
    calculateFinish();
  }

  /**
   * Invoke for save changes for all content providers.
   */
  public void performFinish() throws Exception {
    for (IUiContentProvider provider : m_providers) {
      provider.saveToObject();
    }
  }
}