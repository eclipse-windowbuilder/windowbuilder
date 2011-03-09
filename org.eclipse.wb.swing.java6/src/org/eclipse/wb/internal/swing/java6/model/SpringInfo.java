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
package org.eclipse.wb.internal.swing.java6.model;

import org.apache.commons.lang.NotImplementedException;

/**
 * Base class for all GroupLayout elements.
 * 
 * @author mitin_aa
 * @coverage swing.model.layout.group
 */
public abstract class SpringInfo {
  /**
   * used for size values which wasn't explicitly set
   */
  public static final int UNSET = Integer.MIN_VALUE;
  // fields
  private SpringInfo m_parent;
  private int m_min = UNSET;
  private int m_pref = UNSET;
  private int m_max = UNSET;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public final void setParent(SpringInfo parent) {
    m_parent = parent;
  }

  public final SpringInfo getParent() {
    return m_parent;
  }

  public final void setSizes(int minValue, int prefValue, int maxValue) {
    m_min = minValue;
    m_pref = prefValue;
    m_max = maxValue;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Save
  //
  ////////////////////////////////////////////////////////////////////////////
  public final String getCode() throws Exception {
    return getCode(0);
  }

  /**
   * @param level
   */
  protected String getCode(int level) throws Exception {
    throw new NotImplementedException();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Dump
  //
  ////////////////////////////////////////////////////////////////////////////
  public void dump(@SuppressWarnings("unused") int level, StringBuffer buffer) {
    buffer.append((m_min == UNSET ? "UNSET" : m_min) + " ");
    buffer.append((m_pref == UNSET ? "UNSET" : m_pref) + " ");
    buffer.append((m_max == UNSET ? "UNSET" : m_max));
  }

  @Override
  public String toString() {
    StringBuffer dump = new StringBuffer();
    dump(0, dump);
    return dump.toString();
  }
}
