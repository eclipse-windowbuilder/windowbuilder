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

import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.internal.draw2d.IPreferredSizeProvider;
import org.eclipse.wb.internal.draw2d.scroll.ScrollModel;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * @author lobas_av
 * @coverage gef.graphical
 */
public class HeaderGraphicalViewer extends GraphicalViewer {
  private GraphicalViewer m_mainViewer;
  private final boolean m_horizontal;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public HeaderGraphicalViewer(Composite parent, boolean horizontal) {
    this(parent, SWT.NONE, horizontal);
  }

  public HeaderGraphicalViewer(Composite parent, int style, boolean horizontal) {
    super(parent, checkStyles(style));
    m_horizontal = horizontal;
  }

  private static final int checkStyles(int styles) {
    // ignore horizontal scroll style
    if ((styles & SWT.H_SCROLL) != 0) {
      styles |= ~SWT.H_SCROLL;
    }
    // ignore vertical scroll style
    if ((styles & SWT.V_SCROLL) != 0) {
      styles |= ~SWT.V_SCROLL;
    }
    return styles;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public void setMainViewer(GraphicalViewer mainViewer) {
    // set main viewer
    m_mainViewer = mainViewer;
    // set EditDomain
    setEditDomain(m_mainViewer.getEditDomain());
    // configure canvas
    if (m_horizontal) {
      setHorizontallHook();
    } else {
      setVerticalHook();
    }
  }

  private void setHorizontallHook() {
    // configure root preferred size
    getRootFigureInternal().setPreferredSizeProvider(new IPreferredSizeProvider() {
      public Dimension getPreferredSize(Dimension originalPreferredSize) {
        return new Dimension(m_mainViewer.getRootFigureInternal().getPreferredSize().width
            + m_mainViewer.m_canvas.getVerticalBar().getSize().x, originalPreferredSize.height);
      }
    });
    // configure scrolling
    m_mainViewer.m_canvas.getHorizontalScrollModel().addSelectionListener(
        new ScrollModel.ISelectionListener() {
          public void setSelection(int newSelection) {
            m_canvas.getHorizontalScrollModel().setSelection(newSelection);
            getRootFigureInternal().repaint();
          }
        });
  }

  private void setVerticalHook() {
    // configure root preferred size
    getRootFigureInternal().setPreferredSizeProvider(new IPreferredSizeProvider() {
      public Dimension getPreferredSize(Dimension originalPreferredSize) {
        return new Dimension(originalPreferredSize.width,
            m_mainViewer.getRootFigureInternal().getPreferredSize().height
                + m_mainViewer.m_canvas.getHorizontalBar().getSize().y);
      }
    });
    // configure scrolling
    m_mainViewer.m_canvas.getVerticalScrollModel().addSelectionListener(
        new ScrollModel.ISelectionListener() {
          public void setSelection(int newSelection) {
            m_canvas.getVerticalScrollModel().setSelection(newSelection);
            getRootFigureInternal().repaint();
          }
        });
  }
}