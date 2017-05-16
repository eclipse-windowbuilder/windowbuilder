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
 * A {@link Request} from testing {@link EditPart} on move and reparenting.
 *
 * @author lobas_av
 * @coverage gef.core
 */
public final class DragPermissionRequest extends Request {
  private boolean m_canMove = true;
  private boolean m_canReparent = true;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Permissions
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if move can be performed.
   */
  public boolean canMove() {
    return m_canMove;
  }

  /**
   * Enable/disable move.
   */
  public void setMove(boolean canMove) {
    m_canMove &= canMove;
  }

  /**
   * @return <code>true</code> if reparenting can be performed.
   */
  public boolean canReparent() {
    return m_canReparent;
  }

  /**
   * Enable/disable reparent.
   */
  public void setReparent(boolean canReparent) {
    m_canReparent &= canReparent;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    return "DragPermissionRequest(move=" + m_canMove + ", reparent=" + m_canReparent + ")";
  }
}