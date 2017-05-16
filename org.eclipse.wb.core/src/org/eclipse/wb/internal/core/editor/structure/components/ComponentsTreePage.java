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
package org.eclipse.wb.internal.core.editor.structure.components;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.editor.IDesignPageSite;
import org.eclipse.wb.core.model.HasSourcePosition;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.editor.DesignPageSite;
import org.eclipse.wb.internal.core.editor.Messages;
import org.eclipse.wb.internal.core.editor.structure.IPage;
import org.eclipse.wb.internal.core.gefTree.EditPartFactory;
import org.eclipse.wb.internal.core.model.ObjectReferenceInfo;
import org.eclipse.wb.internal.core.preferences.IPreferenceConstants;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.gef.EditPartsSelectionProvider;
import org.eclipse.wb.internal.core.utils.ui.UiUtils;
import org.eclipse.wb.internal.gef.tree.TreeViewer;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import java.util.List;

/**
 * Implementation of {@link IPage} for displaying hierarchy of {@link ObjectInfo}'s.
 *
 * @author scheglov_ke
 * @coverage core.editor.structure
 */
public final class ComponentsTreePage implements IPage {
  private TreeViewer m_viewer;
  private IEditPartViewer m_graphicalViewer;
  private ObjectInfo m_rootObject;

  ////////////////////////////////////////////////////////////////////////////
  //
  // IPage
  //
  ////////////////////////////////////////////////////////////////////////////
  public void dispose() {
    Control control = getControl();
    if (control != null && !control.isDisposed()) {
      control.dispose();
    }
  }

  public void createControl(Composite parent) {
    m_viewer = new TreeViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
    m_viewer.addSelectionChangedListener(m_selectionListener_Tree);
  }

  public Control getControl() {
    return m_viewer.getControl();
  }

  public void setFocus() {
    getControl().setFocus();
  }

  public void setToolBar(IToolBarManager toolBarManager) {
    {
      IAction action = new Action() {
        @Override
        public void run() {
          UiUtils.expandAll(m_viewer.getTree());
        }
      };
      toolBarManager.add(action);
      action.setImageDescriptor(DesignerPlugin.getImageDescriptor("expand_all.gif"));
      action.setToolTipText(Messages.ComponentsTreePage_expandAllAction);
    }
    {
      Action action = new Action() {
        @Override
        public void run() {
          UiUtils.collapseAll(m_viewer.getTree());
        }
      };
      toolBarManager.add(action);
      action.setImageDescriptor(DesignerPlugin.getImageDescriptor("collapse_all.gif"));
      action.setToolTipText(Messages.ComponentsTreePage_collapseAllAction);
    }
    toolBarManager.update(false);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Selection (internal)
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Listener for selection in {@link #m_viewer}.
   */
  private final ISelectionChangedListener m_selectionListener_Tree =
      new ISelectionChangedListener() {
        public void selectionChanged(SelectionChangedEvent event) {
          selectGraphicalViewer();
        }
      };
  /**
   * Listener for selection in {@link #m_graphicalViewer}.
   */
  private final ISelectionChangedListener m_selectionListener_Graphical =
      new ISelectionChangedListener() {
        public void selectionChanged(SelectionChangedEvent event) {
          selectTreeViewer();
        }
      };

  /**
   * Selects {@link ObjectInfo}'s in {@link #m_viewer} using selection in {@link #m_graphicalViewer}
   * .
   */
  private void selectTreeViewer() {
    List<EditPart> selectedEditParts = m_graphicalViewer.getSelectedEditParts();
    setSelection(m_viewer, m_selectionListener_Tree, selectedEditParts);
    showComponentDefinition(selectedEditParts);
  }

  /**
   * Selects {@link EditPart}'s in {@link #m_graphicalViewer} using selection in {@link #m_viewer}.
   */
  private void selectGraphicalViewer() {
    final List<EditPart> selectedEditParts = m_viewer.getSelectedEditParts();
    // refresh if necessary
    ExecutionUtils.runLog(new RunnableEx() {
      public void run() throws Exception {
        boolean[] refreshFlag = new boolean[1];
        if (!selectedEditParts.isEmpty()) {
          for (EditPart editPart : selectedEditParts) {
            ObjectInfo objectInfo = (ObjectInfo) editPart.getModel();
            objectInfo.getBroadcastObject().selecting(objectInfo, refreshFlag);
          }
        } else {
          m_rootObject.getBroadcastObject().selecting(null, refreshFlag);
        }
        // Do refresh.
        // We remove "graphical listener" because refresh can cause temporary selection changes,
        // for example because of removing some graphical EditPart's. But we know, that we apply
        // selection from "tree", it should stay as is, so no need to listen for "graphical listener".
        if (refreshFlag[0]) {
          m_graphicalViewer.removeSelectionChangedListener(m_selectionListener_Graphical);
          try {
            m_rootObject.refresh();
          } finally {
            m_graphicalViewer.addSelectionChangedListener(m_selectionListener_Graphical);
          }
        }
      }
    });
    // set selection
    setSelection(m_graphicalViewer, m_selectionListener_Graphical, selectedEditParts);
    showComponentDefinition(selectedEditParts);
  }

  /**
   * Sets selection in given {@link IEditPartViewer} using {@link List} of selected {@link EditPart}
   * 's.
   *
   * @param targetViewer
   *          the {@link IEditPartViewer} to set selection.
   * @param selectionListener
   *          the {@link ISelectionChangedListener} that should be temporary removed from
   *          {@link IEditPartViewer} to avoid recursive selection even handling.
   * @param sourceEditParts
   *          the selected {@link EditPart}'s for which corresponding selection should be set.
   */
  private static void setSelection(IEditPartViewer targetViewer,
      ISelectionChangedListener selectionListener,
      List<EditPart> sourceEditParts) {
    // prepare EditPart's in target viewer
    List<EditPart> targetEditParts = Lists.newArrayList();
    for (EditPart sourceEditPart : sourceEditParts) {
      Object model = sourceEditPart.getModel();
      if (model instanceof ObjectReferenceInfo) {
        model = ((ObjectReferenceInfo) model).getObject();
      }
      EditPart targetEditPart = targetViewer.getEditPartByModel(model);
      if (targetEditPart != null) {
        targetEditParts.add(targetEditPart);
      }
    }
    // set selection
    targetViewer.removeSelectionChangedListener(selectionListener);
    try {
      targetViewer.setSelection(targetEditParts);
    } finally {
      targetViewer.addSelectionChangedListener(selectionListener);
    }
  }

  /**
   * Shows definition in source for primary selected {@link EditPart} with {@link ObjectInfo} model.
   */
  private static void showComponentDefinition(List<EditPart> sourceEditParts) {
    IPreferenceStore preferences = DesignerPlugin.getPreferences();
    if (preferences.getBoolean(IPreferenceConstants.P_EDITOR_GOTO_DEFINITION_ON_SELECTION)
        && !sourceEditParts.isEmpty()) {
      EditPart primaryEditPart = sourceEditParts.get(sourceEditParts.size() - 1);
      if (primaryEditPart.getModel() instanceof ObjectInfo) {
        ObjectInfo objectInfo = (ObjectInfo) primaryEditPart.getModel();
        if (objectInfo instanceof HasSourcePosition) {
          HasSourcePosition hasSourcePosition = (HasSourcePosition) primaryEditPart.getModel();
          int position = hasSourcePosition.getSourcePosition();
          IDesignPageSite.Helper.getSite(objectInfo).showSourcePosition(position);
        }
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link TreeViewer} used to display components tree.
   */
  public TreeViewer getTreeViewer() {
    return m_viewer;
  }

  /**
   * Sets the {@link IEditPartViewer} and root {@link ObjectInfo} that should be bound to components
   * tree.
   */
  public void setInput(IEditPartViewer editPartViewer, ObjectInfo rootObject) {
    // set root
    m_rootObject = rootObject;
    // set EditPart viewer
    {
      if (m_graphicalViewer != null) {
        m_graphicalViewer.removeSelectionChangedListener(m_selectionListener_Graphical);
      }
      m_graphicalViewer = editPartViewer;
      m_graphicalViewer.addSelectionChangedListener(m_selectionListener_Graphical);
    }
    // configure Tree viewer
    {
      m_viewer.setEditDomain(m_graphicalViewer.getEditDomain());
      m_viewer.setEditPartFactory(EditPartFactory.INSTANCE);
    }
    // set context menu
    m_viewer.setContextMenu(m_graphicalViewer.getContextMenu());
    // set components tree
    if (m_rootObject != null) {
      DesignPageSite site = DesignPageSite.Helper.getSite(m_rootObject);
      site.setComponentsTree(new ComponentsTreeWrapper(m_viewer));
    }
    // refresh objects viewer
    m_viewer.removeSelectionChangedListener(m_selectionListener_Tree);
    try {
      m_viewer.setInput(m_rootObject);
    } finally {
      m_viewer.addSelectionChangedListener(m_selectionListener_Tree);
    }
  }

  /**
   * @return the <em>models</em> {@link ISelectionProvider} for this {@link Composite}.
   */
  public ISelectionProvider getSelectionProvider() {
    return new EditPartsSelectionProvider(m_viewer);
  }
}
