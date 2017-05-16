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
package org.eclipse.wb.internal.draw2d.scroll;

import com.google.common.collect.Lists;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;

import java.util.List;

/**
 * ScrollModel represents abstract model for support scrolling.
 *
 * @author lobas_av
 * @coverage gef.draw2d
 */
public abstract class ScrollModel implements Listener {
  private final ScrollBar m_scrollBar;
  protected int m_selection;
  private int m_extent;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ScrollModel(ScrollBar scrollBar) {
    if (scrollBar != null) {
      scrollBar.setMinimum(0);
      scrollBar.setIncrement(1);
      scrollBar.setEnabled(false);
      scrollBar.setVisible(false);
      scrollBar.addListener(SWT.Selection, this);
    }
    m_scrollBar = scrollBar;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ScrollModel
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Returns selection <code>ScrollBar</code> value.
   */
  public int getSelection() {
    return m_selection;
  }

  /**
   * Sets selection value.
   */
  public void setSelection(int selection) {
    m_selection = selection;
  }

  /**
   * Configure <code>ScrollBar</code> when change window size of figure's bounds.
   *
   * @param available
   *          real window size
   * @param preferred
   *          needful figure's size
   */
  public void configure(int available, int preferred) {
    if (m_scrollBar == null) {
      return;
    }
    try {
      if (preferred > available) {
        m_extent = preferred - available;
        m_selection = Math.max(0, Math.min(m_extent, m_selection));
        m_scrollBar.setValues(
            m_selection,
            0,
            preferred,
            available,
            available / 20,
            available * 3 / 4);
        if (m_scrollBar.getEnabled()) {
          return;
        }
        m_scrollBar.setSelection(0);
        m_scrollBar.setEnabled(true);
        m_scrollBar.setVisible(true);
      } else {
        m_scrollBar.setVisible(false);
        m_scrollBar.setEnabled(false);
        m_scrollBar.setSelection(0);
      }
      m_selection = 0;
    } finally {
      // notify selection change
      fireSetSelection(m_selection);
    }
  }

  /**
   * Handle positive change <code>ScrollBar</code> selection. Scroll client area and repaint part of
   * area.
   *
   * @param delta
   *          is absolute distance between <i>newSelection</i> and <i>oldSelection</i>
   * @param newSelection
   *          see comment into {@link ScrollModel#handleEvent(Event)}
   */
  protected abstract void handlePositiveScrolling(int delta, int newSelection);

  /**
   * Handle negative change <code>ScrollBar</code> selection. Scroll client area and repaint part of
   * area.
   *
   * @param delta
   *          is absolute distance between <i>newSelection</i> and <i>oldSelection</i>
   * @param newSelection
   *          see comment into {@link ScrollModel#handleEvent(Event)}
   */
  protected abstract void handleNegativeScrolling(int delta, int newSelection);

  ////////////////////////////////////////////////////////////////////////////
  //
  // Handle scrolling
  //
  ////////////////////////////////////////////////////////////////////////////
  public void handleEvent(Event event) {
    if (m_scrollBar.getEnabled()) {
      int selection = Math.max(0, Math.min(m_extent, m_scrollBar.getSelection()));
      m_scrollBar.setSelection(selection);
      int delta = selection - m_selection;
      if (delta == 0) {
        return;
      }
      /*
       * Method scroll() flushes all deferred paint events and we can't update
       * selection until all of these outstanding events processed.
       * So, new selection value should be stored between scroll() and draw() method invocations.
       */
      if (delta > 0) {
        handlePositiveScrolling(delta, selection);
      } else {
        handleNegativeScrolling(-delta, selection);
      }
      // notify selection change
      fireSetSelection(selection);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Listener
  //
  ////////////////////////////////////////////////////////////////////////////
  private List<ISelectionListener> m_listeners;

  /**
   * Registers the given listener as a {@link ISelectionListener} of this {@link ScrollModel}.
   */
  public void addSelectionListener(ISelectionListener listener) {
    if (m_listeners == null) {
      m_listeners = Lists.newArrayList();
    }
    m_listeners.add(listener);
  }

  /**
   * Unregisters the given listener, so that it will no longer receive notification of
   * {@link ScrollModel} selection events.
   */
  public void removeSelectionListener(ISelectionListener listener) {
    m_listeners.remove(listener);
  }

  private void fireSetSelection(int selection) {
    if (m_listeners != null) {
      for (ISelectionListener listener : m_listeners) {
        listener.setSelection(selection);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Listener class
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * A listener interface for receiving notification that an {@link ScrollModel} selection has
   * change.
   */
  public static interface ISelectionListener {
    /**
     * Called when change selection value.
     */
    void setSelection(int newSelection);
  }
}