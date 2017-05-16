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
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.core.policies.IEditPartDecorationListener;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Helper for replacing {@link EditPolicy#SELECTION_ROLE} during child {@link EditPart} decoration
 * and restoring original {@link SelectionEditPolicy} during undecoration.
 *
 * @author scheglov_ke
 * @coverage core.gef.policy
 */
public abstract class SelectionPolicyDecorationHelper {
  private final LayoutEditPolicy m_layoutPolicy;
  private final Map<EditPart, EditPolicy> m_policies = new WeakHashMap<EditPart, EditPolicy>();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SelectionPolicyDecorationHelper(LayoutEditPolicy layoutPolicy) {
    m_layoutPolicy = layoutPolicy;
    m_layoutPolicy.addEditPartListener(new IEditPartDecorationListener() {
      public void decorate(EditPart child) {
        if (shouldChangePolicy(child)) {
          rememberOldPolicy(child);
          setNewPolicy(child);
        }
      }

      public void undecorate(EditPart child) {
        if (shouldChangePolicy(child)) {
          restoreOldPolicy(child);
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implementation
  //
  ////////////////////////////////////////////////////////////////////////////
  private void rememberOldPolicy(EditPart child) {
    EditPolicy selectionPolicy = child.getEditPolicy(EditPolicy.SELECTION_ROLE);
    if (selectionPolicy != null) {
      m_policies.put(child, selectionPolicy);
    }
  }

  private void setNewPolicy(EditPart child) {
    EditPolicy newPolicy = getNewPolicy(child);
    child.installEditPolicy(EditPolicy.SELECTION_ROLE, newPolicy);
  }

  private void restoreOldPolicy(EditPart child) {
    EditPolicy selectionPolicy = m_policies.get(child);
    child.installEditPolicy(EditPolicy.SELECTION_ROLE, selectionPolicy);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implementation specific
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if {@link EditPolicy#SELECTION_ROLE} of given {@link EditPart} should
   *         be changed.
   */
  protected boolean shouldChangePolicy(EditPart child) {
    return true;
  }

  /**
   * @return new {@link EditPolicy} to use for {@link EditPolicy#SELECTION_ROLE}, may be
   *         <code>null</code>.
   */
  protected EditPolicy getNewPolicy(EditPart child) {
    return null;
  }
}
