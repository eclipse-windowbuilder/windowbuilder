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
package org.eclipse.wb.internal.rcp.gef.policy.rcp.perspective;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.gef.policy.PolicyUtils;
import org.eclipse.wb.core.gef.policy.layout.flow.AbstractFlowLayoutEditPolicy;
import org.eclipse.wb.core.gef.policy.validator.LayoutRequestValidators;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.tree.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.rcp.model.rcp.PdeUtils.ViewInfo;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.FolderViewInfo;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.PageLayoutAddViewInfo;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.PageLayoutCreateFolderInfo;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.shortcuts.FastViewInfo;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.shortcuts.ViewShortcutInfo;

/**
 * Implementation of {@link LayoutEditPolicy} for {@link PageLayoutCreateFolderInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.gef.policy
 */
public final class PageLayoutCreateFolderLayoutEditPolicy extends AbstractFlowLayoutEditPolicy {
  private static final ILayoutRequestValidator VALIDATOR = LayoutRequestValidators.or(
      LayoutRequestValidators.modelType(FolderViewInfo.class),
      LayoutRequestValidators.modelType(PageLayoutAddViewInfo.class),
      LayoutRequestValidators.modelType(FastViewInfo.class),
      LayoutRequestValidators.modelType(ViewShortcutInfo.class));
  private final PageLayoutCreateFolderInfo m_folder;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public PageLayoutCreateFolderLayoutEditPolicy(PageLayoutCreateFolderInfo container) {
    m_folder = container;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Requests
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected boolean isGoodReferenceChild(Request request, EditPart editPart) {
    return editPart.getModel() instanceof FolderViewInfo;
  }

  @Override
  protected boolean isRequestCondition(Request request) {
    return super.isRequestCondition(request) || request instanceof ViewDropRequest;
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
    return true;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Command getCommand(Request request, Object referenceObject) {
    if (request instanceof ViewDropRequest) {
      final ViewDropRequest viewDrop_Request = (ViewDropRequest) request;
      final ViewInfo viewInfo = viewDrop_Request.getView();
      final FolderViewInfo reference = (FolderViewInfo) referenceObject;
      return new EditCommand(m_folder) {
        @Override
        protected void executeEdit() throws Exception {
          FolderViewInfo newView = m_folder.command_CREATE(viewInfo.getId(), reference);
          viewDrop_Request.setComponent(newView);
        }
      };
    }
    return super.getCommand(request, referenceObject);
  }

  @Override
  protected Command getMoveCommand(Object moveObject, Object referenceObject) {
    final FolderViewInfo item = (FolderViewInfo) moveObject;
    final FolderViewInfo nextItem = (FolderViewInfo) referenceObject;
    return new EditCommand(m_folder) {
      @Override
      protected void executeEdit() throws Exception {
        m_folder.command_MOVE(item, nextItem);
      }
    };
  }

  @Override
  protected Command getAddCommand(Object addObject, Object referenceObject) {
    if (addObject instanceof PageLayoutAddViewInfo) {
      PageLayoutAddViewInfo oldView = (PageLayoutAddViewInfo) addObject;
      return getAddCommand(oldView, oldView.getId(), referenceObject);
    }
    if (addObject instanceof FolderViewInfo) {
      FolderViewInfo oldView = (FolderViewInfo) addObject;
      return getAddCommand(oldView, oldView.getId(), referenceObject);
    }
    if (addObject instanceof FastViewInfo) {
      FastViewInfo oldView = (FastViewInfo) addObject;
      return getAddCommand(oldView, oldView.getId(), referenceObject);
    }
    if (addObject instanceof ViewShortcutInfo) {
      ViewShortcutInfo oldView = (ViewShortcutInfo) addObject;
      return getAddCommand(oldView, oldView.getId(), referenceObject);
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link Command} that move any "view" into this "folder".
   */
  private Command getAddCommand(final JavaInfo oldView, final String id, Object referenceObject) {
    final FolderViewInfo nextItem = (FolderViewInfo) referenceObject;
    return new EditCommand(m_folder) {
      @Override
      protected void executeEdit() throws Exception {
        FolderViewInfo newView = m_folder.command_CREATE(id, nextItem);
        oldView.delete();
        PolicyUtils.scheduleSelection(getHost(), newView);
      }
    };
  }
}