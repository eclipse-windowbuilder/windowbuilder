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
package org.eclipse.wb.internal.gef.graphical;

import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.internal.draw2d.EventManager;
import org.eclipse.wb.internal.draw2d.FigureCanvas;
import org.eclipse.wb.internal.gef.core.EditDomain;

import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Cursor;

/**
 * A special event manager that will route events to the {@link EditDomain} when appropriate.
 *
 * @author lobas_av
 * @coverage gef.graphical
 */
public class EditEventManager extends EventManager implements KeyListener {
  private final EditDomain m_domain;
  private final IEditPartViewer m_viewer;
  private boolean m_eventCapture;
  private Cursor m_overrideCursor;
  private MouseEvent m_currentMouseEvent;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public EditEventManager(FigureCanvas canvas, EditDomain domain, IEditPartViewer viewer) {
    super(canvas);
    m_domain = domain;
    m_viewer = viewer;
    // add listeners
    Object listener = createListenerProxy(this, new Class[]{KeyListener.class});
    canvas.addKeyListener((KeyListener) listener);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Cursor
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Set the Cursor.
   */
  @Override
  public void setCursor(Cursor cursor) {
    if (m_overrideCursor == null) {
      super.setCursor(cursor);
    } else {
      super.setCursor(m_overrideCursor);
    }
  }

  /**
   * Set the override Cursor.
   */
  public void setOverrideCursor(Cursor cursor) {
    if (m_overrideCursor != cursor) {
      m_overrideCursor = cursor;
      //
      if (m_overrideCursor == null) {
        // if use figure cursor update mouse figure
        if (m_eventCapture) {
          super.setCursor(null);
        } else {
          updateFigureUnderCursor(m_currentMouseEvent);
          updateCursor();
        }
      } else {
        setCursor(m_overrideCursor);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Handle KeyEvent
  //
  ////////////////////////////////////////////////////////////////////////////
  public void keyPressed(KeyEvent event) {
    m_domain.keyPressed(event, m_viewer);
  }

  public void keyReleased(KeyEvent event) {
    m_domain.keyReleased(event, m_viewer);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Handle MouseEvent
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void mouseDoubleClick(MouseEvent event) {
    m_currentMouseEvent = event;
    //
    if (!m_eventCapture) {
      super.mouseDoubleClick(event);
      if (isEventConsumed()) {
        return;
      }
    }
    //
    m_domain.mouseDoubleClick(event, m_viewer);
  }

  @Override
  public void mouseDown(MouseEvent event) {
    m_viewer.getControl().forceFocus();
    m_currentMouseEvent = event;
    //
    if (!m_eventCapture) {
      super.mouseDown(event);
      if (isEventConsumed()) {
        return;
      }
    }
    //
    m_eventCapture = true;
    m_domain.mouseDown(event, m_viewer);
  }

  @Override
  public void mouseUp(MouseEvent event) {
    m_currentMouseEvent = event;
    //
    if (!m_eventCapture) {
      super.mouseUp(event);
      if (isEventConsumed()) {
        return;
      }
    }
    //
    boolean eventCapture = m_eventCapture;
    m_eventCapture = false;
    m_domain.mouseUp(event, m_viewer);
    // after release capture update mouse figure
    if (eventCapture) {
      updateFigureUnderCursor(event);
    }
  }

  @Override
  public void mouseMove(MouseEvent event) {
    m_currentMouseEvent = event;
    //
    if (!m_eventCapture) {
      super.mouseMove(event);
      if (isEventConsumed()) {
        return;
      }
    }
    //
    if ((event.stateMask & ANY_BUTTON) != 0) {
      m_domain.mouseDrag(event, m_viewer);
    } else {
      m_domain.mouseMove(event, m_viewer);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // MouseTrackListener
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void mouseEnter(MouseEvent event) {
    m_currentMouseEvent = event;
    m_domain.viewerEntered(event, m_viewer);
    updateFigureUnderCursor(event);
  }

  @Override
  public void mouseExit(MouseEvent event) {
    m_eventCapture = false;
    m_currentMouseEvent = event;
    m_domain.viewerExited(event, m_viewer);
    updateFigureUnderCursor(event);
  }

  @Override
  public void mouseHover(MouseEvent event) {
  }
}