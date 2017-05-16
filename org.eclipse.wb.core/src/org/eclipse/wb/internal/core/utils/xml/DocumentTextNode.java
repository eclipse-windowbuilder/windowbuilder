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

import org.apache.commons.lang.StringUtils;

/**
 * Node for text inside of some XML element.
 *
 * @author scheglov_ke
 * @coverage core.util.xml
 */
public final class DocumentTextNode extends AbstractDocumentObject {
  public static final String P_TEXT = "P_TEXT";
  public static final String P_CDATA = "P_CDATA";
  private boolean m_isCDATA;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DocumentTextNode(boolean isCDATA) {
    m_isCDATA = isCDATA;
  }

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
  // Text
  //
  ////////////////////////////////////////////////////////////////////////////
  private String m_text;

  public void setText(String text) {
    if (!StringUtils.equals(m_text, text)) {
      String oldValue = m_text;
      m_text = text;
      firePropertyChanged(this, P_TEXT, oldValue, m_text);
    }
  }

  public String getText() {
    return m_text;
  }

  public String getRawText() {
    if (m_isCDATA) {
      return "<![CDATA[" + m_text + "]]>";
    }
    return m_text;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CDATA
  //
  ////////////////////////////////////////////////////////////////////////////
  public boolean isCDATA() {
    return m_isCDATA;
  }

  public void setCDATA(boolean isCDATA) {
    if (m_isCDATA != isCDATA) {
      boolean oldValue = m_isCDATA;
      m_isCDATA = isCDATA;
      firePropertyChanged(this, P_CDATA, oldValue, m_isCDATA);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Offset
  //
  ////////////////////////////////////////////////////////////////////////////
  private int m_offset;

  public void setOffset(int offset) {
    m_offset = offset;
  }

  public int getOffset() {
    return m_offset;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Length
  //
  ////////////////////////////////////////////////////////////////////////////
  private int m_length;

  public void setLength(int length) {
    m_length = length;
  }

  public int getLength() {
    return m_length;
  }
}
