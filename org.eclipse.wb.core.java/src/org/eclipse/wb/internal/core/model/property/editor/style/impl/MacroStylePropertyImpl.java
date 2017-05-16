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
import org.eclipse.jface.action.Separator;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

/**
 * This class represent "macro" property implementation.
 *
 * @author lobas_av
 * @coverage core.model.property.editor
 */
public final class MacroStylePropertyImpl extends SubStylePropertyImpl {
  private final long[] m_flags;
  private final String[] m_sFlags;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public MacroStylePropertyImpl(AbstractStylePropertyEditor editor,
      String title,
      long[] flags,
      String[] sFlags) {
    super(editor, title);
    m_flags = flags;
    m_sFlags = sFlags;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // PropertyEditor
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public PropertyEditor createEditor() {
    return new StringComboPropertyEditor(m_sFlags);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Style
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public long getFlag(String sFlag) {
    return m_flags[ArrayUtils.indexOf(m_sFlags, sFlag)];
  }

  @Override
  public String getFlagValue(Property property) throws Exception {
    long style = getStyleValue(property);
    for (int i = 0; i < m_flags.length; i++) {
      long flag = m_flags[i];
      if ((style & flag) == flag) {
        return m_sFlags[i];
      }
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Value
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Object getValue(Property property) throws Exception {
    return StringUtils.defaultString(getFlagValue(property));
  }

  @Override
  public void setValue(Property property, Object value) throws Exception {
    long style = 0;
    if (value != Property.UNKNOWN_VALUE) {
      String sFlag = (String) value;
      if (!StringUtils.isEmpty(sFlag)) {
        style = getFlag(sFlag);
      }
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
    manager.add(new Separator());
    // add actions
    long style = getStyleValue(property);
    for (int i = 0; i < m_flags.length; i++) {
      // create
      IAction action = new RadioStyleAction(property, this, m_sFlags[i]);
      // configure
      long flag = m_flags[i];
      if ((style & flag) == flag) {
        action.setChecked(true);
      }
      // add to menu
      manager.add(action);
    }
  }
}