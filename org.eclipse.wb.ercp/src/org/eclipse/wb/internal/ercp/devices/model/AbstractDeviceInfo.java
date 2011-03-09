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
package org.eclipse.wb.internal.ercp.devices.model;

/**
 * Abstract description for element of mobile devices.
 * 
 * @author scheglov_ke
 * @coverage ercp.device
 */
public abstract class AbstractDeviceInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // id
  //
  ////////////////////////////////////////////////////////////////////////////
  protected String m_id;

  /**
   * @return the id of this {@link AbstractDeviceInfo}.
   */
  public final String getId() {
    return m_id;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Name
  //
  ////////////////////////////////////////////////////////////////////////////
  protected String m_name;

  /**
   * @return the display name of this {@link AbstractDeviceInfo}.
   */
  public final String getName() {
    return m_name;
  }

  /**
   * Sets the display name for this {@link AbstractDeviceInfo}.
   */
  public final void setName(String name) {
    m_name = name;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Visible
  //
  ////////////////////////////////////////////////////////////////////////////
  private boolean m_visible = true;

  /**
   * @return <code>true</code> if this {@link AbstractDeviceInfo} should be displayed for user.
   */
  public final boolean isVisible() {
    return m_visible;
  }

  /**
   * Specifies if this {@link AbstractDeviceInfo} should be displayed for user.
   */
  public final void setVisible(boolean visible) {
    m_visible = visible;
  }
}
