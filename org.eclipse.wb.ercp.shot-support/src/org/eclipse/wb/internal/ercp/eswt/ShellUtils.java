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
package org.eclipse.wb.internal.ercp.eswt;

import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.widgets.Shell;

/**
 * @author lobas_av
 * 
 */
public class ShellUtils {
  public static void showShell(final Shell shell) {
    shell.addShellListener(new ShellListener() {
      public void shellIconified(ShellEvent shellevent) {
      }

      public void shellDeiconified(ShellEvent shellevent) {
      }

      public void shellDeactivated(ShellEvent shellevent) {
      }

      public void shellClosed(ShellEvent shellevent) {
        shellevent.doit = false;
        shell.removeShellListener(this);
        shell.setVisible(false);
      }

      public void shellActivated(ShellEvent shellevent) {
      }
    });
    shell.setVisible(true);
  }
}