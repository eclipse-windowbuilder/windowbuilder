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

import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.rcp.databinding.model.BindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;

/**
 * Abstract model for observable objects <code>ViewersObservables.observeXXX(...)</code>.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.widgets
 */
public abstract class ViewerObservableInfo extends ObservableInfo {
  protected final BindableInfo m_bindableWidget;
  protected final BindableInfo m_bindableProperty;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public ViewerObservableInfo(BindableInfo bindableWidget, BindableInfo bindableProperty)
      throws Exception {
    Assert.isNotNull(bindableProperty);
    m_bindableWidget = bindableWidget;
    m_bindableProperty = bindableProperty;
  }

  public ViewerObservableInfo(BindableInfo bindableWidget, String propertyReference)
      throws Exception {
    this(bindableWidget, bindableWidget.resolvePropertyReference(propertyReference));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ObservableInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final BindableInfo getBindableObject() {
    return m_bindableWidget;
  }

  @Override
  public final BindableInfo getBindableProperty() {
    return m_bindableProperty;
  }
}