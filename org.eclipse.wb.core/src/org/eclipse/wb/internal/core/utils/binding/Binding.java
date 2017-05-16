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
package org.eclipse.wb.internal.core.utils.binding;

import org.eclipse.core.runtime.IStatus;

/**
 * Container with information about single binding created by {@link DataBindManager}.
 *
 * @author scheglov_ke
 */
public final class Binding {
  private final IDataEditor m_editor;
  private final IDataProvider m_provider;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public Binding(IDataEditor editor, IDataProvider provider) {
    m_editor = editor;
    m_provider = provider;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Internal access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Updates {@link IDataEditor} with value from {@link IDataProvider}.
   *
   * @param def
   *          is <code>true</code> if default value should be requested from {@link IDataProvider}.
   */
  void updateEditor(boolean def) {
    Object value = m_provider.getValue(def);
    m_editor.setValue(value);
  }

  /**
   * Updates {@link IDataProvider} with value from {@link IDataEditor}.
   *
   * @return {@link IStatus} that shows if result of update was successful.
   */
  IStatus updateProvider() {
    try {
      Object value = m_editor.getValue();
      m_provider.setValue(value);
      return ValidationStatus.STATUS_OK;
    } catch (Throwable e) {
      return ValidationStatus.error(e.getMessage(), e);
    }
  }
}
