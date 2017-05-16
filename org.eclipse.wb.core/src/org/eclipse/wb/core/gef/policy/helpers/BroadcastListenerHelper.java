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
import org.eclipse.wb.core.model.broadcast.BroadcastSupport;
import org.eclipse.wb.gef.core.events.IEditPolicyListener;
import org.eclipse.wb.gef.core.policies.EditPolicy;

/**
 * Helper for adding/removing listeners to {@link BroadcastSupport} on {@link EditPolicy}
 * activate/deactivate.
 *
 * @author scheglov_ke
 * @coverage core.gef.policy
 */
public final class BroadcastListenerHelper implements IEditPolicyListener {
  private final ObjectInfo m_object;
  private final EditPolicy m_editPolicy;
  private final Object m_listener;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public BroadcastListenerHelper(ObjectInfo object, EditPolicy editPolicy, Object listener) {
    m_object = object;
    m_editPolicy = editPolicy;
    m_listener = listener;
    m_editPolicy.addEditPolicyListener(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IEditPolicyListener
  //
  ////////////////////////////////////////////////////////////////////////////
  public void activatePolicy(EditPolicy policy) {
    m_object.addBroadcastListener(m_listener);
  }

  public void deactivatePolicy(EditPolicy policy) {
    m_object.removeBroadcastListener(m_listener);
    m_editPolicy.removeEditPolicyListener(this);
  }
}
