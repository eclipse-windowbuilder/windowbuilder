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
package org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.properties;

import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.DetailBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.DetailValueBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.properties.ValuePropertyCodeSupport;

/**
 * Model for detail observable object <code>IValueProperty.value(IValueProperty)</code>.
 * 
 * @author lobas_av
 * @coverage bindings.rcp.model.widgets
 */
public class ValueValuePropertyCodeSupport extends DetailPropertyCodeSupport {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ValueValuePropertyCodeSupport(ViewerPropertySingleSelectionCodeSupport selectionProperty,
      ValuePropertyCodeSupport detailProperty) {
    super("org.eclipse.core.databinding.property.value.IValueProperty",
        "org.eclipse.core.databinding.observable.value.IObservableValue",
        "value",
        selectionProperty,
        detailProperty);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Parser
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected DetailBeanObservableInfo createDetailObservable() {
    return new DetailValueBeanObservableInfo(m_masterObservable,
        null,
        m_detailProperty.getParserPropertyReference(),
        m_detailProperty.getParserPropertyType());
  }
}