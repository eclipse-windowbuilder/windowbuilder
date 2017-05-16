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
import org.eclipse.wb.internal.core.model.property.editor.BooleanPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.style.AbstractStylePropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.style.SubStylePropertyImpl;
import org.eclipse.wb.internal.core.model.property.editor.style.actions.BooleanStyleAction;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;

/**
 * This class represent "set" property implementation.
 *
 * @author lobas_av
 * @coverage core.model.property.editor
 */
public final class BooleanStylePropertyImpl extends SubStylePropertyImpl {
  private final String m_sFlag;
  private final long m_flag;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public BooleanStylePropertyImpl(AbstractStylePropertyEditor editor,
      String title,
      String sFlag,
      long flag) {
    super(editor, title);
    m_sFlag = sFlag;
    m_flag = flag;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // As string
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void getAsString(StringBuilder builder) {
    builder.append(getTitle());
    builder.append(" boolean: ");
    builder.append(m_sFlag);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // PropertyEditor
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public PropertyEditor createEditor() {
    return BooleanPropertyEditor.INSTANCE;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Style
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public long getFlag(String sFlag) {
    return m_flag;
  }

  @Override
  public String getFlagValue(Property property) throws Exception {
    return isSet(property) ? m_sFlag : null;
  }

  private boolean isSet(Property property) throws Exception {
    return (getStyleValue(property) & m_flag) != 0;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Value
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Object getValue(Property property) throws Exception {
    return isSet(property) ? Boolean.TRUE : Boolean.FALSE;
  }

  @Override
  public void setValue(Property property, Object value) throws Exception {
    boolean setValue = value != Property.UNKNOWN_VALUE && (Boolean) value;
    long style = getStyleValue(property);
    if (setValue) {
      style |= m_flag;
    } else {
      style ^= m_flag;
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
    // create
    IAction action = new BooleanStyleAction(property, this);
    // configure
    action.setChecked(isSet(property));
    // add to menu
    manager.add(action);
  }
}