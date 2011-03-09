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
package org.eclipse.wb.internal.rcp.databinding.ui.contentproviders;

import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassConfiguration;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassUiContentProvider;
import org.eclipse.wb.internal.rcp.databinding.model.context.strategies.UpdateStrategyInfo;

/**
 * Content provider for edit (choose strategy class over dialog and combo)
 * {@link UpdateStrategyInfo}.
 * 
 * @author lobas_av
 * @coverage bindings.rcp.ui
 */
public final class UpdateStrategyUiContentProvider extends ChooseClassUiContentProvider {
  private final UpdateStrategyInfo m_strategy;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public UpdateStrategyUiContentProvider(ChooseClassConfiguration configuration,
      UpdateStrategyInfo strategy) {
    super(configuration);
    m_strategy = strategy;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Update
  //
  ////////////////////////////////////////////////////////////////////////////
  public void updateFromObject() {
    setClassName(m_strategy.getStringValue());
  }

  public void saveToObject() {
    m_strategy.setStringValue(getClassName());
  }
}