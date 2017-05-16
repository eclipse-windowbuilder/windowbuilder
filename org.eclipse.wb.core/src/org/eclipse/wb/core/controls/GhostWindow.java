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

import org.eclipse.wb.draw2d.geometry.Interval;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.os.OSSupport;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

/**
 * Special {@link Window} that sets "alpha" for its {@link Shell} depending on mouse location.
 *
 * @author mitin_aa
 * @coverage core.control
 */
public abstract class GhostWindow extends Window {
  // constants
  private static final int INITIAL_DISTANCE = 35;
  private static final int ALPHA_MULTIPLIER = 5;
  private static final int MAX_DISTANCE = 100;
  private static final double SQRT_OF_TWO = 1.4142;
  // fields
  private GhostListener m_listener;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  protected GhostWindow(Shell parentShell) {
    super(parentShell);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Manage ghost listener
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void create() {
    super.create();
    m_listener = new GhostListener(getShell());
    DesignerPlugin.getStandardDisplay().addFilter(SWT.MouseMove, m_listener);
    getShell().addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e) {
        DesignerPlugin.getStandardDisplay().removeFilter(SWT.MouseMove, m_listener);
      }
    });
  }

  @Override
  public int open() {
    Shell shell = getShell();
    if (shell == null || shell.isDisposed()) {
      create();
    }
    setup();
    getShell().setVisible(true);
    return 0;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Moves the window into position related to cursor and sets the appropriate initial alpha value.
   */
  private final void setup() {
    Point mouseLocation = DesignerPlugin.getStandardDisplay().getCursorLocation();
    getShell().setLocation(mouseLocation.x + INITIAL_DISTANCE, mouseLocation.y + INITIAL_DISTANCE);
    setAlpha(getShell(), (int) (255 - INITIAL_DISTANCE * ALPHA_MULTIPLIER * SQRT_OF_TWO));
    m_listener.setEnabled(true);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Ghost tracking
  //
  ////////////////////////////////////////////////////////////////////////////
  private static class GhostListener implements Listener {
    private final Shell m_shell;
    private final Display m_display;
    private boolean m_enabled = true;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public GhostListener(Shell shell) {
      m_shell = shell;
      m_display = DesignerPlugin.getStandardDisplay();
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Listener
    //
    ////////////////////////////////////////////////////////////////////////////
    public void handleEvent(Event event) {
      if (!m_enabled) {
        return;
      }
      // calculate the distance to window
      Point mouseLocation = m_display.getCursorLocation();
      Point shellLocation = m_shell.getLocation();
      Point shellSize = m_shell.getSize();
      Interval shellWidth = new Interval(shellLocation.x, shellSize.x);
      Interval shellHeight = new Interval(shellLocation.y, shellSize.y);
      int distanceX = shellWidth.distance(mouseLocation.x);
      int distanceY = shellHeight.distance(mouseLocation.y);
      boolean inside = distanceX == 0 && distanceY == 0;
      int distance = (int) Math.sqrt(distanceX * distanceX + distanceY * distanceY);
      // if the mouse moved too far then the user don't need this window
      if (distance > MAX_DISTANCE) {
        m_enabled = false;
        setAlpha(m_shell, 0);
        // for platforms which doesn't support alpha
        if (getAlpha(m_shell) == 255) {
          m_shell.setVisible(false);
        }
        return;
      }
      // if inside, don't set alpha again and again, this flickering for me
      if (inside) {
        if (getAlpha(m_shell) < 255) {
          setAlpha(m_shell, 255);
        }
      } else {
        // proportionally set alpha
        int alpha = 255 - distance * ALPHA_MULTIPLIER;
        setAlpha(m_shell, alpha > 0 ? alpha : 0);
      }
    }

    /**
     * Enable this listener when setting up the window.
     */
    public final void setEnabled(boolean enabled) {
      m_enabled = enabled;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Alpha helpers
  //
  ////////////////////////////////////////////////////////////////////////////
  private static void setAlpha(Shell shell, int alpha) {
    OSSupport.get().setAlpha(shell, alpha);
  }

  private static int getAlpha(Shell shell) {
    return OSSupport.get().getAlpha(shell);
  }
}
