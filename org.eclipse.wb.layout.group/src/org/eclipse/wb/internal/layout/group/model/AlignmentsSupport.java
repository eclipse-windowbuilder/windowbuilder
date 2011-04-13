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
package org.eclipse.wb.internal.layout.group.model;

import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.ObjectInfoUtils;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.gef.policy.layout.absolute.actions.AbstractAlignmentActionsSupport;
import org.eclipse.wb.internal.layout.group.Messages;

import org.eclipse.jface.action.Separator;

import org.netbeans.modules.form.layoutdesign.LayoutComponent;
import org.netbeans.modules.form.layoutdesign.LayoutConstants;
import org.netbeans.modules.form.layoutdesign.LayoutDesigner;
import org.netbeans.modules.form.layoutdesign.LayoutModel;

import java.util.List;

public final class AlignmentsSupport<C extends IAbstractComponentInfo>
    extends
      AbstractAlignmentActionsSupport<C> implements LayoutConstants {
  private final IGroupLayoutInfo m_layout;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AlignmentsSupport(IGroupLayoutInfo layout) {
    m_layout = layout;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Fill
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Fill actions list.
   */
  @Override
  protected void fillActions(List<Object> actions) {
    addAlignmentActions(actions);
    addSizeActions(actions);
    addCenterInContainerActions(actions);
  }

  @Override
  protected void addSizeActions(List<Object> actions) {
    // create size actions
    actions.add(new Separator());
    actions.add(new SelectionAction("width", Messages.AlignmentsSupport_linkWidth, ALIGN_WIDTH));
    actions.add(new SelectionAction("height", Messages.AlignmentsSupport_linkHeight, ALIGN_HEIGHT));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Actions
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void commandAlignLeft() throws Exception {
    action_alignEdge(m_components, HORIZONTAL, LEADING);
  }

  @Override
  protected void commandAlignRight() throws Exception {
    action_alignEdge(m_components, HORIZONTAL, TRAILING);
  }

  @Override
  protected void commandAlignTop() throws Exception {
    action_alignEdge(m_components, VERTICAL, LEADING);
  }

  @Override
  protected void commandAlignBottom() throws Exception {
    action_alignEdge(m_components, VERTICAL, TRAILING);
  }

  @Override
  protected void commandAlignCenterHorizontally() throws Exception {
    action_alignCenter(m_components, true);
  }

  @Override
  protected void commandAlignCenterVertically() throws Exception {
    action_alignCenter(m_components, false);
  }

  @Override
  protected void commandCenterHorizontally() throws Exception {
    action_centerInParent(m_components, true);
  }

  @Override
  protected void commandCenterVertically() throws Exception {
    action_centerInParent(m_components, false);
  }

  @Override
  protected void commandReplicateWidth() throws Exception {
    action_toggleSameSize(m_components, true);
  }

  @Override
  protected void commandReplicateHeight() throws Exception {
    action_toggleSameSize(m_components, false);
  }

  @Override
  protected void commandDistributeSpaceHorizontally() throws Exception {
  }

  @Override
  protected void commandDistributeSpaceVertically() throws Exception {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Misc/Helpers
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected final IAbstractComponentInfo getLayoutContainer() {
    return m_layout.getLayoutContainer();
  }

  @Override
  protected boolean isComponentInfo(ObjectInfo object) {
    return m_layout.isRelatedComponent(object);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Alignment
  //
  ////////////////////////////////////////////////////////////////////////////
  public void action_alignEdge(List<C> components, int dimension, int edge) throws Exception {
    LayoutDesigner layoutDesigner = m_layout.getLayoutDesigner();
    layoutDesigner.align(GroupLayoutUtils.getIdsList(components), true, dimension, edge);
    layoutDesigner.updateCurrentState();
    m_layout.saveLayout();
  }

  public void action_alignCenter(List<C> components, boolean isHorizontal) throws Exception {
    LayoutDesigner layoutDesigner = m_layout.getLayoutDesigner();
    final String id = ObjectInfoUtils.getId(getLayoutContainer().getUnderlyingModel());
    final java.awt.Rectangle[] movingBounds = new java.awt.Rectangle[components.size()];
    int i = 0;
    for (IAbstractComponentInfo component : components) {
      movingBounds[i++] = GroupLayoutUtils.getBoundsInLayout(m_layout, component);
    }
    org.eclipse.wb.draw2d.geometry.Rectangle unionBounds =
        GroupLayoutUtils.getRectangleUnion(movingBounds);
    int middlePointInArea =
        isHorizontal ? unionBounds.x + unionBounds.width / 2 : unionBounds.y
            + unionBounds.height
            / 2;
    i = 0;
    for (IAbstractComponentInfo component : components) {
      java.awt.Rectangle componentBounds = movingBounds[i];
      String[] movindIds = new String[]{ObjectInfoUtils.getId(component.getUnderlyingModel())};
      java.awt.Rectangle[] movedBounds =
          new java.awt.Rectangle[]{new java.awt.Rectangle(0,
              0,
              componentBounds.width,
              componentBounds.height)};
      layoutDesigner.startMoving(
          movindIds,
          new java.awt.Rectangle[]{componentBounds},
          new java.awt.Point(0, 0));
      int middlePoint =
          isHorizontal ? componentBounds.width / 2 + componentBounds.x : componentBounds.height
              / 2
              + componentBounds.y;
      int moveDelta = middlePointInArea - middlePoint;
      if (moveDelta != 0) {
        java.awt.Point moveDeltaPoint =
            isHorizontal ? new java.awt.Point(moveDelta, 0) : new java.awt.Point(0, moveDelta);
        layoutDesigner.move(moveDeltaPoint, id, false, false, movedBounds);
        layoutDesigner.endMoving(true);
        layoutDesigner.updateCurrentState();
      } else {
        layoutDesigner.endMoving(false);
      }
      i++;
    }
    m_layout.saveLayout();
  }

  public void action_centerInParent(List<C> components, boolean isHorizontal) throws Exception {
    Rectangle parentBounds = getLayoutContainer().getModelBounds();
    java.awt.Rectangle[] movingBounds = new java.awt.Rectangle[components.size()];
    String[] movingIds = new String[components.size()];
    int i = 0;
    for (IAbstractComponentInfo component : components) {
      movingIds[i] = ObjectInfoUtils.getId(component.getUnderlyingModel());
      movingBounds[i++] = GroupLayoutUtils.getBoundsInLayout(m_layout, component);
    }
    Rectangle unionBounds = GroupLayoutUtils.getRectangleUnion(movingBounds);
    LayoutDesigner layoutDesigner = m_layout.getLayoutDesigner();
    layoutDesigner.startMoving(movingIds, movingBounds, new java.awt.Point(0, 0));
    String id = ObjectInfoUtils.getId(getLayoutContainer().getUnderlyingModel());
    java.awt.Rectangle[] movedBounds = new java.awt.Rectangle[components.size()];
    for (int j = 0; j < movedBounds.length; j++) {
      movedBounds[j] = new java.awt.Rectangle();
      movedBounds[j].width = movingBounds[j].width;
      movedBounds[j].height = movingBounds[j].height;
    }
    int middlePointInParent = isHorizontal ? parentBounds.width / 2 : parentBounds.height / 2;
    int middlePoint =
        isHorizontal ? unionBounds.width / 2 + unionBounds.x : unionBounds.height
            / 2
            + unionBounds.y;
    int moveDelta = middlePointInParent - middlePoint;
    if (moveDelta != 0) {
      java.awt.Point moveDeltaPoint =
          isHorizontal ? new java.awt.Point(moveDelta, 0) : new java.awt.Point(0, moveDelta);
      layoutDesigner.move(moveDeltaPoint, id, false, false, movedBounds);
      layoutDesigner.endMoving(true);
      layoutDesigner.updateCurrentState();
    } else {
      layoutDesigner.endMoving(false);
    }
    m_layout.saveLayout();
  }

  public void action_toggleSameSize(List<C> components, boolean isHorizontal) throws Exception {
    int dimension = isHorizontal ? HORIZONTAL : VERTICAL;
    List<String> idsList = GroupLayoutUtils.getIdsList(components);
    LayoutModel layoutModel = m_layout.getLayoutModel();
    int linked = layoutModel.areComponentsLinkSized(idsList, dimension);
    LayoutDesigner layoutDesigner = m_layout.getLayoutDesigner();
    if (linked == FALSE) {
      for (String id : idsList) {
        final LayoutComponent layoutComponent = layoutModel.getLayoutComponent(id);
        if (layoutDesigner.isComponentResizing(layoutComponent, dimension)) {
          layoutDesigner.setComponentResizing(layoutComponent, dimension, false);
        }
      }
      layoutModel.setSameSize(idsList, dimension);
    } else if (linked == TRUE) {
      layoutModel.unsetSameSize(idsList, dimension);
    }
    m_layout.saveLayout();
  }
}
