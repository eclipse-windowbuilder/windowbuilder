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

import org.eclipse.wb.internal.rcp.databinding.model.BindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables.WidgetPropertyBindableInfo;

/**
 * Model for observable object <code>ViewersObservables.observeMultiSelection(...)</code>.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.widgets
 */
public final class MultiSelectionObservableInfo extends ViewerObservableInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public MultiSelectionObservableInfo(BindableInfo bindableWidget) throws Exception {
    super(bindableWidget, "observeMultiSelection");
  }

  /**
   * Note: this constructor used only for tests.
   */
  public MultiSelectionObservableInfo(BindableInfo bindableWidget,
      WidgetPropertyBindableInfo bindableProperty) throws Exception {
    super(bindableWidget, bindableProperty);
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

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getPresentationText() throws Exception {
    return getBindableObject().getPresentation().getTextForBinding() + ".multiSelection";
  }
}