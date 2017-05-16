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
package org.eclipse.wb.core.controls;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;

/**
 * {@link PageBook} is a {@link Composite} where only a single {@link Control} is visible at a time.
 * It is similar to a notebook, but without tabs.
 *
 * @author scheglov_ke
 * @coverage core.control
 */
public final class PageBook extends Composite {
  private Control m_currentPage;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public PageBook(Composite parent, int style) {
    super(parent, style);
    setLayout(new PageBookLayout());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the currently displaying page.
   */
  public Control getPage() {
    return m_currentPage;
  }

  /**
   * Shows the given page. This method has no effect if the given page is not contained in this
   * {@link PageBook}.
   *
   * @param page
   *          the page to show
   */
  public void showPage(Control page) {
    // check page to show
    if (page == m_currentPage) {
      return;
    }
    if (page.getParent() != this) {
      return;
    }
    // replace page
    Control oldPage = m_currentPage;
    m_currentPage = page;
    // show new page
    if (page != null) {
      if (!page.isDisposed()) {
        page.setVisible(true);
        layout(true);
      }
    }
    // hide old *after* new page has been made visible in order to avoid flashing
    if (oldPage != null && !oldPage.isDisposed()) {
      oldPage.setVisible(false);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Internal layout
  //
  ////////////////////////////////////////////////////////////////////////////
  private class PageBookLayout extends Layout {
    @Override
    protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
      if (wHint != SWT.DEFAULT && hHint != SWT.DEFAULT) {
        return new Point(wHint, hHint);
      }
      Point result = null;
      if (m_currentPage != null) {
        result = m_currentPage.computeSize(wHint, hHint, flushCache);
      } else {
        result = new Point(0, 0);
      }
      if (wHint != SWT.DEFAULT) {
        result.x = wHint;
      }
      if (hHint != SWT.DEFAULT) {
        result.y = hHint;
      }
      return result;
    }

    @Override
    protected void layout(Composite composite, boolean flushCache) {
      if (m_currentPage != null) {
        m_currentPage.setBounds(composite.getClientArea());
      }
    }
  }
}
