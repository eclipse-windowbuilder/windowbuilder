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

import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.runtime.Assert;

import javax.swing.SwingUtilities;
import javax.swing.border.Border;

/**
 * Wrapper for a {@link Border}. Accessing the value of this object must be done
 * from the AWT event dispatcher thread.
 */
public abstract class BorderValue extends WritableValue<Border> {
	private String name;

	/**
	 * Constructs a new instance with the Swing realm and the given value.
	 *
	 * @param border Never {@code null}.
	 */
	public BorderValue(Border border) {
		super(SwingRealm.getRealm(), border, Border.class);
		Assert.isTrue(SwingUtilities.isEventDispatchThread(), "Must be created from AWT event dispatcher thread");
		Assert.isNotNull(border, "Border must not be null.");
		name = border.getClass().getName();
	}

	/**
	 * This method may be called from any thread.
	 *
	 * @return A human-readable name of the stored {@link Border}. Never
	 *         {@code null}.
	 */
	public String getBorderName() {
		return name;
	}
}
