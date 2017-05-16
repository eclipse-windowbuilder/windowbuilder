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
package org.eclipse.wb.core.editor;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.editor.DesignPageSite;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * Provides access to the {@link IDesignPage}.
 *
 * @author scheglov_ke
 * @coverage core.editor
 */
public interface IDesignPageSite {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Operations
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Moves cursor to given position in Java editor.
   */
  void showSourcePosition(int position);

  /**
   * Moves cursor to given position in Java editor and opens Java editor.
   */
  void openSourcePosition(int position);

  /**
   * Handles any unexpected {@link Exception}.
   */
  void handleException(Throwable e);

  /**
   * Performs reparse for this editor. Note, that this is last method that should be invoked for
   * this {@link IDesignPageSite} and in this {@link ObjectInfo} hierarchy.
   */
  void reparse();
  ////////////////////////////////////////////////////////////////////////////
  //
  // Helper
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Helper for accessing {@link IDesignPageSite}.
   *
   * @author scheglov_ke
   */
  public static class Helper {
    private static final String KEY = "key_DesignPageSite";

    /**
     * @return {@link IDesignPageSite} for given {@link ObjectInfo}.
     */
    public static IDesignPageSite getSite(ObjectInfo objectInfo) {
      return (IDesignPageSite) objectInfo.getRoot().getArbitraryValue(KEY);
    }

    /**
     * @return the {@link IProgressMonitor} to use for displaying progress during parsing (initiated
     *         by {@link IDesignPage}). If parsing was complete, or we run tests, then
     *         {@link NullProgressMonitor} will be returned. Never returns <code>null</code>.
     */
    public static IProgressMonitor getProgressMonitor() {
      return DesignPageSite.getProgressMonitor();
    }
  }
}
