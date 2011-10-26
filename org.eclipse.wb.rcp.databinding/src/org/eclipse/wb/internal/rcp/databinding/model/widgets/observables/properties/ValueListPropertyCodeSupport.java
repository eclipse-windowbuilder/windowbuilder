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
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.DetailListBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.properties.ListPropertyCodeSupport;

/**
 * Model for detail observable object <code>IValueProperty.list(IListProperty)</code>.
 * 
 * @author lobas_av
 * @coverage bindings.rcp.model.widgets
 */
public class ValueListPropertyCodeSupport extends DetailPropertyCodeSupport {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ValueListPropertyCodeSupport(ViewerPropertySingleSelectionCodeSupport selectionProperty,
      ListPropertyCodeSupport detailProperty) {
    super("org.eclipse.core.databinding.property.list.IListProperty",
        "org.eclipse.core.databinding.observable.list.IObservableList",
        "list",
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
    DetailListBeanObservableInfo detailObservable =
        new DetailListBeanObservableInfo(m_masterObservable,
            null,
            m_detailProperty.getParserPropertyReference(),
            m_detailProperty.getParserPropertyType());
    detailObservable.setPojoBindable(m_masterObservable.isPojoBindable());
    return detailObservable;
  }
}