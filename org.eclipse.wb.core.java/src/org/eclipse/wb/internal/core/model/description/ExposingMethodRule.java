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
package org.eclipse.wb.internal.core.model.description;

import org.eclipse.wb.internal.core.utils.check.Assert;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Implementation of {@link ExposingRule} that name of given {@link Method}.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public final class ExposingMethodRule extends ExposingRule {
	private final boolean m_include;
	private final String m_methodName;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ExposingMethodRule(boolean include, String methodName) {
		m_include = include;
		m_methodName = methodName;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Filter
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Optional<Boolean> filter(Method method) {
		Assert.isLegal(method.getParameterTypes().length == 0, method.toString());
		// check method name
		if (!method.getName().equals(m_methodName)) {
			return Optional.empty();
		}
		// OK, method satisfies to filter, so include/exclude it
		return Optional.of(m_include);
	}
}
