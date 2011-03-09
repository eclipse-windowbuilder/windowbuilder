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
package org.eclipse.wb.internal.swing.FormLayout.model.ui;

import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.swing.FormLayout.model.FormSizeConstantInfo;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Spinner;

import com.jgoodies.forms.layout.ConstantSize;
import com.jgoodies.forms.layout.ConstantSize.Unit;

/**
 * {@link Composite} for editing {@link FormSizeConstantInfo}.
 * 
 * @author scheglov_ke
 * @coverage swing.FormLayout.ui
 */
public final class ConstantSizeComposite extends Composite {
  private final UnitDescription[] m_units;
  private final Spinner m_valueSpinner;
  private final Combo m_unitsCombo;
  private FormSizeConstantInfo m_currentSize;
  private double m_divider;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ConstantSizeComposite(Composite parent, int style, UnitDescription[] units) {
    super(parent, style);
    m_units = units;
    GridLayoutFactory.create(this).columns(2).noMargins();
    {
      m_valueSpinner = new Spinner(this, SWT.BORDER);
      GridDataFactory.create(m_valueSpinner).hintHC(10);
      m_valueSpinner.setMinimum(0);
      m_valueSpinner.setMaximum(Integer.MAX_VALUE);
      // add listener
      m_valueSpinner.addListener(SWT.Selection, new Listener() {
        public void handleEvent(Event event) {
          m_currentSize.setValue(m_valueSpinner.getSelection() / m_divider);
          notifySelection();
        }
      });
    }
    {
      m_unitsCombo = new Combo(this, SWT.READ_ONLY);
      GridDataFactory.create(m_unitsCombo).hintHC(15);
      // add units
      m_unitsCombo.setVisibleItemCount(m_units.length);
      for (int i = 0; i < m_units.length; i++) {
        UnitDescription description = m_units[i];
        m_unitsCombo.add(description.getTitle());
      }
      // add listener
      m_unitsCombo.addListener(SWT.Selection, new Listener() {
        public void handleEvent(Event event) {
          int unitIndex = m_unitsCombo.getSelectionIndex();
          Unit unit = m_units[unitIndex].getUnit();
          try {
            m_currentSize.setUnit(unit);
            configureSpinnerForUnit();
          } catch (Throwable e) {
          }
          notifySelection();
        }
      });
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the selected {@link FormSizeConstantInfo}.
   */
  public FormSizeConstantInfo getConstantSize() {
    return new FormSizeConstantInfo(m_currentSize.getValue(), m_currentSize.getUnit());
  }

  /**
   * Sets the {@link FormSizeConstantInfo} to display.
   */
  public void setConstantSize(FormSizeConstantInfo size) {
    if (size != null) {
      m_currentSize = new FormSizeConstantInfo(size.getValue(), size.getUnit());
      configureSpinnerForUnit();
      // show value
      {
        int value = (int) (size.getValue() * m_divider);
        if (m_valueSpinner.getSelection() != value) {
          m_valueSpinner.setSelection(value);
        }
      }
      // show unit
      for (int i = 0; i < m_units.length; i++) {
        UnitDescription description = m_units[i];
        if (size.getUnit() == description.getUnit()) {
          m_unitsCombo.select(i);
        }
      }
    } else {
      m_valueSpinner.setSelection(0);
      m_unitsCombo.select(0);
    }
  }

  /**
   * Configures value {@link Spinner} according current {@link Unit}.
   */
  private void configureSpinnerForUnit() {
    Unit unit = m_currentSize.getUnit();
    if (unit == ConstantSize.PIXEL
        || unit == ConstantSize.POINT
        || unit == ConstantSize.DIALOG_UNITS_X
        || unit == ConstantSize.DIALOG_UNITS_Y) {
      m_valueSpinner.setDigits(0);
      m_divider = 1.0;
    } else {
      m_valueSpinner.setDigits(1);
      m_divider = 10.0;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sends the {@link SWT#Selection} notification that value was changed.
   */
  private void notifySelection() {
    notifyListeners(SWT.Selection, null);
  }
}
