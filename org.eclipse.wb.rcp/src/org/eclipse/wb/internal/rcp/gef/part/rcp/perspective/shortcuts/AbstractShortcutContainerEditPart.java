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
package org.eclipse.wb.internal.rcp.gef.part.rcp.perspective.shortcuts;

import org.eclipse.wb.core.gef.policy.selection.NonResizableSelectionEditPolicy;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.shortcuts.AbstractShortcutContainerInfo;

import java.util.Collections;
import java.util.List;

/**
 * {@link EditPart} for {@link AbstractShortcutContainerInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.gef.part
 */
abstract class AbstractShortcutContainerEditPart extends GraphicalEditPart {
  private final AbstractShortcutContainerInfo m_container;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractShortcutContainerEditPart(AbstractShortcutContainerInfo container) {
    m_container = container;
    setModel(m_container);
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
    Rectangle bounds = m_container.getBounds();
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
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Children
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected List<?> getModelChildren() {
    return ExecutionUtils.runObjectLog(new RunnableObjectEx<List<?>>() {
      public List<?> runObject() throws Exception {
        return m_container.getPresentation().getChildrenGraphical();
      }
    }, Collections.emptyList());
  }
}
