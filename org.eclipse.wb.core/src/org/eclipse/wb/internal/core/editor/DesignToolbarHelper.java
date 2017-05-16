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
package org.eclipse.wb.internal.core.editor;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;

import java.util.List;

/**
 * Helper for managing actions on internal {@link ToolBarManager} of {@link DesignPage}.
 *
 * @author lobas_av
 * @author scheglov_ke
 * @coverage core.editor
 */
public class DesignToolbarHelper {
  protected final ToolBarManager m_toolBarManager;
  private IEditPartViewer m_viewer;
  private ObjectInfo m_rootObject;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Initializes new {@link DesignToolbarHelper}.
   *
   * @param designPage
   *          the target {@link DesignPage}.
   * @param toolBar
   *          the target {@link ToolBar}.
   */
  public DesignToolbarHelper(ToolBar toolBar) {
    m_toolBarManager = new ToolBarManager(toolBar);
    toolBar.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e) {
        m_toolBarManager.dispose();
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Initializes {@link IEditPartViewer}.
   */
  protected void initialize(IEditPartViewer viewer) {
    m_viewer = viewer;
  }

  /**
   * Fills {@link ToolBar} with actions.
   */
  public void fill() {
    IWorkbenchWindow window = DesignerPlugin.getActiveWorkbenchWindow();
    {
      m_toolBarManager.add(ActionFactory.UNDO.create(window));
      m_toolBarManager.add(ActionFactory.REDO.create(window));
      m_toolBarManager.add(new Separator());
    }
    {
      m_toolBarManager.add(ActionFactory.CUT.create(window));
      m_toolBarManager.add(ActionFactory.COPY.create(window));
      m_toolBarManager.add(ActionFactory.PASTE.create(window));
      m_toolBarManager.add(ActionFactory.DELETE.create(window));
      m_toolBarManager.add(new Separator());
    }
  }

  protected void fill2() {
    // dynamic actions
    createHierarchyGroups();
    createSelectionGroups();
    // track dynamic actions
    m_viewer.addSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent event) {
        refreshDynamicActions(false, true);
      }
    });
    refreshDynamicActions(false, false);
  }

  /**
   * Sets the root {@link ObjectInfo} on {@link DesignPage}.
   */
  public void setRoot(ObjectInfo rootObject) {
    // delete listener from old root
    if (m_rootObject != null) {
      m_rootObject.removeBroadcastListener(m_objectRefreshListener);
    }
    // set root
    m_rootObject = rootObject;
    // add listener to new root
    m_rootObject.addBroadcastListener(m_objectRefreshListener);
    // refresh now
    refreshDynamicActions(true, true);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Dynamic actions
  //
  ////////////////////////////////////////////////////////////////////////////
  private final ObjectEventListener m_objectRefreshListener = new ObjectEventListener() {
    @Override
    public void refreshed() throws Exception {
      // execute in async to let GEF refresh to update selection
      DesignerPlugin.getStandardDisplay().asyncExec(new Runnable() {
        public void run() {
          refreshDynamicActions(true, true);
        }
      });
    }
  };

  /**
   * Refresh all dynamic actions.
   *
   * @param refreshHierarchy
   *          is <code>true</code> if hierarchy actions should be refreshed.
   * @param refreshSelection
   *          is <code>true</code> if selection actions should be refreshed.
   */
  private void refreshDynamicActions(boolean refreshHierarchy, boolean refreshSelection) {
    Composite redrawControl = m_toolBarManager.getControl().getParent();
    redrawControl.setRedraw(false);
    try {
      if (refreshHierarchy) {
        refreshHierarchyActions();
      }
      if (refreshSelection) {
        refreshSelectionActions();
      }
      // do layout
      m_toolBarManager.update(true);
      redrawControl.layout();
    } finally {
      redrawControl.setRedraw(true);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Hierarchy actions
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final String HIERARCHY_ACTIONS_GROUP = "HIERARCHY_ACTIONS_GROUP";
  private static final String HIERARCHY_ACTIONS_GROUP_END = "HIERARCHY_ACTIONS_GROUP_end";
  private final List<IContributionItem> m_hierarchyItems = Lists.newArrayList();

  /**
   * Creates group {@link Separator}'s for adding/removing actions based on components hierarchy.
   */
  private void createHierarchyGroups() {
    m_toolBarManager.add(new Separator(HIERARCHY_ACTIONS_GROUP));
    m_toolBarManager.add(new Separator(HIERARCHY_ACTIONS_GROUP_END));
  }

  /**
   * Refreshes the actions based on components hierarchy.
   */
  private void refreshHierarchyActions() {
    final List<IContributionItem> toRemove = Lists.newArrayList(m_hierarchyItems);
    // add items for hierarchy
    ExecutionUtils.runLog(new RunnableEx() {
      public void run() throws Exception {
        // prepare items
        List<Object> items;
        {
          items = Lists.newArrayList();
          m_rootObject.getBroadcastObject().addHierarchyActions(items);
        }
        // add items to toolbar
        m_hierarchyItems.clear();
        for (Object object : items) {
          // prepare contribution item
          IContributionItem item;
          if (object instanceof IContributionItem) {
            item = (IContributionItem) object;
          } else {
            IAction action = (IAction) object;
            item = new ActionContributionItem(action);
          }
          // add item
          toRemove.remove(item);
          m_hierarchyItems.add(item);
          m_toolBarManager.remove(item);
          m_toolBarManager.appendToGroup(HIERARCHY_ACTIONS_GROUP, item);
        }
      }
    });
    // remove old items
    for (IContributionItem item : toRemove) {
      m_toolBarManager.remove(item);
      item.dispose();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Selection actions
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final String SELECTION_ACTIONS_GROUP = "SELECTION_ACTIONS_GROUP";
  private static final String SELECTION_ACTIONS_GROUP_END = "SELECTION_ACTIONS_GROUP_end";
  private final List<IContributionItem> m_selectionItems = Lists.newArrayList();

  /**
   * Creates group {@link Separator}'s for adding/removing actions based on selection in
   * {@link #m_viewer}.
   */
  private void createSelectionGroups() {
    m_toolBarManager.add(new Separator(SELECTION_ACTIONS_GROUP));
    m_toolBarManager.add(new Separator(SELECTION_ACTIONS_GROUP_END));
  }

  /**
   * Refreshes the actions based on selection.
   */
  private void refreshSelectionActions() {
    final List<IContributionItem> toRemove = Lists.newArrayList(m_selectionItems);
    // prepare selected ObjectInfo's
    final List<ObjectInfo> selectedObjects = Lists.newArrayList();
    for (EditPart editPart : m_viewer.getSelectedEditParts()) {
      Object model = editPart.getModel();
      if (model instanceof ObjectInfo) {
        selectedObjects.add((ObjectInfo) model);
      } else {
        return;
      }
    }
    // add items for selected objects
    ExecutionUtils.runLog(new RunnableEx() {
      public void run() throws Exception {
        // prepare items
        List<Object> items;
        {
          items = Lists.newArrayList();
          m_rootObject.getBroadcastObject().addSelectionActions(selectedObjects, items);
        }
        // don't remove items that are added again
        m_selectionItems.clear();
        toRemove.removeAll(items);
        // add items to toolbar
        for (Object object : items) {
          // prepare contribution item
          IContributionItem item;
          if (object instanceof IContributionItem) {
            item = (IContributionItem) object;
          } else {
            IAction action = (IAction) object;
            item = new ActionContributionItem(action);
          }
          // add item
          m_selectionItems.add(item);
          m_toolBarManager.remove(item);
          m_toolBarManager.appendToGroup(SELECTION_ACTIONS_GROUP, item);
        }
      }
    });
    // remove old items
    for (IContributionItem item : toRemove) {
      m_toolBarManager.remove(item);
      item.dispose();
    }
  }
}