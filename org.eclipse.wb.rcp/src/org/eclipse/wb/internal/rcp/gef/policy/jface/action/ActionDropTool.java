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
package org.eclipse.wb.internal.rcp.gef.policy.jface.action;

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.core.tools.AbstractCreationTool;
import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.internal.rcp.model.jface.action.ActionContributionItemInfo;
import org.eclipse.wb.internal.rcp.model.jface.action.ActionInfo;

/**
 * {@link Tool} for adding new {@link ActionInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.gef.policy
 */
public final class ActionDropTool extends AbstractCreationTool {
  private final ActionInfo m_action;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ActionDropTool(ActionInfo action) {
    m_action = action;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // AbstractCreationTool
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Request createTargetRequest() {
    return new ActionDropRequest(m_action);
  }

  @Override
  protected void selectAddedObjects() {
    ActionDropRequest request = (ActionDropRequest) getTargetRequest();
    ActionContributionItemInfo item = request.getItem();
    if (item != null) {
      IEditPartViewer viewer = getViewer();
      EditPart editPart = viewer.getEditPartByModel(item);
      if (editPart != null) {
        viewer.select(editPart);
      }
    }
  }
}
