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
package org.eclipse.wb.internal.gef.tree;

import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.internal.draw2d.EventManager;
import org.eclipse.wb.internal.gef.core.EditDomain;
import org.eclipse.wb.internal.gef.tree.dnd.TreeDropListener;
import org.eclipse.wb.internal.gef.tree.dnd.TreeTransfer;
import org.eclipse.wb.os.OSSupport;

import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * @author lobas_av
 * @coverage gef.tree
 */
final class TreeEventManager
    implements
      KeyListener,
      MouseListener,
      MouseMoveListener,
      MouseTrackListener {
  private final Tree m_tree;
  private final IEditPartViewer m_viewer;
  private EditDomain m_domain;
  final TreeDropListener m_dropListener;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TreeEventManager(Tree tree, IEditPartViewer viewer) {
    m_tree = tree;
    m_viewer = viewer;
    // add listeners
    Object listener =
        EventManager.createListenerProxy(this, new Class[]{
            KeyListener.class,
            MouseListener.class,
            MouseMoveListener.class,
            MouseTrackListener.class});
    m_tree.addKeyListener((KeyListener) listener);
    m_tree.addMouseListener((MouseListener) listener);
    m_tree.addMouseMoveListener((MouseMoveListener) listener);
    m_tree.addMouseTrackListener((MouseTrackListener) listener);
    // add DND listeners
    new DragSource(m_tree, DND.DROP_MOVE).setTransfer(new Transfer[]{TreeTransfer.INSTANCE});
    m_dropListener = new TreeDropListener(m_viewer);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public void setDomain(EditDomain domain) {
    m_domain = domain;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // KeyListener
  //
  ////////////////////////////////////////////////////////////////////////////
  public void keyPressed(KeyEvent event) {
    if (m_domain != null) {
      m_domain.keyPressed(event, m_viewer);
    }
  }

  public void keyReleased(KeyEvent event) {
    if (m_domain != null) {
      m_domain.keyReleased(event, m_viewer);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // MouseListener
  //
  ////////////////////////////////////////////////////////////////////////////
  public void mouseDoubleClick(MouseEvent event) {
    if (m_domain != null) {
      m_domain.mouseDoubleClick(event, m_viewer);
    }
  }

  public void mouseDown(MouseEvent event) {
    if (isPlusMinusClick(m_tree, event.x, event.y)) {
      return;
    }
    // OK, send to domain
    if (m_domain != null) {
      m_domain.mouseDown(event, m_viewer);
    }
  }

  public void mouseUp(MouseEvent event) {
    if (m_domain != null) {
      m_domain.mouseUp(event, m_viewer);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // MouseMoveListener
  //
  ////////////////////////////////////////////////////////////////////////////
  public void mouseMove(MouseEvent event) {
    if (m_domain != null) {
      if ((event.stateMask & EventManager.ANY_BUTTON) != 0) {
        m_domain.mouseDrag(event, m_viewer);
      } else {
        m_domain.mouseMove(event, m_viewer);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // MouseTrackListener
  //
  ////////////////////////////////////////////////////////////////////////////
  public void mouseEnter(MouseEvent event) {
    if (m_domain != null) {
      m_domain.viewerEntered(event, m_viewer);
    }
  }

  public void mouseExit(MouseEvent event) {
    if (m_domain != null) {
      m_domain.viewerExited(event, m_viewer);
    }
  }

  public void mouseHover(MouseEvent event) {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if pointer is over {@link TreeItem} plus/minus sign.
   */
  private static boolean isPlusMinusClick(Tree tree, int x, int y) {
    return OSSupport.get().isPlusMinusTreeClick(tree, x, y);
  }
}