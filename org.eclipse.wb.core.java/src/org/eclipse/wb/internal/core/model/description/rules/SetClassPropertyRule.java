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

import org.eclipse.wb.internal.core.model.description.ParameterDescription;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.apache.commons.lang3.function.FailableBiConsumer;

/**
 * The {@link FailableBiConsumer} that sets {@link Class} property with given
 * name.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public final class SetClassPropertyRule implements FailableBiConsumer<ParameterDescription, String, Exception> {
	private final ClassLoader m_classLoader;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	////////////////////////////////////////////////////////////////////////////
	public SetClassPropertyRule(ClassLoader classLoader) {
		m_classLoader = classLoader;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Rule
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void accept(ParameterDescription parameterDescription, String className) throws Exception {
		// prepare class
		Class<?> clazz;
		{
			Assert.isNotNull(className);
			clazz = ReflectionUtils.getClassByName(m_classLoader, className);
		}
		// set property
		parameterDescription.setType(clazz);
	}
}