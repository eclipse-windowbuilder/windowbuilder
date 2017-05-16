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
package org.eclipse.wb.internal.gef.tree.tools;

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.core.requests.SelectionRequest;
import org.eclipse.wb.gef.core.tools.Tool;

/**
 * Special {@link Tool} for handle only double-click mouse event and route it to {@link EditPart}.
 * This need for tree edit part's that it's not contains special tools for handle selection (via
 * SelectEditPartTracker).
 *
 * @author lobas_av
 * @coverage gef.tree
 */
public class DoubleClickEditPartTracker extends Tool {
  private final EditPart m_sourceEditPart;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DoubleClickEditPartTracker(EditPart sourceEditPart) {
    m_sourceEditPart = sourceEditPart;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // High-Level handle MouseEvent
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void handleDoubleClick(int button) {
    if (button == 1) {
      SelectionRequest request = new SelectionRequest(Request.REQ_OPEN);
      request.setLocation(getLocation());
      m_sourceEditPart.performRequest(request);
    }
  }
}