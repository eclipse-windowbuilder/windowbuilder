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
package org.eclipse.wb.internal.core.gef.policy.layout.absolute.actions;

import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.DesignerPlugin;

import java.util.Collections;
import java.util.Comparator;

/**
 * Helper for adding selection actions for absolute layouts.
 *
 * @author mitin_aa
 * @author scheglov_ke
 * @author lobas_av
 * @coverage core.model.layout.absolute
 */
public abstract class SimpleAlignmentActionsSupport<C extends IAbstractComponentInfo>
    extends
      AbstractAlignmentActionsSupport<C> {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Alignments
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void commandAlignLeft() throws Exception {
    C target = m_components.get(0);
    Rectangle targetBounds = getModelBounds(target);
    Point targetLocationInRoot = mapToRoot(target, targetBounds.x, targetBounds.y);
    for (C component : m_components) {
      if (target != component) {
        Rectangle objectBounds = getModelBounds(component);
        int x = mapFromRoot(component, targetLocationInRoot.x, 0).x;
        commandChangeBounds(component, new Point(x, objectBounds.y), null);
      }
    }
  }

  @Override
  protected void commandAlignRight() throws Exception {
    C target = m_components.get(0);
    Rectangle targetBounds = getModelBounds(target);
    Point targetLocationInRoot = mapToRoot(target, targetBounds.x, targetBounds.y);
    for (C component : m_components) {
      if (target != component) {
        Rectangle objectBounds = getModelBounds(component);
        int r = mapFromRoot(component, targetLocationInRoot.x + targetBounds.width, 0).x;
        commandChangeBounds(component, new Point(r - objectBounds.width, objectBounds.y), null);
      }
    }
  }

  @Override
  protected void commandAlignCenterHorizontally() throws Exception {
    C target = m_components.get(0);
    Rectangle targetBounds = getModelBounds(target);
    Point targetLocationInRoot = mapToRoot(target, targetBounds.x, targetBounds.y);
    for (C component : m_components) {
      if (target != component) {
        Rectangle objectBounds = getModelBounds(component);
        int c = mapFromRoot(component, targetLocationInRoot.x + targetBounds.width / 2, 0).x;
        commandChangeBounds(component, new Point(c - objectBounds.width / 2, objectBounds.y), null);
      }
    }
  }

  @Override
  protected void commandAlignTop() throws Exception {
    C target = m_components.get(0);
    Rectangle targetBounds = getModelBounds(target);
    Point targetLocationInRoot = mapToRoot(target, targetBounds.x, targetBounds.y);
    for (C component : m_components) {
      if (target != component) {
        Rectangle objectBounds = getModelBounds(component);
        int y = mapFromRoot(component, 0, targetLocationInRoot.y).y;
        commandChangeBounds(component, new Point(objectBounds.x, y), null);
      }
    }
  }

  @Override
  protected void commandAlignBottom() throws Exception {
    C target = m_components.get(0);
    Rectangle targetBounds = getModelBounds(target);
    Point targetLocationInRoot = mapToRoot(target, targetBounds.x, targetBounds.y);
    for (C component : m_components) {
      if (target != component) {
        Rectangle objectBounds = getModelBounds(component);
        int b = mapFromRoot(component, 0, targetLocationInRoot.y + targetBounds.height).y;
        commandChangeBounds(component, new Point(objectBounds.x, b - objectBounds.height), null);
      }
    }
  }

  @Override
  protected void commandAlignCenterVertically() throws Exception {
    C target = m_components.get(0);
    Rectangle targetBounds = getModelBounds(target);
    Point targetLocationInRoot = mapToRoot(target, targetBounds.x, targetBounds.y);
    for (C component : m_components) {
      if (target != component) {
        Rectangle objectBounds = getModelBounds(component);
        int c = mapFromRoot(component, 0, targetLocationInRoot.y + targetBounds.height / 2).y;
        commandChangeBounds(component, new Point(objectBounds.x, c - objectBounds.height / 2), null);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Distribute space
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void commandDistributeSpaceVertically() throws Exception {
    @SuppressWarnings("unchecked")
    Rectangle clientArea = getModelBounds((C) getLayoutContainer());
    // calculate sum height of objects
    int objectsHeight = 0;
    for (C component : m_components) {
      objectsHeight += getModelBounds(component).height;
    }
    // sort objects by their top positions
    Collections.sort(m_components, new Comparator<C>() {
      public int compare(C component1, C component2) {
        return getModelBounds(component1).y - getModelBounds(component2).y;
      }
    });
    // distribute objects between:
    // 1. top and bottom objects (if Ctrl pressed),
    // 2. or in parents client area
    int space;
    int y;
    int componentsLength = m_components.size();
    if (DesignerPlugin.isCtrlPressed() && componentsLength > 2) {
      // calculate space and start location (y)
      Rectangle topBounds = getModelBounds(m_components.get(0));
      Rectangle bottomBounds = getModelBounds(m_components.get(componentsLength - 1));
      int totalHeight = bottomBounds.bottom() - topBounds.y;
      space = (totalHeight - objectsHeight) / (componentsLength - 1);
      y = topBounds.y;
    } else {
      // calculate space and start location (y)
      space = (clientArea.height - objectsHeight) / (componentsLength + 1);
      y = space;
    }
    // change positions for objects from top to bottom
    for (C component : m_components) {
      Rectangle objectBounds = getModelBounds(component);
      commandChangeBounds(component, new Point(objectBounds.x, y), null);
      y += objectBounds.height;
      y += space;
    }
  }

  @Override
  protected void commandDistributeSpaceHorizontally() throws Exception {
    @SuppressWarnings("unchecked")
    Rectangle clientArea = getModelBounds((C) getLayoutContainer());
    // calculate sum width of objects
    int objectsWidth = 0;
    for (C component : m_components) {
      objectsWidth += getModelBounds(component).width;
    }
    // sort objects by their left positions
    Collections.sort(m_components, new Comparator<C>() {
      public int compare(C component1, C component2) {
        return getModelBounds(component1).x - getModelBounds(component2).x;
      }
    });
    // distribute objects between:
    // 1. left-most and right-most objects (if Ctrl pressed);
    // 2. or in parents client area
    int space;
    int x;
    int componentsLength = m_components.size();
    if (DesignerPlugin.isCtrlPressed() && componentsLength > 2) {
      // calculate space and start location (x)
      Rectangle leftBounds = getModelBounds(m_components.get(0));
      Rectangle rightBounds = getModelBounds(m_components.get(componentsLength - 1));
      int totalWidth = rightBounds.right() - leftBounds.x;
      space = (totalWidth - objectsWidth) / (componentsLength - 1);
      x = leftBounds.x;
    } else {
      // calculate space and start location (x)
      space = (clientArea.width - objectsWidth) / (componentsLength + 1);
      x = space;
    }
    // change positions for objects from left to right
    for (C component : m_components) {
      Rectangle objectBounds = getModelBounds(component);
      commandChangeBounds(component, new Point(x, objectBounds.y), null);
      x += objectBounds.width;
      x += space;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Place at center
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void commandCenterVertically() throws Exception {
    @SuppressWarnings("unchecked")
    Rectangle clientArea = getModelBounds((C) getLayoutContainer());
    int clientHeight = clientArea.height;
    for (C component : m_components) {
      Rectangle objectBounds = getModelBounds(component);
      commandChangeBounds(component, new Point(objectBounds.x,
          (clientHeight - objectBounds.height) / 2), null);
    }
  }

  @Override
  protected void commandCenterHorizontally() throws Exception {
    int clientAreaWidth;
    {
      IAbstractComponentInfo container = getLayoutContainer();
      clientAreaWidth = container.getBounds().width - container.getClientAreaInsets().getWidth();
    }
    // move components
    for (C component : m_components) {
      Rectangle objectBounds = getModelBounds(component);
      int newX = (clientAreaWidth - objectBounds.width) / 2;
      commandChangeBounds(component, new Point(newX, objectBounds.y), null);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Replicate width/height
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void commandReplicateHeight() throws Exception {
    C target = m_components.get(0);
    Rectangle targetBounds = getModelBounds(target);
    for (C component : m_components) {
      if (target != component) {
        Rectangle objectBounds = getModelBounds(component);
        commandChangeBounds(component, null, new Dimension(objectBounds.width, targetBounds.height));
      }
    }
  }

  @Override
  protected void commandReplicateWidth() throws Exception {
    C target = m_components.get(0);
    Rectangle targetBounds = getModelBounds(target);
    for (C component : m_components) {
      if (target != component) {
        Rectangle objectBounds = getModelBounds(component);
        commandChangeBounds(component, null, new Dimension(targetBounds.width, objectBounds.height));
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Change bounds
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Perform "move" or "resize" operation. Modifies location/size values by modifying appropriate
   * "setLocation", "setSize", "setBounds" arguments.
   *
   * @param component
   *          the component which modifications applies to.
   * @param location
   *          the {@link Point} of new location of component. May be null.
   * @param size
   *          the {@link Dimension} of new size of component. May be null.
   */
  protected abstract void commandChangeBounds(C component, Point location, Dimension size)
      throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Returns "model" bounds of given object.
   */
  private Rectangle getModelBounds(C component) {
    return component.getModelBounds();
  }

  /**
   * Maps given coordinates (relative to the parent of given object) to the coordinates in some
   * root.
   */
  @SuppressWarnings("unchecked")
  private Point mapToRoot(C component, int x, int y) {
    C parent = (C) component.getParent();
    while (parent.getParent() != null) {
      if (isValidObjectOnRootPath(parent)) {
        Rectangle bounds = getModelBounds(parent);
        x += bounds.x;
        y += bounds.y;
      }
      parent = (C) parent.getParent();
    }
    return new Point(x, y);
  }

  /**
   * Maps given coordinates (in some root) to the parent coordinates (relative to the parent of
   * given object).
   */
  @SuppressWarnings("unchecked")
  private Point mapFromRoot(C component, int x, int y) {
    C parent = (C) component.getParent();
    while (parent.getParent() != null) {
      if (isValidObjectOnRootPath(parent)) {
        Rectangle bounds = getModelBounds(parent);
        x -= bounds.x;
        y -= bounds.y;
      }
      parent = (C) parent.getParent();
    }
    return new Point(x, y);
  }

  /**
   * Checks if given object is valid object on the path to the root.
   */
  protected abstract boolean isValidObjectOnRootPath(IAbstractComponentInfo parent);
}