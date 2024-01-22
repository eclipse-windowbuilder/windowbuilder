/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.model.description.rules;

import org.eclipse.wb.core.databinding.xsd.component.DefaultMethodOrderType;
import org.eclipse.wb.core.databinding.xsd.component.MethodsOrderType;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.order.MethodOrder;

import org.apache.commons.lang3.function.FailableBiConsumer;

/**
 * The {@link FailableBiConsumer} that sets
 * {@link ComponentDescription#setDefaultMethodOrder(MethodOrder)}.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public final class MethodOrderDefaultRule
		implements FailableBiConsumer<ComponentDescription, MethodsOrderType.Default, Exception> {
	////////////////////////////////////////////////////////////////////////////
	//
	// Rule
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void accept(ComponentDescription componentDescription, MethodsOrderType.Default defaultMethod)
			throws Exception {
		DefaultMethodOrderType order = defaultMethod.getOrder();
		String specification = order.value();
		componentDescription.setDefaultMethodOrder(MethodOrder.parse(specification));
	}
}
