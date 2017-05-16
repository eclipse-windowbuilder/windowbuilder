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
package org.eclipse.wb.internal.draw2d;

import org.eclipse.wb.draw2d.geometry.Rectangle;

import org.eclipse.swt.widgets.Display;

/**
 * Refresh manager handle the job of repainting figures.<br>
 * Asynchronously updates the affected figures.
 *
 * @author lobas_av
 * @coverage gef.draw2d
 */
public class RefreshManager implements Runnable {
  private final FigureCanvas m_canvas;
  private final Rectangle m_dirtyRegion = new Rectangle();
  private boolean m_refreshWork;
  private boolean m_requestWork;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public RefreshManager(FigureCanvas canvas) {
    m_canvas = canvas;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refreshing
  //
  ////////////////////////////////////////////////////////////////////////////
  private synchronized void refresh() {
    // check if refresh already works
    if (m_refreshWork || m_canvas.isDisposed()) {
      return;
    }
    // work refresh
    try {
      m_refreshWork = true;
      m_requestWork = false;
      m_canvas.handleRefresh(
          m_dirtyRegion.x,
          m_dirtyRegion.y,
          m_dirtyRegion.width,
          m_dirtyRegion.height);
      m_dirtyRegion.setBounds(0, 0, 0, 0);
    } finally {
      m_refreshWork = false;
    }
  }

  /**
   * Send repaint request for <code>FigureCanvas</code>. Adds a dirty region (defined by the
   * rectangle <i>x, y, w, h</i>) to the update queue.
   */
  public synchronized void refreshRequest(int x, int y, int width, int height) {
    m_dirtyRegion.union(x, y, width, height);
    //
    if (!m_requestWork) {
      Display.getCurrent().asyncExec(this);
      m_requestWork = true;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Runnable
  //
  ////////////////////////////////////////////////////////////////////////////
  public void run() {
    refresh();
  }
}