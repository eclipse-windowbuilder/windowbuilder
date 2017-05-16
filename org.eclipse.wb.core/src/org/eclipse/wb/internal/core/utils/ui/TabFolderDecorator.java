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
package org.eclipse.wb.internal.core.utils.ui;

import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

/**
 * Helper for decorating {@link CTabFolder} using same colors as for views/editors in Eclipse.
 *
 * @author scheglov_ke
 * @coverage core.ui
 */
public final class TabFolderDecorator {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Creation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Marks given {@link CTabFolder} as decorated.
   */
  public static void decorate(IWorkbenchPart hostPart, CTabFolder tabFolder) {
    new TabFolderDecorator(hostPart, tabFolder);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance fields
  //
  ////////////////////////////////////////////////////////////////////////////
  private final IWorkbenchPart m_hostPart;
  private final CTabFolder m_tabFolder;
  private boolean m_shellActive = true;
  private final IPartListener m_partListener = new IPartListener() {
    public void partActivated(IWorkbenchPart part) {
      if (part == m_hostPart) {
        updateColors();
      }
    }

    public void partDeactivated(IWorkbenchPart part) {
      if (part == m_hostPart) {
        updateColors();
      }
    }

    public void partBroughtToTop(IWorkbenchPart part) {
    }

    public void partClosed(IWorkbenchPart part) {
    }

    public void partOpened(IWorkbenchPart part) {
    }
  };
  private final ShellListener m_shellListener = new ShellAdapter() {
    @Override
    public void shellActivated(ShellEvent e) {
      m_shellActive = true;
      updateColors();
    }

    @Override
    public void shellDeactivated(ShellEvent e) {
      m_shellActive = false;
      updateColors();
    }
  };

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private TabFolderDecorator(IWorkbenchPart hostPart, CTabFolder tabFolder) {
    m_hostPart = hostPart;
    m_tabFolder = tabFolder;
    // add part listener
    final IWorkbenchPage activePage = m_hostPart.getSite().getPage();
    activePage.addPartListener(m_partListener);
    // add Shell listener
    final Shell shell = m_tabFolder.getShell();
    shell.addShellListener(m_shellListener);
    // add dispose listener to remove part/Shell listeners
    m_tabFolder.addListener(SWT.Dispose, new Listener() {
      public void handleEvent(Event event) {
        activePage.removePartListener(m_partListener);
        shell.removeShellListener(m_shellListener);
      }
    });
    // set initial colors
    updateColors();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Internal
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Updates colors depending if host {@link IWorkbenchPart} and {@link Shell} are active or not.
   */
  private void updateColors() {
    if (m_hostPart.getSite().getPage().getActivePart() == m_hostPart) {
      setActiveTabColors();
    } else {
      setInactiveTabColors();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Colors
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets the colors of the tab to the inactive tab colors.
   */
  private void setInactiveTabColors() {
    setInactiveTabColors(m_tabFolder);
  }

  /**
   * Sets the colors of the tab to the active tab colors, taking into account shell focus.
   */
  private void setActiveTabColors() {
    setActiveTabColors(m_shellActive, m_tabFolder);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets the colors of the tab to the inactive tab colors.
   */
  public static void setInactiveTabColors(CTabFolder tabFolder) {
    org.eclipse.ui.themes.ITheme theme =
        PlatformUI.getWorkbench().getThemeManager().getCurrentTheme();
    ColorRegistry colorRegistry = theme.getColorRegistry();
    drawGradient(
        tabFolder,
        colorRegistry.get(IWorkbenchThemeConstants.INACTIVE_TAB_TEXT_COLOR),
        new Color[]{
            colorRegistry.get(IWorkbenchThemeConstants.INACTIVE_TAB_BG_START),
            colorRegistry.get(IWorkbenchThemeConstants.INACTIVE_TAB_BG_END)},
        new int[]{theme.getInt(IWorkbenchThemeConstants.INACTIVE_TAB_PERCENT)},
        theme.getBoolean(IWorkbenchThemeConstants.INACTIVE_TAB_VERTICAL));
  }

  /**
   * Sets the colors of the tab to the active tab colors, taking into account shell focus.
   */
  public static void setActiveTabColors(boolean shellActive, CTabFolder tabFolder) {
    org.eclipse.ui.themes.ITheme theme =
        PlatformUI.getWorkbench().getThemeManager().getCurrentTheme();
    ColorRegistry colorRegistry = theme.getColorRegistry();
    if (shellActive) {
      if (tabFolder.getItemCount() == 0) {
        tabFolder.setSelectionBackground(colorRegistry.get(IWorkbenchThemeConstants.ACTIVE_TAB_BG_END));
      }
      drawGradient(
          tabFolder,
          colorRegistry.get(IWorkbenchThemeConstants.ACTIVE_TAB_TEXT_COLOR),
          new Color[]{
              colorRegistry.get(IWorkbenchThemeConstants.ACTIVE_TAB_BG_START),
              colorRegistry.get(IWorkbenchThemeConstants.ACTIVE_TAB_BG_END)},
          new int[]{theme.getInt(IWorkbenchThemeConstants.ACTIVE_TAB_PERCENT)},
          theme.getBoolean(IWorkbenchThemeConstants.ACTIVE_TAB_VERTICAL));
    } else {
      drawGradient(
          tabFolder,
          colorRegistry.get(IWorkbenchThemeConstants.ACTIVE_NOFOCUS_TAB_TEXT_COLOR),
          new Color[]{
              colorRegistry.get(IWorkbenchThemeConstants.ACTIVE_NOFOCUS_TAB_BG_START),
              colorRegistry.get(IWorkbenchThemeConstants.ACTIVE_NOFOCUS_TAB_BG_END)},
          new int[]{theme.getInt(IWorkbenchThemeConstants.ACTIVE_NOFOCUS_TAB_PERCENT)},
          theme.getBoolean(IWorkbenchThemeConstants.ACTIVE_NOFOCUS_TAB_VERTICAL));
    }
  }

  /**
   * Sets the gradient for the selected {@link TabItem} of given {@link TabFolder}.
   */
  private static void drawGradient(CTabFolder tabFolder,
      Color fgColor,
      Color[] bgColors,
      int[] percentages,
      boolean vertical) {
    if (!tabFolder.isDisposed()) {
      tabFolder.setSelectionForeground(fgColor);
      tabFolder.setSelectionBackground(bgColors, percentages, vertical);
    }
  }
}
