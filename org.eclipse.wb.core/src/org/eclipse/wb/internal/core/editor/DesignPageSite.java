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

import org.eclipse.wb.core.editor.IDesignPageSite;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.editor.structure.components.IComponentsTree;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.TreeViewer;

/**
 * Provides access to the {@link DesignPage}.
 *
 * @author scheglov_ke
 * @coverage core.editor
 */
public abstract class DesignPageSite implements IDesignPageSite {
  public static final DesignPageSite EMPTY = new DesignPageSite() {
  };

  ////////////////////////////////////////////////////////////////////////////
  //
  // Operations
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Moves cursor to given position in Java editor.
   */
  public void showSourcePosition(int position) {
  }

  /**
   * Moves cursor to given position in Java editor and opens Java editor.
   */
  public void openSourcePosition(int position) {
  }

  // TODO(scheglov)
//  /**
//   * Highlight in editor lines with visited {@link ASTNode}s.
//   */
//  public void highlightVisitedNodes(Collection<ASTNode> nodes) {
//  }
  /**
   * Handles any unexpected {@link Exception}.
   */
  public void handleException(Throwable e) {
  }

  /**
   * Performs reparse for this editor. Note, that this is last method that should be invoked for
   * this {@link DesignPageSite} and in this {@link ObjectInfo} hierarchy.
   */
  public void reparse() {
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Components tree
  //
  ////////////////////////////////////////////////////////////////////////////
  private IComponentsTree m_componentTree;

  /**
   * @return the {@link IComponentsTree} for accessing components tree.
   */
  public final IComponentsTree getComponentTree() {
    return m_componentTree;
  }

  /**
   * Sets the {@link TreeViewer} for accessing components tree.
   */
  public final void setComponentsTree(IComponentsTree componentTree) {
    m_componentTree = componentTree;
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Helper
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Helper for accessing {@link DesignPageSite}.
   *
   * @author scheglov_ke
   */
  public static class Helper {
    private static final String KEY = "key_DesignPageSite";

    /**
     * @return {@link DesignPageSite} for given {@link ObjectInfo}.
     */
    public static DesignPageSite getSite(ObjectInfo objectInfo) {
      return (DesignPageSite) objectInfo.getRoot().getArbitraryValue(KEY);
    }

    /**
     * Sets the {@link IDesignPageSite} for given {@link ObjectInfo}.
     */
    public static void setSite(ObjectInfo objectInfo, IDesignPageSite site) {
      objectInfo.getRoot().putArbitraryValue(KEY, site);
    }
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Progress
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final IProgressMonitor NULL_PROGRESS_MONITOR = new NullProgressMonitor();
  private static IProgressMonitor m_progressMonitor;

  /**
   * @return the {@link IProgressMonitor} to use for displaying progress during parsing (initiated
   *         by {@link IDesignPage}). If parsing was complete, or we run tests, then
   *         {@link NullProgressMonitor} will be returned. Never returns <code>null</code>.
   */
  public static IProgressMonitor getProgressMonitor() {
    return m_progressMonitor != null ? m_progressMonitor : NULL_PROGRESS_MONITOR;
  }

  /**
   * Sets the {@link IProgressMonitor} to return from
   * {@link IDesignPageSite.Helper#getProgressMonitor()}.
   *
   * @param progressMonitor
   *          the {@link IProgressMonitor} to use, may be <code>null</code>.
   */
  public static void setProgressMonitor(IProgressMonitor progressMonitor) {
    m_progressMonitor = progressMonitor;
  }
}
