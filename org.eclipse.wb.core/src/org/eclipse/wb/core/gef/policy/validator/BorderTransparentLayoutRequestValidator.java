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
import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.core.requests.CreateRequest;
import org.eclipse.wb.gef.core.requests.IDropRequest;
import org.eclipse.wb.gef.core.requests.PasteRequest;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;

/**
 * Sometimes we want to make {@link EditPolicy} work only inside of inner part of
 * {@link GraphicalEditPart}. So, we want to exclude some insets on borders.
 *
 * @author scheglov_ke
 * @coverage core.gef.policy
 */
public final class BorderTransparentLayoutRequestValidator implements ILayoutRequestValidator {
  private final Insets m_insets;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public BorderTransparentLayoutRequestValidator(int hBorder, int vBorder) {
    this(vBorder, hBorder, vBorder, hBorder);
  }

  public BorderTransparentLayoutRequestValidator(int top, int left, int bottom, int right) {
    m_insets = new Insets(top, left, bottom, right);
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
    return isInnerPartOfHost(host, request);
  }

  public boolean validatePasteRequest(EditPart host, PasteRequest request) {
    if (request.isEraseFeedback()) {
      return true;
    }
    return isInnerPartOfHost(host, request);
  }

  public boolean validateMoveRequest(EditPart host, ChangeBoundsRequest request) {
    return true;
  }

  public boolean validateAddRequest(EditPart host, ChangeBoundsRequest request) {
    if (request.isEraseFeedback()) {
      return true;
    }
    return isInnerPartOfHost(host, request);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implementation
  //
  ////////////////////////////////////////////////////////////////////////////
  private boolean isInnerPartOfHost(EditPart host, IDropRequest request) {
    if (host instanceof GraphicalEditPart) {
      GraphicalEditPart graphicalHost = (GraphicalEditPart) host;
      Figure hostFigure = graphicalHost.getFigure();
      // prepare location in host
      Point location;
      {
        location = request.getLocation().getCopy();
        FigureUtils.translateAbsoluteToFigure2(hostFigure, location);
      }
      // check if "location" is inside of "inner part" of host
      Rectangle hostClientArea = hostFigure.getClientArea();
      return hostClientArea.getCropped(m_insets).contains(location);
    }
    return true;
  }
}
