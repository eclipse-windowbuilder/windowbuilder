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
package org.eclipse.wb.gef.core.tools;

import com.google.common.collect.Lists;

import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.core.IEditPartViewer.IConditional;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.core.requests.DragPermissionRequest;
import org.eclipse.wb.gef.core.requests.GroupRequest;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.internal.gef.core.CompoundCommand;
import org.eclipse.wb.internal.gef.core.ISharedCursors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A drag tracker that moves {@link EditPart EditParts}.
 *
 * @author lobas_av
 * @coverage gef.core
 */
public class DragEditPartTracker extends SelectEditPartTracker {
  private boolean m_canMove;
  private boolean m_canReparent;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DragEditPartTracker(EditPart sourceEditPart) {
    super(sourceEditPart);
    setDefaultCursor(ISharedCursors.CURSOR_MOVE);
    setDisabledCursor(ISharedCursors.CURSOR_NO);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tool
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void deactivate() {
    super.deactivate();
    m_exclusionSet = null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // High-Level handle MouseEvent
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void handleButtonUp(int button) {
    if (m_state == STATE_DRAG_IN_PROGRESS) {
      // prepare models if restoring selection
      List<Object> models = getOperationSetModels();
      eraseTargetFeedback();
      executeCommand();
      m_state = STATE_NONE;
      // restore selection
      restoreSelectionFromModels(models);
    } else {
      super.handleButtonUp(button);
    }
  }

  @Override
  protected void handleDragStarted() {
    super.handleDragStarted();
    if (m_state == STATE_DRAG_IN_PROGRESS) {
      updateTargetRequest();
      updateTargetUnderMouse();
    }
  }

  @Override
  protected void handleDragInProgress() {
    if (m_state == STATE_DRAG_IN_PROGRESS) {
      updateTargetRequest();
      updateTargetUnderMouse();
      showTargetFeedback();
      updateCommand();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Handling operations
  //
  ////////////////////////////////////////////////////////////////////////////
  private Collection<EditPart> m_exclusionSet;

  /**
   * Returns a list of all the edit parts in the {@link Tool#getOperationSet() operation set}.
   */
  @Override
  protected Collection<EditPart> getExclusionSet() {
    if (m_exclusionSet == null) {
      m_exclusionSet = new ArrayList<EditPart>(getOperationSet());
    }
    return m_exclusionSet;
  }

  /**
   * Returns "all ignore" {@link IConditional} if <code>OperationSet</code> is empty.
   */
  @Override
  protected IConditional getTargetingConditional() {
    if (!getViewer().getSelectedEditParts().isEmpty() && getOperationSet().isEmpty()) {
      return new IConditional() {
        public boolean evaluate(EditPart editPart) {
          return false;
        }
      };
    }
    return super.getTargetingConditional();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // OperationSet
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Returns a {@link List} of top-level edit parts excluding dependants that understand the current
   * target request.
   */
  @Override
  protected List<EditPart> createOperationSet() {
    // extract OperationSet from selection
    List<EditPart> operationSet = ToolUtilities.getSelectionWithoutDependants(getViewer());
    // check understandsRequest() from parent
    // FIXME Kosta.20071115 I don't understand, why we should ask parent _here_
    // Yes, if parent does not support move, we should not allow operation.
    // However even if parent does not support move, we still can _reparent_ from it on different parent.
    {
      // XXX just to set "operationSet", probably there is better place
      ChangeBoundsRequest request = (ChangeBoundsRequest) getTargetRequest();
      request.setEditParts(operationSet);
    }
    /*if (!operationSet.isEmpty()) {
    	EditPart firstPart = operationSet.get(0);
    	ChangeBoundsRequest request = (ChangeBoundsRequest) getTargetRequest();
    	request.setEditParts(operationSet);
    	//
    	if (!firstPart.getParent().understandsRequest(request)) {
    		return Collections.emptyList();
    	}
    }*/
    // check permission for move and reparenting
    {
      DragPermissionRequest request = new DragPermissionRequest();
      for (EditPart editPart : operationSet) {
        editPart.performRequest(request);
      }
      m_canMove = request.canMove();
      m_canReparent = request.canReparent();
    }
    //
    return operationSet;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Request
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Creates a {@link ChangeBoundsRequest}. The type is {@link Request#REQ_MOVE}.
   */
  @Override
  protected Request createTargetRequest() {
    return new ChangeBoundsRequest(Request.REQ_MOVE);
  }

  /**
   * Updates the request with the current {@link Tool#getOperationSet() operation set}, move delta,
   * location and type.
   */
  @Override
  protected void updateTargetRequest() {
    super.updateTargetRequest();
    ChangeBoundsRequest request = (ChangeBoundsRequest) getTargetRequest();
    request.setEditParts(getOperationSet());
    request.setMoveDelta(new Point(getDragMoveDelta()));
    request.setLocation(getLocation());
  }

  @Override
  protected void updateTargetRequest(EditPart target) {
    super.updateTargetRequest(target);
    ChangeBoundsRequest request = (ChangeBoundsRequest) getTargetRequest();
    List<EditPart> editParts = request.getEditParts();
    if (!editParts.isEmpty()) {
      if (editParts.get(0).getParent() == target) {
        request.setType(Request.REQ_MOVE);
      } else {
        request.setType(Request.REQ_ADD);
      }
    }
  }

  /**
   * @return <code>true</code> if current {@link #getTargetRequest()} has {@link Request#REQ_MOVE}
   *         type.
   */
  private boolean isMove() {
    return getTargetRequest().getType() == Request.REQ_MOVE;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Command
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Return collection model's of operation set.
   */
  private List<Object> getOperationSetModels() {
    List<Object> models = Lists.newArrayList();
    for (EditPart part : getOperationSet()) {
      models.add(part.getModel());
    }
    return models;
  }

  /**
   * Select {@link EditPart} by their model's.
   */
  private void restoreSelectionFromModels(List<Object> models) {
    if (models != null) {
      IEditPartViewer viewer = getViewer();
      // prepare new EditPart's
      List<EditPart> newEditParts = Lists.newArrayList();
      for (Object model : models) {
        EditPart newEditPart = viewer.getEditPartByModel(model);
        if (newEditPart != null) {
          newEditParts.add(newEditPart);
        }
      }
      // set new selection
      viewer.setSelection(newEditParts);
    }
  }

  @Override
  protected Command getCommand() {
    Request request = getTargetRequest();
    List<EditPart> operationSet = getOperationSet();
    //
    if (isMove()) {
      if (m_canMove && !operationSet.isEmpty()) {
        // if move get command from parent
        EditPart firstPart = operationSet.get(0);
        //
        return firstPart.getParent().getCommand(request);
      }
    } else if (m_canReparent) {
      // if change parent get command from new parent and notify old parent for orphans
      EditPart targetEditPart = getTargetEditPart();
      //
      if (targetEditPart != null) {
        CompoundCommand compoundCommand = targetEditPart.createCompoundCommand();
        if (!operationSet.isEmpty()) {
          EditPart firstPart = operationSet.get(0);
          GroupRequest orphanRequest = new GroupRequest(Request.REQ_ORPHAN);
          orphanRequest.setEditParts(operationSet);
          compoundCommand.add(firstPart.getParent().getCommand(orphanRequest));
        }
        Command reparentCommand = targetEditPart.getCommand(request);
        if (reparentCommand != null) {
          compoundCommand.add(reparentCommand);
          return compoundCommand;
        }
      }
    }
    return null;
  }
}