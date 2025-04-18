/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc.
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
package org.eclipse.wb.internal.core.model.description.rules;

import org.eclipse.wb.core.databinding.xsd.component.MethodsOrderType;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.MethodDescription;
import org.eclipse.wb.internal.core.model.order.MethodOrder;
import org.eclipse.wb.internal.core.utils.check.Assert;

import org.apache.commons.lang3.function.FailableBiConsumer;

/**
 * The {@link FailableBiConsumer} that {@link MethodOrder} for single
 * {@link MethodDescription}.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public final class MethodOrderMethodRule
		implements FailableBiConsumer<ComponentDescription, MethodsOrderType.Method, Exception> {
	////////////////////////////////////////////////////////////////////////////
	//
	// Rule
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void accept(ComponentDescription componentDescription, MethodsOrderType.Method method) throws Exception {
		// prepare order
		MethodOrder order;
		{
			String specification = method.getOrder();
			order = MethodOrder.parse(specification);
		}
		// prepare method
		MethodDescription methodDescription;
		{
			String signature = method.getSignature();
			methodDescription = componentDescription.getMethod(signature);
			Assert.isNotNull(
					methodDescription,
					"Can not find method %s for %s.",
					signature,
					componentDescription);
		}
		// set order
		methodDescription.setOrder(order);
	}
}
