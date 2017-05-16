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

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.events.IEditPartSelectionListener;
import org.eclipse.wb.gef.core.events.IEditPolicyListener;
import org.eclipse.wb.gef.core.policies.EditPolicy;

/**
 * Helper for adding/removing {@link IEditPartSelectionListener} for host {@link EditPart} on
 * {@link EditPolicy} activate/deactivate.
 *
 * @author scheglov_ke
 * @coverage core.gef.policy
 */
public final class SelectionListenerHelper implements IEditPolicyListener {
  private final EditPolicy m_editPolicy;
  private final IEditPartSelectionListener m_listener;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SelectionListenerHelper(EditPolicy editPolicy, IEditPartSelectionListener listener) {
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
    m_editPolicy.getHost().addSelectionListener(m_listener);
  }

  public void deactivatePolicy(EditPolicy policy) {
    m_editPolicy.getHost().removeSelectionListener(m_listener);
    m_editPolicy.removeEditPolicyListener(this);
  }
}
