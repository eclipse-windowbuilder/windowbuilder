/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
