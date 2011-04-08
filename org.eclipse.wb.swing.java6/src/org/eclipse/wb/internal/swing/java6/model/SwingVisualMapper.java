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
package org.eclipse.wb.internal.swing.java6.model;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfoUtils;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.internal.core.laf.BaselineSupportHelper;
import org.eclipse.wb.internal.layout.group.model.GroupLayoutUtils;
import org.eclipse.wb.internal.layout.group.model.IGroupLayoutInfo;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;

import org.netbeans.modules.form.layoutdesign.VisualMapper;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.text.JTextComponent;

/**
 * The VisualMapper for Swing.
 * 
 * @author mitin_aa
 */
public class SwingVisualMapper implements VisualMapper {
  private GroupLayoutInfo2 m_layout;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SwingVisualMapper(GroupLayoutInfo2 layout) {
    m_layout = layout;
    m_layout.addBroadcastListener(new JavaEventListener() {
      @Override
      public void replaceChildAfter(JavaInfo parent, JavaInfo oldChild, JavaInfo newChild)
          throws Exception {
        if (oldChild == m_layout) {
          m_layout = (GroupLayoutInfo2) newChild;
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // VisualMapper
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public int getBaselinePosition(String componentId, int width, int height) {
    return BaselineSupportHelper.getBaseline(getComponent(componentId));
  }

  @Override
  public java.awt.Rectangle getComponentBounds(String componentId) {
    ComponentInfo componentInfo = getComponentInfo(componentId);
    return GroupLayoutUtils.getBoundsInLayout(
        m_layout.getAdapter(IGroupLayoutInfo.class),
        componentInfo);
  }

  @Override
  public Dimension getComponentMinimumSize(String componentId) {
    Component component = getComponent(componentId);
    return component.getMinimumSize();
  }

  @Override
  public Dimension getComponentPreferredSize(String componentId) {
    Component component = getComponent(componentId);
    return component.getPreferredSize();
  }

  @Override
  public java.awt.Rectangle getContainerInterior(String componentId) {
    org.eclipse.wb.draw2d.geometry.Rectangle bounds = getContainer().getModelBounds().getCopy();
    bounds.crop(getContainer().getInsets());
    return new java.awt.Rectangle(0, 0, bounds.width, bounds.height);
  }

  @Override
  public int getPreferredPadding(String component1Id,
      String component2Id,
      int dimension,
      int comp2Alignment,
      PaddingType paddingType) {
    Component comp1 = getComponent(component1Id);
    Component comp2 = getComponent(component2Id);
    JComponent jcomp1 = comp1 instanceof JComponent ? (JComponent) comp1 : null;
    JComponent jcomp2 = comp2 instanceof JComponent ? (JComponent) comp2 : null;
    assert dimension == HORIZONTAL || dimension == VERTICAL;
    assert comp2Alignment == LEADING || comp2Alignment == TRAILING;
    LayoutStyle.ComponentPlacement type =
        paddingType == PaddingType.INDENT
            ? LayoutStyle.ComponentPlacement.INDENT
            : paddingType == PaddingType.RELATED
                ? LayoutStyle.ComponentPlacement.RELATED
                : LayoutStyle.ComponentPlacement.UNRELATED;
    int position = 0;
    if (dimension == HORIZONTAL) {
      if (paddingType == PaddingType.INDENT) {
        position = comp2Alignment == LEADING ? SwingConstants.WEST : SwingConstants.EAST;
      } else {
        position = comp2Alignment == LEADING ? SwingConstants.EAST : SwingConstants.WEST;
      }
    } else {
      position = comp2Alignment == LEADING ? SwingConstants.SOUTH : SwingConstants.NORTH;
    }
    if (jcomp1 == null || jcomp2 == null) {
      // default distance between components
      return type != javax.swing.LayoutStyle.ComponentPlacement.UNRELATED ? 6 : 12;
    }
    int prefPadding =
        paddingType != PaddingType.SEPARATE ? LayoutStyle.getInstance().getPreferredGap(
            jcomp1,
            jcomp2,
            type,
            position,
            getContainer().getContainer()) : PADDING_SEPARATE_VALUE;
    return prefPadding;
  }

  @Override
  public void setComponentVisibility(String componentId, boolean visible) {
  }

  @Override
  public int getPreferredPaddingInParent(String parentId,
      String componentId,
      int dimension,
      int compAlignment) {
    int alignment;
    if (dimension == HORIZONTAL) {
      if (compAlignment == LEADING) {
        alignment = SwingConstants.WEST;
      } else {
        alignment = SwingConstants.EAST;
      }
    } else {
      if (compAlignment == LEADING) {
        alignment = SwingConstants.NORTH;
      } else {
        alignment = SwingConstants.SOUTH;
      }
    }
    Component component = getComponent(componentId);
    if (component instanceof JComponent) {
      return LayoutStyle.getInstance().getContainerGap(
          (JComponent) component,
          alignment,
          getContainer().getContainer());
    } else {
      return 6;
    }
  }

  @Override
  public boolean hasExplicitPreferredSize(String componentId) {
    ComponentInfo componentInfo = getComponentInfo(componentId);
    Component component = componentInfo.getComponent();
    // special case for JTextComponent
    if (JTextComponent.class.isAssignableFrom(componentInfo.getDescription().getComponentClass())) {
      return true;
    } else if (component == null) {
      return false;
    } else {
      return ((JComponent) component).isPreferredSizeSet();
    }
  }

  @Override
  public void rebuildLayout(String containerId) {
  }

  @Override
  public boolean[] getComponentResizability(String compId, boolean[] resizability) {
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Helpers/Misc
  //
  ////////////////////////////////////////////////////////////////////////////
  private Component getComponent(String id) {
    ComponentInfo componentInfo = getComponentInfo(id);
    return componentInfo.getComponent();
  }

  private ComponentInfo getComponentInfo(String id) {
    return (ComponentInfo) ObjectInfoUtils.getById(id);
  }

  private ContainerInfo getContainer() {
    return m_layout.getContainer();
  }
}
