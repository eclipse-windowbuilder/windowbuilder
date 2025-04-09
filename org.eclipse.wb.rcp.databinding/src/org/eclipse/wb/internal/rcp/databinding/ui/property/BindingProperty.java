/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
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
package org.eclipse.wb.internal.rcp.databinding.ui.property;

import org.eclipse.wb.internal.core.databinding.ui.property.AbstractBindingProperty;
import org.eclipse.wb.internal.core.databinding.ui.property.Context;
import org.eclipse.wb.internal.rcp.databinding.ui.providers.BindingLabelProvider;

/**
 * Property for single binding.
 *
 * @author lobas_av
 * @coverage bindings.rcp.ui.properties
 */
public class BindingProperty extends AbstractBindingProperty {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public BindingProperty(Context context) {
		super(context);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// AbstractBindingProperty
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getText() throws Exception {
		int column = m_isTarget ? 2 : 1;
		return BindingLabelProvider.getText(m_binding, column);
	}
}