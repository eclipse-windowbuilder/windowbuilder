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

/**
 * This adapter class provides default implementations for the non GUI methods described by the
 * {@link IUiContentProvider} interface.
 *
 * @author lobas_av
 * @coverage bindings.ui
 */
public abstract class UiContentProviderAdapter implements IUiContentProvider {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Complete
  //
  ////////////////////////////////////////////////////////////////////////////
  public void setCompleteListener(ICompleteListener listener) {
  }

  public String getErrorMessage() {
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Update
  //
  ////////////////////////////////////////////////////////////////////////////
  public void updateFromObject() throws Exception {
  }

  public void saveToObject() throws Exception {
  }
}