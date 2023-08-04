/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
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
import org.eclipse.wb.internal.core.databinding.ui.property.Context;
import org.eclipse.wb.internal.core.databinding.ui.property.SingleObserveBindingProperty;
import org.eclipse.wb.internal.swing.databinding.model.ObserveInfo;
import org.eclipse.wb.internal.swing.databinding.model.bindings.BindingInfo;
import org.eclipse.wb.internal.swing.databinding.model.bindings.JComboBoxBindingInfo;
import org.eclipse.wb.internal.swing.databinding.ui.providers.BindingLabelProvider;

import java.util.List;

/**
 * Property for {@link JComboBoxBindingInfo}.
 *
 * @author lobas_av
 * @coverage bindings.swing.ui.properties
 */
public class JComboBoxSelfObserveProperty extends SingleObserveBindingProperty {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public JComboBoxSelfObserveProperty(Context context, IObserveInfo observeProperty)
			throws Exception {
		super(context, observeProperty);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// SingleObserveBindingProperty
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected IBindingInfo getBinding() throws Exception {
		ObserveInfo observeProperty = (ObserveInfo) m_observeProperty;
		List<BindingInfo> bindings = observeProperty.getBindings();
		return bindings.isEmpty() ? null : bindings.get(0);
	}

	@Override
	protected String getText() throws Exception {
		IBindingInfo binding = getBinding();
		if (binding == null) {
			return "";
		}
		int column = binding.getTargetProperty() == m_observeProperty ? 2 : 1;
		return BindingLabelProvider.getText(binding, column);
	}
}