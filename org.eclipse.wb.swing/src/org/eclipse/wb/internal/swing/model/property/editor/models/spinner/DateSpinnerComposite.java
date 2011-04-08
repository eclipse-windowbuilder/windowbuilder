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
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.core.utils.ui.UiUtils;
import org.eclipse.wb.internal.swing.model.ModelMessages;

import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import org.apache.commons.lang.time.DateUtils;

import java.util.Calendar;
import java.util.Date;

import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerModel;

/**
 * Implementation of {@link AbstractSpinnerComposite} for {@link SpinnerDateModel}.
 * 
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
final class DateSpinnerComposite extends AbstractSpinnerComposite {
  private static final String[] CALENDAR_FIELDS = {
      "MILLISECOND",
      "SECOND",
      "MINUTE",
      "HOUR_OF_DAY",
      "HOUR",
      "AM_PM",
      "DAY_OF_WEEK_IN_MONTH",
      "DAY_OF_WEEK",
      "DAY_OF_YEAR",
      "DAY_OF_MONTH",
      "WEEK_OF_MONTH",
      "WEEK_OF_YEAR",
      "MONTH",
      "YEAR",
      "ERA",
      "ZONE_OFFSET",
      "DST_OFFSET"};
  private static final int DEFAULT_CALENDAR_FIELD = 8;
  private final Button m_minButton;
  private final Button m_maxButton;
  private final CDateTime m_valueField;
  private final CDateTime m_minField;
  private final CDateTime m_maxField;
  private final Combo m_stepCombo;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DateSpinnerComposite(Composite parent, SpinnerModelDialog modelDialog) {
    super(parent, modelDialog);
    GridLayoutFactory.create(this).columns(2);
    // value
    {
      createLabel(ModelMessages.DateSpinnerComposite_initialValue);
      m_valueField = createDateTime();
    }
    // minimum
    {
      m_minButton = createCheck(ModelMessages.DateSpinnerComposite_start);
      m_minField = createDateTime();
      trackCheckField(m_minButton, m_minField);
    }
    // maximum
    {
      m_maxButton = createCheck(ModelMessages.DateSpinnerComposite_end);
      m_maxField = createDateTime();
      trackCheckField(m_maxButton, m_maxField);
    }
    // step
    {
      createLabel(ModelMessages.DateSpinnerComposite_numberType);
      m_stepCombo = new Combo(this, SWT.READ_ONLY);
      GridDataFactory.create(m_stepCombo).grabH().fillH();
      // add items
      for (String field : CALENDAR_FIELDS) {
        m_stepCombo.add(field);
      }
      m_stepCombo.select(DEFAULT_CALENDAR_FIELD);
      UiUtils.setVisibleItemCount(m_stepCombo, m_stepCombo.getItemCount());
      // add listener
      m_stepCombo.addListener(SWT.Selection, m_validateListener);
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
   * Enables/disables given {@link CDateTime} on check {@link Button} enable/disable.
   */
  private void trackCheckField(final Button check, final CDateTime field) {
    check.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event event) {
        if (check.getSelection()) {
          field.setEnabled(true);
          if (check.getSelection() && field.getSelection() == null) {
            field.setSelection(m_valueField.getSelection());
          }
        } else {
          field.setEnabled(false);
        }
        m_modelDialog.validateAll();
      }
    });
  }

  /**
   * Checks/enables check {@link Button} and {@link CDateTime}.
   */
  private static void updateCheckField(Button check, CDateTime field, boolean checked) {
    check.setSelection(checked);
    field.setEnabled(checked);
  }

  /**
   * @return new {@link CDateTime}.
   */
  private CDateTime createDateTime() {
    CDateTime field =
        new CDateTime(this, CDT.BORDER
            | CDT.TEXT_RIGHT
            | CDT.SPINNER
            | CDT.DATE_MEDIUM
            | CDT.TIME_MEDIUM);
    GridDataFactory.create(field).grabH().fillH();
    field.addListener(SWT.Selection, m_validateListener);
    return field;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getTitle() {
    return ModelMessages.DateSpinnerComposite_title;
  }

  @Override
  public boolean setModel(SpinnerModel model) {
    if (model instanceof SpinnerDateModel) {
      SpinnerDateModel dateModel = (SpinnerDateModel) model;
      // values
      setValue(m_valueField, dateModel.getValue());
      setValue(m_minField, dateModel.getStart());
      setValue(m_maxField, dateModel.getEnd());
      // step
      for (int i = 0; i < CALENDAR_FIELDS.length; i++) {
        String field = CALENDAR_FIELDS[i];
        if (ReflectionUtils.getFieldInt(Calendar.class, field) == dateModel.getCalendarField()) {
          m_stepCombo.select(i);
          break;
        }
      }
      // enable/disable min/max fields
      updateCheckField(m_minButton, m_minField, dateModel.getStart() != null);
      updateCheckField(m_maxButton, m_maxField, dateModel.getEnd() != null);
      // OK, this is our model
      return true;
    } else {
      // values
      m_valueField.setSelection(DEFAULT_DATE);
      m_minField.setSelection(DEFAULT_DATE);
      m_maxField.setSelection(DEFAULT_DATE);
      // disable min/max fields
      updateCheckField(m_minButton, m_minField, false);
      updateCheckField(m_maxButton, m_maxField, false);
      // no, we don't know this model
      return false;
    }
  }

  @Override
  public String validate() {
    Date value = m_valueField.getSelection();
    Date minimum = m_minField.getSelection();
    Date maximum = m_maxField.getSelection();
    if (m_minButton.getSelection() && minimum.compareTo(value) > 0) {
      return ModelMessages.DateSpinnerComposite_errMinValue;
    }
    if (m_maxButton.getSelection() && maximum.compareTo(value) < 0) {
      return ModelMessages.DateSpinnerComposite_errMaxValue;
    }
    // OK
    return null;
  }

  @Override
  public SpinnerModel getModel() {
    Date value = m_valueField.getSelection();
    Date minimum = m_minButton.getSelection() ? m_minField.getSelection() : null;
    Date maximum = m_maxButton.getSelection() ? m_maxField.getSelection() : null;
    String stepField = CALENDAR_FIELDS[m_stepCombo.getSelectionIndex()];
    int step = ReflectionUtils.getFieldInt(Calendar.class, stepField);
    return new SpinnerDateModel(value, minimum, maximum, step);
  }

  @Override
  public String getSource() throws Exception {
    String valueSource = getValueSource(m_valueField);
    String minSource = m_minButton.getSelection() ? getValueSource(m_minField) : "null";
    String maxSource = m_maxButton.getSelection() ? getValueSource(m_maxField) : "null";
    //String stepSource = getValueSource(m_stepField);
    String stepField = CALENDAR_FIELDS[m_stepCombo.getSelectionIndex()];
    String stepSource = "java.util.Calendar." + stepField;
    return "new javax.swing.SpinnerDateModel("
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
  private static final Date DEFAULT_DATE = DateUtils.truncate(new Date(), Calendar.DAY_OF_MONTH);

  /**
   * Sets {@link Date} value for {@link CDateTime}.
   */
  private static void setValue(CDateTime field, Object value) {
    if (value instanceof Date) {
      field.setSelection((Date) value);
    } else {
      field.setSelection(null);
    }
  }

  /**
   * @return the source for value from {@link CSpinner}.
   */
  private String getValueSource(CDateTime field) {
    long milliseconds;
    {
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(field.getSelection());
      milliseconds = calendar.getTimeInMillis();
    }
    // OK, return source
    return "new java.util.Date(" + milliseconds + "L)";
  }
}
