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

import org.eclipse.wb.core.databinding.xsd.component.MethodsOrderType;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.MethodDescription;
import org.eclipse.wb.internal.core.model.order.MethodOrder;
import org.eclipse.wb.internal.core.utils.check.Assert;

import org.apache.commons.lang3.function.FailableBiConsumer;

/**
 * The {@link FailableBiConsumer} that {@link MethodOrder} for multiple
 * {@link MethodDescription}s.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public final class MethodOrderMethodsRule
		implements FailableBiConsumer<ComponentDescription, MethodsOrderType.Methods, Exception> {
	////////////////////////////////////////////////////////////////////////////
	//
	// Rule
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void accept(ComponentDescription componentDescription, MethodsOrderType.Methods methods) throws Exception {
		// prepare order
		String specification = methods.getOrder();
		MethodOrder m_order = MethodOrder.parse(specification);
		//
		for (MethodsOrderType.Methods.S signature : methods.getS()) {
			// prepare method
			MethodDescription methodDescription;
			{
				methodDescription = componentDescription.getMethod(signature.getValue());
				Assert.isNotNull(
						methodDescription,
						"Can not find method %s for %s.",
						signature,
						componentDescription);
			}
			// set order
			methodDescription.setOrder(m_order);
		}
	}
}
