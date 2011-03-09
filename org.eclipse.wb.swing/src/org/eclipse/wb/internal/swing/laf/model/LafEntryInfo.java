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
package org.eclipse.wb.internal.swing.laf.model;

/**
 * Abstract LAF entry: category or LAF.
 * 
 * @author mitin_aa
 * @coverage swing.laf.model
 */
public abstract class LafEntryInfo {
  private String m_name;
  private String m_id;
  private boolean m_visible = true;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public LafEntryInfo(String id, String name) {
    m_id = id;
    m_name = name;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets the name for this entry.
   * 
   * @param name
   *          the name string to set.
   */
  public final void setName(String name) {
    m_name = name;
  }

  /**
   * @return the name of this entry.
   */
  public final String getName() {
    return m_name;
  }

  /**
   * Sets the ID for this entry.
   * 
   * @param id
   *          the ID string to set.
   */
  public final void setID(String id) {
    m_id = id;
  }

  /**
   * @return the ID of this entry.
   */
  public final String getID() {
    return m_id;
  }

  /**
   * @return <code>true</code> if this entry should be displayed for the user.
   */
  public final boolean isVisible() {
    return m_visible;
  }

  /**
   * Specifies if this entry should be displayed for the user.
   */
  public final void setVisible(boolean visible) {
    m_visible = visible;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Misc
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    return "id: " + m_id + ", name: " + m_name;
  }
}