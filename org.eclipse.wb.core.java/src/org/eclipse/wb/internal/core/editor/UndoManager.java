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

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.editor.structure.components.IComponentsTree;
import org.eclipse.wb.internal.core.utils.ast.IASTEditorCommitListener;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.BufferChangedEvent;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IBufferChangedListener;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;

import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * Manager for handling undo/redo modifications in {@link ICompilationUnit}.
 *
 * @author scheglov_ke
 * @coverage core.editor
 */
public final class UndoManager {
  private final DesignPage m_designPage;
  private final IBuffer m_buffer;
  private final IFile m_unitFile;
  @SuppressWarnings("unchecked")
  private final Map<String, int[][]> m_sourceToSelection = new LRUMap(32);
  @SuppressWarnings("unchecked")
  private final Map<String, int[][]> m_dumpToSelection = new LRUMap(32);
  @SuppressWarnings("unchecked")
  private final Map<String, int[][]> m_dumpToExpanded = new LRUMap(32);
  private IComponentsTree m_componentsTree;
  private ISelectionProvider m_selectionProvider;
  private ITreeContentProvider m_componentsProvider;
  private ObjectPathHelper m_objectPathHelper;
  private JavaInfo m_root;
  private String m_currentSource;
  private String m_currentDump;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public UndoManager(DesignPage designPage, ICompilationUnit unit) throws Exception {
    m_designPage = designPage;
    m_buffer = unit.getBuffer();
    m_unitFile = (IFile) unit.getUnderlyingResource();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  private boolean m_active;

  /**
   * Activates {@link UndoManager}, so it starts listening for buffer changes.
   */
  public void activate() {
    if (!m_active) {
      m_active = true;
      if (!StringUtils.equals(m_currentSource, m_buffer.getContents())) {
        refreshDesignerEditor();
      }
      addBufferListener();
    }
  }

  /**
   * Deactivates {@link UndoManager}, so it stops listening for buffer changes.
   */
  public void deactivate() {
    if (m_active) {
      m_active = false;
      removeBufferListener();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editor
  //
  ////////////////////////////////////////////////////////////////////////////
  private final IASTEditorCommitListener m_editorListener = new IASTEditorCommitListener() {
    public void aboutToCommit() {
      removeBufferListener();
    }

    public boolean canEditBaseFile() {
      if (m_unitFile.isReadOnly()) {
        ResourcesPlugin.getWorkspace().validateEdit(
            new IFile[]{m_unitFile},
            DesignerPlugin.getShell());
        return !m_unitFile.isReadOnly();
      }
      return true;
    }

    public void commitDone() {
      addBufferListener();
    }
  };
  private final ObjectEventListener m_refreshListener = new ObjectEventListener() {
    @Override
    public void refreshBeforeCreate() throws Exception {
      removeSelectionListener();
    }

    @Override
    public void refreshed2() throws Exception {
      addSelectionListener();
      rememberSource();
      rememberDump();
      rememberState();
    }
  };

  /**
   * Sets the new root {@link JavaInfo} in editor.
   */
  public void setRoot(JavaInfo root) {
    m_root = root;
    m_root.getEditor().setCommitListener(m_editorListener);
    m_root.addBroadcastListener(m_refreshListener);
    // get components tree
    {
      DesignPageSite site = DesignPageSite.Helper.getSite(m_root);
      m_componentsTree = site.getComponentTree();
      m_componentsProvider = m_componentsTree.getContentProvider();
      m_selectionProvider = m_componentsTree.getSelectionProvider();
      m_objectPathHelper = new ObjectPathHelper(m_componentsProvider);
      // listen for tree expansion state
      m_componentsTree.setExpandListener(new Runnable() {
        public void run() {
          Display.getCurrent().asyncExec(new Runnable() {
            public void run() {
              rememberState();
            }
          });
        }
      });
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Selection listener
  //
  ////////////////////////////////////////////////////////////////////////////
  private final ISelectionChangedListener m_selectionListener = new ISelectionChangedListener() {
    public void selectionChanged(SelectionChangedEvent event) {
      rememberState();
    }
  };

  /**
   * Adds {@link ISelectionChangedListener}.
   */
  private void addSelectionListener() {
    m_selectionProvider.addSelectionChangedListener(m_selectionListener);
  }

  /**
   * Removes {@link ISelectionChangedListener}
   */
  private void removeSelectionListener() {
    if (m_selectionProvider != null) {
      m_selectionProvider.removeSelectionChangedListener(m_selectionListener);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Selection
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Remembers current source.
   */
  private void rememberSource() {
    m_currentSource = m_buffer.getContents();
  }

  /**
   * Remembers current dump.
   */
  private void rememberDump() {
    m_currentDump = ObjectPathHelper.getObjectsDump(m_root, 0);
  }

  /**
   * Remembers selected/expanded elements for current source.
   */
  private void rememberState() {
    // selection
    {
      // prepare selected objects
      Object[] selectedObjects;
      {
        IStructuredSelection structuredSelection =
            (IStructuredSelection) m_selectionProvider.getSelection();
        selectedObjects = structuredSelection.toArray();
      }
      // remember
      int[][] paths = m_objectPathHelper.getObjectsPaths(selectedObjects);
      m_sourceToSelection.put(m_currentSource, paths);
      m_dumpToSelection.put(m_currentDump, paths);
    }
    // expanded
    {
      Object[] expandedObjects = m_componentsTree.getExpandedElements();
      int[][] paths = m_objectPathHelper.getObjectsPaths(expandedObjects);
      m_dumpToExpanded.put(m_currentDump, paths);
    }
  }

  /**
   * Tries to restore selected/expanded elements for current source.
   */
  private void restoreState() {
    restoreSelection();
    restoreExpanded();
  }

  /**
   * Tries to restore selected elements for current source.
   */
  private void restoreSelection() {
    // prepare selection
    int[][] paths;
    {
      // get "source based" selection
      paths = m_sourceToSelection.get(m_currentSource);
      // if no "source based" selection, use "dump based" one
      if (paths == null) {
        paths = m_dumpToSelection.get(m_currentDump);
      }
    }
    // do restore
    if (paths != null) {
      removeSelectionListener();
      try {
        Object[] objects = m_objectPathHelper.getObjectsForPaths(paths);
        m_selectionProvider.setSelection(new StructuredSelection(objects));
      } finally {
        addSelectionListener();
      }
    }
  }

  /**
   * Tries to restore expanded elements for current source.
   */
  private void restoreExpanded() {
    int[][] paths = m_dumpToExpanded.get(m_currentDump);
    // do restore
    if (paths != null) {
      Object[] objects = m_objectPathHelper.getObjectsForPaths(paths);
      m_componentsTree.setExpandedElements(objects);
    }
    // if no expanded element, perform default expansion
    if (m_componentsTree.getExpandedElements().length == 0) {
      List<Object> expandedElements = Lists.newArrayList();
      Object element = m_root;
      while (true) {
        expandedElements.add(element);
        Object[] children = m_componentsProvider.getChildren(element);
        if (children.length != 1) {
          break;
        }
        element = children[0];
      }
      m_componentsTree.setExpandedElements(expandedElements.toArray());
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Buffer listener
  //
  ////////////////////////////////////////////////////////////////////////////
  private int m_bufferChangeCount = 0;
  private final IBufferChangedListener m_bufferListener = new IBufferChangedListener() {
    public void bufferChanged(BufferChangedEvent event) {
      if (!m_designPage.isActiveEditor()) {
        return;
      }
      if (event.getText() != null || event.getLength() != 0) {
        scheduleRefresh_onBufferChange();
      }
    }
  };

  private void scheduleRefresh_onBufferChange() {
    final int bufferChangeCount = ++m_bufferChangeCount;
    Runnable runnable = new Runnable() {
      public void run() {
        if (isStillInSave()) {
          Display.getDefault().timerExec(1, this);
          return;
        }
        if (bufferChangeCount == m_bufferChangeCount) {
          refreshDesignerEditor();
        }
      }

      /**
       * Save operation may run event loops, so wait for finishing "doSave()" to reparse when save
       * is complete. This allows also avoid deadlock in case if we will join to wait for finishing
       * auto build job.
       */
      private boolean isStillInSave() {
        String editorClassName = "org.eclipse.wb.internal.core.editor.multi.DesignerEditor";
        StackTraceElement[] elements = new Exception().getStackTrace();
        for (StackTraceElement element : elements) {
          if (element.getClassName().equals(editorClassName)) {
            if (element.getMethodName().equals("doSave")) {
              return true;
            }
          }
        }
        return false;
      }
    };
    Display.getDefault().asyncExec(runnable);
  }

  /**
   * Adds {@link IBufferChangedListener}.
   */
  private void addBufferListener() {
    m_buffer.addBufferChangedListener(m_bufferListener);
  }

  /**
   * Removes {@link IBufferChangedListener}.
   */
  private void removeBufferListener() {
    m_buffer.removeBufferChangedListener(m_bufferListener);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Informs {@link DesignPage} that buffer was changed and reparse required.
   */
  void refreshDesignerEditor() {
    rememberSource();
    // refresh viewer
    removeSelectionListener();
    if (m_designPage.internal_refreshGEF()) {
      addSelectionListener();
      // restore selection
      rememberDump();
      restoreState();
    }
  }
}
