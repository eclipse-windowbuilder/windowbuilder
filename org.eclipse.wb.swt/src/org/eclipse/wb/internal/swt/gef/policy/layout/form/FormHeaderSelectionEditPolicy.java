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
package org.eclipse.wb.internal.swt.gef.policy.layout.form;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.gef.header.AbstractHeaderSelectionEditPolicy;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.draw2d.ILocator;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.graphical.handles.Handle;
import org.eclipse.wb.gef.graphical.handles.MoveHandle;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;

import java.util.List;

/**
 * SelectionEditPolicy for {@link FormHeaderEditPart}.
 *
 * @author mitin_aa
 */
final class FormHeaderSelectionEditPolicy extends AbstractHeaderSelectionEditPolicy {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public FormHeaderSelectionEditPolicy(LayoutEditPolicy mainPolicy) {
    super(mainPolicy);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Handles
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected List<Handle> createSelectionHandles() {
    List<Handle> handles = Lists.newArrayList();
    // move handle
    {
      MoveHandle moveHandle = new MoveHandle(getHost(), new HeaderMoveHandleLocator());
      moveHandle.setBorder(null);
      handles.add(moveHandle);
    }
    //
    return handles;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Move location
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Implementation of {@link ILocator} to place handle directly on header.
   */
  private class HeaderMoveHandleLocator implements ILocator {
    public void relocate(Figure target) {
      Figure reference = getHostFigure();
      Rectangle bounds = reference.getBounds().getCopy();
      FigureUtils.translateFigureToFigure(reference, target, bounds);
      target.setBounds(bounds);
    }
  }
}
