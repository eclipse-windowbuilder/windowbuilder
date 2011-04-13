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

import org.eclipse.wb.core.editor.IContextMenuConstants;
import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.internal.core.model.layout.absolute.IImageProvider;
import org.eclipse.wb.internal.core.model.util.ObjectInfoAction;
import org.eclipse.wb.internal.core.utils.ui.ImageImageDescriptor;
import org.eclipse.wb.internal.layout.group.Messages;

import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;

import org.netbeans.modules.form.layoutdesign.LayoutComponent;
import org.netbeans.modules.form.layoutdesign.LayoutConstants;
import org.netbeans.modules.form.layoutdesign.LayoutDesigner;
import org.netbeans.modules.form.layoutdesign.LayoutInterval;
import org.netbeans.modules.form.layoutdesign.LayoutUtils;
import org.netbeans.modules.form.layoutdesign.VisualMapper;

public final class AnchorsSupport implements LayoutConstants {
  // constant
  public static final int RESIZABLE = 2;
  // layout field
  private final IGroupLayoutInfo m_layout;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AnchorsSupport(IGroupLayoutInfo layout) {
    m_layout = layout;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public void fillContextMenu(AbstractComponentInfo component, IMenuManager manager) {
    {
      IMenuManager anchorsManager = new MenuManager(Messages.AnchorsSupport_anchorsMenu);
      manager.appendToGroup(IContextMenuConstants.GROUP_CONSTRAINTS, anchorsManager);
      anchorsManager.add(new SetAnchorAction(component, Messages.AnchorsSupport_anchorLeft, "h/menu/left.gif", true, LEADING));
      anchorsManager.add(new SetAnchorAction(component, Messages.AnchorsSupport_anchorRight, "h/menu/right.gif", true, TRAILING));
      anchorsManager.add(new SetAnchorAction(component, Messages.AnchorsSupport_anchorTop, "v/menu/top.gif", false, LEADING));
      anchorsManager.add(new SetAnchorAction(component,
          Messages.AnchorsSupport_anchorBottom,
          "v/menu/bottom.gif",
          false,
          TRAILING));
    }
    {
      IMenuManager autoResigingManager = new MenuManager(Messages.AnchorsSupport_autoResizeMenu);
      manager.appendToGroup(IContextMenuConstants.GROUP_CONSTRAINTS, autoResigingManager);
      autoResigingManager.add(new ToggleResizeableAction(component, "h/menu/both.gif", true));
      autoResigingManager.add(new ToggleResizeableAction(component, "v/menu/both.gif", false));
    }
  }

  public void fillContributionManager(AbstractComponentInfo component,
      IContributionManager manager,
      boolean isHorizontal) {
    if (isHorizontal) {
      manager.add(new SetAnchorAction(component, Messages.AnchorsSupport_anchoredLeft, "h/menu/left.gif", true, LEADING));
      manager.add(new SetAnchorAction(component,
          Messages.AnchorsSupport_anchoredRight,
          "h/menu/right.gif",
          true,
          TRAILING));
      manager.add(new MakeResizeableAction(component, "h/menu/both.gif", isHorizontal));
    } else {
      manager.add(new SetAnchorAction(component, Messages.AnchorsSupport_anchoredTop, "v/menu/top.gif", false, LEADING));
      manager.add(new SetAnchorAction(component,
          Messages.AnchorsSupport_anchoredBottom,
          "v/menu/bottom.gif",
          false,
          TRAILING));
      manager.add(new MakeResizeableAction(component, "v/menu/both.gif", isHorizontal));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Anchors/Resizability
  //
  ////////////////////////////////////////////////////////////////////////////
  private boolean isComponentResizable(AbstractComponentInfo component, boolean isHorizontal) {
    int dimension = isHorizontal ? HORIZONTAL : VERTICAL;
    LayoutComponent layoutComponent = getLayoutComponent(component);
    return m_layout.getLayoutDesigner().isComponentResizing(layoutComponent, dimension);
  }

  public int getCurrentAnchors(AbstractComponentInfo component, boolean isHorizontal) {
    if (isComponentResizable(component, isHorizontal)) {
      return RESIZABLE;
    } else {
      return getCurrentAlignment(component, isHorizontal);
    }
  }

  private int getCurrentAlignment(AbstractComponentInfo component, boolean isHorizontal) {
    int dimension = isHorizontal ? HORIZONTAL : VERTICAL;
    LayoutComponent layoutComp = getLayoutComponent(component);
    int[] alignment =
        m_layout.getLayoutDesigner().getAdjustableComponentAlignment(layoutComp, dimension);
    return alignment[0];
  }

  public Integer getEmptySpaceValue(AbstractComponentInfo aroundComponent,
      int dimension,
      int direction) {
    LayoutInterval space = getEmptySpaceInterval(aroundComponent, dimension, direction);
    if (space != null) {
      return space.getPreferredSize(false);
    }
    return null;
  }

  public Integer getDefaultGapSize(AbstractComponentInfo aroundComponent,
      int dimension,
      int direction) {
    LayoutInterval space = getEmptySpaceInterval(aroundComponent, dimension, direction);
    if (space != null) {
      return new Integer(LayoutUtils.getSizeOfDefaultGap(
          space,
          m_layout.getAdapter(VisualMapper.class)));
    }
    return null;
  }

  private LayoutInterval getEmptySpaceInterval(AbstractComponentInfo aroundComponent,
      int dimension,
      int direction) {
    LayoutComponent comp = getLayoutComponent(aroundComponent);
    return LayoutUtils.getAdjacentEmptySpace(comp, dimension, direction);
  }

  private LayoutComponent getLayoutComponent(AbstractComponentInfo component) {
    return GroupLayoutUtils.getLayoutComponent(m_layout, component);
  }

  private void action_setResizability(AbstractComponentInfo component,
      boolean isHorizontal,
      boolean resizable) throws Exception {
    int dimension = isHorizontal ? HORIZONTAL : VERTICAL;
    LayoutComponent layoutComponent = getLayoutComponent(component);
    if (isComponentResizable(component, isHorizontal) != resizable) {
      LayoutDesigner layoutDesigner = m_layout.getLayoutDesigner();
      layoutDesigner.setComponentResizing(layoutComponent, dimension, resizable);
      layoutDesigner.updateCurrentState();
      m_layout.saveLayout();
    }
  }

  private void action_setAlignment(AbstractComponentInfo component,
      boolean isHorizontal,
      int newAlignment) throws Exception {
    int dimension = isHorizontal ? HORIZONTAL : VERTICAL;
    LayoutComponent layoutComp = getLayoutComponent(component);
    LayoutDesigner layoutDesigner = m_layout.getLayoutDesigner();
    int[] alignment = layoutDesigner.getAdjustableComponentAlignment(layoutComp, dimension);
    if ((alignment[1] & 1 << newAlignment) != 0 && alignment[0] != newAlignment) {
      layoutDesigner.adjustComponentAlignment(layoutComp, dimension, newAlignment);
      layoutDesigner.updateCurrentState();
      m_layout.saveLayout();
    }
  }

  public void action_setComponentSize(AbstractComponentInfo component, int dimension, int value)
      throws Exception {
    if (value < 0 && value != NOT_EXPLICITLY_DEFINED) {
      return;
    }
    LayoutComponent layoutComponent = getLayoutComponent(component);
    LayoutInterval space = layoutComponent.getLayoutInterval(dimension);
    if (space != null) {
      int pref = space.getPreferredSize(false);
      int max = space.getMaximumSize(false);
      int min = space.getMinimumSize(false);
      if (value != pref) {
        m_layout.getLayoutModel().setIntervalSize(space, min, value, max);
        m_layout.saveLayout();
      }
    }
  }

  public void action_setEmptySpaceProperties(AbstractComponentInfo aroundComponent,
      int dimension,
      int direction,
      int gapSize) throws Exception {
    LayoutInterval space = getEmptySpaceInterval(aroundComponent, dimension, direction);
    if (space != null) {
      int pref = space.getPreferredSize(false);
      int max = space.getMaximumSize(false);
      boolean isResizable = max != USE_PREFERRED_SIZE && max != pref;
      if (pref != gapSize) {
        m_layout.getLayoutModel().setIntervalSize(
            space,
            isResizable ? NOT_EXPLICITLY_DEFINED : USE_PREFERRED_SIZE,
            gapSize,
            isResizable ? Short.MAX_VALUE : USE_PREFERRED_SIZE);
        m_layout.saveLayout();
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Action classes
  //
  ////////////////////////////////////////////////////////////////////////////
  private final class SetAnchorAction extends ObjectInfoAction {
    private final int m_alignment;
    private final AbstractComponentInfo m_component;
    private final boolean m_isHorizontal;
    private final boolean m_alreadySet;

    private SetAnchorAction(AbstractComponentInfo component,
        String text,
        String imageName,
        boolean isHorizontal,
        int alignment) {
      super(component, text, new ImageImageDescriptor(getImageProvider().getImage(
          "info/layout/groupLayout/" + imageName)), AS_CHECK_BOX);
      m_isHorizontal = isHorizontal;
      m_component = component;
      m_alignment = alignment;
      m_alreadySet = getCurrentAnchors(component, isHorizontal) == alignment;
      setEnabled(component != null);
      setChecked(m_alreadySet);
    }

    @Override
    protected void runEx() throws Exception {
      action_setResizability(m_component, m_isHorizontal, false);
      action_setAlignment(m_component, m_isHorizontal, m_alignment);
    }

    @Override
    protected boolean shouldRun() throws Exception {
      return !m_alreadySet;
    }
  }
  private final class MakeResizeableAction extends ObjectInfoAction {
    private final boolean m_isHorizontal;
    private final AbstractComponentInfo m_component;
    private final boolean m_alreadySet;

    private MakeResizeableAction(AbstractComponentInfo component,
        String imageName,
        boolean isHorizontal) {
      super(component, Messages.AnchorsSupport_autoResizable, new ImageImageDescriptor(getImageProvider().getImage(
          "info/layout/groupLayout/" + imageName)), AS_CHECK_BOX);
      m_component = component;
      m_isHorizontal = isHorizontal;
      setEnabled(component != null);
      boolean isResizable = isComponentResizable(component, isHorizontal);
      setChecked(isResizable);
      m_alreadySet = isResizable == true;
    }

    @Override
    protected void runEx() throws Exception {
      action_setResizability(m_component, m_isHorizontal, true);
    }

    @Override
    protected boolean shouldRun() throws Exception {
      return !m_alreadySet;
    }
  }
  private final class ToggleResizeableAction extends ObjectInfoAction {
    private final boolean m_isHorizontal;
    private final AbstractComponentInfo m_component;

    private ToggleResizeableAction(AbstractComponentInfo component,
        String imageName,
        boolean isHorizontal) {
      super(component,
          isHorizontal ? Messages.AnchorsSupport_resizableHorizontal : Messages.AnchorsSupport_resizableVertical,
          new ImageImageDescriptor(getImageProvider().getImage(
              "info/layout/groupLayout/" + imageName)),
          AS_CHECK_BOX);
      m_component = component;
      m_isHorizontal = isHorizontal;
      setEnabled(component != null);
      setChecked(isComponentResizable(component, isHorizontal));
    }

    @Override
    protected void runEx() throws Exception {
      action_setResizability(
          m_component,
          m_isHorizontal,
          !isComponentResizable(m_component, m_isHorizontal));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Misc
  //
  ////////////////////////////////////////////////////////////////////////////
  private IImageProvider getImageProvider() {
    return m_layout.getAdapter(IImageProvider.class);
  }
}
