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
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.rcp.databinding.DatabindingsProvider;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.AbstractViewerInputBindingInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.CheckedElementsObservableInfo;

/**
 * Content provider for edit (choose checked element type over dialog and combo)
 * {@link CheckedElementsObservableInfo}.
 *
 * @author lobas_av
 * @coverage bindings.rcp.ui
 */
public final class CheckedElementsUiContentProvider extends ChooseClassUiContentProvider {
  private final CheckedElementsObservableInfo m_observable;
  private final DatabindingsProvider m_provider;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CheckedElementsUiContentProvider(ChooseClassConfiguration configuration,
      CheckedElementsObservableInfo observable,
      DatabindingsProvider provider) {
    super(configuration);
    m_observable = observable;
    m_provider = provider;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Update
  //
  ////////////////////////////////////////////////////////////////////////////
  public void updateFromObject() throws Exception {
    // prepare element type
    Class<?> elementType = m_observable.getElementType();
    if (elementType == null) {
      elementType =
          AbstractViewerInputBindingInfo.getViewerInutElementType(m_observable, m_provider);
    }
    // set element type
    if (elementType == null) {
      calculateFinish();
    } else {
      setClassName(CoreUtils.getClassName(elementType));
    }
  }

  public void saveToObject() throws Exception {
    m_observable.setElementType(loadClass(getClassName()));
  }
}