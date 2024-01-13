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

import org.eclipse.wb.core.databinding.xsd.component.PropertyConfiguration;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.GenericPropertyDescription;
import org.eclipse.wb.internal.core.model.description.helpers.ComponentDescriptionHelper.FailableBiConsumer;
import org.eclipse.wb.internal.core.model.property.accessor.ExpressionAccessor;
import org.eclipse.wb.internal.core.model.property.accessor.SetterAccessor;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

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
