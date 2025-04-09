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

import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Implementation of {@link ExposingRule} that checks package of {@link Class} declaring given
 * {@link Method}.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public final class ExposingPackageRule extends ExposingRule {
	private final boolean m_include;
	private final String m_packageName;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ExposingPackageRule(boolean include, String packageName) {
		m_include = include;
		m_packageName = packageName;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Filter
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Optional<Boolean> filter(Method method) {
		String packageName = CodeUtils.getPackage(method.getDeclaringClass().getName());
		if (packageName.equals(m_packageName)) {
			return Optional.of(m_include);
		}
		return Optional.empty();
	}

	@Override
	public Optional<Boolean> filter(Field field) {
		String packageName = CodeUtils.getPackage(field.getDeclaringClass().getName());
		if (packageName.equals(m_packageName)) {
			return Optional.of(m_include);
		}
		return Optional.empty();
	}
}
