/*******************************************************************************
 * Copyright (c) 2007 SAS Institute. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies
 * this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: SAS Institute - initial API and implementation
 *******************************************************************************/
package swingintegration.example;

import com.google.common.collect.Lists;

import org.eclipse.swt.widgets.Display;

import java.awt.AWTEvent;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.util.List;

/**
 * A listener that insures the proper modal behavior of Swing dialogs when running within a SWT
 * environment. When initialized, it blocks and unblocks SWT input as modal Swing dialogs are shown
 * and hidden.
 */
class AwtDialogListener implements AWTEventListener, ComponentListener {
  // modalDialogs should be accessed only from the AWT thread, so no
  // synchronization is needed. 
  private final List<Dialog> modalDialogs = Lists.newArrayList();
  private final Display display;

  /**
   * Registers this object as an AWT event listener so that Swing dialogs have the proper modal
   * behavior in the containing SWT environment. This is called automatically when you construct a
   * {@link EmbeddedSwingComposite}, and it need not be called separately in that case.
   * 
   * @param shell
   */
  AwtDialogListener(Display display) {
    assert display != null;
    this.display = display;
    Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.WINDOW_EVENT_MASK);
  }

  private void handleRemovedDialog(Dialog awtDialog, boolean removeListener) {
    assert awtDialog != null;
    assert modalDialogs != null;
    assert display != null;
    assert EventQueue.isDispatchThread(); // On AWT event thread
    if (removeListener) {
      awtDialog.removeComponentListener(this);
    }
    // Note: there is no isModal() check here because the dialog might 
    // have been changed from modal to non-modal after it was opened. In this case
    // the currently visible dialog would still act modal and we'd need to unblock
    // SWT here when it goes away.
    if (modalDialogs.remove(awtDialog)) {
      display.asyncExec(new Runnable() {
        public void run() {
          SwtInputBlocker.unblock();
        }
      });
    }
  }

  private void handleAddedDialog(final Dialog awtDialog) {
    assert awtDialog != null;
    assert modalDialogs != null;
    assert EventQueue.isDispatchThread(); // On AWT event thread
    if (modalDialogs.contains(awtDialog) || !awtDialog.isModal() || !awtDialog.isVisible()) {
      return;
    }
    modalDialogs.add(awtDialog);
    awtDialog.addComponentListener(this);
    display.asyncExec(new Runnable() {
      public void run() {
        SwtInputBlocker.block();
      }
    });
  }

  void requestFocus() {
    // TODO: this does not always bring the dialog to the top 
    // under some Linux desktops/window managers (e.g. metacity under GNOME).
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        assert modalDialogs != null;
        int size = modalDialogs.size();
        if (size > 0) {
          final Dialog awtDialog = modalDialogs.get(size - 1);
          // In one case, a call to requestFocus() alone does not 
          // bring the AWT dialog to the top. This happens if the 
          // dialog is given a null parent frame. When opened, the dialog
          // can be hidden by the SWT window even when it obtains focus.
          // Calling toFront() solves the problem, but...
          //
          // There are still problems if the Metal look and feel is in use.
          // The SWT window will hide the dialog the first time it is 
          // selected. Once the dialog is brought back to the front by 
          // the user, there is no further problem. 
          //
          // Why? It looks like SWT is not being notified of lost focus when 
          // the Metal dialog first opens; subsequently, when focus is regained, the 
          // focus gain event is not posted to the SwtInputBlocker.  
          //
          // The workaround is to use Windows look and feel, rather than Metal.
          awtDialog.requestFocus();
          awtDialog.toFront();
        }
      }
    });
  }

  private void handleOpenedWindow(WindowEvent event) {
    assert event != null;
    assert EventQueue.isDispatchThread(); // On AWT event thread
    Window window = event.getWindow();
    if (window instanceof Dialog) {
      handleAddedDialog((Dialog) window);
    }
  }

  private void handleClosedWindow(WindowEvent event) {
    assert event != null;
    assert EventQueue.isDispatchThread(); // On AWT event thread
    // Dispose-based close
    Window window = event.getWindow();
    if (window instanceof Dialog) {
      // Remove dialog and component listener
      handleRemovedDialog((Dialog) window, true);
    }
  }

  private void handleClosingWindow(WindowEvent event) {
    assert event != null;
    assert EventQueue.isDispatchThread(); // On AWT event thread
    // System-based close 
    Window window = event.getWindow();
    if (window instanceof Dialog) {
      final Dialog dialog = (Dialog) window;
      // Defer until later. Bad things happen if 
      // handleRemovedDialog() is called directly from 
      // this event handler. The Swing dialog does not close
      // properly and its modality remains in effect.
      EventQueue.invokeLater(new Runnable() {
        public void run() {
          // Remove dialog and component listener
          handleRemovedDialog(dialog, true);
        }
      });
    }
  }

  public void eventDispatched(AWTEvent event) {
    assert event != null;
    assert EventQueue.isDispatchThread(); // On AWT event thread
    switch (event.getID()) {
      case WindowEvent.WINDOW_OPENED :
        handleOpenedWindow((WindowEvent) event);
        break;
      case WindowEvent.WINDOW_CLOSED :
        handleClosedWindow((WindowEvent) event);
        break;
      case WindowEvent.WINDOW_CLOSING :
        handleClosingWindow((WindowEvent) event);
        break;
      default :
        break;
    }
  }

  public void componentHidden(ComponentEvent e) {
    assert e != null;
    assert EventQueue.isDispatchThread(); // On AWT event thread
    Object obj = e.getSource();
    if (obj instanceof Dialog) {
      // Remove dialog but keep listener in place so that we know if/when it is set visible
      handleRemovedDialog((Dialog) obj, false);
    }
  }

  public void componentShown(ComponentEvent e) {
    assert e != null;
    assert EventQueue.isDispatchThread(); // On AWT event thread
    Object obj = e.getSource();
    if (obj instanceof Dialog) {
      handleAddedDialog((Dialog) obj);
    }
  }

  public void componentResized(ComponentEvent e) {
  }

  public void componentMoved(ComponentEvent e) {
  }
}
