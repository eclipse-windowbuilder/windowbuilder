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
package org.eclipse.wb.internal.core.gefTree.policy.generic;

import org.eclipse.wb.core.gef.policy.validator.LayoutRequestValidators;
import org.eclipse.wb.core.gefTree.policy.ObjectLayoutEditPolicy;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.tree.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.core.gef.policy.layout.generic.AbstractContainerRequestValidator;
import org.eclipse.wb.internal.core.model.generic.FlowContainer;

/**
 * {@link LayoutEditPolicy} for {@link FlowContainer}.
 *
 * @author scheglov_ke
 * @coverage core.gefTree.policy
 */
public final class FlowContainerLayoutEditPolicy extends ObjectLayoutEditPolicy<Object> {
  private final FlowContainer m_container;
  private final ILayoutRequestValidator m_requestValidator;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public FlowContainerLayoutEditPolicy(ObjectInfo model, FlowContainer container) {
    super(model);
    m_container = container;
    {
      ILayoutRequestValidator validator = new AbstractContainerRequestValidator(container);
      validator = LayoutRequestValidators.cache(validator);
      m_requestValidator = LayoutRequestValidators.finalize(validator);
    }
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