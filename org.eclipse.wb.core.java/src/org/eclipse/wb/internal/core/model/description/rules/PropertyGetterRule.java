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

import org.eclipse.wb.core.databinding.xsd.component.PropertyConfiguration;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.GenericPropertyDescription;
import org.eclipse.wb.internal.core.model.property.accessor.ExpressionAccessor;
import org.eclipse.wb.internal.core.model.property.accessor.SetterAccessor;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.apache.commons.lang3.function.FailableBiConsumer;

import java.lang.reflect.Method;

/**
 * The {@link FailableBiConsumer} to set "getter" for {@link SetterAccessor}.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public final class PropertyGetterRule
		implements FailableBiConsumer<GenericPropertyDescription, PropertyConfiguration.Getter, Exception> {
	private final ComponentDescription m_componentDescription;

	public PropertyGetterRule(ComponentDescription componentDescription) {
		m_componentDescription = componentDescription;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Rule
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void accept(GenericPropertyDescription propertyDescription, PropertyConfiguration.Getter getter)
			throws Exception {
		String getterName = getter.getName();
		Method getterMethod = ReflectionUtils.getMethod(m_componentDescription.getComponentClass(), getterName);
		for (ExpressionAccessor accessor : propertyDescription.getAccessorsList()) {
			if (accessor instanceof SetterAccessor setterAccessor) {
				setterAccessor.setGetter(getterMethod);
			}
		}
	}
}
