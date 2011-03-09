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
package org.eclipse.wb.internal.css.semantics;

/**
 * String string value, it is used for most properties.
 * 
 * @author scheglov_ke
 * @coverage CSS.semantics
 */
public final class SimpleValue extends AbstractValue {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SimpleValue(AbstractSemanticsComposite composite) {
    super(composite);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    return "{" + m_value + "}";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void set(String value) {
    setValue(value);
  }

  @Override
  public String get() {
    String value = getValue();
    if (value != null) {
      value = value.trim();
      if (value.length() == 0) {
        return null;
      }
    }
    return value;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Value
  //
  ////////////////////////////////////////////////////////////////////////////
  private String m_value;

  public void setValue(String value) {
    if (m_value == value || m_value != null && m_value.equals(value)) {
      return;
    }
    m_value = value;
    notifyListeners();
  }

  public String getValue() {
    return m_value;
  }

  public boolean hasValue() {
    return m_value != null && m_value.trim().length() != 0;
  }
}
