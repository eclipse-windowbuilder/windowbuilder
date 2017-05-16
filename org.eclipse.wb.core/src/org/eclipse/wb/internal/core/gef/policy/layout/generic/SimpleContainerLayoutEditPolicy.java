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

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.gef.policy.PolicyUtils;
import org.eclipse.wb.core.gef.policy.validator.LayoutRequestValidators;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.core.requests.CreateRequest;
import org.eclipse.wb.gef.core.requests.PasteRequest;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.core.model.generic.SimpleContainer;
import org.eclipse.wb.internal.core.utils.state.GlobalState;
import org.eclipse.wb.internal.core.utils.state.IPasteComponentProcessor;

import java.util.List;

/**
 * {@link LayoutEditPolicy} for {@link SimpleContainer_Support}.
 *
 * @author scheglov_ke
 * @coverage core.gef.policy
 */
public final class SimpleContainerLayoutEditPolicy extends LayoutEditPolicy {
  private final ObjectInfo m_model;
  private final SimpleContainer m_container;
  private final ILayoutRequestValidator m_requestValidator;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SimpleContainerLayoutEditPolicy(ObjectInfo model, SimpleContainer container) {
    m_model = model;
    m_container = container;
    {
      ILayoutRequestValidator validator = new AbstractContainerRequestValidator(container);
      validator = LayoutRequestValidators.cache(validator);
      m_requestValidator = validator;
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

  ////////////////////////////////////////////////////////////////////////////
  //
  // Feedbacks
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void showLayoutTargetFeedback(Request request) {
    PolicyUtils.showBorderTargetFeedback(this);
  }

  @Override
  protected void eraseLayoutTargetFeedback(Request request) {
    PolicyUtils.eraseBorderTargetFeedback(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Command getCommand(Request request) {
    if (!m_container.isEmpty()) {
      return null;
    }
    return super.getCommand(request);
  }

  @Override
  protected Command getCreateCommand(final CreateRequest request) {
    final Object newObject = request.getNewObject();
    return new EditCommand(m_model) {
      @Override
      protected void executeEdit() throws Exception {
        m_container.command_CREATE(newObject);
      }
    };
  }

  @Override
  protected Command getPasteCommand(PasteRequest request) {
    return GlobalState.getPasteRequestProcessor().getPasteCommand(
        request,
        new IPasteComponentProcessor() {
          public void process(Object component) throws Exception {
            m_container.command_CREATE(component);
          }
        });
  }

  @Override
  protected Command getAddCommand(ChangeBoundsRequest request) {
    final List<EditPart> parts = request.getEditParts();
    if (parts.size() == 1) {
      return new EditCommand(m_model) {
        @Override
        protected void executeEdit() throws Exception {
          Object object = parts.get(0).getModel();
          m_container.command_ADD(object);
        }
      };
    }
    return null;
  }
}
