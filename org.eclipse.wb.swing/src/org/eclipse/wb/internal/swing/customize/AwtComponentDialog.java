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
package org.eclipse.wb.internal.swing.customize;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.ui.dialogs.ResizableDialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Panel;

import javax.swing.JRootPane;

/**
 * SWT dialog for showing AWT component.
 * 
 * @author lobas_av
 * @coverage swing.customize
 */
public final class AwtComponentDialog extends ResizableDialog {
  private final Component m_component;
  private final String m_title;
  private final String m_settingsName;
  private Frame m_frame;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AwtComponentDialog(AbstractUIPlugin plugin,
      Component component,
      String title,
      String settingsName) {
    super(DesignerPlugin.getShell(), plugin);
    m_component = component;
    m_title = title;
    m_settingsName = settingsName;
    setShellStyle(SWT.RESIZE | SWT.CLOSE | SWT.APPLICATION_MODAL);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText(m_title);
  }

  @Override
  protected Control createDialogArea(Composite parent) {
    Composite container = (Composite) super.createDialogArea(parent);
    container.setLayout(new FillLayout());
    //
    Composite composite = new Composite(container, SWT.EMBEDDED) {
      @Override
      public Point computeSize(int wHint, int hHint, boolean changed) {
        Dimension preferredSize = m_component.getPreferredSize();
        return new Point(preferredSize.width, preferredSize.height);
      }
    };
    // SWT_AWT.new_Frame()
    m_frame = SWT_AWT.new_Frame(composite);
    // java.awt.Panel
    Panel panel = new Panel(new BorderLayout());
    m_frame.add(panel);
    // javax.swing.JRootPane
    JRootPane rootPane = new JRootPane();
    panel.add(rootPane);
    // add main Component
    rootPane.getContentPane().add(m_component);
    m_frame.doLayout();
    // done
    return container;
  }

  @Override
  protected String getDialogSettingsSectionName() {
    return m_settingsName;
  }

  @Override
  protected Point getDefaultSize() {
    //return new Point(450, 300);
    // User asked to use Customizer.getPreferredSize()
    // http://www.eclipse.org/forums/index.php/t/339421/
    return super.getDefaultSize();
  }

  @Override
  public boolean close() {
    dispose(m_frame);
    return super.close();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Swing utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Disposes {@link Frame} safely, I hope that it will not lock.
   */
  private static void dispose(final Frame frame) {
    // schedule dispose
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        frame.dispose();
      }
    });
    // run SWT event loop to wait for scheduled AWT operation
    ExecutionUtils.runIgnore(new RunnableEx() {
      public void run() throws Exception {
        for (int i = 0; i < 1000; i++) {
          while (Display.getCurrent().readAndDispatch()) {
          }
          Thread.sleep(0);
        }
      }
    });
  }
}