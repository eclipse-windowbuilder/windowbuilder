/*******************************************************************************
 * Copyright (c) 2025 Patrick Ziegler and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Patrick Ziegler - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.swing.model.property.editor.border;

import org.eclipse.wb.internal.swing.model.SwingRealm;

import org.eclipse.core.databinding.observable.value.AbstractObservableValue;

import javax.swing.border.Border;

/**
 * Wrapper for a {@link Border}. Accessing the value of this object must be done
 * from the AWT event dispatcher thread.
 */
public final class BorderValue extends AbstractObservableValue<Border> {
	private final Border border;

	/**
	 * Constructs a new instance with the Swing realm and a {@code null} value.
	 */
	public BorderValue() {
		this(null);
	}

	/**
	 * Constructs a new instance with the Swing realm and the given value.
	 */
	public BorderValue(Border border) {
		super(SwingRealm.getRealm());
		this.border = border;
	}

	@Override
	public String toString() {
		return "BorderValue [%s]".formatted(getValueType());
	}

	/**
	 * May be called from any thread.
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public Class<?> getValueType() {
		Border value = doGetValue();
		if (value == null) {
			return null;
		}
		return value.getClass();
	}

	@Override
	public Border doGetValue() {
		// TODO Make protected
		return border;
	}

	/**
	 * May be called from any thread.
	 * 
	 * @return {@code true} if this instance contains a {@link Border}.
	 */
	public boolean isPresent() {
		return border != null;
	}
}
