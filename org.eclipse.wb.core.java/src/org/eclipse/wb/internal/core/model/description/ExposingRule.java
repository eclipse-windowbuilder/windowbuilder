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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Filter for checking that some {@link Method} can be used to expose child.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public abstract class ExposingRule {
	/**
	 * @param method
	 *          the {@link Method} to filter.
	 *
	 * @return <code>true</code> if given {@link Method} can be used to expose child,
	 *         <code>false</code> - if can not be exposed, or <code>empty</code> if given
	 *         {@link Method} does not fall into this rule.
	 */
	public Optional<Boolean> filter(Method method) {
		return Optional.empty();
	}

	/**
	 * @param method
	 *          the {@link Field} to filter.
	 *
	 * @return <code>true</code> if given {@link Field} can be used to expose child,
	 *         <code>false</code> - if can not be exposed, or <code>empty</code> if given {@link Field}
	 *         does not fall into this rule.
	 */
	public Optional<Boolean> filter(Field field) {
		return Optional.empty();
	}
}
