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
package org.eclipse.wb.internal.swing.model.property.editor.models.spinner;

import org.eclipse.wb.internal.swing.utils.SwingUtils;

import org.eclipse.swt.widgets.Composite;

import java.util.function.Supplier;

import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;

import swingintegration.example.EmbeddedSwingComposite;

/**
 * Preview of the currently selected SpinnerModel in the SpinnerModel dialog..
 */
public final class SpinnerModelCanvas extends EmbeddedSwingComposite {
	private JSpinner m_spinner;

	public SpinnerModelCanvas(Composite parent, int style) {
		super(parent, style);
	}

	@Override
	protected JComponent createSwingComponent() {
		m_spinner = new JSpinner();
		return m_spinner;
	}

	/* package */ void disable() {
		SwingUtils.runLogLater(() -> {
			if (m_spinner != null) {
				m_spinner.setEnabled(false);
			}
		});
	}

	/* package */ void setSpinnerModel(Supplier<SpinnerModelValue> supplier) {
		SwingUtils.runLogLater(() -> {
			if (m_spinner != null) {
				SpinnerModelValue modelValue = supplier.get();
				SpinnerModel model = modelValue.getValue();
				m_spinner.setModel(model);
				m_spinner.setEnabled(true);
			}
		});
	}
}
