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
package org.eclipse.wb.internal.swing.model.property.editor.models.spinner;

import org.eclipse.wb.internal.swing.model.SwingRealm;

import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.runtime.Assert;

import javax.swing.SpinnerModel;
import javax.swing.SwingUtilities;

/**
 * Wrapper for a {@link SpinnerModel}. Accessing the value of this object must
 * be done from the AWT event dispatcher thread.
 */
public class SpinnerModelValue extends WritableValue<SpinnerModel> {

	/**
	 * Constructs a new instance with the Swing realm and a {@code null} value.
	 */
	public SpinnerModelValue() {
		this(null);
	}

	/**
	 * Constructs a new instance with the Swing realm and the given value.
	 *
	 * @param model May be {@code null}.
	 */
	public SpinnerModelValue(SpinnerModel model) {
		super(SwingRealm.getRealm(), model, SpinnerModel.class);
		Assert.isTrue(SwingUtilities.isEventDispatchThread(), "Must be created from AWT event dispatcher thread");
	}
}
