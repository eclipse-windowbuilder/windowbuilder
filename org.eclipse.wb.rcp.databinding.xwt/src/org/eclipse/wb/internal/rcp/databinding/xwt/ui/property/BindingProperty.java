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
package org.eclipse.wb.internal.rcp.databinding.xwt.ui.property;

import org.eclipse.wb.internal.core.databinding.ui.property.AbstractBindingProperty;
import org.eclipse.wb.internal.core.databinding.ui.property.Context;
import org.eclipse.wb.internal.rcp.databinding.xwt.ui.providers.BindingLabelProvider;

/**
 * Property for single binding.
 *
 * @author lobas_av
 * @coverage bindings.xwt.ui.properties
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