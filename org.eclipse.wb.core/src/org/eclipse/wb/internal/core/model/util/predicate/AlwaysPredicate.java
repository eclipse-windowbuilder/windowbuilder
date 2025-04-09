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

import java.util.function.Predicate;

/**
 * {@link Predicate} always returns same value.
 *
 * @author scheglov_ke
 * @coverage core.model.util
 */
public final class AlwaysPredicate<T> implements Predicate<T> {
	private final boolean m_value;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public AlwaysPredicate(boolean value) {
		m_value = value;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Predicate
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean test(T t) {
		return m_value;
	}
}
