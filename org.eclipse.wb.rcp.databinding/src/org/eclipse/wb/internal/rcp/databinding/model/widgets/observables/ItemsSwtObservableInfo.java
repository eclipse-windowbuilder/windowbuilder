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
import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables.WidgetBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables.WidgetPropertyBindableInfo;

/**
 * Model for observable object <code>SWTObservables.observeItems(Control)</code>.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.widgets
 */
public final class ItemsSwtObservableInfo extends ObservableInfo {
  protected final WidgetBindableInfo m_bindableWidget;
  protected final WidgetPropertyBindableInfo m_bindableProperty;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ItemsSwtObservableInfo(WidgetBindableInfo bindableWidget,
      WidgetPropertyBindableInfo bindableProperty) {
    m_bindableWidget = bindableWidget;
    m_bindableProperty = bindableProperty;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ObservableInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public BindableInfo getBindableObject() {
    return m_bindableWidget;
  }

  @Override
  public BindableInfo getBindableProperty() {
    return m_bindableProperty;
  }

  @Override
  public boolean canShared() {
    return true;
  }
}