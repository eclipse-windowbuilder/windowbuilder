/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
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