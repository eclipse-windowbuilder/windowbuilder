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
package org.eclipse.wb.core.gef.policy.selection;

import com.google.common.collect.Lists;

import org.eclipse.wb.draw2d.IPositionConstants;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.graphical.handles.Handle;
import org.eclipse.wb.gef.graphical.handles.MoveHandle;
import org.eclipse.wb.gef.graphical.handles.ResizeHandle;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;
import org.eclipse.wb.gef.graphical.tools.ResizeTracker;

import java.util.List;

/**
 * {@link SelectionEditPolicy} that shows {@link Handle}'s around {@link EditPart} but does not
 * support any resizing.
 *
 * @author scheglov_ke
 * @coverage core.gef.policy
 */
public class NonResizableSelectionEditPolicy extends SelectionEditPolicy {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Handles
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected List<Handle> createSelectionHandles() {
    List<Handle> handles = Lists.newArrayList();
    handles.add(new MoveHandle(getHost()));
    handles.add(createHandle(IPositionConstants.SOUTH_EAST));
    handles.add(createHandle(IPositionConstants.SOUTH_WEST));
    handles.add(createHandle(IPositionConstants.NORTH_WEST));
    handles.add(createHandle(IPositionConstants.NORTH_EAST));
    return handles;
  }

  /**
   * @return the {@link ResizeHandle} for given direction.
   */
  private Handle createHandle(int direction) {
    ResizeHandle handle = new ResizeHandle(getHost(), direction);
    handle.setDragTrackerTool(new ResizeTracker(direction, null));
    return handle;
  }
}