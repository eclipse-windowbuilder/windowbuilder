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
package org.eclipse.wb.internal.rcp.gef.policy.widgets;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.gef.policy.PolicyUtils;
import org.eclipse.wb.core.gef.policy.layout.LayoutPolicyUtils2;
import org.eclipse.wb.core.gef.policy.layout.LayoutPolicyUtils2.IPasteProcessor;
import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.core.requests.CreateRequest;
import org.eclipse.wb.gef.core.requests.PasteRequest;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.rcp.model.widgets.ScrolledCompositeInfo;
import org.eclipse.wb.internal.swt.gef.ControlsLayoutRequestValidator;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

/**
 * Implementation of {@link LayoutEditPolicy} for {@link ScrolledCompositeInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.gef.policy
 */
public final class ScrolledCompositeLayoutEditPolicy extends LayoutEditPolicy {
  private final ScrolledCompositeInfo m_composite;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Composite
  //
  ////////////////////////////////////////////////////////////////////////////
  public ScrolledCompositeLayoutEditPolicy(ScrolledCompositeInfo composite) {
    m_composite = composite;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Requests
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected ILayoutRequestValidator getRequestValidator() {
    return ControlsLayoutRequestValidator.INSTANCE;
  }

  @Override
  protected boolean isRequestCondition(Request request) {
    return super.isRequestCondition(request) && m_composite.getContent() == null;
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
  protected Command getCreateCommand(CreateRequest request) {
    final ControlInfo control = (ControlInfo) request.getNewObject();
    return new EditCommand(m_composite) {
      @Override
      protected void executeEdit() throws Exception {
        m_composite.command_CREATE(control);
      }
    };
  }

  @Override
  protected Command getPasteCommand(PasteRequest request) {
    return LayoutPolicyUtils2.getPasteCommand(
        m_composite,
        request,
        ControlInfo.class,
        new IPasteProcessor<ControlInfo>() {
          @Override
          public void process(ControlInfo control) throws Exception {
            m_composite.command_CREATE(control);
          }
        });
  }

  @Override
  protected Command getAddCommand(ChangeBoundsRequest request) {
    if (request.getEditParts().size() == 1) {
      final ControlInfo control = (ControlInfo) request.getEditParts().get(0).getModel();
      return new EditCommand(m_composite) {
        @Override
        protected void executeEdit() throws Exception {
          m_composite.command_ADD(control);
        }
      };
    }
    return null;
  }
}
