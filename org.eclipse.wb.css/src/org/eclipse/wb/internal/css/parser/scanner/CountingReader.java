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

import java.io.IOException;
import java.io.Reader;

/**
 * This class represents a reader which normalizes the line break: \n, \r, \r\n are replaced by \n.
 * 
 * The methods of this reader are not synchronized. The input is buffered.
 * 
 * @author scheglov_ke
 */
public final class CountingReader extends Reader {
  private Reader m_reader;
  private int m_offset;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CountingReader(Reader reader) {
    m_reader = reader;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Reader
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public int read(char cbuf[], int off, int len) throws IOException {
    int count = m_reader.read(cbuf, off, len);
    if (count > 0) {
      m_offset += count;
    }
    return count;
  }

  @Override
  public void close() throws IOException {
    m_reader.close();
    m_reader = null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the offset character that will be read
   */
  public int getOffset() {
    return m_offset;
  }
}
