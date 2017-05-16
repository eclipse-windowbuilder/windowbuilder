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
package org.eclipse.wb.internal.core.utils.xml.parser;

/**
 * Description for single attribute.
 *
 * @author lobas_av
 * @coverage core.util.xml
 */
public final class QAttribute {
  private String m_name;
  private int m_nameOffset;
  private int m_nameLength;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Name
  //
  ////////////////////////////////////////////////////////////////////////////
  public String getName() {
    return m_name;
  }

  public int getNameOffset() {
    return m_nameOffset;
  }

  public int getNameLength() {
    return m_nameLength;
  }

  void setName(String name) {
    m_name = name;
  }

  void setNameOffset(int nameOffset) {
    m_nameOffset = nameOffset;
  }

  void setNameEndOffset(int endOffset) {
    m_nameLength = endOffset - m_nameOffset;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Value
  //
  ////////////////////////////////////////////////////////////////////////////
  private String m_value;
  private int m_valueOffset;
  private int m_valueLength;

  //
  public String getValue() {
    return m_value;
  }

  public int getValueOffset() {
    return m_valueOffset;
  }

  public int getValueLength() {
    return m_valueLength;
  }

  void setValue(String value) {
    m_value = value;
  }

  void setValueOffset(int valueOffset) {
    m_valueOffset = valueOffset;
  }

  void setValueEndOffset(int endOffset) {
    m_valueLength = endOffset - m_valueOffset;
  }
}