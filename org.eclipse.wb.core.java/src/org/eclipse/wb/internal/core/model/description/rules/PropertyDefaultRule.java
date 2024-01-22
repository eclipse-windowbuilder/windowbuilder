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
import org.eclipse.wb.internal.core.model.description.GenericPropertyDescription;
import org.eclipse.wb.internal.core.model.util.ScriptUtils;

import org.apache.commons.lang3.function.FailableBiConsumer;

/**
 * The {@link FailableBiConsumer} that sets the default value of current
 * {@link GenericPropertyDescription}. Right now it supports fairly limited set
 * of expressions: boolean literals.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public final class PropertyDefaultRule
		implements FailableBiConsumer<GenericPropertyDescription, PropertyConfiguration.DefaultValue, Exception> {
	private final ClassLoader m_classLoader;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public PropertyDefaultRule(ClassLoader classLoader) {
		m_classLoader = classLoader;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Rule
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void accept(GenericPropertyDescription propertyDescription, PropertyConfiguration.DefaultValue defaultValue)
			throws Exception {
		String text = defaultValue.getValue();
		propertyDescription.setDefaultValue(getValue(text));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link Object} value for given text.
	 */
	private Object getValue(String text) throws Exception {
		return ScriptUtils.evaluate(m_classLoader, text);
	}
}
