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
package org.eclipse.wb.internal.xwt.model.widgets;

import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.xml.model.TopBoundsSupport;
import org.eclipse.wb.internal.xwt.support.CoordinateUtils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

/**
 * {@link TopBoundsSupport} for {@link ControlInfo}.
 * 
 * @author scheglov_ke
 * @coverage XWT.model.widgets
 */
public class CompositeTopBoundsSupport extends TopBoundsSupport {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CompositeTopBoundsSupport(ControlInfo control) {
    super(control);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // TopBoundsSupport
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void apply() throws Exception {
    Control control = (Control) m_component.getComponentObject();
    // set size from resource properties (or default)
    {
      Dimension size = getResourceSize();
      control.setSize(size.width, size.height);
    }
    // ensure that Shell also has valid bounds
    configureShellWrapper(control);
  }

  @Override
  public void setSize(int width, int height) throws Exception {
    setResourceSize(width, height);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Show
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean show() throws Exception {
    Control control = (Control) m_component.getObject();
    show(control);
    return true;
  }

  /**
   * Shows given control for testing/preview.
   */
  public static void show(Control control) throws Exception {
    showBefore();
    try {
      show0(control);
    } finally {
      showAfter();
    }
  }

  /**
   * Prepares environment for showing control.
   */
  private static void showBefore() throws Exception {
    Shell eclipseShell = DesignerPlugin.getShell();
    // disable redraw to prevent outstanding paints after preview window closed and disposed
    if (EnvironmentUtils.IS_MAC) {
      eclipseShell.redraw();
      eclipseShell.update();
      eclipseShell.setRedraw(false);
    }
    // disable Shell to prevent its activation before closing preview
    eclipseShell.setEnabled(false);
  }

  /**
   * Updates environment after showing control.
   */
  private static void showAfter() throws Exception {
    Shell eclipseShell = DesignerPlugin.getShell();
    if (EnvironmentUtils.IS_MAC) {
      eclipseShell.setRedraw(true);
    }
    eclipseShell.setEnabled(true);
    eclipseShell.forceActive();
  }

  /**
   * Shows given control for testing/preview, raw.
   */
  private static void show0(Control control) throws Exception {
    Shell shell = configureShellWrapper(control);
    // close preview by pressing ESC key
    Runnable clearESC = closeOnESC(shell);
    // set location
    {
      org.eclipse.swt.graphics.Rectangle monitorClientArea =
          DesignerPlugin.getShell().getMonitor().getClientArea();
      // center on primary Monitor 
      int x;
      int y;
      {
        Rectangle shellBounds = CoordinateUtils.getBounds(shell);
        x = monitorClientArea.x + (monitorClientArea.width - shellBounds.width) / 2;
        y = monitorClientArea.y + (monitorClientArea.height - shellBounds.height) / 2;
      }
      // ensure that top-left corner is visible
      x = Math.max(x, monitorClientArea.x + 10);
      y = Math.max(y, monitorClientArea.y + 10);
      // do set location
      shell.setLocation(x, y);
    }
    // show Shell in modal state
    showShell(shell);
    clearESC.run();
  }

  /**
   * Ensures that {@link Shell} wrapper of given {@link Control} has size required to show
   * {@link Control} with its current size, even after layout.
   */
  private static Shell configureShellWrapper(Control control) throws Exception {
    Shell shell = control.getShell();
    // handle wrapper
    if (control != shell) {
      shell.setText("Wrapper Shell");
      shell.setLayout(new FillLayout());
      Rectangle controlBounds = CoordinateUtils.getBounds(control);
      Dimension trimSize =
          CoordinateUtils.computeTrimSize(shell, controlBounds.width, controlBounds.height);
      shell.setSize(trimSize.width, trimSize.height);
      shell.layout();
    }
    // we have Shell
    return shell;
  }

  /**
   * Shows given {@link Shell} and runs event loop.
   */
  private static void showShell(Shell shell) throws Exception {
    shell.setVisible(true);
    shell.setActive();
    // run events loop
    Display display = shell.getDisplay();
    while (!shell.isDisposed() && shell.isVisible()) {
      if (!display.readAndDispatch()) {
        display.sleep();
      }
    }
  }

  /**
   * Add the display filter which closes preview by pressing ESC key.
   */
  private static Runnable closeOnESC(final Shell shell) {
    final Display display = DesignerPlugin.getStandardDisplay();
    final Listener listener = new Listener() {
      public void handleEvent(Event event) {
        if (event.keyCode == SWT.ESC) {
          shell.close();
          event.doit = false;
        }
      }
    };
    // add filter
    display.addFilter(SWT.KeyDown, listener);
    // continuation
    return new Runnable() {
      public void run() {
        display.removeFilter(SWT.KeyDown, listener);
      }
    };
  }
}