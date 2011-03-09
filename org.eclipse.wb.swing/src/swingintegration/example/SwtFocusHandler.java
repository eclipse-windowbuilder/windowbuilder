/*******************************************************************************
 * Copyright (c) 2007 SAS Institute. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies
 * this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: SAS Institute - initial API and implementation
 *******************************************************************************/
package swingintegration.example;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import java.awt.EventQueue;

class SwtFocusHandler implements FocusListener, KeyListener {
  private final Composite composite;
  private final Display display;
  private AwtFocusHandler awtHandler;

  SwtFocusHandler(Composite composite) {
    assert composite != null;
    assert Display.getCurrent() != null; // On SWT event thread
    this.composite = composite;
    display = composite.getDisplay();
    composite.addFocusListener(this);
    composite.addKeyListener(this);
  }

  void setAwtHandler(AwtFocusHandler handler) {
    assert handler != null;
    assert awtHandler == null; // this method is meant to be called once
    assert composite != null;
    assert Display.getCurrent() != null; // On SWT event thread        
    awtHandler = handler;
    // Dismiss Swing popups when the main window is moved. (It would be 
    // better to dismiss popups whenever the titlebar is clicked, but 
    // there does not seem to be a way.)
    final ControlAdapter controlAdapter = new ControlAdapter() {
      @Override
      public void controlMoved(ControlEvent e) {
        assert awtHandler != null;
        awtHandler.postHidePopups();
      }
    };
    final Shell shell = composite.getShell();
    shell.addControlListener(controlAdapter);
    // Cleanup listeners on dispose
    composite.addDisposeListener(new DisposeListener() {
      public void widgetDisposed(DisposeEvent e) {
        shell.removeControlListener(controlAdapter);
      }
    });
  }

  void gainFocusNext() {
    traverse(SWT.TRAVERSE_TAB_NEXT);
  }

  void gainFocusPrevious() {
    traverse(SWT.TRAVERSE_TAB_PREVIOUS);
  }

  private void traverse(final int traversal) {
    assert composite != null;
    // Tab from the containing SWT component while 
    // running on the SWT thread
    Runnable r = new Runnable() {
      public void run() {
        composite.traverse(traversal);
      }
    };
    display.asyncExec(r);
  }

  //    boolean hasFocus() {
  //        assert composite != null;
  //        
  //        // This will return true if the composite has focus, or if any
  //        // foreign (e.g. AWT) child of the composite has focus.
  //        if (display.isDisposed()) {
  //            return false;
  //        }
  //        final boolean[] result = new boolean[1];
  //        display.syncExec(new Runnable() {
  //            public void run() {
  //                result[0] = (!composite.isDisposed() &&
  //                             (display.getFocusControl() == composite));
  //            }
  //        });
  //        return result[0];
  //    }
  // ..................... Listener implementations
  public void focusGained(FocusEvent e) {
    assert awtHandler != null;
    assert Display.getCurrent() != null; // On SWT event thread
    // System.out.println("Gained: " + e.toString() + " (" + e.widget.getClass().getName() + ")");
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        awtHandler.gainFocus();
      }
    });
  }

  public void focusLost(FocusEvent e) {
    // System.out.println("Lost: " + e.toString() + " (" + e.widget.getClass().getName() + ")");
  }

  public void keyPressed(KeyEvent e) {
    assert Display.getCurrent() != null; // On SWT event thread
    // If the embedded swing root pane has no components to receive focus, 
    // then there will be cases where the parent SWT composite will keep 
    // focus. (For example, when tabbing into the root pane container). 
    // By default, in these cases, the focus is swallowed by the Composite
    // and never escapes. This code allows tab and back-tab to do the 
    // proper traversal to other SWT components from the composite.
    // TODO: other keys?
    if (e.keyCode == SWT.TAB) {
      // TODO: In some cases, this gobbles up all the tabs, even from AWT children. Find a more selective way. 
      /*if (e.stateMask == SWT.NONE) {
          traverse(SWT.TRAVERSE_TAB_NEXT);
      } else if (e.stateMask == SWT.SHIFT) {
          traverse(SWT.TRAVERSE_TAB_PREVIOUS);
      }*/
    }
  }

  public void keyReleased(KeyEvent e) {
  }
}
