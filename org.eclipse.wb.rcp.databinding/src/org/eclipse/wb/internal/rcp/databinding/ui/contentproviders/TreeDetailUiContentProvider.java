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
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.DetailBeanObservableInfo;

/**
 * Additional content provider for choose collection element type for
 * {@link DetailBeanObservableInfo} (over selection, multi selection, checkable) viewer input.
 * 
 * @author lobas_av
 * @coverage bindings.rcp.ui
 */
public final class TreeDetailUiContentProvider extends ChooseClassUiContentProvider {
  private final DetailBeanObservableInfo m_observable;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TreeDetailUiContentProvider(ChooseClassConfiguration configuration,
      DetailBeanObservableInfo observable) {
    super(configuration);
    m_observable = observable;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Update
  //
  ////////////////////////////////////////////////////////////////////////////
  public void updateFromObject() throws Exception {
  }

  public void saveToObject() throws Exception {
    m_observable.setDetailPropertyType(getChoosenClass());
  }
}