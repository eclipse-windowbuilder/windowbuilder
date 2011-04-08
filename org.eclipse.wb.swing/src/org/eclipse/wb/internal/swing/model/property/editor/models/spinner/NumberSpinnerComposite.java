/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.swing.model.property.editor.models.spinner;

import org.eclipse.wb.core.controls.CSpinner;
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
  private final CSpinner m_valueField;
  private final CSpinner m_minField;
  private final CSpinner m_maxField;
  private final CSpinner m_stepField;

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
   * Enables/disables given {@link CSpinner} on check {@link Button} enable/disable.
   */
  private static void trackCheckSpinner(final Button check, final CSpinner spinner) {
    check.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event event) {
        spinner.setEnabled(check.getSelection());
      }
    });
  }

  /**
   * Checks/enables check {@link Button} and {@link CSpinner}.
   */
  private static void updateCheckSpinner(Button check, CSpinner spinner, boolean checked) {
    check.setSelection(checked);
    spinner.setEnabled(checked);
  }

  /**
   * @return new {@link CSpinner}.
   */
  private CSpinner createSpinner() {
    CSpinner spinner = new CSpinner(this, SWT.BORDER);
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
    if (model instanceof SpinnerNumberModel) {
      SpinnerNumberModel numberModel = (SpinnerNumberModel) model;
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
    }
    return false;
  }

  @Override
  public String validate() {
    if (m_minButton.getSelection() && m_minField.getSelection() > m_valueField.getSelection()) {
      return ModelMessages.NumberSpinnerComposite_minValue;
    }
    if (m_maxButton.getSelection() && m_maxField.getSelection() < m_valueField.getSelection()) {
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
   * Sets integer value for {@link CSpinner}.
   */
  private static void setValue(CSpinner spinner, Object value) {
    if (value instanceof Number) {
      spinner.setSelection(((Number) value).intValue());
    }
  }

  /**
   * @return the source for value from {@link CSpinner}.
   */
  private String getValueSource(CSpinner spinner) {
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
        return "new Byte((byte) " + value + ")";
      }
    },
    SHORT(Short.class) {
      @Override
      public String getSource(int value) {
        return "new Short((short) " + value + ")";
      }
    },
    INTEGER(Integer.class) {
      @Override
      public String getSourceOptimized(int value) {
        return Integer.toString(value);
      }
    },
    FLOAT(Float.class), LONG(Long.class), DOUBLE(Double.class) {
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
      return "new " + m_type.getName() + "(" + value + ")";
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
