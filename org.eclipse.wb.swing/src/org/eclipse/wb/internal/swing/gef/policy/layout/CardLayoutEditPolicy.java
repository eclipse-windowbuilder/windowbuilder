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
package org.eclipse.wb.internal.swing.gef.policy.layout;

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.swing.gef.policy.ComponentFlowLayoutEditPolicy;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.layout.CardLayoutInfo;

/**
 * Implementation of {@link LayoutEditPolicy} for {@link CardLayoutInfo}.
 * 
 * @author lobas_av
 * @coverage swing.gef.policy
 */
public final class CardLayoutEditPolicy extends ComponentFlowLayoutEditPolicy {
  private final CardLayoutInfo m_layout;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CardLayoutEditPolicy(CardLayoutInfo layout) {
    super(layout);
    m_layout = layout;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Requests
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected boolean isHorizontal(Request request) {
    return true;
  }

  @Override
  protected boolean isGoodReferenceChild(Request request, EditPart editPart) {
    if (editPart.getModel() instanceof ComponentInfo) {
      ComponentInfo component = (ComponentInfo) editPart.getModel();
      return m_layout.isManagedObject(component);
    }
    return false;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Children
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void decorateChild(EditPart child) {
    Object model = child.getModel();
    if (m_layout.isManagedObject(model)) {
      EditPolicy policy = new CardLayoutSelectionEditPolicy(m_layout);
      child.installEditPolicy(EditPolicy.SELECTION_ROLE, policy);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Command
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void command_CREATE(ComponentInfo newObject, ComponentInfo referenceObject)
      throws Exception {
    m_layout.command_CREATE(newObject, referenceObject);
  }

  @Override
  protected void command_MOVE(ComponentInfo object, ComponentInfo referenceObject) throws Exception {
    m_layout.command_MOVE(object, referenceObject);
  }
}