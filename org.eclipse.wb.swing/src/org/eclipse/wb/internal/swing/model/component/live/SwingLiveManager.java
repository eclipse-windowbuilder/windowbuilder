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
package org.eclipse.wb.internal.swing.model.component.live;

import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.clipboard.ComponentInfoMemento;
import org.eclipse.wb.internal.core.model.util.live.AbstractLiveManager;
import org.eclipse.wb.internal.core.model.util.live.ILiveCacheEntry;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.FlowLayoutInfo;

import org.eclipse.swt.graphics.Image;

import java.awt.Component;

import javax.swing.UIManager;

/**
 * Default live components manager for Swing.
 * 
 * @author mitin_aa
 * @coverage swing.model
 */
public class SwingLiveManager extends AbstractLiveManager {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SwingLiveManager(AbstractComponentInfo component) {
    super(component);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // AbstractLiveComponentsManager
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected AbstractComponentInfo createLiveComponent() throws Exception {
    // prepare empty JPanel
    ContainerInfo panel;
    {
      String[] sourceLines =
          new String[]{"  javax.swing.JPanel __wbp_panel = new javax.swing.JPanel();"};
      panel = (ContainerInfo) parse(sourceLines);
    }
    // prepare component
    ComponentInfo component = createClone();
    // add component
    {
      FlowLayoutInfo flowLayoutInfo = (FlowLayoutInfo) panel.getLayout();
      flowLayoutInfo.add(component, null);
    }
    // apply forced size
    {
      String forcedWidthString =
          JavaInfoUtils.getParameter(m_component, "liveComponent.forcedSize.width");
      String forcedHeightString =
          JavaInfoUtils.getParameter(m_component, "liveComponent.forcedSize.height");
      if (forcedWidthString != null && forcedHeightString != null) {
        component.addMethodInvocation("setSize(int,int)", forcedWidthString
            + ", "
            + forcedHeightString);
      }
    }
    return component;
  }

  @Override
  protected String getKey() {
    return super.getKey() + "|" + UIManager.getLookAndFeel().getID();
  }

  @Override
  protected ILiveCacheEntry createComponentCacheEntry(AbstractComponentInfo liveComponentInfo) {
    SwingLiveCacheEntry cacheEntry = new SwingLiveCacheEntry();
    // get component and detach it from parent
    final ComponentInfo componentInfo = (ComponentInfo) liveComponentInfo;
    Component component = componentInfo.getComponent();
    component.getParent().remove(component);
    cacheEntry.setComponent(component);
    // image 
    cacheEntry.setImage(componentInfo.getImage());
    componentInfo.setImage(null);
    // baseline
    cacheEntry.setBaseline(componentInfo.getBaseline());
    // done
    return cacheEntry;
  }

  @Override
  protected ILiveCacheEntry createComponentCacheEntryEx(Throwable e) {
    SwingLiveCacheEntry cacheEntry = new SwingLiveCacheEntry();
    // no component
    cacheEntry.setComponent(null);
    // set image
    {
      Image image = createImageForException(e);
      cacheEntry.setImage(image);
    }
    // done
    return cacheEntry;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public Component getComponent() {
    return ((SwingLiveCacheEntry) getCachedEntry()).getComponent();
  }

  public Image getImage() {
    // get image from memento during paste
    final Image image = ComponentInfoMemento.getImage(m_component);
    if (image != null) {
      return image;
    }
    return ((SwingLiveCacheEntry) getCachedEntry()).getImage();
  }

  public int getBaseline() {
    return ((SwingLiveCacheEntry) getCachedEntry()).getBaseline();
  }
}
