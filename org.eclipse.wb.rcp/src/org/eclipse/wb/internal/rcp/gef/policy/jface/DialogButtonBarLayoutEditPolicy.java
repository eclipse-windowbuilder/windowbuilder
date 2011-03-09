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
package org.eclipse.wb.internal.rcp.gef.policy.jface;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.gef.policy.layout.flow.AbstractFlowLayoutEditPolicy;
import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.rcp.gef.part.jface.DialogButtonBarEditPart;
import org.eclipse.wb.internal.rcp.model.jface.DialogInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

/**
 * {@link LayoutEditPolicy} for dropping buttons on {@link DialogButtonBarEditPart}.
 * 
 * @author scheglov_ke
 * @coverage rcp.gef.policy
 */
public final class DialogButtonBarLayoutEditPolicy extends AbstractFlowLayoutEditPolicy {
  private final CompositeInfo m_composite;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DialogButtonBarLayoutEditPolicy(CompositeInfo composite) {
    m_composite = composite;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected boolean isHorizontal(Request request) {
    return true;
  }

  @Override
  protected boolean isGoodReferenceChild(Request request, EditPart editPart) {
    return editPart.getModel() instanceof ControlInfo;
  }

  @Override
  protected boolean isRequestCondition(Request request) {
    return super.isRequestCondition(request) || request instanceof DialogButtonDropRequest;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Validation
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final ILayoutRequestValidator VALIDATOR =
      new ILayoutRequestValidator.LayoutRequestValidatorStubFalse() {
        @Override
        public boolean validateMoveRequest(EditPart host, ChangeBoundsRequest request) {
          return true;
        }
      };

  @Override
  protected ILayoutRequestValidator getRequestValidator() {
    return VALIDATOR;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Command getCommand(Request request, Object referenceObject) {
    if (request instanceof DialogButtonDropRequest) {
      final DialogButtonDropRequest buttonRequest = (DialogButtonDropRequest) request;
      final ControlInfo reference = (ControlInfo) referenceObject;
      return new EditCommand(m_composite) {
        @Override
        protected void executeEdit() throws Exception {
          ControlInfo newButton = DialogInfo.createButtonOnButtonBar(m_composite, reference);
          buttonRequest.setButton(newButton);
        }
      };
    }
    return super.getCommand(request, referenceObject);
  }

  @Override
  protected Command getMoveCommand(Object moveObject, Object referenceObject) {
    final ControlInfo button = (ControlInfo) moveObject;
    final ControlInfo reference = (ControlInfo) referenceObject;
    return new EditCommand(m_composite) {
      @Override
      protected void executeEdit() throws Exception {
        DialogInfo.moveButtonOnButtonBar(button, reference);
      }
    };
  }
}
