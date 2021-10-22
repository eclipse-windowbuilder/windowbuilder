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
package org.eclipse.wb.internal.rcp.databinding.model.widgets.observables;

import org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables.WidgetBindableInfo;

/**
 * Model for observable object <code>ViewersObservables.observeFilters(...)</code>.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.widgets
 */
public final class FiltersObservableInfo extends ViewerObservableInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public FiltersObservableInfo(WidgetBindableInfo bindableWidget) throws Exception {
    super(bindableWidget, "observeFilters");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ObservableInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean canShared() {
    return true;
  }
}