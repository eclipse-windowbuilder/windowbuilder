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
package org.eclipse.wb.internal.core.gef.policy.menu;

import org.eclipse.wb.core.gef.policy.layout.flow.AbstractFlowLayoutEditPolicy;
import org.eclipse.wb.draw2d.Layer;
import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.core.requests.CreateRequest;
import org.eclipse.wb.gef.core.requests.PasteRequest;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.core.model.menu.IMenuInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuItemInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuPolicy;
import org.eclipse.wb.internal.core.model.menu.MenuObjectInfoUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;

import java.util.List;

/**
 * {@link LayoutEditPolicy} for {@link IMenuItemInfo}'s in {@link IMenuInfo}.
 *
 * @author mitin_aa
 * @author scheglov_ke
 * @coverage core.gef.menu
 */
public final class MenuLayoutEditPolicy extends AbstractFlowLayoutEditPolicy {
  private final IMenuInfo m_menu;
  private final IMenuPolicy m_policy;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public MenuLayoutEditPolicy(IMenuInfo menu) {
    m_menu = menu;
    m_policy = m_menu.getPolicy();
  }

  /////////////////////////////////////////////////////////////////////
  //
  // AbstractFlowLayoutEditPolicy
  //
  /////////////////////////////////////////////////////////////////////
  @Override
  protected boolean isHorizontal(Request request) {
    return m_menu.isHorizontal();
  }

  @Override
  protected boolean isGoodReferenceChild(Request request, EditPart editPart) {
    if (MenuObjectInfoUtils.isImplicitObject(editPart.getModel())) {
      return false;
    }
    return true;
  }

  @Override
  protected Layer getFeedbackLayer() {
    return getLayer(IEditPartViewer.MENU_FEEDBACK_LAYER);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CREATE
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Command getCreateCommand(final Object newObject, final Object referenceObject) {
    return new Command() {
      @Override
      public void execute() throws Exception {
        m_menu.executeEdit(new RunnableEx() {
          public void run() throws Exception {
            m_policy.commandCreate(newObject, referenceObject);
          }
        });
      }
    };
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // PASTE
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Command getPasteCommand(final PasteRequest request, final Object referenceObject) {
    return new Command() {
      @Override
      public void execute() throws Exception {
        m_menu.executeEdit(new RunnableEx() {
          public void run() throws Exception {
            List<?> pastedObject = m_policy.commandPaste(request.getMemento(), referenceObject);
            request.setObjects(pastedObject);
          }
        });
      }
    };
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // MOVE
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Command getMoveCommand(final Object moveObject, final Object referenceObject) {
    return new Command() {
      @Override
      public void execute() throws Exception {
        m_menu.executeEdit(new RunnableEx() {
          public void run() throws Exception {
            m_policy.commandMove(moveObject, referenceObject);
          }
        });
      }
    };
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Validator
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected ILayoutRequestValidator getRequestValidator() {
    return VALIDATOR;
  }

  private final ILayoutRequestValidator VALIDATOR = new ILayoutRequestValidator() {
    public boolean validateCreateRequest(EditPart host, CreateRequest request) {
      return m_policy.validateCreate(request.getNewObject());
    }

    public boolean validatePasteRequest(EditPart host, PasteRequest request) {
      return m_policy.validatePaste(request.getMemento());
    }

    public boolean validateMoveRequest(EditPart host, ChangeBoundsRequest request) {
      for (EditPart editPart : request.getEditParts()) {
        if (!m_policy.validateMove(editPart.getModel())) {
          return false;
        }
      }
      return true;
    }

    public boolean validateAddRequest(EditPart host, ChangeBoundsRequest request) {
      return validateMoveRequest(host, request);
    }
  };
}
