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
package org.eclipse.wb.internal.core.gef.policy.layout.generic;

import org.eclipse.wb.core.gef.policy.layout.flow.ObjectFlowLayoutEditPolicy;
import org.eclipse.wb.core.gef.policy.selection.NonResizableSelectionEditPolicy;
import org.eclipse.wb.core.gef.policy.validator.LayoutRequestValidators;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.core.model.generic.FlowContainer;

/**
 * {@link LayoutEditPolicy} for {@link FlowContainer_Support}.
 *
 * @author scheglov_ke
 * @coverage core.gef.policy
 */
public final class FlowContainerLayoutEditPolicy extends ObjectFlowLayoutEditPolicy<Object> {
  private final FlowContainer m_container;
  private final ILayoutRequestValidator m_requestValidator;
  private final ObjectInfo m_model;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public FlowContainerLayoutEditPolicy(ObjectInfo model, FlowContainer container) {
    super(model);
    m_model = model;
    m_container = container;
    {
      ILayoutRequestValidator validator = new AbstractContainerRequestValidator(container);
      validator = LayoutRequestValidators.cache(validator);
      m_requestValidator = LayoutRequestValidators.finalize(validator);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Decoration
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void decorateChild(EditPart child) {
    if (m_container.validateComponent(child.getModel())) {
      child.installEditPolicy(EditPolicy.SELECTION_ROLE, new NonResizableSelectionEditPolicy());
      new SelectionEditPolicyInstaller(m_model, child).decorate();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected boolean isHorizontal(Request request) {
    return m_container.isHorizontal();
  }

  @Override
  protected boolean isRtl(Request request) {
    return m_container.isRtl();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Requests
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected ILayoutRequestValidator getRequestValidator() {
    return m_requestValidator;
  }

  @Override
  protected boolean isGoodReferenceChild(Request request, EditPart editPart) {
    Object referenceModel = editPart.getModel();
    return m_container.validateReference(referenceModel);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void command_CREATE(Object newObject, Object referenceObject) throws Exception {
    m_container.command_CREATE(newObject, referenceObject);
  }

  @Override
  protected void command_MOVE(Object object, Object referenceObject) throws Exception {
    m_container.command_MOVE(object, referenceObject);
  }
}
