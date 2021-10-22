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
package org.eclipse.wb.internal.swing.databinding.ui.contentproviders.el;

import org.eclipse.jface.text.rules.ICharacterScanner;

/**
 * Helper class for navigate into {@link ICharacterScanner} to forward and back.
 *
 * @author lobas_av
 * @coverage bindings.swing.ui
 */
public final class CharacterScannerWrapper {
  private int m_count;
  private final ICharacterScanner m_scanner;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CharacterScannerWrapper(ICharacterScanner scanner) {
    m_scanner = scanner;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public int read() {
    m_count++;
    return m_scanner.read();
  }

  public void unread() {
    for (int i = 0; i < m_count; i++) {
      m_scanner.unread();
    }
  }

  public boolean test(int ch) {
    return read() == ch;
  }

  public boolean test(int[] chars) {
    for (int ch : chars) {
      if (read() != ch) {
        return false;
      }
    }
    return true;
  }
}