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
package org.eclipse.wb.internal.swing.palette;

import org.eclipse.wb.core.editor.palette.model.EntryInfo;
import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.editor.palette.DesignerPalette;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.ui.UiUtils;
import org.eclipse.wb.internal.gef.core.EditDomain;
import org.eclipse.wb.internal.gef.core.IActiveToolListener;
import org.eclipse.wb.internal.swing.Activator;
import org.eclipse.wb.internal.swing.preferences.IPreferenceConstants;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

/**
 * {@link EntryInfo} that shows popup/cascading Swing palette (this entry should be placed into RCP
 * palette).
 * 
 * @author scheglov_ke
 * @coverage swing.editor.palette
 */
public final class SwingPaletteEntryInfo extends EntryInfo {
  private static final Image ICON = Activator.getImage("popup_palette.png");

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SwingPaletteEntryInfo() {
    setId(getClass().getName());
    setName(PaletteMessages.SwingPaletteEntryInfo_name);
    setDescription(PaletteMessages.SwingPaletteEntryInfo_description);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // EntryInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Image getIcon() {
    return ICON;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Activation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean activate(boolean reload) {
    Display display = Display.getCurrent();
    // create Shell with Swing palette
    final Shell shell = new Shell(DesignerPlugin.getShell(), SWT.ON_TOP | SWT.NO_FOCUS);
    {
      shell.setLayout(new FillLayout());
      DesignerPalette palette = new DesignerPalette(shell, SWT.NONE, false);
      palette.setInput(m_editPartViewer, m_rootJavaInfo, IPreferenceConstants.TOOLKIT_ID);
      // make all Control's non-focusable, to avoid Shell activation on click
      ExecutionUtils.runLog(new RunnableEx() {
        public void run() throws Exception {
          makeNoFocus(shell);
        }
      });
    }
    // show Shell near to mouse cursor
    {
      // prepare default bounds for popup palette
      Rectangle bounds;
      {
        Rectangle mainPaletteBounds = display.getCursorControl().getBounds();
        Point cursorLocation = display.getCursorLocation();
        bounds =
            new Rectangle(cursorLocation.x,
                cursorLocation.y,
                mainPaletteBounds.width,
                mainPaletteBounds.height);
      }
      // check for leaving Display client area
      {
        Rectangle displayArea = display.getClientArea();
        if (bounds.x + bounds.width > displayArea.width - 100) {
          bounds.x = bounds.x - bounds.width;
        }
        if (bounds.y + bounds.height > displayArea.height - 50) {
          bounds.y = displayArea.height - bounds.height - 10;
        }
      }
      // set bounds
      shell.setBounds(bounds);
    }
    // hide Shell when appropriate
    addClosePaletteListeners(shell);
    // show Shell
    shell.setVisible(true);
    return false;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Not only {@link Shell}, but also any {@link Control} should be marked with {@link SWT#NO_FOCUS}
   * to avoid {@link Shell} activation.
   */
  private static void makeNoFocus(Control control) throws Exception {
    ReflectionUtils.setField(control, "style", control.getStyle() | SWT.NO_FOCUS);
    if (control instanceof Composite) {
      Composite composite = (Composite) control;
      for (Control child : composite.getChildren()) {
        makeNoFocus(child);
      }
    }
  }

  private void addClosePaletteListeners(final Shell shell) {
    final Shell shellMain = (Shell) shell.getParent();
    final Display display = shell.getDisplay();
    // close Shell on...
    final Listener shellClose_filter = new Listener() {
      public void handleEvent(Event event) {
        if (event.type == SWT.KeyDown) {
          // ...ESC press
          if (event.keyCode == SWT.ESC) {
            event.doit = false;
            shell.dispose();
          }
        } else if (event.type == SWT.MouseDown) {
          // ...click outside of Shell hierarchy 
          if (!UiUtils.isChildOf(shell, event.widget)) {
            shell.dispose();
          }
        } else if (event.type == SWT.Deactivate) {
          // ..."main" Shell deactivation
          if (event.widget instanceof Shell) {
            display.asyncExec(new Runnable() {
              public void run() {
                if (!UiUtils.isChildOf(shellMain, display.getActiveShell())) {
                  shell.dispose();
                }
              }
            });
          }
        }
      }
    };
    display.addFilter(SWT.KeyDown, shellClose_filter);
    display.addFilter(SWT.MouseDown, shellClose_filter);
    display.addFilter(SWT.Deactivate, shellClose_filter);
    // close Shell: on Tool selection (in popup palette)
    final EditDomain editDomain = m_editPartViewer.getEditDomain();
    final IActiveToolListener toolListener = new IActiveToolListener() {
      public void toolActivated(Tool tool) {
        shell.dispose();
      }
    };
    display.asyncExec(new Runnable() {
      public void run() {
        editDomain.addActiveToolListener(toolListener);
      }
    });
    // remove "shellClose_filter" on Shell dispose
    Listener listener = new Listener() {
      public void handleEvent(Event event) {
        if (event.type == SWT.Dispose) {
          display.removeFilter(SWT.KeyDown, shellClose_filter);
          display.removeFilter(SWT.MouseDown, shellClose_filter);
          display.removeFilter(SWT.Deactivate, shellClose_filter);
        }
      }
    };
    shell.addListener(SWT.Dispose, listener);
  }
}
