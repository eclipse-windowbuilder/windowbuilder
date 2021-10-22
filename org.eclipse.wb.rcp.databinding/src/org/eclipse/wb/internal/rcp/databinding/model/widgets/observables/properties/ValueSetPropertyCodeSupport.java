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
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.DetailSetBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.properties.SetPropertyCodeSupport;

/**
 * Model for detail observable object <code>IValueProperty.set(ISetProperty)</code>.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.widgets
 */
public class ValueSetPropertyCodeSupport extends DetailPropertyCodeSupport {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ValueSetPropertyCodeSupport(ViewerPropertySingleSelectionCodeSupport selectionProperty,
      SetPropertyCodeSupport detailProperty) {
    super("org.eclipse.core.databinding.property.set.ISetProperty",
        "org.eclipse.core.databinding.observable.set.IObservableSet",
        "set",
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
    DetailSetBeanObservableInfo detailObservable =
        new DetailSetBeanObservableInfo(m_masterObservable,
            null,
            m_detailProperty.getParserPropertyReference(),
            m_detailProperty.getParserPropertyType());
    detailObservable.setPojoBindable(m_masterObservable.isPojoBindable());
    return detailObservable;
  }
}