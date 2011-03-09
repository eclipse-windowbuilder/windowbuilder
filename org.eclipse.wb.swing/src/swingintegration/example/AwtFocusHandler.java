/*******************************************************************************
 * Copyright (c) 2007 SAS Institute. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies
 * this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: SAS Institute - initial API and implementation
 *******************************************************************************/
package swingintegration.example;

import com.google.common.collect.Lists;

import java.awt.Component;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.FocusTraversalPolicy;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.List;

import javax.swing.JPopupMenu;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;

class AwtFocusHandler implements FocusListener, ContainerListener, WindowFocusListener {
  private final Frame frame;
  private SwtFocusHandler swtHandler;
  private boolean awtHasFocus = false;
  private Component currentComponent = null;

  AwtFocusHandler(Frame frame) {
    assert frame != null;
    this.frame = frame;
    frame.addContainerListener(new RecursiveContainerListener(this));
    frame.addWindowFocusListener(this);
  }

  void setSwtHandler(SwtFocusHandler handler) {
    assert handler != null;
    assert swtHandler == null; // this method is meant to be called once
    swtHandler = handler;
  }

  void gainFocus() {
    assert frame != null;
    // assert !awtHasFocus;
    assert EventQueue.isDispatchThread(); // On AWT event thread
    FocusTraversalPolicy policy = frame.getFocusTraversalPolicy();
    Component component;
    if (policy instanceof EmbeddedChildFocusTraversalPolicy) {
      EmbeddedChildFocusTraversalPolicy embeddedPolicy = (EmbeddedChildFocusTraversalPolicy) policy;
      component = embeddedPolicy.getCurrentComponent(frame);
    } else {
      // TODO: direction based?
      component = policy.getDefaultComponent(frame);
    }
    if (component != null) {
      // System.out.println("Requesting focus for component: " + component);
      component.requestFocus();
      // TODO: else case error? If not, consider moving flag setting below into this if
    }
    awtHasFocus = true;
  }

  /**
   * Moves focus back to the next SWT component
   */
  void transferFocusNext() {
    assert swtHandler != null;
    assert awtHasFocus;
    awtHasFocus = false;
    swtHandler.gainFocusNext();
  }

  /**
   * Moves focus back to the previous SWT component
   */
  void transferFocusPrevious() {
    assert swtHandler != null;
    assert awtHasFocus;
    awtHasFocus = false;
    swtHandler.gainFocusPrevious();
  }

  boolean awtHasFocus() {
    return awtHasFocus;
  }

  Component getCurrentComponent() {
    return currentComponent;
  }

  // ..................... Listener implementations
  public void focusGained(FocusEvent e) {
    assert e != null;
    assert EventQueue.isDispatchThread(); // On AWT event thread
    // System.out.println("gained (awt). component = " + e.getComponent() + ", opposite = " + e.getOppositeComponent());
    currentComponent = e.getComponent();
  }

  public void focusLost(FocusEvent e) {
    // System.out.println("component focus lost (awt). opposite = " + e.getOppositeComponent());
    // Intentionally leaving currentComponent set. When window focus is lost, 
    // it will be needed. 
  }

  public void componentAdded(ContainerEvent e) {
    assert e != null;
    assert EventQueue.isDispatchThread(); // On AWT event thread
    e.getChild().addFocusListener(this);
  }

  public void componentRemoved(ContainerEvent e) {
    assert e != null;
    assert EventQueue.isDispatchThread(); // On AWT event thread
    e.getChild().removeFocusListener(this);
  }

  public void windowGainedFocus(WindowEvent e) {
    assert EventQueue.isDispatchThread(); // On AWT event thread
    // System.out.println("WindowFocusListener.windowGainedFocus");
    awtHasFocus = true;
  }

  public void windowLostFocus(WindowEvent e) {
    assert e != null;
    assert swtHandler != null;
    assert EventQueue.isDispatchThread(); // On AWT event thread
    // System.out.println("WindowFocusListener.windowLostFocus");
    // Dismiss any popup menus that are
    // open when losing focus. This prevents situations where
    // multiple popup menus are visible at the same time. In JDK 1.4 and earlier, 
    // the dismissal is not done automatically. In JDK 1.5, this code is 
    // unnecessary, but it doesn't seem to hurt anything. 
    // TODO: verify this is OK on other windowing systems
    // TODO: disable in post-1.4 environments
    /* boolean popupShown = */hidePopups();
    // If focus is being lost to the parent SWT composite, then
    // grab it back for AWT and return. Normally the parent SWT composite will
    // do this for us, but it will not see a focus gained event when focus 
    // is transferred to it from its AWT frame child. 
    // This happens, for example, if an AWT control has focus and the 
    // tab of a containing (already active) view is clicked.
    //
    // However, don't grab back focus if a popup was hidden above. The popup
    // area will not be properly redrawn (the popup, or part of it, will 
    // appear to be still there. 
    //if (!popupShown && swtHandler.hasFocus()) {
    // System.out.println("**** Taking back focus: " + e);
    // This seems to have side effects, so it's commented out for now. 
    // (Sometimes, it forces the workbench window to the foreground when another
    // program's window is selected.)
    // TODO: find an alternate approach to reassert focus
    // gainFocus();
    // return;
    //}
    // On a normal change of focus, Swing will turn off any selection
    // in a text field to help indicate focus is lost. This won't happen
    // automatically when transferring to SWT, so turn off the selection
    // manually.
    if (currentComponent instanceof JTextComponent) {
      Caret caret = ((JTextComponent) currentComponent).getCaret();
      if (caret != null) {
        caret.setSelectionVisible(false);
      }
    }
    awtHasFocus = false;
  }

  // Returns true if any popup has been hidden
  private boolean hidePopups() {
    boolean result = false;
    List<JPopupMenu> popups = Lists.newArrayList();
    assert EventQueue.isDispatchThread(); // On AWT event thread
    // Look for popups inside the frame's component hierarchy. 
    // Lightweight popups will be found here. 
    findContainedPopups(frame, popups);
    // Also look for popups in the frame's window hierarchy. 
    // Heavyweight popups will be found here.
    findOwnedPopups(frame, popups);
    // System.out.println("Hiding popups, count=" + popups.size());
    for (JPopupMenu popup : popups) {
      if (popup.isVisible()) {
        result = true;
        popup.setVisible(false);
      }
    }
    return result;
  }

  private void findOwnedPopups(Window window, List<JPopupMenu> popups) {
    assert window != null;
    assert EventQueue.isDispatchThread(); // On AWT event thread
    Window[] ownedWindows = window.getOwnedWindows();
    for (Window ownedWindow : ownedWindows) {
      findContainedPopups(ownedWindow, popups);
      findOwnedPopups(ownedWindow, popups);
    }
  }

  private void findContainedPopups(Container container, List<JPopupMenu> popups) {
    assert container != null;
    assert popups != null;
    assert EventQueue.isDispatchThread(); // On AWT event thread
    Component[] components = container.getComponents();
    for (int i = 0; i < components.length; i++) {
      Component c = components[i];
      // JPopupMenu is a container, so check for it first
      if (c instanceof JPopupMenu) {
        popups.add((JPopupMenu) c);
      } else if (c instanceof Container) {
        findContainedPopups((Container) c, popups);
      }
    }
  }

  void postHidePopups() {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        hidePopups();
      }
    });
  }
}
