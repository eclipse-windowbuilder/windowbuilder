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
package org.eclipse.wb.internal.css.parser.scanner;

/**
 * The "Token" - container for token type and location (offset/length).
 * 
 * @author scheglov_ke
 */
public final class Token {
  private final int m_type;
  private final String m_value;
  private final int m_offset;
  private final int m_length;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public Token(int type, String value, int offset, int length) {
    m_type = type;
    m_value = value;
    m_offset = offset;
    m_length = length;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access 
  //
  ////////////////////////////////////////////////////////////////////////////
  public int getType() {
    return m_type;
  }

  public String getValue() {
    return m_value;
  }

  public int getOffset() {
    return m_offset;
  }

  public int getLength() {
    return m_length;
  }

  public int getEnd() {
    return m_offset + m_length;
  }
}