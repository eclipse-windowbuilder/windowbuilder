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

import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.core.utils.ui.UiUtils;
import org.eclipse.wb.internal.swing.model.ModelMessages;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Spinner;

import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

/**
 * Implementation of {@link AbstractSpinnerComposite} for {@link SpinnerNumberModel}.
 *
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
final class NumberSpinnerComposite extends AbstractSpinnerComposite {
	private final Combo m_typeCombo;
	private final Button m_minButton;
	private final Button m_maxButton;
	private final Spinner m_valueField;
	private final Spinner m_minField;
	private final Spinner m_maxField;
	private final Spinner m_stepField;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public NumberSpinnerComposite(Composite parent, SpinnerModelDialog modelDialog) {
		super(parent, modelDialog);
		GridLayoutFactory.create(this).columns(2);
		// type
		{
			createLabel(ModelMessages.NumberSpinnerComposite_numberType);
			m_typeCombo = new Combo(this, SWT.READ_ONLY);
			GridDataFactory.create(m_typeCombo).grabH().fillH();
			for (NumberTypeDescription typeDescription : NumberTypeDescription.values()) {
				m_typeCombo.add(typeDescription.getTitle());
			}
			m_typeCombo.select(0);
			UiUtils.setVisibleItemCount(m_typeCombo, m_typeCombo.getItemCount());
			m_typeCombo.addListener(SWT.Selection, m_validateListener);
		}
		// value
		{
			createLabel(ModelMessages.NumberSpinnerComposite_initialValue);
			m_valueField = createSpinner();
		}
		// minimum
		{
			m_minButton = createCheck(ModelMessages.NumberSpinnerComposite_minimum);
			m_minField = createSpinner();
			trackCheckSpinner(m_minButton, m_minField);
		}
		// maximum
		{
			m_maxButton = createCheck(ModelMessages.NumberSpinnerComposite_maximum);
			m_maxField = createSpinner();
			trackCheckSpinner(m_maxButton, m_maxField);
		}
		// step
		{
			createLabel(ModelMessages.NumberSpinnerComposite_stepSize);
			m_stepField = createSpinner();
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// GUI utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * {@link Listener} that performs validation of host {@link SpinnerModelDialog}.
	 */
	private final Listener m_validateListener = new Listener() {
		@Override
		public void handleEvent(Event event) {
			m_modelDialog.validateAll();
		}
	};

	/**
	 * Creates {@link Label}, configured for column <code>1</code>.
	 */
	private void createLabel(String text) {
		Label label = new Label(this, SWT.NONE);
		SpinnerModelDialog.configureColumn_1(label);
		label.setText(text);
	}

	/**
	 * Creates check {@link Button}, configured for column <code>1</code>.
	 */
	private Button createCheck(String text) {
		Button button = new Button(this, SWT.CHECK);
		SpinnerModelDialog.configureColumn_1(button);
		button.setText(text);
		return button;
	}

	/**
	 * Enables/disables given {@link Spinner} on check {@link Button}
	 * enable/disable.
	 */
	private void trackCheckSpinner(final Button check, final Spinner spinner) {
		check.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				spinner.setEnabled(check.getSelection());
			}
		});
		check.addListener(SWT.Selection, m_validateListener);
	}

	/**
	 * Checks/enables check {@link Button} and {@link Spinner}.
	 */
	private static void updateCheckSpinner(Button check, Spinner spinner, boolean checked) {
		check.setSelection(checked);
		spinner.setEnabled(checked);
	}

	/**
	 * @return new {@link Spinner}.
	 */
	private Spinner createSpinner() {
		Spinner spinner = new Spinner(this, SWT.BORDER);
		GridDataFactory.create(spinner).grabH().fillH();
		// configure range
		spinner.setMinimum(Integer.MIN_VALUE);
		spinner.setMaximum(Integer.MAX_VALUE);
		// add listener
		spinner.addListener(SWT.Selection, m_validateListener);
		return spinner;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getTitle() {
		return ModelMessages.NumberSpinnerComposite_title;
	}

	@Override
	public boolean setModel(SpinnerModel model) {
		if (model instanceof SpinnerNumberModel numberModel) {
			// type
			NumberTypeDescription[] values = NumberTypeDescription.values();
			for (int i = 0; i < values.length; i++) {
				NumberTypeDescription typeDescription = values[i];
				if (typeDescription.getType() == numberModel.getValue().getClass()) {
					m_typeCombo.select(i);
				}
			}
			// values
			setValue(m_valueField, numberModel.getValue());
			setValue(m_minField, numberModel.getMinimum());
			setValue(m_maxField, numberModel.getMaximum());
			setValue(m_stepField, numberModel.getStepSize());
			// enable/disable min/max fields
			updateCheckSpinner(m_minButton, m_minField, numberModel.getMinimum() != null);
			updateCheckSpinner(m_maxButton, m_maxField, numberModel.getMaximum() != null);
			// OK, this is our model
			return true;
		} else {
			// disable min/max fields
			updateCheckSpinner(m_minButton, m_minField, false);
			updateCheckSpinner(m_maxButton, m_maxField, false);
			return false;
		}
	}

	@Override
	public String validate() {
		m_minField.setBackground(COLOR_VALID);
		m_maxField.setBackground(COLOR_VALID);
		m_typeCombo.setBackground(COLOR_VALID);
		if (m_typeCombo.getSelectionIndex() == -1) {
			m_typeCombo.setBackground(COLOR_INVALID);
			return ModelMessages.DateSpinnerComposite_numberValue;
		}
		if (m_minButton.getSelection() && m_minField.getSelection() > m_valueField.getSelection()) {
			m_minField.setBackground(COLOR_INVALID);
			return ModelMessages.NumberSpinnerComposite_minValue;
		}
		if (m_maxButton.getSelection() && m_maxField.getSelection() < m_valueField.getSelection()) {
			m_maxField.setBackground(COLOR_INVALID);
			return ModelMessages.NumberSpinnerComposite_maxValue;
		}
		return null;
	}

	@Override
	public SpinnerModel getModel() {
		int value = m_valueField.getSelection();
		Integer minimum = m_minButton.getSelection() ? m_minField.getSelection() : null;
		Integer maximum = m_maxButton.getSelection() ? m_maxField.getSelection() : null;
		Number step = m_stepField.getSelection();
		return new SpinnerNumberModel(value, minimum, maximum, step);
	}

	@Override
	public String getSource() throws Exception {
		String valueSource = getValueSource(m_valueField);
		String minSource = m_minButton.getSelection() ? getValueSource(m_minField) : "null";
		String maxSource = m_maxButton.getSelection() ? getValueSource(m_maxField) : "null";
		String stepSource = getValueSource(m_stepField);
		return "new javax.swing.SpinnerNumberModel("
		+ valueSource
		+ ", "
		+ minSource
		+ ", "
		+ maxSource
		+ ", "
		+ stepSource
		+ ")";
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IntegerField utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Sets integer value for {@link Spinner}.
	 */
	private static void setValue(Spinner spinner, Object value) {
		if (value instanceof Number) {
			spinner.setSelection(((Number) value).intValue());
		}
	}

	/**
	 * @return the source for value from {@link Spinner}.
	 */
	private String getValueSource(Spinner spinner) {
		NumberTypeDescription typeDescription =
				NumberTypeDescription.values()[m_typeCombo.getSelectionIndex()];
		int value = spinner.getSelection();
		if (m_maxButton.getSelection() && m_maxButton.getSelection()) {
			return typeDescription.getSourceOptimized(value);
		} else {
			return typeDescription.getSource(value);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// NumberTypeDescription
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Description for {@link Number} in {@link SpinnerNumberModel}.
	 */
	private static enum NumberTypeDescription {
		BYTE(Byte.class) {
			@Override
			public String getSource(int value) {
				return "Byte.valueOf((byte) " + value + ")";
			}
		},
		SHORT(Short.class) {
			@Override
			public String getSource(int value) {
				return "Short.valueOf((short) " + value + ")";
			}
		},
		INTEGER(Integer.class) {
			@Override
			public String getSourceOptimized(int value) {
				return Integer.toString(value);
			}
		},
		FLOAT(Float.class),
		LONG(Long.class),
		DOUBLE(Double.class) {
			@Override
			public String getSourceOptimized(int value) {
				return Double.toString(value);
			}
		};

		////////////////////////////////////////////////////////////////////////////
		//
		// Instance fields
		//
		////////////////////////////////////////////////////////////////////////////
		private final Class<?> m_type;

		////////////////////////////////////////////////////////////////////////////
		//
		// Constructor
		//
		////////////////////////////////////////////////////////////////////////////
		private NumberTypeDescription(Class<?> type) {
			m_type = type;
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Access
		//
		////////////////////////////////////////////////////////////////////////////
		/**
		 * @return the type of this {@link NumberTypeDescription}.
		 */
		public Class<?> getType() {
			return m_type;
		}

		/**
		 * @return the title of this {@link NumberTypeDescription}.
		 */
		public final String getTitle() {
			return CodeUtils.getShortClass(m_type.getName());
		}

		/**
		 * @return the <code>new Type(value)</code> source.
		 */
		public String getSource(int value) {
			return m_type.getName() + ".valueOf(" + value + ")";
		}

		/**
		 * @return usually same as {@link #getSource(int)}, but for <code>int</code> and
		 *         <code>double</code> returns optimized value.
		 */
		public String getSourceOptimized(int value) {
			return getSource(value);
		}
	}
}
