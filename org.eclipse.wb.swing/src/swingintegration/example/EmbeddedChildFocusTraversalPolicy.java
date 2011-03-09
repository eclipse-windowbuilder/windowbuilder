/*******************************************************************************
 * Copyright (c) 2007 SAS Institute. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies
 * this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: SAS Institute - initial API and implementation
 *******************************************************************************/
package swingintegration.example;

import java.awt.Component;
import java.awt.Container;
import java.awt.EventQueue;

import javax.swing.LayoutFocusTraversalPolicy;

class EmbeddedChildFocusTraversalPolicy extends LayoutFocusTraversalPolicy {
  private static final long serialVersionUID = 0L;
  private final AwtFocusHandler awtHandler;

  EmbeddedChildFocusTraversalPolicy(AwtFocusHandler handler) {
    assert handler != null;
    awtHandler = handler;
  }

  @Override
  public Component getComponentAfter(Container container, Component component) {
    assert container != null;
    assert component != null;
    assert awtHandler != null;
    assert EventQueue.isDispatchThread(); // On AWT event thread
    if (component.equals(getLastComponent(container))) {
      // Instead of cycling around to the first component, transfer to the next SWT component
      awtHandler.transferFocusNext();
      return null;
    } else {
      return super.getComponentAfter(container, component);
    }
  }

  @Override
  public Component getComponentBefore(Container container, Component component) {
    assert container != null;
    assert component != null;
    assert awtHandler != null;
    assert EventQueue.isDispatchThread(); // On AWT event thread
    if (component.equals(getFirstComponent(container))) {
      // Instead of cycling around to the last component, transfer to the previous SWT component
      awtHandler.transferFocusPrevious();
      return null;
    } else {
      return super.getComponentBefore(container, component);
    }
  }

  @Override
  public Component getDefaultComponent(Container container) {
    assert container != null;
    assert awtHandler != null;
    assert EventQueue.isDispatchThread(); // On AWT event thread
    // This is a hack which depends on knowledge of current JDK implementation to 
    // work. The implementation above of getComponentBefore/After
    // properly returns null when transferring to SWT. However, the calling AWT container
    // will then immediately try this method to find the next recipient of
    // focus. But we don't want *any* AWT component to receive focus... it's just
    // been transferred to SWT. So, this method must return null when AWT does 
    // not own the focus. When AWT *does* own the focus, behave normally.  
    if (awtHandler.awtHasFocus()) {
      // System.out.println("getDefault: super");
      return super.getDefaultComponent(container);
    } else {
      // System.out.println("getDefault: null");
      return null;
    }
  }

  public Component getCurrentComponent(Container container) {
    assert container != null;
    assert awtHandler != null;
    assert EventQueue.isDispatchThread(); // On AWT event thread
    Component currentAwtComponent = awtHandler.getCurrentComponent();
    if (currentAwtComponent != null && container.isAncestorOf(currentAwtComponent)) {
      return currentAwtComponent;
    } else {
      return getDefaultComponent(container);
    }
  }
}
