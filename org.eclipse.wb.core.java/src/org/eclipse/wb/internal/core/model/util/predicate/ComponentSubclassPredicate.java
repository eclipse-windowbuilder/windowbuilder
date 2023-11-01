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

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import java.util.function.Predicate;

/**
 * {@link Predicate} that checks that given {@link Object} is {@link JavaInfo} with compatible
 * component class.
 *
 * @author scheglov_ke
 * @coverage core.model.util
 */
public final class ComponentSubclassPredicate implements Predicate<Object> {
	private final String m_superClass;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ComponentSubclassPredicate(String superClass) {
		m_superClass = superClass;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Object
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String toString() {
		return m_superClass;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Predicate
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean test(Object t) {
		if (t instanceof JavaInfo javaInfo) {
			Class<?> componentClass = javaInfo.getDescription().getComponentClass();
			return ReflectionUtils.isSuccessorOf(componentClass, m_superClass);
		}
		return false;
	}
}
