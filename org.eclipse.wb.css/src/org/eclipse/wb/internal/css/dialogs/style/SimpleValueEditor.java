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
import org.eclipse.wb.internal.css.semantics.SimpleValue;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * Editor for {@link SimpleValue}.
 * 
 * @author scheglov_ke
 * @coverage CSS.ui
 */
public final class SimpleValueEditor extends AbstractValueEditor {
  private final SimpleValue m_value;
  private final String[] m_values;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SimpleValueEditor(StyleEditOptions options,
      SimpleValue value,
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
  private Combo m_combo;

  @Override
  public void doFillGrid(Composite parent, int numColumns) {
    doFillGrid(parent, numColumns, false, false);
  }

  public void doFillGrid(Composite parent, int numColumns, boolean grab, boolean editable) {
    requireColumns(3, numColumns);
    createTitleLabel(parent);
    // combo
    {
      int style = editable ? SWT.NONE : SWT.READ_ONLY;
      m_combo = new Combo(parent, style);
      GridDataFactory.create(m_combo).spanH(numColumns - 2).hintHC(10).fill();
      if (grab) {
        GridDataFactory.modify(m_combo).grabH();
      }
      // add values
      if (m_values != null) {
        for (int i = 0; i < m_values.length; i++) {
          String specialValue = m_values[i];
          m_combo.add(specialValue);
        }
      }
      // configure drop count
      m_combo.setVisibleItemCount(Math.min(m_combo.getItemCount(), 20));
      // add listeners
      {
        Listener listener = new Listener() {
          public void handleEvent(Event event) {
            m_value.setValue(m_combo.getText());
          }
        };
        m_combo.addListener(SWT.Modify, listener);
        m_combo.addListener(SWT.Selection, listener);
      }
    }
    // add value listener
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
      if (!m_combo.getText().equals(newValue)) {
        m_combo.setText(newValue);
      }
    } else {
      m_combo.setText("");
    }
  }
}
