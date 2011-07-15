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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Locale;

/**
 * Length value, it is used for any length/size properties, for example for
 * margin/padding/border-width.
 * 
 * @author scheglov_ke
 * @coverage CSS.semantics
 */
public final class LengthValue extends AbstractValue {
  private static final NumberFormat VALUE_DECIMAL_FORMAT = NumberFormat.getInstance(Locale.ENGLISH);
  public static final String UNIT_NAMES[] = new String[]{
      "px",
      "pt",
      "in",
      "cm",
      "mm",
      "pc",
      "em",
      "ex",
      "%"};
  public static final String UNIT_TITLES[] = new String[]{
      "pixels",
      "points",
      "in",
      "cm",
      "mm",
      "picas",
      "ems",
      "exs",
      "%"};
  private String m_value;
  private String m_unit;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public LengthValue(AbstractSemanticsComposite composite) {
    super(composite);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    return "{" + ToStringBuilder.reflectionToString(this, ToStringStyle.SIMPLE_STYLE) + "}";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Assignment
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets value of this {@link LengthValue} same as given {@link LengthValue}.
   */
  public void set(LengthValue value) {
    m_value = value.m_value;
    m_unit = value.m_value;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Assigns value as string, should be separated if possible on value and unit, or use full string
   * as value without unit.
   */
  @Override
  public void set(String s) {
    // try to find unit
    if (s != null) {
      for (int I = 0; I < UNIT_NAMES.length; I++) {
        String unit = UNIT_NAMES[I];
        if (s.endsWith(unit)) {
          // check that value is correct number
          String value = StringUtils.substring(s, 0, -unit.length());
          try {
            VALUE_DECIMAL_FORMAT.parse(value);
          } catch (Throwable e) {
            continue;
          }
          // OK, we have correct value and unit
          m_value = value;
          m_unit = unit;
          notifyListeners();
          return;
        }
      }
    }
    // no, we can not separate given string on value and unit, so use string itself as value
    m_value = s;
    m_unit = "px";
    notifyListeners();
  }

  @Override
  public String get() {
    String value = m_value;
    if (value != null) {
      value = value.trim();
      if (value.length() == 0) {
        value = null;
      }
    }
    //
    if (value == null) {
      return null;
    }
    if (requiresUnit()) {
      return value + m_unit;
    }
    return value;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Value
  //
  ////////////////////////////////////////////////////////////////////////////
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

  ////////////////////////////////////////////////////////////////////////////
  //
  // Unit
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Checks if current value requires unit. We need unit only if value is number.
   */
  public boolean requiresUnit() {
    if (hasValue()) {
      ParsePosition parsePosition = new ParsePosition(0);
      VALUE_DECIMAL_FORMAT.parse(m_value, parsePosition);
      return parsePosition.getIndex() == m_value.length();
    }
    return false;
  }

  public void setUnit(String unit) {
    if (m_unit == unit || m_unit != null && m_unit.equals(unit)) {
      return;
    }
    m_unit = unit;
    notifyListeners();
  }

  public String getUnit() {
    return m_unit;
  }

  public boolean hasUnit() {
    return m_unit != null;
  }
}
