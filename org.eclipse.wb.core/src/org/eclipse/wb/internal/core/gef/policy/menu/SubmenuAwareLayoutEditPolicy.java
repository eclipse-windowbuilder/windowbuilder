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

import org.eclipse.wb.core.gef.policy.PolicyUtils;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.draw2d.Layer;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.core.requests.CreateRequest;
import org.eclipse.wb.gef.core.requests.IDropRequest;
import org.eclipse.wb.gef.core.requests.PasteRequest;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.core.gef.part.menu.MenuEditPart;
import org.eclipse.wb.internal.core.gef.part.menu.MenuPopupEditPart;
import org.eclipse.wb.internal.core.gef.part.menu.SubmenuAwareEditPart;
import org.eclipse.wb.internal.core.model.menu.IMenuInfo;
import org.eclipse.wb.internal.core.model.menu.IMenuObjectInfo;
import org.eclipse.wb.internal.core.model.menu.MenuObjectInfoUtils;

/**
 * {@link LayoutEditPolicy} for {@link SubmenuAwareEditPart} that shows sub-menu when user targets
 * on host. It is useful for example to add item into inner sub-menu, without expanding it before.
 *
 * @author mitin_aa
 * @author scheglov_ke
 * @coverage core.gef.menu
 */
public final class SubmenuAwareLayoutEditPolicy extends LayoutEditPolicy {
  private final IMenuObjectInfo m_object;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SubmenuAwareLayoutEditPolicy(IMenuObjectInfo object) {
    m_object = object;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Feedbacks
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Layer getFeedbackLayer() {
    return getLayer(IEditPartViewer.MENU_FEEDBACK_LAYER);
  }

  @Override
  public void showTargetFeedback(Request request) {
    PolicyUtils.showBorderTargetFeedback(this);
  }

  @Override
  public void eraseTargetFeedback(Request request) {
    PolicyUtils.eraseBorderTargetFeedback(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Request Routing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public EditPart getTargetEditPart(Request request) {
    // check that we understand this request
    if (!isRequestCondition(request)) {
      return null;
    }
    // "popup" always shows sub-menu
    if (getHost() instanceof MenuPopupEditPart) {
      return getHost();
    }
    // if standalone item, no fall through, always show menu
    if (!(getHost().getParent() instanceof MenuEditPart)) {
      return getHost();
    }
    // check if we have sub-menu for this item
    if (getSubMenu() == null) {
      return null;
    }
    // prepare location in figure
    Figure figure = getHostFigure();
    Point location = ((IDropRequest) request).getLocation().getCopy();
    FigureUtils.translateAbsoluteToFigure2(figure, location);
    // if request's mouse location are in middle 1/3 height (width) of figure then return getHost()
    IMenuInfo parentMenu = ((MenuEditPart) getHost().getParent()).getMenu();
    if (parentMenu.isHorizontal()) {
      int halfWidth = figure.getSize().width / 2;
      if (between(location.x, halfWidth - halfWidth / 4, halfWidth + halfWidth / 4)) {
        return getHost();
      }
    } else {
      int height = figure.getSize().height;
      if (between(location.y, height / 2 - height / 3, height / 2 + height / 3)) {
        return getHost();
      }
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private IMenuInfo getSubMenu() {
    return MenuObjectInfoUtils.getSubMenu(m_object);
  }

  private static boolean between(int x, int a, int b) {
    return x >= a && x <= b;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Request Validator
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected ILayoutRequestValidator getRequestValidator() {
    return m_validator;
  }

  private final ILayoutRequestValidator m_validator = new ILayoutRequestValidator() {
    public boolean validateCreateRequest(EditPart host, CreateRequest request) {
      IMenuInfo subMenu = getSubMenu();
      return subMenu != null && subMenu.getPolicy().validateCreate(request.getNewObject());
    }

    public boolean validatePasteRequest(EditPart host, PasteRequest request) {
      IMenuInfo subMenu = getSubMenu();
      return subMenu != null && subMenu.getPolicy().validatePaste(request.getMemento());
    }

    public boolean validateMoveRequest(EditPart host, ChangeBoundsRequest request) {
      IMenuInfo subMenu = getSubMenu();
      if (subMenu == null) {
        return false;
      }
      // check that each object can be accepted by sub-menu
      for (EditPart editPart : request.getEditParts()) {
        if (!subMenu.getPolicy().validateMove(editPart.getModel())) {
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
