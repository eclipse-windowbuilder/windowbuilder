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
package org.eclipse.wb.internal.rcp.databinding.xwt.ui.property;

import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.ui.property.AbstractBindingProperty;
import org.eclipse.wb.internal.core.databinding.ui.property.Context;

/**
 * Property for bindable properties.
 *
 * @author lobas_av
 * @coverage bindings.xwt.ui.properties
 */
public class ObserveProperty
    extends
      org.eclipse.wb.internal.rcp.databinding.ui.property.ObserveProperty {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ObserveProperty(Context context, IObserveInfo observeProperty) throws Exception {
    super(context, observeProperty);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // AbstractObserveProperty
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public AbstractBindingProperty createBindingProperty() throws Exception {
    return new BindingProperty(m_context);
  }
}