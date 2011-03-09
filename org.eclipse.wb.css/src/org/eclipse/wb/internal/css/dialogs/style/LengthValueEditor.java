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
package org.eclipse.wb.internal.css.dialogs.style;

import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.css.semantics.AbstractValue;
import org.eclipse.wb.internal.css.semantics.IValueListener;
import org.eclipse.wb.internal.css.semantics.LengthValue;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import org.apache.commons.lang.ArrayUtils;

/**
 * Editor for {@link LengthValue}.
 * 
 * @author scheglov_ke
 * @coverage CSS.ui
 */
public final class LengthValueEditor extends AbstractValueEditor {
  private final LengthValue m_value;
  private final String[] m_values;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public LengthValueEditor(StyleEditOptions options,
      LengthValue value,
      String title,
      String[] values) {
    super(options, title, value);
    m_value = value;
    m_values = values;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  private Combo m_valueCombo;
  private Combo m_unitCombo;

  @Override
  public void doFillGrid(Composite parent, int numColumns) {
    doFillGrid(parent, numColumns, false);
  }

  public void doFillGrid(Composite parent, int numColumns, boolean grab) {
    requireColumns(4, numColumns);
    createTitleLabel(parent);
    // value combo
    {
      m_valueCombo = new Combo(parent, SWT.NONE);
      GridDataFactory.create(m_valueCombo).spanH(numColumns - 3).hintHC(10).fill();
      if (grab) {
        GridDataFactory.modify(m_valueCombo).grabH();
      }
      // add values
      if (m_values != null) {
        for (int i = 0; i < m_values.length; i++) {
          String specialValue = m_values[i];
          m_valueCombo.add(specialValue);
        }
      }
      // configure drop count
      m_valueCombo.setVisibleItemCount(Math.min(m_valueCombo.getItemCount(), 20));
      // add listeners
      {
        Listener listener = new Listener() {
          public void handleEvent(Event event) {
            // update value
            m_value.setValue(m_valueCombo.getText());
            // update unit
            {
              boolean requiresUnit = m_value.requiresUnit();
              m_unitCombo.setEnabled(requiresUnit);
              if (requiresUnit && !m_value.hasUnit()) {
                m_value.setUnit(LengthValue.UNIT_NAMES[m_unitCombo.getSelectionIndex()]);
              }
            }
          }
        };
        m_valueCombo.addListener(SWT.Modify, listener);
        m_valueCombo.addListener(SWT.Selection, listener);
      }
    }
    // unit combo
    {
      m_unitCombo = new Combo(parent, SWT.READ_ONLY);
      String[] unitTitles = LengthValue.UNIT_TITLES;
      for (int i = 0; i < unitTitles.length; i++) {
        String unitTitle = unitTitles[i];
        m_unitCombo.add(unitTitle);
      }
      m_unitCombo.setVisibleItemCount(m_unitCombo.getItemCount());
      m_unitCombo.select(0);
      // add listener
      m_unitCombo.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
          m_value.setUnit(LengthValue.UNIT_NAMES[m_unitCombo.getSelectionIndex()]);
        }
      });
    }
    // add listener for value
    m_value.addListener(new IValueListener() {
      public void changed(AbstractValue value) {
        updateControlsFromValue();
      }
    });
    // clear button
    createClearButton(parent);
  }

  private void updateControlsFromValue() {
    if (m_value.hasValue()) {
      String newValue = m_value.getValue();
      if (!newValue.equals(m_valueCombo.getText())) {
        m_valueCombo.setText(newValue);
      }
    } else {
      m_valueCombo.setText("");
    }
    //
    if (m_value.hasUnit()) {
      int index = ArrayUtils.indexOf(LengthValue.UNIT_NAMES, m_value.getUnit());
      if (m_unitCombo.getSelectionIndex() != index) {
        m_unitCombo.select(index);
      }
    }
    if (!m_value.requiresUnit()) {
      m_unitCombo.setEnabled(false);
    }
  }
}
