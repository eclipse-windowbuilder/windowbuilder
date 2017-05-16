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

import org.eclipse.swt.widgets.Composite;

/**
 * Base class of all content providers.
 *
 * @author lobas_av
 * @coverage bindings.ui
 */
public interface IUiContentProvider {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Complete
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets listener for notification of calculate state.
   */
  void setCompleteListener(ICompleteListener listener);

  /**
   * @return current error state message or <code>null</code>.
   */
  String getErrorMessage();

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the number of columns of the content provider.
   */
  int getNumberOfControls();

  /**
   * Creates all controls of the content provider and fills it to a composite. The composite is
   * assumed to have <code>GridLayout</code> as layout.
   */
  void createContent(Composite parent, int columns);

  ////////////////////////////////////////////////////////////////////////////
  //
  // Update
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Invoke for initialize all controls of the content provider from edit object.
   */
  void updateFromObject() throws Exception;

  /**
   * Invoke for save changes of edit object.
   */
  void saveToObject() throws Exception;
}