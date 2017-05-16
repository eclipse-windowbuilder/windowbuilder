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
package org.eclipse.wb.internal.core.utils.xml;

import org.apache.commons.lang.StringEscapeUtils;

import java.io.PrintWriter;

/**
 * Node for attribute in XML.
 *
 * @author scheglov_ke
 * @coverage core.util.xml
 */
public final class DocumentAttribute extends AbstractDocumentObject {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Enclosing element
  //
  ////////////////////////////////////////////////////////////////////////////
  private DocumentElement m_enclosingElement;

  public void setEnclosingElement(DocumentElement element) {
    m_enclosingElement = element;
  }

  public DocumentElement getEnclosingElement() {
    return m_enclosingElement;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Name offset
  //
  ////////////////////////////////////////////////////////////////////////////
  private int m_nameOffset;

  public void setNameOffset(int offset) {
    m_nameOffset = offset;
  }

  public int getNameOffset() {
    return m_nameOffset;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Name length
  //
  ////////////////////////////////////////////////////////////////////////////
  private int m_nameLength;

  public void setNameLength(int length) {
    m_nameLength = length;
  }

  public int getNameLength() {
    return m_nameLength;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Value offset
  //
  ////////////////////////////////////////////////////////////////////////////
  private int m_valueOffset;

  public void setValueOffset(int offset) {
    m_valueOffset = offset;
  }

  public int getValueOffset() {
    return m_valueOffset;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Value length
  //
  ////////////////////////////////////////////////////////////////////////////
  private int m_valueLength;

  public void setValueLength(int length) {
    m_valueLength = length;
  }

  public int getValueLength() {
    return m_valueLength;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Name
  //
  ////////////////////////////////////////////////////////////////////////////
  private String m_name;

  public void setName(String name) {
    m_name = name;
  }

  public String getName() {
    return m_name;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Value
  //
  ////////////////////////////////////////////////////////////////////////////
  private String m_value;

  public void setValue(String value) {
    String oldValue = m_value;
    {
      if (getModel() != null && !"UTF-8".equals(getModel().getCharset())) {
        value = escapeXml(value);
      } else {
        value = escapeXml0(value);
      }
      m_value = value;
    }
    if (m_enclosingElement != null) {
      firePropertyChanged(m_enclosingElement, m_name, oldValue, m_value);
    }
  }

  public String getValue() {
    return m_value;
  }

  /**
   * Performs escaping for non-Latin characters.
   */
  private static String escapeXml(String s) {
    s = escapeXml0(s);
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c > 0x7F) {
        result.append("&#" + Integer.toString(c) + ";");
      } else {
        result.append(c);
      }
    }
    return result.toString();
  }

  /**
   * Performs escaping which is required for any XML.
   */
  private static String escapeXml0(String s) {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c < 32) {
        result.append("&#" + Integer.toString(c) + ";");
      } else if (c == '&') {
        result.append("&amp;");
      } else if (c == '\'') {
        result.append("&apos;");
      } else if (c == '"') {
        result.append("&quot;");
      } else if (c == '<') {
        result.append("&lt;");
      } else if (c == '>') {
        result.append("&gt;");
      } else {
        result.append(c);
      }
    }
    return result.toString();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Write
  //
  ////////////////////////////////////////////////////////////////////////////
  public void write(PrintWriter writer) {
    writer.print(m_name);
    writer.print("=");
    writer.print('"');
    writer.print(StringEscapeUtils.escapeXml(m_value));
    writer.print('"');
  }
}
