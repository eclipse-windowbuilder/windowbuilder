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
package org.eclipse.wb.internal.rcp.gef.policy.rcp.perspective.shortcuts;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.gef.policy.layout.flow.AbstractFlowLayoutEditPolicy;
import org.eclipse.wb.core.gef.policy.validator.LayoutRequestValidators;
import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.tree.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.rcp.gef.policy.rcp.perspective.PerspectiveDropRequest;
import org.eclipse.wb.internal.rcp.model.rcp.PdeUtils.PerspectiveInfo;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.PageLayoutInfo;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.shortcuts.PerspectiveShortcutContainerInfo;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.shortcuts.PerspectiveShortcutInfo;

/**
 * Implementation of {@link LayoutEditPolicy} for {@link PerspectiveShortcutContainerInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.gef.policy
 */
public final class PerspectiveShortcutContainerLayoutEditPolicy
    extends
      AbstractFlowLayoutEditPolicy {
  private static final ILayoutRequestValidator VALIDATOR =
      LayoutRequestValidators.modelType(PerspectiveShortcutInfo.class);
  private final PageLayoutInfo m_page;
  private final PerspectiveShortcutContainerInfo m_container;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public PerspectiveShortcutContainerLayoutEditPolicy(PerspectiveShortcutContainerInfo container) {
    m_container = container;
    m_page = container.getPage();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Requests
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected boolean isGoodReferenceChild(Request request, EditPart editPart) {
    return editPart.getModel() instanceof PerspectiveShortcutInfo;
  }

  @Override
  protected boolean isRequestCondition(Request request) {
    return super.isRequestCondition(request) || request instanceof PerspectiveDropRequest;
  }

  @Override
  protected ILayoutRequestValidator getRequestValidator() {
    return VALIDATOR;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // AbstractFlowLayoutEditPolicy
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected boolean isHorizontal(Request request) {
    return false;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Command getCommand(Request request, Object referenceObject) {
    if (request instanceof PerspectiveDropRequest) {
      final PerspectiveDropRequest perspectiveDrop_Request = (PerspectiveDropRequest) request;
      final PerspectiveInfo perspectiveInfo = perspectiveDrop_Request.getPerspective();
      final PerspectiveShortcutInfo reference = (PerspectiveShortcutInfo) referenceObject;
      return new EditCommand(m_page) {
        @Override
        protected void executeEdit() throws Exception {
          PerspectiveShortcutInfo newPerspective =
              m_container.command_CREATE(perspectiveInfo.getId(), reference);
          perspectiveDrop_Request.setComponent(newPerspective);
        }
      };
    }
    return super.getCommand(request, referenceObject);
  }

  @Override
  protected Command getMoveCommand(Object moveObject, Object referenceObject) {
    final PerspectiveShortcutInfo item = (PerspectiveShortcutInfo) moveObject;
    final PerspectiveShortcutInfo nextItem = (PerspectiveShortcutInfo) referenceObject;
    return new EditCommand(m_page) {
      @Override
      protected void executeEdit() throws Exception {
        m_container.command_MOVE(item, nextItem);
      }
    };
  }
}