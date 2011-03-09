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
package org.eclipse.wb.internal.dev.tree;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IStartup;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * In Vista {@link Tree} shows expanders only when {@link Tree} has focus. So, we can not see if
 * item has children, so we loose part of information. I don't like this. This class forces
 * expanders visibility for all {@link Tree} widgets in Eclipse.
 * 
 * @author scheglov_ke
 * @author mitin_aa
 */
public final class VistaTreeFixer implements IStartup {
  ////////////////////////////////////////////////////////////////////////////
  //
  // IStartup
  //
  ////////////////////////////////////////////////////////////////////////////
  public void earlyStartup() {
    if (isWindowsVista() && isFixNeeded()) {
      scheduleTreeWidgetsUpdates();
    }
  }

  private static boolean isFixNeeded() {
    try {
      String hostName = InetAddress.getLocalHost().getHostName();
      return "SCHEGLOV-WIN".equalsIgnoreCase(hostName) || "SABLIN-AA".equalsIgnoreCase(hostName);
    } catch (Throwable e) {
      return false;
    }
  }

  private static boolean isWindowsVista() {
    String osName = System.getProperty("os.name");
    return "Windows Vista".equals(osName) || "Windows 7".equals(osName);
  }

  private void scheduleTreeWidgetsUpdates() {
    final Display display = Display.getDefault();
    display.asyncExec(new Runnable() {
      public void run() {
        display.timerExec(100, new Runnable() {
          public void run() {
            updateAllTreeWidgets(display);
            display.timerExec(100, this);
          }
        });
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tree updating
  //
  ////////////////////////////////////////////////////////////////////////////
  private final Set<Tree> m_updatedTrees = new HashSet<Tree>();

  private void updateAllTreeWidgets(Display display) {
    // update all existing Tree's
    for (Shell shell : display.getShells()) {
      updateTreeWidgetsInHeirarchy(shell);
    }
    // remove disposed Tree's
    for (Iterator<Tree> I = m_updatedTrees.iterator(); I.hasNext();) {
      Tree tree = I.next();
      if (tree.isDisposed()) {
        I.remove();
      }
    }
  }

  private void updateTreeWidgetsInHeirarchy(Control control) {
    if (control instanceof Tree) {
      disableExpanderFadeInOut((Tree) control);
    } else if (control instanceof Composite) {
      Composite composite = (Composite) control;
      for (Control child : composite.getChildren()) {
        updateTreeWidgetsInHeirarchy(child);
      }
    }
  }

  @SuppressWarnings("restriction")
  private void disableExpanderFadeInOut(Tree tree) {
    if (!m_updatedTrees.contains(tree)) {
      m_updatedTrees.add(tree);
      org.eclipse.swt.internal.win32.OS.SendMessage(tree.handle, 0x1100 + 44, 0x0040, 0x0000);
      tree.redraw();
    }
  }
}
