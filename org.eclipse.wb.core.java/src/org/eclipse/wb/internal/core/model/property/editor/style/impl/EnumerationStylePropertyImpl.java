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
package org.eclipse.wb.internal.core.model.property.editor.style.impl;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.StringComboPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.style.AbstractStylePropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.style.SubStylePropertyImpl;
import org.eclipse.wb.internal.core.model.property.editor.style.actions.RadioStyleAction;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;

import org.apache.commons.lang.ArrayUtils;

/**
 * This class represent "enum" property implementation.
 *
 * @author lobas_av
 * @coverage core.model.property.editor
 */
public final class EnumerationStylePropertyImpl extends SubStylePropertyImpl {
  private final long[] m_values;
  private final String[] m_sValues;
  private final long m_flagsClearMask;
  private final long m_enumClearMask;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public EnumerationStylePropertyImpl(AbstractStylePropertyEditor editor,
      String title,
      long[] values,
      String[] sValues,
      int clearMask) {
    super(editor, title);
    m_values = values;
    m_sValues = sValues;
    m_flagsClearMask = clearMask;
    m_enumClearMask = ~clearMask;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // PropertyEditor
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public PropertyEditor createEditor() {
    return new StringComboPropertyEditor(m_sValues);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Style
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public long getFlag(String sFlag) {
    int index = ArrayUtils.indexOf(m_sValues, sFlag);
    return index != ArrayUtils.INDEX_NOT_FOUND ? m_values[index] : 0;
  }

  @Override
  public String getFlagValue(Property property) throws Exception {
    long value = getStyleValue(property) & m_flagsClearMask;
    int index = ArrayUtils.indexOf(m_values, value);
    return index != ArrayUtils.INDEX_NOT_FOUND ? m_sValues[index] : null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Value
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Object getValue(Property property) throws Exception {
    return getFlagValue(property);
  }

  @Override
  public void setValue(Property property, Object value) throws Exception {
    long style = getStyleValue(property) & m_enumClearMask;
    if (value != Property.UNKNOWN_VALUE) {
      String sValue = (String) value;
      style |= m_values[ArrayUtils.indexOf(m_sValues, sValue)];
    }
    setStyleValue(property, style);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Popup menu
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void contributeActions(Property property, IMenuManager manager) throws Exception {
    // separate sub-properties
    manager.add(new org.eclipse.jface.action.Separator());
    // add actions
    long style = getStyleValue(property) & m_flagsClearMask;
    for (int i = 0; i < m_values.length; i++) {
      // create
      IAction action = new RadioStyleAction(property, this, m_sValues[i]);
      // configure
      if (m_values[i] == style) {
        action.setChecked(true);
      }
      // add to menu
      manager.add(action);
    }
  }
}