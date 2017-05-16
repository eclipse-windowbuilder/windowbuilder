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

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.internal.core.utils.gef.EditPartsContentProvider;
import org.eclipse.wb.internal.core.utils.gef.EditPartsSelectionProvider;
import org.eclipse.wb.internal.core.utils.ui.UiUtils;
import org.eclipse.wb.internal.gef.tree.TreeViewer;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.widgets.TreeItem;

/**
 * Implementation of {@link IComponentsTree} for GEF {@link TreeViewer}.
 *
 * @author scheglov_ke
 * @coverage core.editor.structure
 */
final class ComponentsTreeWrapper implements IComponentsTree {
  private final TreeViewer m_viewer;
  private final ITreeContentProvider m_contentProvider;
  private final ISelectionProvider m_selectionProvider;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ComponentsTreeWrapper(TreeViewer viewer) {
    m_viewer = viewer;
    m_contentProvider = new EditPartsContentProvider(m_viewer);
    m_selectionProvider = new EditPartsSelectionProvider(m_viewer);
    m_viewer.getTree().addTreeListener(new TreeListener() {
      public void treeCollapsed(TreeEvent e) {
        if (m_expandListener != null) {
          m_expandListener.run();
        }
      }

      public void treeExpanded(TreeEvent e) {
        if (m_expandListener != null) {
          m_expandListener.run();
        }
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Providers
  //
  ////////////////////////////////////////////////////////////////////////////
  public ITreeContentProvider getContentProvider() {
    return m_contentProvider;
  }

  public ISelectionProvider getSelectionProvider() {
    return m_selectionProvider;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Expanded
  //
  ////////////////////////////////////////////////////////////////////////////
  private Runnable m_expandListener;

  public Object[] getExpandedElements() {
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

  public void setExpandedElements(Object[] elements) {
    // prepare EditPart's by model elements
    EditPart[] editParts = new EditPart[elements.length];
    for (int i = 0; i < elements.length; i++) {
      Object element = elements[i];
      editParts[i] = m_viewer.getEditPartByModel(element);
    }
    // expand using EditPart's
    UiUtils.setExpandedByData(m_viewer.getTree(), editParts);
  }

  public void setExpandListener(Runnable listener) {
    m_expandListener = listener;
  }
}
