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
package org.eclipse.wb.internal.swing.MigLayout.model.ui;

import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.swing.MigLayout.model.MigDimensionInfo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import net.miginfocom.layout.UnitValue;

import org.apache.commons.lang.StringUtils;

/**
 * Field for editing {@link UnitValue} property of {@link MigDimensionInfo}.
 * 
 * @author scheglov_ke
 * @coverage swing.MigLayout.ui
 */
public final class DimensionUnitValueField {
  private final String m_propertyName;
  private final Listener m_listener;
  private MigDimensionInfo m_dimension;
  // UI
  private final Button m_checkButton;
  private final ErrorMessageTextField m_field;
  private final Text m_textWidget;
  // listener
  private boolean m_updatingDimension;
  private final Listener m_modifyListener = new Listener() {
    public void handleEvent(Event e) {
      toDimension(m_textWidget.getText());
    }
  };

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DimensionUnitValueField(Composite parent,
      String labelText,
      String propertyName,
      Listener listener) {
    m_propertyName = StringUtils.capitalize(propertyName);
    m_listener = listener;
    {
      m_checkButton = new Button(parent, SWT.CHECK);
      m_checkButton.setText(labelText);
      m_checkButton.addListener(SWT.Selection, new Listener() {
        public void handleEvent(Event event) {
          if (m_checkButton.getSelection()) {
            m_textWidget.setEnabled(true);
            setText("100px");
            toDimension(m_textWidget.getText());
          } else {
            m_textWidget.setEnabled(false);
            setText("");
            toDimension(null);
          }
        }
      });
    }
    // prepare field/widget
    m_field = new ErrorMessageTextField(parent, SWT.BORDER);
    GridDataFactory.create(m_field.getLayoutControl()).grabH().fillH();
    m_textWidget = (Text) m_field.getControl();
    // listen for modification
    m_textWidget.addListener(SWT.Modify, m_modifyListener);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Updates this field from {@link MigDimensionInfo}.
   */
  public void fromDimension(MigDimensionInfo dimension) {
    if (!m_updatingDimension) {
      m_dimension = dimension;
      try {
        UnitValue value =
            (UnitValue) ReflectionUtils.invokeMethod2(m_dimension, "get" + m_propertyName);
        if (value == null) {
          m_checkButton.setSelection(false);
          m_textWidget.setEnabled(false);
          setText("");
        } else {
          m_checkButton.setSelection(true);
          m_textWidget.setEnabled(true);
          // update text
          String text = m_dimension.getString(value);
          if (!m_textWidget.getText().equals(text)) {
            setText(text);
          }
        }
      } catch (Throwable e) {
      }
    }
  }

  /**
   * Uses text from {@link #m_textWidget} to update {@link MigDimensionInfo}.
   */
  private void toDimension(String s) {
    m_updatingDimension = true;
    try {
      ReflectionUtils.invokeMethod2(m_dimension, "set" + m_propertyName, String.class, s);
      notifyModified(true);
      m_field.setErrorMessage(null);
    } catch (Throwable e) {
      notifyModified(false);
      m_field.setErrorMessage(e.getMessage());
    } finally {
      m_updatingDimension = false;
    }
  }

  /**
   * Notifies {@link #m_listener} that this field was updated, with given valid state.
   */
  private void notifyModified(boolean valid) {
    Event event = new Event();
    event.doit = valid;
    m_listener.handleEvent(event);
  }

  /**
   * Sets text to {@link #m_textWidget}.
   */
  private void setText(String text) {
    if (!m_textWidget.getText().equals(text)) {
      m_textWidget.removeListener(SWT.Modify, m_modifyListener);
      try {
        m_textWidget.setText(text);
        m_field.setErrorMessage(null);
      } finally {
        m_textWidget.addListener(SWT.Modify, m_modifyListener);
      }
    }
  }
}
