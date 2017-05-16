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
package org.eclipse.wb.core.gef.policy.helpers;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;
import org.eclipse.wb.internal.core.utils.state.GlobalState;

/**
 * Helper for re-displaying selection of {@link SelectionEditPolicy} on refresh broadcast in model.
 *
 * @author scheglov_ke
 * @coverage core.gef.policy
 */
public class SelectionEditPolicyRefreshHelper {
  private final SelectionEditPolicy m_policy;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SelectionEditPolicyRefreshHelper(SelectionEditPolicy policy) {
    m_policy = policy;
    ObjectInfo hierarchyObject = GlobalState.getActiveObject();
    new BroadcastListenerHelper(hierarchyObject, policy, new ObjectEventListener() {
      @Override
      public void refreshed() throws Exception {
        boolean isActivePolicy = m_policy.isActive();
        boolean isSelectedHost = m_policy.getHost().getSelected() != EditPart.SELECTED_NONE;
        if (isActivePolicy && isSelectedHost) {
          m_policy.refreshSelection();
        }
      }
    });
  }
}
