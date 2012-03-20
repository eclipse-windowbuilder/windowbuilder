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

import org.eclipse.wb.core.gef.policy.selection.NonResizableSelectionEditPolicy;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.core.utils.state.GlobalState;
import org.eclipse.wb.internal.swing.gef.policy.ComponentFlowLayoutEditPolicy;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.layout.GenericFlowLayoutInfo;

/**
 * Implementation of {@link LayoutEditPolicy} for {@link GenericFlowLayoutInfo}.
 * 
 * @author scheglov_ke
 * @author sablin_aa
 * @coverage swing.gef.policy
 */
public abstract class GenericFlowLayoutEditPolicy extends ComponentFlowLayoutEditPolicy {
  private final GenericFlowLayoutInfo m_layout;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public GenericFlowLayoutEditPolicy(GenericFlowLayoutInfo layout) {
    super(layout);
    m_layout = layout;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected boolean isRtl(Request request) {
    return m_layout.getContainer().isRTL();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Decoration
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void decorateChild(EditPart child) {
    if (child.getModel() instanceof ComponentInfo) {
      child.installEditPolicy(EditPolicy.SELECTION_ROLE, new NonResizableSelectionEditPolicy());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // AbstractFlowLayoutEditPolicy
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected final boolean isGoodReferenceChild(Request request, EditPart editPart) {
    Object model = editPart.getModel();
    return model instanceof ComponentInfo && GlobalState.getValidatorHelper().canReference(model);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void command_CREATE(ComponentInfo newObject, ComponentInfo referenceObject)
      throws Exception {
    m_layout.add(newObject, referenceObject);
  }

  @Override
  protected void command_MOVE(ComponentInfo object, ComponentInfo referenceObject) throws Exception {
    m_layout.move(object, referenceObject);
  }
}
