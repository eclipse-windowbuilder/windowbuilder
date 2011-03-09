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
package org.eclipse.wb.internal.core.model.property.editor;

import org.eclipse.wb.core.controls.CCombo3;
import org.eclipse.wb.core.controls.CComboBox;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.table.PropertyTable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

/**
 * The {@link PropertyEditor} for selecting single value using {@link CComboBox}.
 * 
 * @author sablin_aa
 * @author scheglov_ke
 * @coverage core.model.property.editor
 */
public abstract class AbstractComboBoxPropertyEditor extends TextDisplayPropertyEditor {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Editing
  //
  ////////////////////////////////////////////////////////////////////////////
  private CComboBox m_combo;

  @Override
  public final boolean activate(final PropertyTable propertyTable,
      final Property property,
      Point location) throws Exception {
    m_combo = new CComboBox(propertyTable, SWT.NONE);
    // initialize
    fillItems(property, m_combo);
    selectCurrent(property, m_combo);
    // install listeners
    m_combo.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        switch (e.keyCode) {
          case SWT.DEL :
            try {
              property.setValue(Property.UNKNOWN_VALUE);
            } catch (Exception exception) {
              propertyTable.handleException(exception);
            }
          case SWT.ESC :
            e.doit = false;
            m_combo.getDisplay().asyncExec(new Runnable() {
              public void run() {
                propertyTable.deactivateEditor(false);
              }
            });
            break;
        }
      }
    });
    m_combo.addFocusListener(new FocusAdapter() {
      @Override
      public void focusLost(FocusEvent e) {
        propertyTable.deactivateEditor(true);
      }
    });
    m_combo.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        toProperty(propertyTable, property);
      }
    });
    m_combo.setFocus();
    // keep editor active
    return true;
  }

  @Override
  public final void deactivate(PropertyTable propertyTable, Property property, boolean save) {
    if (save) {
      try {
        toProperty(propertyTable, property);
      } catch (Throwable e) {
        propertyTable.deactivateEditor(false);
        propertyTable.handleException(e);
      }
    }
    if (m_combo != null) {
      m_combo.dispose();
      m_combo = null;
    }
  }

  @Override
  public void setBounds(Rectangle bounds) {
    m_combo.setBounds(bounds);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Abstract methods
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds items to given {@link CComboBox}.
   */
  protected abstract void fillItems(Property property, CComboBox combo) throws Exception;

  /**
   * Selects current item in given {@link CCombo3}.
   */
  protected abstract void selectCurrent(Property property, CComboBox combo) throws Exception;

  /**
   * Transfers data from widget to {@link Property}.
   */
  private void toProperty(PropertyTable propertyTable, Property property) {
    try {
      toPropertyEx(property, m_combo);
    } catch (Exception e) {
      propertyTable.handleException(e);
    }
    propertyTable.deactivateEditor(false);
  }

  protected abstract void toPropertyEx(Property property, CComboBox combo) throws Exception;
}
