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
package org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders;

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.databinding.ui.editor.ICompleteListener;
import org.eclipse.wb.internal.core.databinding.ui.editor.IUiContentProvider;

import org.eclipse.swt.widgets.Composite;

import java.util.List;

/**
 * {@link IUiContentProvider} which is a container for other {@link IUiContentProvider}'s.
 *
 * @author lobas_av
 * @coverage bindings.ui
 */
public abstract class UIContentContainer<T> implements IUiContentProvider {
  protected final T m_binding;
  private final String m_errorPrefix;
  protected final List<IUiContentProvider> m_providers = Lists.newArrayList();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public UIContentContainer(T binding, String errorPrefix) {
    m_binding = binding;
    m_errorPrefix = errorPrefix;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public T getBinding() {
    return m_binding;
  }

  public List<IUiContentProvider> getProviders() {
    return m_providers;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Complete
  //
  ////////////////////////////////////////////////////////////////////////////
  public void setCompleteListener(ICompleteListener listener) {
    for (IUiContentProvider provider : m_providers) {
      provider.setCompleteListener(listener);
    }
  }

  public String getErrorMessage() {
    for (IUiContentProvider provider : m_providers) {
      String errorMessage = provider.getErrorMessage();
      if (errorMessage != null) {
        return m_errorPrefix + errorMessage;
      }
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  public int getNumberOfControls() {
    int columns = 0;
    for (IUiContentProvider provider : m_providers) {
      columns = Math.max(columns, provider.getNumberOfControls());
    }
    return columns;
  }

  public void createContent(Composite parent, int columns) {
    for (IUiContentProvider provider : m_providers) {
      provider.createContent(parent, columns);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Update
  //
  ////////////////////////////////////////////////////////////////////////////
  public void updateFromObject() throws Exception {
    for (IUiContentProvider provider : m_providers) {
      provider.updateFromObject();
    }
  }

  public void saveToObject() throws Exception {
    for (IUiContentProvider provider : m_providers) {
      provider.saveToObject();
    }
  }
}