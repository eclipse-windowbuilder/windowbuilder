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
package org.eclipse.wb.internal.swing.databinding.ui.property;

import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.ui.property.AbstractBindingProperty;
import org.eclipse.wb.internal.core.databinding.ui.property.Context;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.swing.databinding.Messages;
import org.eclipse.wb.internal.swing.databinding.model.bindings.JTableBindingInfo;

/**
 * Property for {@link JTableBindingInfo}.
 *
 * @author lobas_av
 * @coverage bindings.swing.ui.properties
 */
public class JTableSelfObserveProperty extends ObserveProperty {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public JTableSelfObserveProperty(Context context, IObserveInfo observeProperty) throws Exception {
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
				if (property.getBinding() instanceof JTableBindingInfo) {
					property.editBinding();
					return;
				}
			}
			Assert.fail(Messages.JTableSelfObserveProperty_errNotFound);
		} else {
			super.createBinding();
		}
	}
}