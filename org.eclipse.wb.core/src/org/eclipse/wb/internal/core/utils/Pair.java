/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
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
package org.eclipse.wb.internal.core.utils;

import java.util.Objects;

/**
 * Pair of two objects.
 *
 * @author scheglov_ke
 * @coverage core.util
 */
public final class Pair<L, R> {
	private final L left;
	private final R right;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public Pair(L left, R right) {
		this.left = left;
		this.right = right;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Object
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof Pair<?, ?> other)) {
			return false;
		}
		return Objects.equals(getLeft(), other.getLeft())
				&& Objects.equals(getRight(), other.getRight());
	}

	@Override
	public int hashCode() {
		int hLeft = getLeft() == null ? 0 : getLeft().hashCode();
		int hRight = getRight() == null ? 0 : getRight().hashCode();
		return hLeft + 37 * hRight;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public L getLeft() {
		return left;
	}

	public R getRight() {
		return right;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Factory
	//
	////////////////////////////////////////////////////////////////////////////
	public static <L, R> Pair<L, R> create(L left, R right) {
		return new Pair<>(left, right);
	}
}