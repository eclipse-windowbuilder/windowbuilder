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

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.draw2d.IColorConstants;
import org.eclipse.wb.internal.core.gef.header.HeadersContainerEditPart;
import org.eclipse.wb.internal.core.gef.header.HeadersContextMenuProvider;
import org.eclipse.wb.internal.core.gef.header.HeadersEditPartFactory;
import org.eclipse.wb.internal.gef.graphical.GraphicalViewer;
import org.eclipse.wb.internal.gef.graphical.HeaderGraphicalViewer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * The {@link Composite} with horizontal/vertical {@link HeaderGraphicalViewer}'s and main
 * {@link GraphicalViewer}.
 *
 * @author scheglov_ke
 * @coverage core.editor
 */
public final class ViewersComposite extends Composite {
  private static final int HEADER_SIZE = 15;
  private final GraphicalViewer m_viewer;
  private final HeaderGraphicalViewer m_horizontalViewer;
  private final HeaderGraphicalViewer m_verticalViewer;
  private HeadersContainerEditPart m_headersContainerHorizontal;
  private HeadersContainerEditPart m_headersContainerVertical;
  ////////////////////////////////////////////////////////////////////////////
  //
  // Composite
  //
  ////////////////////////////////////////////////////////////////////////////
  public ViewersComposite(Composite parent, int style) {
    super(parent, style);
    // create header viewers
    m_horizontalViewer = new HeaderGraphicalViewer(this, true);
    m_verticalViewer = new HeaderGraphicalViewer(this, false);
    m_viewer = new GraphicalViewer(this);
    // add listeners
    {
      addListener(SWT.Resize, new Listener() {
        public void handleEvent(Event event) {
          layout();
        }
      });
      addListener(SWT.Paint, new Listener() {
        public void handleEvent(Event event) {
          GC gc = event.gc;
          gc.setForeground(IColorConstants.buttonDarker);
          //
          Rectangle clientArea = getClientArea();
          gc.drawLine(HEADER_SIZE, HEADER_SIZE, clientArea.width, HEADER_SIZE);
          gc.drawLine(HEADER_SIZE, HEADER_SIZE, HEADER_SIZE, clientArea.height);
        }
      });
    }
    // set context menu for headers
    {
      m_horizontalViewer.setContextMenu(new HeadersContextMenuProvider(m_horizontalViewer));
      m_verticalViewer.setContextMenu(new HeadersContextMenuProvider(m_verticalViewer));
    }
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Binds headers and main {@link GraphicalViewer}'s.
   */
  public void bindViewers() {
    HeadersEditPartFactory editPartFactory = new HeadersEditPartFactory();
    //
    m_horizontalViewer.setMainViewer(m_viewer);
    m_horizontalViewer.setEditPartFactory(editPartFactory);
    m_headersContainerHorizontal = new HeadersContainerEditPart(m_viewer, true);
    m_horizontalViewer.setInput(m_headersContainerHorizontal);
    //
    m_verticalViewer.setMainViewer(m_viewer);
    m_verticalViewer.setEditPartFactory(editPartFactory);
    m_headersContainerVertical = new HeadersContainerEditPart(m_viewer, false);
    m_verticalViewer.setInput(m_headersContainerVertical);
  }
  /**
   * Sets the root {@link ObjectInfo}.
   */
  public void setRoot(ObjectInfo rootObject) {
    rootObject.addBroadcastListener(new ObjectEventListener() {
      @Override
      public void refreshed2() throws Exception {
        m_headersContainerHorizontal.refreshHeaders();
        m_headersContainerVertical.refreshHeaders();
      }
    });
  }
  /**
   * @return the main {@link GraphicalViewer}
   */
  public GraphicalViewer getViewer() {
    return m_viewer;
  }
  /**
   * @return the horizontal {@link HeaderGraphicalViewer}
   */
  public HeaderGraphicalViewer getHorizontalViewer() {
    return m_horizontalViewer;
  }
  /**
   * @return the vertical {@link HeaderGraphicalViewer}.
   */
  public HeaderGraphicalViewer getVerticalViewer() {
    return m_verticalViewer;
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Control
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean setFocus() {
    return m_viewer.getControl().setFocus();
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Layout
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void layout() {
    Rectangle clientArea = getClientArea();
    int x = HEADER_SIZE + 1;
    int y = HEADER_SIZE + 1;
    int width = clientArea.width - x;
    int height = clientArea.height - y;
    //
    m_horizontalViewer.getControl().setBounds(x, 0, width, HEADER_SIZE);
    m_verticalViewer.getControl().setBounds(0, y, HEADER_SIZE, height);
    m_viewer.getControl().setBounds(x, y, width, height);
  }
}
