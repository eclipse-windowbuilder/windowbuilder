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
package org.eclipse.wb.tests.gef;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.gef.graphical.tools.SelectionTool;
import org.eclipse.wb.gef.tree.TreeEditPart;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.ui.UiUtils;
import org.eclipse.wb.internal.gef.tree.TreeViewer;

import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.description.Description;

import java.util.Collections;
import java.util.List;

/**
 * {@link EventSender} for {@link TreeViewer}.
 * 
 * @author scheglov_ke
 */
public final class TreeRobot {
  private final TreeViewer m_viewer;
  private final EventSender m_sender;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TreeRobot(TreeViewer treeViewer) {
    m_viewer = treeViewer;
    m_sender = new EventSender(treeViewer.getControl());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tools
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Cancels current operation by loading default {@link Tool}.
   */
  public void cancel() {
    m_viewer.getEditDomain().loadDefaultTool();
  }

  /**
   * Collapses all items.
   */
  public TreeRobot collapseAll() {
    m_viewer.collapseAll();
    return this;
  }

  /**
   * Expands all items.
   */
  public TreeRobot expandAll() {
    m_viewer.expandAll();
    return this;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Click
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Emulates mouse click using button <code>1</code> in last location.
   */
  public void click() {
    m_sender.click();
  }

  /**
   * Emulate mouse click on given {@link EditPart}.
   */
  public void click(Object object, int deltaX, int deltaY) {
    Point location = getLocation(getEditPart(object), deltaX, deltaY);
    m_sender.click(location.x, location.y, 1);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Drag
  //
  ////////////////////////////////////////////////////////////////////////////
  private boolean m_dragInProgress;

  public TreeRobot startDrag(Object... models) {
    m_viewer.expandAll();
    // select EditPart's to drag
    assertThat(models).isNotEmpty();
    select(models);
    // drag using first EditPart
    {
      TreeEditPart selectedEditPart = m_justSelectedEditParts[0];
      Point location = getLocation(selectedEditPart, 0, 0);
      Event event = createDNDEvent(selectedEditPart, location);
      notifyDropTarget(DND.DragEnter, event);
    }
    // remember state
    m_dragInProgress = true;
    // allow chaining
    return this;
  }

  public TreeRobot dragOn(Object model) {
    TreeEditPart target = getEditPart(model);
    Point location = getLocationOn(target);
    Event event = createDNDEvent(target, location);
    notifyDropTarget(DND.DragOver, event);
    return this;
  }

  public TreeRobot dragBefore(Object model) {
    TreeEditPart target = getEditPart(model);
    Point location = getLocationBefore(target);
    Event event = createDNDEvent(target, location);
    notifyDropTarget(DND.DragOver, event);
    return this;
  }

  public TreeRobot dragAfter(Object model) {
    TreeEditPart target = getEditPart(model);
    Point location = getLocationAfter(target);
    Event event = createDNDEvent(target, location);
    notifyDropTarget(DND.DragOver, event);
    return this;
  }

  public TreeRobot endDrag() {
    Event event = createDNDEvent();
    notifyDropTarget(DND.Drop, event);
    m_dragInProgress = false;
    return this;
  }

  /**
   * @return the currently active command in {@link TreeViewer} drag listener.
   */
  private Command getDragCommand() {
    return ExecutionUtils.runObjectLog(new RunnableObjectEx<Command>() {
      public Command runObject() throws Exception {
        Object eventManager = ReflectionUtils.getFieldObject(m_viewer, "m_eventManager");
        Object dropListener = ReflectionUtils.getFieldObject(eventManager, "m_dropListener");
        return (Command) ReflectionUtils.getFieldObject(dropListener, "m_command");
      }
    }, null);
  }

  private Event createDNDEvent(TreeEditPart dragPart, Point locationInTree) {
    Tree tree = dragPart.getWidget().getParent();
    // create DNDEvent
    Event event = createDNDEvent();
    // configure event
    event.widget = tree;
    event.item = dragPart.getWidget();
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
      public Event runObject() throws Exception {
        Class<?> dndClass =
            ReflectionUtils.getClassByName(
                TreeRobot.class.getClassLoader(),
                "org.eclipse.swt.dnd.DNDEvent");
        return (Event) ReflectionUtils.getConstructorBySignature(dndClass, "<init>()").newInstance();
      }
    });
  }

  private void notifyDropTarget(int eventType, Event event) {
    DropTarget dropTarget = (DropTarget) m_viewer.getTree().getData("DropTarget");
    dropTarget.notifyListeners(eventType, event);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Selection/visibility
  //
  ////////////////////////////////////////////////////////////////////////////
  private TreeEditPart[] m_justSelectedEditParts;

  public TreeRobot select(Object... models) {
    TreeEditPart[] editParts = getEditParts(models);
    m_justSelectedEditParts = editParts;
    m_viewer.setSelection(ImmutableList.<EditPart>copyOf(editParts));
    return this;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Emulation: moveTo
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Mouse move to given location.
   */
  public TreeRobot moveTo(int x, int y) {
    m_sender.moveTo(x, y);
    return this;
  }

  /**
   * Moves mouse to "ON" location for given target.
   */
  public TreeRobot moveOn(Object model) {
    TreeEditPart editPart = getEditPart(model);
    setExpanded(editPart, true);
    Point location = getLocationOn(editPart);
    moveTo(location.x, location.y);
    return this;
  }

  /**
   * Moves mouse to "BEFORE" location for given {@link EditPart}.
   */
  public TreeRobot moveBefore(Object model) {
    TreeEditPart editPart = getEditPart(model);
    setExpanded(editPart, true);
    Point location = getLocationBefore(editPart);
    moveTo(location.x, location.y);
    return this;
  }

  /**
   * Moves mouse to "AFTER" location for given {@link EditPart}.
   */
  public TreeRobot moveAfter(Object model) {
    TreeEditPart editPart = getEditPart(model);
    setExpanded(editPart, true);
    Point location = getLocationAfter(editPart);
    moveTo(location.x, location.y);
    return this;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  public TreeEditPart getEditPart(Object model) {
    TreeEditPart editPart = getEditPartNull(model);
    assertThat(editPart).isNotNull();
    return editPart;
  }

  public TreeEditPart getEditPartNull(Object object) {
    TreeEditPart editPart =
        object instanceof TreeEditPart
            ? (TreeEditPart) object
            : (TreeEditPart) m_viewer.getEditPartByModel(object);
    return editPart;
  }

  public TreeEditPart[] getEditParts(Object[] models) {
    TreeEditPart[] editParts = new TreeEditPart[models.length];
    for (int i = 0; i < models.length; i++) {
      Object model = models[i];
      editParts[i] = getEditPart(model);
    }
    return editParts;
  }

  /**
   * Expands items flow to specified in underlying {@link Tree}.
   */
  public void setExpanded(TreeEditPart editPart, boolean expanded) {
    TreeEditPart parentEditPart = (TreeEditPart) editPart.getParent();
    if (parentEditPart != null) {
      TreeItem widget = parentEditPart.getWidget();
      if (expanded) {
        setExpanded(parentEditPart, expanded);
        if (widget != null) {
          widget.setExpanded(expanded);
        }
      } else {
        if (widget != null) {
          widget.setExpanded(expanded);
        }
        setExpanded(parentEditPart, expanded);
      }
    }
  }

  public void setExpanded(Object model, boolean expanded) {
    setExpanded(getEditPart(model), expanded);
  }

  /**
   * @return the current {@link Command} from currently loaded {@link Tool}.
   */
  public Command getCommand() throws Exception {
    Tool tool = m_viewer.getEditDomain().getActiveTool();
    // if drag
    if (m_dragInProgress) {
      return getDragCommand();
    }
    // when drag is in progress, ask command from "drag tracker"
    if (tool instanceof SelectionTool) {
      Tool dragTracker = (Tool) ReflectionUtils.getFieldObject(tool, "m_dragTracker");
      if (dragTracker != null) {
        tool = dragTracker;
      }
    }
    // OK, get Command from active tool
    return (Command) ReflectionUtils.getFieldObject(tool, "m_command");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Location utils
  //
  ////////////////////////////////////////////////////////////////////////////
  public Point getLocationOn(Object object) {
    Rectangle bounds = getBounds(getEditPart(object));
    int x = bounds.x + 0;
    int y = bounds.y + bounds.height / 2;
    Point location = new Point(x, y);
    return location;
  }

  public Point getLocationBefore(Object object) {
    Rectangle bounds = getBounds(getEditPart(object));
    int x = bounds.x + 0;
    int y = bounds.y + 4;
    Point location = new Point(x, y);
    return location;
  }

  public Point getLocationAfter(Object object) {
    Rectangle bounds = getBounds(getEditPart(object));
    int x = bounds.x + 0;
    int y = bounds.bottom() - 4;
    Point location = new Point(x, y);
    return location;
  }

  /**
   * @return bounds of given {@link EditPart} in {@link Tree}.
   */
  public static Rectangle getBounds(TreeEditPart editPart) {
    TreeItem widget = editPart.getWidget();
    return new Rectangle(widget.getBounds());
  }

  /**
   * @return location of given {@link EditPart} with offset. Negative offset means offset from
   *         right/bottom side.
   */
  public static Point getLocation(TreeEditPart editPart, int deltaX, int deltaY) {
    Rectangle bounds = getBounds(editPart);
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

  ////////////////////////////////////////////////////////////////////////////
  //
  // Selection
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Asserts that selection is empty, i.e. no {@link EditPart} selected.
   */
  public TreeRobot assertSelectedEmpty() {
    List<EditPart> selectedEditParts = m_viewer.getSelectedEditParts();
    assertThat(selectedEditParts).isEmpty();
    return this;
  }

  /**
   * Asserts that given object is primary selected.
   */
  public TreeRobot assertPrimarySelected(Object object) {
    TreeEditPart editPart = getEditPart(object);
    assertThat(editPart.getSelected()).isEqualTo(EditPart.SELECTED_PRIMARY);
    return this;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Expanded
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Asserts that given objects are not expanded.
   */
  public void assertNotExpandedObjects(ObjectInfo... objects) {
    Object[] expanded = getExpandedElements();
    assertThat(expanded).doesNotContain((Object[]) objects);
  }

  /**
   * Asserts that only given objects are expanded.
   */
  public void assertExpandedObjects(ObjectInfo... objects) {
    Object[] expanded = getExpandedElements();
    assertThat(expanded).containsOnly((Object[]) objects);
  }

  /**
   * @return models of expanded {@link EditPart}s.
   */
  private Object[] getExpandedElements() {
    TreeItem[] expandedItems = UiUtils.getExpanded(m_viewer.getTree());
    // prepare models
    Object[] models = new Object[expandedItems.length];
    for (int i = 0; i < expandedItems.length; i++) {
      TreeItem treeItem = expandedItems[i];
      EditPart editPart = (EditPart) treeItem.getData();
      models[i] = editPart.getModel();
    }
    //
    return models;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // TreeViewer feedbacks
  //
  ////////////////////////////////////////////////////////////////////////////
  public TreeRobot assertFeedback_empty() {
    // no selection
    {
      List<TreeItem> selectedItems = getFeedbackSelection();
      assertThat(selectedItems).isEmpty();
    }
    // no insert
    {
      Tree tree = (Tree) m_viewer.getControl();
      Object item = tree.getData("_wbp_insertMarkItem");
      assertThat(item).isNull();
    }
    return this;
  }

  public TreeRobot assertFeedback_on(Object object) {
    TreeEditPart editPart = getEditPart(object);
    List<TreeItem> selectedItems = getFeedbackSelection();
    assertThat(selectedItems).containsOnly(editPart.getWidget());
    return this;
  }

  public TreeRobot assertFeedback_notOn(Object object) {
    TreeEditPart editPart = getEditPart(object);
    List<TreeItem> selectedItems = getFeedbackSelection();
    assertThat(selectedItems).doesNotContain(editPart.getWidget());
    return this;
  }

  private List<TreeItem> getFeedbackSelection() {
    Tree tree = (Tree) m_viewer.getControl();
    List<TreeItem> selectedItems = Lists.newArrayList();
    Collections.addAll(selectedItems, tree.getSelection());
    for (EditPart selectedEditPart : m_viewer.getSelectedEditParts()) {
      selectedItems.remove(((TreeEditPart) selectedEditPart).getWidget());
    }
    return selectedItems;
  }

  public TreeRobot assertFeedback_before(Object object) {
    assertFeedback_insert(object, true);
    return this;
  }

  public TreeRobot assertFeedback_after(Object object) {
    assertFeedback_insert(object, false);
    return this;
  }

  private void assertFeedback_insert(Object object, boolean before) {
    Tree tree = (Tree) m_viewer.getControl();
    TreeEditPart editPart = getEditPart(object);
    // item
    Object item = tree.getData("_wbp_insertMarkItem");
    assertThat(item).isSameAs(editPart.getWidget());
    // before/after
    Boolean location = (Boolean) tree.getData("_wbp_insertMarkLocation");
    assertThat(location).isEqualTo(before);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Assertions
  //
  ////////////////////////////////////////////////////////////////////////////
  public void assertNullEditPart(Object object) {
    assertThat(getEditPartNull(object)).isNull();
  }

  public void assertNotNullEditPart(Object object) {
    assertThat(getEditPartNull(object)).isNotNull();
  }

  /**
   * Asserts that currently loaded {@link Tool} has <code>null</code> as command.
   */
  public TreeRobot assertCommandNull() throws Exception {
    final Command command = getCommand();
    assertThat(command).describedAs(new Description() {
      public String value() {
        return "Unexpected command " + command;
      }
    }).isNull();
    return this;
  }

  /**
   * Asserts that currently loaded {@link Tool} has not <code>null</code> as command.
   */
  public TreeRobot assertCommandNotNull() throws Exception {
    Command command = getCommand();
    assertThat(command).describedAs("No command.").isNotNull();
    return this;
  }
}