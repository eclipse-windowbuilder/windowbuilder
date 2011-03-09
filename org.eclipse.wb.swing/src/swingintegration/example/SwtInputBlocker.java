/*******************************************************************************
 * Copyright (c) 2007 SAS Institute. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies
 * this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: SAS Institute - initial API and implementation
 *******************************************************************************/
package swingintegration.example;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

class SwtInputBlocker extends Dialog {
  static private SwtInputBlocker instance = null;
  static private int blockCount = 0;
  private Shell shell;

  private SwtInputBlocker(Shell parent) {
    super(parent, SWT.NONE);
  }

  private Object open() {
    assert Display.getCurrent() != null; // On SWT event thread
    final Shell parent = getParent();
    shell = new Shell(parent, SWT.APPLICATION_MODAL);
    shell.setSize(0, 0);
    shell.addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(FocusEvent e) {
        // On some platforms (e.g. Linux/GTK), the 0x0 shell still appears as a dot 
        // on the screen, so make it invisible by moving it below other windows. This
        // is unnecessary under Windows and causes a flash, so only make the call when necessary. 
        if (Platform.isGtk()) {
          shell.moveBelow(null);
        }
        AwtEnvironment.getInstance(shell.getDisplay()).requestAwtDialogFocus();
      }
    });
    shell.open();
    Display display = parent.getDisplay();
    while (!shell.isDisposed()) {
      if (!display.readAndDispatch()) {
        display.sleep();
      }
    }
    return null;
  }

  private void close() {
    assert shell != null;
    shell.dispose();
  }

  static synchronized void unblock() {
    assert blockCount >= 0;
    assert Display.getCurrent() != null; // On SWT event thread
    // System.out.println("Deleting SWT blocker");
    if (blockCount == 0) {
      return;
    }
    if (blockCount == 1 && instance != null) {
      instance.close();
      instance = null;
    }
    blockCount--;
  }

  static synchronized void block() {
    assert blockCount >= 0;
    // System.out.println("Creating SWT blocker");
    final Display display = Display.getCurrent();
    assert display != null; // On SWT event thread
    blockCount++;
    if (blockCount == 1) {
      assert instance == null; // should be no existing blocker
      // get a shell to parent the blocking dialog
      Shell shell = AwtEnvironment.getInstance(display).getShell();
      // If there is a shell to block, block input now. If there are no shells, 
      // then there is no input to block. In the case of no shells, we are not
      // protecting against a shell that might get created later. This is a rare
      // enough case to skip, at least for now. In the future, a listener could be 
      // added to cover it. 
      // TODO: if (shell==null) add listener to block shells created later?
      //
      // Block is implemented with a hidden modal dialog. Using setEnabled(false) is another option, but 
      // on some platforms that will grey the disabled controls.
      if (shell != null) {
        instance = new SwtInputBlocker(shell);
        instance.open();
      }
    }
  }
}
