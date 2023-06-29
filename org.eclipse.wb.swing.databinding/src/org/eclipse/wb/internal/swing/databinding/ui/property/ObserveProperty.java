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
package org.eclipse.wb.internal.swing.databinding.ui.property;

import org.eclipse.wb.internal.core.databinding.model.IBindingInfo;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.ui.property.AbstractBindingProperty;
import org.eclipse.wb.internal.core.databinding.ui.property.AbstractObserveProperty;
import org.eclipse.wb.internal.core.databinding.ui.property.Context;
import org.eclipse.wb.internal.swing.databinding.model.ObserveInfo;

import java.util.List;

/**
 * Property for {@link ObserveInfo}.
 *
 * @author lobas_av
 * @coverage bindings.swing.ui.properties
 */
public class ObserveProperty extends AbstractObserveProperty {
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
	public void getBindings(List<IBindingInfo> bindings, List<Boolean> isTargets) throws Exception {
		ObserveInfo observeProperty = (ObserveInfo) m_observeProperty;
		bindings.addAll(observeProperty.getBindings());
		//
		for (IBindingInfo binding : bindings) {
			isTargets.add(binding.getTargetProperty() == m_observeProperty);
		}
	}

	@Override
	public AbstractBindingProperty createBindingProperty() throws Exception {
		return new BindingProperty(m_context);
	}
}