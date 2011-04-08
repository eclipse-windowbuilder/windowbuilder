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
package org.eclipse.wb.internal.swing.model.component.top;

import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.TopBoundsSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.swing.model.ModelMessages;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.utils.SwingUtils;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

/**
 * Implementation of {@link TopBoundsSupport} for Swing.
 * 
 * @author scheglov_ke
 * @coverage swing.model.top
 */
public class SwingTopBoundsSupport extends TopBoundsSupport {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SwingTopBoundsSupport(ComponentInfo component) {
    super(component);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // TopBoundsSupport
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void apply() throws Exception {
    // if one these methods is present, we already set size during AST evaluation
    if (hasMethodInvocations(new String[]{
        "setSize(int,int)",
        "setBounds(int,int,int,int)",
        "setSize(java.awt.Dimension)"})) {
      return;
    }
    // set size from resource properties (or default)
    {
      Component component = (Component) m_component.getObject();
      org.eclipse.wb.draw2d.geometry.Dimension size = getResourceSize();
      component.setSize(size.width, size.height);
    }
  }

  @Override
  public void setSize(int width, int height) throws Exception {
    // check for: setSize(java.awt.Dimension)void
    if (setSizeDimension("setSize", width, height)) {
      return;
    }
    // check for: setSize(int,int)void
    if (setSizeInts("setSize(int,int)", 0, 1, width, height)) {
      return;
    }
    // check for: setBounds(int,int,int,int)void
    if (setSizeInts("setBounds(int,int,int,int)", 2, 3, width, height)) {
      return;
    }
    // remember size in resource properties
    setResourceSize(width, height);
  }

  @Override
  protected Dimension getDefaultSize() {
    // if "preferred" size if big enough, don't change it
    if (m_component.getObject() instanceof Component) {
      Component component = (Component) m_component.getObject();
      java.awt.Dimension size = component.getSize();
      if (size.width >= 200 || size.height >= 200) {
        return new Dimension(size.width, size.height);
      }
    }
    // use toolkit defaults
    return super.getDefaultSize();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Show
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean show() throws Exception {
    Component component = (Component) m_component.getObject();
    show(component);
    // yes, we've shown window
    return true;
  }

  /**
   * Shows given {@link Component} for testing/preview.
   */
  public static void show(Component component) throws Exception {
    // prepare Window
    final Window window;
    {
      Rectangle monitorClientArea = DesignerPlugin.getShell().getMonitor().getClientArea();
      window = prepareWindow(component, monitorClientArea);
    }
    // show and wait for close
    {
      // add close listener
      final Shell mainShell = DesignerPlugin.getShell();
      window.addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent e) {
          mainShell.getDisplay().asyncExec(new Runnable() {
            public void run() {
              mainShell.setEnabled(true);
              mainShell.forceActive();
            }
          });
        }
      });
      SwingUtils.runLaterAndWait(new RunnableEx() {
        public void run() throws Exception {
          window.setFocusableWindowState(true);
          window.setVisible(true);
          window.toFront();
        }
      });
      // wait for close
      mainShell.setEnabled(false);
      while (window.isVisible()) {
        while (mainShell.getDisplay().readAndDispatch()) {
        }
        mainShell.getDisplay().sleep();
      }
    }
  }

  /**
   * Creates Window instance (if needed) to show be shown.
   */
  private static Window prepareWindow(final Component component, final Rectangle clientArea)
      throws Exception {
    return SwingUtils.runObjectLaterAndWait(new RunnableObjectEx<Window>() {
      public Window runObject() throws Exception {
        final Window window;
        if (component instanceof Window) {
          window = (Window) component;
        } else {
          JFrame frame = new JFrame();
          window = frame;
          // add component on frame
          component.setPreferredSize(component.getSize());
          frame.getContentPane().add(component, BorderLayout.CENTER);
          // configure frame
          frame.setTitle(ModelMessages.SwingTopBoundsSupport_wrapperTitle);
          frame.pack();
        }
        // set window location
        {
          int x = clientArea.x + (clientArea.width - window.getWidth()) / 2;
          int y = clientArea.y + (clientArea.height - window.getHeight()) / 2;
          window.setLocation(x, y);
        }
        // configure special windows
        {
          if (window instanceof Dialog) {
            ((Dialog) window).setModal(false);
          }
          if (window instanceof JFrame) {
            ((JFrame) window).setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
          }
        }
        return window;
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Changes {@link Dimension} argument of given method.
   * 
   * @return <code>true</code> if method was found and change done.
   */
  protected final boolean setSizeDimension(String methodName, int width, int height)
      throws Exception {
    MethodInvocation invocation =
        m_component.getMethodInvocation(methodName + "(java.awt.Dimension)");
    if (invocation != null) {
      AstEditor editor = m_component.getEditor();
      Expression dimensionExpression = (Expression) invocation.arguments().get(0);
      editor.replaceExpression(dimensionExpression, "new java.awt.Dimension("
          + width
          + ", "
          + height
          + ")");
      return true;
    }
    // not found
    return false;
  }

  /**
   * Changes <code>int</code> arguments of given method.
   * 
   * @return <code>true</code> if method was found and change done.
   */
  private boolean setSizeInts(String methodSignature,
      int widthIndex,
      int heightIndex,
      int width,
      int height) throws Exception {
    MethodInvocation invocation = m_component.getMethodInvocation(methodSignature);
    if (invocation != null) {
      AstEditor editor = m_component.getEditor();
      editor.replaceExpression(
          (Expression) invocation.arguments().get(widthIndex),
          Integer.toString(width));
      editor.replaceExpression(
          (Expression) invocation.arguments().get(heightIndex),
          Integer.toString(height));
      return true;
    }
    // not found
    return false;
  }
}
