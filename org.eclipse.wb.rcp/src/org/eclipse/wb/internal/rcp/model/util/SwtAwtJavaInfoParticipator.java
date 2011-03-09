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
package org.eclipse.wb.internal.rcp.model.util;

import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.IJavaInfoInitializationParticipator;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.creation.factory.StaticFactoryCreationSupport;
import org.eclipse.wb.internal.core.model.description.factory.FactoryMethodDescription;

import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

import java.util.List;

/**
 * {@link IJavaInfoInitializationParticipator} that supports rendering
 * {@link SWT_AWT#new_Frame(Composite)}.
 * 
 * @author scheglov_ke
 * @coverage rcp.model.util
 */
public final class SwtAwtJavaInfoParticipator implements IJavaInfoInitializationParticipator {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final Object INSTANCE = new SwtAwtJavaInfoParticipator();

  private SwtAwtJavaInfoParticipator() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IJavaInfoInitializationParticipator
  //
  ////////////////////////////////////////////////////////////////////////////
  public void process(JavaInfo javaInfo) throws Exception {
    if (javaInfo.getCreationSupport() instanceof StaticFactoryCreationSupport) {
      StaticFactoryCreationSupport creationSupport =
          (StaticFactoryCreationSupport) javaInfo.getCreationSupport();
      FactoryMethodDescription description = creationSupport.getDescription();
      if (description.getDeclaringClass() == SWT_AWT.class
          && description.getSignature().equals("new_Frame(org.eclipse.swt.widgets.Composite)")) {
        addFrameListeners((AbstractComponentInfo) javaInfo);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implementation
  //
  ////////////////////////////////////////////////////////////////////////////
  private void addFrameListeners(final AbstractComponentInfo newFrame) {
    newFrame.addBroadcastListener(new ObjectEventListener() {
      @Override
      public void refreshed() throws Exception {
        fixFrameBounds();
        drawFrameChildren();
      }

      private void fixFrameBounds() {
        if (newFrame.getParent() instanceof AbstractComponentInfo) {
          Rectangle bounds = newFrame.getBounds();
          AbstractComponentInfo parent = (AbstractComponentInfo) newFrame.getParent();
          Insets clientAreaInsets = parent.getClientAreaInsets();
          bounds.translate(clientAreaInsets.left, clientAreaInsets.top);
        }
      }

      private void drawFrameChildren() {
        List<AbstractComponentInfo> components = newFrame.getChildren(AbstractComponentInfo.class);
        for (AbstractComponentInfo component : components) {
          drawComponent(component);
        }
      }

      private void drawComponent(AbstractComponentInfo component) {
        Point location = component.getBounds().getLocation();
        ObjectInfo parent = component.getParent();
        for (; parent != null; parent = parent.getParent()) {
          if (parent instanceof AbstractComponentInfo) {
            AbstractComponentInfo parentComponent = (AbstractComponentInfo) parent;
            Image parentImage = parentComponent.getImage();
            if (parentImage != null) {
              GC gc = new GC(parentImage);
              try {
                gc.drawImage(component.getImage(), location.x, location.y);
              } finally {
                gc.dispose();
              }
            }
            location.translate(parentComponent.getBounds().getLocation());
          }
        }
      }
    });
  }
}
