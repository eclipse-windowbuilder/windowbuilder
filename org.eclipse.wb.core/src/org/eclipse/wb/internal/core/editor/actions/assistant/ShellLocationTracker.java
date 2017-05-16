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
package org.eclipse.wb.internal.core.editor.actions.assistant;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * Helper for saving/restoring {@link Shell} location to/from {@link IDialogSettings}.
 *
 * @author scheglov_ke
 * @coverage core.editor.action.assistant
 */
public final class ShellLocationTracker {
  private final IDialogSettings m_settings;
  private final String m_sectionName;
  private Shell m_shell;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ShellLocationTracker(IDialogSettings settings, String sectionName) {
    m_settings = settings;
    m_sectionName = sectionName;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public void setShell(Shell shell) {
    m_shell = shell;
    m_shell.addListener(SWT.Dispose, new Listener() {
      public void handleEvent(Event event) {
        if (!m_shell.isDisposed() && !m_shell.getMaximized()) {
          Point location = m_shell.getLocation();
          saveLocation(location);
        }
      }
    });
  }

  public Point getInitialLocation(Point initialSize) {
    Point location = loadLocation();
    if (location != null) {
      return location;
    }
    // default location - centered on parent Shell
    Rectangle parentBounds = getParentShell().getBounds();
    int x = parentBounds.x + (parentBounds.width - initialSize.x) / 2;
    int y = parentBounds.y + (parentBounds.height - initialSize.y) / 2;
    return new Point(x, y);
  }

  private Shell getParentShell() {
    Shell parent = (Shell) m_shell.getParent();
    if (parent != null) {
      return parent;
    }
    return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Location
  //
  ////////////////////////////////////////////////////////////////////////////
  private Point loadLocation() {
    IDialogSettings settings = getLocationSettings();
    try {
      return new Point(settings.getInt("x"), settings.getInt("y"));
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private void saveLocation(Point location) {
    IDialogSettings settings = getLocationSettings();
    settings.put("x", location.x);
    settings.put("y", location.y);
  }

  private IDialogSettings getLocationSettings() {
    if (m_settings.getSection(m_sectionName) == null) {
      return m_settings.addNewSection(m_sectionName);
    }
    return m_settings.getSection(m_sectionName);
  }
}
