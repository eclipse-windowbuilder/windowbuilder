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
package org.eclipse.wb.core.model.broadcast;

/**
 * Request object for {@link EditorActivatedListener#invoke(EditorActivatedRequest)}.
 *
 * @author scheglov_ke
 * @coverage core.model
 */
public final class EditorActivatedRequest {
  private boolean m_reparseRequested;
  private boolean m_refreshRequested;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public boolean isReparseRequested() {
    return m_reparseRequested;
  }

  public boolean isRefreshRequested() {
    return m_refreshRequested;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Requesting
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Specifies that reparse should be performed.
   */
  public void requestReparse() {
    m_reparseRequested = true;
  }

  /**
   * Specifies that refresh should be performed.
   */
  public void requestRefresh() {
    m_refreshRequested = true;
  }
}
