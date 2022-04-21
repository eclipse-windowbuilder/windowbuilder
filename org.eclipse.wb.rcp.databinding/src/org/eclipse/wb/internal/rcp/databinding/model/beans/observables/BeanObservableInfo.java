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
package org.eclipse.wb.internal.rcp.databinding.model.beans.observables;

import org.eclipse.wb.internal.rcp.databinding.model.BindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.IMasterDetailProvider;

/**
 * Abstract model for observable objects <code>BeansObservables.observeXXX(...)</code>.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.beans
 */
public abstract class BeanObservableInfo extends ObservableInfo implements IMasterDetailProvider {
  protected final BindableInfo m_bindableObject;
  protected final BindableInfo m_bindableProperty;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public BeanObservableInfo(BindableInfo bindableObject, BindableInfo bindableProperty) {
    m_bindableObject = bindableObject;
    m_bindableProperty = bindableProperty;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ObservableInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final BindableInfo getBindableObject() {
    return m_bindableObject;
  }

  @Override
  public final BindableInfo getBindableProperty() {
    return m_bindableProperty;
  }

  @Override
  public final boolean canShared() {
    return true;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IMasterDetailProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final ObservableInfo getMasterObservable() throws Exception {
    return this;
  }
}