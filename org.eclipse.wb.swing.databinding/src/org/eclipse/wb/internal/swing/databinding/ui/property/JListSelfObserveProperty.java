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

import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.ui.property.AbstractBindingProperty;
import org.eclipse.wb.internal.core.databinding.ui.property.Context;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.swing.databinding.Messages;
import org.eclipse.wb.internal.swing.databinding.model.bindings.JListBindingInfo;

/**
 * Property for {@link JListBindingInfo}.
 *
 * @author lobas_av
 * @coverage bindings.swing.ui.properties
 */
public class JListSelfObserveProperty extends ObserveProperty {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public JListSelfObserveProperty(Context context, IObserveInfo observeProperty) throws Exception {
		super(context, observeProperty);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Operation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void createBinding() throws Exception {
		if (isModified()) {
			for (AbstractBindingProperty property : m_bindingProperties) {
				if (property.getBinding() instanceof JListBindingInfo) {
					property.editBinding();
					return;
				}
			}
			Assert.fail(Messages.JListSelfObserveProperty_errNotFound);
		} else {
			super.createBinding();
		}
	}
}