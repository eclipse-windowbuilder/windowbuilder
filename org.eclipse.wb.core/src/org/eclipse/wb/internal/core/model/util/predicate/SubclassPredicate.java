/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
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
package org.eclipse.wb.internal.core.model.util.predicate;

import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import java.util.function.Predicate;

/**
 * {@link Predicate} that checks that given {@link Object} has compatible class.
 *
 * @author scheglov_ke
 * @coverage core.model.util
 */
public final class SubclassPredicate implements Predicate<Object> {
	private final Class<?> m_superClass;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public SubclassPredicate(Class<?> superClass) {
		m_superClass = superClass;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Predicate
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean test(Object t) {
		return ReflectionUtils.isAssignableFrom(m_superClass, t);
	}
}
