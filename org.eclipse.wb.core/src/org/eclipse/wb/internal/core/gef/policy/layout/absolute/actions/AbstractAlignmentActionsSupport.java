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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.gef.GefMessages;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.Separator;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Abstract helper for adding selection actions for absolute based layouts.
 *
 * @author mitin_aa
 * @coverage core.model.layout.absolute
 */
public abstract class AbstractAlignmentActionsSupport<C extends IAbstractComponentInfo> {
  // align horizontally
  protected static final int ALIGN_H_LEFT = 1;
  protected static final int ALIGN_H_CENTERS = 2;
  protected static final int ALIGN_H_SPACE = 4;
  protected static final int ALIGN_H_RIGHT = 5;
  // align vertically
  protected static final int ALIGN_V_TOP = 11;
  protected static final int ALIGN_V_CENTERS = 12;
  protected static final int ALIGN_V_SPACE = 14;
  protected static final int ALIGN_V_BOTTOM = 15;
  // replicate size
  protected static final int ALIGN_WIDTH = 21;
  protected static final int ALIGN_HEIGHT = 22;
  // center the widget
  protected static final int ALIGN_H_CENTER = 3;
  protected static final int ALIGN_V_CENTER = 13;
  // fields
  protected List<C> m_components;

  ////////////////////////////////////////////////////////////////////////////
  //
  // ObjectEventListener
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Create selection actions for given objects.
   */
  @SuppressWarnings("unchecked")
  public final void addAlignmentActions(List<ObjectInfo> objects, List<Object> actions) {
    // check selection
    {
      // empty selection
      if (objects.isEmpty()) {
        return;
      }
      // target is not on our container
      IAbstractComponentInfo layoutContainer = getLayoutContainer();
      if (layoutContainer == null
          || objects.get(0).getParent() != layoutContainer.getUnderlyingModel()) {
        return;
      }
    }
    // prepare components
    m_components = Lists.newArrayList();
    for (ObjectInfo object : objects) {
      if (!isComponentInfo(object)) {
        return;
      }
      m_components.add((C) object);
    }
    // selection should not include parent
    {
      Set<ObjectInfo> parents = Sets.newHashSet();
      for (IAbstractComponentInfo component : m_components) {
        parents.add(component.getParent());
      }
      for (ObjectInfo object : objects) {
        if (parents.contains(object)) {
          return;
        }
      }
    }
    // OK
    fillActions(actions);
  }

  /**
   * Fill actions list.
   */
  protected void fillActions(List<Object> actions) {
    addAlignmentActions(actions);
    addSizeActions(actions);
    addSpaceActions(actions);
    addCenterInContainerActions(actions);
  }

  protected void addAlignmentActions(List<Object> actions) {
    // create horizontal actions
    actions.add(new Separator());
    actions.add(new SelectionAction("h_left",
        GefMessages.AbstractAlignmentActionsSupport_alignLeft,
        ALIGN_H_LEFT));
    actions.add(new SelectionAction("h_centers",
        GefMessages.AbstractAlignmentActionsSupport_alighHorizontalCenters,
        ALIGN_H_CENTERS));
    actions.add(new SelectionAction("h_right",
        GefMessages.AbstractAlignmentActionsSupport_alignRight,
        ALIGN_H_RIGHT));
    // create vertical actions
    actions.add(new Separator());
    actions.add(new SelectionAction("v_top",
        GefMessages.AbstractAlignmentActionsSupport_alignTop,
        ALIGN_V_TOP));
    actions.add(new SelectionAction("v_centers",
        GefMessages.AbstractAlignmentActionsSupport_alignVerticalCenters,
        ALIGN_V_CENTERS));
    actions.add(new SelectionAction("v_bottom",
        GefMessages.AbstractAlignmentActionsSupport_alignBottom,
        ALIGN_V_BOTTOM));
  }

  protected void addSpaceActions(List<Object> actions) {
    // create space actions
    actions.add(new Separator());
    actions.add(new SelectionAction("h_space",
        GefMessages.AbstractAlignmentActionsSupport_spaceEquallyHorizontal,
        ALIGN_H_SPACE));
    actions.add(new SelectionAction("v_space",
        GefMessages.AbstractAlignmentActionsSupport_spaceEquallyVertical,
        ALIGN_V_SPACE));
  }

  protected void addSizeActions(List<Object> actions) {
    // create size actions
    actions.add(new Separator());
    actions.add(new SelectionAction("width",
        GefMessages.AbstractAlignmentActionsSupport_replicateWidth,
        ALIGN_WIDTH));
    actions.add(new SelectionAction("height",
        GefMessages.AbstractAlignmentActionsSupport_replicateHeight,
        ALIGN_HEIGHT));
  }

  protected void addCenterInContainerActions(List<Object> actions) {
    // create center actions
    actions.add(new Separator());
    actions.add(new SelectionAction("h_center",
        GefMessages.AbstractAlignmentActionsSupport_centerHorizontally,
        ALIGN_H_CENTER));
    actions.add(new SelectionAction("v_center",
        GefMessages.AbstractAlignmentActionsSupport_centerVertically,
        ALIGN_V_CENTER));
  }

  /**
   * @return <code>true</code> if given object is supported model.
   */
  protected abstract boolean isComponentInfo(ObjectInfo object);

  /**
   * @return <code>true</code> if given command supported for current selection.
   */
  protected final boolean isActionEnabled(int command) {
    if (m_components.size() == 1) {
      // for single selection support only set h/v center
      return command == ALIGN_H_CENTER || command == ALIGN_V_CENTER;
    }
    if (command == ALIGN_H_SPACE || command == ALIGN_V_SPACE) {
      // space equally support only one parent selection
      ObjectInfo parent = m_components.get(0).getParent();
      for (Iterator<C> I = m_components.listIterator(1); I.hasNext();) {
        C component = I.next();
        if (component.getParent() != parent) {
          return false;
        }
      }
    }
    return true;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Alignment
  //
  ////////////////////////////////////////////////////////////////////////////
  protected abstract void commandAlignLeft() throws Exception;

  protected abstract void commandAlignRight() throws Exception;

  protected abstract void commandAlignCenterHorizontally() throws Exception;

  protected abstract void commandAlignTop() throws Exception;

  protected abstract void commandAlignBottom() throws Exception;

  protected abstract void commandAlignCenterVertically() throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Distribute space
  //
  ////////////////////////////////////////////////////////////////////////////
  protected abstract void commandDistributeSpaceVertically() throws Exception;

  protected abstract void commandDistributeSpaceHorizontally() throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Place at center
  //
  ////////////////////////////////////////////////////////////////////////////
  protected abstract void commandCenterVertically() throws Exception;

  protected abstract void commandCenterHorizontally() throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Replicate width/height
  //
  ////////////////////////////////////////////////////////////////////////////
  protected abstract void commandReplicateHeight() throws Exception;

  protected abstract void commandReplicateWidth() throws Exception;

  protected abstract IAbstractComponentInfo getLayoutContainer();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Action class
  //
  ////////////////////////////////////////////////////////////////////////////
  protected class SelectionAction extends Action {
    private final int m_command;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public SelectionAction(String image, String tooltip, int command) {
      m_command = command;
      String iconPath = "info/layout/absolute/align_" + image;
      setImageDescriptor(DesignerPlugin.getImageDescriptor(iconPath + ".gif"));
      setDisabledImageDescriptor(DesignerPlugin.getImageDescriptor(iconPath + "_disabled.gif"));
      setEnabled(isActionEnabled(m_command));
      setToolTipText(tooltip);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // IAction
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public void run() {
      ExecutionUtils.run(m_components.get(0).getUnderlyingModel(), new RunnableEx() {
        public void run() throws Exception {
          switch (m_command) {
            case ALIGN_H_CENTER :
              commandCenterHorizontally();
              break;
            case ALIGN_HEIGHT :
              commandReplicateHeight();
              break;
            case ALIGN_WIDTH :
              commandReplicateWidth();
              break;
            case ALIGN_V_CENTERS :
              commandAlignCenterVertically();
              break;
            case ALIGN_V_BOTTOM :
              commandAlignBottom();
              break;
            case ALIGN_V_TOP :
              commandAlignTop();
              break;
            case ALIGN_H_CENTERS :
              commandAlignCenterHorizontally();
              break;
            case ALIGN_H_RIGHT :
              commandAlignRight();
              break;
            case ALIGN_H_LEFT :
              commandAlignLeft();
              break;
            case ALIGN_V_SPACE :
              commandDistributeSpaceVertically();
              break;
            case ALIGN_H_SPACE :
              commandDistributeSpaceHorizontally();
              break;
            case ALIGN_V_CENTER :
              commandCenterVertically();
              break;
            default :
              break;
          }
        }
      });
    }
  }
}