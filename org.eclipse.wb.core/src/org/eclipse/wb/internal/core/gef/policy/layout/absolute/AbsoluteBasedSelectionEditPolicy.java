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
package org.eclipse.wb.internal.core.gef.policy.layout.absolute;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.draw2d.IPositionConstants;
import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.graphical.handles.Handle;
import org.eclipse.wb.gef.graphical.handles.MoveHandle;
import org.eclipse.wb.gef.graphical.handles.ResizeHandle;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;
import org.eclipse.wb.gef.graphical.tools.ResizeTracker;
import org.eclipse.wb.internal.core.gef.policy.snapping.PlacementsSupport;

import java.util.List;

/**
 * Generic {@link SelectionEditPolicy} for absolute based layouts.
 *
 * @author mitin_aa
 * @coverage core.gef.policy
 */
public abstract class AbsoluteBasedSelectionEditPolicy<C extends IAbstractComponentInfo>
    extends
      SelectionEditPolicy {
  // constants
  public static final String REQ_RESIZE = "_absolute_resize";

  ////////////////////////////////////////////////////////////////////////////
  //
  // Handles
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected List<Handle> createSelectionHandles() {
    List<Handle> handles = Lists.newArrayList();
    MoveHandle moveHandle = new MoveHandle(getHost());
    handles.add(moveHandle);
    handles.add(createResizeHandle(IPositionConstants.NORTH));
    handles.add(createResizeHandle(IPositionConstants.SOUTH));
    handles.add(createResizeHandle(IPositionConstants.WEST));
    handles.add(createResizeHandle(IPositionConstants.EAST));
    handles.add(createResizeHandle(IPositionConstants.SOUTH_EAST));
    handles.add(createResizeHandle(IPositionConstants.SOUTH_WEST));
    handles.add(createResizeHandle(IPositionConstants.NORTH_WEST));
    handles.add(createResizeHandle(IPositionConstants.NORTH_EAST));
    return handles;
  }

  /**
   * @return the {@link ResizeHandle} for given direction.
   */
  private Handle createResizeHandle(int direction) {
    ResizeHandle handle = new ResizeHandle(getHost(), direction);
    handle.setDragTrackerTool(new ResizeTracker(direction, REQ_RESIZE) {
      @Override
      protected Command getCommand() {
        return getLayoutEditPolicy().getResizeCommandImpl(getRequest());
      }

      @Override
      protected void showSourceFeedback() {
        getLayoutEditPolicy().showResizeFeedback(getRequest());
        setShowingFeedback(true);
      }

      @Override
      protected void eraseSourceFeedback() {
        if (isShowingFeedback()) {
          setShowingFeedback(false);
          getLayoutEditPolicy().eraseResizeFeedback(getRequest());
        }
      }
    });
    return handle;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Routing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean understandsRequest(Request request) {
    return super.understandsRequest(request) || request.getType() == REQ_RESIZE;
  }

  @Override
  public void performRequest(Request request) {
    getLayoutEditPolicy().performRequest(request);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @SuppressWarnings("unchecked")
  protected final AbsoluteBasedLayoutEditPolicy<C> getLayoutEditPolicy() {
    return (AbsoluteBasedLayoutEditPolicy<C>) getHost().getParent().getEditPolicy(
        EditPolicy.LAYOUT_ROLE);
  }

  protected final PlacementsSupport getPlacementsSupport() {
    return getLayoutEditPolicy().getPlacementsSupport();
  }
}