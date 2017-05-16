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
package org.eclipse.wb.core.gef.policy.layout.flow;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.gef.policy.PolicyUtils;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.draw2d.IColorConstants;
import org.eclipse.wb.draw2d.Polyline;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.draw2d.geometry.Transposer;
import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.requests.AbstractCreateRequest;
import org.eclipse.wb.gef.core.requests.ChangeBoundsRequest;
import org.eclipse.wb.gef.core.requests.CreateRequest;
import org.eclipse.wb.gef.core.requests.IDropRequest;
import org.eclipse.wb.gef.core.requests.PasteRequest;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.core.utils.GenericsUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Implementation of {@link LayoutEditPolicy} for for "flow based" layouts, i.e. layouts where each
 * {@link EditPart} located in sequence one after other, in columns or rows, with possible wrapping.
 *
 * @author scheglov_ke
 * @coverage core.gef.policy
 */
public abstract class AbstractFlowLayoutEditPolicy extends LayoutEditPolicy {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Configuration
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if children are located horizontally, i.e. in columns.
   */
  protected abstract boolean isHorizontal(Request request);

  /**
   * @return <code>true</code> if container has RTL orientation, i.e. children are added right to
   *         left.
   */
  protected boolean isRtl(Request request) {
    return false;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Reference children
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Checks that given {@link EditPart} can be used as reference child.
   *
   * @param request
   *          the {@link Request} to check, may be <code>null</code>.
   * @param editPart
   *          the {@link EditPart}, child of host {@link EditPart}.
   */
  protected abstract boolean isGoodReferenceChild(Request request, EditPart editPart);

  /**
   * @return the {@link List} of {@link EditPart}'s that can be used as references.
   */
  private List<EditPart> getReferenceChildren(Request request) {
    List<EditPart> allChildren = getHost().getChildren();
    ArrayList<EditPart> referenceChildren = Lists.newArrayList();
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
  @Override
  public Command getCommand(Request request) {
    Command command = super.getCommand(request);
    if (command == null) {
      command = getCommand(request, getReferenceObject(request));
    }
    return command;
  }

  /**
   * @return the {@link Command} for generic {@link Request}, if {@link #getCommand(Request)} was
   *         not able to find other {@link Command}.
   */
  protected Command getCommand(Request request, Object referenceObject) {
    return null;
  }

  /**
   * @return the model object that should be used as reference in command.
   */
  private Object getReferenceObject(Request request) {
    EditPart reference = getInsertionReference(request);
    return reference != null ? reference.getModel() : null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands: create
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected final Command getCreateCommand(CreateRequest request) {
    return getCreateCommand(request.getNewObject(), getReferenceObject(request));
  }

  /**
   * @return the {@link Command} for {@link Request#REQ_CREATE}.
   */
  protected Command getCreateCommand(Object newObject, Object referenceObject) {
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands: paste
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected final Command getPasteCommand(PasteRequest request) {
    return getPasteCommand(request, getReferenceObject(request));
  }

  /**
   * @return the {@link Command} for {@link Request#REQ_PASTE}.
   */
  protected Command getPasteCommand(PasteRequest request, Object referenceObject) {
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands: move
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected final Command getMoveCommand(ChangeBoundsRequest request) {
    if (request.getEditParts().size() != 1) {
      return null;
    }
    EditPart moveEditPart = request.getEditParts().get(0);
    // checks for no-op
    {
      EditPart referenceEditPart = getInsertionReference(request);
      List<EditPart> children = getReferenceChildren(request);
      if (children.contains(moveEditPart)) {
        // move of last to last
        if (referenceEditPart == null && children.indexOf(moveEditPart) == children.size() - 1) {
          return Command.EMPTY;
        }
        // move before already next
        if (children.indexOf(moveEditPart) + 1 == children.indexOf(referenceEditPart)) {
          return Command.EMPTY;
        }
      }
    }
    // OK, now we can create command
    return getMoveCommand(moveEditPart.getModel(), getReferenceObject(request));
  }

  /**
   * @return the {@link Command} for {@link Request#REQ_MOVE}.
   */
  protected abstract Command getMoveCommand(Object moveObject, Object referenceObject);

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands: add
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected final Command getAddCommand(ChangeBoundsRequest request) {
    if (request.getEditParts().size() != 1) {
      return null;
    }
    EditPart editPart = request.getEditParts().get(0);
    return getAddCommand(editPart.getModel(), getReferenceObject(request));
  }

  /**
   * @return the {@link Command} for {@link Request#REQ_ADD}.
   */
  protected Command getAddCommand(Object addObject, Object referenceObject) {
    return getMoveCommand(addObject, referenceObject);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Feedbacks
  //
  ////////////////////////////////////////////////////////////////////////////
  private Polyline m_insertionLine;
  private EditPart m_reference = null;
  private boolean m_beforeReference = true;

  @Override
  protected void showLayoutTargetFeedback(Request request) {
    final boolean horizontal = isHorizontal(request);
    final boolean rtl = isRtl(request);
    // prepare children
    List<EditPart> children = getReferenceChildren(request);
    if (children.isEmpty()) {
      m_reference = null;
      showLayoutTargetFeedback_noReference(horizontal, rtl);
      return;
    }
    // prepare transposer
    final Transposer transposer = new Transposer();
    transposer.setEnabled(!horizontal);
    Point p = transposer.t(getLocationFromRequest(request));
    // prepare children that belong same row
    List<EditPart> rowChildren = Lists.newArrayList();
    {
      // find child nearest to cursor
      EditPart nearestEditPart = null;
      {
        int minDistance = Integer.MAX_VALUE;
        for (EditPart child : children) {
          Rectangle childBounds = getAbsoluteBounds(horizontal, child);
          // prepare distance
          int distance;
          if (p.y < childBounds.y) {
            distance = childBounds.y - p.y;
          } else if (p.y > childBounds.bottom()) {
            distance = p.y - childBounds.bottom();
          } else {
            distance = 0;
          }
          // check for better child
          if (distance < minDistance) {
            nearestEditPart = child;
            minDistance = distance;
          }
        }
      }
      // add children that intersect any child that is already in row
      rowChildren.add(nearestEditPart);
      Rectangle nearestBounds = getAbsoluteBounds(horizontal, nearestEditPart);
      int rowMinY = nearestBounds.y;
      int rowMaxY = nearestBounds.bottom();
      while (true) {
        int rowChildrenCount = rowChildren.size();
        //
        for (EditPart child : children) {
          if (!rowChildren.contains(child)) {
            Rectangle childBounds = getAbsoluteBounds(horizontal, child);
            if (intervalsIntersects(rowMinY, rowMaxY, childBounds.y, childBounds.bottom())) {
              rowChildren.add(child);
              rowMinY = Math.min(rowMinY, childBounds.y);
              rowMaxY = Math.max(rowMaxY, childBounds.bottom());
            }
          }
        }
        // stop if we can not add more children to row
        if (rowChildren.size() == rowChildrenCount) {
          break;
        }
      }
    }
    // sort row by X
    Collections.sort(rowChildren, new Comparator<EditPart>() {
      public int compare(EditPart part_1, EditPart part_2) {
        int x1 = getAbsoluteBounds(horizontal, part_1).x;
        int x2 = getAbsoluteBounds(horizontal, part_2).x;
        if (horizontal && rtl) {
          return x2 - x1;
        } else {
          return x1 - x2;
        }
      }
    });
    // find reference
    m_reference = null;
    m_beforeReference = true;
    if (!rowChildren.isEmpty()) {
      for (EditPart child : rowChildren) {
        Rectangle bounds = getAbsoluteBounds(horizontal, child);
        boolean isReference;
        if (horizontal && rtl) {
          isReference = p.x > bounds.getCenter().x;
        } else {
          isReference = p.x < bounds.getCenter().x;
        }
        if (isReference) {
          m_reference = child;
          break;
        }
      }
      // no reference, so use "after last"
      if (m_reference == null) {
        m_reference = rowChildren.get(rowChildren.size() - 1);
        m_beforeReference = false;
      }
    }
    //
    if (m_reference != null) {
      Rectangle bounds = getAbsoluteBounds(horizontal, m_reference);
      // prepare X for line
      int x;
      if (m_beforeReference) {
        int referenceIndex = rowChildren.indexOf(m_reference);
        if (referenceIndex != 0) {
          EditPart prevReference = rowChildren.get(referenceIndex - 1);
          Rectangle prevBounds = getAbsoluteBounds(horizontal, prevReference);
          if (horizontal && rtl) {
            x = bounds.right() + Math.min(3, (prevBounds.left() - bounds.right()) / 2);
          } else {
            x = bounds.left() - Math.min(3, (bounds.left() - prevBounds.right()) / 2);
          }
        } else {
          if (horizontal && rtl) {
            x = bounds.right() + 3;
          } else {
            x = bounds.left() - 3;
          }
        }
      } else {
        if (horizontal && rtl) {
          x = bounds.left() - 3;
        } else {
          x = bounds.right() + 3;
        }
      }
      // add line
      {
        Polyline feedbackLine = getLineFeedback();
        //
        Point p1 = new Point(x, bounds.y - 4);
        p1 = transposer.t(p1);
        FigureUtils.translateAbsoluteToFigure(feedbackLine, p1);
        //
        Point p2 = new Point(x, bounds.y + bounds.height + 4);
        p2 = transposer.t(p2);
        FigureUtils.translateAbsoluteToFigure(feedbackLine, p2);
        //
        feedbackLine.setPoint(p1, 0);
        feedbackLine.setPoint(p2, 1);
      }
    }
  }

  private void showLayoutTargetFeedback_noReference(boolean horizontal, boolean rtl) {
    Polyline feedbackLine = getLineFeedback();
    Figure hostFigure = getHostFigure();
    Rectangle bounds = hostFigure.getBounds().getCopy();
    FigureUtils.translateFigureToAbsolute(hostFigure, bounds);
    // prepare points
    Point p1;
    Point p2;
    if (horizontal) {
      if (rtl) {
        p1 = new Point(bounds.right(), bounds.top());
        p2 = new Point(bounds.right(), bounds.bottom());
      } else {
        p1 = new Point(bounds.left(), bounds.top());
        p2 = new Point(bounds.left(), bounds.bottom());
      }
    } else {
      p1 = new Point(bounds.left(), bounds.top());
      p2 = new Point(bounds.right(), bounds.top());
    }
    // if host is big enough, tweak points for better look
    if (horizontal) {
      if (bounds.width > 20) {
        if (rtl) {
          p1.x -= 5;
          p2.x -= 5;
        } else {
          p1.x += 5;
          p2.x += 5;
        }
      }
      if (bounds.height > 20) {
        p1.y += 5;
        p2.y -= 5;
      }
    } else {
      if (bounds.width > 20) {
        p1.x += 5;
        p2.x -= 5;
      }
      if (bounds.height > 20) {
        p1.y += 5;
        p2.y += 5;
      }
    }
    // set points
    feedbackLine.setPoint(p1, 0);
    feedbackLine.setPoint(p2, 1);
  }

  @Override
  protected void eraseLayoutTargetFeedback(Request request) {
    if (m_insertionLine != null) {
      removeFeedback(m_insertionLine);
      m_insertionLine = null;
    }
  }

  /**
   * @return the {@link Polyline} that should be used as feedback.
   */
  private Polyline getLineFeedback() {
    if (m_insertionLine == null) {
      m_insertionLine = new Polyline();
      // presentation
      m_insertionLine.setLineWidth(2);
      m_insertionLine.setForeground(IColorConstants.red);
      // default points
      m_insertionLine.addPoint(new Point(0, 0));
      m_insertionLine.addPoint(new Point(0, 0));
      // add
      addFeedback(m_insertionLine);
    }
    return m_insertionLine;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Reference
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link EditPart} <em>before</em> which given {@link Request} should be handled.
   */
  private EditPart getInsertionReference(Request request) {
    List<EditPart> children = getReferenceChildren(request);
    if (m_reference == null) {
      return null;
    }
    // prepare reference in terms "next"
    EditPart reference;
    if (m_beforeReference) {
      reference = m_reference;
    } else {
      reference = GenericsUtils.getNextOrNull(children, m_reference);
    }
    // skip moving children
    if (request instanceof AbstractCreateRequest) {
      return reference;
    } else {
      List<EditPart> selectedEditParts = getHost().getViewer().getSelectedEditParts();
      int index = children.indexOf(reference);
      while (selectedEditParts.contains(reference)) {
        if (index == children.size() - 1) {
          return null;
        }
        reference = children.get(++index);
      }
    }
    // return reference
    return reference;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Checks two intervals for intersection.
   */
  private static boolean intervalsIntersects(int x11, int x12, int x21, int x22) {
    int x1 = Math.max(x11, x21);
    int x2 = Math.min(x12, x22);
    return x2 - x1 > 0;
  }

  /**
   * @return absolute mouse location from given {@link Request}.
   */
  private static Point getLocationFromRequest(Request request) {
    return ((IDropRequest) request).getLocation();
  }

  /**
   * @return absolute bounds of given {@link EditPart}'s {@link Figure}, transposed if needed by
   *         layout.
   */
  private static Rectangle getAbsoluteBounds(boolean horizontal, EditPart editPart) {
    Rectangle bounds = PolicyUtils.getAbsoluteBounds((GraphicalEditPart) editPart);
    if (!horizontal) {
      bounds.transpose();
    }
    return bounds;
  }
}
