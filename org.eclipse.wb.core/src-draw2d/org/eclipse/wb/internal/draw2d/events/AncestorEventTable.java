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
package org.eclipse.wb.internal.draw2d.events;

import com.google.common.collect.Lists;

import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.events.IAncestorListener;
import org.eclipse.wb.draw2d.events.IFigureListener;

import java.util.List;

/**
 * Utils class use for receiving changes in the ancestor hierarchy of the listening {@link Figure}.
 *
 * @author lobas_av
 * @coverage gef.draw2d
 */
public class AncestorEventTable implements IFigureListener {
  private final Figure m_figure;
  private List<IAncestorListener> m_listeners;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AncestorEventTable(Figure figure) {
    m_figure = figure;
    hookFigure(m_figure);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IFigureListener
  //
  ////////////////////////////////////////////////////////////////////////////
  public void figureMoved(Figure source) {
    for (IAncestorListener listener : m_listeners) {
      listener.ancestorMoved(source);
    }
  }

  public void figureReparent(Figure source, Figure oldParent, Figure newParent) {
    if (oldParent != null) {
      unhookFigure(oldParent);
    }
    if (newParent != null) {
      hookFigure(newParent);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Ancestor
  //
  ////////////////////////////////////////////////////////////////////////////
  private void hookFigure(Figure figure) {
    for (Figure ancestor = figure; ancestor != null; ancestor = ancestor.getParent()) {
      ancestor.addFigureListener(this);
    }
  }

  private void unhookFigure(Figure figure) {
    for (Figure ancestor = figure; ancestor != null; ancestor = ancestor.getParent()) {
      ancestor.removeFigureListener(this);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Unregisters figure listeners, so that this class will no longer receive notification of
   * ancestor events.
   */
  public void unhookFigure() {
    unhookFigure(m_figure);
  }

  /**
   * Return <code>true</code> if this event table contains no listeners.
   */
  public boolean isEmpty() {
    return m_listeners.isEmpty();
  }

  /**
   * Add {@link IAncestorListener} listener.
   */
  public void addAncestorListener(IAncestorListener listener) {
    if (m_listeners == null) {
      m_listeners = Lists.newArrayList();
    }
    m_listeners.add(listener);
  }

  /**
   * Remove {@link IAncestorListener} listener.
   */
  public void removeAncestorListener(IAncestorListener listener) {
    m_listeners.remove(listener);
  }
}