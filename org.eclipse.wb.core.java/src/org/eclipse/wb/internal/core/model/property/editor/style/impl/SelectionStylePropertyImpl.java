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

/**
 * This class represent "select" property implementation.
 *
 * @author lobas_av
 * @coverage core.model.property.editor
 */
public final class SelectionStylePropertyImpl extends SubStylePropertyImpl {
  private final long[] m_flags;
  private final int m_defaultIndex;
  private final String[] m_sFlags;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SelectionStylePropertyImpl(AbstractStylePropertyEditor editor,
      String title,
      long[] flags,
      String[] sFlags,
      long defaultFlag) {
    super(editor, title);
    m_flags = flags;
    m_sFlags = sFlags;
    m_defaultIndex = ArrayUtils.indexOf(m_flags, defaultFlag);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // As string
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void getAsString(StringBuilder builder) {
    builder.append(getTitle());
    builder.append(" select: ");
    builder.append(m_defaultIndex);
    for (String sFlag : m_sFlags) {
      builder.append(" ");
      builder.append(sFlag);
    }
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
      if (i == m_defaultIndex) {
        continue;
      }
      if ((style & m_flags[i]) != 0) {
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
    long style = getStyleValue(property);
    for (int i = 0; i < m_flags.length; i++) {
      if ((style & m_flags[i]) != 0) {
        return m_sFlags[i];
      }
    }
    return m_sFlags[m_defaultIndex];
  }

  private long getCurrentFlag(Property property) throws Exception {
    long style = getStyleValue(property);
    for (int i = 0; i < m_flags.length; i++) {
      if ((style & m_flags[i]) != 0) {
        return m_flags[i];
      }
    }
    return m_flags[m_defaultIndex];
  }

  @Override
  public void setValue(Property property, Object value) throws Exception {
    long style = getStyleValue(property) ^ getCurrentFlag(property);
    if (value != Property.UNKNOWN_VALUE) {
      String sFlag = (String) value;
      int index = ArrayUtils.indexOf(m_sFlags, sFlag);
      if (index != m_defaultIndex) {
        style |= m_flags[index];
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
    // default
    IAction defaultAction = null;
    boolean defineChecked = false;
    // add actions
    long style = getStyleValue(property);
    for (int i = 0; i < m_flags.length; i++) {
      // create
      IAction action = new RadioStyleAction(property, this, m_sFlags[i]);
      // configure
      if ((style & m_flags[i]) != 0) {
        action.setChecked(true);
        defineChecked = true;
      }
      // default
      if (m_defaultIndex == i) {
        defaultAction = action;
      }
      // add to menu
      manager.add(action);
    }
    // default
    if (!defineChecked) {
      defaultAction.setChecked(true);
    }
  }
}