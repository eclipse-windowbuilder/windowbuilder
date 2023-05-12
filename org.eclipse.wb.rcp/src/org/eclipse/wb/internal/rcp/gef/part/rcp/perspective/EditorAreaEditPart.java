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
package org.eclipse.wb.internal.rcp.gef.part.rcp.perspective;

import org.eclipse.wb.core.gef.policy.selection.NonResizableSelectionEditPolicy;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.internal.rcp.gef.policy.rcp.perspective.PageLayoutSidesLayoutEditPolicy;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.EditorAreaInfo;

import org.eclipse.draw2d.geometry.Rectangle;

/**
 * {@link EditPart} for {@link EditorAreaInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.gef.part
 */
public final class EditorAreaEditPart extends GraphicalEditPart {
  private final EditorAreaInfo m_editorArea;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public EditorAreaEditPart(EditorAreaInfo editorArea) {
    m_editorArea = editorArea;
    setModel(m_editorArea);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Figure
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Figure createFigure() {
    return new Figure();
  }

  @Override
  protected void refreshVisuals() {
    Rectangle bounds = m_editorArea.getBounds();
    getFigure().setBounds(bounds);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Policies
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createEditPolicies() {
    installEditPolicy(EditPolicy.SELECTION_ROLE, new NonResizableSelectionEditPolicy());
    installEditPolicy(new PageLayoutSidesLayoutEditPolicy(m_editorArea.getPage(),
        m_editorArea,
        false));
  }
}
