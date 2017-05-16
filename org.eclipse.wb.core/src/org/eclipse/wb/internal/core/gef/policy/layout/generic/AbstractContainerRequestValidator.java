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

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.core.requests.CreateRequest;
import org.eclipse.wb.gef.core.requests.PasteRequest;
import org.eclipse.wb.gef.tree.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.core.model.generic.AbstractContainer;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.state.GlobalState;

import java.util.List;

/**
 * {@link LayoutEditPolicy} for {@link AbstractContainer}.
 *
 * @author scheglov_ke
 * @coverage core.gef.policy
 */
public final class AbstractContainerRequestValidator implements ILayoutRequestValidator {
  private final AbstractContainer m_container;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractContainerRequestValidator(AbstractContainer container) {
    m_container = container;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Validation
  //
  ////////////////////////////////////////////////////////////////////////////
  public boolean validateCreateRequest(EditPart host, CreateRequest request) {
    Object newObject = request.getNewObject();
    return m_container.validateComponent(newObject);
  }

  public boolean validatePasteRequest(EditPart host, final PasteRequest request) {
    return ExecutionUtils.runObjectLog(new RunnableObjectEx<Boolean>() {
      public Boolean runObject() throws Exception {
        List<?> mementos = (List<?>) request.getMemento();
        for (Object memento : mementos) {
          Object component = GlobalState.getValidatorHelper().getPasteComponent(memento);
          if (!m_container.validateComponent(component)) {
            return false;
          }
        }
        return true;
      }
    }, false);
  }

  public boolean validateMoveRequest(EditPart host, ChangeBoundsRequest request) {
    return validateComponents(request);
  }

  public boolean validateAddRequest(EditPart host, ChangeBoundsRequest request) {
    return validateComponents(request);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private boolean validateComponents(ChangeBoundsRequest request) {
    for (EditPart editPart : request.getEditParts()) {
      Object object = editPart.getModel();
      if (!m_container.validateComponent(object)) {
        return false;
      }
    }
    return true;
  }
}
