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
package org.eclipse.wb.tests.designer.XML.palette.ui;

import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.tests.gef.TreeRobot;

import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

public class TreeDragHelper {
  private final Tree m_tree;
  private Event m_lastEvent;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TreeDragHelper(Tree tree) {
    m_tree = tree;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public TreeDragHelper startDrag(TreeItem source) {
    m_tree.setSelection(source);
    Point location = getLocation(source, 0, 0);
    Event event = createDNDEvent(source, location);
    notifyDragSource(DND.DragStart, event);
    notifyDropTarget(DND.DragEnter, event);
    return this;
  }

  public TreeDragHelper dragOn(TreeItem target) {
    m_tree.showItem(target);
    Point location = getLocationOn(target);
    m_lastEvent = createDNDEvent(target, location);
    notifyDropTarget(DND.DragOver, m_lastEvent);
    return this;
  }

  public TreeDragHelper dragBefore(TreeItem target) {
    m_tree.showItem(target);
    Point location = getLocationBefore(target);
    m_lastEvent = createDNDEvent(target, location);
    notifyDropTarget(DND.DragOver, m_lastEvent);
    return this;
  }

  public TreeDragHelper dragAfter(TreeItem target) {
    m_tree.showItem(target);
    Point location = getLocationAfter(target);
    m_lastEvent = createDNDEvent(target, location);
    notifyDropTarget(DND.DragOver, m_lastEvent);
    return this;
  }

  public void endDrag() {
    notifyDropTarget(DND.Drop, m_lastEvent);
  }

  private Event createDNDEvent(TreeItem item, Point locationInTree) {
    Tree tree = item.getParent();
    // create DNDEvent
    Event event = createDNDEvent();
    // configure event
    event.widget = tree;
    event.item = item;
    {
      org.eclipse.swt.graphics.Point absoluteLocation =
          Display.getCurrent().map(tree, null, locationInTree.getSwtPoint());
      event.x = absoluteLocation.x;
      event.y = absoluteLocation.y;
    }
    return event;
  }

  private static Event createDNDEvent() {
    return ExecutionUtils.runObject(new RunnableObjectEx<Event>() {
      @Override
      public Event runObject() throws Exception {
        Class<?> dndClass =
            ReflectionUtils.getClassByName(
                TreeRobot.class.getClassLoader(),
                "org.eclipse.swt.dnd.DNDEvent");
        return (Event) ReflectionUtils.getConstructorBySignature(dndClass, "<init>()").newInstance();
      }
    });
  }

  private void notifyDragSource(int eventType, Event event) {
    DragSource dropTarget = (DragSource) m_tree.getData("DragSource");
    dropTarget.notifyListeners(eventType, event);
  }

  private void notifyDropTarget(int eventType, Event event) {
    DropTarget dropTarget = (DropTarget) m_tree.getData("DropTarget");
    dropTarget.notifyListeners(eventType, event);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Location utils
  //
  ////////////////////////////////////////////////////////////////////////////
  public Point getLocationOn(TreeItem item) {
    Rectangle bounds = getBounds(item);
    int x = bounds.x + 0;
    int y = bounds.y + bounds.height / 2;
    Point location = new Point(x, y);
    return location;
  }

  public Point getLocationBefore(TreeItem item) {
    Rectangle bounds = getBounds(item);
    int x = bounds.x + 0;
    int y = bounds.y + 0;
    Point location = new Point(x, y);
    return location;
  }

  public Point getLocationAfter(TreeItem item) {
    Rectangle bounds = getBounds(item);
    int x = bounds.x + 0;
    int y = bounds.y + bounds.height / 2 + 1;
    Point location = new Point(x, y);
    return location;
  }

  /**
   * @return bounds of given {@link EditPart} in {@link Tree}.
   */
  public static Rectangle getBounds(TreeItem item) {
    return new Rectangle(item.getBounds());
  }

  /**
   * @return location of given {@link EditPart} with offset. Negative offset means offset from
   *         right/bottom side.
   */
  public static Point getLocation(TreeItem item, int deltaX, int deltaY) {
    Rectangle bounds = getBounds(item);
    Point location = new Point(0, 0);
    if (deltaX >= 0) {
      location.x = bounds.x + deltaX;
    } else {
      location.x = bounds.right() + deltaX;
    }
    if (deltaY >= 0) {
      location.y = bounds.y + deltaY;
    } else {
      location.y = bounds.bottom() + deltaY;
    }
    return location;
  }
}
