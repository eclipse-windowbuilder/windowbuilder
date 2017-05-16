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
package org.eclipse.wb.gef.core.requests;

import org.eclipse.wb.gef.core.EditPart;

/**
 * A {@link Request} to select an {@link EditPart}.
 *
 * @author lobas_av
 * @coverage gef.core
 */
public class SelectionRequest extends LocationRequest {
  private int m_lastButtonPressed;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Constructs an empty {@link SelectionRequest}.
   */
  public SelectionRequest() {
  }

  /**
   * Constructs a {@link SelectionRequest} with the specified <i>type</i>.
   */
  public SelectionRequest(Object type) {
    super(type);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Returns the last button that was pressed. This is useful if there is more than one mouse button
   * pressed and the most recent button pressed needs to be identified.
   */
  public int getLastButtonPressed() {
    return m_lastButtonPressed;
  }

  /**
   * Sets the last mouse button that was pressed.
   */
  public void setLastButtonPressed(int lastButtonPressed) {
    m_lastButtonPressed = lastButtonPressed;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    StringBuffer buffer = new StringBuffer("SelectionRequest(type=");
    buffer.append(getType());
    buffer.append(", location=");
    buffer.append(getLocation());
    buffer.append(", stateMask=");
    buffer.append(getStateMask());
    buffer.append(", button=");
    buffer.append(m_lastButtonPressed);
    buffer.append(")");
    return buffer.toString();
  }
}