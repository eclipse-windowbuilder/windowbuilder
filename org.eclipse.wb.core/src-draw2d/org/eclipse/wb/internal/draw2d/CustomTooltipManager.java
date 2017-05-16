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

import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;

/**
 * Helper class for displaying tooltip's.
 *
 * @author lobas_av
 * @coverage gef.draw2d
 */
public final class CustomTooltipManager implements ICustomTooltipSite {
  private final FigureCanvas m_canvas;
  private final EventManager m_eventManager;
  private Shell m_tooltipShell;
  private Figure m_tooltipFigure;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CustomTooltipManager(FigureCanvas canvas, EventManager eventManager) {
    m_canvas = canvas;
    m_eventManager = eventManager;
    // tooltip show/hide behavior listeners
    m_canvas.addMouseTrackListener(new MouseTrackListener() {
      public void mouseHover(MouseEvent event) {
        if (event.stateMask == 0
            && (m_tooltipFigure == null || m_tooltipFigure != m_eventManager.getCursorFigure())) {
          handleShowCustomTooltip(event.x, event.y);
        }
      }

      public void mouseExit(MouseEvent event) {
        Control cursorControl = Display.getCurrent().getCursorControl();
        if (cursorControl == null || cursorControl.getShell() != m_tooltipShell) {
          hideTooltip();
        }
      }

      public void mouseEnter(MouseEvent event) {
      }
    });
    ScrollBar verticalBar = m_canvas.getVerticalBar();
    if (verticalBar != null) {
      verticalBar.addListener(SWT.Selection, new Listener() {
        public void handleEvent(Event event) {
          hideTooltip();
        }
      });
    }
    m_canvas.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e) {
        hideTooltip();
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ICustomTooltipSide
  //
  ////////////////////////////////////////////////////////////////////////////
  public void hideTooltip() {
    if (m_tooltipShell != null && !m_tooltipShell.isDisposed()) {
      m_tooltipShell.close();
      m_tooltipShell.dispose();
    }
    m_tooltipShell = null;
    m_tooltipFigure = null;
  }

  public Listener getHideListener() {
    return m_hideListener;
  }

  private final Listener m_hideListener = new Listener() {
    public void handleEvent(Event event) {
      Control tooltipControl = (Control) event.widget;
      switch (event.type) {
        case SWT.MouseDown : {
          // convert location from tooltip to canvas
          Point p = tooltipControl.toDisplay(event.x, event.y);
          p = m_canvas.toControl(p);
          // send MouseDown to canvas
          Event newEvent = new Event();
          newEvent.x = p.x;
          newEvent.y = p.y;
          m_canvas.notifyListeners(SWT.MouseDown, newEvent);
          // hide tooltip
          hideTooltip();
          break;
        }
        case SWT.MouseExit :
          hideTooltip();
          break;
      }
    }
  };

  ////////////////////////////////////////////////////////////////////////////
  //
  // Custom Tooltip
  //
  ////////////////////////////////////////////////////////////////////////////
  protected final void handleShowCustomTooltip(int mouseX, int mouseY) {
    hideTooltip();
    Figure cursorFigure = m_eventManager.getCursorFigure();
    if (cursorFigure != null) {
      ICustomTooltipProvider tooltipProvider = cursorFigure.getCustomTooltipProvider();
      if (tooltipProvider != null) {
        m_tooltipFigure = cursorFigure;
        //
        m_tooltipShell =
            new Shell(m_canvas.getShell(), SWT.NO_FOCUS | SWT.ON_TOP | SWT.TOOL | SWT.SINGLE);
        GridLayoutFactory.create(m_tooltipShell).noMargins();
        //
        m_tooltipShell.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_INFO_FOREGROUND));
        m_tooltipShell.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
        //
        Control tooltipControl =
            tooltipProvider.createTooltipControl(m_tooltipShell, this, cursorFigure);
        //
        if (tooltipControl == null) {
          hideTooltip();
        } else {
          // calculate tooltip size
          // for non-windows systems the tooltip may have invalid tooltip bounds
          // because some widget's API functions may fail if tooltip content is not visible
          // ex., on MacOSX tree widget's items has zero bounds since they are not yet visible.
          // the workaround is to preset tooltip size to big values before any computeSize called.
          if (!EnvironmentUtils.IS_WINDOWS) {
            m_tooltipShell.setSize(1000, 1000);
          }
          Point tooltipSize = m_tooltipShell.computeSize(SWT.DEFAULT, SWT.DEFAULT);
          // calculate tooltip location
          Point tooltipLocation = m_canvas.toDisplay(mouseX - 5, mouseY + 10);
          Rectangle clientArea = Display.getCurrent().getClientArea();
          if (tooltipLocation.x + tooltipSize.x >= clientArea.width) {
            tooltipLocation.x = clientArea.width - tooltipSize.x - 1;
          }
          if (tooltipLocation.y + tooltipSize.y >= clientArea.height) {
            tooltipLocation.y = clientArea.height - tooltipSize.y - 1;
          }
          // set location/size and open
          m_tooltipShell.setBounds(
              tooltipLocation.x,
              tooltipLocation.y,
              tooltipSize.x,
              tooltipSize.y);
          tooltipProvider.show(m_tooltipShell);
        }
      }
    }
  }
}