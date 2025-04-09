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

import org.mvel2.MVEL;

import java.util.function.Predicate;

/**
 * {@link Predicate} that evaluates its value using some script expressions, currently using MVEL.
 *
 * @author scheglov_ke
 * @coverage core.model.util
 */
public final class ExpressionPredicate<T> implements Predicate<T> {
	private final String m_expression;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ExpressionPredicate(String expression) {
		m_expression = expression;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Object
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String toString() {
		return m_expression;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Predicate
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean test(T t) {
		return MVEL.evalToBoolean(m_expression, t);
	}
}
