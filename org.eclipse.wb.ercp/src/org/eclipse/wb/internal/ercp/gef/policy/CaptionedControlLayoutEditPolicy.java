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
package org.eclipse.wb.internal.ercp.gef.policy;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.gef.policy.PolicyUtils;
import org.eclipse.wb.core.gef.policy.layout.LayoutPolicyUtils2;
import org.eclipse.wb.core.gef.policy.layout.LayoutPolicyUtils2.IPasteProcessor;
import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.core.requests.CreateRequest;
import org.eclipse.wb.gef.core.requests.PasteRequest;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.ercp.model.widgets.mobile.CaptionedControlInfo;
import org.eclipse.wb.internal.swt.gef.ControlsLayoutRequestValidator;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

import java.util.List;

/**
 * Implementation of {@link LayoutEditPolicy} for dropping {@link ControlInfo} on
 * {@link CaptionedControlInfo} .
 * 
 * @author scheglov_ke
 * @coverage swt.gef.policy
 */
public final class CaptionedControlLayoutEditPolicy extends LayoutEditPolicy {
  private final CaptionedControlInfo m_captionedControl;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CaptionedControlLayoutEditPolicy(CaptionedControlInfo composite) {
    m_captionedControl = composite;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Routing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected ILayoutRequestValidator getRequestValidator() {
    return ControlsLayoutRequestValidator.INSTANCE;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Feedback
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
  // Command
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Command getCreateCommand(CreateRequest request) {
    if (m_captionedControl.getChildrenControls().isEmpty()) {
      final ControlInfo control = (ControlInfo) request.getNewObject();
      return new EditCommand(m_captionedControl) {
        @Override
        protected void executeEdit() throws Exception {
          JavaInfoUtils.add(control, null, m_captionedControl, null);
        }
      };
    }
    return null;
  }

  @Override
  protected Command getPasteCommand(PasteRequest request) {
    if (m_captionedControl.getChildrenControls().isEmpty()) {
      return LayoutPolicyUtils2.getPasteCommand(
          m_captionedControl,
          request,
          ControlInfo.class,
          new IPasteProcessor<ControlInfo>() {
            public void process(ControlInfo component) throws Exception {
              JavaInfoUtils.add(component, null, m_captionedControl, null);
            }
          });
    }
    return null;
  }

  @Override
  protected Command getAddCommand(ChangeBoundsRequest request) {
    List<EditPart> addParts = request.getEditParts();
    if (m_captionedControl.getChildrenControls().isEmpty() && addParts.size() == 1) {
      final ControlInfo control = (ControlInfo) addParts.get(0).getModel();
      return new EditCommand(m_captionedControl) {
        @Override
        protected void executeEdit() throws Exception {
          JavaInfoUtils.move(control, null, m_captionedControl, null);
        }
      };
    }
    return null;
  }
}