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
package org.eclipse.wb.gef.tree.policies;

import com.google.common.collect.Lists;

import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.core.requests.CreateRequest;
import org.eclipse.wb.gef.core.requests.IDropRequest;
import org.eclipse.wb.gef.core.requests.PasteRequest;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.tree.TreeEditPart;
import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.gef.tree.TreeViewer;

import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import org.apache.commons.lang.ArrayUtils;

import java.util.List;

/**
 * Implementation of {@link EditPolicy} for {@link TreeViewer} and {@link TreeEditPart}'s, that
 * understands requests {@link Request#REQ_CREATE}, {@link Request#REQ_PASTE},
 * {@link Request#REQ_MOVE} or {@link Request#REQ_ADD} and asks corresponding methods for
 * {@link Command}.
 *
 * @author lobas_av
 * @coverage gef.tree
 */
public abstract class LayoutEditPolicy extends EditPolicy {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the <i>host</i> {@link TreeEditPart} on which this policy is installed.
   */
  @Override
  public TreeEditPart getHost() {
    return (TreeEditPart) super.getHost();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Widget
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Convenience method to return the host's {@link TreeItem}.
   */
  protected final TreeItem getHostWidget() {
    return getHost().getWidget();
  }

  /**
   * Convenience method to return the host's {@link Tree}.
   */
  protected final Tree getTree() {
    return getHostWidget().getParent();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Request/Command
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link ILayoutRequestValidator} for validating {@link Request#REQ_CREATE},
   *         {@link Request#REQ_PASTE}, {@link Request#REQ_MOVE} and {@link Request#REQ_ADD}
   *         requests.
   */
  protected ILayoutRequestValidator getRequestValidator() {
    return ILayoutRequestValidator.TRUE;
  }

  /**
   * @return <code>true</code> if the {@link Request} is an {@link Request#REQ_CREATE},
   *         {@link Request#REQ_PASTE}, {@link Request#REQ_MOVE} or {@link Request#REQ_ADD}.
   */
  protected boolean isRequestCondition(Request request) {
    Object type = request.getType();
    ILayoutRequestValidator validator = getRequestValidator();
    EditPart host = getHost();
    if (type == Request.REQ_CREATE) {
      return validator.validateCreateRequest(host, (CreateRequest) request);
    }
    if (type == Request.REQ_PASTE) {
      return validator.validatePasteRequest(host, (PasteRequest) request);
    }
    if (type == Request.REQ_MOVE) {
      return validator.validateMoveRequest(host, (ChangeBoundsRequest) request);
    }
    if (type == Request.REQ_ADD) {
      return validator.validateAddRequest(host, (ChangeBoundsRequest) request);
    }
    return false;
  }

  @Override
  public boolean understandsRequest(Request request) {
    return isRequestCondition(request);
  }

  /**
   * @return <i>host</i> if the {@link Request} is an {@link Request#REQ_CREATE},
   *         {@link Request#REQ_PASTE}, {@link Request#REQ_MOVE} or {@link Request#REQ_ADD}.
   */
  @Override
  public EditPart getTargetEditPart(Request request) {
    if (isRequestCondition(request)) {
      // if target item is host, then check for before/after locations
      {
        IDropRequest dropRequest = (IDropRequest) request;
        Point location = dropRequest.getLocation();
        TreeItem targetItem = getTree().getItem(location.getSwtPoint());
        if (targetItem == getHostWidget()
            && (isBeforeLocation(targetItem, location) || isAfterLocation(targetItem, location))) {
          return null;
        }
      }
      // OK drop on host
      return getHost();
    }
    return null;
  }

  /**
   * Factors incoming requests into various specific methods.
   */
  @Override
  public Command getCommand(Request request) {
    // prepare drop location
    IDropRequest dropRequest = (IDropRequest) request;
    Point location = dropRequest.getLocation();
    // prepare target item
    TreeItem targetItem = getTree().getItem(location.getSwtPoint());
    if (targetItem == null || targetItem.getData() == null) {
      return null;
    }
    // prepare children
    List<EditPart> children = getReferenceChildren(request);
    // calculate next reference
    Object referenceObject = null;
    if (targetItem == getHostWidget()) {
      // drop to this
    } else {
      // drop to children
      EditPart dropPart = (EditPart) targetItem.getData();
      // prepare index of target part
      int dropIndex = children.indexOf(dropPart);
      if (dropIndex == -1) {
        return null;
      }
      // analyze before/after target
      int nextIndex = dropIndex + 1;
      if (isBeforeLocation(targetItem, location)) {
        referenceObject = dropPart.getModel();
      } else if (nextIndex < children.size()) {
        referenceObject = children.get(nextIndex).getModel();
      }
    }
    // route request
    return getCommand(request, referenceObject);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Reference children
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Checks that given {@link EditPart} can be used as reference child.
   */
  protected boolean isGoodReferenceChild(Request request, EditPart editPart) {
    return false;
  }

  /**
   * @return the {@link List} of {@link EditPart}'s that can be used as references.
   */
  private List<EditPart> getReferenceChildren(Request request) {
    List<EditPart> allChildren = getHost().getChildren();
    List<EditPart> referenceChildren = Lists.newArrayList();
    //
    for (EditPart editPart : allChildren) {
      if (isGoodReferenceChild(request, editPart)) {
        referenceChildren.add(editPart);
      }
    }
    //
    return referenceChildren;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link Command} for given {@link Request} and reference {@link Object}.
   */
  protected Command getCommand(Request request, Object referenceObject) {
    Object type = request.getType();
    if (Request.REQ_CREATE.equals(type)) {
      CreateRequest createRequest = (CreateRequest) request;
      return getCreateCommand(createRequest.getNewObject(), referenceObject);
    }
    if (Request.REQ_PASTE.equals(type)) {
      PasteRequest pasteRequest = (PasteRequest) request;
      return getPasteCommand(pasteRequest, referenceObject);
    }
    if (Request.REQ_MOVE.equals(type)) {
      ChangeBoundsRequest boundsRequest = (ChangeBoundsRequest) request;
      return getMoveCommand(boundsRequest.getEditParts(), referenceObject);
    }
    if (Request.REQ_ADD.equals(type)) {
      ChangeBoundsRequest boundsRequest = (ChangeBoundsRequest) request;
      return getAddCommand(boundsRequest.getEditParts(), referenceObject);
    }
    return null;
  }

  /**
   * @return the {@link Command} for {@link Request#REQ_CREATE}.
   */
  protected Command getCreateCommand(Object newObject, Object referenceObject) {
    return null;
  }

  /**
   * @return the {@link Command} for {@link Request#REQ_PASTE}.
   */
  protected Command getPasteCommand(PasteRequest request, Object referenceObject) {
    return null;
  }

  /**
   * @return the {@link Command} for {@link Request#REQ_MOVE}.
   */
  protected Command getMoveCommand(List<EditPart> moveParts, Object referenceObject) {
    return null;
  }

  /**
   * @return the {@link Command} for {@link Request#REQ_ADD}.
   */
  protected Command getAddCommand(List<EditPart> addParts, Object referenceObject) {
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Feedback
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void showTargetFeedback(Request request) {
    if (isRequestCondition(request)) {
      showLayoutTargetFeedback(request);
    }
  }

  @Override
  public void eraseTargetFeedback(Request request) {
    if (isRequestCondition(request)) {
      eraseLayoutTargetFeedback(request);
    }
  }

  /**
   * Shows target feedback for {@link Request#REQ_ADD}, {@link Request#REQ_MOVE},
   * {@link Request#REQ_CREATE} or {@link Request#REQ_PASTE}.
   */
  protected void showLayoutTargetFeedback(Request request) {
    // prepare drop location
    IDropRequest dropRequest = (IDropRequest) request;
    Point location = dropRequest.getLocation();
    // prepare tree widget's
    Tree tree = getTree();
    TreeItem hostItem = getHostWidget();
    TreeItem targetItem = tree.getItem(location.getSwtPoint());
    //
    if (targetItem == hostItem) {
      // drop to this
      appendToSelection();
      setTreeInsertMark(null, true);
      // support expand for not DND
      Object type = request.getType();
      if ((Request.REQ_CREATE.equals(type) || Request.REQ_PASTE.equals(type))
          && !hostItem.getExpanded()) {
        tree.getShell().getDisplay().asyncExec(new Runnable() {
          public void run() {
            TreeItem hostWidget = getHostWidget();
            if (hostWidget != null) {
              hostWidget.setExpanded(true);
            }
          }
        });
      }
    } else if (targetItem != null && hostItem.indexOf(targetItem) != -1) {
      // drop to children
      removeFromSelection();
      boolean beforeLocation = isBeforeLocation(targetItem, location);
      if (EnvironmentUtils.IS_LINUX) {
        /*
         * Feature in Linux: during DND dragOver() operation the
         * DropTargetEvent.feedback resets all previous tree insert marks.
         */
        request.setDNDFeedback(beforeLocation
            ? DND.FEEDBACK_INSERT_BEFORE
            : DND.FEEDBACK_INSERT_AFTER);
      }
      setTreeInsertMark(targetItem, beforeLocation);
    }
  }

  private void setTreeInsertMark(TreeItem targetItem, boolean before) {
    Tree tree = getTree();
    tree.setInsertMark(targetItem, before);
    // store for tests
    tree.setData("_wbp_insertMarkItem", targetItem);
    tree.setData("_wbp_insertMarkLocation", before);
  }

  /**
   * Erases target feedback for {@link Request#REQ_ADD}, {@link Request#REQ_MOVE},
   * {@link Request#REQ_CREATE} or {@link Request#REQ_PASTE}.
   */
  protected void eraseLayoutTargetFeedback(Request request) {
    removeFromSelection();
    setTreeInsertMark(null, true);
  }

  /**
   * @return <code>true</code> if given location should be considered as location <em>before</em>
   *         {@link TreeItem}.
   */
  private static boolean isBeforeLocation(TreeItem item, Point location) {
    Rectangle bounds = new Rectangle(item.getBounds());
    return location.y - bounds.y < 5;
  }

  /**
   * @return <code>true</code> if given location should be considered as location <em>after</em>
   *         {@link TreeItem}.
   */
  private static boolean isAfterLocation(TreeItem item, Point location) {
    Rectangle bounds = new Rectangle(item.getBounds());
    return bounds.bottom() - location.y < 5;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Selection in Tree utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Add host widget to tree selection.
   */
  private void appendToSelection() {
    TreeItem widget = getHostWidget();
    Tree tree = getTree();
    TreeItem[] selection = tree.getSelection();
    if (!ArrayUtils.contains(selection, widget)) {
      selection = (TreeItem[]) ArrayUtils.add(selection, widget);
      tree.setSelection(selection);
    }
  }

  /**
   * Remove host widget from tree selection.
   */
  private void removeFromSelection() {
    TreeItem widget = getHostWidget();
    Tree tree = getTree();
    TreeItem[] selection = tree.getSelection();
    if (ArrayUtils.contains(selection, widget)) {
      selection = (TreeItem[]) ArrayUtils.removeElement(selection, widget);
      tree.setSelection(selection);
    }
  }
}