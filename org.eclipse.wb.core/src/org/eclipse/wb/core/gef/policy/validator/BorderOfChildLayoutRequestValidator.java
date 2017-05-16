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
package org.eclipse.wb.core.gef.policy.validator;

import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.core.requests.CreateRequest;
import org.eclipse.wb.gef.core.requests.IDropRequest;
import org.eclipse.wb.gef.core.requests.PasteRequest;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.internal.core.utils.state.GlobalState;
import org.eclipse.wb.internal.core.utils.state.IParametersProvider;

import java.util.List;

/**
 * Container may have layout when children are placed side by side, without any spacing between
 * them. So, if children are containers too, user can not use design canvas to point to container to
 * create/move child.
 * <p>
 * This implementation of {@link ILayoutRequestValidator} checks that if container is selected, then
 * border around its children is considered as transparent.
 *
 * @author scheglov_ke
 * @coverage core.gef.policy
 */
public final class BorderOfChildLayoutRequestValidator implements ILayoutRequestValidator {
  public static final ILayoutRequestValidator INSTANCE = new BorderOfChildLayoutRequestValidator();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private BorderOfChildLayoutRequestValidator() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ILayoutRequestValidator
  //
  ////////////////////////////////////////////////////////////////////////////
  public boolean validateCreateRequest(EditPart host, CreateRequest request) {
    if (request.isEraseFeedback()) {
      return true;
    }
    return isTargetingToHost_containerSelected(host, request);
  }

  public boolean validatePasteRequest(EditPart host, PasteRequest request) {
    if (request.isEraseFeedback()) {
      return true;
    }
    return isTargetingToHost_containerSelected(host, request);
  }

  public boolean validateMoveRequest(EditPart host, ChangeBoundsRequest request) {
    return true;
  }

  public boolean validateAddRequest(EditPart host, ChangeBoundsRequest request) {
    if (request.isEraseFeedback()) {
      return true;
    }
    // if "child" and "host" are siblings, check for borders
    if (host instanceof GraphicalEditPart) {
      GraphicalEditPart graphicalHost = (GraphicalEditPart) host;
      if (request.getEditParts().get(0).getParent() == host.getParent()) {
        return isTargeting_innerPartOfHost(graphicalHost, request);
      }
    }
    return true;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implementation
  //
  ////////////////////////////////////////////////////////////////////////////
  private static boolean isTargetingToHost_containerSelected(EditPart host, IDropRequest request) {
    if (host instanceof GraphicalEditPart) {
      GraphicalEditPart graphicalHost = (GraphicalEditPart) host;
      if (isTransparentOnBorders(graphicalHost)) {
        return isTargeting_innerPartOfHost(graphicalHost, request);
      }
    }
    return true;
  }

  private static boolean isTargeting_innerPartOfHost(GraphicalEditPart host, IDropRequest request) {
    Figure hostFigure = host.getFigure();
    // prepare location in host
    Point location;
    {
      location = request.getLocation().getCopy();
      FigureUtils.translateAbsoluteToFigure2(hostFigure, location);
    }
    // check if "location" is inside of "inner part" of host
    Rectangle hostClientArea = hostFigure.getClientArea();
    return hostClientArea.getExpanded(-3, -3).contains(location);
  }

  private static boolean isTransparentOnBorders(GraphicalEditPart host) {
    {
      Object hostModel = host.getModel();
      IParametersProvider provider = GlobalState.getParametersProvider();
      if (provider.hasTrueParameter(hostModel, "GEF.transparentOnBorders.always")) {
        return true;
      }
    }
    return isChildOf_selectedEditPart(host);
  }

  private static boolean isChildOf_selectedEditPart(EditPart child) {
    List<EditPart> selectedEditParts = child.getViewer().getSelectedEditParts();
    return selectedEditParts.size() == 1 && areParentChild(selectedEditParts.get(0), child);
  }

  private static boolean areParentChild(EditPart parent, EditPart child) {
    for (EditPart part = child.getParent(); part != null; part = part.getParent()) {
      if (part == parent) {
        return true;
      }
    }
    return false;
  }
}
